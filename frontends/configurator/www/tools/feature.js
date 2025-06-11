import {UUID} from '../basic-tools/uuid.js';

export class Feature {
	constructor(name) {
		this.id = UUID.Generate();
		this.name = name;
		this.specifications = [];
		this.listeners = {
			specification: []
		};
	}
	addEventListener(event, callback) {
		this.listeners[event].push(callback);
	}
	async it(message, block) {
		const specification = {message: message};
		try {
			await block.call();
			specification.success = true;
		}
		catch(exception) {
			console.log(exception);
			specification.success = false;
			specification.error = exception.message;
		}
		this.specifications.push(specification);
		//callback
		this.listeners['specification'].forEach(c => c.call(undefined, this, specification));
	}
}
