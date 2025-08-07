# 📦 Recanto Céu Azul — Sistema Completo

Este projeto é composto por duas aplicações Spring Boot (API e Web) e um banco de dados MySQL, todos dockerizados e orquestrados via Docker Compose.

---

## 🚀 Tecnologias

- Java 17 + Spring Boot
- HTML/CSS + Bootstrap
- MySQL 8
- Docker + Docker Compose

---

## 📦 Pré-requisitos

Antes de iniciar, você precisa ter o **Docker** e o **Docker Compose** instalados na sua máquina.

- 🔗 [Download do Docker Desktop (Windows/Mac)](https://www.docker.com/products/docker-desktop/)
- 🐧 [Instruções de instalação do Docker no Linux](https://docs.docker.com/engine/install/)

> Verifique se o Docker está funcionando corretamente executando `docker --version` no terminal.

---

## ⚙️ Configuração

1. **Clone o repositório:**

   ```bash
   git clone https://github.com/mmatias2405/RecantoCeuAzul.git
   cd RecantoCeuAzul
   ```

2. **Configure o ambiente:**

   Renomeie o arquivo `.env.example` para `.env`:

   ```bash
   copy .env.example .env
   ```

   Edite o arquivo `.env` preenchendo as variáveis com os valores desejados:

   ```env
   # Banco de dados
   MYSQL_USER=root
   MYSQL_ROOT_PASSWORD= #Sua senha aqui
   MYSQL_DATABASE=recantoceuazul
   MYSQL_PORT=3307

   # API
   API_PORT=8081

   # WEB
   WEB_PORT=8080
   ```

---

## ▶️ Executando a aplicação

No Windows, basta executar o arquivo `start.bat` que:

- Compila os projetos da API e do frontend
- Sobe os containers Docker
- Abre o navegador automaticamente na aplicação web

```bash
start.bat
```

> Aguarde alguns segundos até que todos os serviços estejam no ar.

A aplicação estará disponível em:

- 🌐 **Frontend (Web):** [http://localhost:8080](http://localhost:8080)
- 🔗 **Backend (API):** [http://localhost:8081](http://localhost:8081)
- 🛢️ **MySQL:** acessível localmente na porta 3307 (ou a que você definir no `.env`)

---

## ⏹️ Parando a aplicação

Para parar todos os serviços e containers, execute:

```bash
stop.bat
```

---

## 🐞 Problemas comuns

- **Erro 500 ao abrir a aplicação?**  
  Isso pode ocorrer se o navegador abrir antes dos serviços estarem prontos. Basta recarregar a página após alguns segundos.

- **Portas já estão em uso?**  
  Edite o arquivo `.env` e altere as portas para outras disponíveis na sua máquina.

---

## 📁 Estrutura do projeto

```bash
├── api/              # Projeto Spring Boot (API)
├── web/              # Aplicação Web (Frontend)
├── mysql-init/       # Arquivo de inicialização do Banco de Dados
├── docker-compose.yml
├── start.bat
├── stop.bat
├── .env.example
└── README.md
```
