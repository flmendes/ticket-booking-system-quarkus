# GitHub Actions CI/CD

## Overview

This project uses GitHub Actions for continuous integration and deployment with separate workflows for different branches and purposes.

## Workflows

### 1. CI/CD - Develop Branch (JVM Image)

**File**: `.github/workflows/ci-develop.yml`

**Triggers**:
- Push to `develop` branch
- Pull requests to `develop` branch

**Pipeline Steps**:

#### Step 1: Compile and Test
- Sets up JDK 21 with Temurin distribution
- Runs PostgreSQL and Redis services
- Compiles the project
- Executes unit tests
- Runs integration tests
- Generates test reports
- Uploads test coverage artifacts

#### Step 2: Code Quality Analysis
- Runs SonarCloud analysis
- Executes SpotBugs for bug detection
- Runs Checkstyle for code style validation
- Uploads code quality reports

#### Step 3: Security Scanning
- Trivy vulnerability scanner (filesystem)
- OWASP Dependency Check (CVSS threshold: 7)
- Uploads security reports to GitHub Security

#### Step 4: Build JVM Docker Image
- Builds Quarkus application
- Creates multi-platform Docker image (linux/amd64, linux/arm64)
- Pushes to GitHub Container Registry
- Scans Docker image with Trivy
- Generates Software Bill of Materials (SBOM)
- Tags: `develop-latest`, `develop-{sha}`

**Image Registry**: `ghcr.io/{owner}/{repo}:develop-latest`

---

### 2. CI/CD - Main Branch (Native Image)

**File**: `.github/workflows/ci-main.yml`

**Triggers**:
- Push to `main`/`master` branch
- Pull requests to `main`/`master` branch
- Release publication

**Pipeline Steps**:

#### Step 1: Compile and Test
- Sets up GraalVM 21
- Runs PostgreSQL and Redis services
- Compiles the project
- Executes unit and integration tests
- Generates comprehensive test reports

#### Step 2: Code Quality Analysis
- SonarCloud with quality gate enforcement
- SpotBugs analysis
- Checkstyle validation
- PMD analysis
- Stricter quality standards than develop

#### Step 3: Security Scanning
- Trivy vulnerability scanner (CRITICAL, HIGH)
- OWASP Dependency Check (CVSS threshold: 7)
- Snyk security scan
- Comprehensive security reporting

#### Step 4: Build Native Docker Image
- Builds GraalVM native executable
- Creates optimized native Docker image
- Pushes to GitHub Container Registry
- Scans native image for vulnerabilities
- Generates SBOM for native image
- Performance testing (on releases)
- Tags: `latest`, `{version}`, `main-{sha}`

**Image Registry**: `ghcr.io/{owner}/{repo}:latest`

**Build Time**: ~30-45 minutes (native compilation)

---

### 3. Pull Request Validation

**File**: `.github/workflows/pr-validation.yml`

**Triggers**:
- Pull requests to any branch
- PR opened, synchronized, or reopened

**Pipeline Steps**:
- Quick compilation check
- Unit test execution
- Code style validation
- Breaking changes detection
- API compatibility check
- Database migration check
- Code size analysis

**Purpose**: Fast feedback for pull requests without full image build

---

## Required Secrets

Configure the following secrets in your GitHub repository settings:

### Required
- `GITHUB_TOKEN` - Automatically provided by GitHub Actions

### Optional (for enhanced features)
- `SONAR_TOKEN` - SonarCloud authentication token
- `SNYK_TOKEN` - Snyk security scanning token

### Setting Up Secrets

1. Go to repository **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**
3. Add each secret with its value

---

## SonarCloud Setup

### Steps to Configure SonarCloud

