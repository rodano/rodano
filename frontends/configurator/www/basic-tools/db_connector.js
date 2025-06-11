export class DBConnector {
	constructor(name, keypath) {
		this.name = name;
		this.keypath = keypath;
		this.database = undefined;
	}
	isOpen() {
		return !!this.database;
	}
	open() {
		return new Promise((resolve, reject) => {
			const version = 1;
			const request = indexedDB.open(this.name, version);
			request.addEventListener('upgradeneeded', () => {
				const db = request.result;
				//delete old store
				if(db.objectStoreNames.contains(this.name)) {
					db.deleteObjectStore(this.name);
				}
				//create new store
				db.createObjectStore(this.name, {keyPath: this.keypath});
			});
			//onsuccess is called after onupgradeneeded
			request.addEventListener('success', () => {
				//store handle to database
				this.database = request.result;
				//add error handler directly to the db to catch all errors
				this.database.addEventListener('error', () => {
					reject(`Uncaught general error with database ${this.name}`);
				});
				this.database.addEventListener('abort', () => {
					reject(`Uncaught abort error with database ${this.name}`);
				});
				resolve(this.database);
			});
			request.addEventListener('error', () => {
				//user did not allow to use IndexedDB
				reject('Use of IndexedDB not allowed');
			});
		});
	}
	drop() {
		return new Promise((resolve, reject) => {
			if(this.isOpen()) {
				this.database.close();
			}
			const request = indexedDB.deleteDatabase(this.name);
			request.addEventListener('error', () => {
				reject(`Error while deleting database ${this.name}`);
			});
			request.addEventListener('success', resolve);
		});
	}
	getCursor() {
		//start transaction
		const transaction = this.database.transaction([this.name], 'readwrite');
		//retrieve store
		const store = transaction.objectStore(this.name);
		//do request
		return store.openCursor();
	}
	add(item) {
		return new Promise((resolve, reject) => {
			if(!this.isOpen()) {
				reject('The database must be open before a transaction can occur');
			}
			//start transaction
			const transaction = this.database.transaction([this.name], 'readwrite');
			transaction.addEventListener('error', () => {
				reject(`Error with transaction while adding item ${item[this.keypath]} in database ${this.name}: ${transaction.error}`);
			});
			transaction.addEventListener('abort', () => {
				reject(`Transaction aborted while adding item ${item[this.keypath]} in database ${this.name}: ${transaction.error}`);
			});
			//retrieve store
			const store = transaction.objectStore(this.name);
			//do request
			const request = store.put(item);
			request.addEventListener('error', () => {
				reject(`Error with request while adding item ${item[this.keypath]} in database ${this.name}: ${transaction.error}`);
			});
			request.addEventListener('success', resolve);
		});
	}
	addAll(items) {
		return Promise.all(items.map(item => this.add(item)));
	}
	get(key) {
		return new Promise((resolve, reject) => {
			if(!this.isOpen()) {
				reject('The database must be open before a transaction can occur');
			}
			//start transaction
			const transaction = this.database.transaction([this.name]);
			transaction.addEventListener('error', () => {
				reject(`Error with transaction while retrieving item ${key} in database ${this.name}: ${transaction.error}`);
			});
			transaction.addEventListener('abort', () => {
				reject(`Transaction aborted while retrieving item ${key} in database ${this.name}: ${transaction.error}`);
			});
			//retrieve store
			const store = transaction.objectStore(this.name);
			//do request
			const request = store.get(key);
			request.addEventListener('error', () => {
				reject(`Error with request while retrieving item ${key} in database ${this.name}: ${transaction.error}`);
			});
			request.addEventListener('success', () => {
				resolve(request.result);
			});
		});
	}
	getAll() {
		return new Promise((resolve, reject) => {
			if(!this.isOpen()) {
				reject('The database must be open before a transaction can occur');
			}
			//start transaction
			const transaction = this.database.transaction([this.name]);
			transaction.addEventListener('error', () => {
				reject(`Error with transaction while retrieving all items from database ${this.name}: ${transaction.error}`);
			});
			transaction.addEventListener('abort', () => {
				reject(`Transaction aborted while retrieving all items from database ${this.name}: ${transaction.error}`);
			});
			//retrieve store
			const store = transaction.objectStore(this.name);
			//do request
			const request = store.getAll();
			request.addEventListener('error', () => {
				reject(`Error with request while retrieving all items from database ${this.name}: ${transaction.error}`);
			});
			request.addEventListener('success', () => {
				resolve(request.result);
			});
		});
	}
	getSome(filter) {
		return this.getAll().then(results => {
			//apply filter on results if needed
			return filter ? results.filter(filter) : results;
		});
	}
	remove(key) {
		return new Promise((resolve, reject) => {
			if(!this.isOpen()) {
				reject('The database must be open before a transaction can occur');
			}
			//start transaction
			const transaction = this.database.transaction([this.name], 'readwrite');
			transaction.addEventListener('error', () => {
				reject(`Error with transaction while removing item ${key} in database ${this.name}: ${transaction.error}`);
			});
			transaction.addEventListener('abort', () => {
				reject(`Transaction aborted while removing item ${key} in database ${this.name}: ${transaction.error}`);
			});
			//retrieve store
			const store = transaction.objectStore(this.name);
			//do request
			const request = store.delete(key);
			request.addEventListener('error', () => {
				reject(`Error with request while removing item ${key} in database ${this.name}: ${transaction.error}`);
			});
			request.addEventListener('success', () => {
				resolve(request.result);
			});
		});
	}
	removeAll() {
		return this.removeSome();
	}
	removeSome(filter) {
		return this.getSome(filter).then(items => Promise.all(items.map(item => this.remove(item[this.keypath]))));
	}
}
