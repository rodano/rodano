import {Wizards} from '../wizards.js';
import {Languages} from '../languages.js';
import {Config} from '../model_config.js';
import {bus} from '../model/config/entities_hooks.js';
import {StudyHandler} from '../study_handler.js';
import {Router} from '../router.js';
import {UI} from '../tools/ui.js';
import {Entities} from '../model/config/entities.js';
import {RuleCondition} from '../model/config/entities/rule_condition.js';
import {RuleConditionCriterion} from '../model/config/entities/rule_condition_criterion.js';
import {RuleConditionList} from '../model/config/entities/rule_condition_list.js';
import {RuleConstraint} from '../model/config/entities/rule_constraint.js';
import {Validator} from '../model/config/entities/validator.js';
import {MediaTypes} from '../media_types.js';
import {FormHelpers} from '../form_helpers.js';

function clean_range_input(input) {
	input.value = '';
	input.dataset.nodeId = '';
	input.style.display = 'inline';
	const label = input.nextElementSibling;
	label.textContent = '';
}

let validator;

function generate_rules_for_dependency(validated_field_model, dependency_field_model, side) {
	const dataset_model = validated_field_model.datasetModel;
	const dependency_dataset_model = dependency_field_model.datasetModel;
	//keep a hook on root container
	//create constraint
	const constraint = new RuleConstraint();
	//keep a hook on container (condition list or condition)
	let parent_container;
	//check if field models are on same dataset model
	if(dataset_model !== dependency_dataset_model) {
		//in this case, requirements on dataset models are:
		//either both on the same scope models
		//either both on the same event
		//either the dataset model is attached to an event that is linked to a scope models which is linked to dependency dataset model
		const event_models = dataset_model.getEventModels();
		const scope_models = [...dataset_model.getScopeModels(), ...event_models.map(e => e.scopeModel)];
		const dependency_event_models = dependency_dataset_model.getEventModels();
		const dependency_scope_models = dependency_dataset_model.getScopeModels();
		//check if both dataset models are on the same scope model or the same event
		let root_condition = undefined;
		if(scope_models.includesOne(dependency_scope_models)) {
			root_condition = 'SCOPE';
		}
		else if(event_models.includesOne(dependency_event_models)) {
			root_condition = 'EVENT';
		}
		else {
			throw new Error('Unable to use an field model dependency that do not share an event or a scope model with the selected field model.');
		}
		parent_container = new RuleConditionList();
		parent_container.constraint = constraint;
		parent_container.mode = 'AND';
		constraint.conditions[root_condition] = parent_container;

		//add datasets condition
		const datasets_condition = new RuleCondition({id: `DATASETS_${side}`});
		datasets_condition.parent = parent_container;
		parent_container.conditions.push(datasets_condition);

		//add datasets condition criterion
		const datasets_condition_criterion = new RuleConditionCriterion({property: 'DATASET'});
		datasets_condition_criterion.condition = datasets_condition;
		datasets_condition.criterion = datasets_condition_criterion;

		//add dataset condition
		const dataset_condition = new RuleCondition({id: `DATASET_${side}`});
		dataset_condition.parent = datasets_condition;
		datasets_condition.conditions.push(dataset_condition);

		//add datasets condition criterion
		const dataset_condition_criterion = new RuleConditionCriterion({property: 'ID', operator: 'EQUALS', values: [dependency_dataset_model.id]});
		dataset_condition_criterion.condition = dataset_condition;
		dataset_condition.criterion = dataset_condition_criterion;

		parent_container = dataset_condition;
	}
	else {
		parent_container = new RuleConditionList();
		parent_container.constraint = constraint;
		parent_container.mode = 'AND';
		constraint.conditions['DATASET'] = parent_container;
	}

	//add values condition
	const values_condition = new RuleCondition({id: `FIELDS_${side}`});
	values_condition.parent = parent_container;
	parent_container.conditions.push(values_condition);

	//add values condition criterion
	const values_condition_criterion = new RuleConditionCriterion({property: 'FIELD'});
	values_condition_criterion.condition = values_condition;
	values_condition.criterion = values_condition_criterion;

	//add value condition
	const value_condition = new RuleCondition({id: `FIELD_${side}`});
	value_condition.parent = values_condition;
	values_condition.conditions.push(value_condition);

	//add value condition criterion
	const value_condition_criterion = new RuleConditionCriterion({property: 'ID', operator: 'EQUALS', values: [dependency_field_model.id]});
	value_condition_criterion.condition = value_condition;
	value_condition.criterion = value_condition_criterion;

	return constraint;
}

