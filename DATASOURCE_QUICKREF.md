# Datasource Configuration - Quick Reference

## Principais Mudanças para Testes de Carga

### Connection Pool
```yaml
# ANTES
max-size: 20
min-size: 5
# (sem outros parâmetros)

# DEPOIS
max-size: 40          # +100% capacidade
min-size: 10          # +100% conexões warm
initial-size: 10      # Pool pronto no startup
acquisition-timeout: 15s
max-lifetime: 30min
```

### Validação e Confiabilidade
```yaml
# ADICIONADO
validation-query-sql: "SELECT 1"
validate-on-borrow: true
detect-leak-time: 60s
leak-detection-interval: 30s
```

### PostgreSQL Performance
```yaml
# ADICIONADO
connectTimeout: 30s
socketTimeout: 60s
tcpKeepAlive: true
prepareThreshold: 5
preparedStatementCacheQueries: 256
reWriteBatchedInserts: true
```

### Hibernate Optimization
```yaml
# ADICIONADO
statement-batch-size: 20
statement-fetch-size: 100
query.plan-cache-max-size: 2048
connection.handling-mode: delayed_acquisition_and_release_after_transaction
```

### Redis Pool
```yaml
# ADICIONADO
max-pool-size: 50
max-pool-wait: 5s
reconnect-enabled: true
```

## Perfis Disponíveis

### Default Profile (50 usuários)
```bash
./mvnw quarkus:dev
```
- Pool size: 40 conexões
- Timeouts moderados
- Validação habilitada

### LoadTest Profile (100+ usuários)
```bash
./mvnw quarkus:dev -Dquarkus.profile=loadtest
```
- Pool size: 60 conexões
- Timeouts agressivos (fail fast)
- Logging mínimo

## Como Monitorar

### Ver Conexões Ativas
```bash
docker exec -it postgres-ticket psql -U ticketuser -d ticketdb -c "
SELECT count(*) as total, state
FROM pg_stat_activity
WHERE usename = 'ticketuser'
GROUP BY state;"
```

### Ver Pool Size do PostgreSQL
```bash
docker exec -it postgres-ticket psql -U ticketuser -d ticketdb -c "
SHOW max_connections;"
```

### Verificar Leaks
```bash
# Verifique os logs da aplicação para:
# - "Connection leak detected"
# - "Unable to acquire JDBC Connection"
# - "HikariPool - Connection is not available"
```

## Troubleshooting Rápido

| Erro | Solução |
|------|---------|
| "Connection timeout" | Aumentar max-size ou acquisition-timeout |
| "Too many connections" | Reduzir max-size ou aumentar max_connections do PostgreSQL |
| "Socket timeout" | Aumentar socketTimeout ou otimizar queries |
| "Connection reset" | Habilitar tcpKeepAlive |
| Queries lentas | Verificar prepareThreshold e caches |

## Benchmarks Esperados

Com as configurações atuais (50 usuários, 60s):

| Métrica | Esperado |
|---------|----------|
| Success Rate | > 85% |
| P95 Response Time | < 500ms |
| P99 Response Time | < 2000ms |
| Pool Utilization | 60-80% |
| Connection Waits | < 5% |

## Next Steps

- 📖 Leia o guia completo: [DATASOURCE_TUNING.md](./DATASOURCE_TUNING.md)
- 🚀 Execute testes: `./run-gatling-booking.sh`
- 📊 Analise métricas durante o teste
- ⚙️ Ajuste conforme necessário
