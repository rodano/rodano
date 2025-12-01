# Rodano

This repository contains the Rodano platform and tools helping in its management. Rodano is a usual modern web application, with a backend providing an HTTP API to multiple frontends.

The detailed content of this repository is the following:
- the folder `backend` contains the source code of the backend, written in Java, including the model, the business layers and the HTTP API
- the folder `frontends` contains the different frontends (the main frontend used by the medical staff, the configurator used by data managers, and the ePro mobile web application for study subjects)
- the folder `manager` contains a web application to manage Rodano when it is run inside a Dockerized environment
- a Docker Compose file to start the application locally
- the `ansible` folder contains an Ansible script to deploy the application on a remote server over SSH

## Start Rodano locally

To start Rodano locally, you need a configuration file. You can use a study repository if you have access to one, as it contains the study's configuration and custom code. Alternatively, you can use the test configuration that's already embedded in this repository.

### Requirements

Make sure Docker and Docker Compose are available on your computer.

Then, create a file named `env` at the root of this repository containing the following:
```
STUDY_PATH=/path/to/the/study/folder
TAG=rodano-version
MANAGER_MAGIC_TOKEN=my_super_long_token
```

The variables to configure are:
- `STUDY_PATH`: the path to a folder containing a study repository (including its configuration and its custom code)
- `TAG`: the version of Rodano to use, using a Git tag
- `MANAGER_MAGIC_TOKEN`: a magic token that will allow you to access the manager container

To use the embedded test configuration, `STUDY_PATH` must be set to `/path/to/rodano/repository///backend/src/man/resources/config`.

Then, run:
```
docker compose --env-file env --project-name rodano up
```
The project name must be set to `rodano` so the `manager` container can work properly.

Finally, open your browser and browse [http://localhost:7586](http://localhost:7586).

To force the refresh of the images, run:
```
docker compose --env-file env pull
```

## Deploy Rodano instances

See the [dedicated documentation](ansible/README.md).
