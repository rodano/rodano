import '../basic-tools/extension.js';

import {Config} from '../model_config.js';
import {FormHelpers} from '../form_helpers.js';
import {Router} from '../router.js';
import {ScopeCriterionRight} from '../model/config/entities/scope_criterion_right.js';
import {RightAssignables} from '../model/config/entities_categories.js';

function manage_right_entity() {
	const study = selected_widget.section.layout.layoutable.getStudy();
	const entity_config_name = /**@type {HTMLSelectElement}*/ (document.getElementById('cms_widget_required_right_entity')).value;
	let nodes;
	if(entity_config_name) {
		const entity = RightAssignables.find(e => e.configuration_name === entity_config_name);
		nodes = study.getChildren(entity);
	}
	else {
		nodes = [];
	}
	FormHelpers.FillSelect(document.getElementById('cms_widget_required_right_id'), nodes, true);
}

let selected_widget;

export default {
	form: 'edit_cms_widget_form',
	init: function() {
		document.getElementById('cms_widget_required_right_entity').addEventListener('change', manage_right_entity);

		document.getElementById('edit_cms_widget_form').addEventListener(
			'submit',
			function(event) {
				event.stop();
				const widget = Router.selectedOverlayNode;
				FormHelpers.UpdateObject(widget, this);
				Router.SelectNode(widget.section.layout);
			}
		);

		document.getElementById('edit_cms_widget_close').addEventListener(
			'click',
			function() {
				Router.SelectNode(Router.selectedOverlayNode.section.layout);
			}
		);
	},
	open: function(widget) {
		selected_widget = widget;

		//add required right to widget
		if(!widget.requiredRight) {
			widget.requiredRight = new ScopeCriterionRight();
		}

		FormHelpers.FillSelect(document.getElementById('cms_widget_required_feature'), widget.section.layout.layoutable.getStudy().features, true);
		const right_assignables = /**@type {[string, string][]}*/ (RightAssignables.map(e => [e.configuration_name, e.label]));
		/**@type {HTMLSelectElement}*/ (document.getElementById('cms_widget_required_right_entity')).fill(right_assignables, true, widget.requiredRight.rightEntity);
		FormHelpers.FillSelectEnum(document.getElementById('cms_widget_required_right_right'), Config.Enums.ProfileRightType, true);

		FormHelpers.UpdateForm(document.getElementById('edit_cms_widget_form'), widget);
		//FormHelpers.EnhanceInputLocalized(document.getElementById('widget_text_before'));
		//FormHelpers.EnhanceInputLocalized(document.getElementById('widget_text_after'));
		manage_right_entity();
		/**@type {HTMLSelectElement}*/ (document.getElementById('cms_widget_required_right_id')).value = widget.requiredRight.id;
	}
};
