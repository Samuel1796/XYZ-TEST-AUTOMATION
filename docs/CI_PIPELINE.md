# CI Pipeline Explanation (GitHub Actions)

This document explains the CI workflow in `.github/workflows/test-automation.yml`, including what a runner is, how jobs run, and what each step does.

---

## What is a Runner?

A **runner** is a machine that executes your GitHub Actions jobs. GitHub provides hosted runners (e.g., `ubuntu-latest`), or you can use self-hosted runners. In this project:

- **Runner**: `ubuntu-latest` (GitHub-hosted Linux VM)
- **Purpose**: It checks out the code, installs dependencies, runs tests, and generates/publishes reports.

The runner is temporary and starts clean on each workflow run.

---

## Workflow Overview

**Workflow name:** `CI - Selenium UI Tests`

**Triggers:**
- `push` to `master`
- manual trigger (`workflow_dispatch`)

**Job:** `ui-tests-xyz-bank`
- Runs on `ubuntu-latest`
- Has `contents: write` permission (needed to publish Allure report to `gh-pages`)

---

## Step-by-Step Explanation

Below is the full flow of the job and what each step does.

### 1. Checkout
```yaml
- name: Checkout
  uses: actions/checkout@v4
```
**What it does:**
- Downloads the repository into the runner so the workflow can access the code.

---

### 2. Set up Java 17
```yaml
- name: Set up Java 17
  uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: "17"
    cache: maven
```
**What it does:**
- Installs Java 17 (Temurin distribution).
- Enables Maven dependency caching to speed up builds.

---

### 3. Print target URL
```yaml
- name: Print target URL
  run: 'echo "Testing URL: ${{ secrets.APP_BASE_URL }}"'
```
**What it does:**
- Logs the application URL from the `APP_BASE_URL` secret.
- Useful for verifying the target environment during CI runs.

---

### 4. Install Chrome and ChromeDriver (matching versions)
```yaml
- name: Install Chrome and ChromeDriver (matching versions)
  run: |
    set -e
    CHROME_JSON="https://googlechromelabs.github.io/chrome-for-testing/last-known-good-versions-with-downloads.json"
    CHROME_VERSION=$(curl -sS "$CHROME_JSON" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['channels']['Stable']['version'])")
    echo "Chrome/ChromeDriver version: $CHROME_VERSION"
    BASE="https://storage.googleapis.com/chrome-for-testing-public/$CHROME_VERSION/linux64"
    cd /tmp
    curl -sSLO "$BASE/chrome-linux64.zip"
    curl -sSLO "$BASE/chromedriver-linux64.zip"
    unzip -o chrome-linux64.zip
    unzip -o chromedriver-linux64.zip
    sudo mv chrome-linux64 /opt/chrome-for-testing
    sudo mv chromedriver-linux64/chromedriver /usr/local/bin/chromedriver
    sudo chmod +x /usr/local/bin/chromedriver
    echo "CHROME_BIN=/opt/chrome-for-testing/chrome" >> $GITHUB_ENV
    echo "/opt/chrome-for-testing/chrome" >> $GITHUB_PATH
    /opt/chrome-for-testing/chrome --version
    chromedriver --version
```
**What it does:**
- Downloads a **matching** version of Chrome and ChromeDriver.
- Ensures Selenium can start Chrome reliably.
- Sets `CHROME_BIN` so Selenium can use the exact Chrome binary.

---

### 5. Run Selenium tests
```yaml
- name: Run Selenium tests
  env:
    CHROME_BIN: ${{ env.CHROME_BIN }}
  run: |
    mvn -B -e test \
      -Dheadless.mode=true \
      -Dbase.url=${{ secrets.APP_BASE_URL }} \
      -DtrimStackTrace=false \
      -Dallure.executor.url=${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }} \
      -Dallure.executor.reportUrl=https://${{ github.repository_owner }}.github.io/${{ github.event.repository.name }}/ \
      -Dallure.executor.buildName="CI #${{ github.run_number }} ${{ github.ref_name }}" \
      -Dallure.executor.buildOrder=${{ github.run_number }}
```
**What it does:**
- Runs all tests in **headless** mode.
- Injects the target URL (`base.url`) from secrets.
- Writes Allure executor metadata so the report links back to this CI run.

**Why `-B -e`:**
- `-B` = batch mode (non-interactive logs)
- `-e` = show full stack traces on errors

---

### 6. Test summary (Readable logs)
```yaml
- name: Test summary (Readable logs)
  if: always()
  run: |
    python3 - <<'PY'
    ...
    PY
```
**What it does:**
- Parses Surefire XML reports.
- Prints a clean summary with totals and failed tests.
- Fails the job if there were any test failures.

---

### 7. Build Slack payload and email body
```yaml
- name: Build Slack payload and email body
  if: always()
  run: |
    python3 - <<'PY'
    ...
    PY
```
**What it does:**
- Builds:
  - `slack_payload.json` (for Slack notification)
  - `email_body.txt` (for email notification)
- Includes failures, run URL, and totals.

---

