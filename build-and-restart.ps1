# Stop on first error
$ErrorActionPreference = "Stop"

Write-Host "Running Gradle tests..."
./gradlew.bat test

Write-Host "Building project..."
./gradlew.bat build

Write-Host "Gradle build and tests succeeded."

Write-Host "Shutting down Docker Compose and cleaning up..."
docker-compose down -v --rmi local

Write-Host "Rebuilding and starting Docker Compose services..."
docker compose up --build
