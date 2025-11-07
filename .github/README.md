# GitHub Actions Configuration

## Quick Setup Guide

This directory contains GitHub Actions workflows for CI/CD automation.

## Workflows

### 1. `ci-develop.yml` - Development Pipeline (JVM)
Runs on every push/PR to `develop` branch. Builds JVM Docker images.

### 2. `ci-main.yml` - Production Pipeline (Native)
Runs on every push/PR to `main` branch. Builds native GraalVM images.

### 3. `pr-validation.yml` - Pull Request Validation
Quick checks for all pull requests.

## Initial Setup

### Step 1: Enable GitHub Actions

1. Go to repository **Settings** → **Actions** → **General**
2. Under "Actions permissions", select: **Allow all actions and reusable workflows**
3. Save changes

### Step 2: Configure Container Registry

1. Go to repository **Settings** → **Packages**
2. Ensure package visibility is set appropriately
3. No additional configuration needed (GITHUB_TOKEN is automatic)

### Step 3: Optional - Configure SonarCloud

1. Sign up at [SonarCloud](https://sonarcloud.io)
2. Import your repository
3. Generate a token
4. Add as `SONAR_TOKEN` secret in repository settings

### Step 4: Optional - Configure Snyk

1. Sign up at [Snyk](https://snyk.io)
2. Get your API token
3. Add as `SNYK_TOKEN` secret in repository settings

## Testing Locally

You can test the Docker builds locally:

### JVM Build
```bash
# Build application
./mvnw clean package -DskipTests

# Build Docker image
docker build -f src/main/docker/Dockerfile.jvm -t test-jvm .

# Run
docker run -p 8080:8080 test-jvm
```

### Native Build
```bash
# Build native (requires GraalVM or use container build)
./mvnw clean package -Dnative -Dquarkus.native.container-build=true

# Build Docker image
docker build -f src/main/docker/Dockerfile.native-micro -t test-native .

# Run
docker run -p 8080:8080 test-native
```

## Triggering Workflows

### Manual Trigger

You can manually trigger workflows from the Actions tab:

1. Go to **Actions**
2. Select workflow
3. Click **Run workflow**
4. Select branch
5. Click **Run workflow**

### Automatic Triggers

Workflows trigger automatically on:
- **Develop**: Push or PR to `develop` branch
- **Main**: Push or PR to `main` branch, or release creation
- **PR Validation**: Any pull request

## Monitoring

### View Workflow Status

1. Go to **Actions** tab
2. Select workflow from left sidebar
3. Click on specific run

### Check Build Artifacts

1. Open workflow run
2. Scroll to bottom of page
3. Download artifacts (test reports, coverage, etc.)

### View Docker Images

1. Go to repository main page
2. Click **Packages** on right sidebar
3. Select package to view all tags

## Troubleshooting

### Build Fails on Test Step

Check PostgreSQL/Redis services are healthy:
```yaml
services:
  postgres:
    options: >-
      --health-cmd pg_isready
      --health-interval 10s
```

### Docker Push Fails

Ensure permissions are correct:
```yaml
permissions:
  contents: read
  packages: write
```

### Native Build Takes Too Long

Increase timeout:
```yaml
timeout-minutes: 90
```

## Customization

### Modify Build Steps

Edit the YAML files to customize:

1. Test commands
2. Code quality checks
3. Security scan thresholds
4. Docker build options
5. Deployment targets

### Add New Workflows

Create new `.yml` files in this directory following the same structure.

## Security

- Never commit secrets to the repository
- Use GitHub Secrets for sensitive data
- Review security scan results regularly
- Keep dependencies updated

## Support

For detailed documentation, see:
- [GITHUB_ACTIONS.md](../GITHUB_ACTIONS.md) - Complete CI/CD guide
- [PROJECT_STRUCTURE.md](../PROJECT_STRUCTURE.md) - Project overview

---

**Last Updated**: 2025-11-07
