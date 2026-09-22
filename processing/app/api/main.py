from fastapi import FastAPI

from app.api.processing import router


app = FastAPI(
    title="DMFS Processing Service",
    description=(
        "Python scientific processing service "
        "for the Drone Farm Management System."
    ),
    version="1.0.0"
)


app.include_router(
    router
)


@app.get("/")
def root():

    return {
        "service": "DMFS Processing Service",
        "status": "running"
    }


@app.get("/health")
def health():

    return {
        "status": "UP"
    }