# 🐳 Docker Setup - Atlas DF to MongoDB

Este guia explica como executar a aplicação Atlas DF to MongoDB usando Docker e Docker Compose.

## 📋 Pré-requisitos

- Docker instalado
- Docker Compose instalado
- Credenciais do MongoDB Atlas

## 🚀 Início Rápido

### 1. Configurar Variáveis de Ambiente

```bash
# Copiar arquivo de exemplo
cp .env.example .env

# Editar com suas credenciais
nano .env  # ou seu editor preferido
```

### 2. Executar com Script Automático

```bash
# Usar o script de inicialização
./docker-start.sh
```

### 3. Executar Manualmente

```bash
# Construir a imagem
docker-compose build

# Iniciar os serviços
docker-compose up -d

# Verificar logs
docker-compose logs -f atlas-df-mongodb-app
```

## 🔧 Configuração

### Variáveis de Ambiente Obrigatórias

```env
# MongoDB Atlas - Conexão Principal
MONGODB_MAIN_URI=mongodb://user:pass@cluster.mongodb.net/...
MONGODB_MAIN_DATABASE=bradesco

# MongoDB Atlas - Banco Flat
MONGODB_FLAT_URI=mongodb+srv://user:pass@cluster.mongodb.net/...
MONGODB_FLAT_DATABASE=bradesco_flat

# MongoDB Atlas - Controle
MONGODB_CONTROL_URI=mongodb+srv://user:pass@cluster.mongodb.net/...
MONGODB_CONTROL_DATABASE=control_db
```

### Portas Expostas

- **8081**: API da aplicação
- **8081/api/actuator/health**: Health check

## 📊 Monitoramento

### Health Check

```bash
# Verificar saúde da aplicação
curl http://localhost:8081/api/actuator/health
```

### Logs

```bash
# Logs em tempo real
docker-compose logs -f atlas-df-mongodb-app

# Logs persistentes (volume)
ls -la logs/
```

### Status dos Containers

```bash
# Verificar status
docker-compose ps

# Estatísticas de uso
docker stats atlas-df-mongodb-app
```

## 🛠️ Comandos Úteis

### Gerenciamento de Containers

```bash
# Parar aplicação
docker-compose down

# Parar e remover volumes
docker-compose down -v

# Reconstruir imagem
docker-compose build --no-cache

# Reiniciar apenas a aplicação
docker-compose restart atlas-df-mongodb-app
```

### Debug e Desenvolvimento

```bash
# Acessar container em execução
docker-compose exec atlas-df-mongodb-app bash

# Ver configurações da aplicação
docker-compose exec atlas-df-mongodb-app env | grep MONGODB

# Executar em modo interativo
docker-compose run --rm atlas-df-mongodb-app bash
```

## 📁 Estrutura de Arquivos Docker

```
.
├── Dockerfile              # Definição da imagem
├── docker-compose.yml      # Orquestração dos serviços
├── .dockerignore           # Arquivos ignorados no build
├── docker-start.sh         # Script de inicialização
├── .env.example            # Exemplo de variáveis
├── .env                    # Suas variáveis (não versionado)
└── logs/                   # Logs persistentes
```

## 🔍 Troubleshooting

### Problemas Comuns

1. **Erro de conexão MongoDB**
   ```bash
   # Verificar variáveis de ambiente
   docker-compose exec atlas-df-mongodb-app env | grep MONGODB
   ```

2. **Container não inicia**
   ```bash
   # Ver logs detalhados
   docker-compose logs atlas-df-mongodb-app
   ```

3. **Porta já em uso**
   ```bash
   # Alterar porta no docker-compose.yml
   ports:
     - "8082:8080"  # Usar porta 8082 no host
   ```

4. **Problemas de memória**
   ```bash
   # Ajustar JAVA_OPTS no docker-compose.yml
   environment:
     - JAVA_OPTS=-Xmx4g -Xms2g
   ```

## 🚀 Deploy em Produção

### Recomendações

1. **Usar imagem otimizada**
   - Considere usar `openjdk:17-jre-slim` para produção
   - Implemente multi-stage build

2. **Configurações de segurança**
   - Use secrets do Docker Swarm ou Kubernetes
   - Não exponha portas desnecessárias

3. **Monitoramento**
   - Configure logs centralizados
   - Use Prometheus/Grafana para métricas

4. **Backup**
   - Configure backup dos volumes de logs
   - Monitore uso de disco

## 📞 Suporte

Para problemas específicos do Docker:
1. Verifique os logs: `docker-compose logs`
2. Confirme as variáveis de ambiente
3. Teste conectividade com MongoDB Atlas
4. Verifique recursos disponíveis (CPU/RAM)