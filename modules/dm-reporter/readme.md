# DM Reporter
An RShiny data reporting tool for custom reports for Rodano.

## Dockerized application

### Build image

```bash
docker build -t dm-reporter:[version] .
```

The image uses the R 4.5.3 Rocker Shiny base and restores the necessary R libraries with renv.

### Run container

```bash
docker run --name=dm-reporter-[study name] -p [host port]:3838 -v [/path/to/study/custom-reports]:/opt/custom-reports -e [environment_variables] dm-reporter:[rodano version]
```

Environment variables:
- **API_URL=[address:port of Rodano API]** Rodano API's host name or IP. If unset, http://localhost:8080 will be used by default.

## Non-dockerized R project

Environment:
- Unless environment variable **API_URL** is set, the application will refer to Rodano API located at http://localhost:8080
- Path to study's "custom-reports" folder should be stored in a text file named "study_path.cfg" should be present in folder "app".