### 8. Notify Slack
```yaml
- name: Notify Slack
  if: always()
  continue-on-error: true
  uses: slackapi/slack-github-action@v1.27.0
  with:
    payload-file-path: slack_payload.json
  env:
    SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```
**What it does:**
- Sends the Slack payload if a webhook URL is provided.
- `continue-on-error: true` prevents job failure if Slack is not configured.

---

### 9. Send Email report
```yaml
- name: Send Email report
  if: always() && vars.SEND_EMAIL == 'true'
  uses: dawidd6/action-send-mail@v3
  with:
    server_address: ${{ secrets.SMTP_SERVER }}
    server_port: ${{ secrets.SMTP_PORT }}
    username: ${{ secrets.SMTP_USERNAME }}
    password: ${{ secrets.SMTP_PASSWORD }}
    subject: Selenium UI Tests - ${{ job.status == 'success' && 'PASS' || 'FAIL' }} - ${{ github.repository }} - ${{ github.ref_name }}
    to: ${{ secrets.EMAIL_TO }}
    from: ${{ secrets.SMTP_USERNAME }}
    secure: false
    body: file://email_body.txt
```
**What it does:**
- Sends an email summary when `SEND_EMAIL` is set to `true` in repository variables.
- Uses SMTP credentials stored in GitHub secrets.

---

### 10. Print Surefire reports (on failure)
```yaml
- name: Print Surefire reports (on failure)
  if: failure()
  run: |
    ...
```
**What it does:**
- Prints failing Surefire logs to help debugging without downloading artifacts.

---

### 11. Upload Surefire artifacts
```yaml
- name: Upload Surefire reports
  if: always()
  uses: actions/upload-artifact@v4
  with:
    name: surefire-reports
    path: target/surefire-reports
```
**What it does:**
- Uploads test reports so you can download them from GitHub Actions UI.

---

### 12. Checkout gh-pages branch
```yaml
- name: Checkout gh-pages
  if: always()
  uses: actions/checkout@v4
  with:
    ref: gh-pages
    path: gh-pages
```
**What it does:**
- Pulls the `gh-pages` branch into a separate directory.
- Used for publishing the Allure report history.

---

### 13. Copy Allure history
```yaml
- name: Copy Allure history
  if: always()
  run: |
    mkdir -p target/allure-results/history
    if [ -d gh-pages/history ]; then
      cp -r gh-pages/history/* target/allure-results/history/
    fi
```
**What it does:**
- Preserves Allure trend history across builds.
- Without this, trend charts reset each run.

---

### 14. Verify Allure results
```yaml
- name: Verify Allure results
  if: always()
  run: |
    if ls target/allure-results/*-result.json 1> /dev/null 2>&1 || ls target/allure-results/*-container.json 1> /dev/null 2>&1; then
      echo "Allure results present."
    else
      echo "No Allure results found."
      exit 1
    fi
```
**What it does:**
- Fails early if no Allure results were generated.

---

### 15. Build Allure report
```yaml
- name: Build Allure report
  if: always()
  run: mvn allure:report
```
**What it does:**
- Generates HTML report into `target/allure-report`.

---

### 16. Prepare Allure report for GitHub Pages
```yaml
- name: Prepare Allure report for GitHub Pages
  if: always()
  run: |
    rm -rf gh-pages/*
    if [ -d target/allure-report ]; then
      cp -r target/allure-report/* gh-pages/
    elif [ -d target/site/allure-maven-plugin ]; then
      cp -r target/site/allure-maven-plugin/* gh-pages/
    fi
```
**What it does:**
- Copies the generated report into `gh-pages/`.
- Supports both `target/allure-report` and the Maven plugin fallback path.

---

### 17. Patch Allure report for GitHub Pages
```yaml
- name: Patch Allure report for GitHub Pages
  if: always()
  run: |
    ...
```
**What it does:**
- Updates HTML/JS paths so Allure works from a GitHub Pages subpath.
- Fixes broken asset links when hosting from `/repo-name/`.

---

### 18. Publish to gh-pages
```yaml
- name: Publish to gh-pages
  if: always()
  uses: peaceiris/actions-gh-pages@v4
  with:
    github_token: ${{ secrets.GITHUB_TOKEN }}
    publish_dir: gh-pages
```
**What it does:**
- Pushes the updated report to the `gh-pages` branch.
- Report becomes available at:
  `https://<owner>.github.io/<repo>/`

---

## Summary of Key Concepts

- **Runner**: The machine that executes the workflow (here: `ubuntu-latest`).
- **Job**: A set of steps executed on a single runner.
- **Steps**: Individual tasks in a job (checkout, install, test, publish).
- **Artifacts**: Files uploaded from the runner (Surefire reports).
- **Secrets**: Encrypted values stored in repo settings (`APP_BASE_URL`, SMTP, Slack).
- **Variables**: Non-secret flags like `SEND_EMAIL`.
- **Allure report**: HTML test report generated from results.

---

## Where to Look

- Workflow definition: `.github/workflows/test-automation.yml`
- Allure report output: `target/allure-report/`
- Allure results (raw): `target/allure-results/`
- Surefire XML: `target/surefire-reports/`

