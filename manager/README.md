# Manager

A web application to manage a Rodano instance. It is designed to run as a sidecar container in a Rodano Docker Compose environment. It can perform the following tasks:
- start and stop the backend service (if the main application runs in a Docker Compose environment)
- display backend service logs (if the main application runs in a Docker Compose environment)
- create and restore backups
- show connected users

## Local use

Although designed as a sidecar container, the manager can also run outside of the Docker Compose environment.

Two setups are supported:
1. The rest of the application runs in Docker. The manager connects to the Docker Compose environment and operates normally.
2. Nothing runs in Docker. The manager runs in degraded mode: it cannot start/stop the backend service or stream its logs, but it can still reach locally running services (backend, database, etc.).

From the `website` folder, install the dependencies:
```
python3 -m venv .venv
source .venv/bin/activate
pip3 install -r requirements.txt
```

Set the required environment variables:
- `MAGIC_TOKEN`: authentication token (required)
- `DEBUG`: enable the debug mode, that allows to perform backup on non production instances (default `false`)
- `BACKEND_HOST`: backend host (default: `localhost`)
- `BACKEND_PORT`: backend port (default: `8080`)
- `DATABASE_HOST`: database host (default: `localhost`)
- `DATABASE_PORT`: database port (default: `3306`)
- `DATABASE_NAME`: database name (default: `rodano`)
- `DATABASE_USER`: database user (default: `root`)
- `DATABASE_PASSWORD`: database password (default: `root`)
- `BACKUPS_STORAGE_PATH`: backup storage path (default: `/tmp/backups`)
- `USER_CONTENT`: application user content directory (default: `/tmp/user_content`)

Then start the server:
```
python server.py
```
