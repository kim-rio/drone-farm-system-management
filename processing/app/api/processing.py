import shutil
import uuid
from pathlib import Path

from fastapi import (
    APIRouter,
    File,
    UploadFile,
    HTTPException
)

from app.processing.pipeline import (
    SurveyProcessingPipeline
)


router = APIRouter(
    prefix="/api/processing",
    tags=["Processing"]
)


UPLOAD_DIRECTORY = Path(
    "output/uploads"
)

PROCESSED_DIRECTORY = Path(
    "output/processed"
)


UPLOAD_DIRECTORY.mkdir(
    parents=True,
    exist_ok=True
)

PROCESSED_DIRECTORY.mkdir(
    parents=True,
    exist_ok=True
)


pipeline = SurveyProcessingPipeline()


@router.post("/uav")
async def process_uav(
    file: UploadFile = File(...)
):

    if not file.filename:

        raise HTTPException(
            status_code=400,
            detail="File name is required."
        )

    if not file.filename.lower().endswith(
        ".uav"
    ):

        raise HTTPException(
            status_code=400,
            detail="Only .uav files are accepted."
        )

    file_id = uuid.uuid4().hex

    input_path = (
        UPLOAD_DIRECTORY
        / f"{file_id}.uav"
    )

    output_path = (
        PROCESSED_DIRECTORY
        / f"{file_id}_processed.csv"
    )

    with input_path.open(
        "wb"
    ) as buffer:

        shutil.copyfileobj(
            file.file,
            buffer
        )

    result = pipeline.process(
        str(input_path),
        str(output_path)
    )

    return result