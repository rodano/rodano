import './basic-tools/extension.js';

import {StudyHandler} from './study_handler.js';
import {Languages} from './languages.js';
import {CSV} from './basic-tools/csv.js';

const CHECK_TYPE_BLOCKING = 'Blocking';
const CHECK_CLASSIFICATION_ONLINE = 'Online';

function retrieve_field_models(study) {
	const scope_models = study.getLeafScopeModels();
	const dataset_model_ids = [
		...scope_models.flatMap(s => s.datasetModelIds),
		...scope_models.flatMap(s => s.eventModels).flatMap(e => e.datasetModelIds)
	];
	return study.datasetModels.filter(d => dataset_model_ids.includes(d.id)).flatMap(d => d.fieldModels);
}

function generate_field_model_lines(field_model) {
	const language = Languages.GetLanguage();
	const form_models = field_model.getFormModels();
	const study = field_model.datasetModel.study;

	const form_model_label = form_models.isEmpty() ? '' : form_models[0].getLocalizedShortname(language);

	const field_model_lines = [];
	const field_model_data = [form_model_label, field_model.datasetModel.id, field_model.id, field_model.getLocalizedShortname(language)];
	let has_check = false;
	if(!field_model.validatorIds.isEmpty()) {
		has_check = true;
		field_model.getValidators().forEach(function(validator) {
			let classification;
			if(validator.workflowId) {
				const workflow = study.getWorkflow(validator.workflowId);
				classification = workflow.getLocalizedShortname(language);
			}
			else {
				classification = CHECK_TYPE_BLOCKING;
			}
			field_model_lines.push([validator.id, ...field_model_data, CHECK_CLASSIFICATION_ONLINE, classification, validator.getLocalizedDescription(language), validator.getLocalizedMessage(language)]);
		});
	}
	if(['DATE', 'DATE_SELECT'].includes(field_model.type) && !field_model.allowDateInFuture) {
		has_check = true;
		field_model_lines.push(['FUTURE_DATE_NOT_ALLOWED', ...field_model_data, CHECK_CLASSIFICATION_ONLINE, CHECK_TYPE_BLOCKING, 'Date cannot be in the future.', 'Date cannot be in the future']);
	}
	if(field_model.matcher) {
		has_check = true;
		const message = field_model.matcherMessage[language] || `Text does not match: ${field_model.matcher}.`;
		field_model_lines.push([`${field_model.datasetModel.id}_${field_model.id}_MATCHER`, ...field_model_data, CHECK_CLASSIFICATION_ONLINE, CHECK_TYPE_BLOCKING, 'Text must match a regular expression', message]);
	}
	if(field_model.maxLength) {
		has_check = true;
		const message = `Text must have at most ${field_model.maxLength} characters`;
		field_model_lines.push(['MAX_LENGTH', ...field_model_data, CHECK_CLASSIFICATION_ONLINE, CHECK_TYPE_BLOCKING, 'Text is limited in size', message]);
	}
	if(['NUMBER'].includes(field_model.dataType) && field_model.minValue) {
		has_check = true;
		const message = `Number is too small. Number must be greater or equal to ${field_model.minValue}`;
		field_model_lines.push([`${field_model.datasetModel.id}_${field_model.id}_MIN_VALUE`, ...field_model_data, CHECK_CLASSIFICATION_ONLINE, CHECK_TYPE_BLOCKING, 'Number must be greater or equal to a value', message]);
	}
	if(['NUMBER'].includes(field_model.dataType) && field_model.maxValue) {
		has_check = true;
		const message = `Number is too big. Number must be lower or equal to ${field_model.maxValue}`;
		field_model_lines.push([`${field_model.datasetModel.id}_${field_model.id}_MAX_VALUE`, ...field_model_data, CHECK_CLASSIFICATION_ONLINE, CHECK_TYPE_BLOCKING, 'Number must be lower or equal to a value', message]);
	}
	if(!has_check) {
		field_model_lines.push(['', ...field_model_data, 'No check']);
	}
	return field_model_lines;
}

