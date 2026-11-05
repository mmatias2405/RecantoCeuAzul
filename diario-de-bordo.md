# Diário de Bordo — Integração do Módulo de Detecção Preditiva de Vazamentos

Projeto: **Sistema Inteligente de Gestão Hídrica e Detecção de Vazamentos — Residencial Recanto do Céu Azul**

---

## Etapa 1 — Diagnóstico Arquitetural e Inspeção do Modelo Preditivo (`.joblib`)

- **Data/Hora e Etapa**: `2026-11-04 21:22 BRT` — Etapa 1: Mapeamento Arquitetural do Repositório, Schema do InfluxDB/MySQL e Inspeção Técnica do Modelo `.joblib`.
- **Arquivos Criados/Modificados**:
  - `diario-de-bordo.md` (criado)
- **Decisões de Arquitetura e Implementação**:
  1. **Mapeamento da Estrutura do Repositório e Infraestrutura (`docker-compose.yml`)**:
     - O sistema é composto por dois módulos Java 17 + Spring Boot 3.5.6 independentes e dois bancos de dados orquestrados via `docker-compose.yml` na rede `recantonet`:
       - **`api/` (`com.recantoceuazul.api`, porta `8081`)**: Backend REST + consumidor MQTT (`Spring Integration MQTT` / Eclipse Paho) + cliente InfluxDB (`influxdb-client-java` v7.5.0) + persistência relacional (`Spring Data JPA` / MySQL Connector). Possui `MqttConfig`, `AtorController` (`/api/ator`), `MedicaoController` (`/api/medicao`), `ResidenciaController` (`/api/residencia`), `InfluxDbService`, `MedicaoService` e `MqttService`.
       - **`web/` (`com.recantoceuazul.web`, porta `8080`)**: Aplicação web MVC com `Spring Boot + Thymeleaf + Bootstrap 5.3.2 + Chart.js`. Centraliza rotas de interface e proxy no `HomeController`, comunicando-se com o módulo `api` via `RestTemplate` (`${API_URL}`, padrão `http://api:8081/api/` no Docker). Possui templates em `web/src/main/resources/templates/` (`index.html`, `cadastro.html`, `dashboardMorador.html`, `dashboardAdmin.html`, `consumo.html`, `registrarResidencia.html`) e estilização em `web/src/main/resources/static/customStyles.css`.
       - **`db` (MySQL 8.0, porta `3306`)**: Armazena o cadastro de `ator`, `residencia`, tabela associativa `possuir`, histórico consolidado mensal `medicao` (`volume_agua` acumulado e `delta` mensal em m³) e views analíticas (`v_media_consumo_condominio` e `v_dashboard_mensal`), inicializados por `mysql-init/init.sql`.
       - **`influxdb` (InfluxDB 2.8.0, porta `8086`)**: Banco de séries temporais configurado com organização padrão `docs` e bucket `home` (`${INFLUXDB_ORG}` / `${INFLUXDB_BUCKET}`).

  2. **Inspeção Técnica do Modelo de Machine Learning (`modelo_rf_recomendado_api.joblib`)**:
     - Localizado na raiz do repositório: `/modelo_rf_recomendado_api.joblib` (~4,56 MB).
     - Inspecionado via script Python com `joblib` (v1.6.0) e `scikit-learn` (v1.9.1):
       - **Classe do Estimador**: `sklearn.ensemble._forest.RandomForestClassifier`
       - **Hiperparâmetros Principais**: `n_estimators=100`, `criterion='gini'`, `max_features='sqrt'`, `min_samples_split=4`, `min_samples_leaf=2`, `bootstrap=True`, `random_state=42`, treinado com `_n_samples=22000` (profundidade das árvores entre 22 e 36).
       - **Número de Features Esperadas (`n_features_in_`)**: `3`
       - **Nomes Exatos das Features (`feature_names_in_`)**: `['Z_D_Med', 'Z_D_Max', 'Z_D_Std']`
         - `Z_D_Med`: Z-Score da demanda média no período/janela analisada (importância: `32,98%`, limiares de corte entre `-3,57` e `+3,67`, mediana `-0,0017`).
         - `Z_D_Max`: Z-Score da demanda máxima no período/janela analisada (importância: `39,44%`, limiares de corte entre `-4,01` e `+3,14`, mediana `+0,1949`).
         - `Z_D_Std`: Z-Score do desvio padrão da demanda no período/janela analisada (importância: `27,58%`, limiares de corte entre `-2,79` e `+4,38`, mediana `-0,1624`).
       - **Classes de Saída (`classes_`)**: `[0, 1]` (`0` = Rede Normal / Sem Vazamento; `1` = Anomalia / Provável Vazamento).
       - **Métodos Suportados**: Suporta tanto `predict(X)` (classificação binária `0`/`1`) quanto `predict_proba(X)` (vetor de probabilidades `[P(classe=0), P(classe=1)]`).

  3. **Análise do Fluxo Atual MQTT -> InfluxDB -> Spring Boot -> Interface Web**:
     - **Ingestão (`MqttService` -> `InfluxDbService.processarMensagemMqtt`)**: Mensagens JSON recebidas no tópico MQTT contendo `residenciaId` e `volume_litros` (> 0) são gravadas no InfluxDB:
       - **Bucket**: `${influx.bucket}` (`home`)
       - **Measurement**: `consumo_agua`
       - **Tag**: `residenciaId` (identificador da residência em String, ex: `"1"` a `"20"`, conforme também observado em `influxdb-init/dummy-data.lp`)
       - **Field**: `volume_litros` (`double`, volume consumido em litros)
       - **Timestamp**: `Instant.now()` com precisão de milissegundos (`WritePrecision.MS`).
     - **Consulta de Consumo em Tempo Real (`InfluxDbService.getConsumoHoje` -> `MedicaoService.getMedicaoHoje`)**:
       - `InfluxDbService.getConsumoHoje(String residenciaId)` executa uma query Flux filtrando `_measurement == "consumo_agua"`, `_field == "volume_litros"` e `r["residenciaId"] == "<id>"` desde o início do mês corrente (`date.truncate(t: now(), unit: 1mo)` no fuso `America/Sao_Paulo`), aplicando `|> sum()`.
       - `MedicaoService.getMedicaoHoje(int residenciaId)` converte os litros acumulados no mês para m³ (`/ 1000`) e soma à última leitura consolidada no MySQL (`Medicao.volumeAgua`), retornando `MedicaoHojeResponse` (`consumo` e `hora`).
       - No frontend (`dashboardMorador.html`), o valor inicial é renderizado via Thymeleaf no hidrômetro digital e atualizado assincronamente a cada 30 segundos por `fetch('/medicao/hoje/residencia/' + idResidencia)` -> `HomeController` -> `GET /api/medicao/hoje/residencia/{id}`.
     - **Consolidação Mensal (`InfluxDbService.medicoesDoMesPassado` -> `MedicaoService.consolidarConsumoMesAnterior`)**: Rotina `@Scheduled(cron = "0 0 0 1 * ?")` soma os litros do mês anterior no InfluxDB agrupados por `residenciaId` e persiste o fechamento mensal no MySQL.

