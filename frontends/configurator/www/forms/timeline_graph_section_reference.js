import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {TimelineGraphSectionReferenceEntry} from '../model/config/entities/timeline_graph_section_reference_entry.js';

function draw_entry(entry) {
	const instance = document.importNode(/**@type {HTMLTemplateElement}*/ (document.getElementById('timeline_graph_section_reference_entry')).content, true);

	/**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="timepoint"]')).value = entry.timepoint || '';
	/**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="value"]')).value = entry.value === undefined ? '' : entry.value;
	/**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="label"]')).value = entry.label || '';

	instance.querySelector('button').addEventListener(
		'click',
		function(event) {
			event.stop();
			const line = this.parentNode.parentNode;
			line.parentNode.removeChild(line);
		}
	);

	return instance;
}

let selected_timeline_graph_section_reference;

export default {
	form: 'edit_timeline_graph_section_reference_form',
	init: function() {
		document.getElementById('edit_timeline_graph_section_reference_form').addEventListener(
			'submit',
			function(event) {
				event.stop();
				FormHelpers.UpdateObject(selected_timeline_graph_section_reference, this);
				selected_timeline_graph_section_reference.entries = document.querySelectorAll('#timeline_graph_section_reference_entries > tbody > tr').map(function(reference_entry) {
					const entry = new TimelineGraphSectionReferenceEntry();
					entry.timepoint = reference_entry.querySelector('input[data-name="timepoint"]').value;
					const entry_value = reference_entry.querySelector('input[data-name="value"]').value;
					entry.value = entry_value ? parseFloat(entry_value) : undefined;
					entry.label = reference_entry.querySelector('input[data-name="label"]').value;
					entry.reference = selected_timeline_graph_section_reference;
					return entry;
				});
				FormStaticActions.AfterSubmission(selected_timeline_graph_section_reference);
			}
		);

		document.getElementById('timeline_graph_section_reference_entries_add').addEventListener(
			'click',
			function(event) {
				event.stop();
				document.querySelector('#timeline_graph_section_reference_entries > tbody').appendChild(draw_entry(new TimelineGraphSectionReferenceEntry()));
			}
		);
	},
	open: function(timeline_graph_section_reference) {
		selected_timeline_graph_section_reference = timeline_graph_section_reference;

		FormHelpers.FillSelect(document.getElementById('timeline_graph_section_reference_reference_section_id'), timeline_graph_section_reference.section.timelineGraph.sections, false, undefined, {value_property: 'id', label_property: 'label'});
		FormHelpers.FillLocalizedInput(document.getElementById('timeline_graph_section_reference_label'), timeline_graph_section_reference.section.timelineGraph.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('timeline_graph_section_reference_tooltip'), timeline_graph_section_reference.section.timelineGraph.study.languages);
		FormHelpers.UpdateForm(document.getElementById('edit_timeline_graph_section_reference_form'), timeline_graph_section_reference);
		timeline_graph_section_reference.entries.map(draw_entry).forEach(Node.prototype.appendChild, document.querySelector('#timeline_graph_section_reference_entries > tbody').empty('tr'));
	}
};
