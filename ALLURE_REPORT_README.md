# XYZ Bank Test Automation - Allure Report Setup

## Setup Complete ✅

Your Allure report is automatically configured to:
- **Update on every run** – `target/allure-results` and `target/allure-report` are cleared at the start of each run (no need for `mvn clean` for a fresh report)
- **Generate fresh reports** in `target/allure-report/`
- **Bug report on failure** – each failed test gets: (1) **Error overview** (exception type, message, stack trace) and (2) **Bug report – failure screenshot** image
- **Categories (error overview)** – failures are grouped by type: Assertion/Validation, Element/UI not found, Timeout, Other; each category notes "(screenshot in test)"
- **User Stories & AC in report** – environment tab shows User Story 1 & 2 and their acceptance criteria
- **Report title** – Overview shows "XYZ Bank – Test Report" (via executor.json)
- **Work seamlessly with IntelliJ** – no scripts needed

## How to Use

### Run Tests in IntelliJ

1. Right-click test class or method → **Run**
2. Or press `Ctrl+Shift+F10` (test class) or `Ctrl+F5` (test method)

That's it! Maven automatically:
- Deletes old `target/` folder (old reports gone)
- Runs tests and generates fresh JSON results
- Generates fresh HTML report

### View Report

After tests complete:
1. Navigate in Project Explorer: `target/allure-report/index.html`
2. Right-click → **Open in Browser** (or press `Ctrl+Alt+B`)
3. View your fresh report

## How It Works

```
Run Tests in IntelliJ
        ↓
Maven clean phase
└─ Deletes old target/ folder (old reports deleted)
        ↓
Maven test phase
├─ Runs tests → generates fresh JSON in target/allure-results/
└─ Allure plugin → generates fresh HTML in target/allure-report/
        ↓
Result: Fresh report ready in target/allure-report/index.html
```

## Configuration

### pom.xml ✅
- **maven-clean-plugin**: Deletes `target/` on `mvn clean`
- **maven-antrun-plugin**: Cleans allure dirs at start of every run; copies `categories.json`, `environment.properties`, and `executor.json` into results (report title, user stories, defect categories)
- **allure-maven**: Generates the report after tests

### allure.properties ✅
```properties
allure.results.directory=target/allure-results
allure.report.directory=target/allure-report
```

## Directory Structure

```
target/
├── allure-results/          ← Fresh test JSON (auto-generated)
├── allure-report/           ← Fresh HTML Report (auto-generated)
│   └── index.html          ← OPEN THIS IN BROWSER
└── ...
```

## Benefits

✅ No manual cleanup needed  
✅ No scripts to run  
✅ No accumulation of old reports  
✅ Always fresh reports per run  
✅ IntelliJ integrated  
✅ Standard Maven setup  

## Shortcuts

| Action | Shortcut |
|--------|----------|
| Run test class | `Ctrl+Shift+F10` |
| Run test method | `Ctrl+F5` |
| Open in browser | `Ctrl+Alt+B` |

## Troubleshooting

**Old report still showing?** → Hard refresh browser (`Ctrl+Shift+R`)

**Report not generating?** → Check `target/allure-results/` has JSON files

**Tests not running?** → Ensure Maven is configured in IntelliJ settings

## Summary

Your setup is ready. Just run tests in IntelliJ, and fresh reports are automatically generated in `target/allure-report/`. The old report is deleted each time. No additional setup needed!

