import {TokenManager} from './token.js';

const api_base_url = 'api';

function build_authorization_header() {
	return {Authorization: `Bearer ${TokenManager.GetToken()}`};
}

async function FetchFile(resource, options = {}) {
	options.headers = {...build_authorization_header(), ...options.headers};

	const response = await fetch(resource, options);
	let filename = response.headers.get('Content-Disposition')?.split('filename=')[1] ?? 'download';
	filename = filename.replaceAll(/["']+/g, '');
	return new File([await response.blob()], filename);
}

async function Fetch(resource, options = {}) {
	options.headers = {...build_authorization_header(), ...options.headers};

	const response = await fetch(resource, options);
	const body = await response.json();

	if(response.ok) {
		return body;
	}

	//throw the errors if it's managed by the caller
	const managed_errors = options?.['managed-error'] || [];
	if(managed_errors.includes(response.status)) {
		throw {status: response.status, body};
	}

	//default error handling
	if(response.status === 401) {
		document.getElementById('token').showModal();
	}
	else {
		document.getElementById('error_message').textContent = body.error;
		document.getElementById('error').showModal();
	}
}

export {api_base_url, FetchFile, Fetch};
