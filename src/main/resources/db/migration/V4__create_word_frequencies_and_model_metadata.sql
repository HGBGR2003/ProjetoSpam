-- Tabela para armazenar a contagem de cada palavra separada por classe (spam/ham)
CREATE TABLE word_frequencies (
    id          BIGSERIAL    PRIMARY KEY,
    word        VARCHAR(255) NOT NULL UNIQUE,
    spam_count  INTEGER      NOT NULL DEFAULT 0,
    ham_count   INTEGER      NOT NULL DEFAULT 0
);

-- Índice para buscas rápidas por palavra (classificação em tempo real)
CREATE INDEX idx_word_frequencies_word ON word_frequencies (word);

-- Tabela singleton que armazena os totais globais do modelo treinado
CREATE TABLE model_metadata (
    id                  BIGSERIAL PRIMARY KEY,
    total_spam_emails   BIGINT NOT NULL DEFAULT 0,
    total_ham_emails    BIGINT NOT NULL DEFAULT 0,
    total_spam_words    BIGINT NOT NULL DEFAULT 0,
    total_ham_words     BIGINT NOT NULL DEFAULT 0,
    vocabulary_size     BIGINT NOT NULL DEFAULT 0,
    trained_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
