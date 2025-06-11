import {DBConnector} from '../../basic-tools/db_connector.js';

class TemplateRepository {
	constructor(id) {
		this.id = id;
		this.opening = undefined;
	}
}
class DBRepository extends TemplateRepository {
	constructor(id, database_id) {
		super(id);
		this.databaseId = database_id;
		this.database = new DBConnector(database_id, 'id');
	}

	isReady() {
		return this.database.isOpen();
	}

	open() {
		if(this.isReady()) {
			return Promise.resolve(this);
		}
		if(!this.opening) {
			this.opening = this.database.open().then(() => {
				this.opening = undefined;
				return this;
			});
		}
		return this.opening;
	}

	add(template) {
		return this.database.add(template);
	}

	get(type, id) {
		return this.database.get(id);
	}

	getAll(type) {
		return this.database.getSome(template => template.type === type);
	}

	remove(type, id) {
		return this.database.remove(id);
	}

	getConfig() {
		return {
			id: this.id,
			database_id: this.databaseId,
		};
	}
}

class HTTPRepository extends TemplateRepository {
	constructor(id, url, login, password) {
		super(id);
		this.url = url;
		this.login = login;
		this.password = password;
		this.token = undefined;
	}

	isReady() {
		return !!this.token;
	}

	open() {
		if(this.isReady()) {
			return Promise.resolve(this);
		}
		if(!this.opening) {
			this.opening = this.authenticate().then(token => {
				this.token = token;
				this.opening = undefined;
				return this;
			});
		}
		return this.opening;
	}

	async authenticate() {
		const headers = new Headers();
		const body = new Blob([JSON.stringify({login: this.login, password: this.password})], {type: 'application/json'});
		const options = {
			headers: headers,
			method: 'POST',
			mode: 'cors',
			body: body
		};

		const response = await fetch(`${this.url}/authentication`, options);
		const message = await response.json();
		return message.data;
	}

	fetchAuthenticated(url, options) {
		if(!options) {
			options = {};
		}
		if(!options.headers) {
			options.headers = new Headers();
		}
		options.headers.append('Authorization', `Bearer ${this.token}`);
		return fetch(url, options);
	}

	fetchAuthenticatedJSON(url, options) {
		return this.fetchAuthenticated(url, options).then(r => r.json());
	}

	add(template) {
		const body = new Blob([JSON.stringify(template)], {type: 'application/json'});
		const options = {
			method: 'POST',
			body: body
		};
		return this.fetchAuthenticatedJSON(`${this.url}/templates`, options);
	}

	get(type, id) {
		return this.fetchAuthenticatedJSON(`${this.url}/templates?type=${type}&id=${id}`);
	}

	getAll(type) {
		return this.fetchAuthenticatedJSON(`${this.url}/templates?type=${type}`);
	}

	remove(type, id) {
		const options = {
			method: 'DELETE',
			mode: 'cors',
		};
		return this.fetchAuthenticatedJSON(`${this.url}/templates?type=${type}&id=${id}`, options);
	}

	getConfig() {
		return {
			id: this.id,
			url: this.url,
			login: this.login,
			password: this.password
		};
	}
}

export {TemplateRepository, DBRepository, HTTPRepository};
