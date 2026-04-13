# Backend

The backend of Rodano. In addition to the API, it is able to initialize and migrate a database.

## System Requirements

* JDK 23
* Maven 3.8.6
* MariaDB 12.0

## Profiles

The application uses Spring Boot. It contains 3 different profiles that perform different operations. Each profile can be launched using the following command:

```
mvn spring-boot:run -Dspring-boot.run.profiles=xxx
```

where xxx is the name of the profile to trigger and can be set to:
* `api`: launch the API (default)
* `database`: initialize a database
* `migration`: migrate a database

Only one of these profiles can be used at the same time.

Most of the time, you will need to specify a configuration file and the database that will be used by the application. Read the following sections to know in details how to configure the application.

### API

This profile is the main profile and starts the API. Being the default, it can be started with the following command:

```
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Drodano.config=/path/to/config.json -Drodano.database.name=database_name"
```

### Database

This profile will initialize a blank database with the data required to start the application:
* creation of all the tables
* creation of a root scope
* creation of default users

You can add following parameters:
* `rodano.init.with-data` (default `false`): add predefined test data in the database (only works with the `test` study)
* `rodano.init.with-users` (default `false`): generate one user for each profile of the configuration
* `rodano.init.users-password` (default `Password1!`): used with `rodano.init.with-users`, define the password for the generated users

For example:

```
mvn spring-boot:run -Dspring-boot.run.profiles=database -Dspring-boot.run.jvmArguments="-Drodano.config=/path/to/config.json -Drodano.database.name=database_name -Drodano.init.with-data=true -Drodano.init.with-users=true -Drodano.init.users-password=MySuperPassword"
```

### Migration

This profile will migrate a database. Here is an example of how to launch a database migration:

```
mvn spring-boot:run -Dspring-boot.run.profiles=migration -Dspring-boot.run.jvmArguments="-Drodano.config=/path/to/config.json -Drodano.database.name=database_name"
```

## Batch Import

The application uses JBeret (Jakarta Batch implementation) to import configuration from JSON files into the database. The batch import reads the study configuration and populates all model tables, including workflows, forms, field models, charts, reports, profiles, scope models, event models, validators, rules, menus, widgets, and more.

### How It Works

The batch import job (`import-config`) runs 35 sequential steps, each populating a specific set of model tables from the JSON configuration file. Steps cover:

1. Project
2. Rule definition properties
3. Rule definition actions
4. Static features
5. Features
6. Resource categories
7. Dataset models
8. Workflows
9. Profiles
10. Form models
11. Scope models
12. Scope model parents
13. Payment plans
14. Reports
15. Charts
16. Timeline graphs
17. Workflow widgets
18. Workflow summaries
19. Menus
20. Profile rights
21. Privacy policies
22. Validators
23. Event action rules
24. Crons
25. Selections
26. Field model backfill (missing links)
27. Workflow backfill (missing links)
28. Project backfill (missing profile ID)
29. Workflow rules
30. Dataset model rules
31. Form model rules
32. Scope model rules
33. Validator rules
34. Cron rules
35. Status parameter backfill

Steps 26–35 are backfill and rule-linking passes that run after the primary data is inserted. They resolve cross-entity references and attach rules to their respective owners. Each step runs in its own transaction, processing records in chunks of 100–200 items. The import uses deterministic UUID generation to ensure consistent identifiers across repeated imports.

### Prerequisites

