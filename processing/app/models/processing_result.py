from typing import List, Optional

from pydantic import BaseModel, Field


class AnomalyCandidate(BaseModel):
    id: int
    easting: float
    northing: float

    peak_residual_nT: float
    max_analytic_signal: float

    area_m2: float
    equivalent_radius_m: float
    estimated_depth_m: float

    pixel_x: int
    pixel_y: int

    interpretation_status: str = "CANDIDATE"
    requires_geologist_review: bool = True


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

    candidates: List[AnomalyCandidate] = Field(
        default_factory=list
    )

    warnings: List[str] = Field(
        default_factory=list
    )