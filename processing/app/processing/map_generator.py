from pathlib import Path

import matplotlib

matplotlib.use("Agg")

import matplotlib.pyplot as plt
import numpy as np
import rasterio
from rasterio.transform import from_origin


class MagneticMapGenerator:

    BAND_NAMES = [
        "TMI",
        "RTP",
        "Residual_TMI",
        "Regional_TMI",
        "VD1",
        "VD2",
        "HoriG",
        "TotG",
        "AnalyticSignal",
        "TiltG"
    ]

    def generate(
        self,
        layers: dict,
        eastings: np.ndarray,
        northings: np.ndarray,
        geotiff_file: str,
        preview_file: str
    ) -> dict:

        geotiff_path = Path(
            geotiff_file
        )

        preview_path = Path(
            preview_file
        )

        geotiff_path.parent.mkdir(
            parents=True,
            exist_ok=True
        )

        preview_path.parent.mkdir(
            parents=True,
            exist_ok=True
        )

        # --------------------------------------------------
        # Grid dimensions
        # --------------------------------------------------

        tmi = np.asarray(
            layers["TMI"],
            dtype=float
        )

        ny, nx = tmi.shape

        # --------------------------------------------------
        # Cell size
        # --------------------------------------------------

        if len(eastings) > 1:

            cell_x = float(
                np.median(
                    np.diff(
                        eastings
                    )
                )
            )

        else:

            cell_x = 12.5

        if len(northings) > 1:

            cell_y = float(
                np.median(
                    np.diff(
                        northings
                    )
                )
            )

        else:

            cell_y = 12.5

        # --------------------------------------------------
        # Raster origin
        # --------------------------------------------------

        west = float(
            eastings.min()
        )

        east = float(
            eastings.max()
        ) + cell_x

        south = float(
            northings.min()
        )

        north = float(
            northings.max()
        ) + cell_y

        transform = from_origin(
            west,
            north,
            cell_x,
            cell_y
        )

        # --------------------------------------------------
        # Write multi-band GeoTIFF
        # --------------------------------------------------

        with rasterio.open(
            geotiff_path,
            "w",
            driver="GTiff",
            height=ny,
            width=nx,
            count=len(
                self.BAND_NAMES
            ),
            dtype="float32",
            crs="EPSG:25835",
            transform=transform,
            nodata=np.nan,
            compress="deflate"
        ) as dataset:

            for band_number, name in enumerate(
                self.BAND_NAMES,
                start=1
            ):

                array = np.asarray(
                    layers[name],
                    dtype=np.float32
                )

                # Raster convention expects northern rows first.
                array = np.flipud(
                    array
                )

                dataset.write(
                    array,
                    band_number
                )

                dataset.set_band_description(
                    band_number,
                    name
                )

        # --------------------------------------------------
        # Create RTP preview
        # --------------------------------------------------

        rtp = np.asarray(
            layers["RTP"],
            dtype=float
        )

        rtp_display = np.flipud(
            rtp
        )

        extent = [

            west,

            east,

            south,

            north
        ]

        fig, ax = plt.subplots(
            figsize=(10, 8)
        )

        image = ax.imshow(
            rtp_display,
            extent=extent,
            origin="upper",
            aspect="equal"
        )

        colorbar = fig.colorbar(
            image,
            ax=ax,
            shrink=0.8
        )

        colorbar.set_label(
            "RTP Magnetic Field"
        )

        ax.set_title(
            "UAV Magnetic RTP Map"
        )

        ax.set_xlabel(
            "Easting (m) — EPSG:25835"
        )

        ax.set_ylabel(
            "Northing (m) — EPSG:25835"
        )

        fig.tight_layout()

        fig.savefig(
            preview_path,
            dpi=200
        )

        plt.close(fig)

        return {

            "geotiff_file":
                str(geotiff_path),

            "preview_file":
                str(preview_path),

            "width":
                nx,

            "height":
                ny,

            "cell_size_x_m":
                cell_x,

            "cell_size_y_m":
                cell_y,

            "west":
                west,

            "east":
                east,

            "south":
                south,

            "north":
                north,

            "crs":
                "EPSG:25835",

            "bands":
                self.BAND_NAMES
        }