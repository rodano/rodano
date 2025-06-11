import {Bundle} from './bundle.js';

export class Suite {
	constructor(name, path, bundles) {
		this.name = name;
		this.path = path;
		this.bundles = bundles || [];
		this.beginTime = undefined;
		this.endTime = undefined;
	}
	getSpecifications() {
		return this.bundles.flatMap(b => b.getSpecifications());
	}
	getSuccessesNumber() {
		return this.getSpecifications().filter(s => s.success).length;
	}
	getFailsNumber() {
		return this.getSpecifications().filter(s => !s.success).length;
	}
	getDuration() {
		//suite may still be running
		const stop = this.endTime || new Date();
		return stop.getTime() - this.beginTime.getTime();
	}
	async run(runner) {
		this.beginTime = new Date();
		for(let i = 0; i < this.bundles.length; i++) {
			await runner.run(this.bundles[i]);
		}
		this.endTime = new Date();
		return this;
	}
	static fromJSON(s, base_path) {
		function concatenate_paths(path) {
			return path ? `${base_path || ''}${s.path || ''}${path}` : undefined;
		}
		//create the suite and its bundles
		return new Suite(s.name, s.path, s.bundles.map(b => new Bundle(b.dom, concatenate_paths(b.website), concatenate_paths(b.test))));
	}
}
