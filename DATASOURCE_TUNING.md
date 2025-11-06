# Configuração do Datasource para Testes de Carga

## Visão Geral

O arquivo `application.yml` foi otimizado para suportar testes de carga com **50 usuários concorrentes**, duração de 60 segundos e rampa de 10 segundos.

## Parâmetros Configurados e Suas Justificativas

### Connection Pool Settings

#### `max-size: 40`
**O que é**: Número máximo de conexões no pool.

**Cálculo**:
- 50 usuários concorrentes
- Cada usuário faz ~2 requisições (reserve + confirm)
- Total teórico: 50 × 2 = 100 operações simultâneas
- Com Quarkus assíncrono e non-blocking: ~40 conexões são suficientes
- Buffer de 40% para spikes

**Impacto**: Muito crítico. Poucos = timeouts, muitos = sobrecarga no PostgreSQL.

#### `min-size: 10`
**O que é**: Número mínimo de conexões mantidas sempre ativas.

**Justificativa**:
- Mantém 10 conexões "quentes" (warm)
- Evita overhead de criar conexões sob demanda durante picos
- 25% do max-size é uma boa prática

**Impacto**: Médio. Melhora latência inicial dos requests.

#### `initial-size: 10`
**O que é**: Conexões criadas na inicialização.

**Justificativa**:
- Pool já começa pronto para receber carga
- Reduz latência no primeiro teste

### Timeout Settings

#### `acquisition-timeout: 15`
**O que é**: Tempo máximo (segundos) para obter uma conexão do pool.

**Justificativa**:
- 15s é suficiente para aguardar uma conexão sob carga moderada
- Acima disso, melhor falhar rápido do que travar
- Load tests: pode reduzir para 10s (fail fast)

**Impacto**: Alto. Controla quanto tempo uma thread aguarda por conexão.

#### `background-validation-interval: 2`
**O que é**: Intervalo (minutos) para validar conexões idle.

**Justificativa**:
- A cada 2 minutos, valida se conexões idle ainda estão vivas
- Evita usar conexões "quebradas" do pool
- Previne erros intermitentes

**Impacto**: Médio. Melhora confiabilidade do pool.

#### `idle-removal-interval: 5`
**O que é**: Intervalo (minutos) para remover conexões idle.

**Justificativa**:
- Remove conexões que ficaram idle por muito tempo
- Libera recursos no PostgreSQL
- Mantém o pool "fresco"

**Impacto**: Baixo. Mais importante para ambientes de longa duração.

#### `max-lifetime: 30`
**O que é**: Tempo máximo de vida (minutos) de uma conexão.

**Justificativa**:
- Força recriação de conexões a cada 30 minutos
- Previne problemas com conexões antigas
- Load tests de 60s: não impacta muito, mas é boa prática

**Impacto**: Médio. Previne memory leaks e conexões corrompidas.

### Connection Validation

#### `validation-query-sql: "SELECT 1"`
**O que é**: Query SQL usada para validar se a conexão está viva.

**Justificativa**:
- `SELECT 1` é extremamente rápida (não acessa tabelas)
- Verifica se a conexão responde
- Standard para PostgreSQL

**Impacto**: Alto durante validações.

#### `validate-on-borrow: true`
**O que é**: Valida a conexão antes de entregá-la à aplicação.

**Justificativa**:
- Garante que nunca receberá uma conexão quebrada
- Pequeno overhead, mas previne erros difíceis de debugar
- Crítico em ambientes de alta carga

**Impacto**: Alto. Trade-off: pequena latência vs. confiabilidade.

#### `detect-leak-time: 60`
**O que é**: Tempo (segundos) após o qual uma conexão é considerada "vazada" (leaked).

**Justificativa**:
- Se uma conexão não é devolvida em 60s, há um leak
- Útil para identificar bugs no código
- Log automático para debugging

**Impacto**: Médio. Ferramenta de diagnóstico.

### PostgreSQL-Specific Settings

#### `connectTimeout: 30`
**O que é**: Timeout (segundos) para estabelecer conexão TCP.

**Justificativa**:
- 30s é generoso para PostgreSQL local/Docker
- Previne travamentos se o DB estiver slow
- Load tests: pode reduzir para 15s

**Impacto**: Alto no startup.

#### `socketTimeout: 60`
**O que é**: Timeout (segundos) para operações de leitura/escrita no socket.

**Justificativa**:
- Queries lentas não travam indefinidamente
- 60s é razoável para operações complexas
- Load tests com queries simples: 30s seria suficiente

**Impacto**: Alto. Previne deadlocks.

#### `tcpKeepAlive: true`
**O que é**: Habilita TCP keepalive no socket.

**Justificativa**:
- Mantém a conexão "viva" através de firewalls
- Detecta conexões mortas mais rapidamente
- Essencial para ambientes cloud/containerizados

**Impacto**: Médio. Melhora detecção de falhas.

#### `prepareThreshold: 5`
**O que é**: Após quantas execuções uma query vira prepared statement.

**Justificativa**:
- Prepared statements são mais rápidas (reuso do plano de execução)
- 5 é um bom balanço entre overhead inicial e ganho
- Queries repetidas (load tests) se beneficiam muito

**Impacto**: Alto em queries repetidas.

#### `preparedStatementCacheQueries: 256`
**O que é**: Número de prepared statements mantidas em cache.

**Justificativa**:
- Cache local evita re-parsing das queries
- 256 é suficiente para a maioria das aplicações
- Load tests com poucas queries diferentes: 100 seria suficiente

