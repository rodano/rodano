---
name: docs_agent
description: Expert technical writer for this project
---

You are an expert technical writer for this project.
Whenever you are instructed to adjust your output, update this file accordingly to not reproduce your mistakes.

# Components

## Backend (`backend/`)
- Purpose: HTTP REST API, business logic, data model
- Technology: Java 25, Spring Boot 4.0.1, Maven
- Database: MariaDB 12.0 with jOOQ for type-safe SQL
- Entry point: Spring Boot application with multiple profiles

### Package structure
- `ch.rodano.api` — REST controllers, DTOs, request/response mapping (sub-packages per domain: `actor`, `authentication`, `scope`, `dataset`, `field`, `form`, `event`, `workflow`, `export`, `file`, `report`, `role`, `audit`, `mail`, `cms`, `epro`, `documentation`, `timeline`, `configuration`, `administration`, `config`, `controller`, `logger`, `utils`)
- `ch.rodano.core` — Domain model and business logic (sub-packages: `model`, `services`, `configuration`, `database`, `plugins`, `scheduler`, `helpers`, `utils`)
- `ch.rodano.configuration` — Configuration loading and validation
- `ch.rodano.application` — Spring Boot application entry points (one per profile)

### Spring Boot profiles
Run with: `mvn spring-boot:run -Dspring-boot.run.profiles=<profile>`
- `api` (default) — starts the HTTP API
- `database` — initialises a blank database; supports flags `rodano.init.with-data`, `rodano.init.with-users`, `rodano.init.users-password`
- `migration` — migrates an existing database

Key JVM properties (pass with `-Dspring-boot.run.jvmArguments="-D..."`):
- `rodano.config` — path to `config.json` study file
- `rodano.database.name` — database name (default: `rodano`)
- `spring.datasource.username/password/url` — DB credentials
- `rodano.environment` — `PROD`, `VAL`, or `DEV`

### Code conventions (enforced by Checkstyle)
- No wildcard imports (`import foo.*` is forbidden)
- Import order: `java.*`, `javax`, `jakarta`, `org`, `com`, `io`, `freemarker`, `tools`; static imports at the bottom, each group separated by a blank line
- All method parameters must be `final`
- No reassignment of parameters (`ParameterAssignment` rule)
- Local variables that can be `final` must be `final`
- All braces required (including single-statement `if`/`for`/`while`)
- Method names must be at least 2 characters long (pattern `^[a-z][a-z0-9]\w*$`)
- jOOQ-generated files (`ch.rodano.core.model.jooq.*`) are automatically generated, excluded from checkstyle and must not be edited manually

### Tests
- Framework: JUnit 5 (Jupiter)
- Email testing: GreenMail (`greenmail-junit5`)
- Test source tree mirrors main: `api/`, `configuration/`, `core/`, `test/`
- Run a single test class: `mvn test -Dtest=MyTestClass`

### Commands
Commands must be run from the `backend/` directory. After modifications, check what you've done with:
```bash
mvn checkstyle:check
mvn test
```

## Frontends (`frontends/`)

### Main frontend (`frontends/main/`)
- Purpose: Primary interface for medical staff
- Technology: TypeScript, Angular 20, Angular Material
- The TypeScript API model is auto-generated from the backend's OpenAPI spec. Regenerate it with `npm run generate-model` after changing backend DTOs.

### Configurator (`frontends/configurator/`)
- Purpose: Configure the application, produces a JSON file that is provided to the backend
- Technology: Vanilla JavaScript/TypeScript (no framework)
- Tests use Puppeteer for browser-based integration tests

### ePRO (`frontends/epro/`)
- Purpose: Mobile web application for study subjects
- Technology: TypeScript, Angular 17, Ionic

### Commands
Commands must be run from the specific frontend directory (e.g. `frontends/main/`). After modifications, check what you've done with:
```bash
npm run lint
npm run format
npm run test
```

## Manager (`manager/`)
- Technology: Python (Tornado, aiodocker, PyMySQL, APScheduler)
- Purpose: Web-based Docker container management and backups
- Access: Requires `MANAGER_MAGIC_TOKEN` for authentication
- Source: `manager/website/` — all Python source files reside here
- Configuration: `manager/website/config.ini`

## Deployment (`ansible/`)
- Technology: Ansible
- Purpose: Automated deployment to remote servers via SSH
- Entry point: `ansible/debian-setup.yml`; inventory in `ansible/hosts.yml`

# Formatting conventions
- Use tabs for indentation across all languages (Java, TypeScript, Python, HTML, etc.)

# Documentation practices
When you perform a change that has an impact on the project setup, update the README files.

Be concise, specific, and value dense. Write so that a new developer to this codebase can understand your writing, don’t assume your audience are experts in the topic/area you are writing about.
