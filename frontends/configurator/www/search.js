import {Config} from './model_config.js';
import {StudyTree} from './study_tree.js';
import {Router} from './router.js';
import {FormHelpers} from './form_helpers.js';
import {Entities} from './model/config/entities.js';

export const Search = {
	Init: function() {
		let state;
		const search_dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('search'));
		const search_form = search_dialog.querySelector('form');

		document.getElementById('tree_advanced_search').addEventListener(
			'click',
			function(event) {
				event.stopPropagation();
				state = StudyTree.GetTree().save(state);
				search_dialog.showModal();
			}
		);

		document.getElementById('search_cancel').addEventListener(
			'click',
			function() {
				search_dialog.close();
			}
		);

		document.getElementById('search_reset').addEventListener(
			'click',
			function() {
				reset_all();
				StudyTree.Filter();
				if(state) {
					StudyTree.GetTree().restore(state);
				}
				search_dialog.close();
			}
		);

		function reset_all() {
			//reset property, operator and value
			document.getElementById('search_property').style.display = 'none';
			search_form['property'].value = '';
			document.getElementById('search_operator').style.display = 'none';
			search_form['operator'].value = '';
			document.getElementById('search_value').style.display = 'none';
			document.getElementById('search_value').querySelector('input,select')?.remove();
		}

		function draw_value(property) {
			const search_value = document.getElementById('search_value');
			//build new value
			let value_input;
			switch(property[1].type) {
				case 'boolean':
					value_input = document.createFullElement('input', {type: 'checkbox', name: 'value'});
					break;
				case 'number':
					value_input = document.createFullElement('input', {type: 'number', name: 'value', required: 'required'});
					break;
				default:
					value_input = document.createFullElement('input', {name: 'value', required: 'required'});
			}
			search_value.appendChild(value_input);
			search_value.style.display = '';
		}

		const entities = Object.values(Entities)
			.filter(e => e.id)
			.map(e => [e.name, e.label])
			.sort((e1, e2) => e1[1].compareTo(e2[1]));
		search_form['entity'].fill(entities, true);
		search_form['entity'].addEventListener(
			'change',
			function() {
				reset_all();
				if(this.value) {
					const properties = Object.entries(Config.Entities[this.value].getProperties())
						.filter(e => !e[1].back_reference && !['array', 'object'].includes(e[1].type))
						.map(e => e[0]);
					properties.sort();
					search_form['property'].fill(properties, true);
					document.getElementById('search_property').style.display = '';
				}
			}
		);
		search_form['property'].addEventListener(
			'change',
			function() {
				//reset operator and value
				document.getElementById('search_operator').style.display = 'none';
				search_form['operator'].value = '';
				document.getElementById('search_value').style.display = 'none';
				document.getElementById('search_value').querySelector('input,select')?.remove();
				if(this.value) {
					//retrieve property
					const property = Object.entries(Config.Entities[search_form['entity'].value].getProperties()).find(e => e[0] === this.value);
					if(property[1].type === 'boolean') {
						draw_value(property);
						search_form['operator'].removeAttribute('required');
					}
					else {
						search_form['operator'].setAttribute('required', 'required');
						const operators = Config.Enums.NativeType[property[1].type].operators;
						const entries = Object.fromEntries(operators.map(o => [o.name, o]));
						//update ui
						FormHelpers.FillSelectEnum(search_form['operator'], entries, true);
						document.getElementById('search_operator').style.display = '';
					}
				}
			}
		);
		search_form['operator'].addEventListener(
			'change',
			function() {
				//reset value
				document.getElementById('search_value').style.display = 'none';
				document.getElementById('search_value').querySelector('input,select')?.remove();
				if(this.value) {
					if(Config.Enums.Operator[this.value].has_value) {
						//retrieve property
						const property = Object.entries(Config.Entities[search_form['entity'].value].getProperties()).find(e => e[0] === search_form['property'].value);
						draw_value(property);
					}
				}
			}
		);
		search_form.addEventListener(
			'submit',
			function(event) {
				event.preventDefault();
				//retrieve parameters
				const entity_name = search_form['entity'].value;
				const entity = Config.Entities[entity_name];
				const property_name = search_form['property'].value;
				const property = Object.entries(entity.getProperties()).find(e => e[0] === property_name);
				let operator = undefined;
				let value = undefined;
				if(property[1].type === 'boolean') {
					operator = Config.Enums.Operator.EQUALS;
					value = search_form['value'].checked;
				}
				else {
					operator = Config.Enums.Operator[search_form['operator'].value];
					if(operator.has_value) {
						switch(property[1].type) {
							case 'number':
								value = Number.parseFloat(search_form['value'].value);
								break;
							default:
								//search will be case insensitive so take only lower case value
								value = search_form['value'].value.toLowerCase();
								value = value === '' ? undefined : value;
						}
					}
				}
				//filter tree
				StudyTree.Filter(node => {
					if(node.constructor !== entity) {
						return false;
					}
					let node_value = node[property_name];
					if(String.isString(node_value)) {
						node_value = node_value.toLowerCase();
					}
					return operator.test(node_value, value);
				});
				//deselect selected node if it's not in the filtered tree
				//this happens if selected does not match the filter or is not an ancestor of a node that matches the filter
				if(!StudyTree.GetSelection()?.isDisplayed()) {
					Router.SelectHelp();
				}
				search_dialog.close();
			}
		);
	},
	Focus: function() {
		document.getElementById('tree_search')['search'].focus();
	}
};
