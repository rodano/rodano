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

### Use

Once the env file is configured, run:
```
docker compose --env-file .env --project-name rodano up
```
The project name must be set to `rodano` so the `manager` container can work properly.

Finally, open your browser and browse [http://localhost:7586](http://localhost:7586).

To force the refresh of the images, run:
```
docker compose --env-file .env pull
```

If you use the embedded test configuration, you can initialize the database with sample data using the following command:
```
docker compose --env-file .env run --entrypoint="java -Dspring.profiles.active=database -Drodano.init.with-data=true -Drodano.init.with-users=true -cp /app/rodano.jar org.springframework.boot.loader.launch.PropertiesLauncher" backend
```
See the [backend documentation](backend/README.md) for advanced configuration of the database initialization.

## Deploy Rodano instances

See the [dedicated documentation](ansible/README.md).
