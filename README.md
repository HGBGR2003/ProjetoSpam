# 📚 Projetos

Repositório com o projeto final de inteligencia computacional.

---

## 🛠️ Tecnologias Utilizadas

* **REACT** — estrutura e estilização das interfaces
* **Java** - Lógica da Aplicação.
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

### Pré-requisitos

- Docker Desktop ou Docker Engine + Compose v2
- Pasta `dataset/spam/` e `dataset/ham/` na raiz do projeto (arquivos `.txt` do corpus)

### Subir tudo de uma vez

```bash
docker compose up --build
```

Isso sobe, em ordem: **PostgreSQL** → **backend Spring Boot** → **front-end Next.js**.

### URLs após subida

| Serviço | URL |
|---------|-----|
| Front-end | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| PostgreSQL (host) | localhost:5433 |

### Fluxo pós-subida (treino manual)

1. Aguardar nos logs do backend as mensagens `Importação concluída` (na primeira vez pode demorar).
2. Disparar o treinamento:
   ```bash
   curl -X POST http://localhost:8080/api/model/train
   ```
3. Acompanhar o progresso:
   ```bash
   curl http://localhost:8080/api/model/train/latest
   ```
4. Quando `status` = `COMPLETED`, abrir http://localhost:3000 e classificar e-mails.

### Comandos úteis

```bash
docker compose down          # parar os containers
docker compose down -v       # parar e apagar volume do banco (reset total)
docker compose logs -f backend
curl http://localhost:8080/actuator/health   # verificar saúde do backend
```

### Observações Docker

- O dataset **não** entra na imagem; é montado via volume `./dataset:/app/dataset`.
- Se as pastas `dataset/spam` ou `dataset/ham` não existirem, o backend sobe normalmente, mas sem dados para treino.
- O treinamento **não** é automático na subida — execute `POST /api/model/train` após a importação.

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
