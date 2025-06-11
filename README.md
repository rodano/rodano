# Rodano

This repository contains the Rodano platform and tools helping in its management. Rodano is a usual modern web application, with a backend providing an HTTP API to multiple frontends.

The detailed content of this repository is the following:
- the folder `backend` contains the source code of the backend, written in Java, including the model, the business layers and the HTTP API
- the folder `frontends` contains the different frontends (the main frontend used by the medical staff, the configurator used by data managers, and the ePro mobile web application for study subjects)
- a Docker Compose file to start the application locally
- the `ansible` folder contains an Ansible script to deploy the application on a remote server over SSH

## Start Rodano locally

To start Rodano locally, you must already have access to a study repository, containing the configuration of the study and its custom code.

### Requirements

Make sure Docker and Docker Compose are available on your computer.

Then, create a file named `env` at the root of this repository containing the following:
```
STUDY_PATH=/path/to/the/study/folder
TAG=rodano-version
```

The variables to configure are:
- `STUDY_PATH`: the path to a folder containing a study repository (including its configuration and its custom code)
- `TAG`: the version of Rodano to use, using a Git tag

Then, run:
```
docker compose --env-file env up
```

Finally, open your browser and browse [http://localhost:7586](http://localhost:7586).

To force the refresh of the images, run:
```
docker compose --env-file env pull
```

## Deploy Rodano instances

In the `ansible` folder, an Ansible playbook is provided to manage a pool of Rodano instances. The playbook only works with Debian-like servers. To start, fill-in the `hosts.yml` file with the right information:
```
rodanos:
  hosts:
    my-study.com:
      ansible_ssh_user: debian
      study_git_url: https://github.com/my-organization/my-study.git
      study_git_reference: master
      rodano_git_reference: dev
      server_name: my-study.com
      server_email: my.email@my-study.com
```

The variables to configure are:
- `ansible_ssh_user`: the user to use on the server
- `study_git_url`: the URL of the Git repository containing the study
- `study_git_reference`: the Git reference to fetch from the Git repository
- `server_name`: the FQDN of the server, use to generate the Let's Encrypt certificate
- `server_email`: an email, use to generate the Let's Encrypt certificate

Then, you can run:
```
ansible-playbook -i hosts.yml debian-setup.yml
```
The playbook will set up or update the instances with the specified parameters.
