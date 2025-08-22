# Atlas DF to MongoDB - Sistema de Processamento de Dados

## Visão Geral

Este sistema é uma aplicação Spring Boot desenvolvida em Java 21 que processa dados financeiros do Bradesco, realizando movimentação, enriquecimento e separação de dados entre diferentes clusters MongoDB. O sistema opera com agendamento automático e controle de concorrência para garantir execuções seguras.

## Configuração

### Variáveis de Ambiente

Para maior segurança, as credenciais do MongoDB são configuradas através de variáveis de ambiente:

#### 1. Configuração Local (Desenvolvimento)

```bash
# Copie o arquivo de exemplo
cp .env.example .env

# Edite o arquivo .env com suas credenciais
vim .env
```

#### 2. Variáveis Disponíveis

| Variável | Descrição | Exemplo |
|----------|-----------|----------|
| `MONGODB_MAIN_URI` | URI do MongoDB principal (Data Federation) | `mongodb://user:pass@host/?ssl=true&authSource=admin` |
| `MONGODB_MAIN_DATABASE` | Database principal | `bradesco` |
| `MONGODB_FLAT_URI` | URI do MongoDB destino (Bradesco Flat) | `mongodb+srv://user:pass@host/?retryWrites=true&w=majority` |
| `MONGODB_FLAT_DATABASE` | Database destino | `bradesco_flat` |
| `MONGODB_CONTROL_URI` | URI do MongoDB de controle | `mongodb+srv://user:pass@host/?retryWrites=true&w=majority` |
| `MONGODB_CONTROL_DATABASE` | Database de controle | `control_db` |

#### 3. Configuração em Produção

```bash
# Defina as variáveis no ambiente de produção
export MONGODB_MAIN_URI="mongodb://user:pass@host/?ssl=true&authSource=admin"
export MONGODB_MAIN_DATABASE="bradesco"
export MONGODB_FLAT_URI="mongodb+srv://user:pass@host/?retryWrites=true&w=majority"
export MONGODB_FLAT_DATABASE="bradesco_flat"
export MONGODB_CONTROL_URI="mongodb+srv://user:pass@host/?retryWrites=true&w=majority"
export MONGODB_CONTROL_DATABASE="control_db"
```

> **⚠️ Importante**: O arquivo `.env` está no `.gitignore` e não será commitado. Nunca commite credenciais no código!

## Funcionalidades Principais

### 1. Controle de Execução
- **Agendamento**: Execução automática a cada 10 minutos (configurável)
- **Controle de Concorrência**: Previne execuções paralelas através da collection `process_control`
- **Estados do Sistema**: `PRONTO`, `EM_EXECUCAO`, `PROCESSADO`, `ERRO`
- **Recuperação de Erro**: Requer intervenção manual para reverter status de erro

### 2. Fluxo de Processamento

#### Etapa 1: Validação Inicial
- Consulta a collection `load_data` buscando documentos com:
  - `data`: Data atual (formato YYYY-MM-DD)
  - `status`: "ready"
- Se encontrado, prossegue para o processamento

#### Etapa 2: Movimentação e Enriquecimento
- **Parte 1**: Move dados de `bradesco.flat` para `bradesco_flat.temp` usando pipeline `$out`
- **Parte 2**: Adiciona campo `validade` baseado no comprimento do campo `DATA`:
  - "válido": se `DATA` tem 122 caracteres
  - "inválido": caso contrário
- Cria índice no campo `validade` para otimização

#### Etapa 3: Separação de Dados
- **Dados Válidos**: Criação de collection `YYYYMMDD_valido` com projeção específica
- **Dados Inválidos**: Criação de collection `YYYYMMDD_invalido`
- Ambas as collections são criadas no database `bradesco_flat`

#### Etapa 4: Finalização
- Atualização do status na collection `load_data` para "processado"
- Adição de array `procedimentos_executados` com detalhes de cada etapa
- Remoção da collection temporária `temp`
- Atualização do status do processo para "processado"

### 3. Logging e Monitoramento
- **Process Logs**: Registro detalhado de cada etapa com timestamps e tempo de execução
- **Controle de Processo**: Monitoramento do status geral do sistema
- **Logs de Aplicação**: Arquivo `logs/application.log` com rotação automática

## Estrutura de Dados

### Collection: load_data

```json
{
  "_id": "ObjectId",
  "data": "2025-08-21",
  "status": "ready",
  "data_processamento": "2025-08-21T16:30:00",
  "procedimentos_executados": [
    {
      "nome": "VALIDACAO_INICIAL",
      "tempo_execucao_segundos": 1.25,
      "data_execucao": "2025-08-21T16:25:00"
    },
    {
      "nome": "MOVIMENTACAO_DADOS",
      "tempo_execucao_segundos": 15.0,
      "data_execucao": "2025-08-21T16:25:01"
    },
    {
      "nome": "ENRIQUECIMENTO_DADOS",
      "tempo_execucao_segundos": 8.5,
      "data_execucao": "2025-08-21T16:25:16"
    },
    {
      "nome": "SEPARACAO_VALIDOS",
      "tempo_execucao_segundos": 12.0,
      "data_execucao": "2025-08-21T16:25:25"
    },
    {
      "nome": "SEPARACAO_INVALIDOS",
      "tempo_execucao_segundos": 3.0,
      "data_execucao": "2025-08-21T16:25:37"
    },
    {
      "nome": "LIMPEZA_FINALIZACAO",
      "tempo_execucao_segundos": 2.0,
      "data_execucao": "2025-08-21T16:25:40"
    }
  ]
}
```

