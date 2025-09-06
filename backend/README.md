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

The application uses JBeret (Jakarta Batch implementation) to import configuration from JSON files into the database. The batch import reads the study configuration and populates model tables including:

* Workflows and workflow states
* Forms and field models
* Charts and reports
* Profiles and permissions
* Scope models and event models
* Validators and rules
* Menus and widgets
* And other configuration entities

### Batch Import Process

The batch import job (`import-config`) consists of 21 sequential steps that populate different model tables. Each step reads a specific section from the JSON configuration file and inserts the corresponding records into the database.

The main steps include:
1. Project and rule definitions
2. Features and resource categories
3. Dataset models
4. **Workflows** (including workflow states and actions)
5. Profiles
6. Form models and field models
7. Scope models and event models
8. Payment plans
9. Reports, charts, and graphs
10. Workflow widgets and summaries
11. Menus
12. Profile rights and permissions
13. Privacy policies
14. Validators
15. Event action rules
16. Crons and selections

### Running Batch Import via API

The batch import is executed via REST API endpoints. Start the application in `api` profile, then make HTTP requests to trigger the import.

#### Start a Batch Import Job

Using default configuration from `application.yml`:
```http
POST http://localhost:8080/api/batch/import
Content-Type: application/json

{}
```

With custom parameters:
```http
POST http://localhost:8080/api/batch/import
Content-Type: application/json

{
  "projectId": "MY_PROJECT",
  "config": "file:/path/to/config.json",
  "dbUrl": "jdbc:mariadb://localhost:3306/rodano",
  "dbUser": "user",
  "dbPass": "password"
}
```

#### Check Batch Job Status by Execution ID
```http
GET http://localhost:8080/api/batch/execution/1
```

Response includes:
- Job execution status (STARTING, STARTED, COMPLETED, FAILED)
- Start and end times
- Exit status
- Details for each step

#### Stop a Running Batch Job
```http
POST http://localhost:8080/api/batch/execution/1/stop
```

### Batch Import Configuration

Configure default batch import parameters in `application.yml`:
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

These defaults are used when making a batch import request without parameters.

### Full Database Initialization Workflow

To initialize a completely new database:

1. **Create the database schema:**
```bash
   mvn spring-boot:run \
  -Dspring-boot.run.profiles=database \
  -Dspring-boot.run.arguments="--rodano.config=/absolute/path/to/config.json \ 
  --rodano.database.name=database_name \ 
  --rodano.init.with-data=true --rodano.init.with-users=true --rodano.init.users-password=MySuperPassword"
```
This creates all tables and seeds the runtime tables (scope, user, etc.) and creates the uuid links to the models.

2. **Start the Application:**
```bash
   mvn spring-boot:run
```

3. **Run the batch import via HTTP:**
```http
   POST http://localhost:8080/api/batch/import
   Content-Type: application/json
   
   {}
```

4. **Wait for completion** - Monitor the job status until it shows `COMPLETED`


### Important Notes

* The batch import must be run **after** the database schema is created
* The batch import uses deterministic UUID generation to ensure consistent identifiers across imports
* If you modify your JSON configuration, truncate the model tables and re-run the batch import
* Each batch import execution is tracked with a unique execution ID that can be used to monitor progress
* The batch import runs in a separate transaction for each chunk (default 100-200 items per chunk)


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