- **Validação/Testes Realizados**:
  - Inspeção automatizada via script Python em ambiente isolado carregando `modelo_rf_recomendado_api.joblib` com `joblib.load()`, validando `type(rf)`, `n_features_in_`, `feature_names_in_`, `classes_`, `feature_importances_` e chamadas reais a `predict()` e `predict_proba()`.
  - Verificação de conectividade e inspeção do schema do InfluxDB ativo na porta `8086` (organização `docs`, bucket `home`) e dos scripts `influxdb-init/dummy-data.lp` e `mysql-init/init.sql`.
- **Hash/Mensagem do Commit Associado**:
  - `9a589ce` — `docs(diario): adiciona mapeamento arquitetural e inspecao do modelo joblib`

---

## Etapa 2 — Camada de Inferência do Modelo Preditivo (`.joblib`)

- **Data/Hora e Etapa**: `2026-11-04 21:28 BRT` — Etapa 2: Construção do Microserviço Python (FastAPI) e CLI de Inferência para o Modelo `modelo_rf_recomendado_api.joblib`.
- **Arquivos Criados/Modificados**:
  - `ml-service/predictor.py` (criado)
  - `ml-service/app.py` (criado)
  - `ml-service/requirements.txt` (criado)
  - `ml-service/Dockerfile` (criado)
  - `modelo_rf_recomendado_api.joblib` (versionado na raiz do projeto)
  - `docker-compose.yml` (modificado — inclusão do serviço `ml-service` e variável `ML_SERVICE_URL` no serviço `api`)
  - `.dockerignore` (criado — isolamento do contexto de build do container Python)
  - `.env.example` (modificado — inclusão de `ML_PORT=8000`)
  - `.gitignore` (modificado — exclusão de `__pycache__/`, `*.py[cod]` e `.venv/`)
