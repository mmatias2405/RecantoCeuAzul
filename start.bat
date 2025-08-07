@echo off
setlocal enabledelayedexpansion

REM === Verifica se o .env existe ===
if not exist ".env" (
    echo ===============================
    echo .env não encontrado. Criando um novo...
    echo ===============================

    REM Gerar uma senha aleatória com 16 caracteres (hex)
    for /f %%i in ('powershell -Command "[System.Guid]::NewGuid().ToString('N')"') do (
    set "MYSQL_PASSWORD=%%i"
    )

    REM Define variáveis padrão
    set "MYSQL_USER=root"
    set "MYSQL_DATABASE=recantoceuazul"
    set "MYSQL_PORT=3307"
    set "API_PORT=8081"
    set "WEB_PORT=8080"

    (
        echo # Banco de dados
    ) > .env

    echo MYSQL_USER=!MYSQL_USER!>> .env
    echo MYSQL_PASSWORD=!MYSQL_PASSWORD!>> .env
    echo MYSQL_DATABASE=!MYSQL_DATABASE!>> .env
    echo MYSQL_PORT=!MYSQL_PORT!>> .env

    (
        echo. 
        echo # API
    ) >> .env

    echo API_PORT=!API_PORT!>> .env

    (
        echo.
        echo # WEB
    ) >> .env

    echo WEB_PORT=!WEB_PORT!>> .env

    echo Arquivo .env criado com sucesso!
) else (
    echo ===============================
    echo .env já existe. Lendo variáveis...
    echo ===============================

    for /f "usebackq tokens=1,2 delims==" %%A in (".env") do (
        set "%%A=%%B"
    )
)

REM Remover espaços desnecessários nas variáveis
for %%V in (MYSQL_PORT API_PORT WEB_PORT) do (
    set "TMP=!%%V!"
    set "%%V=!TMP: =!"
)

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
echo Aguardando a API responder em http://localhost:%API_PORT%/api/administrador...
echo ===============================

:waitloop
powershell -Command ^
    "$resp = Invoke-WebRequest -Uri 'http://localhost:%API_PORT%/api/administrador' -UseBasicParsing -ErrorAction SilentlyContinue; " ^
    "if ($resp.StatusCode -eq 200) { exit 0 } else { exit 1 }"

if errorlevel 1 (
    echo Servico ainda nao disponivel, aguardando 3 segundos...
    timeout /t 3 /nobreak >nul
    goto waitloop
)

echo ===============================
echo API pronta! Abrindo navegador em http://localhost:%WEB_PORT%
echo ===============================

start http://localhost:%WEB_PORT%

pause
