# 📦 Recanto Céu Azul — Sistema Completo

Este projeto é composto por duas aplicações Spring Boot (API e Web), um banco de dados MySQL e o InfluxDB para séries temporais. A infraestrutura pode ser orquestrada via Docker Compose.

---

## 🚀 Tecnologias

- Java 17 + Spring Boot
- HTML/CSS + Bootstrap
- MySQL 8
- InfluxDB 2
- Docker + Docker Compose

---

## 📦 Pré-requisitos

Para rodar o projeto, você precisará das seguintes ferramentas instaladas:

- **Docker e Docker Compose:**
  - 🔗 [Docker Desktop (Windows/Mac)](https://www.docker.com/products/docker-desktop/)
  - 🐧 [Instalação no Linux](https://docs.docker.com/engine/install/)
- **Java 17 (JDK)** e **Maven** (Apenas para o Ambiente de Desenvolvimento)

> Verifique se o Docker está funcionando executando `docker --version` no terminal.

---

## ⚙️ Configuração Inicial

1. **Clone o repositório:**
   ```bash
   git clone [https://github.com/mmatias2405/RecantoCeuAzul.git](https://github.com/mmatias2405/RecantoCeuAzul.git)
   cd RecantoCeuAzul
   ```

2. **Configure as variáveis de ambiente (Docker):**
   Copie o arquivo de exemplo e configure suas credenciais.
   
   **No Linux:**
   ```bash
   cp .env.example .env
   ```
   **No Windows:**
   ```cmd
   copy .env.example .env
   ```
   Edite o arquivo `.env` gerado com os seus dados (senhas, portas, tokens).

---

## ▶️ Executando a aplicação

Existem duas maneiras de rodar este projeto: **Modo Completo** (para testes finais ou produção) e **Modo de Desenvolvimento** (para programar e testar de forma rápida).

### Opção 1: Modo Completo (Full Docker)
Neste formato, todos os quatro containers (MySQL, InfluxDB, API e Web) são iniciados simultaneamente. 

**⚠️ Importante:** Como o código da aplicação é compilado dentro do container, qualquer alteração no código exigirá que você recompile as imagens inteiras para testar. Esse método não é recomendado para desenvolvimento ativo.

Para iniciar tudo:
```bash
docker compose up -d --build
```

A aplicação estará disponível em:
- 🌐 **Frontend (Web):** [http://localhost:8080](http://localhost:8080)
- 🔗 **Backend (API):** [http://localhost:8081](http://localhost:8081)

---

### Opção 2: Modo de Desenvolvimento (Recomendado para codar)
Para ter agilidade na criação de novas features, você deve subir apenas os serviços de infraestrutura (bancos de dados) via Docker e rodar o Spring Boot diretamente na sua máquina.

**Passo 1: Subir apenas os bancos de dados**
```bash
docker compose up -d db influxdb
```

**Passo 2: Configurar o `application-dev.properties` da API**
Dentro do diretório `api/src/main/resources/`, existe um template de configuração preparado para o ambiente local.

Copie este arquivo para criar a sua configuração real (que será ignorada pelo Git por segurança):

**No Linux:**
```bash
cp api/src/main/resources/application-dev.properties.example api/src/main/resources/application-dev.properties
```
**No Windows:**
```cmd
copy api\src\main\resources\application-dev.properties.example api\src\main\resources\application-dev.properties
```
Edite o novo arquivo `application-dev.properties` preenchendo as senhas locais e o Token gerado pelo InfluxDB.

**Passo 3: Executar a API e a Web localmente com o profile `dev`**

Abra dois terminais (um na pasta `api` e outro na pasta `web`) e execute:

**No Linux:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**No Windows:**
```cmd
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```
Dessa forma, ao alterar qualquer classe Java ou arquivo HTML, basta reiniciar o serviço localmente sem precisar reconstruir containers no Docker.

---

## ⏹️ Parando a aplicação

Para parar e remover todos os serviços e containers do Docker, execute na raiz do projeto:

```bash
docker compose down
```

---

## 🐞 Problemas comuns

- **Erro de Conexão no Banco (Modo Dev)?**  
  Certifique-se de que a porta `3306` mapeada no `docker-compose.yml` não está sendo usada por uma instalação local do MySQL na sua máquina.
- **Portas já estão em uso?**  
  Edite o arquivo `.env` e altere as portas `API_PORT` e `WEB_PORT` para outras disponíveis.
- **InfluxDB recusando conexão?**
  Verifique se o container iniciou corretamente e se o token inserido no `application-dev.properties` é válido.

---

## 📁 Estrutura do projeto

```bash
├── api/
│   ├── src/main/resources/
│   │   ├── application.properties               # Configuração padrão (Docker)
│   │   └── application-dev.properties.example   # Template para ambiente local
├── web/                                         # Projeto Spring Boot (Frontend Web)
├── mysql-init/                                  # Scripts de inicialização do Banco MySQL
├── docker-compose.yml
├── .env.example
└── README.md
```
