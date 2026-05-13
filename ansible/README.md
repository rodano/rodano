# Ansible deployer

This folder contains an Ansible playbook to deploy Rodano and manage a pool of Rodano instances. The playbook only works with Debian-like servers.

## Use

To start, fill-in the `hosts.yml` file with the right information:
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
	  manager_magic_token=mylongmagictoken
```

The variables to configure are:
- `ansible_ssh_user`: the user to use on the server
- `study_git_url`: the URL of the Git repository containing the study
- `study_git_reference`: the Git reference to fetch from the Git repository
- `rodano_git_reference`: the Git reference used for Rodano
- `server_name`: the FQDN of the server, use to generate the Let's Encrypt certificate
- `server_email`: an email, use to generate the Let's Encrypt certificate
- `manager_magic_token`: a magic token that will allow you to access the manager container

Then, you need to install the dependencies:
```
python3 -m venv .venv
source .venv/bin/activate
pip3 install -r requirements.txt
ansible-galaxy install -r requirements.yml
```

Finally, run the playbook:
```
ansible-playbook -i hosts.yml debian-setup.yml
```
The playbook will set up or update the instances with the specified parameters.

## Development

To lint the Ansible scripts, use:
```
ansible-lint *
```
