# Contexto e Diretrizes do Projeto - Sistema Inteligente de Gestão Hídrica e Detecção de Vazamentos

## 1. Visão Geral da Arquitetura
Este repositório contém um sistema IoT e Web para monitoramento de consumo de água e detecção preditiva de vazamentos em residências:
- **Ingestão IoT**: Sensores de vazão conectados a microcontroladores ESP32 publicam dados de telemetria em um Broker MQTT.
- **Backend & Web App (Java Spring Boot)**:
  - Atua como *subscriber* do Broker MQTT, processando os payloads e gravando as séries temporais no **InfluxDB** (demanda/vazão em tempo real).
  - Consolida o consumo mensal em um banco relacional **MySQL**, onde ficam os dados cadastrais das residências e seus responsáveis.
  - Serve a **Interface Web** (acompanhamento em tempo real e histórico mês a mês).
- **Machine Learning (`.joblib`)**: Modelo preditivo treinado em Python (Scikit-Learn/Joblib) localizado no repositório, responsável por avaliar a probabilidade/existência de vazamentos na rede hidráulica com base no perfil de demanda extraído do InfluxDB.
- **Infraestrutura**: Orquestrada via `docker-compose.yml` na raiz do projeto.

---

## 2. Regras Estritas de Operação Autônoma (OBRIGATÓRIO)

Você está operando em modo 100% autônomo. Para cada tarefa, siga rigorosamente este protocolo:

### 2.1. Exploração e Não-Regressão
1. Antes de alterar qualquer código, mapeie a estrutura do projeto Spring Boot (`pom.xml` ou `build.gradle`, pacotes, controllers, services, templates/views) e inspecione o arquivo `.joblib` usando um script Python rápido para descobrir exatamente quais *features* (`feature_names_in_`, formato do input, classes/probabilidades de saída) o modelo espera receber.
2. **Proibido quebrar funcionalidades existentes**: A ingestão MQTT -> InfluxDB, a consolidação no MySQL e as telas atuais devem continuar funcionando perfeitamente.

### 2.2. Estratégia de Integração Java <-> Modelo `.joblib`
Como a aplicação principal é em Java Spring Boot e o modelo está serializado em `.joblib` (Python):
- Verifique primeiro se já existe algum script Python ou container configurado para servir o modelo.
- Caso não exista, escolha a abordagem mais limpa e resiliente para a estrutura atual:
  - **Opção A (Preferencial se o Docker Compose estiver ativo)**: Criar um microserviço leve em Python (FastAPI ou Flask) com `joblib`/`scikit-learn` que carrega o arquivo `.joblib` em memória e expõe um endpoint REST interno (ex: `POST /predict`), adicionando-o ao `docker-compose.yml` caso pertinente, ou;
  - **Opção B (Execução direta)**: Criar um script Python enxuto na pasta do modelo e invocá-lo via `ProcessBuilder` no Spring Boot passando os parâmetros via JSON/CLI e lendo o `stdout`.
- Documente no `diario-de-bordo.md` o motivo técnico da escolha.

### 2.3. Commits Atômicos e Semânticos (Conventional Commits v1.0.0-beta.4)
- **NUNCA** faça um único commit gigante no final.
- Faça **commits atômicos** a cada unidade lógica concluída e compilada/testada.
- Siga estritamente o padrão em português: `<tipo>[escopo opcional]: <descrição curta no imperativo/minúsculo>`
  - Tipos permitidos: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`.
  - Exemplos:
    - `feat(ml): adiciona script de inferencia para o modelo joblib`
    - `feat(api): cria endpoint e service para predicao de vazamento com dados do influxdb`
    - `feat(ui): exibe alerta e probabilidade de vazamento no painel da residencia`
    - `docs(diario): registra implementacao do modulo de machine learning`

### 2.4. Atualização Contínua do `diario-de-bordo.md`
Cada etapa concluída DEVE ser registrada imediatamente no arquivo `diario-de-bordo.md` na raiz do projeto. Este arquivo será a base para a redação do relatório acadêmico final (TCC/Projeto Integrador).
Para cada entrada no `diario-de-bordo.md`, utilize a seguinte estrutura:
- **Data/Hora e Etapa**: Título claro da funcionalidade implementada.
- **Arquivos Criados/Modificados**: Lista exata com o caminho dos arquivos.
- **Decisões de Arquitetura e Implementação**: Explicação técnica detalhada de *como* foi feito (ex: qual query Flux/InfluxQL foi usada para agregar a demanda do InfluxDB, como os dados foram transformados para alimentar o `.joblib`, como o Spring Boot se comunica com o modelo e como a View consome esse endpoint).
- **Validação/Testes Realizados**: Como o código foi validado (ex: `mvn compile`, testes unitários, validação do script Python).
- **Hash/Mensagem do Commit Associado**.