- **Decisões de Arquitetura e Implementação**:
  1. **Escolha Arquitetural (Opção A — Microserviço FastAPI + Suporte Híbrido CLI)**:
     - Conforme as diretrizes do `GEMINI.md`, optou-se prioritariamente pela **Opção A (Microserviço REST em Python com FastAPI + Uvicorn)** integrado ao `docker-compose.yml` (`recanto-ml`, porta `8000`), mantendo o núcleo desacoplado em `ml-service/predictor.py` para permitir também execução direta via CLI (`ProcessBuilder` / linha de comando) caso necessário em ambiente de desenvolvimento local sem container.
     - **Justificativa Técnica**: O artefato `modelo_rf_recomendado_api.joblib` possui ~4,56 MB (100 árvores de decisão profundas). Carregá-lo uma única vez em memória durante o ciclo `lifespan` do FastAPI elimina o *cold start* de importação do `scikit-learn`/`numpy` e desserialização do `.joblib` a cada requisição HTTP, reduzindo a latência de inferência de ~1s para menos de 5ms, além de manter a imagem JRE da API Spring Boot (`eclipse-temurin:17-jre-alpine`) enxuta e com responsabilidades bem segregadas.
  2. **Contrato de Entrada e Saída (`POST /predict` e `GET /health`)**:
     - **Entrada (`PredicaoVazamentoRequest`)**: Aceita em JSON tanto as features padronizadas pré-calculadas (`Z_D_Med`, `Z_D_Max`, `Z_D_Std`) quanto a série de leituras brutas de demanda hídrica em litros (`leiturasLitros`, mínimo de 3 amostras) acompanhada opcionalmente das estatísticas de referência (`referencia`: `mu_med`, `sd_med`, `mu_max`, `sd_max`, `mu_std`, `sd_std`), utilizando parâmetros calibrados do condomínio como *fallback* seguro e prevenindo divisão por zero (`_safe_zscore` limitado ao intervalo `[-5.0, 5.0]`).
     - **Inferência**: Monta a matriz `numpy.float64` de dimensão `(1, 3)` respeitando estritamente a ordem declarada em `model.feature_names_in_` (`['Z_D_Med', 'Z_D_Max', 'Z_D_Std']`), executa `model.predict()` e `model.predict_proba()` extraindo a probabilidade exata da classe `1` (`positive_class_index`).
     - **Saída Padronizada (`PredicaoVazamentoResponse`)**:
       - `vazamentoDetectado` (`boolean`): `true` quando `classePredita == 1`.
       - `classePredita` (`int`): `0` (normal) ou `1` (vazamento).
       - `probabilidadeVazamento` (`float`): percentual de `0.0` a `100.0` calculado via `predict_proba`.
       - `nivelRisco` (`string`): `"Baixo"` (`< 40%`), `"Moderado"` (`40%` a `69,99%` ou classificação positiva limítrofe) e `"Alto"` (`>= 70%`).
       - `featuresUtilizadas` (`map`): valores exatos de `Z_D_Med`, `Z_D_Max` e `Z_D_Std` submetidos ao modelo para rastreabilidade.
  3. **Orquestração Docker**:
     - Adicionado o serviço `ml-service` na rede `recantonet` com *healthcheck* nativo em Python (`urllib.request` em `http://localhost:8000/health`), sem depender de pacotes extras como `curl` na imagem `python:3.12-slim`. O serviço `api` recebeu `ML_SERVICE_URL: http://ml-service:8000` e dependência `service_healthy`.
- **Validação/Testes Realizados**:
  - Execução isolada via CLI (`predictor.py --json`) testando perfis simulados de Rede Normal (`probabilidadeVazamento: 0.0%`, `nivelRisco: "Baixo"`), Risco Moderado (`probabilidadeVazamento: 69.49%`, `nivelRisco: "Moderado"`) e Risco Alto (`probabilidadeVazamento: 100.0%`, `nivelRisco: "Alto"`).
  - Testes automatizados dos endpoints HTTP FastAPI via `TestClient` validando `GET /health` (`200 OK`), `POST /predict` com série normal (`3.83%`, `Baixo`), `POST /predict` com pico anômalo de vazamento (`95.05%`, `Alto`) e tratamento de dados insuficientes (`422 Unprocessable Entity`).
- **Hash/Mensagem do Commit Associado**:
  - `e22698b` — `feat(ml): adiciona microservico fastapi e cli de inferencia para o modelo joblib`


---