The batch import must be run **after** the database schema has been initialized (see [Full Initialization Workflow](#full-initialization-workflow)). The application must be running in `api` profile when triggering the import via HTTP.

### Configuration

Default batch import parameters can be set in `application.yml`:

```yaml
batch:
  import:
    job: import-config
    projectId: MY_PROJECT
    config: file:/path/to/config.json
    db:
      url: jdbc:mariadb://localhost:3306/rodano
      user: user
      password: password
```

Any of these defaults can be overridden per-request in the request body.

### API Endpoints

#### Start a batch import

Starts an import job. All fields are optional — omitted fields fall back to the defaults configured in `application.yml`. `projectId` can be either a UUID or a project code string.

```http
POST http://localhost:8080/api/batch/import
Content-Type: application/json

{
  "projectId": "MY_PROJECT"
}
```

With full overrides:

```http
POST http://localhost:8080/api/batch/import
Content-Type: application/json

{
  "projectId": "MY_PROJECT",
  "config": "classpath:config/config.json",
  "dbUrl": "jdbc:mariadb://localhost:3306/rodano",
  "dbUser": "root",
  "dbPassword": "root"
}
```

The response returns HTTP 202 with the execution ID and the resolved parameters used for the job:

```json
{
  "executionId": 1,
  "job": "import-config",
  "parameters": { ... }
}
```

#### Check job status

```http
GET http://localhost:8080/api/batch/execution/1
```

Returns the overall job status (`STARTING`, `STARTED`, `COMPLETED`, `FAILED`) along with per-step details including start/end times and exit status.

#### Stop a running job

```http
POST http://localhost:8080/api/batch/execution/1/stop
```

Returns HTTP 202. Stopping is asynchronous — the job may finish processing its current chunk before halting. If stopped mid-run, the model tables may be in a partially imported state; truncate the affected tables and re-run the import before proceeding.

### Initialize Project Runtime Data

After a successful batch import, the project configuration exists in the model tables but has no runtime data yet. The initialization step creates the root scope, sets up the model catalog (UUID links for scope models, dataset models, event models, form models, and field models), creates the initial admin user, and optionally seeds demo users and data.

If the provided admin email already exists in the database, the user will not be recreated — instead, an ADMIN role for this project will be added to the existing user.

```http
POST http://localhost:8080/administration/projects/initialize
Content-Type: application/json

{
  "projectCode": "MY_PROJECT",
  "adminName": "Admin User",
  "adminEmail": "admin@example.ch",
  "adminPassword": "Password1!",
  "withDemoUsers": false,
  "withDemoData": false
}
```

| Field | Required | Default | Description |
|---|---|---|---|
| `projectCode` | Yes | — | Code of the project to initialize. Must match a project already imported via the batch import. |
| `adminName` | No | `Admin User` | Display name for the initial admin user |
| `adminEmail` | Yes | — | Email of the initial admin user |
| `adminPassword` | Yes | — | Password for the initial admin user |
| `withDemoUsers` | No | `false` | Generate one user per profile defined in the configuration |
| `withDemoData` | No | `false` | Seed predefined demo data — only works with the `test` study. Demo data is backdated 3 years. |

Returns HTTP 200 with a confirmation message including the project code and resolved UUID, or HTTP 400 if the project code is not found (i.e. the batch import has not been run yet).

### Full Initialization Workflow

To set up a project from scratch:

1. **Initialize the database schema:**
```bash
mvn spring-boot:run \
  -Dspring-boot.run.profiles=database \
  -Dspring-boot.run.arguments="--rodano.database.name=database_name"
```

2. **Start the API:**
```bash
mvn spring-boot:run
```

3. **Run the batch import:**
```http
POST http://localhost:8080/api/batch/import
Content-Type: application/json

{
  "projectId": "MY_PROJECT"
}
```

4. **Monitor until complete:**
```http
GET http://localhost:8080/api/batch/execution/1
```

5. **Initialize runtime data:**
```http
POST http://localhost:8080/administration/projects/initialize
Content-Type: application/json

{
  "projectCode": "MY_PROJECT",
  "adminName": "Admin User",
  "adminEmail": "admin@example.ch",
  "adminPassword": "Password1!"
}
```

### Notes

- The batch import must be run before initializing runtime data — the initialize endpoint will return HTTP 400 if the project code is not found in the database.
- If you modify the JSON configuration, truncate the model tables and re-run the batch import to apply changes.
- Each batch import execution gets a unique execution ID that can be used to monitor progress.
- `projectId` in the import request accepts either a UUID or a project code. If a code is provided but cannot be resolved to an existing project, a random UUID is generated.
- Demo data is only supported with the `test` study configuration.

## Configuration properties

The application and its profiles rely on configuration properties. The default values for these properties are stored in files in the folder `src/main/resources`. On top of the global `application.properties` file, which contains configuration properties for Spring Boot and general Rodano settings, each profile has its own property file named `application-xxx.properties`.

Most of these properties should not be touched. However, some of them, used by all profiles, are of interest:
* `rodano.config`: the path to the configuration file (test configuration by default)
* `rodano.database.name`: the name of the database to use (rodano by default)
* `spring.datasource.username`: the username to log in to the database
* `spring.datasource.password`: the password to log in to the database
* `spring.datasource.url`: the JDBC URL used by the datasource
* `rodano.environment`: the study environment (choose among PROD, VAL, or DEV)
* `rodano.path.data`: the path to the directory that stores the files uploaded by the users

The logging level can be set using the parameter `logging.level.com.example.app=xxx`. The log level can be ERROR, WARN, INFO, DEBUG or TRACE. For example, `logging.level.ch.rodano.core=DEBUG`.

### Overriding configuration properties

It is possible to override the value of these properties with the different approaches:
* directly in the command line using `-Dproperty=value`
* by adding another profile which has a property file associated with it
* using environment variables, that are read by Spring on top of Java properties

Both techniques can be used at the same time, but note that the properties passed through the command line will always take precedence.

#### Using properties file

You can create a profile named `local`, associated with a file name `application-local.properties`. Then, you can launch the application with the following command:

```
mvn spring-boot:run -Dspring-boot.run.profiles=xxx,local -Dspring-boot.run.jvmArguments="-Drodano.database.name=database_name"
```

The order of the active profiles is important. The profile `local` must appear after the profile `xxx` so the properties from the file `application-local.properties` will override the properties from the profile `xxx` (stored in `application-xxx.properties`).


If you have a custom property file for a study, named `application-study.properties`, and want to launch the API, you can use the following command:

```
mvn spring-boot:run -Dspring-boot.run.profiles=api,study -Dspring-boot.run.jvmArguments="-Drodano.database.name=database_name"
```

#### Using command line parameters

When launched via the spring-boot-maven-plugin (i.e `mvn spring-boot:run`), the application is started in a forked JVM instance. Consequently, the arguments passed to the JVM will not be available in Rodano (parameters will only be passed to the Spring parent process that starts Rodano in a second step, and won't be inherited by Rodano).

If you use the spring-boot-maven-plugin, and you want to pass JVM properties to the Rodano process, you will need to wrap them within the `spring-boot.run.jvmArguments` argument, like this:

```
mvn spring-boot:run -Dspring-boot.run.jvmArguments="[all your properties separated by a space]"
```

## Plugins

Spring has a mechanism to load additional compiled code, also known as plugins. These plugins allow adding content in the application of to customize its behavior. This code must be compiled to be used by Spring.

When using the spring-boot-maven-plugin (i.e `mvn spring-boot:run`), the parameter `spring-boot.run.additional-classpath` allows loading additional compiled code:

```
mvn spring-boot:run -Dspring-boot.run.profiles=xxx -Dspring-boot.run.additional-classpath-elements=/path/to/study/plugins/target/classes
```

When using the executable JAR package provided by Spring, use:

```
java -cp target/rodano-backend-exec.jar -Dloader.path=/path/to/study/plugins/target/classes org.springframework.boot.loader.launch.PropertiesLauncher
```

## Development

### Properties of interest

Here are some useful debug properties to set in your `application-local.properties` file:

```
logging.level.ch.rodano.core=DEBUG
logging.level.ch.rodano.core.database=DEBUG

logging.level.org.springframework.web=DEBUG
logging.level.org.springframework.security=DEBUG

logging.level.org.jooq=DEBUG
logging.level.org.jooq.tools.LoggerListener=DEBUG

rodano.schedule.session-cleaner=false
```

### Run all tests

```
mvn test
```

To run only one test, use:

```
mvn test -Dtest=ch.rodano.core.services.bll.EventServiceTest#progression
```

### Deploy package on Rodano Maven repository (without tests)

```
mvn deploy -DskipTests
```

### Generating Database classes using jOOQ

In order to generate the database classes, configure a Java Application within the `rodano-backend` project (ie. same classpath) using the main class `org.jooq.codegen.GenerationTool` and add `src/main/resources/jooq/jooq-database.xml`as an argument.
It is important that some jOOQ classes and auxiliary classes (`jooq-x.x.x.jar`,`jooq-meta-x.x.x.jar`,`jooq-codegen-x.x.x.jar`,`reactive-streams-x.x.x.jar`,`[JDBC-driver].jar`) be present on the classpath for the code generation to work ; as they are already imported via Maven, nothing needs to be done.

Note that one can also run a command line (this time specifying the dependencies):

```
java -cp jooq-x.x.x.jar;jooq-meta-x.x.x.jar;jooq-codegen-x.x.x.jar;reactive-streams-x.x.x.jar;[JDBC-driver].jar org.jooq.codegen.GenerationTool jooq-database.xml
```

## Docker

This folder contains a Docker file to build a Docker image containing the backend. An additional image is provided in the same Docker file, to compile study plugins.

### Build the Docker images

To build the Docker image of the backend, run:

```
docker build -t ghcr.io/rodano/backend .
```

To build the Docker image used to compile study plugins, use:

```
docker build -t ghcr.io/rodano/plugins-compiler --target plugins-compiler .
```

### Use Docker image

To use the Docker image, remember that a database is required. You can either use the Docker compose configuration file that is provided at the root of hte repository or install a local database.

If you have a local database installed, you can run:

```
docker run -p 8080:8080 -e rodano.database.host=host.docker.internal ghcr.io/rodano/backend:dev
```

It is possible to load study plugins by mounting them (compiled) in the special folder `/app/plugins`, like this:

```
docker run -p 8080:8080 -v /path/to/study/plugins/target/classes:/app/plugins -e rodano.database.host=host.docker.internal ghcr.io/rodano/backend:dev
```
