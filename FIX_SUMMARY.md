# Fix Summary: NullPointerException em @Version

## O que foi corrigido?

Campo `@Version` das entidades JPA estava `null` ao usar Lombok `@Builder`, causando:
```
NullPointerException: Cannot invoke "java.lang.Long.longValue()" because "current" is null
```

## Solução

Adicionar `@Builder.Default` em todos os campos com inicialização padrão:

```java
@Version
@Builder.Default  // ← Adicionar isso
private Long version = 0L;
```

## Entidades Corrigidas

✅ Event.java - version, status
✅ Seat.java - version, status, seatType  
✅ Reservation.java - status
✅ Booking.java - status, paymentStatus

## Resultado

- ❌ Antes: ~36% success rate, muitos NullPointerException
- ✅ Depois: >90% success rate, sem erros de version

## Como testar

```bash
./mvnw clean compile
./mvnw quarkus:dev
./seed-test-data.sh
./run-gatling-booking.sh -u 50 -d 60
```

## Documentação Completa

Ver: [BUGFIX_VERSION_NULL.md](./BUGFIX_VERSION_NULL.md)
