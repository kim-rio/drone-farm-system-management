from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path

import numpy as np
from scipy import ndimage


@dataclass
class MagneticAnomaly:

    id: int

    easting: float

    northing: float

    peak_residual_nt: float

    max_analytic_signal: float

    area_m2: float

    equivalent_radius_m: float

    estimated_depth_m: float

    pixel_x: int

    pixel_y: int


class MagneticAnomalyDetector:

    def __init__(
        self,
        cell_size_m: float = 12.5,
        threshold_sigma: float = 2.5,
        minimum_area_m2: float = 250.0,
        edge_buffer_m: float = 50.0
    ):

        self.cell_size_m = cell_size_m

        self.threshold_sigma = (
            threshold_sigma
        )

        self.minimum_area_m2 = (
            minimum_area_m2
        )

        self.edge_buffer_m = (
            edge_buffer_m
        )

    # =========================================================
    # MAIN DETECTOR
    # =========================================================

    def detect(
        self,
        layers: dict
    ) -> list[MagneticAnomaly]:

        required_layers = [
            "TMI",
            "Residual_TMI",
            "AnalyticSignal"
        ]

        missing_layers = [
            layer
            for layer in required_layers
            if layer not in layers
        ]

        if missing_layers:
            raise ValueError(
                "Missing required geophysical layers: "
                f"{missing_layers}"
            )

        tmi = np.asarray(
            layers["TMI"],
            dtype=float
        )

        residual = np.asarray(
            layers["Residual_TMI"],
            dtype=float
        )

        analytic_signal = np.asarray(
            layers["AnalyticSignal"],
            dtype=float
        )

        if (
            tmi.shape
            != residual.shape
            or
            residual.shape
            != analytic_signal.shape
        ):
            raise ValueError(
                "TMI, residual and analytic-signal "
                "grids must have identical dimensions."
            )

        residual = np.nan_to_num(
            residual,
            nan=0.0,
            posinf=0.0,
            neginf=0.0
        )

        analytic_signal = np.nan_to_num(
            analytic_signal,
            nan=0.0,
            posinf=0.0,
            neginf=0.0
        )

        # -----------------------------------------------------
        # Light smoothing.
        # -----------------------------------------------------

        residual_smoothed = (
            ndimage.gaussian_filter(
                residual,
                sigma=1.0
            )
        )

        # -----------------------------------------------------
        # Robust MAD statistics.
        # -----------------------------------------------------

        residual_median = float(
            np.median(
                residual_smoothed
            )
        )

        absolute_deviation = np.abs(
            residual_smoothed
            - residual_median
        )

        mad = float(
            np.median(
                absolute_deviation
            )
        )

        if mad > 0:

            robust_scale = (
                1.4826
                * mad
            )

            anomaly_score = (
                np.abs(
                    residual_smoothed
                    - residual_median
                )
                / robust_scale
            )

        else:

            standard_deviation = float(
                np.std(
                    residual_smoothed
                )
            )

            if standard_deviation > 0:

                anomaly_score = (
                    np.abs(
                        residual_smoothed
                        - residual_median
                    )
                    / standard_deviation
                )

            else:

                anomaly_score = np.zeros_like(
                    residual_smoothed
                )

        # -----------------------------------------------------
        # Detect both positive and negative anomalies.
        # -----------------------------------------------------

        mask = (
            anomaly_score
            >= self.threshold_sigma
        )

        # -----------------------------------------------------
        # Close small gaps.
        # -----------------------------------------------------

        structure = np.ones(
            (3, 3),
            dtype=bool
        )

        mask = ndimage.binary_closing(
            mask,
            structure=structure,
            iterations=1
        )

        mask = ndimage.binary_fill_holes(
            mask
        )

        # -----------------------------------------------------
        # Connected components.
        # -----------------------------------------------------

        labels, region_count = ndimage.label(
            mask,
            structure=structure
        )

        ny, nx = residual.shape

        edge_pixels = max(
            0,
            int(
                round(
                    self.edge_buffer_m
                    / self.cell_size_m
                )
            )
        )

        pixel_area = (
            self.cell_size_m
            ** 2
        )

        anomalies = []

        # -----------------------------------------------------
        # Analyze each region.
        # -----------------------------------------------------

        for region_id in range(
            1,
            region_count + 1
        ):

            region_mask = (
                labels == region_id
            )

            pixel_count = int(
                np.count_nonzero(
                    region_mask
                )
            )

            area_m2 = (
                pixel_count
                * pixel_area
            )

            if area_m2 < (
                self.minimum_area_m2
            ):
                continue

            ys, xs = np.where(
                region_mask
            )

            if len(xs) == 0:
                continue

            # Strongest analytic-signal point.
            signal_values = (
                analytic_signal[
                    ys,
                    xs
                ]
            )

            peak_position = int(
                np.argmax(
                    signal_values
                )
            )

            peak_y = int(
                ys[
                    peak_position
                ]
            )

            peak_x = int(
                xs[
                    peak_position
                ]
            )

            # -------------------------------------------------
            # Ignore edge artefacts.
            # -------------------------------------------------

            if (
                peak_x < edge_pixels
                or
                peak_x >= (
                    nx - edge_pixels
                )
                or
                peak_y < edge_pixels
                or
                peak_y >= (
                    ny - edge_pixels
                )
            ):
                continue

            peak_residual = float(
                residual[
                    peak_y,
                    peak_x
                ]
            )

            maximum_analytic_signal = float(
                analytic_signal[
                    peak_y,
                    peak_x
                ]
            )

            equivalent_radius = float(
                np.sqrt(
                    area_m2
                    / np.pi
                )
            )

            estimated_depth = (
                self._estimate_depth(
                    analytic_signal,
                    peak_x,
                    peak_y,
                    maximum_analytic_signal
                )
            )

            anomalies.append(
                MagneticAnomaly(

                    id=region_id,

                    easting=0.0,

                    northing=0.0,

                    peak_residual_nt=(
                        peak_residual
                    ),

                    max_analytic_signal=(
                        maximum_analytic_signal
                    ),

                    area_m2=(
                        float(area_m2)
                    ),

                    equivalent_radius_m=(
                        equivalent_radius
                    ),

                    estimated_depth_m=(
                        estimated_depth
                    ),

                    pixel_x=peak_x,

                    pixel_y=peak_y
                )
            )

        # -----------------------------------------------------
        # If strict residual detection returns nothing,
        # use analytic-signal candidate detection.
        # -----------------------------------------------------

        if not anomalies:

            anomalies = (
                self._fallback_detection(
                    residual,
                    analytic_signal
                )
            )

        # -----------------------------------------------------
        # Strongest candidate first.
        # -----------------------------------------------------

        anomalies.sort(
            key=lambda anomaly:
                anomaly.max_analytic_signal,
            reverse=True
        )

        # -----------------------------------------------------
        # Re-number.
        # -----------------------------------------------------

        for number, anomaly in enumerate(
            anomalies,
            start=1
        ):

            anomaly.id = number

        return anomalies

    # =========================================================
    # FALLBACK DETECTION
    # =========================================================

    def _fallback_detection(
        self,
        residual: np.ndarray,
        analytic_signal: np.ndarray
    ) -> list[MagneticAnomaly]:

        ny, nx = analytic_signal.shape

        # -----------------------------------------------------
        # Select strongest 3% of analytic-signal values.
        # -----------------------------------------------------

        threshold = float(
            np.percentile(
                analytic_signal,
                97.0
            )
        )

        mask = (
            analytic_signal
            >= threshold
        )

        structure = np.ones(
            (5, 5),
            dtype=bool
        )

        mask = ndimage.binary_closing(
            mask,
            structure=structure,
            iterations=2
        )

        mask = ndimage.binary_fill_holes(
            mask
        )

        labels, count = ndimage.label(
            mask,
            structure=structure
        )

        anomalies = []

        pixel_area = (
            self.cell_size_m
            ** 2
        )

        edge_pixels = max(
            0,
            int(
                round(
                    self.edge_buffer_m
                    / self.cell_size_m
                )
            )
        )

        minimum_pixels = max(
            2,
            int(
                np.ceil(
                    self.minimum_area_m2
                    / pixel_area
                )
            )
        )

        for region_id in range(
            1,
            count + 1
        ):

            region = (
                labels == region_id
            )

            pixel_count = int(
                region.sum()
            )

            if pixel_count < minimum_pixels:
                continue

            ys, xs = np.where(
                region
            )

            if len(xs) == 0:
                continue

            signal_values = (
                analytic_signal[
                    ys,
                    xs
                ]
            )

            peak_position = int(
                np.argmax(
                    signal_values
                )
            )

            peak_y = int(
                ys[
                    peak_position
                ]
            )

            peak_x = int(
                xs[
                    peak_position
                ]
            )

            if (
                peak_x < edge_pixels
                or
                peak_x >= (
                    nx - edge_pixels
                )
                or
                peak_y < edge_pixels
                or
                peak_y >= (
                    ny - edge_pixels
                )
            ):
                continue

            area_m2 = (
                pixel_count
                * pixel_area
            )

            peak_as = float(
                analytic_signal[
                    peak_y,
                    peak_x
                ]
            )

            peak_residual = float(
                residual[
                    peak_y,
                    peak_x
                ]
            )

            equivalent_radius = float(
                np.sqrt(
                    area_m2
                    / np.pi
                )
            )

            estimated_depth = (
                self._estimate_depth(
                    analytic_signal,
                    peak_x,
                    peak_y,
                    peak_as
                )
            )

            anomalies.append(
                MagneticAnomaly(

                    id=region_id,

                    easting=0.0,

                    northing=0.0,

                    peak_residual_nt=(
                        peak_residual
                    ),

                    max_analytic_signal=(
                        peak_as
                    ),

                    area_m2=(
                        area_m2
                    ),

                    equivalent_radius_m=(
                        equivalent_radius
                    ),

                    estimated_depth_m=(
                        estimated_depth
                    ),

                    pixel_x=peak_x,

                    pixel_y=peak_y
                )
            )

        return anomalies

    # =========================================================
    # DEPTH ESTIMATION
    # =========================================================

    def _estimate_depth(
        self,
        analytic_signal: np.ndarray,
        peak_x: int,
        peak_y: int,
        peak_value: float
    ) -> float:

        if (
            peak_value <= 0
            or not np.isfinite(
                peak_value
            )
        ):
            return self.cell_size_m

        half_value = (
            peak_value
            / 2.0
        )

        ny, nx = (
            analytic_signal.shape
        )

        # Search over 300 m around the peak.
        search_radius_m = 300.0

        search_radius_pixels = max(
            8,
            int(
                round(
                    search_radius_m
                    / self.cell_size_m
                )
            )
        )

        x_min = max(
            0,
            peak_x
            - search_radius_pixels
        )

        x_max = min(
            nx,
            peak_x
            + search_radius_pixels
            + 1
        )

        y_min = max(
            0,
            peak_y
            - search_radius_pixels
        )

        y_max = min(
            ny,
            peak_y
            + search_radius_pixels
            + 1
        )

        local = analytic_signal[
            y_min:y_max,
            x_min:x_max
        ]

        local_x = (
            peak_x
            - x_min
        )

        local_y = (
            peak_y
            - y_min
        )

        half_widths = []

        # -----------------------------------------------------
        # X profile
        # -----------------------------------------------------

        if (
            0 <= local_x < local.shape[1]
            and
            0 <= local_y < local.shape[0]
        ):

            profile_x = (
                local[
                    local_y,
                    :
                ]
            )

            above_x = np.where(
                profile_x >= half_value
            )[0]

            if len(above_x) >= 2:

                width_x_pixels = (
                    above_x[-1]
                    - above_x[0]
                )

                half_width_x = (
                    width_x_pixels
                    * self.cell_size_m
                    / 2.0
                )

                half_widths.append(
                    half_width_x
                )

        # -----------------------------------------------------
        # Y profile
        # -----------------------------------------------------

        if (
            0 <= local_x < local.shape[1]
            and
            0 <= local_y < local.shape[0]
        ):

            profile_y = (
                local[
                    :,
                    local_x
                ]
            )

            above_y = np.where(
                profile_y >= half_value
            )[0]

            if len(above_y) >= 2:

                width_y_pixels = (
                    above_y[-1]
                    - above_y[0]
                )

                half_width_y = (
                    width_y_pixels
                    * self.cell_size_m
                    / 2.0
                )

                half_widths.append(
                    half_width_y
                )

        # -----------------------------------------------------
        # Estimate from measured half-width.
        # -----------------------------------------------------

        if len(half_widths) == 2:

            depth = float(
                np.mean(
                    half_widths
                )
            )

        elif len(half_widths) == 1:

            depth = float(
                half_widths[0]
            )

        else:

            # No valid half-width measurement.
            # Use equivalent-radius scale as a fallback.
            depth = self.cell_size_m

        return max(
            depth,
            self.cell_size_m
        )

    # =========================================================
    # ASSIGN PROJECTED COORDINATES
    # =========================================================

    def assign_coordinates(
        self,
        anomalies: list[MagneticAnomaly],
        eastings: np.ndarray,
        northings: np.ndarray
    ) -> list[MagneticAnomaly]:

        if (
            len(eastings) == 0
            or len(northings) == 0
        ):
            return anomalies

        nx = len(
            eastings
        )

        ny = len(
            northings
        )

        for number, anomaly in enumerate(
            anomalies,
            start=1
        ):

            pixel_x = max(
                0,
                min(
                    anomaly.pixel_x,
                    nx - 1
                )
            )

            pixel_y = max(
                0,
                min(
                    anomaly.pixel_y,
                    ny - 1
                )
            )

            anomaly.id = number

            anomaly.easting = float(
                eastings[pixel_x]
            )

            anomaly.northing = float(
                northings[pixel_y]
            )

            anomaly.pixel_x = (
                pixel_x
            )

            anomaly.pixel_y = (
                pixel_y
            )

        return anomalies

    # =========================================================
    # SAVE GEOJSON
    # =========================================================

    def save_geojson(
        self,
        anomalies: list[MagneticAnomaly],
        output_file: str
    ) -> str:

        from pyproj import Transformer

        transformer = (
            Transformer.from_crs(
                "EPSG:25835",
                "EPSG:4326",
                always_xy=True
            )
        )

        features = []

        for anomaly in anomalies:

            longitude, latitude = (
                transformer.transform(
                    anomaly.easting,
                    anomaly.northing
                )
            )

            features.append({

                "type":
                    "Feature",

                "geometry": {

                    "type":
                        "Point",

                    "coordinates": [
                        longitude,
                        latitude
                    ]
                },

                "properties": {

                    "id":
                        anomaly.id,

                    "peak_residual_nT":
                        round(
                            anomaly.peak_residual_nt,
                            3
                        ),

                    "max_analytic_signal":
                        round(
                            anomaly.max_analytic_signal,
                            3
                        ),

                    "area_m2":
                        round(
                            anomaly.area_m2,
                            2
                        ),

                    "equivalent_radius_m":
                        round(
                            anomaly.equivalent_radius_m,
                            2
                        ),

                    "estimated_depth_m":
                        round(
                            anomaly.estimated_depth_m,
                            2
                        ),

                    "interpretation_status":
                        "CANDIDATE",

                    "requires_geologist_review":
                        True
                }
            })

        geojson = {

            "type":
                "FeatureCollection",

            "features":
                features
        }

        output_path = Path(
            output_file
        )

        output_path.parent.mkdir(
            parents=True,
            exist_ok=True
        )

        with open(
            output_path,
            "w",
            encoding="utf-8"
        ) as file:

            json.dump(
                geojson,
                file,
                indent=2
            )

        return str(
            output_path
        )