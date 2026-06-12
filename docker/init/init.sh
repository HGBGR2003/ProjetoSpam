#!/bin/bash
set -e

echo ">>> Baixando modelo treinado do Google Drive..."
FILE_ID="13gZm3GWN91aTqpebskdEK0iiLMhdiMBX"
wget -q --no-check-certificate \
  "https://drive.usercontent.google.com/download?id=${FILE_ID}&export=download&confirm=t" \
  -O /tmp/modelo_treinado.sql

echo ">>> Removendo linha incompativel com Postgres 15..."
sed -i 's/SET transaction_timeout = 0;//g' /tmp/modelo_treinado.sql

echo ">>> Importando banco de dados..."
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -f /tmp/modelo_treinado.sql

echo ">>> Importacao concluida com sucesso!"
