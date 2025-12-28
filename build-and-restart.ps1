# Stop on first error
$ErrorActionPreference = "Stop"

Write-Host "Building project..."
./gradlew.bat build
if ($LASTEXITCODE -ne 0) {
    Write-Error "Gradle build failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}

Write-Host "Gradle build and tests succeeded."

Write-Host "Shutting down Docker Compose and cleaning up..."
docker-compose down -v --rmi local

Write-Host "Rebuilding and starting Docker Compose services..."
docker compose up --build
