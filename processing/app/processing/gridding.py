from pathlib import Path

import numpy as np
import pandas as pd
from pyproj import Transformer
from scipy.interpolate import griddata


class MagneticGridProcessor:

    def __init__(
        self,
        source_crs: str = "EPSG:4326",
        target_crs: str = "EPSG:25835"
    ):

        self.source_crs = source_crs
        self.target_crs = target_crs

        self.transformer = Transformer.from_crs(
            source_crs,
            target_crs,
            always_xy=True
        )

    def process(
        self,
        dataframe: pd.DataFrame,
        output_file: str,
        cell_size_m: float = 12.5
    ) -> pd.DataFrame:

        df = dataframe.copy()

        # -----------------------------------------------
        # 1. Check columns
        # -----------------------------------------------

        required_columns = [
            "lat",
            "lon",
            "tmi_input"
        ]

        missing_columns = [
            column
            for column in required_columns
            if column not in df.columns
        ]

        if missing_columns:
            raise ValueError(
                "Missing required columns: "
                f"{missing_columns}"
            )

        # -----------------------------------------------
        # 2. Convert values to numeric
        # -----------------------------------------------

        latitude = pd.to_numeric(
            df["lat"],
            errors="coerce"
        )

        longitude = pd.to_numeric(
            df["lon"],
            errors="coerce"
        )

        magnetic = pd.to_numeric(
            df["tmi_input"],
            errors="coerce"
        )

        # -----------------------------------------------
        # 3. Keep valid data
        # -----------------------------------------------

        valid = (
            latitude.notna()
            & longitude.notna()
            & magnetic.notna()
        )

        latitude = latitude[valid]
        longitude = longitude[valid]
        magnetic = magnetic[valid]

        if len(magnetic) < 3:
            raise ValueError(
                "Not enough valid survey points "
                "for gridding."
            )

        # -----------------------------------------------
        # 4. Convert WGS84 coordinates to metres
        # -----------------------------------------------

        easting, northing = (
            self.transformer.transform(
                longitude.to_numpy(),
                latitude.to_numpy()
            )
        )

        easting = np.asarray(
            easting,
            dtype=float
        )

        northing = np.asarray(
            northing,
            dtype=float
        )

        magnetic_values = magnetic.to_numpy(
            dtype=float
        )

        # -----------------------------------------------
        # 5. Determine survey bounds
        # -----------------------------------------------

        x_min = float(
            np.min(easting)
        )

        x_max = float(
            np.max(easting)
        )

        y_min = float(
            np.min(northing)
        )

        y_max = float(
            np.max(northing)
        )

        # -----------------------------------------------
        # 6. Create 12.5 m grid
        # -----------------------------------------------

        if cell_size_m <= 0:
            raise ValueError(
                "cell_size_m must be greater than zero."
            )

        x_grid = np.arange(
            x_min,
            x_max + cell_size_m,
            cell_size_m
        )

        y_grid = np.arange(
            y_min,
            y_max + cell_size_m,
            cell_size_m
        )

        grid_x, grid_y = np.meshgrid(
            x_grid,
            y_grid
        )

        # -----------------------------------------------
        # 7. Interpolate magnetic field
        # -----------------------------------------------

        grid_tmi = griddata(
            (
                easting,
                northing
            ),
            magnetic_values,
            (
                grid_x,
                grid_y
            ),
            method="linear"
        )

        # -----------------------------------------------
        # 8. Fill outside convex hull
        # -----------------------------------------------

        if np.isnan(grid_tmi).any():

            nearest = griddata(
                (
                    easting,
                    northing
                ),
                magnetic_values,
                (
                    grid_x,
                    grid_y
                ),
                method="nearest"
            )

            grid_tmi = np.where(
                np.isnan(grid_tmi),
                nearest,
                grid_tmi
            )

        # -----------------------------------------------
        # 9. Create survey-relative anomaly grid
        # -----------------------------------------------

        reference = float(
            np.median(magnetic_values)
        )

        grid_relative_anomaly = (
            grid_tmi - reference
        )

        # -----------------------------------------------
        # 10. Create output dataframe
        # -----------------------------------------------

        grid_dataframe = pd.DataFrame({
            "easting": grid_x.ravel(),
            "northing": grid_y.ravel(),
            "tmi": grid_tmi.ravel(),
            "relative_magnetic_anomaly":
                grid_relative_anomaly.ravel(),
        })

        grid_dataframe["crs"] = (
            self.target_crs
        )

        grid_dataframe["cell_size_m"] = (
            cell_size_m
        )

        # -----------------------------------------------
        # 11. Save grid
        # -----------------------------------------------

        output_path = Path(
            output_file
        )

        output_path.parent.mkdir(
            parents=True,
            exist_ok=True
        )

        grid_dataframe.to_csv(
            output_path,
            index=False
        )

        return grid_dataframe