# 📚 Projetos

Repositório com o projeto final de inteligência computacional.

---

## 🛠️ Tecnologias Utilizadas

* **React** — estrutura e estilização das interfaces
* **Java** — lógica da aplicação
* **Vercel** — deploy e hospedagem dos projetos

---

## 🚀 Projetos

### 📧 Detector de Spam

Classificador automático de e-mails spam/ham utilizando técnicas de aprendizado de máquina e processamento de linguagem natural. O pipeline completo inclui pré-processamento de texto, extração de features com TF-IDF e comparação entre múltiplos modelos (Naive Bayes, SVM, MLP, Random Forest).

* 🧠 **Disciplina:** Inteligência Computacional

---

## 📌 Organização do Repositório

```
📦 repositorio
 ┣ 📂 Projeto de Spam
 ┃ ┣ 📂 Front-End
```

---

## Executar com Docker

O projeto inclui o **modelo já treinado** (`modelo_treinado.sql`, ~200 MB via Git LFS). Na primeira subida, o PostgreSQL restaura esse dump automaticamente — **não é necessário treinar o modelo**.

O arquivo contém apenas `word_frequencies` e `model_metadata` (sem e-mails de treinamento).

### Pré-requisitos

- Docker Desktop ou Docker Engine + Compose v2
- [Git LFS](https://git-lfs.com/) instalado (obrigatório para clonar o dump)
- Arquivo `modelo_treinado.sql` na raiz do projeto (~200 MB após `git lfs pull`)

### Obter o modelo pré-treinado

**Opção A — Git LFS (recomendado):**

```bash
git lfs install
git clone <url-do-repositorio>
cd ProjetoSpam
git lfs pull
```

Confirme que o arquivo tem ~200 MB (não ~130 bytes — isso seria só o ponteiro LFS).

**Opção B — Link externo:** baixe `modelo_treinado.sql` e coloque na raiz do projeto, ao lado de `docker-compose.yml`.

### Subir tudo de uma vez

```bash
docker compose up --build
```

Ordem de subida: **PostgreSQL** (restore do dump) → **backend** → **front-end**.

Na **primeira execução**, aguarde nos logs do container `db` a mensagem `Restore concluído`. O restore de ~200 MB pode levar **5 a 30 minutos**, conforme o hardware.

### Dockerfile

```dockerfile
# Stage 1: build do JAR com Maven Wrapper
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copia somente os arquivos de dependência primeiro (cache de camadas)
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./
RUN chmod +x mvnw

# Baixa dependências sem compilar (aproveita cache Docker)
RUN ./mvnw dependency:go-offline -q

# Copia o código-fonte e compila
COPY src ./src
RUN ./mvnw package -DskipTests -q

# Stage 2: imagem de runtime mínima
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

# Copia apenas o JAR gerado
COPY --from=builder /build/target/*.jar app.jar

# Porta exposta pelo Spring Boot
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### URLs após subida

| Serviço | URL |
|---------|-----|
| Front-end | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| PostgreSQL (host) | localhost:5433 |

### Testar classificação (sem treinar)

1. Abra http://localhost:3000
2. Cole um e-mail ou envie um arquivo `.txt`
3. O sistema classifica usando o modelo já restaurado no banco

Teste via API:

```bash
curl -X POST http://localhost:8080/api/classify \
  -H "Content-Type: application/json" \
  -d "{\"text\":\"ganhe prêmio grátis clique aqui\"}"
```

### Comandos úteis

```bash
docker compose down              # parar os containers
docker compose down -v           # parar e apagar volume (força novo restore na próxima subida)
docker compose logs -f db        # acompanhar restore do banco
docker compose logs -f backend
curl http://localhost:8080/actuator/health
```

### Observações Docker

- **Não** é necessário pasta `dataset/` nem `POST /api/model/train` para avaliar o projeto.
- O restore roda **somente** na primeira criação do volume `pgdata`. Subidas seguintes reutilizam o banco persistido.
- Se o banco não foi restaurado (volume antigo vazio), execute `docker compose down -v` e suba novamente.
- Para desenvolvimento local com importação manual do corpus, use `APP_IMPORT_ENABLED=true` e monte `./dataset` (ver seção de treinamento abaixo).

---

## Treinamento do modelo (Naive Bayes)

O treinamento é **assíncrono** e otimizado para grandes volumes (~630k e-mails): leitura por cursor (sem OFFSET), limpeza de texto, INSERT em bulk via JDBC.

### Endpoints

| Método | URL | Descrição |
|--------|-----|-----------|
| `POST` | `http://localhost:8080/api/model/train` | Inicia treino; retorna **202** com `jobId` |
| `GET` | `http://localhost:8080/api/model/train/status/{jobId}` | Progresso (`progressPercent`) e sumário ao concluir |
| `GET` | `http://localhost:8080/api/model/train/latest` | Último job disparado |

### Fluxo recomendado

1. Subir o backend (`.\mvnw.cmd spring-boot:run`) com PostgreSQL em `spamdetector`.
2. `POST /api/model/train` — anotar o `jobId` da resposta.
3. Acompanhar logs no terminal ou `GET .../status/{jobId}` a cada 1–2 min.
4. Quando `status` = `COMPLETED`, validar no banco:
   - `SELECT COUNT(*) FROM word_frequencies;`
   - `SELECT * FROM model_metadata ORDER BY trained_at DESC LIMIT 1;`

### Configuração (`application.properties`)

| Propriedade | Padrão | Descrição |
|-------------|--------|-----------|
| `training.batch-size` | `5000` | E-mails por lote na leitura |
| `training.insert-batch-size` | `1000` | Linhas por batch JDBC |
| `training.max-emails` | `0` | `0` = todos; use `10000` ou `50000` para teste rápido |

### Observações

- Apenas **um** treinamento por vez (segundo `POST` retorna 409).
- Jobs ficam em memória; ao reiniciar o servidor, use os logs ou o banco para confirmar o último treino concluído.
- Tempo estimado com ~630k e-mails: **20–50 min** (hardware e tamanho médio do `content` variam).