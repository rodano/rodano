import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class CMSWidget extends Node {
	static getProperties() {
		return {
			section: {type: Entities.CMSSection.name, back_reference: true},
			type: {type: 'string'},
			textBefore: {type: 'string'},
			textAfter: {type: 'string'},
			parameters: {type: 'object'},
			width: {type: 'string'},
			requiredFeature: {type: 'string'},
			requiredRight: {type: Entities.ScopeCriterionRight.name}
		};
	}

	constructor(values) {
		super();
		this.section = undefined;
		this.type = undefined;
		this.textBefore = undefined;
		this.textAfter = undefined;
		this.parameters = {};
		this.width = 'FULL';
		this.requiredFeature = undefined;
		this.requiredRight = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}
	getLocalizedLabel() {
		return 'Widget';
	}

	//bus
	onChangeWorkflowWidgetId(event) {
		if(this.type === 'WORKFLOW') {
			if(this.parameters.workflow && this.parameters.workflow === event.oldValue) {
				this.parameters.workflow = event.newValue;
			}
		}
	}
	onDeleteWorkflowWidget(event) {
		if(this.type === 'WORKFLOW') {
			if(this.parameters.workflow === event.node.id) {
				this['delete']();
			}
		}
	}
	onChangeWorkflowSummaryId(event) {
		if(this.type === 'WORKFLOWS_SUMMARY') {
			if(this.parameters.summary && this.parameters.summary === event.oldValue) {
				this.parameters.summary = event.newValue;
			}
		}
	}
	onDeleteWorkflowSummary(event) {
		if(this.type === 'WORKFLOWS_SUMMARY') {
			if(this.parameters.summary === event.node.id) {
				this['delete']();
			}
		}
	}
}
