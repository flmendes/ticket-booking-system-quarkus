# Scripts para Testes de Carga - Guia Rápido

## Visão Geral

Este projeto inclui scripts automatizados para facilitar os testes de carga com Gatling. Os scripts usam **docker exec** para executar comandos diretamente no container PostgreSQL definido no `docker-compose.yml`, eliminando a necessidade de instalar o cliente `psql` localmente.

## Scripts Disponíveis

| Script | Descrição |
|--------|-----------|
| `seed-test-data.sh` | Cria dados de teste (eventos e assentos) |
| `reset-test-data.sh` | Reseta assentos para AVAILABLE |
| `run-gatling-booking.sh` | Executa teste de fluxo de reserva |
| `run-gatling-race.sh` | Executa teste de race condition |
| `run-gatling-stress.sh` | Executa teste de stress progressivo |
| `run-all-tests.sh` | 🚀 Executa todos os testes automaticamente |

## Uso Básico

### Opção 1: Execução Automática Completa (Recomendado) 🚀

Execute todos os testes automaticamente com um único comando:

```bash
./run-all-tests.sh
```

Este script:
- ✅ Inicia os containers (docker-compose up -d)
- ✅ Aguarda o PostgreSQL ficar pronto
- ✅ Cria os dados de teste
- ✅ Executa todos os testes (Booking, Race Condition, Stress)
- ✅ Reseta os dados entre cada teste
- ✅ Gera relatórios para cada execução

### Opção 2: Execução Manual Individual

Para controle mais granular:

```bash
# 1. Iniciar serviços com docker-compose
docker-compose up -d

# 2. Aguardar PostgreSQL inicializar (alguns segundos)
sleep 5

# 3. Criar dados de teste (primeira vez)
./seed-test-data.sh

# 4. Executar teste específico
./run-gatling-booking.sh

# 5. Resetar dados antes do próximo teste
./reset-test-data.sh
```

### Como Funciona

Os scripts `seed-test-data.sh` e `reset-test-data.sh` executam comandos SQL diretamente no container `postgres-ticket` usando:

```bash
docker exec -i postgres-ticket psql -U ticketuser -d ticketdb < script.sql
```

Isso é mais simples e eficiente do que criar containers temporários, pois utiliza o container já existente do docker-compose.

## Variáveis de Ambiente

As variáveis podem ser customizadas se necessário:

```bash
# Exemplo: usar um container diferente
POSTGRES_CONTAINER=meu-postgres ./seed-test-data.sh

# Exemplo: usar database diferente
DB_NAME=outro_banco DB_USER=outro_user ./reset-test-data.sh
```

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `POSTGRES_CONTAINER` | `postgres-ticket` | Nome do container PostgreSQL |
| `DB_NAME` | `ticketdb` | Nome do banco |
| `DB_USER` | `ticketuser` | Usuário |

## Sequência Completa de Teste

```bash
# Setup inicial (apenas uma vez)
docker-compose up -d
sleep 5
./seed-test-data.sh

# Loop de testes
./run-gatling-booking.sh -u 50 -d 60
./reset-test-data.sh

./run-gatling-race.sh -u 100
./reset-test-data.sh

./run-gatling-stress.sh
./reset-test-data.sh
```

## Troubleshooting Rápido

### Erro: "Docker daemon is not running"
```bash
# Inicie o Docker
open -a Docker  # Mac
# ou
sudo systemctl start docker  # Linux
```

### Erro: "PostgreSQL container 'postgres-ticket' is not running"
```bash
# Verifique se os containers estão rodando
docker ps

# Se não estiverem, inicie com docker-compose
docker-compose up -d

# Aguarde alguns segundos para inicialização
sleep 5
```

### Erro: "SQL file not found"
```bash
# Execute a partir da raiz do projeto
cd /path/to/ticket-booking-system
./seed-test-data.sh
```

### Verificar conexão manualmente
```bash
# Executar comando SQL diretamente no container
docker exec -it postgres-ticket psql -U ticketuser -d ticketdb -c "SELECT version();"

# Listar tabelas
docker exec -it postgres-ticket psql -U ticketuser -d ticketdb -c "\dt"

# Contar assentos disponíveis
docker exec -it postgres-ticket psql -U ticketuser -d ticketdb -c "SELECT COUNT(*) FROM seats WHERE status='AVAILABLE';"
```

## Dados Criados

O script `seed-test-data.sh` cria:

| Evento | Assentos | Tipos | Padrões |
|--------|----------|-------|---------|
| Event 1 | 500 | REGULAR, VIP, PREMIUM | A1-A100, VIP-1 a VIP-50, PREM-1 a PREM-50, D1-D300 |
| Event 2 | 300 | Mixed | A1-A300 |
| Event 3 | 400 | REGULAR | SEAT-1 a SEAT-400 |
| Event 4 | 200 | VIP, REGULAR | S1-S200 |
| Event 5 | 600 | Mixed | L1-L600 |

**Total: 2000 assentos** prontos para teste de carga.

## Documentação Detalhada

- **[DOCKER_SCRIPTS_USAGE.md](./DOCKER_SCRIPTS_USAGE.md)** - Guia completo de uso com Docker
- **[QUICKSTART_GATLING.md](./QUICKSTART_GATLING.md)** - Início rápido com Gatling
- **[LOAD_TEST_SETUP.md](./LOAD_TEST_SETUP.md)** - Setup detalhado dos testes de carga
- **[GATLING_TESTING.md](./GATLING_TESTING.md)** - Documentação completa do Gatling

## Exemplo Prático Completo

```bash
#!/bin/bash
# Script de exemplo para rodar todos os testes

echo "=== Setup inicial ==="
docker-compose up -d
sleep 5  # Aguardar PostgreSQL inicializar

echo "=== Criando dados de teste ==="
./seed-test-data.sh

echo "=== Teste 1: Booking Flow ==="
./run-gatling-booking.sh -u 50 -d 60
./reset-test-data.sh

echo "=== Teste 2: Race Condition ==="
./run-gatling-race.sh -u 100
./reset-test-data.sh

echo "=== Teste 3: Stress Test ==="
./run-gatling-stress.sh -n 10 -s 50 -p 200
./reset-test-data.sh

echo "=== Testes concluídos! ==="
echo "Relatórios em: target/gatling/"
```

## Vantagens desta Abordagem

✅ Não requer instalação do PostgreSQL client
✅ Usa o container existente do docker-compose
✅ Mais rápido (não cria containers temporários)
✅ Funciona em qualquer OS (Mac, Windows, Linux)
✅ Simples e direto
✅ Fácil de usar em CI/CD
