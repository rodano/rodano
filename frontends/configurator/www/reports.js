import './basic-tools/extension.js';

import {UI} from './tools/ui.js';
import {Settings} from './settings.js';
import {Languages} from './languages.js';
import {NodeTools} from './node_tools.js';
import {Report} from './model/config/report.js';
import {CSV} from './basic-tools/csv.js';
import {Router} from './router.js';
import {Entities} from './model/config/entities.js';

const WorkflowToggle = {
	selected: {
		image: 'images/bullet_green.png',
		label: 'Remove workflow ${workflowId} from all field models of dataset model ${datasetModelId}'
	},
	unselected: {
		image: 'images/bullet_red.png',
		label: 'Add workflow ${workflowId} on all field models of dataset model ${datasetModelId}'
	}
};

function generate_export_event_models_form_models(study, scope_model) {
	const language = Languages.GetLanguage();
	const event_models = scope_model.eventModels;
	const data = [];
	//add header
	data.push([scope_model.getLocalizedLabel(language), 'Directly attached', ...event_models.map(e => e.getLocalizedLabel(language))]);
	//add data
	study.formModels.forEach(form_model => {
		data.push([
			form_model.getLocalizedLabel(language),
			scope_model.formModelIds.includes(form_model.id) ? 'X' : '',
			...event_models.map(e => e.formModelIds.includes(form_model.id) ? 'X' : '')
		]);
	});
	return data;
}

