import {Config} from '../model_config.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {TimelineGraphSectionScale} from '../model/config/entities/timeline_graph_section_scale.js';
import {TimelineGraphSectionPosition} from '../model/config/entities/timeline_graph_section_position.js';

function manage_type(event) {
	const section_type_id = event ? this.value : selected_timeline_graph_section.type;
	const section_type = Config.Enums.TimelineSectionType[section_type_id];

	const timeline_graph_section_dataset_model_id = /**@type {HTMLSelectElement}*/ (document.getElementById('timeline_graph_section_dataset_model_id'));

	if(section_type !== Config.Enums.TimelineSectionType.ACTION) {
		timeline_graph_section_dataset_model_id.parentElement.style.display = 'block';
		document.getElementById('timeline_graph_section_position').style.display = 'block';
	}
	else {
		timeline_graph_section_dataset_model_id.value = '';
		timeline_graph_section_dataset_model_id.parentElement.style.display = 'none';
		/**@type {HTMLInputElement}*/ (document.getElementById('timeline_graph_section_position_start')).value = '';
		/**@type {HTMLInputElement}*/ (document.getElementById('timeline_graph_section_position_stop')).value = '';
		document.getElementById('timeline_graph_section_position').style.display = 'none';
	}
	if([Config.Enums.TimelineSectionType.LINE, Config.Enums.TimelineSectionType.DOT, Config.Enums.TimelineSectionType.BAR].includes(section_type)) {
		//add scale to section
		if(!selected_timeline_graph_section.scale) {
			selected_timeline_graph_section.scale = new TimelineGraphSectionScale();
		}
		document.getElementById('timeline_graph_section_scale').querySelectorAll('input,select').forEach(i => i.removeAttribute('disabled'));
		document.getElementById('timeline_graph_section_scale').style.display = 'block';
	}
	else {
		delete selected_timeline_graph_section.scale;
		document.getElementById('timeline_graph_section_scale').querySelectorAll('input,select').forEach(i => i.setAttribute('disabled', 'disabled'));
		document.getElementById('timeline_graph_section_scale').style.display = 'none';
	}
	document.getElementById('timeline_graph_section_scope_paths').style.display = section_type === Config.Enums.TimelineSectionType.PERIOD ? 'block' : 'none';
	if(event) {
		manage_dataset_model();
	}
}

function manage_dataset_model(event) {
	const dataset_model_id = event ? this.value : selected_timeline_graph_section.datasetModelId;

	const timeline_graph_section_date_field_model_id = /**@type {HTMLSelectElement}*/ (document.getElementById('timeline_graph_section_date_field_model_id'));
	const timeline_graph_section_end_date_field_model_id = /**@type {HTMLSelectElement}*/ (document.getElementById('timeline_graph_section_end_date_field_model_id'));
	const timeline_graph_section_value_field_model_id = /**@type {HTMLSelectElement}*/ (document.getElementById('timeline_graph_section_value_field_model_id'));
	const timeline_graph_section_label_field_model_id = /**@type {HTMLSelectElement}*/ (document.getElementById('timeline_graph_section_label_field_model_id'));
	const timeline_graph_section_meta_field_model_ids = /**@type {AppPalette}*/ (document.getElementById('timeline_graph_section_meta_field_model_ids'));

	if(dataset_model_id) {
		const dataset_model = selected_timeline_graph_section.timelineGraph.study.getDatasetModel(dataset_model_id);

		FormHelpers.FillSelect(timeline_graph_section_date_field_model_id, dataset_model.fieldModels, true);
		document.getElementById('timeline_graph_section_date_field_model').style.display = 'block';

		FormHelpers.FillSelect(timeline_graph_section_end_date_field_model_id, dataset_model.fieldModels, true);
		document.getElementById('timeline_graph_section_end_date_field_model').style.display = 'block';

		FormHelpers.FillSelect(timeline_graph_section_value_field_model_id, dataset_model.fieldModels, true);
		document.getElementById('timeline_graph_section_value_field_model').style.display = 'block';

		FormHelpers.FillSelect(timeline_graph_section_label_field_model_id, dataset_model.fieldModels, true);
		document.getElementById('timeline_graph_section_label_field_model').style.display = 'block';

		FormHelpers.FillPalette(timeline_graph_section_meta_field_model_ids, dataset_model.fieldModels);
		document.getElementById('timeline_graph_section_meta_field_models').style.display = 'block';
	}
	else {
		timeline_graph_section_date_field_model_id.value = '';
		document.getElementById('timeline_graph_section_date_field_model').style.display = 'none';

		timeline_graph_section_end_date_field_model_id.value = '';
		document.getElementById('timeline_graph_section_end_date_field_model').style.display = 'none';

		timeline_graph_section_value_field_model_id.value = '';
		document.getElementById('timeline_graph_section_value_field_model').style.display = 'none';

		timeline_graph_section_label_field_model_id.value = '';
		document.getElementById('timeline_graph_section_label_field_model').style.display = 'none';

		timeline_graph_section_meta_field_model_ids.value = '';
		document.getElementById('timeline_graph_section_meta_field_models').style.display = 'none';
	}
}

