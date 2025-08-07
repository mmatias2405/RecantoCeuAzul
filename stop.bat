@echo off
echo ===============================
echo Parando os containers Docker...
echo ===============================

docker-compose down

echo ===============================
echo Limpando volumes nao utilizados...
echo ===============================
docker volume prune -f
echo ===============================
echo Limpando imagens nao utilizadas...
echo ===============================
docker image prune -a -f


echo ===============================
echo Finalizado!
echo ===============================
pause