# Changelog - Datasource Optimization

## [2025-01-06] Otimização para Testes de Carga

### Adicionado
- ✅ Connection pool otimizado para 50 usuários concorrentes
- ✅ Validação de conexões (validate-on-borrow)
- ✅ Detecção de connection leaks
- ✅ TCP keepalive para containers Docker
- ✅ Prepared statements cache (256 queries)
- ✅ Batch inserts otimizados (reWriteBatchedInserts)
- ✅ Hibernate statement batching (20 statements)
- ✅ Query plan cache (2048 plans)
- ✅ Redis connection pool (50 conexões)
- ✅ Profile de load test (application-loadtest.yml)

### Modificado
- max-size: 20 → 40 conexões
- min-size: 5 → 10 conexões
- SQL logging: true → false (performance)
- Hibernate connection handling: delayed acquisition

### Parâmetros Críticos

**Connection Pool**:
- max-size: 40 (capacidade para 50 usuários)
- acquisition-timeout: 15s
- max-lifetime: 30min
- validate-on-borrow: true

**PostgreSQL**:
- connectTimeout: 30s
- socketTimeout: 60s
- tcpKeepAlive: true
- prepareThreshold: 5
- reWriteBatchedInserts: true

**Hibernate**:
- statement-batch-size: 20
- statement-fetch-size: 100
- query.plan-cache-max-size: 2048

**Redis**:
- max-pool-size: 50
- reconnect-enabled: true

### Performance Impact

| Métrica | Antes | Depois | Melhoria |
|---------|-------|--------|----------|
| Pool Capacity | 20 | 40 | +100% |
| Warm Connections | 5 | 10 | +100% |
| Prepared Stmt Cache | 0 | 256 | ∞ |
| Batch Size | 0 | 20 | +20x |
| Redis Pool | ~10 | 50 | +5x |

### Documentação
- [DATASOURCE_TUNING.md](./DATASOURCE_TUNING.md) - Guia completo
- [DATASOURCE_QUICKREF.md](./DATASOURCE_QUICKREF.md) - Referência rápida
- [application-loadtest.yml](./src/main/resources/application-loadtest.yml) - Profile stress

### Arquivos Modificados
- `src/main/resources/application.yml` - Configuração principal
- `src/main/resources/application-loadtest.yml` - Profile load test (NOVO)

### Como Testar
```bash
# Profile default (50 users)
./mvnw quarkus:dev
./run-gatling-booking.sh -u 50 -d 60

# Profile load test (100+ users)
./mvnw quarkus:dev -Dquarkus.profile=loadtest
./run-gatling-stress.sh
```

### Compatibilidade
- ✅ Quarkus 3.29.0
- ✅ PostgreSQL 16
- ✅ Redis 7
- ✅ Java 21
- ✅ Docker/Docker Compose

### Breaking Changes
- ❌ Nenhuma mudança incompatível
- ⚠️ SQL logging desabilitado por padrão (melhor performance)
