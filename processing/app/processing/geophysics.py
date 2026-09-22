from __future__ import annotations

from typing import Any

import numpy as np
import pandas as pd
import pyIGRF14 as pyIGRF
from scipy import ndimage


class GeophysicalProcessor:

    def __init__(
        self,
        cell_size_m: float = 12.5
    ):

        self.cell_size_m = cell_size_m

    # ==========================================================
    # PUBLIC METHOD
    # ==========================================================

    def process(
        self,
        dataframe: pd.DataFrame,
        grid_dataframe: pd.DataFrame
    ) -> dict[str, Any]:

        # ------------------------------------------------------
        # 1. Build regular TMI grid
        # ------------------------------------------------------

        grid, eastings, northings = (
            self._build_grid(
                grid_dataframe
            )
        )

        # ------------------------------------------------------
        # 2. Determine survey location
        # ------------------------------------------------------

        latitude, longitude = (
            self._survey_location(
                dataframe
            )
        )

        # ------------------------------------------------------
        # 3. Determine survey date
        # ------------------------------------------------------

        survey_year = (
            self._survey_year(
                dataframe
            )
        )

        # ------------------------------------------------------
        # 4. Obtain IGRF magnetic field
        # ------------------------------------------------------

        igrf = self._calculate_igrf(
            latitude,
            longitude,
            survey_year
        )

        inclination = igrf["inclination_deg"]
        declination = igrf["declination_deg"]

        # ------------------------------------------------------
        # 5. Prepare grid for spectral processing
        # ------------------------------------------------------

        processing_grid = (
            self._prepare_grid(
                grid
            )
        )

        # ------------------------------------------------------
        # 6. Calculate magnetic derivatives
        # ------------------------------------------------------

        derivatives = (
            self._calculate_derivatives(
                processing_grid
            )
        )

        # ------------------------------------------------------
        # 7. Reduction to pole
        # ------------------------------------------------------

        rtp = (
            self._reduction_to_pole(
                processing_grid,
                inclination,
                declination
            )
        )

        # ------------------------------------------------------
        # 8. Regional/residual separation
        # ------------------------------------------------------

        regional, residual = (
            self._regional_residual(
                processing_grid
            )
        )

        # ------------------------------------------------------
        # 9. Build analytic signal
        # ------------------------------------------------------

        analytic_signal = (
            np.sqrt(
                derivatives["dx"] ** 2
                + derivatives["dy"] ** 2
                + derivatives["vd1"] ** 2
            )
        )

        # ------------------------------------------------------
        # 10. Horizontal gradient
        # ------------------------------------------------------

        horizontal_gradient = (
            np.sqrt(
                derivatives["dx"] ** 2
                + derivatives["dy"] ** 2
            )
        )

        # ------------------------------------------------------
        # 11. Total gradient
        # ------------------------------------------------------

        total_gradient = (
            np.sqrt(
                derivatives["dx"] ** 2
                + derivatives["dy"] ** 2
                + derivatives["vd1"] ** 2
            )
        )

        # ------------------------------------------------------
        # 12. Tilt derivative
        # ------------------------------------------------------

        tilt = np.arctan2(
            derivatives["vd1"],
            horizontal_gradient + 1e-12
        )

        # ------------------------------------------------------
        # 13. Create feature layers
        # ------------------------------------------------------

        layers = {
            "TMI": processing_grid,
            "RTP": rtp,
            "Residual_TMI": residual,
            "Regional_TMI": regional,
            "VD1": derivatives["vd1"],
            "VD2": derivatives["vd2"],
            "HoriG": horizontal_gradient,
            "TotG": total_gradient,
            "AnalyticSignal": analytic_signal,
            "TiltG": tilt
        }

        # ------------------------------------------------------
        # 14. Convert back into flat dataframe
        # ------------------------------------------------------

        feature_dataframe = (
            self._layers_to_dataframe(
                eastings,
                northings,
                layers
            )
        )

        # ------------------------------------------------------
        # 15. Metadata
        # ------------------------------------------------------

        metadata = {

            "crs": "EPSG:25835",

            "cell_size_m": (
                self.cell_size_m
            ),

            "survey_latitude": (
                latitude
            ),

            "survey_longitude": (
                longitude
            ),

            "survey_year": (
                survey_year
            ),

            "igrf_declination_deg": (
                declination
            ),

            "igrf_inclination_deg": (
                inclination
            ),

            "igrf_total_intensity_nt": (
                igrf["total_intensity_nt"]
            ),

            "processing_note": (
                "FFT-derived magnetic features. "
                "Residual_TMI is survey-grid regional/residual "
                "separation. Geological interpretation remains "
                "subject to geologist review."
            )
        }

        return {
            "feature_dataframe": feature_dataframe,
            "layers": layers,
            "metadata": metadata
        }

    # ==========================================================
    # GRID
    # ==========================================================

    def _build_grid(
        self,
        grid_dataframe: pd.DataFrame
    ):

        required = [
            "easting",
            "northing",
            "tmi"
        ]

        missing = [
            column
            for column in required
            if column not in grid_dataframe.columns
        ]

        if missing:

            raise ValueError(
                f"Grid is missing columns: {missing}"
            )

        df = grid_dataframe.copy()

        df["easting"] = pd.to_numeric(
            df["easting"],
            errors="coerce"
        )

        df["northing"] = pd.to_numeric(
            df["northing"],
            errors="coerce"
        )

        df["tmi"] = pd.to_numeric(
            df["tmi"],
            errors="coerce"
        )

        df = df.dropna(
            subset=[
                "easting",
                "northing",
                "tmi"
            ]
        )

        eastings = np.sort(
            df["easting"].unique()
        )

        northings = np.sort(
            df["northing"].unique()
        )

        pivot = (
            df.pivot(
                index="northing",
                columns="easting",
                values="tmi"
            )
            .reindex(
                index=northings,
                columns=eastings
            )
        )

        grid = pivot.to_numpy(
            dtype=float
        )

        if np.isnan(grid).any():

            filled = ndimage.generic_filter(
                grid,
                function=lambda values:
                    np.nanmean(values),
                size=3,
                mode="nearest"
            )

            grid = np.where(
                np.isnan(grid),
                filled,
                grid
            )

        if np.isnan(grid).any():

            grid = np.nan_to_num(
                grid,
                nan=float(
                    np.nanmedian(grid)
                )
            )

        return (
            grid,
            eastings,
            northings
        )

    # ==========================================================
    # SURVEY LOCATION
    # ==========================================================

    def _survey_location(
        self,
        dataframe: pd.DataFrame
    ):

        latitude_column = self._find_column(
            dataframe,
            [
                "lat",
                "latitude"
            ]
        )

        longitude_column = self._find_column(
            dataframe,
            [
                "lon",
                "longitude"
            ]
        )

        if (
            latitude_column is None
            or longitude_column is None
        ):

            raise ValueError(
                "Cannot determine survey latitude "
                "and longitude."
            )

        latitude = pd.to_numeric(
            dataframe[latitude_column],
            errors="coerce"
        )

        longitude = pd.to_numeric(
            dataframe[longitude_column],
            errors="coerce"
        )

        latitude = latitude.dropna()
        longitude = longitude.dropna()

        if latitude.empty or longitude.empty:

            raise ValueError(
                "Survey contains no valid coordinates."
            )

        return (
            float(latitude.mean()),
            float(longitude.mean())
        )

    # ==========================================================
    # SURVEY YEAR
    # ==========================================================

    def _survey_year(
        self,
        dataframe: pd.DataFrame
    ) -> float:

        year_column = self._find_column(
            dataframe,
            [
                "year"
            ]
        )

        if year_column is None:

            # Laanila reference data is from 2021.
            return 2021.33

        values = pd.to_numeric(
            dataframe[year_column],
            errors="coerce"
        )

        values = values.dropna()

        if values.empty:

            return 2021.33

        return float(
            values.mean()
        )

    # ==========================================================
    # IGRF
    # ==========================================================

    def _calculate_igrf(
        self,
        latitude: float,
        longitude: float,
        survey_year: float
    ) -> dict:

        result = pyIGRF.igrf_value(
            latitude,
            longitude,
            0.0,
            survey_year
        )

        # pyIGRF:
        # D, I, H, X, Y, Z, F

        declination = float(
            result[0]
        )

        inclination = float(
            result[1]
        )

        total_intensity = float(
            result[6]
        )

        return {

            "declination_deg":
                declination,

            "inclination_deg":
                inclination,

            "total_intensity_nt":
                total_intensity
        }

    # ==========================================================
    # GRID PREPARATION
    # ==========================================================

    def _prepare_grid(
        self,
        grid: np.ndarray
    ) -> np.ndarray:

        result = grid.copy()

        # Remove extreme numerical spikes only for
        # spectral stability. The original TMI is preserved.
        median = np.median(
            result
        )

        mad = np.median(
            np.abs(
                result - median
            )
        )

        if mad > 0:

            robust_z = (
                0.6745
                * (result - median)
                / mad
            )

            extreme = np.abs(
                robust_z
            ) > 8.0

            filtered = ndimage.median_filter(
                result,
                size=3
            )

            result = np.where(
                extreme,
                filtered,
                result
            )

        return result

    # ==========================================================
    # WAVENUMBERS
    # ==========================================================

    def _wavenumbers(
        self,
        grid: np.ndarray
    ):

        ny, nx = grid.shape

        kx = (
            2.0
            * np.pi
            * np.fft.fftfreq(
                nx,
                d=self.cell_size_m
            )
        )

        ky = (
            2.0
            * np.pi
            * np.fft.fftfreq(
                ny,
                d=self.cell_size_m
            )
        )

        kx_grid, ky_grid = np.meshgrid(
            kx,
            ky
        )

        k = np.sqrt(
            kx_grid ** 2
            + ky_grid ** 2
        )

        return (
            kx_grid,
            ky_grid,
            k
        )

    # ==========================================================
    # DERIVATIVES
    # ==========================================================

    def _calculate_derivatives(
        self,
        grid: np.ndarray
    ) -> dict:

        kx, ky, k = (
            self._wavenumbers(
                grid
            )
        )

        # Cosine taper reduces edge discontinuities.
        tapered = self._taper(
            grid
        )

        spectrum = np.fft.fft2(
            tapered
        )

        dx = np.real(
            np.fft.ifft2(
                spectrum
                * (1j * kx)
            )
        )

        dy = np.real(
            np.fft.ifft2(
                spectrum
                * (1j * ky)
            )
        )

        safe_k = np.where(
            k == 0,
            0.0,
            k
        )

        vd1 = np.real(
            np.fft.ifft2(
                spectrum
                * safe_k
            )
        )

        vd2 = np.real(
            np.fft.ifft2(
                spectrum
                * safe_k ** 2
            )
        )

        return {

            "dx": dx,

            "dy": dy,

            "vd1": vd1,

            "vd2": vd2
        }

    # ==========================================================
    # REDUCTION TO POLE
    # ==========================================================

    def _reduction_to_pole(
        self,
        grid: np.ndarray,
        inclination_deg: float,
        declination_deg: float
    ) -> np.ndarray:

        kx, ky, k = (
            self._wavenumbers(
                grid
            )
        )

        inclination = np.radians(
            inclination_deg
        )

        declination = np.radians(
            declination_deg
        )

        # Directional wavenumber.
        directional = (
            kx
            * np.cos(declination)
            * np.cos(inclination)
            +
            ky
            * np.sin(declination)
            * np.cos(inclination)
            +
            1j
            * k
            * np.sin(inclination)
        )

        denominator = (
            directional ** 2
        )

        epsilon = 1e-10

        denominator = np.where(
            np.abs(denominator) < epsilon,
            epsilon + 0j,
            denominator
        )

        numerator = (
            k ** 2
        )

        filter_rtp = (
            numerator
            / denominator
        )

        filter_rtp = np.nan_to_num(
            filter_rtp,
            nan=1.0,
            posinf=1.0,
            neginf=1.0
        )

        spectrum = np.fft.fft2(
            self._taper(grid)
        )

        rtp = np.real(
            np.fft.ifft2(
                spectrum
                * filter_rtp
            )
        )

        return rtp

    # ==========================================================
    # REGIONAL / RESIDUAL
    # ==========================================================

    def _regional_residual(
        self,
        grid: np.ndarray
    ):

        # Approximately 400 m regional scale.
        sigma_pixels = max(
            400.0
            / self.cell_size_m
            / 2.0,
            1.0
        )

        regional = (
            ndimage.gaussian_filter(
                grid,
                sigma=sigma_pixels
            )
        )

        residual = (
            grid - regional
        )

        return (
            regional,
            residual
        )

    # ==========================================================
    # TAPER
    # ==========================================================

    def _taper(
        self,
        grid: np.ndarray
    ) -> np.ndarray:

        ny, nx = grid.shape

        x = np.linspace(
            0.0,
            np.pi,
            nx
        )

        y = np.linspace(
            0.0,
            np.pi,
            ny
        )

        wx = (
            0.5
            * (1.0 - np.cos(x))
        )

        wy = (
            0.5
            * (1.0 - np.cos(y))
        )

        window = np.outer(
            wy,
            wx
        )

        # Keep the taper moderate.
        window = (
            0.25
            + 0.75 * window
        )

        return (
            grid * window
        )

    # ==========================================================
    # LAYERS -> DATAFRAME
    # ==========================================================

    def _layers_to_dataframe(
        self,
        eastings: np.ndarray,
        northings: np.ndarray,
        layers: dict
    ) -> pd.DataFrame:

        east_grid, north_grid = (
            np.meshgrid(
                eastings,
                northings
            )
        )

        result = pd.DataFrame({
            "easting":
                east_grid.ravel(),

            "northing":
                north_grid.ravel()
        })

        for name, values in layers.items():

            result[name] = (
                values.ravel()
            )

        result["crs"] = (
            "EPSG:25835"
        )

        result["cell_size_m"] = (
            self.cell_size_m
        )

        return result

    # ==========================================================
    # COLUMN FINDER
    # ==========================================================

    @staticmethod
    def _find_column(
        dataframe: pd.DataFrame,
        candidates: list[str]
    ) -> str | None:

        lowered = {
            column.lower(): column
            for column in dataframe.columns
        }

        for candidate in candidates:

            if candidate.lower() in lowered:

                return lowered[
                    candidate.lower()
                ]

        return None