function retrieve_range_type(input) {
	if(input.dataset.nodeId) {
		return StudyHandler.GetStudy().getNode(input.dataset.nodeId).dataType;
	}
	if(input.value) {
		if(Number.isNumber(input.value)) {
			return 'NUMBER';
		}
		if(Date.parseToDisplay(input.value)) {
			return 'DATE';
		}
	}
	throw new Error('Field model cannot be retrieved or value cannot be parsed');
}

function filter_field_models(dataset_model) {
	return dataset_model.fieldModels.filter(f => [Config.Enums.DataType.NUMBER.name, Config.Enums.DataType.DATE.name].includes(f.dataType));
}

function manage_dataset_model() {
	const study = StudyHandler.GetStudy();
	const dataset_model_id = /**@type {HTMLInputElement}*/ (document.getElementById('wizard_validator_dataset_model_id')).value;
	const wizard_validator_field_model_id = /**@type {HTMLSelectElement}*/ (document.getElementById('wizard_validator_field_model_id'));
	if(dataset_model_id) {
		const dataset_model = study.getDatasetModel(dataset_model_id);

		FormHelpers.FillSelect(wizard_validator_field_model_id, filter_field_models(dataset_model), true);
		wizard_validator_field_model_id.parentElement.style.display = 'block';
	}
	else {
		wizard_validator_field_model_id.parentElement.style.display = 'none';
	}
}

