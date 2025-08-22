#!/bin/bash

# ===================================================================
# SCRIPT DE INICIALIZAÇÃO DOCKER - ATLAS DF TO MONGODB
# ===================================================================

set -e

echo "🚀 Iniciando Atlas DF to MongoDB com Docker..."

# Verificar se o arquivo .env existe
if [ ! -f ".env" ]; then
    echo "❌ Arquivo .env não encontrado!"
    echo "📋 Criando .env a partir do .env.example..."
    cp .env.example .env
    echo "⚠️  ATENÇÃO: Configure suas credenciais no arquivo .env antes de continuar!"
    echo "📝 Edite o arquivo .env com suas credenciais do MongoDB Atlas"
    exit 1
fi

# Verificar se as variáveis essenciais estão definidas
source .env

if [ -z "$MONGODB_MAIN_URI" ] || [ -z "$MONGODB_FLAT_URI" ] || [ -z "$MONGODB_CONTROL_URI" ]; then
    echo "❌ Variáveis de ambiente do MongoDB não configuradas!"
    echo "📝 Configure as URIs do MongoDB no arquivo .env"
    exit 1
fi

# Criar diretório de logs se não existir
mkdir -p logs

echo "🔧 Construindo imagem Docker..."
docker-compose build

echo "🚀 Iniciando aplicação..."
docker-compose up -d

echo "✅ Aplicação iniciada com sucesso!"
echo "🌐 Acesse: http://localhost:8081/api/actuator/health"
echo "📊 Logs: docker-compose logs -f atlas-df-mongodb-app"
echo "🛑 Parar: docker-compose down"

# Aguardar alguns segundos e verificar status
sleep 5
echo "📋 Status dos containers:"
docker-compose ps