import {Node} from './node.js';
import {Report} from './report.js';

export class IdentifiableNode extends Node {
	static ID_MAX_LENGTH = 62;
	static ID_REGEXP = /^[A-Z][A-Z0-9_]{0,61}$/;

	constructor() {
		super();
		this.id = undefined;
	}

	static checkId(id, check_format) {
		if(id) {
			if(id.length > IdentifiableNode.ID_MAX_LENGTH) {
				return 'Too long';
			}
			if(check_format && !IdentifiableNode.ID_REGEXP.test(id)) {
				return 'Wrong format';
			}
		}
		return undefined;
	}

	//get node full id
	getFullId() {
		if(!this.hasParent()) {
			return this.id;
		}
		return `${this.getParent().getFullId()}_${this.id}`;
	}

	report(settings) {
		const report = new Report(this);
		Report.checkId(report, this, settings.id_check !== false);
		return report;
	}
}
