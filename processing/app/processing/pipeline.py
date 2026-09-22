from pathlib import Path
import json

import numpy as np

from app.models.processing_result import (
    ProcessingResult
)

from app.processing.uav_parser import (
    UavParser
)

from app.processing.quality import (
    SurveyQualityChecker
)

from app.processing.cleaning import (
    SurveyCleaner
)

from app.processing.magnetic import (
    MagneticProcessor
)

from app.processing.correction import (
    MagneticCorrectionProcessor
)

from app.processing.gridding import (
    MagneticGridProcessor
)

from app.processing.geophysics import (
    GeophysicalProcessor
)

from app.processing.anomaly import (
    MagneticAnomalyDetector
)

from app.processing.map_generator import (
    MagneticMapGenerator
)


class SurveyProcessingPipeline:

    def __init__(self):

        # =====================================================
        # Parser
        # =====================================================

        self.parser = (
            UavParser()
        )

        # =====================================================
        # Quality checker
        # =====================================================

        self.quality_checker = (
            SurveyQualityChecker()
        )

        # =====================================================
        # Cleaner
        # =====================================================

        self.cleaner = (
            SurveyCleaner()
        )

        # =====================================================
        # Magnetic processor
        # =====================================================

        self.magnetic_processor = (
            MagneticProcessor()
        )

        # =====================================================
        # Magnetic correction/source selector
        # =====================================================

        self.correction_processor = (
            MagneticCorrectionProcessor()
        )

        # =====================================================
        # Spatial grid
        # =====================================================

        self.grid_processor = (
            MagneticGridProcessor(
                source_crs="EPSG:4326",
                target_crs="EPSG:25835"
            )
        )

        # =====================================================
        # Geophysical processor
        # =====================================================

        self.geophysical_processor = (
            GeophysicalProcessor(
                cell_size_m=12.5
            )
        )

        # =====================================================
        # Anomaly detector
        # =====================================================

        self.anomaly_detector = (
            MagneticAnomalyDetector(
                cell_size_m=12.5,
                threshold_sigma=2.5,
                minimum_area_m2=250.0,
                edge_buffer_m=50.0
            )
        )

        # =====================================================
        # Map generator
        # =====================================================

        self.map_generator = (
            MagneticMapGenerator()
        )

    def process(
        self,
        input_file: str,
        output_file: str
    ) -> ProcessingResult:

        path = Path(
            input_file
        )

        # =====================================================
        # 1. CHECK INPUT
        # =====================================================

        if not path.exists():

            return ProcessingResult(
                success=False,
                message=(
                    "Input UAV file does not exist."
                ),
                input_file=str(path)
            )

        try:

            # =================================================
            # 2. PARSE UAV FILE
            # =================================================

            uav_data = (
                self.parser.parse(
                    input_file
                )
            )

            dataframe = (
                uav_data.dataframe
            )

            original_count = len(
                dataframe
            )

            # =================================================
            # 3. SELECT MAGNETIC SOURCE
            # =================================================

            magnetic_column = (
                self.correction_processor
                .select_magnetic_column(
                    dataframe,
                    uav_data.magnetic_column
                )
            )

            if magnetic_column is None:

                return ProcessingResult(
                    success=False,
                    message=(
                        "No usable magnetic "
                        "measurement was found "
                        "in the UAV file."
                    ),
                    input_file=str(path)
                )

            # =================================================
            # 4. QUALITY CHECK
            # =================================================

            warnings = (
                self.quality_checker.check(
                    dataframe,
                    uav_data.latitude_column,
                    uav_data.longitude_column,
                    magnetic_column
                )
            )

            # =================================================
            # 5. CLEAN DATA
            # =================================================

            cleaned = (
                self.cleaner.clean(
                    dataframe,
                    uav_data.latitude_column,
                    uav_data.longitude_column,
                    magnetic_column
                )
            )

            # =================================================
            # 6. MAGNETIC PROCESSING
            # =================================================

            processed = (
                self.magnetic_processor.process(
                    cleaned,
                    magnetic_column
                )
            )

            # =================================================
            # 7. MAGNETIC REFERENCE
            # =================================================

            processed = (
                self.correction_processor.process(
                    processed,
                    magnetic_column
                )
            )

            # =================================================
            # 8. SAVE PROCESSED CSV
            # =================================================

            output_path = Path(
                output_file
            )

            output_path.parent.mkdir(
                parents=True,
                exist_ok=True
            )

            processed.to_csv(
                output_path,
                index=False
            )

            # =================================================
            # 9. CREATE 12.5 METRE GRID
            # =================================================

            grid_path = (
                output_path.parent
                / f"{output_path.stem}_grid.csv"
            )

            grid_dataframe = (
                self.grid_processor.process(
                    processed,
                    str(grid_path),
                    cell_size_m=12.5
                )
            )

            # =================================================
            # 10. GEOPHYSICAL FEATURE GENERATION
            # =================================================

            geophysical_result = (
                self.geophysical_processor.process(
                    processed,
                    grid_dataframe
                )
            )

            feature_dataframe = (
                geophysical_result[
                    "feature_dataframe"
                ]
            )

            layers = (
                geophysical_result[
                    "layers"
                ]
            )

            metadata = (
                geophysical_result[
                    "metadata"
                ]
            )

            # =================================================
            # 11. SAVE FEATURE CSV
            # =================================================

            feature_path = (
                output_path.parent
                / f"{output_path.stem}_features.csv"
            )

            feature_dataframe.to_csv(
                feature_path,
                index=False
            )

            # =================================================
            # 12. GET GRID COORDINATES
            # =================================================

            eastings = np.asarray(
                sorted(
                    grid_dataframe[
                        "easting"
                    ].unique()
                ),
                dtype=float
            )

            northings = np.asarray(
                sorted(
                    grid_dataframe[
                        "northing"
                    ].unique()
                ),
                dtype=float
            )

            # =================================================
            # 13. DETECT MAGNETIC ANOMALIES
            # =================================================

            anomalies = (
                self.anomaly_detector.detect(
                    layers
                )
            )

            # =================================================
            # 14. ASSIGN REAL COORDINATES
            # =================================================

            anomalies = (
                self.anomaly_detector
                .assign_coordinates(
                    anomalies,
                    eastings,
                    northings
                )
            )

            # =================================================
            # 15. SAVE ANOMALIES AS GEOJSON
            # =================================================

            anomaly_geojson_path = (
                output_path.parent
                / f"{output_path.stem}_anomalies.geojson"
            )

            self.anomaly_detector.save_geojson(
                anomalies,
                str(
                    anomaly_geojson_path
                )
            )

            # =================================================
            # 16. CREATE MULTI-BAND GEOTIFF
            # =================================================

            geotiff_path = (
                output_path.parent
                / f"{output_path.stem}_feature_stack.tif"
            )

            # =================================================
            # 17. CREATE MAP PREVIEW
            # =================================================

            preview_path = (
                output_path.parent
                / f"{output_path.stem}_rtp_map.png"
            )

            map_metadata = (
                self.map_generator.generate(
                    layers,
                    eastings,
                    northings,
                    str(geotiff_path),
                    str(preview_path)
                )
            )

            # =================================================
            # 18. CREATE PROCESSING REPORT
            # =================================================

            report = {

                "survey": {

                    "input_file":
                        str(path),

                    "records":
                        original_count,

                    "valid_records":
                        len(processed),

                    "removed_records":
                        (
                            original_count
                            - len(processed)
                        )
                },

                "magnetic": {

                    "source_column":
                        magnetic_column,

                    "processing":
                        (
                            "Final supplied magnetic "
                            "field retained. "
                            "Survey-relative residual "
                            "also generated."
                        )
                },

                "coordinate_system": {

                    "crs":
                        "EPSG:25835",

                    "cell_size_m":
                        12.5
                },

                "igrf": {

                    "survey_year":
                        metadata[
                            "survey_year"
                        ],

                    "latitude":
                        metadata[
                            "survey_latitude"
                        ],

                    "longitude":
                        metadata[
                            "survey_longitude"
                        ],

                    "declination_deg":
                        metadata[
                            "igrf_declination_deg"
                        ],

                    "inclination_deg":
                        metadata[
                            "igrf_inclination_deg"
                        ],

                    "total_intensity_nt":
                        metadata[
                            "igrf_total_intensity_nt"
                        ]
                },

                "feature_layers": [

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
                ],

                "products": {

                    "processed_csv":
                        str(
                            output_path
                        ),

                    "grid_csv":
                        str(
                            grid_path
                        ),

                    "feature_csv":
                        str(
                            feature_path
                        ),

                    "feature_stack":
                        str(
                            geotiff_path
                        ),

                    "preview_map":
                        str(
                            preview_path
                        ),

                    "anomaly_geojson":
                        str(
                            anomaly_geojson_path
                        )
                },

                "anomalies": {

                    "count":
                        len(
                            anomalies
                        ),

                    "candidates": [

                        {

                            "id":
                                anomaly.id,

                            "easting":
                                round(
                                    anomaly.easting,
                                    3
                                ),

                            "northing":
                                round(
                                    anomaly.northing,
                                    3
                                ),

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

                        for anomaly
                        in anomalies[:20]
                    ]
                }
            }

            report_path = (
                output_path.parent
                / f"{output_path.stem}_report.json"
            )

            with open(
                report_path,
                "w",
                encoding="utf-8"
            ) as file:

                json.dump(
                    report,
                    file,
                    indent=2
                )

            # =================================================
            # 19. RETURN RESULT
            # =================================================

            return ProcessingResult(

                success=True,

                message=(
                    "UAV magnetic survey processed "
                    "through geophysical feature "
                    "generation."
                ),

                input_file=(
                    str(path)
                ),

                output_file=(
                    str(output_path)
                ),

                grid_file=(
                    str(grid_path)
                ),

                feature_file=(
                    str(feature_path)
                ),

                geotiff_file=(
                    str(geotiff_path)
                ),

                preview_file=(
                    str(preview_path)
                ),

                anomaly_geojson=(
                    str(
                        anomaly_geojson_path
                    )
                ),

                report_file=(
                    str(
                        report_path
                    )
                ),

                record_count=(
                    original_count
                ),

                valid_record_count=(
                    len(processed)
                ),

                removed_record_count=(
                    original_count
                    - len(processed)
                ),

                latitude_column=(
                    uav_data.latitude_column
                ),

                longitude_column=(
                    uav_data.longitude_column
                ),

                magnetic_column=(
                    magnetic_column
                ),

                igrf_declination_deg=(
                    metadata[
                        "igrf_declination_deg"
                    ]
                ),

                igrf_inclination_deg=(
                    metadata[
                        "igrf_inclination_deg"
                    ]
                ),

                igrf_total_intensity_nt=(
                    metadata[
                        "igrf_total_intensity_nt"
                    ]
                ),

                anomaly_count=(
                    len(anomalies)
                ),

                warnings=warnings
            )

        except Exception as exception:

            return ProcessingResult(

                success=False,

                message=str(
                    exception
                ),

                input_file=str(
                    path
                )
            )