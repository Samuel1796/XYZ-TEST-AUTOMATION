# Repository secrets for CI - Selenium UI Tests

Add these secrets in **GitHub** → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**.

---

## Repo secrets checklist (what to add)

| Secret name        | What to put | Your scenario |
|--------------------|-------------|----------------|
| **APP_BASE_URL**   | Base URL of the app under test (no trailing slash). | e.g. `https://www.globalsqa.com/angularJs-protractor/BankingProject` |
| **SLACK_WEBHOOK_URL** | Slack Incoming Webhook URL. | From Slack → Incoming Webhooks (optional if you don’t use Slack). |
| **SMTP_SERVER**    | Gmail SMTP host. | `smtp.gmail.com` |
| **SMTP_PORT**      | Gmail SMTP port. | `587` |
| **SMTP_USERNAME**  | Sender (sender email = “from” address). | `yoshninjas.1@gmail.com` |
| **SMTP_PASSWORD**  | Gmail **App Password** (not your normal password). | 16‑char app password for yoshninjas.1@gmail.com |
| **EMAIL_TO**       | Recipient(s) for the test report email. | `sppppppp@gmail.com` |

**To enable email:** In **Variables** tab add **SEND_EMAIL** = `true` (must be a Variable; secrets cannot be used in workflow conditions). In **Secrets** add **EMAIL_TO** = `sboakye1796@gmail.com`. Emails then send from **yoshninjas.1@gmail.com** to **sboakye1796@gmail.com**.

---

## Required secrets

| Secret name | Description | Example / How to get |
|-------------|-------------|------------------------|
| **APP_BASE_URL** | Base URL of the app under test (no trailing slash). Passed as `-Dbase.url` to Maven in CI. | `https://www.globalsqa.com/angularJs-protractor/BankingProject` |
| **SLACK_WEBHOOK_URL** | Slack Incoming Webhook URL for test result notifications. | See [Slack](#slack-webhook) below. |
| **SMTP_SERVER** | SMTP server host (Gmail: `smtp.gmail.com`). | `smtp.gmail.com` |
| **SMTP_PORT** | SMTP port (Gmail TLS: `587`). | `587` |
| **SMTP_USERNAME** | SMTP login / sender email (Gmail: your full Gmail address). | `your.email@gmail.com` |
| **SMTP_PASSWORD** | SMTP password. **Gmail:** use an [App Password](#gmail-app-password), not your normal password. | 16-character app password |
| **EMAIL_TO** | Comma-separated recipient email(s) for the test report. **Required only if you enable email** (see below). | `team@example.com` or `you@gmail.com,other@gmail.com` |

---

## Enabling email reports

Email is **optional**. The workflow only sends email when you enable it and set a recipient:

1. **Enable sending:** In **Variables** tab (Actions), add **Name** `SEND_EMAIL`, **Value** `true`. (Must be a Variable; GitHub does not allow secrets in step conditions.)
2. **Recipient:** In **Secrets** tab, add **EMAIL_TO** with the recipient(s), e.g. `sboakye1796@gmail.com` (comma-separated for multiple).

If the Variable **SEND_EMAIL** is not `true`, the email step is skipped. If **EMAIL_TO** is not set, the send-mail step will fail with “At least one of 'to', 'cc' or 'bcc' must be specified”.

**If you see *"At least one of 'to', 'cc' or 'bcc' must be specified"*:** Add a **Secret** named exactly **EMAIL_TO** (case-sensitive, no extra spaces) with value `sboakye1796@gmail.com` (or your recipient). The workflow only sends when this secret is set.

---

## Serving the Allure report (gh-pages)

CI publishes the Allure report to the **gh-pages** branch. To serve it: **Settings** → **Pages** → **Source**: Deploy from a branch → **gh-pages** / (root). The report will be at `https://<owner>.github.io/<repo>/`. See the main [README](../README.md#allure-report-on-github-pages-ci) for details.

---

## Optional / provided by GitHub

| Secret name | Notes |
|-------------|--------|
| **GITHUB_TOKEN** | Do **not** add manually. It is provided automatically for `peaceiris/actions-gh-pages` (publishing Allure to GitHub Pages). |

---

## Slack webhook

1. In Slack: **Apps** → **Incoming Webhooks** → **Add to Slack** (or open the app from your workspace).
2. Choose the channel for test notifications.
3. Copy the **Webhook URL** (e.g. `https://hooks.slack.com/services/T00…/B00…/xxx…`).
4. Add it as repository secret **SLACK_WEBHOOK_URL**.

---

## Gmail (SMTP) setup

To send email from Gmail in CI:

1. **Use an App Password (recommended)**  
   - Go to [Google Account](https://myaccount.google.com/) → **Security** → **2-Step Verification** (must be on).  
   - **App passwords** → **Select app** (e.g. “Mail”) → **Generate**.  
   - Copy the 16-character password (no spaces).  
   - In GitHub: add **SMTP_USERNAME** = your Gmail address, **SMTP_PASSWORD** = this app password.

2. **Secrets to set**
   - **SMTP_SERVER**: `smtp.gmail.com`
   - **SMTP_PORT**: `587`
   - **SMTP_USERNAME**: `your.name@gmail.com`
   - **SMTP_PASSWORD**: the 16-character app password
   - **EMAIL_TO**: one or more addresses (comma-separated), e.g. `your.name@gmail.com`

3. **Security**  
   - Do not use your normal Gmail password.  
   - App passwords are per-app and can be revoked without changing your main password.

---

## Checklist

- [ ] **APP_BASE_URL** – URL of the XYZ Bank (or your) app
- [ ] **SLACK_WEBHOOK_URL** – from Slack Incoming Webhooks
- [ ] **SMTP_SERVER** – `smtp.gmail.com` for Gmail
- [ ] **SMTP_PORT** – `587` for Gmail
- [ ] **SMTP_USERNAME** – Gmail address
- [ ] **SMTP_PASSWORD** – Gmail App Password (not account password)
- [ ] **EMAIL_TO** – recipient(s) for email report

After adding these, the workflow **CI - Selenium UI Tests** will run tests on the runner, publish Allure to GitHub Pages, and send Slack and email reports.