## Etapa 3 — Integração no Backend Spring Boot (`InfluxDB` -> `Modelo ML` -> `Endpoint API`)

- **Data/Hora e Etapa**: `2026-11-04 21:35 BRT` — Etapa 3: Extração de Séries Temporais do InfluxDB, Engenharia de Features (`Z_D_Med`, `Z_D_Max`, `Z_D_Std`), Integração com Camada ML e Exposição REST.
- **Arquivos Criados/Modificados**:
  - `api/src/main/java/com/recantoceuazul/api/dto/AnaliseVazamentoResponseDTO.java` (criado)
  - `api/src/main/java/com/recantoceuazul/api/dto/InferenciaMlRequestDTO.java` (criado)
  - `api/src/main/java/com/recantoceuazul/api/dto/InferenciaMlResponseDTO.java` (criado)
  - `api/src/main/java/com/recantoceuazul/api/service/InfluxDbService.java` (modificado — adição do método `buscarUltimasLeiturasCondominio`)
  - `api/src/main/java/com/recantoceuazul/api/service/VazamentoService.java` (criado)
  - `api/src/main/java/com/recantoceuazul/api/controller/VazamentoController.java` (criado)
  - `api/src/main/resources/application.properties` (modificado — adição de `ml.service.url` e `ml.python.bin`)
  - `api/src/main/resources/application-dev.properties.example` (modificado — adição de `ml.service.url`)
  - `api/src/test/java/com/recantoceuazul/api/controller/VazamentoControllerTest.java` (criado)
  - `api/src/test/java/com/recantoceuazul/api/service/VazamentoServiceTest.java` (criado)
- **Decisões de Arquitetura e Implementação**:
  1. **Query Flux no InfluxDB (`InfluxDbService.buscarUltimasLeiturasCondominio`)**:
     - Consulta o bucket configurado (`${influx.bucket}`, padrão `home`) recuperando as últimas `N = 30` leituras válidas (`|> sort(columns: ["_time"]) |> tail(n: 30)`) de `volume_litros` no measurement `consumo_agua`, agrupadas por `residenciaId` na janela de `-365d`. Essa modelagem garante compatibilidade simultânea com telemetria MQTT em tempo real e com cargas históricas de homologação (`dummy-data.lp`), sem afetar os métodos existentes `processarMensagemMqtt`, `medicoesDoMesPassado` e `getConsumoHoje`.
  2. **Cálculo de Estatísticas e Padronização Z-Score (`VazamentoService`)**:
     - Exige um mínimo de `3` leituras válidas (`MIN_LEITURAS_ANALISE = 3`) para a residência avaliada. Caso haja menos de 3 pontos no InfluxDB, retorna imediatamente `AnaliseVazamentoResponseDTO.dadosInsuficientes(...)` com `analiseDisponivel = false`, `status = "Dados insuficientes para análise"`, `probabilidadeVazamento = 0.0` e mensagem orientativa amigável, sem invocar desnecessariamente o motor ML.
     - Quando há leituras suficientes, calcula $D_{\text{Med}}$ (média amostral), $D_{\text{Max}}$ (máximo) e $D_{\text{Std}}$ (desvio padrão amostral) da residência e obtém o vetor padronizado `[Z_D_Med, Z_D_Max, Z_D_Std]` em três níveis adaptativos de referência:
       1. *Baseline empírico do condomínio*: média e desvio padrão de $(D_{\text{Med}}, D_{\text{Max}}, D_{\text{Std}})$ entre as residências ativas ($\ge 3$ residências com dados no InfluxDB);
       2. *Baseline histórico intra-residência*: subdivisão cronológica em janelas quando uma única residência possui série longa ($\ge 12$ leituras);
       3. *Baseline residencial calibrado*: parâmetros históricos de referência do condomínio caso haja menos de 3 residências e série curta.
  3. **Comunicação Resiliente com o Modelo e Contrato da API (`VazamentoController`)**:
     - `VazamentoService` envia `POST ${ml.service.url}/predict` via `RestTemplate` (timeouts de 3s/5s) e possui *fallback* automático via `ProcessBuilder` para `ml-service/predictor.py` em ambiente local caso o container Python não esteja ativo.
     - `VazamentoController` expõe `GET /api/residencia/{id}/vazamento` (compatível com o padrão singular `/api/residencia` já adotado em `ResidenciaController`) e o alias `GET /api/residencias/{id}/vazamento`, validando a existência da residência no MySQL (`404 Not Found` para IDs inexistentes, `400 Bad Request` para IDs inválidos e `200 OK` com `AnaliseVazamentoResponseDTO` para residências cadastradas).
