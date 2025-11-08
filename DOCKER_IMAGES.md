# Docker Images - Ticket Booking System

Este projeto oferece **3 variantes otimizadas** de imagens Docker, cada uma com características específicas de tamanho, segurança e desempenho.

## 📦 Variantes Disponíveis

### 1. Alpine Optimized (261 MB) - **Padrão**
**Tags:** `develop`, `develop-latest`, `develop-<sha>`

```bash
docker pull ghcr.io/<org>/ticket-booking-system:develop-latest
```

**Características:**
- ✅ Base: Alpine Linux + Eclipse Temurin JRE 21
- ✅ Tamanho: **261 MB**
- ✅ Multi-arquitetura: `linux/amd64`, `linux/arm64`
- ✅ Shell disponível (facilita debug)
- ✅ Boa compatibilidade geral
- ⭐ **Recomendada para desenvolvimento e testes**

**Dockerfile:** `src/main/docker/Dockerfile.jvm`

---

### 2. Distroless + jlink (134 MB) - **Mais Segura** 🔒
**Tags:** `develop-distroless`, `develop-distroless-latest`, `develop-distroless-<sha>`

```bash
docker pull ghcr.io/<org>/ticket-booking-system:develop-distroless-latest
```

**Características:**
- ✅ Base: Google Distroless (base-debian12) + JRE customizado via jlink
- ✅ Tamanho: **134 MB** (-72% vs original)
- ✅ Multi-arquitetura: `linux/amd64`, `linux/arm64`
- 🔒 **Máxima segurança**: sem shell, package manager ou utilitários
- 🔒 Superfície de ataque mínima
- ✅ Apenas módulos Java necessários
- ⚠️ Debug mais complexo (sem shell)
- ⭐ **Recomendada para produção e ambientes regulados**

**Dockerfile:** `src/main/docker/Dockerfile.jvm.distroless-jlink`

---

### 3. Ultra-optimized Alpine + jlink (125 MB) - **Menor** 🏆
**Tags:** `develop-optimized`, `develop-optimized-latest`, `develop-optimized-<sha>`

```bash
docker pull ghcr.io/<org>/ticket-booking-system:develop-optimized-latest
```

**Características:**
- ✅ Base: Alpine Linux + JRE customizado via jlink
- ✅ Tamanho: **125 MB** (-73% vs original)
- ✅ Multi-arquitetura: `linux/amd64`, `linux/arm64`
- ✅ Shell disponível (facilita debug)
- ✅ Menor tamanho absoluto
- ⚠️ Alpine usa musl libc (possíveis incompatibilidades)
- ⭐ **Recomendada para ambientes com restrições de espaço**

**Dockerfile:** `src/main/docker/Dockerfile.jvm.optimized`

---

## 📊 Comparação Rápida

| Variante | Tamanho | Segurança | Debug | Customização JVM | Uso Recomendado |
|----------|---------|-----------|-------|------------------|-----------------|
| **Alpine Optimized** | 261 MB | ⭐⭐⭐ | ✅ Fácil | `JAVA_OPTS` | Dev/Test/Prod Geral |
| **Distroless + jlink** | 134 MB | ⭐⭐⭐⭐⭐ | ⚠️ Difícil | `JAVA_TOOL_OPTIONS` | Produção Segura |
| **Ultra-optimized** | 125 MB | ⭐⭐⭐ | ✅ Fácil | `JAVA_OPTS` | Restrições de Espaço |

## 🚀 Como Usar

### Desenvolvimento Local

```bash
# Variante padrão
docker build -f src/main/docker/Dockerfile.jvm -t ticket-booking:dev .

# Variante distroless
docker build -f src/main/docker/Dockerfile.jvm.distroless-jlink -t ticket-booking:distroless .

# Variante ultra-otimizada
docker build -f src/main/docker/Dockerfile.jvm.optimized -t ticket-booking:optimized .
```

### Executar Container

```bash
# Padrão
docker run -p 8080:8080 ghcr.io/<org>/ticket-booking-system:develop-latest

# Distroless (mais segura)
docker run -p 8080:8080 ghcr.io/<org>/ticket-booking-system:develop-distroless-latest

# Ultra-otimizada (menor)
docker run -p 8080:8080 ghcr.io/<org>/ticket-booking-system:develop-optimized-latest
```

### Debug de Containers

**Alpine Optimized & Ultra-optimized** (com shell):
```bash
docker exec -it <container-id> sh
```

**Distroless** (sem shell - use debug tools):
```bash
# Usar kubectl debug ou ferramentas externas
kubectl debug -it <pod-name> --image=busybox --target=<container-name>
```

## 🔧 Customização de JVM Options

### Alpine Optimized & Ultra-optimized (com shell)

Estas variantes usam `JAVA_OPTS` que pode ser facilmente customizado:

```bash
# Via docker run
docker run -e JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC" \
  ghcr.io/<org>/ticket-booking-system:develop-latest

# Via docker-compose
services:
  app:
    image: ghcr.io/<org>/ticket-booking-system:develop-latest
    environment:
      JAVA_OPTS: "-Xmx512m -Xms256m -XX:+UseG1GC"

# Via Kubernetes
env:
  - name: JAVA_OPTS
    value: "-Xmx512m -Xms256m -XX:+UseG1GC"
```

**Valores Padrão:**
```bash
JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 \
  -Djava.util.logging.manager=org.jboss.logmanager.LogManager \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0"
```

### Distroless (sem shell)

Estas variantes usam `JAVA_TOOL_OPTIONS` que é automaticamente lido pela JVM:

