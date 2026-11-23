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

## Trust and Security Model

### Code Execution and Study Folder Access

Every `*.R` file in `${STUDY_PATH}/dm-reporter` (the `custom-reports` folder) is executed within the DM Reporter service using R's `source()` function. This means:

**Write access to study folders grants the ability to execute arbitrary code on the platform.** Users or processes with write access to the study's `custom-reports` directory can:
1. Add or modify R scripts in the folder
2. These scripts will be automatically sourced and executed by the DM Reporter service
3. Execute any R code with the permissions of the DM Reporter service process

**Recommendation:** Restrict write access to the `custom-reports` directory to authorized administrators only. Treat this location as critically as you would treat a code repository or deployment pipeline.

### Feature Flags and UI

The `REPORTER_MODULE_*` and `LOGIN_REPORTER` feature flags control UI visibility—they determine which tabs and report options appear in the frontend. However, **these flags do not enforce data access restrictions** at the backend:

- The backend continues to validate all data access through the user's authentication token and assigned roles
- Backend authorization checks determine whether a user can actually access the requested data, regardless of which UI tabs are visible

**Important:** Data security relies on backend authorization checks, not UI hiding. All sensitive data access must be protected through proper role-based access control (RBAC) and authentication mechanisms, not feature flag toggling.