function dragstart(event) {
	this.style.opacity = 0.6;
	event.dataTransfer.effectAllowed = 'copy';
	event.dataTransfer.setData('text/plain', this.textContent);
}

function dragend() {
	this.style.opacity = 1;
}

let selected_timeline_graph_section;

export default {
	form: 'edit_timeline_graph_section_form',
	init: function() {
		document.getElementById('edit_timeline_graph_section_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);

		document.getElementById('timeline_graph_section_type').addEventListener('change', manage_type);
		document.getElementById('timeline_graph_section_dataset_model_id').addEventListener('change', manage_dataset_model);

		document.getElementById('timeline_graph_section_tooltip_patterns').querySelectorAll('span').forEach(function(pattern) {
			pattern.addEventListener('dragstart', dragstart);
			pattern.addEventListener('dragend', dragend);
		});
	},
	open: function(timeline_graph_section) {
		selected_timeline_graph_section = timeline_graph_section;

		//enable or disable scale
		const scale_fields = document.getElementById('timeline_graph_section_scale').querySelectorAll('input,select');
		scale_fields.forEach(i => i.value = '');
		if(timeline_graph_section.scale) {
			scale_fields.forEach(i => i.removeAttribute('disabled'));
		}
		else {
			scale_fields.forEach(i => i.setAttribute('disabled', 'disabled'));
		}

		//add position to section
		if(!timeline_graph_section.position) {
			const position = new TimelineGraphSectionPosition();
			timeline_graph_section.position = position;
		}

		FormHelpers.FillSelectEnum(document.getElementById('timeline_graph_section_type'), Config.Enums.TimelineSectionType);
		FormHelpers.FillSelectEnum(document.getElementById('timeline_graph_section_mark'), Config.Enums.TimelineSectionMark);
		FormHelpers.FillSelectEnum(document.getElementById('timeline_graph_section_scale_position'), Config.Enums.TimelineSectionScalePosition);
		FormHelpers.FillPalette(document.getElementById('timeline_graph_section_event_model_ids'), timeline_graph_section.timelineGraph.study.getEventModels());
		FormHelpers.FillSelect(document.getElementById('timeline_graph_section_dataset_model_id'), timeline_graph_section.timelineGraph.study.datasetModels, true);
		manage_dataset_model();
		manage_type();
		FormHelpers.FillLocalizedInput(document.getElementById('timeline_graph_section_label'), timeline_graph_section.timelineGraph.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('timeline_graph_section_tooltip'), timeline_graph_section.timelineGraph.study.languages);
		FormHelpers.UpdateForm(document.getElementById('edit_timeline_graph_section_form'), timeline_graph_section);
	}
};
