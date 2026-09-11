# Rodano

This repository contains the Rodano platform and tools for managing it. Rodano is a standard modern web application, with a backend providing an HTTP API to multiple frontends.

This repository contains the following:
- `backend`: backend source code written in Java, including the model, business layers, and HTTP API
- `frontends`: the various frontends (the main frontend used by medical staff, the configurator used by data managers, and the ePRO mobile web application for study subjects)
- `manager`: a web application to manage Rodano when running inside a Dockerized environment
- `ansible`: an Ansible script to deploy the application to a remote server over SSH
- a Docker Compose file to run the application locally

## Start Rodano locally

To start Rodano locally, you need a configuration file. You can use a study repository if you have access to one, as it contains the study configuration and custom code. Alternatively, you can use the test configuration already embedded in this repository.

### Requirements

Make sure Docker and Docker Compose are available on your computer.

Then, edit the file `.env` at the root of this repository to configure the following variables:
- `STUDY_PATH`: the path to a folder containing a study repository (including its configuration and its custom code)
- `TAG`: the version of Rodano to use, using a Git tag
- `MANAGER_MAGIC_TOKEN`: a magic token that will allow you to access the manager container

To use the embedded test configuration, `STUDY_PATH` must be set to `/path/to/rodano/repository/backend/src/main/resources/config`.

### SMTP server

The SMTP server is configured through environment variables. By default, Docker Compose starts a bundled `smtp` service used for testing. To use an external SMTP server, set the following variables in the `.env` file: `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD` and `SMTP_STARTTLS`.

### Use

Once the env file is configured, run:
```
docker compose up
```

Finally, open your browser and browse [http://localhost:7586](http://localhost:7586).

To force the refresh of the images, run:
```
docker compose pull
```

If you use the embedded test configuration, you can initialize the database with sample data using the following command:
```
docker compose run --entrypoint="java -Dspring.profiles.active=database -Drodano.init.with-data=true -Drodano.init.with-users=true -cp /app/rodano.jar org.springframework.boot.loader.launch.PropertiesLauncher" backend
```
See the [backend documentation](backend/README.md) for advanced configuration of the database initialization.

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
- `--dry-run`: run every check and print the commands without changing anything.
- `--yes`: skip the interactive confirmation prompt.