function draw_event_models_form_models(study, scope_model) {
	const table = document.createElement('table');

	const event_models = scope_model.eventModels.slice();

	//colgroup
	const colgroup = document.createElement('colgroup');
	table.appendChild(colgroup);
	colgroup.appendChild(document.createFullElement('col', {style: 'width: 15rem;'}));
	[...Array(event_models.length + 1).keys()].map(() => document.createElement('col')).forEach(Node.prototype.appendChild, colgroup);

	//header
	const header = document.createElement('thead');
	table.appendChild(header);

	const form_modelable_line = document.createElement('tr');
	const event_models_line = document.createElement('tr');
	header.appendChild(form_modelable_line);
	header.appendChild(event_models_line);

	const scope_model_cell = document.createFullElement('th', {rowspan: 2});
	scope_model_cell.appendChild(NodeTools.Draw(scope_model));
	const download = document.createFullElement('img', {src: 'images/disk.png'});
	download.addEventListener(
		'click',
		function() {
			const data = generate_export_event_models_form_models(study, scope_model);
			new CSV(data).download(`${scope_model.id.toLowerCase()}_test_and_assessments.csv`);
		}
	);
	scope_model_cell.appendChild(download);
	form_modelable_line.appendChild(scope_model_cell);

	const scope_model_attachment_cell = document.createFullElement('th', {'data-scope-model-id': scope_model.id,rowspan: 2}, 'Directly attached');
	form_modelable_line.appendChild(scope_model_attachment_cell);

	event_models.forEach(function(event_model) {
		const event_model_cell = document.createFullElement('th', {'data-event-model-id': event_model.id});
		event_model_cell.appendChild(NodeTools.Draw(event_model));
		event_models_line.appendChild(event_model_cell);
	});

	function toggle_scope_model_form_model() {
		const cell = this.parentNode;
		const scope_model = study.getScopeModel(cell.dataset.scopeModelId);
		if(scope_model.formModelIds.includes(cell.dataset.formModelId)) {
			scope_model.formModelIds.removeElement(cell.dataset.formModelId);
			this.style.opacity = '0.1';
		}
		else {
			scope_model.formModelIds.push(cell.dataset.formModelId);
			this.style.opacity = '1';
		}
	}

	function toggle_event_model_form_model() {
		const cell = this.parentNode;
		const event = study.getEventModel(cell.dataset.eventModelId);
		if(event.formModelIds.includes(cell.dataset.formModelId)) {
			event.formModelIds.removeElement(cell.dataset.formModelId);
			this.style.opacity = '0.1';
		}
		else {
			event.formModelIds.push(cell.dataset.formModelId);
			this.style.opacity = '1';
		}
	}

	function update_column(cell, updater) {
		let cells;
		if(cell.dataset.scopeModelId) {
			cells = table.querySelectorAll(`[data-scope-model-id="${cell.dataset.scopeModelId}"]`);
		}
		else {
			cells = table.querySelectorAll(`[data-event-model-id="${cell.dataset.eventModelId}"]`);
		}
		cells.forEach(updater);
	}

	function highlight_column() {
		update_column(this, c => c.style.backgroundColor = 'var(--background-light-color)');
	}

	function lowlight_column() {
		update_column(this, c => c.style.backgroundColor = '');
	}

	function draw_form_model(form_model) {
		const form_model_line = document.createElement('tr');
		const form_model_cell = document.createFullElement('td');
		form_model_cell.appendChild(NodeTools.Draw(form_model));
		form_model_line.appendChild(form_model_cell);

		const scope_model_form_model_cell = document.createElement('td');
		scope_model_form_model_cell.addEventListener('mouseover', highlight_column);
		scope_model_form_model_cell.addEventListener('mouseout', lowlight_column);
		scope_model_form_model_cell.dataset.scopeModelId = scope_model.id;
		scope_model_form_model_cell.dataset.formModelId = form_model.id;

		const toggle = document.createFullElement('img', {src: 'images/tick.png'});
		toggle.style.opacity = (scope_model.formModelIds.includes(form_model.id) ? 1 : 0.1).toString();
		toggle.addEventListener('click', toggle_scope_model_form_model);
		scope_model_form_model_cell.appendChild(toggle);

		form_model_line.appendChild(scope_model_form_model_cell);

		function draw_event_model(event_model) {
			const event_model_form_model_cell = document.createElement('td');
			event_model_form_model_cell.addEventListener('mouseover', highlight_column);
			event_model_form_model_cell.addEventListener('mouseout', lowlight_column);
			event_model_form_model_cell.dataset.eventModelId = event_model.id;
			event_model_form_model_cell.dataset.formModelId = form_model.id;

			const toggle = document.createFullElement('img', {src: 'images/tick.png'});
			toggle.style.opacity = (event_model.formModelIds.includes(form_model.id) ? 1 : 0.1).toString();
			toggle.addEventListener('click', toggle_event_model_form_model);
			event_model_form_model_cell.appendChild(toggle);

			return event_model_form_model_cell;
		}

		event_models.map(draw_event_model).forEach(Node.prototype.appendChild, form_model_line);

		return form_model_line;
	}

	//body
	const body = document.createElement('tbody');
	table.appendChild(body);
	study.formModels.map(draw_form_model).forEach(Node.prototype.appendChild, body);

	return table;
}