1. **Create SonarCloud Account**
   - Go to [SonarCloud](https://sonarcloud.io)
   - Sign up with GitHub account

2. **Import Repository**
   - Click **+** → **Analyze new project**
   - Select your repository
   - Choose **GitHub Actions** as CI/CD platform

3. **Generate Token**
   - Go to **Account** → **Security**
   - Generate new token
   - Add as `SONAR_TOKEN` secret in GitHub

4. **Update Workflow**
   - Project key: `{owner}_ticket-booking-system`
   - Organization: `{owner}`

---

## Docker Image Usage

### Pulling Images

#### Development (JVM)
```bash
# Pull latest develop build
docker pull ghcr.io/{owner}/{repo}:develop-latest

# Run the image
docker run -d \
  -p 8080:8080 \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://postgres:5432/ticketing \
  -e QUARKUS_REDIS_HOSTS=redis://redis:6379 \
  ghcr.io/{owner}/{repo}:develop-latest
```

#### Production (Native)
```bash
# Pull latest production build
docker pull ghcr.io/{owner}/{repo}:latest

# Run the native image
docker run -d \
  -p 8080:8080 \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://postgres:5432/ticketing \
  -e QUARKUS_REDIS_HOSTS=redis://redis:6379 \
  ghcr.io/{owner}/{repo}:latest
```

### Image Comparison

| Feature | JVM Image (Develop) | Native Image (Main) |
|---------|---------------------|---------------------|
| **Startup Time** | ~2-3 seconds | ~0.05 seconds |
| **Memory Usage** | ~200-300 MB | ~50-100 MB |
| **Image Size** | ~400-500 MB | ~150-200 MB |
| **Build Time** | ~2-3 minutes | ~30-45 minutes |
| **Platform** | Multi-arch (amd64, arm64) | Single-arch (amd64) |
| **Use Case** | Development, Testing | Production |

---

## Build Status Badges

Add these badges to your README.md:

```markdown
![Develop CI](https://github.com/{owner}/{repo}/workflows/CI%2FCD%20-%20Develop%20(JVM)/badge.svg?branch=develop)
![Main CI](https://github.com/{owner}/{repo}/workflows/CI%2FCD%20-%20Main%20(Native)/badge.svg?branch=main)
![PR Validation](https://github.com/{owner}/{repo}/workflows/Pull%20Request%20Validation/badge.svg)
```

---

## Workflow Configuration

### Customizing Build Behavior

#### Adjust Test Coverage Requirements

Edit `.github/workflows/ci-main.yml`:

```yaml
- name: Run tests with coverage
  run: ./mvnw verify jacoco:report

- name: Check coverage threshold
  run: ./mvnw jacoco:check -Djacoco.threshold=80
```

#### Modify Security Scan Thresholds

```yaml
- name: Run OWASP Dependency Check
  run: |
    ./mvnw org.owasp:dependency-check-maven:check \
      -DfailBuildOnCVSS=9  # Change from 7 to 9 for stricter checks
```

#### Change Docker Build Platforms

```yaml
platforms: linux/amd64,linux/arm64,linux/arm/v7
```

---

## Monitoring and Troubleshooting

### Viewing Workflow Runs

1. Go to **Actions** tab in GitHub
2. Select workflow from left sidebar
3. Click on specific run to see details

### Common Issues

#### 1. Native Build Timeout

**Problem**: Native build exceeds 60 minutes

**Solution**: Increase timeout in workflow
```yaml
timeout-minutes: 90
```

#### 2. Test Failures with Services

**Problem**: Tests fail to connect to PostgreSQL/Redis

**Solution**: Verify service health checks
```yaml
options: >-
  --health-cmd pg_isready
  --health-interval 10s
  --health-timeout 5s
  --health-retries 5
```

#### 3. Docker Push Permission Denied

**Problem**: Cannot push to GitHub Container Registry

**Solution**: Ensure proper permissions
```yaml
permissions:
  contents: read
  packages: write
```

#### 4. SonarCloud Analysis Fails

**Problem**: SonarCloud token invalid

**Solution**:
- Verify `SONAR_TOKEN` secret is set
- Check token hasn't expired
- Regenerate token if necessary

---

## Performance Optimization

### Caching Strategy

The workflows use aggressive caching:

```yaml
- name: Cache Maven packages
  uses: actions/cache@v4
  with:
    path: ~/.m2
    key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
```

### Parallel Job Execution

Jobs run in parallel where possible:
- `compile-and-test` (first)
- `code-quality` and `security-scan` (parallel, after tests)
- `build-*-image` (after all checks pass)

---

## Release Process

### Creating a Release

1. **Tag the release**
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```

2. **Create GitHub Release**
   - Go to **Releases** → **Draft a new release**
   - Select tag
   - Generate release notes
   - Publish release

3. **Automatic Actions**
   - Native image build triggered
   - Performance tests executed
   - Docker images tagged with version
   - SBOM generated and attached

### Release Tags

The workflow automatically creates semantic version tags:

- `latest` - Latest stable release
- `v1.0.0` - Specific version
- `v1.0` - Minor version
- `v1` - Major version

---

## Cost Optimization

### GitHub Actions Minutes

- Public repositories: **Unlimited**
- Private repositories: 2,000 minutes/month (free tier)

### Optimization Tips

1. **Skip redundant workflows**
   ```yaml
   on:
     push:
       branches: [ develop ]
       paths-ignore:
         - '**.md'
         - 'docs/**'
   ```

2. **Use smaller runners**
   - Standard: `ubuntu-latest`
   - Larger: `ubuntu-latest-8-cores` (if needed)

3. **Cancel outdated runs**
   ```yaml
   concurrency:
     group: ${{ github.workflow }}-${{ github.ref }}
     cancel-in-progress: true
   ```

---

## Security Best Practices

### 1. Dependency Scanning
- OWASP Dependency Check runs on every build
- Trivy scans both filesystem and container images
- Snyk provides additional vulnerability detection

### 2. SBOM Generation
- CycloneDX format
- Includes all dependencies
- Retained for 90 days (main), 30 days (develop)

### 3. Security Reporting
- Results uploaded to GitHub Security tab
- SARIF format for standardization
- Integration with GitHub Advanced Security

### 4. Secret Management
- Never commit secrets to repository
- Use GitHub Secrets for sensitive data
- Rotate tokens regularly

---

## Integration with Other Tools

### Dependabot

Create `.github/dependabot.yml`:

```yaml
version: 2
updates:
  - package-ecosystem: "maven"
    directory: "/"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 5
```

### CodeQL Analysis

Already integrated in security scanning steps for SARIF upload.

---

## Support and Troubleshooting

### Getting Help

1. Check [GitHub Actions Documentation](https://docs.github.com/en/actions)
2. Review workflow run logs in Actions tab
3. Check [Quarkus CI/CD Guide](https://quarkus.io/guides/deploying-to-kubernetes)

### Logs and Artifacts

- **Test Coverage**: Available for 7-30 days
- **Security Reports**: Available for 7-30 days
- **SBOM**: Available for 30-90 days
- **Build Logs**: Available for 90 days

---

**Last Updated**: 2025-11-07
**Maintainer**: Development Team
