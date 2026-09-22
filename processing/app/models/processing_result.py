from typing import List, Optional

from pydantic import BaseModel, Field


class ProcessingResult(BaseModel):

    success: bool

    message: str

    input_file: Optional[str] = None

    output_file: Optional[str] = None

    grid_file: Optional[str] = None

    feature_file: Optional[str] = None

    geotiff_file: Optional[str] = None

    preview_file: Optional[str] = None

    anomaly_geojson: Optional[str] = None

    report_file: Optional[str] = None

    record_count: Optional[int] = None

    valid_record_count: Optional[int] = None

    removed_record_count: Optional[int] = None

    latitude_column: Optional[str] = None

    longitude_column: Optional[str] = None

    magnetic_column: Optional[str] = None

    igrf_declination_deg: Optional[float] = None

    igrf_inclination_deg: Optional[float] = None

    igrf_total_intensity_nt: Optional[float] = None

    anomaly_count: Optional[int] = None

    warnings: List[str] = Field(
        default_factory=list
    )