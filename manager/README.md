# Manager

A manager container for Rodano when it's run in a Docker Compose environment. It's able to perform the following task:
- stop and start the backend
- perform a backup
- restore a backup
- display backend logs

> In a Docker Compose environment, the project name must be `rodano` so the manager can detect the other application containers.

## Local use

The container comes with a website that will perform the management operations in the Docker Compose environment. This website is located in the `website` folder.

To run it locally, install the dependencies:
```
python3 -m venv .venv
source .venv/bin/activate
pip3 install -r requirements.txt
```

Then set a magic token as an environment variable:
```
export MAGIC_TOKEN=super_secret_token
```

Then, start the website:
```
python server.py
```