- **Validação/Testes Realizados**:
  - Execução completa de `./mvnw clean test` no módulo `api/`: compilação limpa em Java 17 e aprovação de 100% da suíte de testes (`15 tests, 0 failures, 0 errors`), incluindo os testes legados (`AtorControllerTest`, `MedicaoControllerTest`, etc.) e os novos testes unitários (`VazamentoControllerTest` e `VazamentoServiceTest`).
- **Hash/Mensagem do Commit Associado**:
  - `a526efd` — `feat(api): cria endpoint e service para predicao de vazamento com dados do influxdb`


---

## Etapa 4 — Integração na Interface Web (Frontend Thymeleaf + Bootstrap)

- **Data/Hora e Etapa**: `2026-11-04 21:39 BRT` — Etapa 4: Criação do Card Visual de Diagnóstico da Rede Hidráulica e Integração Dinâmica no Painel do Morador (`dashboardMorador.html`).
- **Arquivos Criados/Modificados**:
  - `web/src/main/java/com/recantoceuazul/web/dto/AnaliseVazamentoResponse.java` (criado)
  - `web/src/main/java/com/recantoceuazul/web/controller/HomeController.java` (modificado — carregamento via `Model` em `/morador` e rota assíncrona `/residencia/{id}/vazamento` com proteção de sessão/IDOR)
  - `web/src/main/resources/templates/dashboardMorador.html` (modificado — inclusão do componente visual de diagnóstico preditivo e atualização assíncrona via `fetch`)
  - `web/src/main/resources/static/customStyles.css` (modificado — classes temáticas `.diagnostico-vazamento-card`, estados semafóricos e tipografia da probabilidade)
  - `web/src/main/resources/application.properties` (modificado — valor *fallback* `http://localhost:8081/api/` em `api.url`)
  - `web/src/test/java/com/recantoceuazul/web/controller/HomeControllerVazamentoViewTest.java` (criado)
- **Decisões de Arquitetura e Implementação**:
  1. **Análise da Interface Existente**:
     - A camada web utiliza **Spring Boot MVC + Thymeleaf + Bootstrap 5.3.2 + Chart.js + CSS próprio (`customStyles.css`)**. A tela `dashboardMorador.html` é o ponto central onde o morador acompanha tanto o consumo em tempo real (`hidrometro-card`, atualizado a cada 30s) quanto o histórico consolidado mês a mês (cards mensais e gráfico comparativo).
  2. **Integração Dupla (SSR via Thymeleaf `Model` + Atualização Assíncrona via `fetch`)**:
     - No `HomeController.mostrarDashboardMorador`, a consulta `GET /api/residencia/{id}/vazamento` foi adicionada em um bloco `try/catch` isolado para garantir **não-regressão total**: qualquer indisponibilidade pontual do serviço preditivo faz *fallback* para `AnaliseVazamentoResponse.indisponivelPadrao()` sem afetar a exibição do hidrômetro digital ou do histórico mensal.
     - Adicionada também a rota `GET /residencia/{id}/vazamento` (`@ResponseBody`) no `HomeController`, protegida por validação de sessão e verificação anti-IDOR (garantindo que moradores `MORAR` consultem apenas residências vinculadas ao seu cadastro), permitindo atualização dinâmica a cada 30 segundos e sob demanda pelo botão **"Atualizar Diagnóstico"**.
  3. **Design Visual e Experiência do Morador (`dashboardMorador.html` + `customStyles.css`)**:
     - Construído o card **"Diagnóstico da Rede Hidráulica / Detecção de Vazamentos"** seguindo o padrão visual de sombra, bordas arredondadas (`15px`) e paleta Bootstrap 5 do sistema:
       - **Verde (`diagnostico-normal` / `text-success`)**: `"Rede Normal"` e badge `"Risco Baixo"`.
       - **Amarelo/Âmbar (`diagnostico-moderado` / `text-warning`)**: alertas ou observações de `"Risco Moderado"`.
       - **Vermelho (`diagnostico-alerta` / `text-danger`)**: `"Alerta: Provável Vazamento Detectado"` e badge `"Risco Alto"`.
       - **Cinza/Informativo (`diagnostico-indisponivel` / `text-secondary`)**: `"Dados insuficientes para análise"` e badge `"Aguardando Leituras"`.
     - Exibe em destaque a probabilidade calculada pelo modelo (`predict_proba` formatada em `%` no padrão `pt-BR` acompanhada de barra de progresso semafórica), a caixa **"Orientação ao Morador"** com recomendações práticas de inspeção hidráulica e os metadados de auditoria (quantidade de leituras avaliadas e horário da última verificação).
