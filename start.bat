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
echo Aguardando a API responder em http://localhost:8081/api/administrador...
echo ===============================

:waitloop
rem Faz uma requisição GET e verifica se o código é 200 OK
powershell -Command ^
    "$resp = Invoke-WebRequest -Uri 'http://localhost:8081/api/administrador' -UseBasicParsing -ErrorAction SilentlyContinue; " ^
    "if ($resp.StatusCode -eq 200) { exit 0 } else { exit 1 }"

if errorlevel 1 (
    echo Servico ainda nao disponivel, aguardando 3 segundos...
    timeout /t 3 /nobreak >nul
    goto waitloop
)

echo ===============================
echo API pronta! Abrindo navegador em http://localhost:8080
echo ===============================

start http://localhost:8080

pause