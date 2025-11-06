# Docker Scripts Usage Guide

Os scripts `seed-test-data.sh` e `reset-test-data.sh` foram atualizados para usar Docker com o cliente PostgreSQL, eliminando a necessidade de instalar o `psql` localmente.

## Requisitos

- Docker instalado e em execução
- Acesso ao banco de dados PostgreSQL (local ou em container)

## Cenários de Uso

### Cenário 1: PostgreSQL rodando em Docker Compose

Se você está usando Docker Compose para rodar o PostgreSQL:

```bash
# Seed data
DB_HOST=host.docker.internal ./seed-test-data.sh

# Reset data
DB_HOST=host.docker.internal ./reset-test-data.sh
```

**Nota**: `host.docker.internal` é um hostname especial que o Docker usa para acessar o host a partir de um container.

### Cenário 2: PostgreSQL rodando localmente (fora do Docker)

```bash
# Seed data
./seed-test-data.sh

# Reset data
./reset-test-data.sh
```

**Nota**: O `localhost` funciona porque o container Docker acessa a rede do host.

### Cenário 3: PostgreSQL em servidor remoto

```bash
# Seed data
DB_HOST=seu-servidor.com \
DB_PORT=5432 \
DB_NAME=ticketdb \
DB_USER=ticketuser \
DB_PASSWORD=ticketpass \
./seed-test-data.sh

# Reset data
DB_HOST=seu-servidor.com \
DB_PORT=5432 \
DB_NAME=ticketdb \
DB_USER=ticketuser \
DB_PASSWORD=ticketpass \
./reset-test-data.sh
```

### Cenário 4: Conectar ao container PostgreSQL diretamente (sem network)

Se você quiser conectar diretamente ao container PostgreSQL pelo nome:

```bash
# Primeiro, descubra a network do container PostgreSQL
docker network ls
docker inspect <container-id-do-postgres> | grep NetworkMode

# Execute com a mesma network
docker run --rm -i \
    --network <nome-da-network> \
    -e PGPASSWORD="ticketpass" \
    -v "$(pwd)/src/test/resources/test-data-seed.sql:/tmp/test-data-seed.sql:ro" \
    postgres:16-alpine \
    psql -h <nome-do-container-postgres> -p 5432 -U ticketuser -d ticketdb -f /tmp/test-data-seed.sql
```

## Variáveis de Ambiente

Todas as variáveis possuem valores padrão e podem ser sobrescritas:

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_PORT` | `5432` | Porta do PostgreSQL |
| `DB_NAME` | `ticketdb` | Nome do banco de dados |
| `DB_USER` | `ticketuser` | Usuário do banco |
| `DB_PASSWORD` | `ticketpass` | Senha do banco |
| `POSTGRES_IMAGE` | `postgres:16-alpine` | Imagem Docker a usar |

## Exemplos Práticos

### Exemplo 1: Setup completo com Docker Compose

Assumindo que você tem este `docker-compose.yml`:

```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: ticketdb
      POSTGRES_USER: ticketuser
      POSTGRES_PASSWORD: ticketpass
    ports:
      - "5432:5432"
```

Execute:

```bash
# 1. Inicie os serviços
docker-compose up -d

# 2. Aguarde o PostgreSQL inicializar (alguns segundos)
sleep 5

# 3. Seed dos dados (use host.docker.internal para Mac/Windows ou localhost para Linux)
DB_HOST=host.docker.internal ./seed-test-data.sh

# 4. Rode os testes Gatling
./run-gatling-booking.sh

# 5. Reset dos dados antes do próximo teste
DB_HOST=host.docker.internal ./reset-test-data.sh
```

### Exemplo 2: PostgreSQL local sem Docker

Se você tem PostgreSQL instalado localmente:

```bash
# Os scripts usarão Docker apenas para executar o cliente psql
./seed-test-data.sh
./reset-test-data.sh
```

### Exemplo 3: Uso com diferentes versões do PostgreSQL

```bash
# Usar PostgreSQL 15
POSTGRES_IMAGE=postgres:15-alpine ./seed-test-data.sh

# Usar PostgreSQL 14
POSTGRES_IMAGE=postgres:14-alpine ./reset-test-data.sh
```

## Troubleshooting

### Erro: "Cannot connect to database"

**Problema**: O container não consegue acessar o banco de dados.

**Soluções**:

1. **Se PostgreSQL está em Docker Compose (Mac/Windows)**:
   ```bash
   DB_HOST=host.docker.internal ./seed-test-data.sh
   ```

2. **Se PostgreSQL está em Docker Compose (Linux)**:
   ```bash
   # Opção 1: Use a network do compose
   docker network ls
   # Adicione --network <nome-da-network> nos scripts

   # Opção 2: Use o IP do host
   DB_HOST=$(ip route | grep default | awk '{print $3}') ./seed-test-data.sh
   ```

3. **Verifique se o PostgreSQL está acessível**:
   ```bash
   docker run --rm postgres:16-alpine \
     psql -h host.docker.internal -p 5432 -U ticketuser -d ticketdb -c "SELECT 1;"
   ```

### Erro: "Docker daemon is not running"

**Solução**: Inicie o Docker Desktop ou Docker Engine:

```bash
# Mac
open -a Docker

# Linux (systemd)
sudo systemctl start docker

# Verifique o status
docker info
```

### Erro: "SQL file not found"

**Problema**: O script não encontrou o arquivo SQL.

**Solução**: Execute o script a partir do diretório raiz do projeto:

```bash
cd /Users/flavio/Developer/Labs/java/ticket-booking-system
./seed-test-data.sh
```

### Erro: "Permission denied" ao montar volume

**Problema**: Docker não tem permissão para acessar o arquivo SQL.

**Solução**: Verifique as permissões do arquivo:

```bash
chmod +r src/test/resources/test-data-seed.sql
```

## Como Funciona

### seed-test-data.sh

1. Verifica se Docker está disponível e rodando
2. Testa a conexão com o banco usando um container temporário
3. Monta o arquivo SQL como volume read-only no container
4. Executa o script SQL através do container
5. Remove o container automaticamente após a execução

### reset-test-data.sh

1. Verifica se Docker está disponível e rodando
2. Cria um container temporário com o cliente PostgreSQL
3. Executa comandos SQL inline (usando heredoc)
4. Remove o container automaticamente após a execução

## Vantagens desta Abordagem

✅ **Não requer instalação local**: Não precisa instalar PostgreSQL client na máquina
✅ **Portátil**: Funciona em qualquer sistema com Docker
✅ **Isolado**: Não interfere com outras instalações PostgreSQL
✅ **Versão específica**: Pode escolher a versão exata do cliente PostgreSQL
✅ **Limpeza automática**: Containers são removidos automaticamente (--rm)

## Alternativa: Usar Docker Exec (se PostgreSQL já está em container)

Se você preferir executar comandos diretamente no container PostgreSQL:

```bash
# Encontre o nome do container
docker ps | grep postgres

# Execute diretamente
docker exec -i <container-name> psql -U ticketuser -d ticketdb -f - < src/test/resources/test-data-seed.sql
```

Porém, os scripts atuais são mais flexíveis e funcionam em qualquer cenário.
