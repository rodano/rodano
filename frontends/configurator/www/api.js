function generate_form_data(parameters) {
	const form_data = new FormData();
	for(const key in parameters) {
		if(parameters.hasOwnProperty(key)) {
			if(Object.isObject(parameters[key])) {
				const json = JSON.stringify(parameters[key], undefined, 1);
				form_data.append(key, json);
			}
			else {
				form_data.append(key, parameters[key]);
			}
		}
	}
	return form_data;
}

export class API {
	constructor(base_url, bearer_token, interceptor, request_timeout) {
		this.timeout = request_timeout || 5000;
		this.url = base_url || 'http://localhost:8080/api';
		this.urls = {
			sessions: `${this.url}/sessions`,
			config: `${this.url}/config`,
			me: `${this.url}/me`
		};
		this.bearerToken = bearer_token;
		this.interceptor = interceptor;

		this.auth = {
			login: (email, password, ts_token, ts_code, ts_key, ts_trust, config) => {
				const data = JSON.stringify({email: email, password: password, tsToken: ts_token, tsCode: ts_code, tsKey: ts_key, tsTrust: ts_trust});
				return this.post(this.urls.sessions, data, config);
			},
			logout: () => {
				return this.remove(this.bearerToken);
			}
		};

		this.config = {
			pull: () => {
				return this.get(this.urls.config);
			},
			push: (config, compressed) => {
				return this.put(this.urls.config, generate_form_data({config: config, compressed: compressed}));
			}
		};

		this.users = {
			me: () => {
				return this.get(this.urls.me);
			}
		};
	}

	request(url, method, data, config) {
		//set custom headers
		const headers = new Headers();
		if(config?.headers) {
			Object.entries(config.headers).forEach(([key, value]) => headers.append(key, value));
		}
		if(this.bearerToken) {
			headers.append('Authorization', `Bearer ${this.bearerToken}`);
		}

		const options = {
			headers: headers,
			method: method
		};

		if(data) {
			options.body = data;
		}

		const request = new Request(url, options);
		const controller = new AbortController();
		const timeout_id = setTimeout(() => {
			controller.abort();
		}, this.timeout);

		return fetch(request, {signal: controller.signal})
			.then(response => {
				if(config?.managed) {
					return response;
				}
				return this.interceptor(request, response, url, method, data, config);
			})
			.finally(() => clearTimeout(timeout_id));
	}

	get(url, config) {
		return this.request(url, 'GET', undefined, config);
	}

	put_post(url, method, data, config) {
		const complete_config = config || {};
		//adjust headers
		if(data.constructor !== FormData) {
			complete_config.headers = complete_config.headers || {};
			complete_config.headers['Content-Type'] = 'application/json';
		}
		return this.request(url, method, data, complete_config);
	}

	put(url, data, config) {
		return this.put_post(url, 'PUT', data, config);
	}

	post(url, data, config) {
		return this.put_post(url, 'POST', data, config);
	}

	remove(url, config) {
		return this.request(url, 'DELETE', undefined, config);
	}
}