function draw_workflows_field_models(study, dataset_model) {
	function update_workflow_toggle(workflow_id) {
		let properties;
		if(dataset_model.fieldModels.every(a => a.workflowIds.includes(workflow_id))) {
			properties = WorkflowToggle.selected;
		}
		else {
			properties = WorkflowToggle.unselected;
		}
		const cell = table.querySelector(`th[data-dataset-model-id="${dataset_model.id}"][data-workflow-id="${workflow_id}"]`);
		const toggle = cell.querySelector('img');
		toggle.setAttributes({src: properties.image, alt: properties.label.replaceObject(cell.dataset), title: properties.label.replaceObject(cell.dataset)});
	}

	const table = document.createElement('table');

	//try to retain only workflows that make sense
	const workflows = study.workflows.filter(w => !w.aggregateWorkflowId && w.getScopeModels().isEmpty() && w.getEventModels().isEmpty() && w.getFormModels().isEmpty());
	//sort workflows
	workflows.sort((workflow_1, workflow_2) => {
		return workflow_1.getLocalizedLabel().compareTo(workflow_2.getLocalizedLabel());
	});

	//colgroup
	const colgroup = document.createElement('colgroup');
	table.appendChild(colgroup);
	colgroup.appendChild(document.createFullElement('col', {style: 'width: 25rem;'}));
	[...Array(workflows.length).keys()].map(() => document.createElement('col')).forEach(Node.prototype.appendChild, colgroup);

	//header
	const header = document.createElement('thead');
	table.appendChild(header);

	const workflows_line = document.createElement('tr');
	header.appendChild(workflows_line);

	const dataset_model_cell = document.createFullElement('th');
	dataset_model_cell.appendChild(NodeTools.Draw(dataset_model));
	workflows_line.appendChild(dataset_model_cell);

	workflows
		.map(workflow => {
			const workflow_cell = document.createFullElement('th', {'data-dataset-model-id': dataset_model.id, 'data-workflow-id': workflow.id, style: 'text-align: center'});
			workflow_cell.appendChild(NodeTools.Draw(workflow));
			const toggle = document.createFullElement('img');
			toggle.addEventListener('click', toggle_dataset_model_workflow);
			workflow_cell.appendChild(toggle);
			return workflow_cell;
		})
		.forEach(Node.prototype.appendChild, workflows_line);

	workflows.forEach(w => update_workflow_toggle(w.id));

	function toggle_dataset_model_workflow() {
		const cell = this.parentNode;
		const workflow_id = cell.dataset.workflowId;
		let properties;
		if(dataset_model.fieldModels.some(a => a.workflowIds.includes(workflow_id))) {
			dataset_model.fieldModels.forEach(a => a.workflowIds.removeElement(workflow_id));
			table.querySelectorAll(`td[data-dataset-model-id="${dataset_model.id}"][data-workflow-id="${workflow_id}"] img`).forEach(i => i.style.opacity = '0.1');
			properties = WorkflowToggle.unselected;
		}
		else {
			dataset_model.fieldModels.filter(a => !a.workflowIds.includes(workflow_id)).forEach(a => a.workflowIds.push(workflow_id));
			table.querySelectorAll(`td[data-dataset-model-id="${dataset_model.id}"][data-workflow-id="${workflow_id}"] img`).forEach(i => i.style.opacity = '1');
			properties = WorkflowToggle.selected;
		}
		this.setAttributes({src: properties.image, alt: properties.label.replaceObject(cell.dataset), title: properties.label.replaceObject(cell.dataset)});
	}

	function toggle_field_model_workflow() {
		const cell = this.parentNode;
		const field_model = study.getDatasetModel(cell.dataset.datasetModelId).getFieldModel(cell.dataset.fieldModelId);
		if(field_model.workflowIds.includes(cell.dataset.workflowId)) {
			field_model.workflowIds.removeElement(cell.dataset.workflowId);
			this.style.opacity = '0.1';
		}
		else {
			field_model.workflowIds.push(cell.dataset.workflowId);
			this.style.opacity = '1';
		}
		update_workflow_toggle(cell.dataset.workflowId);
	}

	function update_column(cell, updater) {
		const cells = table.querySelectorAll(`[data-workflow-id="${cell.dataset.workflowId}"]`);
		cells.forEach(updater);
	}

	function highlight_column() {
		update_column(this, c => c.style.backgroundColor = 'var(--background-light-color)');
	}

	function lowlight_column() {
		update_column(this, c => c.style.backgroundColor = '');
	}

	function draw_field_model(field_model) {
		const field_model_line = document.createElement('tr');
		const field_model_cell = document.createFullElement('td');
		field_model_cell.appendChild(NodeTools.Draw(field_model));
		field_model_line.appendChild(field_model_cell);

		function draw_workflow(workflow) {
			const workflow_field_model_cell = document.createFullElement('td', {'data-dataset-model-id': dataset_model.id, 'data-field-model-id': field_model.id, 'data-workflow-id': workflow.id});
			workflow_field_model_cell.addEventListener('mouseover', highlight_column);
			workflow_field_model_cell.addEventListener('mouseout', lowlight_column);

			const toggle = document.createFullElement('img', {src: 'images/tick.png'});
			toggle.style.opacity = (field_model.workflowIds.includes(workflow.id) ? 1 : 0.1).toString();
			toggle.addEventListener('click', toggle_field_model_workflow);
			workflow_field_model_cell.appendChild(toggle);

			return workflow_field_model_cell;
		}

		workflows.map(draw_workflow).forEach(Node.prototype.appendChild, field_model_line);
		return field_model_line;
	}

	//body
	const body = document.createElement('tbody');
	table.appendChild(body);
	dataset_model.fieldModels.map(draw_field_model).forEach(Node.prototype.appendChild, body);

	return table;
}

