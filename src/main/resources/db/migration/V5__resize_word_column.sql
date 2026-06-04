-- A tokenização ignora tokens com mais de 100 caracteres (URLs, hashes, base64).
-- A coluna é ajustada para refletir esse limite e evitar erros de truncamento.
ALTER TABLE word_frequencies
    ALTER COLUMN word TYPE VARCHAR(100);