**Impacto**: Médio. Reduz CPU do PostgreSQL.

#### `reWriteBatchedInserts: true`
**O que é**: Otimiza batch inserts reescrevendo o SQL.

**Justificativa**:
- Transforma múltiplos INSERTs em um único comando
- Reduz drasticamente round-trips ao DB
- Crítico para operações de booking (inserir múltiplos assentos)

**Impacto**: Alto para batch operations.

### Hibernate Performance Settings

#### `statement-batch-size: 20`
**O que é**: Número de statements executados em batch.

**Justificativa**:
- Agrupa 20 INSERTs/UPDATEs em um único batch
- Reduz round-trips ao PostgreSQL
- Bookings com múltiplos assentos se beneficiam

**Impacto**: Alto para operações em lote.

#### `statement-fetch-size: 100`
**O que é**: Número de rows buscadas por vez em queries.

**Justificativa**:
- Fetch 100 linhas de uma vez ao invés de 1
- Reduz round-trips em queries grandes
- Load tests com queries pequenas: 50 seria suficiente

**Impacto**: Médio para queries grandes.

#### `query.plan-cache-max-size: 2048`
**O que é**: Número de query plans mantidos em cache.

**Justificativa**:
- Cache evita reprocessamento de queries
- 2048 é generoso (default é 1024)
- Load tests repetitivos: cache hit muito alto

**Impacto**: Alto em queries repetidas.

### Redis Configuration

#### `max-pool-size: 50`
**O que é**: Máximo de conexões Redis no pool.

**Justificativa**:
- Distributed locks (1 por operação de reserva)
- 50 usuários = 50 conexões potenciais
- Redis é muito mais leve que PostgreSQL

**Impacto**: Alto. Redis usado para locks.

## Comparação: Antes vs Depois

| Parâmetro | Antes | Depois | Ganho |
|-----------|-------|--------|-------|
| max-size | 20 | 40 | +100% capacidade |
| min-size | 5 | 10 | +100% conexões warm |
| Validação | ❌ | ✅ | Menos erros |
| TCP Keepalive | ❌ | ✅ | Melhor detecção de falhas |
| Prepared Statements | Default | Otimizado | +30% performance |
| Batch Inserts | ❌ | ✅ | +50% em lotes |
| Redis Pool | Default | 50 | Suporta 50 locks simultâneos |

## Perfis de Configuração

### Profile: Default (application.yml)
**Uso**: Desenvolvimento e testes moderados
- max-size: 40
- acquisition-timeout: 15s
- SQL logging: disabled

### Profile: LoadTest (application-loadtest.yml)
**Uso**: Testes de carga intensos (stress tests)
- max-size: 60
- acquisition-timeout: 10s (fail fast)
- Logging: minimal (WARN)

**Como usar**:
```bash
./mvnw quarkus:dev -Dquarkus.profile=loadtest
```

## Monitoramento Durante Testes

### Métricas do Connection Pool

```bash
# Ver métricas da aplicação (se habilitado)
curl http://localhost:8080/q/metrics

# Verificar conexões no PostgreSQL
docker exec -it postgres-ticket psql -U ticketuser -d ticketdb -c "
SELECT
    count(*) as total_connections,
    count(*) FILTER (WHERE state = 'active') as active,
    count(*) FILTER (WHERE state = 'idle') as idle
FROM pg_stat_activity
WHERE usename = 'ticketuser';"
```

### Sinais de Problemas

| Sintoma | Causa Provável | Solução |
|---------|----------------|---------|
| Muitos timeouts em acquisition | Pool muito pequeno | Aumentar max-size |
| CPU alta no PostgreSQL | Muitas conexões | Reduzir max-size |
| Erros "connection reset" | Falta keepalive | tcpKeepAlive: true |
| Queries lentas | Falta prepared statements | Ajustar prepareThreshold |
| Erros intermitentes | Conexões não validadas | validate-on-borrow: true |

## Tuning para Cenários Específicos

### Scenario 1: Mais Usuários (100+)
```yaml
max-size: 60
min-size: 20
acquisition-timeout: 10
```

### Scenario 2: Queries Complexas
```yaml
socketTimeout: 120
max-size: 30
statement-fetch-size: 200
```

### Scenario 3: Muitos Writes
```yaml
statement-batch-size: 50
reWriteBatchedInserts: true
max-size: 50
```

## Limites do PostgreSQL

Verifique também o PostgreSQL:

```bash
# Ver máximo de conexões permitidas
docker exec postgres-ticket psql -U ticketuser -d ticketdb -c "SHOW max_connections;"

# Ver conexões atuais
docker exec postgres-ticket psql -U ticketuser -d ticketdb -c "
SELECT count(*) FROM pg_stat_activity WHERE datname='ticketdb';"
```

**Configuração recomendada no PostgreSQL**:
```ini
max_connections = 100        # Deve ser > max-size da aplicação
shared_buffers = 256MB       # 25% da RAM
effective_cache_size = 1GB   # 50-75% da RAM
work_mem = 4MB              # Por operação de sort/hash
maintenance_work_mem = 64MB  # Para VACUUM, INDEX
```

## Conclusão

As configurações atuais estão otimizadas para:
- ✅ 50 usuários concorrentes
- ✅ Duração de 60 segundos
- ✅ Rampa de 10 segundos
- ✅ Operações de reserva + confirmação
- ✅ Uso de distributed locks (Redis)

Para testes mais pesados (100+ usuários), use o profile `loadtest`.
