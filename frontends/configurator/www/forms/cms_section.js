import {Config} from '../model_config.js';
import {FormHelpers} from '../form_helpers.js';
import {Router} from '../router.js';
import {ScopeCriterionRight} from '../model/config/entities/scope_criterion_right.js';
import {RightAssignables} from '../model/config/entities_categories.js';

function manage_right_entity(event) {
	const study = selected_section.layout.layoutable.getStudy();
	const entity_config_name = event ? /**@type {HTMLSelectElement}*/ (document.getElementById('cms_section_required_right_entity')).value : selected_section.requiredRight.rightEntity;
	let nodes;
	if(entity_config_name) {
		const entity = RightAssignables.find(e => e.configuration_name === entity_config_name);
		nodes = study.getChildren(entity);
	}
	else {
		nodes = [];
	}
	FormHelpers.FillSelect(document.getElementById('cms_section_required_right_id'), nodes, true);
}

let selected_section;

export default {
	form: 'edit_cms_section_form',
	init: function() {
		document.getElementById('cms_section_required_right_entity').addEventListener('change', manage_right_entity);

		document.getElementById('edit_cms_section_form').addEventListener(
			'submit',
			function(event) {
				event.stop();
				const section = Router.selectedOverlayNode;
				FormHelpers.UpdateObject(section, this);
				Router.SelectNode(section.layout);
			}
		);

		document.getElementById('edit_cms_section_close').addEventListener(
			'click',
			function() {
				Router.SelectNode(Router.selectedOverlayNode.layout);
			}
		);
	},
	open: function(section) {
		selected_section = section;

		//add required right to section
		if(!section.requiredRight) {
			section.requiredRight = new ScopeCriterionRight();
		}

		FormHelpers.FillSelect(document.getElementById('cms_section_required_feature'), section.layout.layoutable.getStudy().features, true);
		const right_assignables = /**@type {[string, string][]}*/ (RightAssignables.map(e => [e.configuration_name, e.label]));
		/**@type {HTMLSelectElement}*/ (document.getElementById('cms_section_required_right_entity')).fill(right_assignables, true, section.requiredRight.rightEntity);
		FormHelpers.FillSelectEnum(document.getElementById('cms_section_required_right_right'), Config.Enums.ProfileRightType, true);
		FormHelpers.FillLocalizedInput(document.getElementById('cms_section_labels'), section.layout.layoutable.getStudy().languages);
		FormHelpers.UpdateForm(document.getElementById('edit_cms_section_form'), section);

		manage_right_entity();
		/**@type {HTMLSelectElement}*/ (document.getElementById('cms_section_required_right_id')).value = section.requiredRight.id;
	}
};
