#!/bin/bash
set -euo pipefail

DUMP_FILE="/backup/modelo_treinado.sql"

echo "=== Iniciando restore do modelo treinado ==="

if [ ! -f "$DUMP_FILE" ]; then
    echo "ERRO: $DUMP_FILE não encontrado."
    echo "Coloque modelo_treinado.sql na raiz do projeto (git lfs pull) antes de subir o Docker."
    exit 1
fi

echo "Restaurando dump (pode levar de 5 a 30 minutos, conforme o hardware)..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -f "$DUMP_FILE"

echo "=== Restore concluído ==="