```bash
# Via docker run
docker run -e JAVA_TOOL_OPTIONS="-Xmx512m -Xms256m -XX:+UseG1GC" \
  ghcr.io/<org>/ticket-booking-system:develop-distroless-latest

# Via docker-compose
services:
  app:
    image: ghcr.io/<org>/ticket-booking-system:develop-distroless-latest
    environment:
      JAVA_TOOL_OPTIONS: "-Xmx512m -Xms256m -XX:+UseG1GC"

# Via Kubernetes
env:
  - name: JAVA_TOOL_OPTIONS
    value: "-Xmx512m -Xms256m -XX:+UseG1GC"
```

**Valores Padrão:**
```bash
JAVA_TOOL_OPTIONS="-Dquarkus.http.host=0.0.0.0 \
  -Djava.util.logging.manager=org.jboss.logmanager.LogManager \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0"
```

### ⚠️ Importante

- **Alpine/Ultra-optimized**: Use `JAVA_OPTS`
- **Distroless**: Use `JAVA_TOOL_OPTIONS`
- Ao customizar, você **substitui** completamente os valores padrão (não adiciona)
- Certifique-se de incluir `-Dquarkus.http.host=0.0.0.0` nas suas customizações

### Exemplos Comuns de Customização

```bash
# Aumentar memória heap
JAVA_OPTS="-Xmx1g -Xms512m -Dquarkus.http.host=0.0.0.0"

# Usar G1GC em vez do padrão
JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dquarkus.http.host=0.0.0.0"

# Habilitar debug remoto
JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -Dquarkus.http.host=0.0.0.0"

# JVM Flight Recorder
JAVA_OPTS="-XX:StartFlightRecording=duration=60s,filename=/tmp/recording.jfr -Dquarkus.http.host=0.0.0.0"
```

## 🔧 Outras Variáveis de Ambiente

Todas as variantes também suportam configuração Quarkus via variáveis de ambiente:

```bash
# Quarkus Config
QUARKUS_HTTP_PORT=8080
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://db:5432/ticketdb
QUARKUS_REDIS_HOSTS=redis://redis:6379
QUARKUS_LOG_LEVEL=INFO
```

## 📈 Estatísticas de Build

### Tempo de Build (aproximado)
- Alpine Optimized: ~2-3 minutos
- Distroless + jlink: ~3-4 minutos (jlink adiciona tempo)
- Ultra-optimized: ~3-4 minutos (jlink adiciona tempo)

### Camadas Docker
- Alpine Optimized: ~8 camadas
- Distroless + jlink: ~6 camadas
- Ultra-optimized: ~7 camadas

## 🔐 Segurança

### Distroless + jlink (Mais Segura)
- ✅ Sem shell ou package manager
- ✅ Sem utilitários de sistema
- ✅ Apenas binário Java + libs necessárias
- ✅ Usuário non-root por padrão
- ✅ Imagem base do Google (auditada)

### Alpine Optimized & Ultra-optimized
- ✅ Usuário non-root configurado
- ✅ Imagem base mínima
- ⚠️ Inclui shell (útil para debug, mas aumenta superfície de ataque)

## 🏗️ CI/CD

O GitHub Actions builda automaticamente as 3 variantes em cada push para `develop`:

```yaml
# .github/workflows/ci-develop.yml
# Variante 1: Alpine Optimized
- Build and push JVM Docker image (Alpine optimized)
  Tags: develop, develop-latest, develop-<sha>

# Variante 2: Distroless + jlink
- Build and push Distroless+jlink Docker image (134MB - Most Secure)
  Tags: develop-distroless, develop-distroless-latest, develop-distroless-<sha>

# Variante 3: Ultra-optimized
- Build and push Ultra-optimized Docker image (125MB - Smallest)
  Tags: develop-optimized, develop-optimized-latest, develop-optimized-<sha>
```

## 📝 Recomendações

### Para Produção
**Use: Distroless + jlink** (`develop-distroless-latest`)
- Máxima segurança
- Tamanho reduzido
- Conformidade com melhores práticas

### Para Ambientes Regulados (HIPAA, PCI-DSS, etc.)
**Use: Distroless + jlink** (`develop-distroless-latest`)
- Minimiza superfície de ataque
- Auditoria simplificada
- Menor risco de vulnerabilidades

### Para Desenvolvimento/Testes
**Use: Alpine Optimized** (`develop-latest`)
- Facilita debug
- Boa compatibilidade
- Tamanho razoável

### Para Edge/IoT/Ambientes Limitados
**Use: Ultra-optimized** (`develop-optimized-latest`)
- Menor tamanho possível
- Menos uso de rede
- Menor uso de armazenamento

## 🔄 Migração entre Variantes

Todas as variantes são funcionalmente equivalentes. Para migrar:

```bash
# De Alpine Optimized para Distroless
docker pull ghcr.io/<org>/ticket-booking-system:develop-distroless-latest

# Atualizar compose/k8s manifest
image: ghcr.io/<org>/ticket-booking-system:develop-distroless-latest
```

**Nota:** Lembre-se que Distroless não tem shell, então ajuste seus scripts de healthcheck/debug se necessário.

## 📚 Mais Informações

- [Dockerfile Padrão](src/main/docker/Dockerfile.jvm)
- [Dockerfile Distroless](src/main/docker/Dockerfile.jvm.distroless-jlink)
- [Dockerfile Ultra-optimized](src/main/docker/Dockerfile.jvm.optimized)
- [CI/CD Workflow](.github/workflows/ci-develop.yml)
