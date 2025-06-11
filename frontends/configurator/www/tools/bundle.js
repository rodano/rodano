import {Feature} from './feature.js';

export class Bundle {
	constructor(dom, website, test) {
		this.dom = dom;
		this.website = website;
		this.test = test;
		this.features = [];
		this.beginTime = undefined;
		this.endTime = undefined;
		this.listeners = {
			begin: [],
			end: [],
			feature: [],
			specification: []
		};
	}
	addEventListener(event, callback) {
		this.listeners[event].push(callback);
	}
	begin() {
		this.beginTime = new Date();
		//callback
		this.listeners['begin'].forEach(c => c.call(undefined, this));
	}
	end() {
		this.endTime = new Date();
		//callback
		this.listeners['end'].forEach(c => c.call(undefined, this));
	}
	getDuration() {
		return this.endTime.getTime() - this.beginTime.getTime();
	}
	async describe(name, block) {
		if(this.beginTime === undefined) {
			throw new Error('Bundle must be started before beginning testing');
		}
		const feature = new Feature(name);
		feature.addEventListener('specification', (_, specification) => {
			this.listeners['specification'].forEach(c => c.call(undefined, this, feature, specification));
		});
		this.features.push(feature);
		this.listeners['feature'].forEach(c => c.call(undefined, this, feature));
		await block.call(undefined, feature);
	}
	getSpecifications() {
		return this.features.flatMap(f => f.specifications);
	}
	getSuccessesNumber() {
		return this.getSpecifications().filter(s => s.success).length;
	}
	getFailsNumber() {
		return this.getSpecifications().filter(s => !s.success).length;
	}
}
