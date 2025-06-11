import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class ChartRange extends Node {
	static getProperties() {
		return {
			chart: {type: Entities.Chart.name, back_reference: true},
			id: {type: 'string'},
			labels: {type: 'object'},
			value: {type: 'string'},
			min: {type: 'number'},
			max: {type: 'number'},
			other: {type: 'boolean'},
			show: {type: 'boolean'}
		};
	}

	constructor(values) {
		super();
		this.chart = undefined;
		this.id = undefined;
		this.labels = {};
		this.value = undefined;
		this.min = undefined;
		this.max = undefined;
		this.other = false;
		this.show = false;
		EntitiesHooks?.CreateNode.call(this, values);
	}
}
