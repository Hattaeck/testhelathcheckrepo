# 🛡️ healthcheck

**External healthcheck tool for a Mattermost VPS.**

This tool verifies server availability from an **external** perspective (not from localhost), ensuring your services are truly reachable by users.

---

## 🎯 Purpose
The project is a lightweight external signal that answers the simple question: *“Is the server reachable from the outside?”* It is designed as a readable pet-project for learning Java 21 and Gradle.

> **Note:** This is not a replacement for complex monitoring like Prometheus or Zabbix, but a clean, scriptable alternative for personal use.

## 🔍 What it checks
* **HTTP**: Mattermost API availability.
* **TCP**: Port reachability.
* **SSH**: Authentication using a private key.

## ✨ Main Features
* **Real-world perspective**: Checks from outside the server network.
* **Dual Output**: Human-readable text for manual checks or **JSON** for automation.
* **Proper Exit Codes**:
    * `0` — All checks passed.
    * `1` — One or more checks failed.
* **Secure**: Supports **SSH key-based authentication only** (no passwords).
* **Cross-platform**: Works on Windows, Linux, and macOS.

---



## 📋 Requirements
* **Java 21** or higher.
* Network access to the target server.
* Configured SSH key-based access.

---
## 📁 Project Structure
- Main.java: Entry point, handles --json and --verbose arguments.
- Checks.java: Core logic for HTTP, TCP, and SSH checks.
- CheckResult.java: Data model for storing check outcomes.
- Level.java: Status levels (OK, WARN, FAIL).
- config.properties: Private server credentials (ignored by Git).
- config.example.properties: Safe configuration template.
- docs/screenshots: Images for README documentation.
- build.gradle: Project build and dependency configuration.
---


## ⚙️ Configuration
The tool requires a configuration file for server parameters.

1.  Copy the example config:
    `src/main/resources/config.example.properties` → `src/main/resources/config.properties`
2.  Fill `config.properties` with your data:
    * Server host
    * Mattermost HTTP port
    * SSH port & user
    * Path to your private SSH key

⚠️ **Important:** `config.properties` contains sensitive data and is ignored by Git. Only the example file is stored in the repository.

---


## 🚀 Running the Project
The project is executed via the **Gradle Wrapper**.

### Supported Modes:
* **Standard**: Human-readable output for manual monitoring.
* **JSON**: Structured output for integration with scripts and automation tools.
* **Verbose**: Detailed logs with additional execution context.

### 🚩 Exit Codes:
The tool returns standard exit codes, making it perfect for automation:
* `0` — **Success**: All checks passed successfully.
* `1` — **Failure**: One or more checks failed.

> This makes the tool suitable for **cron jobs**, **shell/PowerShell scripts**, and **CI pipelines**.

---

## 🛠️ Architecture Decisions

### Why TCP check comes before SSH authentication?
At the first stage, the tool verifies **TCP reachability** rather than performing a full SSH login.

**Reasons:**
1.  **Fast Detection**: Immediate identification of network-level failures.
2.  **Error Separation**: Clear distinction between network issues (port closed) and authentication issues (key rejected).
3.  **Efficiency**: Avoids unnecessary and heavy SSH handshakes if the port is unreachable.
4.  **Extensibility**: Easier to implement timeouts, retries, and metrics at the socket level.

*Note: SSH authentication is implemented as a separate and explicit secondary step.*

---

## 🔒 Security Considerations
* **No Passwords**: The tool exclusively uses SSH key-based authentication.
* **Zero Secrets**: No sensitive data or credentials are stored in the source code or repository.
* **Public-Safe**: The project is completely safe to host on public platforms like GitHub.

---

## 🎯 Intended Use
* Monitoring a personal VPS.
* Checking Mattermost service availability.
* **Learning**: A practical example of using Java 21 and Gradle.
* **Demonstration**: A clean and simple implementation of an external healthcheck tool.

---

## 💡 Summary
This project is **not** a replacement for enterprise monitoring systems like *Prometheus* or *Zabbix*.

It is a **lightweight external signal** designed to answer one crucial question:
> *“Is my server actually reachable from the outside world right now?”*




## Screenshots

![Run human](docs/screenshots/run-human.png)
![Run json](docs/screenshots/run-json.png)


