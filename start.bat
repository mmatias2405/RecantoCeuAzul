@echo off
setlocal enabledelayedexpansion

echo ===============================
echo Gerando os JARs dos projetos...
echo ===============================

cd api
call mvnw clean package -DskipTests
if errorlevel 1 (
    echo Erro ao compilar o projeto da API.
    pause
    exit /b
)

cd ../web
call mvnw clean package -DskipTests
if errorlevel 1 (
    echo Erro ao compilar o projeto Web.
    pause
    exit /b
)

cd ..
echo ===============================
echo Subindo os containers Docker...
echo ===============================

docker-compose up --build -d

echo ===============================
echo Abrindo navegador em http://localhost:8080
echo ===============================

start http://localhost:8080
pause