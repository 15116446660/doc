from fastapi import FastAPI

app = FastAPI(
    title="Document Analyzer AI Service",
    description="A microservice for performing AI-powered analysis on documents.",
    version="0.1.0",
)

@app.get("/")
def read_root():
    return {"message": "Welcome to the Document Analyzer AI Service"}

@app.get("/health", summary="Health Check")
def read_health():
    """
    Health check endpoint to confirm the service is running.
    """
    return {"status": "ok"}