Wizards.Register('validator', {
	title: 'New validator',
	description: 'This wizard will help you to create a validator.',
	steps: 4,
	mode: Wizards.Mode.ASIDE,
	labels: {
		'3': 'Create validator',
		'4': 'Close'
	},
	no_return: 4,
	init: function() {
		const study = StudyHandler.GetStudy();
		const wizard_validator_dataset_model_id = document.getElementById('wizard_validator_dataset_model_id');
		FormHelpers.FillSelect(wizard_validator_dataset_model_id, study.datasetModels.filter(d => !filter_field_models(d).isEmpty()), true);

		wizard_validator_dataset_model_id.addEventListener('change', manage_dataset_model);
		manage_dataset_model();

		document.getElementById('wizard_validator')['wizard_validator_range_strict'].addEventListener(
			'change',
			function() {
				const wizard_validator_range_min_operator = document.getElementById('wizard_validator_range_min_operator');
				const wizard_validator_range_max_operator = document.getElementById('wizard_validator_range_max_operator');
				const operator_symbol = this.checked ? '<' : '≤';
				wizard_validator_range_min_operator.textContent = operator_symbol;
				wizard_validator_range_max_operator.textContent = operator_symbol;
			}
		);

		function check_valid_node(dragged_node) {
			return dragged_node.getEntity() === Entities.FieldModel && ['NUMBER', 'DATE'].includes(dragged_node.dataType);
		}

		function dragover(event) {
			//check drop availability in some cases
			if(event.dataTransfer.types.includes(MediaTypes.NODE_GLOBAL_ID)) {
				let allow_drop = false;
				//BUG some browsers do not allow to check what is dragged during drag https://bugs.webkit.org/show_bug.cgi?id=58206 or http://code.google.com/p/chromium/issues/detail?id=50009
				//check if drop is possible only on browsers that allow sniffing data
				if(event.dataTransfer.getData(MediaTypes.NODE_GLOBAL_ID)) {
					try {
						const dragged_node = StudyHandler.GetStudy().getNode(event.dataTransfer.getData(MediaTypes.NODE_GLOBAL_ID));
						//check that drop is possible
						if(dragged_node && check_valid_node(dragged_node)) {
							allow_drop = true;
						}
					}
					catch(exception) {
						console.log(exception);
						//node comes from an other config, no way to check if drop is possible
					}
				}
				//SHORTCUT allow drop for browsers that don't allow sniffing data
				else {
					console.warn('Unable to sniff data, allowing item drop');
					allow_drop = true;
				}
				//ENDSHORTCUT
				if(allow_drop) {
					event.preventDefault();
					this.classList.add('dragover');
				}
			}
		}

		function dragleave() {
			this.classList.remove('dragover');
		}

		/**@this {HTMLInputElement}*/
		function drop(event) {
			event.preventDefault();
			this.classList.remove('dragover');
			//dropping a node global id
			if(event.dataTransfer.types.includes(MediaTypes.NODE_GLOBAL_ID)) {
				try {
					//retrieve dragged node
					const dragged_node = StudyHandler.GetStudy().getNode(event.dataTransfer.getData(MediaTypes.NODE_GLOBAL_ID));
					//check again that drop is possible because of webkit based browser which would have allowed a drop whereas it should not have been
					if(dragged_node && check_valid_node(dragged_node)) {
						this.value = '';
						this.dataset.nodeId = event.dataTransfer.getData(MediaTypes.NODE_GLOBAL_ID);
						this.style.display = 'none';
						this.nextElementSibling.textContent = dragged_node.getLocalizedLabel(Languages.GetLanguage());
					}
				}
				catch {
					//node comes from an other config
				}
			}
		}

		const wizard_validator_range_min = document.getElementById('wizard_validator')['wizard_validator_range_min'];
		const wizard_validator_range_max = document.getElementById('wizard_validator')['wizard_validator_range_max'];

		wizard_validator_range_min.addEventListener('dragenter', dragover);
		wizard_validator_range_min.addEventListener('dragover', dragover);
		wizard_validator_range_min.addEventListener('dragleave', dragleave);
		wizard_validator_range_min.addEventListener('drop', drop);

		wizard_validator_range_max.addEventListener('dragenter', dragover);
		wizard_validator_range_max.addEventListener('dragover', dragover);
		wizard_validator_range_max.addEventListener('dragleave', dragleave);
		wizard_validator_range_max.addEventListener('drop', drop);

		function reset_listener() {
			clean_range_input(this.previousElementSibling.previousElementSibling);
		}

		document.getElementById('wizard_validator_range_min_reset').addEventListener('click', reset_listener);
		document.getElementById('wizard_validator_range_max_reset').addEventListener('click', reset_listener);
	},
	onStart: function() {
		validator = undefined;
		//reset fields
		/**@type {HTMLInputElement}*/ (document.getElementById('wizard_validator_dataset_model_id')).value = '';
		/**@type {HTMLInputElement}*/ (document.getElementById('wizard_validator_field_model_id')).value = '';
		manage_dataset_model();
		clean_range_input(document.getElementById('wizard_validator')['wizard_validator_range_min']);
		clean_range_input(document.getElementById('wizard_validator')['wizard_validator_range_max']);
	},
	onCancel: function() {
		if(validator) {
			validator['delete']();
			validator = undefined;
		}
	},
	onValidate: function(step) {
		switch(step) {
			case 1: {
				const dataset_model_id = /**@type {HTMLInputElement}*/ (document.getElementById('wizard_validator_dataset_model_id')).value;
				const field_model_id = /**@type {HTMLInputElement}*/ (document.getElementById('wizard_validator_field_model_id')).value;
				if(!dataset_model_id || !field_model_id) {
					document.getElementById('wizard_error').textContent = 'A dataset model and a field model are required';
					document.getElementById('wizard_error').style.display = 'block';
					return false;
				}
				break;
			}
			case 3: {
				const type = document.getElementById('wizard_validator')['wizard_validator_type'].value;
				if(type === 'range') {
					const min = document.getElementById('wizard_validator')['wizard_validator_range_min'];
					const max = document.getElementById('wizard_validator')['wizard_validator_range_max'];

					const min_value = min.value;
					const max_value = max.value;

					//at least one value must be set
					if(!min_value && !max_value && !min.dataset.nodeId && !max.dataset.nodeId) {
						document.getElementById('wizard_error').textContent = 'A min or a max value is required';
						document.getElementById('wizard_error').style.display = 'block';
						return false;
					}

					//retrieve min and max value type
					let min_value_type;
					if(min_value) {
						try {
							min_value_type = retrieve_range_type(min);
						}
						catch(exception) {
							console.log(exception);
							document.getElementById('wizard_error').textContent = 'Min value must be a number or a date using format dd.mm.yyyy';
							document.getElementById('wizard_error').style.display = 'block';
							return false;
						}
					}
					let max_value_type;
					if(max_value) {
						try {
							max_value_type = retrieve_range_type(max);
						}
						catch(exception) {
							console.log(exception);
							document.getElementById('wizard_error').textContent = 'Max value must be a number or a date using format dd.mm.yyyy';
							document.getElementById('wizard_error').style.display = 'block';
							return false;
						}
					}

					//check consistency between type of min and max values
					if(min_value_type && max_value_type && min_value_type !== max_value_type) {
						document.getElementById('wizard_error').textContent = 'Min and max values must be both dates or both numbers';
						document.getElementById('wizard_error').style.display = 'block';
						return false;
					}

					//in case hard-coded are set, check consistency between min and max values
					if(min_value && max_value) {
						const error = min_value_type === 'NUMBER' && (parseFloat(min_value) >= parseFloat(max_value)) || Date.parseToDisplay(min_value).isAfter(Date.parseToDisplay(max_value));
						if(error) {
							document.getElementById('wizard_error').textContent = 'Max value must be greater than min value';
							document.getElementById('wizard_error').style.display = 'block';
							return false;
						}
					}
				}
				break;
			}
		}
		return true;
	},
	onNext: function(step) {
		const study = StudyHandler.GetStudy();
		switch(step) {
			case 1: {
				//find suitable validator id
				let node_id = 'NEW_VALIDATOR';
				let i = 2;
				while(study.getHasChild(Entities.Validator, undefined, node_id)) {
					node_id = `${node_id}_${i}`;
					i++;
				}
				validator = new Validator({id: node_id});

				const dataset_model_id = /**@type {HTMLInputElement}*/ (document.getElementById('wizard_validator_dataset_model_id')).value;
				const field_model_id = /**@type {HTMLInputElement}*/ (document.getElementById('wizard_validator_field_model_id')).value;
				const field_model = study.getDatasetModel(dataset_model_id).getFieldModel(field_model_id);
				field_model.validatorIds.push(node_id);
				break;
			}
			case 2: {
				//manage ui from step 2
				const type = document.getElementById('wizard_validator')['wizard_validator_type'].value;
				if(type === 'range') {
					document.getElementById('wizard_validator_range').style.display = 'block';
					document.getElementById('wizard_validator_comparator').style.display = 'none';
				}
				else {
					document.getElementById('wizard_validator_range').style.display = 'none';
					document.getElementById('wizard_validator_comparator').style.display = 'block';
				}
				break;
			}
			case 3: {
				const dataset_model_id = /**@type {HTMLInputElement}*/ (document.getElementById('wizard_validator_dataset_model_id')).value;
				const field_model_id = /**@type {HTMLInputElement}*/ (document.getElementById('wizard_validator_field_model_id')).value;
				const field_model = study.getDatasetModel(dataset_model_id).getFieldModel(field_model_id);

				const min = document.getElementById('wizard_validator')['wizard_validator_range_min'];
				const max = document.getElementById('wizard_validator')['wizard_validator_range_max'];

				const min_value = min.value;
				const max_value = max.value;

				const min_set = min_value || min.dataset.nodeId;
				const max_set = max_value || max.dataset.nodeId;

				let validator_type;
				let min_value_message, max_value_message;

				bus.disable();

				const constraint = new RuleConstraint({
					constrainable: validator
				});
				validator.constraint = constraint;

				//retrieve dependencies if required
				if(min.dataset.nodeId) {
					const min_field_model = study.getNode(min.dataset.nodeId);
					validator_type = min_field_model.dataType;
					min_value_message = min_field_model.getLocalizedLabel(Languages.GetLanguage());
					try {
						//generate and merge constraint for retrieval of dependencies
						constraint.merge(generate_rules_for_dependency(field_model, min_field_model, 'MIN'), true);
					}
					catch(error) {
						UI.Notify(error.message, {tag: 'error', icon: 'images/notifications_icons/warning.svg'});
					}
				}
				else {
					validator_type = Number.isNumber(min_value) ? 'NUMBER' : 'DATE';
					min_value_message = min_value;
				}
				if(max.dataset.nodeId) {
					const max_field_model = study.getNode(max.dataset.nodeId);
					validator_type = max_field_model.dataType;
					max_value_message = max_field_model.getLocalizedLabel(Languages.GetLanguage());
					try {
						//generate and merge constraint for retrieval of dependencies
						constraint.merge(generate_rules_for_dependency(field_model, max_field_model, 'MAX'), true);
					}
					catch(error) {
						UI.Notify(error.message, {tag: 'error', icon: 'images/notifications_icons/warning.svg'});
					}
				}
				else {
					validator_type = Number.isNumber(max_value) ? 'NUMBER' : 'DATE';
					max_value_message = max_value;
				}

				//generate condition list
				const condition_list = new RuleConditionList();
				condition_list.constraint = constraint;
				condition_list.mode = 'AND';
				constraint.conditions['FIELD'] = condition_list;

				//manage comparator type and operator strict mode
				const criterion_property = `VALUE_${validator_type}`;
				const criterion_operator_suffix = document.getElementById('wizard_validator')['wizard_validator_range_strict'].checked ? '' : '_EQUALS';

				//exclude blank value
				const not_blank_condition = new RuleCondition({id: 'NOT_BLANK', breakType: 'ALLOW'});
				not_blank_condition.parent = condition_list;
				condition_list.conditions.push(not_blank_condition);
				const not_blank_condition_criterion = new RuleConditionCriterion({property: 'VALUE', operator: 'NOT_BLANK'});
				not_blank_condition_criterion.condition = not_blank_condition;
				not_blank_condition.criterion = not_blank_condition_criterion;

				//generate min condition
				if(min_set) {
					const min_condition = new RuleCondition({id: 'MIN'});
					min_condition.parent = condition_list;
					condition_list.conditions.push(min_condition);

					const min_condition_criterion = new RuleConditionCriterion({property: criterion_property, operator: `GREATER${criterion_operator_suffix}`});
					if(min.dataset.nodeId) {
						min_condition_criterion.values.push(`=FIELD_MIN:${criterion_property}`);
					}
					else {
						min_condition_criterion.values.push(min_value);
					}
					min_condition_criterion.condition = min_condition;
					min_condition.criterion = min_condition_criterion;
				}

				//generate max condition
				if(max_set) {
					const max_condition = new RuleCondition({id: 'MAX'});
					max_condition.id = 'MAX';
					max_condition.parent = condition_list;
					condition_list.conditions.push(max_condition);

					const max_condition_criterion = new RuleConditionCriterion({property: criterion_property, operator: `LOWER${criterion_operator_suffix}`});
					if(max.dataset.nodeId) {
						max_condition_criterion.values.push(`=FIELD_MAX:${criterion_property}`);
					}
					else {
						max_condition_criterion.values.push(max_value);
					}
					max_condition_criterion.condition = max_condition;
					max_condition.criterion = max_condition_criterion;
				}

				//set message according to min value and max value
				if(min_set && max_set) {
					validator.message = {
						en: `Value must be between ${min_value_message} and ${max_value_message}`,
						fr: `La valeur doit être comprise entre ${min_value_message} et ${max_value_message}`
					};
				}
				else if(min_set) {
					validator.message = {
						en: `Value must be greater than ${min_value_message}`,
						fr: `La valeur doit être plus grande que ${min_value_message}`
					};
				}
				else {
					validator.message = {
						en: `Value must be lower than ${max_value_message}`,
						fr: `La valeur doit être plus petite que ${max_value_message}`
					};
				}
				validator.shortname = validator.message;

				bus.enable();

				study.addChild(validator);
				Router.SelectNode(validator);
				break;
			}
		}
	}
});