- **Validação/Testes Realizados**:
  - Build completo e empacotamento (`./mvnw clean package`) nos dois módulos (`web/` e `api/`) com `BUILD SUCCESS`.
  - Criação e execução do teste `HomeControllerVazamentoViewTest` (`@WebMvcTest`) validando a renderização real do template Thymeleaf `dashboardMorador.html` com sessão autenticada de morador (`200 OK`, sem erros de expressões OGNL/SpringEL).
  - Teste de fumaça HTTP com servidor `uvicorn` real respondendo aos contratos JSON consumidos pela aplicação.
- **Hash/Mensagem do Commit Associado**:
  - `0121010` — `feat(ui): exibe card de diagnostico e probabilidade de vazamento no painel do morador`


---

## Etapa 5 — Revisão Geral de Qualidade, Governança de Repositório e Síntese Acadêmica

- **Data/Hora e Etapa**: `2026-11-04 21:40 BRT` — Etapa 5: Auditoria de `git status`/`git log`, Validação de Orquestração (`docker compose config`) e Redação do Resumo Executivo para o Relatório Acadêmico.
- **Arquivos Criados/Modificados**:
  - `diario-de-bordo.md` (modificado — inclusão da auditoria final e da seção `Resumo Executivo para o Relatório Acadêmico`)
- **Decisões de Arquitetura e Implementação**:
  - Revisada a árvore de trabalho (`git status` limpo, sem artefatos temporários `.pyc`, logs ou diretórios `target` versionados indevidamente).
  - Validado o manifesto `docker-compose.yml` via `docker compose config -q`, confirmando a integridade sintática dos serviços `db`, `influxdb`, `ml-service`, `api` e `web`, das redes e dos *healthchecks*.
  - Auditado o histórico de commits na branch `feat/integracao-ml-vazamentos`, assegurando aderência integral ao padrão *Conventional Commits* em português.
- **Validação/Testes Realizados**:
  - `git status`, `git log --oneline` e `docker compose config -q` executados sem alertas ou inconsistências.
- **Hash/Mensagem do Commit Associado**:
  - Mensagem: `docs(diario): adiciona resumo executivo para o relatorio academico`

---

## Resumo Executivo para o Relatório Acadêmico

### Arquitetura e Fluxo Ponta a Ponta da Detecção Preditiva de Vazamentos Hídricos

O módulo de detecção preditiva de vazamentos foi concebido e integrado à plataforma **Residencial Recanto do Céu Azul** com o objetivo de transformar dados brutos de telemetria hidrométrica em diagnósticos preventivos acionáveis para os moradores, preservando integralmente os fluxos já existentes de ingestão IoT via MQTT e consolidação faturável mensal em banco relacional MySQL.

#### 1. Aquisição e Persistência em Série Temporal (ESP32 -> MQTT -> InfluxDB)
Os sensores de vazão acoplados aos microcontroladores ESP32 publicam periodicamente leituras de consumo no Broker MQTT. O backend Java Spring Boot (`api`) atua como assinante desse tópico por meio do `MqttService` (`Spring Integration MQTT`), validando os payloads JSON (`residenciaId` e `volume_litros`) e persistindo cada medição positiva no banco de séries temporais **InfluxDB 2.8** (bucket `home`, *measurement* `consumo_agua`, tag `residenciaId` e field `volume_litros`).

#### 2. Extração Estatística e Engenharia de Atributos no Backend (`api`)
Quando o diagnóstico de uma unidade habitacional é requisitado nos endpoints `GET /api/residencia/{id}/vazamento` (ou `GET /api/residencias/{id}/vazamento`), o `VazamentoController` valida o cadastro da residência no MySQL e aciona o `VazamentoService`. Por meio de uma consulta em linguagem **Flux** executada pelo `InfluxDbService`, o sistema recupera as últimas leituras temporais registradas para as residências do condomínio (`sort(columns: ["_time"]) |> tail(n: 30)`).