function generate_form_model_relations(form_model) {
	const relations = [];
	for(const layout of form_model.layouts) {
		for(const cell of layout.getCells()) {
			for(const criterion of cell.visibilityCriteria) {
				const targets = [];
				//add cells in targeted layouts
				targets.pushAll(criterion.targetLayoutIds
					.map(i => form_model.getLayout(i))
					.flatMap(l => l.getCells())
					.filter(c => c.hasFieldModel()));
				//add target cells
				targets.pushAll(criterion.targetCellIds
					.map(i => form_model.getCell(i)));
				const target_field_models = targets.map(c => {return {datasetModelId: c.datasetModelId, fieldModelId: c.fieldModelId};});
				//build relations
				relations.push({
					datasetModelId: cell.datasetModelId,
					fieldModelId: cell.fieldModelId,
					values: criterion.values,
					targets: target_field_models,
					negate: criterion.action === 'HIDE'
				});
			}
		}
	}
	return relations;
}

export const Digests = {
	DataValidationPlan: function() {
		const data = [];
		data.push(['Validator ID', 'Form model', 'Dataset model ID', 'Field model ID', 'ECRF Label', 'Check classification', 'Check type', 'Check description', 'Check message']);
		const lines = retrieve_field_models(StudyHandler.GetStudy()).flatMap(generate_field_model_lines);
		lines.sort((line_1, line_2) => {
			const form_model_1 = line_1[1];
			const form_model_2 = line_2[1];
			if(form_model_1 !== form_model_2) {
				return form_model_1.compareTo(form_model_2);
			}
			const dataset_model_1 = line_1[2];
			const dataset_model_2 = line_2[2];
			if(dataset_model_1 !== dataset_model_2) {
				return dataset_model_1.compareTo(dataset_model_2);
			}
			return line_1[3].compareTo(line_2[3]);
		});
		data.pushAll(lines);
		new CSV(data).download('dvp.csv');
	},
	FieldModelRelations: function() {
		const all_relations = StudyHandler.GetStudy().formModels.flatMap(generate_form_model_relations);
		//merge relations by dataset model and field models
		const relations = all_relations.reduce((relations, current) => {
			let dataset_model_relations = relations.find(r => r.datasetModelId === current.datasetModelId);
			if(!dataset_model_relations) {
				dataset_model_relations = {datasetModelId: current.datasetModelId, relations: []};
				relations.push(dataset_model_relations);
			}

			let field_model_relations = dataset_model_relations.relations.find(r => r.fieldModelId === current.fieldModelId);
			if(!field_model_relations) {
				field_model_relations = {fieldModelId: current.fieldModelId, relations: []};
				dataset_model_relations.relations.push(field_model_relations);
			}
			const relation = {values: current.values, target: current.targets, negate: current.negate};
			//check relations has not already been added
			//this may happen if multiple form models have the same visibility criteria
			if(!field_model_relations.relations.some(r => Object.equals(r, relation))) {
				field_model_relations.relations.push(relation);
			}
			return relations;
		}, []);
		//download file
		const filename = 'field_model_relations.json';
		const blob = new Blob([JSON.stringify(relations, undefined, '\t')], {type: 'application/json'});
		const file = new File([blob], filename, {type: 'application/json', lastModified: Date.now()});
		const url = window.URL.createObjectURL(file);
		//Chrome does not support to set location href
		if(/Chrome/.test(navigator.userAgent)) {
			const link = document.createFullElement('a', {href: url, download: filename});
			const event = new MouseEvent('click', {bubbles: true, cancelable: true});
			link.dispatchEvent(event);
		}
		else {
			location.href = url;
		}
		//revoke url after event has been dispatched
		setTimeout(function() {
			window.URL.revokeObjectURL(url);
		}, 0);
	}
};
