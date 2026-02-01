# healthcheck

External checker for a Mattermost VPS (HTTP + TCP + SSH auth).

## Requirements
- Java 21+
- Windows / Linux / macOS
- SSH key-based auth configured

## Setup
1) Copy example config:
- `src/main/resources/config.example.properties` → `src/main/resources/config.properties`

2) Fill `config.properties` with your server values.

## Run (Gradle)
```powershell
.\gradlew.bat run
.\gradlew.bat run --args="--json"
.\gradlew.bat run --args="--verbose"
