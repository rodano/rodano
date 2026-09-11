# Backend

The backend of Rodano. In addition to the API, it is able to initialize and migrate a database.

## System Requirements

* JDK 25
* Maven 3.9.0
* MariaDB 12.0

## Profiles

The application uses Spring Boot. It contains 2 different profiles that perform different operations. Each profile can be launched using the following command:

```
mvn spring-boot:run -Dspring-boot.run.profiles=xxx
```

where xxx is the name of the profile to trigger and can be set to:
* `api`: launch the API (default)
* `database`: initialize a database

Only one of these profiles can be used at the same time.

Most of the time, you will need to specify a configuration file and the database that will be used by the application. Read the following sections to know in detail how to configure the application.

### API

This is the main profile and starts the API. Being the default, it can be started with the following command:

```
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Drodano.config=/path/to/config.json -Drodano.database.name=database_name"
```

On startup, the API automatically migrates the database to the latest version (see [Database migrations](#database-migrations)). If a migration fails, the startup is aborted to avoid running on a partially migrated database.

The API also exposes a prototype MCP Streamable HTTP endpoint at `/mcp`. MCP requests use HTTP Basic authentication with the name and key of an active Rodano robot. The robot's roles determine which study models and data the MCP tools can read.

### Database

This profile initializes a blank database with everything required to start the application:
* creation of all the tables
* creation of a root scope
* creation of default users

You can add the following parameters:
* `rodano.init.with-data` (default `false`): add predefined test data in the database (only works with the `test` study)
* `rodano.init.with-users` (default `false`): generate one user for each profile of the configuration
* `rodano.init.users-password` (default `Password1!`): used with `rodano.init.with-users`, define the password for the generated users

For example:

```
mvn spring-boot:run -Dspring-boot.run.profiles=database -Dspring-boot.run.jvmArguments="-Drodano.config=/path/to/config.json -Drodano.database.name=database_name -Drodano.init.with-data=true -Drodano.init.with-users=true -Drodano.init.users-password=MySuperPassword"
```

## Database migrations

Migrations bring an existing database up to date with the current code. They are run automatically at API startup: the application reads the highest version recorded in the `internal_patch` table and applies every migration whose number is greater, in ascending order, until the database reaches the latest version. A failing migration aborts the startup.

There are two kinds of migrations, both identified by a unique number and merged into a single ordered sequence:

* **SQL migrations**: files named `src/main/resources/database_scripts/migrations/db_update_<number>.sql`. Each script is responsible for inserting its own row into the `internal_patch` table (e.g. `insert into internal_patch (script, date, context, name) values (<number>, now(3), '<description>', 'db_update_<number>.sql');`).
* **Java migrations**: classes that extend `AbstractDatabaseMigration` (or the `DBUpdateConfig` helper to add events/datasets/fields) and are annotated with `@MigrationBean`. Each declares its number through `migrationTaskNumber()`; the base class records the version in `internal_patch` automatically.

To add a migration, create the appropriate file/class with a number higher than the latest existing one. It will be picked up and applied on the next startup.

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

The logging level can be set using the parameter `logging.level.<package>=xxx`. The log level can be ERROR, WARN, INFO, DEBUG or TRACE. For example, `logging.level.ch.rodano.core=DEBUG`.

### Overriding configuration properties

It is possible to override the value of these properties using one of the following approaches:
* directly in the command line using `-Dproperty=value`
* by adding another profile which has a property file associated with it
* using environment variables, that are read by Spring on top of Java properties

These approaches can be combined, but note that properties passed through the command line will always take precedence.

#### Using properties file

You can create a profile named `local`, associated with a file named `application-local.properties`. Then, you can launch the application with the following command:

```
mvn spring-boot:run -Dspring-boot.run.profiles=xxx,local -Dspring-boot.run.jvmArguments="-Drodano.database.name=database_name"
```

The order of the active profiles is important. The profile `local` must appear after the profile `xxx` so that the properties from the file `application-local.properties` override the properties from the profile `xxx` (stored in `application-xxx.properties`).


If you have a custom property file for a study, named `application-study.properties`, and want to launch the API, you can use the following command:

```
mvn spring-boot:run -Dspring-boot.run.profiles=api,study -Dspring-boot.run.jvmArguments="-Drodano.database.name=database_name"
```

#### Using command line parameters

When launched via the spring-boot-maven-plugin (i.e. `mvn spring-boot:run`), the application is started in a forked JVM instance. Consequently, the arguments passed to the JVM will not be available in Rodano (parameters will only be passed to the Spring parent process that starts Rodano in a second step, and won't be inherited by Rodano).

If you use the spring-boot-maven-plugin, and you want to pass JVM properties to the Rodano process, you will need to wrap them within the `spring-boot.run.jvmArguments` argument, like this:

```
mvn spring-boot:run -Dspring-boot.run.jvmArguments="[all your properties separated by a space]"
```

## Plugins

Spring has a mechanism to load additional compiled code, also known as plugins. These plugins allow adding content in the application or to customize its behavior. This code must be compiled to be used by Spring.

When using the spring-boot-maven-plugin (i.e. `mvn spring-boot:run`), the parameter `spring-boot.run.additional-classpath-elements` allows loading additional compiled code:

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

In order to generate the database classes, configure a Java Application within the `rodano-backend` project (i.e. same classpath) using the main class `org.jooq.codegen.GenerationTool` and add `src/main/resources/jooq/jooq-database.xml` as an argument.
It is important that some jOOQ classes and auxiliary classes (`jooq-x.x.x.jar`,`jooq-meta-x.x.x.jar`,`jooq-codegen-x.x.x.jar`,`reactive-streams-x.x.x.jar`,`[JDBC-driver].jar`) be present on the classpath for the code generation to work; as they are already imported via Maven, nothing needs to be done.

Alternatively, you can run the generation tool directly from the command line, specifying the dependencies explicitly:

```
java -cp jooq-x.x.x.jar;jooq-meta-x.x.x.jar;jooq-codegen-x.x.x.jar;reactive-streams-x.x.x.jar;[JDBC-driver].jar org.jooq.codegen.GenerationTool jooq-database.xml
```

## Docker

This folder contains a Dockerfile to build a Docker image containing the backend. The same Dockerfile also provides an additional image used to compile study plugins.

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

To use the Docker image, remember that a database is required. You can either use the Docker Compose configuration file that is provided at the root of the repository or install a local database.

If you have a local database installed, you can run:

```
docker run -p 8080:8080 -e rodano.database.host=host.docker.internal ghcr.io/rodano/backend:dev
```

It is possible to load study plugins by mounting them (compiled) in the special folder `/app/plugins`, like this:

```
docker run -p 8080:8080 -v /path/to/study/plugins/target/classes:/app/plugins -e rodano.database.host=host.docker.internal ghcr.io/rodano/backend:dev
```
