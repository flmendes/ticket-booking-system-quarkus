# Setting Main as Default Branch in GitHub

This guide explains how to set `main` as the default branch for your GitHub repository.

## Current Configuration

- Repository: https://github.com/flmendes/ticket-booking-system-quarkus.git
- Branches:
  - `main` - Production releases (Native GraalVM builds)
  - `develop` - Development branch (JVM builds)
  - `feature_add_vertical_slicing` - Feature branch

## Steps to Set Main as Default Branch

### Option 1: Using GitHub Web Interface (Recommended)

1. **Navigate to Repository Settings**
   - Go to https://github.com/flmendes/ticket-booking-system-quarkus
   - Click on **Settings** tab (requires admin access)

2. **Access Branches Settings**
   - In the left sidebar, click **Branches**
   - Look for the "Default branch" section

3. **Change Default Branch**
   - Click the switch icon (⇄) next to the current default branch
   - Select **main** from the dropdown menu
   - Click **Update** button
   - Confirm by clicking **I understand, update the default branch**

4. **Verify the Change**
   - The default branch should now show as `main`
   - New pull requests will target `main` by default
   - Clone operations will check out `main` by default

### Option 2: Using GitHub CLI

If you have GitHub CLI installed:

```bash
# Install gh CLI if not already installed (macOS)
brew install gh

# Login to GitHub
gh auth login

# Set main as default branch
gh repo edit flmendes/ticket-booking-system-quarkus --default-branch main
```

### Option 3: Using GitHub API

```bash
# Using curl with GitHub API
curl -X PATCH \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer YOUR_GITHUB_TOKEN" \
  https://api.github.com/repos/flmendes/ticket-booking-system-quarkus \
  -d '{"default_branch":"main"}'
```

## Post-Change Updates

After setting `main` as the default branch, you may want to:

### 1. Update Local Git Configuration

```bash
# Update your local repository to track main
git branch --set-upstream-to=origin/main main

# Fetch latest changes
git fetch origin

# Switch to main branch
git checkout main

# Pull latest changes
git pull origin main
```

### 2. Update CI/CD Workflows

The GitHub Actions workflows are already configured to handle both branches:
- `.github/workflows/ci-main.yml` - Runs on pushes to main/master
- `.github/workflows/ci-develop.yml` - Runs on pushes to develop
- `.github/workflows/pr-validation.yml` - Runs on PRs to develop/main/master

No changes needed for workflows!

### 3. Update Branch Protection Rules (Recommended)

After setting main as default, consider adding branch protection:

1. Go to **Settings** → **Branches**
2. Click **Add rule** under "Branch protection rules"
3. Set "Branch name pattern" to `main`
4. Enable recommended protections:
   - ✅ Require a pull request before merging
   - ✅ Require status checks to pass before merging
     - Select: `Compile and Test`
     - Select: `Code Quality Analysis`
     - Select: `Security Scanning`
   - ✅ Require conversation resolution before merging
   - ✅ Do not allow bypassing the above settings
   - ✅ Restrict who can push to matching branches (optional)

5. Click **Create** to save the rule

### 4. Update Documentation References

The following documentation files reference branches and have been reviewed:
- `README.md` - No changes needed (generic instructions)
- `GITHUB_ACTIONS.md` - Already documents both main and develop branches
- `PROJECT_STRUCTURE.md` - Already documents the branching strategy
- `.github/README.md` - Already explains the dual-branch strategy

## Branch Strategy

After setting main as default, the recommended workflow is:

```
main (default, production, native builds)
  ↑
  PR from develop
  ↑
develop (JVM builds, integration)
  ↑
  PR from feature branches
  ↑
feature/* (new features)
```

### Development Workflow

1. **Create Feature Branch**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/my-new-feature
   ```

2. **Develop and Commit**
   ```bash
   git add .
   git commit -m "Add new feature"
   git push origin feature/my-new-feature
   ```

3. **Create PR to develop**
   - Create PR from `feature/my-new-feature` → `develop`
   - Wait for CI checks to pass
   - Get code review approval
   - Merge to develop

4. **Release to Production**
   - Create PR from `develop` → `main`
   - Wait for all CI checks to pass
   - Get approval
   - Merge to main
   - Native GraalVM image is built and pushed to registry

## Verification

After changing the default branch, verify:

```bash
# Check remote information
git remote show origin

# Should show:
# HEAD branch: main
# Remote branches:
#   develop  tracked
#   main     tracked
```

Test cloning (in a new directory):
```bash
cd /tmp
git clone https://github.com/flmendes/ticket-booking-system-quarkus.git test-clone
cd test-clone
git branch
# Should show: * main (indicating main is default)
```

## Troubleshooting

### Issue: "You don't have permission to change default branch"

**Solution**: You need admin access to the repository. Contact the repository owner or ask them to:
1. Add you as an admin (Settings → Collaborators → Add people with Admin role)
2. Change the default branch themselves

### Issue: "Default branch cannot be deleted"

**Solution**: This is expected behavior. GitHub prevents deletion of the default branch. If you need to delete main:
1. First change default to another branch (e.g., develop)
2. Then delete main
3. Then set default back to main (after recreating it)

### Issue: Open PRs targeting wrong branch

**Solution**: After changing default branch, existing PRs remain targeting the old default. For each PR:
1. Edit the PR
2. Click "Edit" next to the target branch
3. Select `main` as the new target
4. Save changes

## Benefits of Main as Default

1. **Industry Standard**: Most projects use `main` as default (GitHub's standard)
2. **Production-Ready**: Main represents production code with native builds
3. **Better PR Flow**: New PRs automatically target production branch
4. **Cleaner Onboarding**: New contributors clone main by default
5. **Clear Semantics**: main = production, develop = integration

## Related Documentation

- [GitHub Actions CI/CD](GITHUB_ACTIONS.md) - CI/CD pipeline documentation
- [Project Structure](PROJECT_STRUCTURE.md) - Overall project structure
- [GitHub Branching Docs](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-branches-in-your-repository/changing-the-default-branch)
