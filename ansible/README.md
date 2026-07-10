# Ansible deployer

This folder contains an Ansible playbook to deploy Rodano and manage a pool of Rodano instances. The playbook only works with Debian-like servers.

## Use

To start, fill-in the `hosts.yml` file with the right information:
```
all:
  hosts:
    my-study.com:
      ansible_ssh_user: debian
      study_git_url: https://github.com/my-organization/my-study.git
      study_git_reference: master
      server_name: my-study.com
      server_email: my.email@my-study.com
      rodano_environment:
        TAG: dev
        MANAGER_MAGIC_TOKEN: mylongmagictoken
```

The variables to configure are:
- `ansible_ssh_user`: the user to use on the server
- `study_git_url`: the URL of the Git repository containing the study
- `study_git_reference`: the Git reference to fetch from the Git repository
- `server_name`: the FQDN of the server, use to generate the Let's Encrypt certificate
- `server_email`: an email, use to generate the Let's Encrypt certificate
- `rodano_environment`: variables from this dictionary will override the values defined in the root environment file

Then, you need to install the dependencies:
```
python3 -m venv .venv
source .venv/bin/activate
pip3 install -r requirements.txt
ansible-galaxy install -r requirements.yml
```

Finally, run the playbook:
```
ansible-playbook site.yml
```
The playbook will set up or update the instances with the specified parameters.

### Optional runtime parameters

The following optional parameters can be passed to the playbook using `--extra-vars`:

- `reset_database`: When set to `true`, removes the database container and deletes the database volume before starting the service. This will destroy all existing data. Default: `false`.
- `initialize_database`: When set to `true`, runs the database initialization routine to populate the database with initial data and users. This can be used only for the test study. Default: `false`.

### Fast deployment

For quick application updates without infrastructure setup, use the `deploy.yml` playbook. This playbook skips all server setup tasks and only performs the deployment steps. This is significantly faster than running the full `site.yml` playbook and should be used for routine application updates:

```
ansible-playbook deploy.yml
```

The `deploy.yml` playbook supports the same optional parameters as `site.yml`:
```
ansible-playbook deploy.yml --extra-vars "initialize_database=true"
```

**Note:** Use `site.yml` for initial server setup or when infrastructure changes are needed. Use `deploy.yml` for application updates only.

## Development

To lint the Ansible scripts, use:
```
ansible-lint *
```
