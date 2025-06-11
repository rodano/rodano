import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';

export default {
	form: 'edit_timeline_graph_form',
	init: function() {
		document.getElementById('edit_timeline_graph_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);
	},
	open: function(timeline_graph) {
		FormHelpers.FillSelect(document.getElementById('timeline_graph_scope_model_id'), timeline_graph.study.scopeModels);
		FormHelpers.FillSelect(document.getElementById('timeline_graph_study_start_event_model_id'), timeline_graph.study.getEventModels(), true);
		FormHelpers.FillSelect(document.getElementById('timeline_graph_study_stop_event_model_id'), timeline_graph.study.getEventModels(), true);
		FormHelpers.FillLocalizedInput(document.getElementById('timeline_graph_shortname'), timeline_graph.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('timeline_graph_longname'), timeline_graph.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('timeline_graph_description'), timeline_graph.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('timeline_graph_foot_note'), timeline_graph.study.languages);
		FormHelpers.UpdateForm(document.getElementById('edit_timeline_graph_form'), timeline_graph);
	}
};
