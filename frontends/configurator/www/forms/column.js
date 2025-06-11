import {FormHelpers} from '../form_helpers.js';
import {FormStaticActions} from '../form_static_actions.js';
import {Router} from '../router.js';

function close_form() {
	Router.SelectNode(selected_column.layout.formModel, 'edit_form_model_content');
}

let selected_column;

export default {
	form: 'edit_column_form',
	init: function() {
		document.getElementById('edit_column_form').addEventListener(
			'submit',
			function(event) {
				event.stop();
				FormHelpers.UpdateObject(selected_column, this);
				FormStaticActions.AfterSubmission(selected_column);
				close_form();
			}
		);

		document.getElementById('edit_column_close').addEventListener('click', close_form);
	},
	open: function(column) {
		selected_column = column;
		FormHelpers.UpdateForm(document.getElementById('edit_column_form'), column);
	}
};