### Collection: process_control

```json
{
  "_id": "ObjectId",
  "status": "PROCESSADO",
  "data_atualizacao": "2025-08-21T16:30:00",
  "processo_id": "proc_20250821_163000",
  "erro_detalhes": null
}
```

**Nota**: O campo `erro_detalhes` é automaticamente limpo quando o status não é "ERRO".

### Collection: process_logs

```json
{
  "_id": "ObjectId",
  "processo_id": "proc_20250821_163000",
  "mensagem": "Dados movidos com sucesso para coleção temp",
  "data_inicio": "2025-08-21T16:25:01",
  "data_fim": "2025-08-21T16:25:16",
  "tempo_total_segundos": 15.0,
  "etapa": "MOVIMENTACAO_DADOS",
  "status": "CONCLUIDO"
}
```

## Configuração

### Conexões MongoDB

1. **Cluster Principal** DF (Data Federation) (bradesco):
   ```
   mongodb://<connection_string>
   ```

2. **Cluster Flat** (bradesco_flat):
   ```
   mongodb+srv://<connection_string>
   ```

3. **Cluster Controle** (control_db):
   ```
   mongodb+srv://<connection_string>
   ```

### Propriedades Principais

```properties
# Agendamento
app.scheduler.fixed-rate=600000  # 10 minutos

# Collections
mongodb.control.collection.loadData=load_data
mongodb.control.collection.process-control=process_control
mongodb.principal.collection.flat=flat
mongodb.flat.collection.temp=temp

# Atlas Configuration
mongodb.atlas.project-id=6398c59375d749520b30b321
mongodb.atlas.cluster-name=bradesco

# Performance
app.batch.size=1000
```

## Exemplos de Uso

### 1. Preparar Dados para Processamento

Insira um documento na collection `load_data`:

```javascript
db.load_data.insertOne({
  "data": "2025-08-21",
  "status": "ready"
});
```

### 2. Verificar Status do Sistema

```javascript
// Verificar status do controle
db.process_control.findOne();

// Verificar logs recentes
db.process_logs.find().sort({"data_inicio": -1}).limit(10);

// Verificar dados processados
db.load_data.find({"status": "processado"});
```

### 3. Recuperar de Erro

```javascript
// Resetar status para permitir nova execução
db.process_control.updateOne(
  {},
  {
    $set: {
      "status": "PRONTO",
      "data_atualizacao": new Date()
    },
    $unset: {
      "erro_detalhes": ""
    }
  }
);
```

## Monitoramento

### Endpoints de Health Check

- `GET /actuator/health` - Status geral da aplicação
- `GET /actuator/info` - Informações da aplicação
- `GET /actuator/metrics` - Métricas de performance

### Logs

- **Console**: Logs em tempo real durante desenvolvimento
- **Arquivo**: `logs/application.log` com rotação (10MB, 30 dias)
- **Nível**: DEBUG para componentes MongoDB e da aplicação

## Estrutura do Projeto

```
src/main/java/com/example/atlasdfmongodb/
├── config/          # Configurações MongoDB
├── controller/      # Controllers REST
├── model/          # Modelos de dados
│   ├── LoadData.java
│   ├── ProcessControl.java
│   └── ProcessLog.java
├── repository/     # Repositórios MongoDB
├── service/        # Lógica de negócio
│   └── FluxoPrincipalService.java
└── scheduler/      # Agendamento de tarefas
```

## Requisitos

- **Java**: 21+
- **Spring Boot**: 3.x
- **MongoDB**: 4.4+
- **Maven**: 3.8+

## Execução

```bash
# Compilar
mvn clean compile

# Executar
mvn spring-boot:run

# Executar com profile específico
mvn spring-boot:run -Dspring-boot.run.profiles=production
```

## Troubleshooting

### Problemas Comuns

1. **Processo travado em EM_EXECUCAO**:
   - Verificar logs para identificar erro
   - Resetar status manualmente se necessário

2. **Dados não encontrados na load_data**:
   - Verificar formato da data (YYYY-MM-DD)
   - Confirmar status "ready"

3. **Erro de conexão MongoDB**:
   - Verificar credenciais e strings de conexão
   - Confirmar conectividade de rede

4. **Performance lenta**:
   - Ajustar `app.batch.size`
   - Verificar índices nas collections
   - Monitorar recursos do cluster

### Logs de Debug

Para ativar logs detalhados:

```properties
logging.level.com.example.atlasdfmongodb=DEBUG
logging.level.org.springframework.data.mongodb=DEBUG
```

## Contribuição

Para contribuir com o projeto:

1. Faça fork do repositório
2. Crie uma branch para sua feature
3. Implemente as mudanças
4. Execute os testes
5. Submeta um pull request

## Licença

Este projeto é propriedade do Bradesco e destinado ao uso interno.