Caso a residência possua menos de três leituras válidas no InfluxDB, o serviço aplica degradação graciosa e retorna imediatamente o estado amigável `"Dados insuficientes para análise"`, evitando inferências estatisticamente inconsistentes. Havendo amostragem suficiente, calculam-se três descritores estatísticos do perfil recente de demanda em litros da unidade:
- **Demanda Média ($D_{\text{Med}}$)**: tendência central do volume consumido na janela observada;
- **Demanda Máxima ($D_{\text{Max}}$)**: pico de solicitação hidráulica registrado no período;
- **Desvio Padrão da Demanda ($D_{\text{Std}}$)**: grau de dispersão e variabilidade do consumo.

Para eliminar vieses decorrentes de diferenças estruturais de porte familiar entre unidades, essas grandezas são convertidas em escores padronizados (*Z-Scores*) — `Z_D_Med`, `Z_D_Max` e `Z_D_Std` — utilizando uma estratégia adaptativa em três níveis: (i) *baseline* empírico coletivo do condomínio quando há múltiplas residências ativas no InfluxDB; (ii) *baseline* histórico intra-residência por janelamento temporal em séries longas; ou (iii) parâmetros históricos de referência calibrados para o condomínio.

#### 3. Inferência por Aprendizado de Máquina (`.joblib` em Microserviço Python FastAPI)
O modelo preditivo pré-treinado (`modelo_rf_recomendado_api.joblib`) consiste em um classificador ensemble do tipo **`RandomForestClassifier`** (`Scikit-Learn`), composto por 100 árvores de decisão treinadas sobre 22.000 amostras rotuladas nas classes `0` (operação normal) e `1` (anomalia compatível com vazamento), tendo como atributos de entrada exatamente o trio `['Z_D_Med', 'Z_D_Max', 'Z_D_Std']`.

Para viabilizar a interoperabilidade entre o ecossistema Java Spring Boot e o artefato Python `.joblib` com baixa latência e isolamento de dependências, implementou-se o microserviço containerizado **`ml-service`** (**Python 3.12 + FastAPI + Uvicorn**), orquestrado na rede interna `recantonet` do `docker-compose.yml` (com suporte complementar via CLI em ambientes locais de desenvolvimento). O modelo é carregado uma única vez em memória durante a inicialização do serviço (`lifespan`), expondo o endpoint interno `POST /predict`. A cada chamada, o serviço executa simultaneamente `predict()` (classificação binária) e `predict_proba()` (probabilidade percentual de vazamento da classe positiva), categorizando o risco hidráulico em **Baixo** (`< 40%`), **Moderado** (`40%` a `69,99%`) ou **Alto** (`>= 70%`).

#### 4. Apresentação Visual e Orientação ao Morador (`web`)
Na aplicação web (`Spring Boot + Thymeleaf + Bootstrap 5`), o `HomeController` consome o diagnóstico preditivo de forma desacoplada e segura (com validação de sessão e proteção contra acesso indevido entre unidades — anti-IDOR), entregando os dados tanto na renderização inicial da página quanto por atualização assíncrona via `fetch` a cada 30 segundos ou sob demanda. No painel do morador (`dashboardMorador.html`), o componente **"Diagnóstico da Rede Hidráulica / Detecção de Vazamentos"** apresenta o resultado por codificação cromática semafórica (verde para *Rede Normal*, amarelo para *Risco Moderado* e vermelho para *Alerta: Provável Vazamento Detectado*), exibe a probabilidade percentual estimada pelo modelo em barra de progresso dinâmica e fornece recomendações práticas de inspeção predial, promovendo o uso racional de recursos hídricos e a mitigação rápida de perdas na rede interna.

---

## Etapa 6 — Rastreamento do Início do Indício de Vazamento e Reorganização UX do Histórico por Ano

