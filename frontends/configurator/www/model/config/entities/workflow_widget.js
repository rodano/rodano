import '../../../basic-tools/extension.js';
import {Entities} from '../entities.js';

import {EntitiesHooks} from '../entities_hooks.js';
import {DisplayableNode} from '../node_displayable.js';
import {WorkflowEntities} from '../workflow_entities.js';
import {WorkflowWidgetColumnType} from '../workflow_widget_column_type.js';

export class WorkflowWidget extends DisplayableNode {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			workflowEntity: {type: 'string'},
			workflowStatesSelectors: {type: 'array'},
			filterExpectedEvents: {type: 'boolean'},
			columns: {type: 'array', subtype: Entities.WorkflowWidgetColumn.name}
		};
	}

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.workflowEntity = undefined;
		this.workflowStatesSelectors = [];
		this.filterExpectedEvents = true;
		this.columns = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	//bus
	onDeleteWorkflowStatesSelector(event) {
		this.workflowStatesSelectors.removeElement(event.node);
	}
	onMoveWorkflowStatesSelector(event) {
		if(event.newParent === this) {
			event.node.parent.workflowStatesSelectors.removeElement(event.node);
			event.node.parent = this;
			this.workflowStatesSelectors.push(event.node);
		}
	}
	onDeleteWorkflowWidgetColumn(event) {
		this.columns.removeElement(event.node);
	}
	onMoveWorkflowWidgetColumn(event) {
		if(event.newParent === this) {
			event.node.widget.columns.removeElement(event.node);
			event.node.widget = this;
			this.columns.push(event.node);
		}
	}

	//report
	report(settings) {
		const report = super.report(settings);
		//retrieve workflow
		const reference_workflow = this.study.getWorkflow(this.workflowStatesSelectors[0].workflowId);
		const field_model_workflow = !reference_workflow.getFieldModels().isEmpty();
		const form_model_workflow = !reference_workflow.getFormModels().isEmpty() || field_model_workflow;
		const event_model_workflow = !reference_workflow.getEventModels().isEmpty() || form_model_workflow;
		//check field model column
		const has_field_model_column = this.columns.some(function(column) {
			return WorkflowEntities.FIELD === WorkflowWidgetColumnType[column.type].workflow_entity;
		});
		if(has_field_model_column && !field_model_workflow) {
			report.addError(`Workflow widget ${this.id} has a configured column showing value data but its workflow is not attached to any field model`);
		}
		//check form model column
		const has_form_model_column = this.columns.some(function(column) {
			return WorkflowEntities.FORM === WorkflowWidgetColumnType[column.type].workflow_entity;
		});
		if(has_form_model_column && !form_model_workflow) {
			report.addError(`Workflow widget ${this.id} has a configured column showing form model data but its workflow is not attached to any form model nor field model`);
		}
		//check event model column
		const has_event_model_column = this.columns.some(function(column) {
			return WorkflowEntities.EVENT === WorkflowWidgetColumnType[column.type].workflow_entity;
		});
		if(has_event_model_column && !event_model_workflow) {
			report.addError(`Workflow widget ${this.id} has a configured column showing event model data but its workflow is not attached to any event model, form model nor field model`);
		}
		return report;
	}
}
