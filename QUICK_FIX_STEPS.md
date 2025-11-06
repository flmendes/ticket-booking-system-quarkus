# Quick Fix - 3 Comandos

## Problema
NullPointerException em @Version durante testes de carga.

## Solução Rápida

```bash
# 1. Parar aplicação (Ctrl+C)

# 2. Corrigir banco de dados
./fix-database-versions.sh

# 3. Reiniciar aplicação
./mvnw quarkus:dev

# 4. Testar
./run-gatling-booking.sh -u 50 -d 60
```

## Resultado Esperado
✅ Success rate > 90%
✅ Sem NullPointerException

## Alternativa: Reset Completo (mais rápido)

```bash
# 1. Recriar banco do zero
docker-compose down -v
docker-compose up -d

# 2. Seed dados
./seed-test-data.sh

# 3. Iniciar app
./mvnw quarkus:dev

# 4. Testar
./run-gatling-booking.sh
```

## Mais Detalhes
Ver: [FIX_VERSION_COMPLETE.md](./FIX_VERSION_COMPLETE.md)