- **Data/Hora e Etapa**: `2026-11-05 09:42 BRT` — Etapa 6: Detecção Temporal do Início de Anomalia Hidráulica (`inicioIndicioVazamento`) no Pipeline ML/API e Reformulação da Experiência de Usuário (UX) por Ano no Painel do Morador.
- **Arquivos Criados/Modificados**:
  - `ml-service/predictor.py` (modificado — inclusão do algoritmo robusto por mediana e MAD para localizar o primeiro ponto de ruptura da série temporal e formatá-lo no fuso `America/Sao_Paulo`)
  - `ml-service/app.py` (modificado — suporte ao vetor `timestampsLeituras` no contrato `PredictRequest` e exposição de `inicioIndicioVazamento` em `PredictResponse`)
  - `api/src/main/java/com/recantoceuazul/api/dto/LeituraTemporalDTO.java` (criado — par imutável `timestamp` + `volumeLitros` extraído do InfluxDB)
  - `api/src/main/java/com/recantoceuazul/api/dto/InferenciaMlRequestDTO.java` (modificado — envio de `timestampsLeituras` em ISO-8601)
  - `api/src/main/java/com/recantoceuazul/api/dto/InferenciaMlResponseDTO.java` (modificado — recepção de `inicioIndicioVazamento`)
  - `api/src/main/java/com/recantoceuazul/api/dto/AnaliseVazamentoResponseDTO.java` (modificado — adição do atributo `inicioIndicioVazamento` no payload REST da API)
  - `api/src/main/java/com/recantoceuazul/api/service/InfluxDbService.java` (modificado — criação de `buscarUltimasLeiturasTemporaisCondominio(int)` preservando `_time` dos registros Flux)
  - `api/src/main/java/com/recantoceuazul/api/service/VazamentoService.java` (modificado — ampliação de `LIMITE_JANELA_LEITURAS` para `40`, baseline aparado nos 80% perfis mais estáveis do condomínio para evitar mascaramento quando múltiplas casas apresentam vazamento simultâneo e cálculo local de contingência `identificarInicioIndicioLocal`)
  - `api/src/test/java/com/recantoceuazul/api/service/VazamentoServiceTest.java` (modificado — adição do teste unitário `deveIdentificarMomentoDeInicioDoIndicioDeVazamento`)
  - `web/src/main/java/com/recantoceuazul/web/dto/AnaliseVazamentoResponse.java` (modificado — mapeamento de `inicioIndicioVazamento`)
  - `web/src/main/resources/static/customStyles.css` (modificado — estilos dos seletores por ano, KPIs do período, cards mensais compactos e selo temporal `.diagnostico-inicio-pill` dentro do quadro de probabilidade)
  - `web/src/main/resources/templates/dashboardMorador.html` (modificado — nova barra superior de seleção de residência, seção de probabilidade com indicação explícita do momento de início das leituras anômalas e navegação de consolidações dividida por ano com gráfico e KPIs sincronizados)
  - `web/src/test/java/com/recantoceuazul/web/controller/HomeControllerVazamentoViewTest.java` (modificado — asserções de renderização de `#diagnostico-inicio-indicio` e `#filtro-anos-tabs`)
- **Decisões de Arquitetura e Implementação**:
  - **Detecção estatística robusta do início do vazamento (`Mediana + MAD`)**: Quando a probabilidade de vazamento é relevante (`>= 40%` ou classe positiva `1`), analisa-se a série temporal da residência calculando um limiar resistente a *outliers* dado por $\text{Limiar} = \text{Mediana} + 2{,}2 \times \max(1{,}4826 \times \text{MAD},\; 15{,}0\text{ L})$. Identifica-se o início da sequência sustentada de leituras acima desse patamar ao final da janela recente, convertendo seu `_time` UTC para `dd/MM/yyyy às HH:mm` (`America/Sao_Paulo`).
  - **Baseline coletivo aparado (Trimmed Baseline 80%)**: Em cenários de condomínio com 8 ou mais unidades monitoradas, o `VazamentoService` ordena os perfis pelo desvio padrão amostral (`dStd`) e utiliza os 80% perfis mais estáveis como referência de normalidade, impedindo que a presença simultânea de 3 ou 4 residências com vazamento inflacione artificialmente o desvio padrão coletivo e reduza os *Z-Scores* reais.
  - **Reorganização UX do Histórico Consolidado por Ano**: Com o crescimento do acervo de medições consolidadas (2025 e 2026), o `dashboardMorador.html` passou a agrupar os registros por ano (`2026`, `2025` e visão `Todos os anos`), selecionando automaticamente o ano mais recente e atualizando de forma reativa três indicadores-resumo (Consumo Acumulado no Período, Média Mensal e ÚltimoHidrometro Consolidado), o gráfico de linha `Chart.js` e a grade responsiva de cartões mensais.
- **Validação/Testes Realizados**:
  - `cd api && ./mvnw test`: **16 testes executados, 0 falhas** (incluindo validação de `inicioIndicioVazamento`).
  - `cd web && ./mvnw test`: **2 testes executados, 0 falhas** (`MockMvc` validando a renderização Thymeleaf do selo temporal no quadro de probabilidade e das abas de filtro por ano).
- **Hash/Mensagem do Commit Associado**:
  - Commit API/ML: `9b0df10` — `feat(api): identifica momento inicial de indicio de vazamento nas leituras do influxdb`
  - Commit UI/Web: `351cdd0` — `feat(ui): organiza historico por ano e exibe inicio do indicio de vazamento`
