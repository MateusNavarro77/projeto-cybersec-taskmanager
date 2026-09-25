# Projeto Cybersec Taskmanager

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![DevSecOps](https://img.shields.io/badge/DevSecOps-SAST%20%7C%20DAST-111827?style=flat-square)

API REST segura para gerenciamento de tarefas e checklists, desenvolvida com Spring Boot. O projeto aplica autenticação JWT, hash de senhas com BCrypt, validação de entrada, isolamento dos recursos por usuário e migrações versionadas de banco de dados.

> [!NOTE]
> O objetivo principal deste projeto foi construir uma **pipeline DevSecOps simplificada**, contemplando práticas de **SAST**, **DAST**, testes unitários, testes de integração e **deploy em uma VPS**.

Este projeto fez parte do projeto final da disciplina **Segurança do Cyberespaço**, cursada no **9º semestre da Graduação em Engenharia da Computação**.

## Visão geral

Cada usuário autenticado pode criar checklists e tarefas, associar tarefas a checklists, filtrar tarefas por status/prioridade e controlar seu ciclo de conclusão. A API também expõe documentação OpenAPI e métricas para observabilidade.

Principais recursos:

- Cadastro, login e consulta do usuário autenticado.
- Autenticação stateless com tokens JWT de curta duração.
- Senhas armazenadas com BCrypt.
- CRUD de checklists e tarefas.
- Marcação de tarefas como concluídas ou reabertas.
- Isolamento dos dados por usuário no serviço e na consulta ao repositório.
- Validação de payloads e tratamento global de exceções.
- UUIDs como identificadores das entidades.
- Migrações de banco com Flyway.
- Documentação interativa com Swagger UI.
- Métricas via Actuator, Micrometer e Prometheus.

## Componentes relacionados

Este backend faz parte de um conjunto de repositórios que compõem a solução completa:

- [Frontend do Task Manager](https://github.com/MateusNavarro77/projeto-cybersec-taskmanager-frontend): frontend básico desenvolvido em Angular para tornar a aplicação mais amigável e simples de utilizar.
- [Infraestrutura de monitoramento](https://github.com/MateusNavarro77/projeto-cybersec-monitoramento): configuração da infraestrutura utilizada para monitorar a aplicação e seus serviços.
- [Deploy na VPS](https://github.com/MateusNavarro77/projeto-cybersec-deploy): configuração do Docker Compose utilizada para executar a aplicação e os serviços na VPS.

## Stack

| Área | Tecnologia |
| --- | --- |
| Linguagem | Java 21 |
| Framework | Spring Boot 4.0.6 |
| API | Spring Web MVC, Bean Validation e OpenAPI |
| Persistência | Spring Data JPA, Hibernate e PostgreSQL 17 |
| Segurança | Spring Security, JWT (`java-jwt`) e BCrypt |
| Migrações | Flyway |
| Testes | JUnit 5, Mockito, MockMvc e Testcontainers |
| Qualidade e segurança | JaCoCo, Sonar e OWASP Dependency-Check |
| Observabilidade | Spring Actuator, Micrometer, Prometheus e Grafana |
| Empacotamento | Maven Wrapper e Docker |

## Arquitetura

O código segue uma arquitetura em camadas:

```text
controller -> service -> repository -> entity
     |           |
    DTOs     regras de negócio e escopo por usuário
```

As configurações de banco são separadas por perfil:

- `dev`: PostgreSQL local.
- `unittest`: H2 em memória, sem necessidade de Docker.
- `integrationtest`: PostgreSQL gerenciado pelo Testcontainers.
- `prod`: PostgreSQL e segredo JWT fornecidos por variáveis de ambiente.

## Pré-requisitos

- Java 21.
- Docker e Docker Compose — necessários para PostgreSQL local, Testcontainers, DAST e observabilidade.
- Git.

O Maven Wrapper (`./mvnw`) já está incluído no projeto.

## Executando localmente

### 1. Subir o PostgreSQL

```bash
docker compose up -d postgres
```

O perfil `dev` usa, por padrão:

```text
Host: localhost
Porta: 5432
Banco: secure_task_manager
Usuário: admin
Senha: admin123
```

Esses valores são destinados apenas ao desenvolvimento local. Não os utilize em produção.

### 2. Compilar e executar

```bash
./mvnw clean install
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

## Testes

### Testes unitários

Usam H2 e não dependem de Docker:

```bash
./mvnw test -Dspring.profiles.active=unittest
```

Os testes de serviço cobrem autenticação, regras de tarefas, checklists, validações e isolamento por usuário.

### Testes de integração

Usam Testcontainers com PostgreSQL. O Docker precisa estar em execução:

```bash
./mvnw verify -Dspring.profiles.active=integrationtest
```

Os testes de integração exercitam a aplicação e os endpoints de autenticação, tarefas e checklists com `MockMvc` e um banco PostgreSQL real em container.

Os relatórios de cobertura gerados pelo JaCoCo ficam em:

```text
target/site/jacoco-unit/
target/site/jacoco-it/
```

## DevSecOps

A proposta da pipeline simplificada é organizar a entrega nesta sequência:

```text
commit
  -> SAST e análise de dependências
  -> testes unitários
  -> testes de integração
  -> build da aplicação e da imagem Docker
  -> deploy em VPS
  -> DAST contra a API publicada
  -> observabilidade e validação pós-deploy
```

### SAST e análise de dependências

- O projeto possui configuração de organização Sonar no `pom.xml`, permitindo integrar a análise estática ao SonarQube ou SonarCloud.
- O plugin OWASP Dependency-Check analisa vulnerabilidades conhecidas nas dependências Maven.
- O arquivo `supression.xml` registra exceções documentadas para falsos positivos, quando a política da pipeline decidir habilitá-lo.

### DAST com OWASP ZAP

O script `dast.sh` inicia a aplicação no perfil de testes e executa o `zap-api-scan.py` contra a especificação OpenAPI:

```bash
mkdir -p dast-logs
./dast.sh
```

O comando precisa de Docker, da porta `8080` livre e gera:

```text
zap-api-report.html
zap-api-report.json
```

### Deploy em VPS

O `Dockerfile.prod` cria uma imagem enxuta baseada no Java 21 JRE, executa a aplicação com um usuário sem privilégios e ativa o perfil `prod`.

Exemplo de build e execução:

```bash
./mvnw clean package -DskipTests
docker build -f Dockerfile.prod -t cybersec-taskmanager:latest .

docker run -d \
  --name cybersec-taskmanager \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL='jdbc:postgresql://<host-do-banco>:5432/secure_task_manager' \
  -e DB_USERNAME='<usuario>' \
  -e DB_PASSWORD='<senha>' \
  -e JWT_SECRET='<segredo-forte-e-aleatorio>' \
  cybersec-taskmanager:latest
```

Em uma VPS, recomenda-se colocar a aplicação atrás de um proxy reverso com HTTPS, restringir as portas do firewall e fornecer segredos por mecanismo seguro. O repositório fornece os componentes para o fluxo, mas a configuração do provedor de CI/CD e do provisionamento da VPS deve ser adaptada ao ambiente escolhido.

## Endpoints principais

A URL base da API é `http://localhost:8080/api/v1`.

| Método | Endpoint | Autenticação | Descrição |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | Não | Cria um usuário |
| `POST` | `/auth/login` | Não | Retorna um JWT |
| `GET` | `/auth/me` | JWT | Retorna o usuário autenticado |
| `GET/POST` | `/checklists` | JWT | Lista ou cria checklists |
| `GET/PUT/DELETE` | `/checklists/{id}` | JWT | Consulta, atualiza ou remove um checklist |
| `GET` | `/checklists/{id}/tasks` | JWT | Lista as tarefas do checklist |
| `GET/POST` | `/tasks` | JWT | Lista ou cria tarefas |
| `GET` | `/tasks/orphan` | JWT | Lista tarefas sem checklist |
| `GET/PUT/DELETE` | `/tasks/{id}` | JWT | Consulta, atualiza ou remove uma tarefa |
| `PATCH` | `/tasks/{id}/complete` | JWT | Conclui uma tarefa |
| `PATCH` | `/tasks/{id}/reopen` | JWT | Reabre uma tarefa |

Para testar os endpoints rapidamente, use o arquivo [`api.http`](api.http), compatível com clientes HTTP de IDEs como IntelliJ IDEA e VS Code.

## Documentação e observabilidade

Com a aplicação em execução:

- Swagger UI: [`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html)
- Especificação OpenAPI: [`http://localhost:8080/v3/api-docs`](http://localhost:8080/v3/api-docs)
- Health check: [`http://localhost:8080/actuator/health`](http://localhost:8080/actuator/health)
- Métricas Prometheus: [`http://localhost:8080/actuator/prometheus`](http://localhost:8080/actuator/prometheus)

Para iniciar o ambiente de observabilidade:

```bash
docker compose up -d postgres prometheus grafana
```

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

As credenciais padrão do Grafana no Compose são `admin`/`admin123`; altere-as antes de qualquer uso fora do ambiente local.

## Configuração de produção

O perfil `prod` exige as variáveis:

| Variável | Finalidade |
| --- | --- |
| `DB_URL` | URL JDBC do PostgreSQL |
| `DB_USERNAME` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Segredo usado para assinar os tokens |

Também é possível definir `CORS_ALLOWED_ORIGINS` como uma lista de origens separadas por vírgula. Em produção, prefira origens explícitas em vez de `*`.

## Estrutura do projeto

```text
src/main/java/.../
├── config/          # OpenAPI e configurações gerais
├── controller/      # Endpoints REST
├── dto/             # Contratos de entrada e saída
├── entity/          # Entidades JPA
├── exception/       # Exceções e handler global
├── repository/      # Repositórios Spring Data
├── security/        # JWT, filtros e configuração Spring Security
└── service/         # Regras de negócio

src/main/resources/
├── application*.yaml
└── db/migration/    # Migrações Flyway
```

## Fluxo rápido da API

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"Mateus","email":"mateus@example.com","password":"SenhaSegura123!"}'

curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"mateus@example.com","password":"SenhaSegura123!"}'
```

Use o campo `token` retornado no login como `Authorization: Bearer <token>` nas rotas protegidas.
