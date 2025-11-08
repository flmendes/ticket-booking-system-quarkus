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

| Variante | Tamanho | Segurança | Debug | Uso Recomendado |
|----------|---------|-----------|-------|-----------------|
| **Alpine Optimized** | 261 MB | ⭐⭐⭐ | ✅ Fácil | Dev/Test/Prod Geral |
| **Distroless + jlink** | 134 MB | ⭐⭐⭐⭐⭐ | ⚠️ Difícil | Produção Segura |
| **Ultra-optimized** | 125 MB | ⭐⭐⭐ | ✅ Fácil | Restrições de Espaço |

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

## 🔧 Variáveis de Ambiente

Todas as variantes suportam as mesmas variáveis de ambiente:

```bash
# Java Options
JAVA_OPTS="-Xmx512m -Xms256m"

# Quarkus Config
QUARKUS_HTTP_PORT=8080
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://db:5432/ticketdb
QUARKUS_REDIS_HOSTS=redis://redis:6379
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
