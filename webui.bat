@echo off
echo ============================
echo 启动 Spring Boot 后端服务...
echo ============================
cd backend

REM 编译并启动后端（建议已配置好target/*.jar输出路径）
mvn clean package

REM 查找jar包名（假设只有一个jar包）
for %%i in (target\*.jar) do set JAR_NAME=%%i

start "Spring Boot 后端" cmd /k java -jar %JAR_NAME%

cd ..

REM 等待后端启动（可根据实际情况调整秒数）
echo 等待后端启动...
timeout /t 10

echo ============================
echo 启动前端服务...
echo ============================
cd frontend

REM 安装依赖
npm install

REM 启动前端
npm start

pause
