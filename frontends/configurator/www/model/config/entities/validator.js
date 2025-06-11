import '../../../basic-tools/extension.js';

import {EntitiesHooks} from '../entities_hooks.js';
import {Utils} from '../utils.js';
import {Report} from '../report.js';
import {DisplayableNode} from '../node_displayable.js';
import {Entities} from '../entities.js';
import {RuleEntities} from '../rule_entities.js';

export class Validator extends DisplayableNode {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			required: {type: 'boolean'},
			script: {type: 'boolean'},
			workflowId: {type: 'string'},
			invalidStateId: {type: 'string'},
			validStateId: {type: 'string'},
			message: {type: 'object'},
			constraint: {type: Entities.RuleConstraint.name}
		};
	}
	static RuleEntities = [
		RuleEntities.SCOPE,
		RuleEntities.EVENT,
		RuleEntities.DATASET,
		RuleEntities.FIELD
	];

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.required = false;
		this.script = false;
		this.workflowId = undefined;
		this.invalidStateId = undefined;
		this.validStateId = undefined;
		this.message = {};
		this.constraint = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getFieldModels() {
		return this.study.datasetModels.flatMap(d => d.fieldModels).filter(f => f.validatorIds.includes(this.id));
	}
	getLocalizedMessage(languages) {
		return Utils.getLocalizedField.call(this, 'message', languages);
	}

	//rulable and layoutable
	getStudy() {
		return this.study;
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.RuleConstraint:
				return this.constraint ? [this.constraint] : [];
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	getRelations(entity) {
		switch(entity) {
			case Entities.FieldModel:
				return this.getFieldModels();
		}
		throw new Error(`Entity ${entity.name} is not related to entity ${this.getEntity().name}`);
	}

	//bus
	onChangeLanguageId(event) {
		super.onChangeLanguageId(event);
		this.message[event.newValue] = this.message[event.oldValue];
		delete this.message[event.oldValue];
	}
	onDeleteLanguage(event) {
		super.onDeleteLanguage(event);
		delete this.message[event.node.id];
	}
	onChangeWorkflowId(event) {
		if(this.workflowId && this.workflowId === event.oldValue) {
			this.workflowId = event.newValue;
		}
	}
	onDeleteWorkflow(event) {
		if(this.workflowId === event.node.id) {
			this.workflowId = undefined;
		}
	}
	onDeleteWorkflowState(event) {
		if(this.workflowId === event.node.workflow.id) {
			if(this.invalidStateId === event.node.id) {
				this.invalidStateId = undefined;
			}
			if(this.validStateId === event.node.id) {
				this.validStateId = undefined;
			}
		}
	}
	onChangeWorkflowStateId(event) {
		if(this.workflowId === event.node.workflow.id) {
			if(this.invalidStateId === event.oldValue) {
				this.invalidStateId = event.newValue;
			}
			if(this.validStateId === event.oldValue) {
				this.validStateId = event.newValue;
			}
		}
	}

	//report
	report(settings) {
		const report = super.report(settings);
		if(!this.required && !this.script && Object.isEmpty(this.constraint)) {
			report.addWarning(`Validator ${this.id} is useless because it is not required and it has no script neither constraint`);
		}
		Report.checkLocalizedLabel(report, this, 'message');
		return report;
	}
}
