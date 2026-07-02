# Backend Quality Assurance & Test Suite Guide

This backend system uses automated unit, service, controller, integration, and concurrency test suites to ensure stability, performance, and locking integrity.

---

## 1. Testing Stack

* **JUnit 5**: Standard testing framework runner.
* **Mockito**: Mocking and validation framework for service-level dependencies.
* **MockMvc**: Fast API controller schema testing bypassing full HTTP client calls.
* **PostgreSQL Testcontainers**: Spins up a real, ephemeral `postgres:16-alpine` database instance locally or inside the build container.
* **Awaitility**: Monitors asynchronous cron schedulers and threads.
* **JaCoCo**: Generates code coverage reports.

---

## 2. Running Automated Tests

Launch the complete testing pipeline using Maven:

```bash
mvn clean verify
```

To run unit tests exclusively:

```bash
mvn test
```

To run integration/concurrency tests requiring Testcontainers:

```bash
mvn failsafe:integration-test
```

---

## 3. Code Coverage Reports

Once tests finish executing successfully, generate the HTML coverage dashboard:

```bash
mvn jacoco:report
```

Open the report in your browser to inspect coverage metrics:
- Local Path: `target/site/jacoco/index.html`

The project enforces an **85% overall code coverage threshold** inside the build pipeline. Any build below this threshold will be blocked automatically.
