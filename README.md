# Rodano

This repository contains the Rodano platform and tools for managing it. Rodano is a standard modern web application, with a backend providing an HTTP API to multiple frontends.

This repository contains the following:
- `backend`: backend source code written in Java, including the model, business layers, and HTTP API
- `frontends`: the various frontends (the main frontend used by medical staff, the configurator used by data managers, and the ePRO mobile web application for study subjects)
- `manager`: a web application to manage Rodano when running inside a Dockerized environment
- `ansible`: an Ansible script to deploy the application to a remote server over SSH
- a Docker Compose file to run the application locally

## Start Rodano locally

To start Rodano locally, you need a study repository, which contains the study configuration and its custom code. Alternatively, to quickly test the application, you can use the test configuration embedded in this repository.

### Requirements

Make sure Docker and Docker Compose are installed and that the Docker daemon is running.

Then, edit the file `.env` at the root of this repository to configure the following variables:
- `STUDY_PATH`: the path to a folder containing a study repository (including its configuration and its custom code), either absolute or relative to the root of this repository. The folder must contain `config.json`. Compose mounts its `backend` folder to compile plugins and `backend/target/classes` to load them.
- `TAG`: the version of Rodano to use, as a Docker image tag (`dev` or a release tag `vX.Y.Z`). It must be compatible with the study configuration.
- `MANAGER_MAGIC_TOKEN`: a magic token that will allow you to access the manager container

The `.env` file is tracked by Git; do not commit your study path or secrets.

### Quick test with the embedded test configuration

The default values of the `.env` file run the test study embedded in this repository (`STUDY_PATH=./backend/src/main/resources/config`) with the `dev` images. No configuration is required: just follow the steps in [Use](#use).

### SMTP server

The SMTP server is configured through environment variables. By default, Docker Compose starts and uses the `smtp` service. To use an external SMTP server, set the following variables in `.env`: `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD` and `SMTP_STARTTLS`. Compose will still start the bundled `smtp` service unless you change the Compose configuration.

### Use

Once the env file is configured, run from the root of this repository:

```sh
docker compose up
```

Finally, open your browser and browse [http://localhost:7586](http://localhost:7586). On a fresh database, complete the bootstrap form to create the study and its first administrator. The database and the uploaded files are kept in Docker volumes between runs.

The frontend port is exposed on all network interfaces and this setup uses development database credentials: it is intended for local use only.

To refresh the images, run:

```sh
docker compose pull
```

If you use the embedded test configuration, you can initialize the database with sample data and demo users instead of using the bootstrap form. **This command clears the existing database content.** Run it before starting the application, or stop the backend first with `docker compose stop backend`:

```sh
docker compose run --rm --entrypoint="java -Dspring.profiles.active=database -Drodano.init.with-data=true -Drodano.init.with-users=true -cp /app/rodano.jar org.springframework.boot.loader.launch.PropertiesLauncher" backend
```

The command creates the administrator `test@rodano.ch` with the password `Password1!`. See the [backend documentation](backend/README.md) for advanced configuration of the database initialization.

## Deploy Rodano instances

See the [dedicated documentation](ansible/README.md).

## Releasing

Releases follow a Git-flow process driven by the `release.py` script at the root of the repository. Pushing a version tag (`vX.Y.Z` or `vX.Y.Z-rcN`) triggers the CI workflows that create the GitHub release, deploy the documentation website, and publish the versioned Docker images.

The version strings inside `backend/pom.xml` and the frontends' `package.json` files are kept in sync with the release, as they are authoritative for the published Maven and npm artifacts (the `package-lock.json` files are updated only to keep the root package version consistent).

The script runs from a clean working tree and expects the standard `dev` and `main` branches to be up to date with `origin`. Each procedure prints a summary and asks for confirmation before making any change.

```
# 1. Cut the release branch (releases/vX.Y.Z) from dev and set the files to X.Y.Z-SNAPSHOT
./release.py branch X.Y.Z

# 2. (optional, repeatable) Publish a release candidate: bumps files to
#    X.Y.Z-rc.N and pushes the tag vX.Y.Z-rcN (a CI prerelease)
./release.py rc X.Y.Z

# 3. Publish the final release: bumps files to X.Y.Z, merges the release
#    branch into main, pushes the tag vX.Y.Z, back-merges main into dev and
#    deletes the release branch
./release.py release X.Y.Z

# 4. (optional) Remove the release branch and RC tags for a version
./release.py cleanup X.Y.Z
```

Useful flags:
- `--dry-run`: print mutating commands instead of executing them. It still fetches remote refs and does not simulate changes that later checks may depend on.
- `--yes`: skip the interactive confirmation prompt.

## CI test reports

The [Test workflow](.github/workflows/test.yml) uses embedded [JUnit report merger](.github/actions/junit-report-merger/action.yml) and [PDF reporter](.github/actions/junit-pdf-reporter/action.yml) actions. It references them with `$/`, so they run from the same commit as the workflow. The PDF reports show the tested commit, preceded by its tag when tests run on a release tag.
