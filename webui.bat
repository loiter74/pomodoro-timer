@echo off
echo ============================
echo Starting Spring Boot backend service...
echo ============================
cd backend

REM Build and start the backend (make sure target/*.jar is generated)
mvn clean package

REM Find the jar file name (assuming there is only one jar file)
for %%i in (target\*.jar) do set JAR_NAME=%%i

start "Spring Boot Backend" cmd /k java -jar %JAR_NAME%

cd ..

REM Wait for the backend to start (adjust the seconds as needed)
echo Waiting for backend to start...
timeout /t 10

echo ============================
echo Starting frontend service...
echo ============================
cd frontend

REM Install dependencies
npm install

REM Start frontend
npm start

pause
