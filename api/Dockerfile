# ESTÁGIO 1: BUILD (Compilação)
# Usa uma imagem com Maven e JDK para compilar a aplicação
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia os arquivos de configuração (pom.xml, mvnw, .mvn)
# para que o Docker possa cachear a camada de dependências
COPY pom.xml .
COPY mvnw .
COPY .mvn ./.mvn

# Copia o código-fonte
COPY src ./src

# Executa o build do Maven. O `-DskipTests` está aqui.
# O uso de './mvnw' garante o uso do wrapper.
RUN chmod +x mvnw \
    && ./mvnw clean package -DskipTests

# --------------------------------------------------------------------------------

# ESTÁGIO 2: RUN (Execução)
# Usa uma imagem mais leve (só com o JRE) para o runtime final
FROM eclipse-temurin:17-jre-alpine

# Adiciona informações do mantenedor
LABEL maintainer="matheusmatias555@gmail.com"

# Adiciona um volume (prática recomendada para o Spring Boot)
VOLUME /tmp

# Expõe a porta que a aplicação vai usar
EXPOSE 8081

# Copia o JAR construído do estágio 'build' para o estágio final
ARG ARTIFACT_NAME=api-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/${ARTIFACT_NAME} app.jar

# Define o ponto de entrada para rodar a aplicação
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]