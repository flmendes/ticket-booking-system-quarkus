# Fix Completo: NullPointerException em @Version

## Problema

O erro persiste porque **dados existentes no banco** têm `version = NULL`. O `@Builder.Default` só funciona para **novas entidades**, mas as entidades carregadas do banco continuam com NULL.

## Root Cause

```
1. Dados criados ANTES do fix → version = NULL no banco
2. ReservationService faz UPDATE nessas entidades (linha 114, 180)
3. Hibernate tenta incrementar version
4. version.longValue() → NullPointerException (version é null)
```

## Solução Completa (3 Partes)

### Parte 1: @Builder.Default (JÁ FEITO ✅)
Garante que **novas entidades** tenham version = 0L.

### Parte 2: @PreUpdate (ADICIONADO AGORA ✅)
Garante que version nunca seja null **antes de um update**.

```java
@PreUpdate
protected void onUpdate() {
    if (version == null) {
        version = 0L;  // Fallback de segurança
    }
}
```

### Parte 3: Corrigir Dados Existentes (NECESSÁRIO FAZER)
Atualizar o banco de dados para corrigir registros com NULL.

## Como Aplicar o Fix

### Opção 1: Corrigir Banco Existente (Recomendado)

**Passo 1**: Parar a aplicação
```bash
# Pressione Ctrl+C na aplicação Quarkus
```

**Passo 2**: Executar script de correção
```bash
./fix-database-versions.sh
```

Este script faz:
```sql
UPDATE seats SET version = 0 WHERE version IS NULL;
UPDATE events SET version = 0 WHERE version IS NULL;
```

**Passo 3**: Reiniciar aplicação
```bash
./mvnw quarkus:dev
```

**Passo 4**: Testar
```bash
./run-gatling-booking.sh -u 50 -d 60
```

### Opção 2: Recriar Banco do Zero (Mais Rápido)

**Passo 1**: Parar aplicação
```bash
# Pressione Ctrl+C
```

**Passo 2**: Recriar banco
```bash
# Parar containers
docker-compose down

# Remover volumes (apaga dados)
docker volume rm ticket-booking-system_postgres_data

# Iniciar novamente
docker-compose up -d
```

**Passo 3**: Seed novos dados
```bash
./seed-test-data.sh
```

**Passo 4**: Iniciar aplicação
```bash
./mvnw quarkus:dev
```

**Passo 5**: Testar
```bash
./run-gatling-booking.sh -u 50 -d 60
```

## O que foi Corrigido

### Event.java
```java
// ANTES
@PrePersist
protected void onCreate() {
    if (version == null) version = 0L;
}

// DEPOIS (adicionado)
@PreUpdate
protected void onUpdate() {
    if (version == null) version = 0L;  // ← NOVO
}
```

### Seat.java
```java
// ANTES
@PrePersist
protected void onCreate() {
    if (version == null) version = 0L;
}

// DEPOIS (adicionado)
@PreUpdate
protected void onUpdate() {
    if (version == null) version = 0L;  // ← NOVO
}
```

## Por Que @PreUpdate é Necessário?

### Ciclo de Vida JPA

```
1. LOAD da database → version = NULL (dados antigos)
2. Modificar entity (setStatus, etc)
3. @PreUpdate → version = 0L (AGORA!)
4. Hibernate flush → incrementa version (0L → 1L) ✅
```

**Sem @PreUpdate:**
```
1. LOAD da database → version = NULL
2. Modificar entity
3. Hibernate flush → version.longValue() → 💥 NullPointerException
```

## Verificar se Funcionou

### Verificar banco tem NULLs
```bash
docker exec -it postgres-ticket psql -U ticketuser -d ticketdb -c "
SELECT 'seats' as table, COUNT(*) as null_versions
FROM seats WHERE version IS NULL
UNION ALL
SELECT 'events' as table, COUNT(*) as null_versions
FROM events WHERE version IS NULL;"
```

**Resultado esperado:**
```
 table  | null_versions
--------+---------------
 seats  |             0
 events |             0
```

### Ver logs durante teste
```bash
# Não deve ter mais:
# ❌ NullPointerException: Cannot invoke "java.lang.Long.longValue()"

# Deve ter:
# ✅ Successfully reserved X seats
```

## Comparação: Antes vs Depois

| Aspecto | Antes | Depois |
|---------|-------|--------|
| **Novas entidades** | ❌ version = null | ✅ version = 0L (@Builder.Default) |
| **Dados existentes** | ❌ version = null | ✅ version = 0L (script SQL) |
| **Update entities** | ❌ NullPointerException | ✅ version inicializado (@PreUpdate) |
| **Success rate** | ~36% | **>90%** |

## Testes para Validar

```bash
# 1. Teste básico
./run-gatling-booking.sh -u 10 -d 30

# 2. Teste médio
./run-gatling-booking.sh -u 50 -d 60

# 3. Teste stress (se tudo funcionou)
./run-gatling-stress.sh
```

## Resumo dos Arquivos

**Código:**
- ✅ Event.java - adicionado @PreUpdate
- ✅ Seat.java - adicionado @PreUpdate
- ✅ Reservation.java, Booking.java - já têm @Builder.Default

**Scripts:**
- ✅ fix-null-versions.sql - SQL para corrigir dados
- ✅ fix-database-versions.sh - Script automático
- ✅ FIX_VERSION_COMPLETE.md - Este guia

## Troubleshooting

### Ainda tenho erro após script
**Causa**: Aplicação já tinha dados em cache

**Solução**:
```bash
# 1. Parar aplicação
# 2. Reexecutar script
./fix-database-versions.sh
# 3. Limpar target
./mvnw clean compile
# 4. Reiniciar
./mvnw quarkus:dev
```

### Script diz "container not running"
**Solução**:
```bash
docker-compose up -d
sleep 5
./fix-database-versions.sh
```

### Prefiro começar do zero
**Solução**:
```bash
docker-compose down -v
docker-compose up -d
./seed-test-data.sh
./mvnw quarkus:dev
```

## Próximos Passos

1. ✅ Executar fix do banco: `./fix-database-versions.sh`
2. ✅ Reiniciar aplicação: `./mvnw quarkus:dev`
3. ✅ Executar teste: `./run-gatling-booking.sh`
4. ✅ Verificar success rate > 90%
5. ✅ Analisar relatórios Gatling

## Resultado Esperado

```bash
./run-gatling-booking.sh -u 50 -d 60

# Resultado:
# ✅ Success rate: >90%
# ✅ P95 response time: <500ms
# ✅ No NullPointerException
# ✅ All assertions passed
```

Pronto para testes de carga! 🚀
