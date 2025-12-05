@echo off
REM Build and start Docker Compose in detached mode
docker-compose -f docker-compose.yml up -d --build
echo All services started!
pause