function draw_checks(checks, container, number) {
	number.textContent = checks.length;
	checks
		.map(check => {
			//check container
			const li = document.createFullElement('li');
			//link to node
			li.appendChild(document.createFullElement(
				'a',
				{href: `#node=${check.node.getGlobalId()}`, title: check.node.getLocalizedLabel(Languages.GetLanguage())},
				check.message
			));
			//link to patch
			if(check.patch) {
				li.appendChild(
					document.createFullElement(
						'a',
						{href: '#', style: 'margin-left: 1rem;', title: check.solution},
						'Correct this',
						{click: function(event) {
							event.stop();
							check.patch.call(check.node);
							this.parentNode.style.textDecoration = 'line-through';
						}}
					)
				);
			}
			return li;
		})
		.forEach(Node.prototype.appendChild, container.empty());
}

function report_full(node, settings) {
	const report = node.report ? node.report(settings) : new Report(node);
	if(node.getEntity().children) {
		for(const [entity_name, relation] of Object.entries(node.getEntity().children)) {
			const entity = Entities[entity_name];
			for(let n = 0; n < relation.size; n++) {
				node.getChildren(entity, n).map(n => report_full(n, settings)).forEach(Report.prototype.add, report);
			}
		}
	}
	return report;
}

export const Reports = {
	Init: function() {
		//dialogs are managed by the application URL
		(document.getElementById('consistency_check')).addEventListener('close', () => Router.CloseReport());
		(document.getElementById('field_models_workflows')).addEventListener('close', () => Router.CloseReport());
		(document.getElementById('event_models_form_models')).addEventListener('close', () => Router.CloseReport());
	},
	DrawEventModelsFormModels: function(study) {
		const dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('event_models_form_models'));
		study.scopeModels
			.map(s => draw_event_models_form_models(study, s))
			.forEach(Node.prototype.appendChild, dialog.querySelector('.textcontent').empty());
		dialog.showModal();
	},
	DrawWorkflowsFieldModels: function(study) {
		const dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('field_models_workflows'));
		study.datasetModels
			.map(d => draw_workflows_field_models(study, d))
			.forEach(Node.prototype.appendChild, dialog.querySelector('.textcontent').empty());
		dialog.showModal();
	},
	GenerateReport: function(study) {
		return report_full(study, Settings.All());
	},
	ShowConsistencyCheck: function(study) {
		UI.StartLoading();

		UI.Delay(() => {
			const report = Reports.GenerateReport(study);
			if(!report.isEmpty()) {
				draw_checks(report.errors, document.getElementById('consistency_check_section_errors'), document.getElementById('consistency_check_tab_errors'));
				draw_checks(report.warnings, document.getElementById('consistency_check_section_warnings'), document.getElementById('consistency_check_tab_warnings'));
				draw_checks(report.infos, document.getElementById('consistency_check_section_infos'), document.getElementById('consistency_check_tab_infos'));
			}
			else {
				UI.Notify('Everything\'s fine');
			}
			UI.StopLoading();
			/**@type {HTMLDialogElement}*/ (document.getElementById('consistency_check')).showModal();
		});
	}
};
