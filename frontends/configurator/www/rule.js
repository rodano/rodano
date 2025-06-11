import './basic-tools/extension.js';

import {Forms} from './tools/forms.js';
import {UI} from './tools/ui.js';
import {Effects} from './tools/effects.js';
import {Config} from './model_config.js';
import {bus} from './model/config/entities_hooks.js';
import {Languages} from './languages.js';
import {bus_ui} from './bus_ui.js';
import {FormHelpers} from './form_helpers.js';
import {ConstraintHelpers} from './constraint_helpers.js';
import {RuleCondition} from './model/config/entities/rule_condition.js';
import {RuleConditionCriterion} from './model/config/entities/rule_condition_criterion.js';
import {RuleActionParameter} from './model/config/entities/rule_action_parameter.js';
import {RuleAction} from './model/config/entities/rule_action.js';
import {RuleEvaluation} from './model/config/entities/rule_evaluation.js';
import {Entities} from './model/config/entities.js';
import {MediaTypes} from './media_types.js';
import {FormulasHelp} from './formulas_help.js';
import {RuleConditionList} from './model/config/entities/rule_condition_list.js';

function generate_options(condition_id, properties) {
	return properties.
		filter(p => p.hasOwnProperty('type')).
		map(p => `=${condition_id}:${p.id}`);
}

function generate_datalist_options() {
	return [
		//add first level conditions
		...selected_constraint.getEntities().map(e => generate_options(e.name, selected_study.getAllRuleDefinitionProperties(e))),
		//add other conditions
		...selected_constraint.getAllConditions().map(c => generate_options(c.id, selected_study.getAllRuleDefinitionProperties(c.getRuleEntity())))
	];
}

function refresh_datalist(datalist, options) {
	const datalist_options = options || generate_datalist_options();
	datalist.fill(datalist_options);
}

function refresh_condition_selects() {
	//update action selects
	const entities_conditions_ids = {};
	document.getElementById('rule').querySelectorAll('select[class="rule_condition"]').forEach(function(element) {
		//retrieve good condition ids and cache them
		let conditions_ids;
		if(element.entity) {
			//cache condition ids for the current entity
			if(!entities_conditions_ids.hasOwnProperty(element.entity.name)) {
				entities_conditions_ids[element.entity.name] = selected_constraint.getAllConditionsIdsForEntity(element.entity);
			}
			conditions_ids = entities_conditions_ids[element.entity.name];
		}
		else {
			conditions_ids = selected_constraint.getAllConditionsIds();
		}
		const blank_option = !element.firstElementChild.value;
		element.fill(conditions_ids, blank_option, element.value);
	});
	//update condition formula
	const all_options = generate_datalist_options();
	document.getElementById('rule').querySelectorAll('datalist[class="datalist_condition"]').forEach(function(element) {
		refresh_datalist(element, all_options);
	});
}

function add_condition() {
	const entity = this.parentNode.parentNode.entity;
	let condition_list = selected_conditions[entity.name];
	//create conditions list it if it does not exist
	if(!condition_list) {
		//create conditions
		condition_list = new RuleConditionList();
		condition_list.constraint = selected_constraint;
		condition_list.mode = 'OR';
		condition_list.conditions = [];
		selected_conditions[entity.name] = condition_list;
	}
	//condition
	const condition_id = condition_list.generateChildId(Object.values(Config.Enums.RuleEntities).indexOf(entity) + 1);
	const condition = new RuleCondition({id: condition_id});
	//add condition to conditions list
	condition.parent = condition_list;
	condition_list.conditions.push(condition);
	//criterion
	const criterion = new RuleConditionCriterion();
	criterion.condition = condition;
	condition.criterion = criterion;
	//update ui
	this.parentNode.nextElementSibling.appendChild(draw_condition(entity, condition));
	refresh_condition_selects();
}

function update_condition(source_entity, condition) {
	update_condition_from_container(source_entity, condition, document.getElementById(condition.id));
	const property = selected_study.getAllRuleDefinitionProperty(source_entity, condition.criterion.property);
	if(property.target) {
		const target = Config.Enums.RuleEntities[property.target];
		condition.conditions.forEach(c => update_condition(target, c));
	}
}

function update_condition_from_container(entity, condition, container) {
	const criterion_container = container.firstElementChild;
	condition.inverse = container.querySelector('input[name="inverse"]').checked;
	condition.dependency = container.querySelector('input[name="dependency"]').checked;
	condition.criterion.property = criterion_container.querySelector('select[name="property"]').value;
	if(selected_study.getAllRuleDefinitionProperty(entity, condition.criterion.property).type) {
		condition.criterion.operator = criterion_container.querySelector('select[name="operator"]').value;
		condition.criterion.values = criterion_container.querySelectorAll('[name="value"]').map(e => e.value);
		/*var condition_id_select = criterion_container.querySelector('select[name="condition_id"]');
		if(condition_id_select) {
			condition.criterion.conditionId = condition_id_select.value;
		}*/
	}
	else {
		condition.criterion.operator = undefined;
		condition.criterion.values = [];
	}
}

function update_evaluation_from_container(evaluation, container) {
	evaluation.conditionId = container.querySelector('select[name="condition"]').value;
	evaluation.property = container.querySelector('select[name="property"]').value;
	evaluation.operator = container.querySelector('select[name="operator"]').value;
	evaluation.values = container.querySelectorAll('[name="value"]').map(e => e.value);
}

function update_action_from_container(action, container) {
	const action_properties = container.children[0];
	const action_container = container.children[1];
	const parameters_container = container.children[2];

	//update properties
	action.optional = action_properties.children[0].querySelector('input[name="action_optional"]').checked;
	action.id = action_properties.children[1].querySelector('input[name="action_id"]').value || '';
	action.label = JSON.parse(action_properties.children[2].querySelector('app-localized-input[name="action_label"]').value);

	//update action
	const type = action_container.querySelector('select[name="type"]').value;
	if(type === 'STATIC_ACTION') {
		action.configurationWorkflowId = undefined;
		action.configurationActionId = undefined;
		action.staticActionId = action_container.querySelector('select[name="static_action"]').value;
		action.conditionId = undefined;
		action.actionId = undefined;
	}
	else if(type === 'CONFIGURATION_ACTION') {
		action.configurationWorkflowId = action_container.querySelector('select[name="workflow"]').value;
		action.configurationActionId = action_container.querySelector('select[name="workflow_action"]').value;
		action.staticActionId = undefined;
		action.conditionId = undefined;
		action.actionId = undefined;
	}
	else {
		action.configurationWorkflowId = undefined;
		action.configurationActionId = undefined;
		action.staticActionId = undefined;
		//condition can be a root entity
		const condition_id = action_container.querySelector('select[name="condition"]').value;
		if(Config.Enums.RuleEntities.hasOwnProperty(condition_id)) {
			action.rulableEntity = condition_id;
			action.conditionId = undefined;
		}
		//or a specific one depending on condition
		else {
			action.conditionId = condition_id;
			action.rulableEntity = undefined;
		}
		action.actionId = action_container.querySelector('select[name="condition_action"]').value;
	}

	//update action parameters
	for(let i = 0; i < parameters_container.children.length; i++) {
		//retrieve parameter container and value
		const parameter_container = parameters_container.children[i];
		const parameter_value = parameter_container.querySelector('input, select, textarea').value;

		//retrieve action parameter
		const action_parameter = action.getParameter(parameter_container.dataset.parameterId);

		//condition parameter
		if(parameter_container.dataset.condition) {
			//condition can be a root entity
			if(Config.Enums.RuleEntities.hasOwnProperty(parameter_value)) {
				action_parameter.rulableEntity = parameter_value;
				action_parameter.conditionId = undefined;
			}
			//or a specific one depending on condition
			else {
				action_parameter.conditionId = parameter_value;
				action_parameter.rulableEntity = undefined;
			}
		}
		else {
			action_parameter.value = parameter_value;
		}
	}
}

function update_select_title(event) {
	//retrieve selected option
	let option;
	//in the case of a mouseover over an option, option is the event target
	if(event && this !== event.target) {
		option = event.target;
	}
	//in other cases retrieve selected option directly from select
	else {
		option = this.options[this.selectedIndex];
	}
	if(option?.value) {
		this.setAttribute('title', `${option.textContent} - ${option.value}`);
	}
	else {
		this.removeAttribute('title');
	}
}

function condition_dragstart(event) {
	event.dataTransfer.setDragImage(this.parentNode.lastElementChild, -5, -5);
	event.dataTransfer.effectAllowed = 'linkMove';
	event.dataTransfer.setData('text/plain', this.parentNode.parentNode.condition.id);
	event.dataTransfer.setData(MediaTypes.RULE_CONDITION_ID, this.parentNode.parentNode.condition.id);
}

function check_drop_condition(dragged_condition, drop_condition) {
	//check drag and drop is not useless
	if(dragged_condition !== drop_condition && dragged_condition.parent !== drop_condition) {
		//check drag and drop is not recursive
		if(!drop_condition.getAncestorConditions().includes(dragged_condition)) {
			const drop_condition_entity = drop_condition.getRuleEntity();
			const condition_entity = dragged_condition.getRuleEntity();
			const condition_parent_entity = dragged_condition.parent ? dragged_condition.parent.getRuleEntity() : undefined;
			//drag and drop is possible in two cases:
			//drag and drop one condition in the same kind of condition
			//drag and drop one condition which can be a child of landing condition
			if(condition_entity === drop_condition_entity || condition_parent_entity === drop_condition_entity) {
				return true;
			}
		}
	}
	return false;
}

function condition_dragover(event) {
	if(event.dataTransfer.types.includes(MediaTypes.RULE_CONDITION_ID)) {
		let allow_drop = false;
		//BUG some browsers do not allow to check what is dragged during drag https://bugs.webkit.org/show_bug.cgi?id=58206 or http://code.google.com/p/chromium/issues/detail?id=50009
		//check if drop is possible only on browsers that allow sniffing data
		if(event.dataTransfer.getData(MediaTypes.RULE_CONDITION_ID)) {
			const condition = selected_constraint.getCondition(event.dataTransfer.getData(MediaTypes.RULE_CONDITION_ID));
			const landing_condition = this.parentNode.condition;
			if(check_drop_condition(condition, landing_condition)) {
				allow_drop = true;
			}
		}
		//SHORTCUT allow drop for browsers that don't allow sniffing data
		else {
			console.warn('Unable to sniff data, allowing item drop');
			allow_drop = true;
		}
		//ENDSHORTCUT
		if(allow_drop) {
			this.classList.add('highlight');
			event.preventDefault();
		}
	}
}

function condition_dragleave() {
	this.classList.remove('highlight');
}

function condition_drop(event) {
	event.preventDefault();
	condition_dragleave.call(this);
	if(event.dataTransfer.types.includes(MediaTypes.RULE_CONDITION_ID)) {
		const condition = selected_constraint.getCondition(event.dataTransfer.getData(MediaTypes.RULE_CONDITION_ID));
		const landing_condition = this.parentNode.condition;
		if(check_drop_condition(condition, landing_condition)) {
			//add dropped condition as child of landing condition
			event.preventDefault();
			//update model
			condition.parent.conditions.removeElement(condition);
			condition.parent = landing_condition;
			landing_condition.conditions.push(condition);
			//update ui
			const condition_li = document.getElementById(condition.id);
			condition_li.parentNode.removeChild(condition_li);
			this.nextElementSibling.appendChild(condition_li);
			update_and_check_condition(condition);
		}
	}
}

function fill_condition_select(select, condition, property, value) {
	if(property.configurationEntity) {
		//retrieve available nodes for this condition
		const elements = condition.getAvailableResults(property);
		elements.sort(function(a, b) {
			return a.getLocalizedFullLabel(Languages.GetLanguage()).compareTo(b.getLocalizedFullLabel(Languages.GetLanguage()));
		});
		FormHelpers.FillSelect(select, elements, true, value, {label_property: 'getLocalizedFullLabel'});
	}
	else {
		select.fill(property.options, false, value);
	}
}

function update_and_check_condition(condition) {
	const condition_ui = document.getElementById(condition.id);
	//update condition ui
	const property_name = condition_ui.querySelector('select[name="property"]').value;
	if(property_name) {
		const entity = condition.getRuleEntity();
		const property = selected_study.getAllRuleDefinitionProperty(entity, property_name);
		if(property.configurationEntity || property.options) {
			const operator_name = condition_ui.querySelector('select[name="operator"]').value;
			if(operator_name) {
				const operator = Config.Enums.Operator[operator_name];
				if(operator.has_value) {
					if([Config.Enums.Operator.EQUALS, Config.Enums.Operator.NOT_EQUALS].includes(operator)) {
						const select = condition_ui.querySelector('select[name="value"]');
						fill_condition_select(select, condition, property, select.value);
					}
				}
			}
		}
	}
	//check condition
	if(!condition.isValid()) {
		condition_ui.classList.add('error');
		UI.Notify('This modification invalidates a descendant condition', {
			tag: 'error',
			icon: 'images/notifications_icons/warning.svg',
			body: `Descendant condition with id ${condition.id} must be updated.`
		});
	}
	else {
		condition_ui.classList.remove('error');
		//do the same for children conditions
		condition.conditions.forEach(update_and_check_condition);
	}
}

function draw_condition(entity, condition) {
	const condition_li = document.createFullElement('li', {id: condition.id});
	condition_li.condition = condition;

	const condition_div = document.createFullElement('div');
	condition_div.addEventListener('dragover', condition_dragover);
	condition_div.addEventListener('dragleave', condition_dragleave);
	condition_div.addEventListener('drop', condition_drop);
	condition_li.appendChild(condition_div);

	function property_change() {
		const property = selected_study.getAllRuleDefinitionProperty(entity, property_select.value);
		//update model
		condition.criterion.property = property.id;
		//update operators
		if(property.type) {
			//build operators list
			const type = Config.Enums.DataType[property.type];
			const operators = Object.fromEntries(type.operators.map(o => [o.name, o]));
			//update ui
			FormHelpers.FillSelectEnum(operator_select, operators, true, condition.criterion.operator);
			operator_select.style.display = 'block';
		}
		else {
			FormHelpers.FillSelectEnum(operator_select, {}, true);
			operator_select.value = '';
			operator_select.style.display = 'none';
		}
		//update ui
		//operator_mode_select.style.display = 'inline';
		value_global_container.style.display = 'none';
		//manage continue button
		continue_button.style.display = property.target ? 'inline' : 'none';
	}

	function operator_change() {
		//update model
		condition.criterion.operator = operator_select.value || undefined;
		//clear values
		value_container.empty();
		//update ui
		if(operator_select.value && Config.Enums.Operator[operator_select.value].has_value) {
			//create initial value if there is no one
			if(condition.criterion.values.isEmpty()) {
				condition.criterion.values.push('');
			}
			condition.criterion.values.map(draw_value).forEach(Node.prototype.appendChild, value_container);
			value_global_container.style.display = 'block';
		}
		else {
			value_global_container.style.display = 'none';
		}
	}

	function draw_value(value) {
		const property = selected_study.getAllRuleDefinitionProperty(entity, property_select.value);
		const value_p = document.createFullElement('p', {style: 'position: relative; margin: 0 5px 3px;'});

		//select
		if(['EQUALS', 'NOT_EQUALS'].includes(operator_select.value) && (property.configurationEntity || property.options)) {
			const select = document.createFullElement('select', {name: 'value'});
			fill_condition_select(select, condition, property, value);
			select.addEventListener('mouseover', update_select_title);
			select.addEventListener(
				'change',
				function() {
					//update model
					condition.criterion.values = condition_div.querySelectorAll('[name="value"]').map(e => e.value);
					//update title to show selected value
					update_select_title.call(select);
					//check descendants conditions
					update_and_check_condition(condition);
				}
			);
			update_select_title.call(select);
			value_p.appendChild(select);
		}
		//input
		else {
			const input = document.createFullElement('input', {name: 'value', value: value || '', autocomplete: 'off', style: 'width: 25rem;'});
			value_p.appendChild(input);
			ConstraintHelpers.EnhanceValueInput(selected_constraint, input);
		}

		const remove_value_button = document.createFullElement('img', {src: 'images/delete.png', alt: 'Delete value', title: 'Delete value', style: 'margin-left: 0.5rem;'});
		remove_value_button.addEventListener(
			'click',
			function() {
				const value = this.parentNode;
				const container = value.parentNode;
				if(container.children.length > 1) {
					container.removeChild(value);
					//update model
					condition.criterion.values = container.querySelectorAll('[name="value"]').map(e => e.value);
					//check descendants conditions
					update_and_check_condition(condition);
				}
			}
		);
		value_p.appendChild(remove_value_button);
		return value_p;
	}

	//dependency
	const dependency_properties = {name: 'dependency', type: 'checkbox', title: 'Dependency flag'};
	if(condition.dependency) {
		dependency_properties.checked = 'checked';
	}
	condition_div.appendChild(document.createFullElement('input', dependency_properties));

	//break
	function update_rule_break() {
		let src, title;
		switch(condition.breakType) {
			case 'NONE' :
				src = 'images/stop_blue.png';
				title = 'No break. Click to break and allow.';
				break;
			case 'ALLOW' :
				src = 'images/stop_green.png';
				title = 'Break and allow. Click to break and deny.';
				break;
			case 'DENY' :
				src = 'images/stop_red.png';
				title = 'Break and deny. Click to no break.';
				break;
		}
		this.setAttribute('src', src);
		this.setAttribute('title', title);
	}
	const break_type = document.createFullElement('img', {alt: 'Break', title: 'Break', src: 'images/stop_blue.png', style: 'cursor: pointer; margin: 0 3px;'});
	break_type.addEventListener(
		'click',
		function() {
			condition.breakType = condition.breakType === 'NONE' ? 'ALLOW' : condition.breakType === 'ALLOW' ? 'DENY' : 'NONE';
			update_rule_break.call(this);
		}
	);
	update_rule_break.call(break_type);
	condition_div.appendChild(break_type);

	//inverse
	const inverse_properties = {name: 'inverse', type: 'checkbox', title: 'Inverse condition', style: 'float: left;'};
	if(condition.inverse) {
		inverse_properties.checked = 'checked';
	}
	condition_div.appendChild(document.createFullElement('input', inverse_properties));

	//property select
	const property_select = document.createFullElement('select', {name: 'property', style: 'float: left; margin: 0 0.5rem;'});
	property_select.addEventListener(
		'change',
		function() {
			condition.criterion.operator = undefined;
			property_change();
		}
	);
	//fill select
	selected_study.getAllRuleDefinitionProperties(entity).forEach(function(property) {
		const option_properties = {value: property.id};
		if(property.id === condition.criterion.property) {
			option_properties.selected = 'selected';
		}
		property_select.appendChild(document.createFullElement('option', option_properties, property.target ? `${property.label} (${property.target})` : property.label));
	});
	condition_div.appendChild(property_select);

	//operator select
	const operator_select = document.createFullElement('select', {name: 'operator', style: 'float: left; margin: 0 0.5rem;'});
	operator_select.addEventListener('change', operator_change);
	condition_div.appendChild(operator_select);

	//values
	const value_global_container = document.createFullElement('div', {style: 'float: left;'});
	const value_container = document.createFullElement('div', {style: 'float: left;'});
	value_global_container.appendChild(value_container);
	const add_value_button = document.createFullElement('img', {src: 'images/add.png', alt: 'Add value', title: 'Add value', style: 'float: right;'});
	add_value_button.addEventListener(
		'click',
		function() {
			value_container.appendChild(draw_value());
		}
	);
	value_global_container.appendChild(add_value_button);

	condition_div.appendChild(value_global_container);

	//condition
	/*condition_div.appendChild(document.createFullElement('label', {style : 'float: left; margin: 0 0.5rem;'}, 'or condition n°'));
	var condition_select = document.createFullElement('select', {name : 'condition_id', 'class' : 'rule_condition', style : 'float: left;'});
	condition_select.fill(selected_constraint.getAllConditionsIds(), true, condition.criterion.conditionId);
	condition_div.appendChild(condition_select);*/

	//continue button
	const continue_button = document.createFullElement('img', {alt: 'Add a condition', title: 'Add a condition', src: 'images/control_play.png', style: 'margin: 0 1rem;'});
	continue_button.addEventListener(
		'click',
		function() {
			//condition
			const new_condition = new RuleCondition({id: condition.generateChildId()});
			new_condition.parent = condition;
			condition.conditions.push(new_condition);
			//criterion
			const new_condition_criterion = new RuleConditionCriterion();
			new_condition_criterion.condition = new_condition;
			new_condition.criterion = new_condition_criterion;
			//update ui
			const property = selected_study.getAllRuleDefinitionProperty(entity, property_select.value);
			const target = Config.Enums.RuleEntities[property.target];
			child_conditions.appendChild(draw_condition(target, new_condition));
			refresh_condition_selects();
		}
	);
	condition_div.appendChild(continue_button);

	//set up initial state
	property_change();
	operator_change();

	//delete button
	const delete_button = document.createFullElement('img', {src: 'images/cross.png', alt: 'Delete', title: 'Delete', 'class': 'rule_condition_action'});
	delete_button.addEventListener(
		'click',
		function(event) {
			event.stop();
			UI.Validate('Are you sure you want to delete this condition?').then(confirmed => {
				if(confirmed) {
					//update model
					//remove condition from parent condition list if there is a parent and this parent is a condition
					//there is no parent for condition created in this application at first level
					//there is a parent which is not a condition for condition revived from configuration
					if(condition.parent?.getEntity() === Entities.RuleCondition) {
						condition.parent.conditions.removeElement(condition);
					}
					//remove condition from entity
					else {
						const entity = condition_li.parentNode.parentNode.entity;
						selected_conditions[entity.name].conditions.removeElement(condition);
					}
					//update ui
					condition_li.parentNode.removeChild(condition_li);
					refresh_condition_selects();
				}
			});
		}
	);
	condition_div.appendChild(delete_button);

	//move button
	const move_button = document.createFullElement('img', {src: 'images/arrows_up_down.png', 'data-sort': 'sort', alt: 'Sort condition', title: 'Sort condition', 'class': 'rule_condition_action'});
	condition_div.appendChild(move_button);

	//drag button
	const drag_button = document.createFullElement('img', {src: 'images/anchor.png', alt: 'Drag condition', title: 'Drag condition', 'class': 'rule_condition_action'});
	drag_button.addEventListener('dragstart', condition_dragstart);
	condition_div.appendChild(drag_button);

	//id
	const condition_id = document.createFullElement('span', {contenteditable: 'true', 'class': 'rule_condition_id'}, condition.id);
	condition_id.addEventListener(
		'keydown',
		function(event) {
			//block space key
			if(event.key === ' ') {
				event.stop();
			}
		}
	);
	condition_id.addEventListener(
		'keypress',
		function(event) {
			//validate with enter or escape
			if(event.key === 'Enter' || event.key === 'Escape') {
				event.stop();
				this.blur();
			}
		}
	);
	condition_id.addEventListener(
		'blur',
		function() {
			if(this.textContent !== condition.id) {
				//condition id must not be empty
				if(this.textContent) {
					//condition id must be unique
					if(!selected_constraint.getAllConditionsIds().includes(this.textContent)) {
						//update model
						condition.id = this.textContent;
						//update ui
						condition_li.setAttribute('id', condition.id);
					}
					else {
						this.textContent = condition.id;
						UI.Notify('Condition id must be unique', {tag: 'error', icon: 'images/notifications_icons/warning.svg'});
					}
				}
				else {
					this.textContent = condition.id;
					UI.Notify('Condition id cannot be blank', {tag: 'error', icon: 'images/notifications_icons/warning.svg'});
				}
			}
		}
	);

	condition_div.appendChild(condition_id);

	const child_conditions = document.createFullElement('ul', {'class': `rule_condition ${condition.mode.toLowerCase()}`, style: 'clear: both;'});
	child_conditions.addEventListener(
		'click',
		function(event) {
			if(event.target === this) {
				const position = this.getBoundingClientRect();
				if(event.clientX < position.left + 5) {
					//update model and ui
					if(condition.mode === 'OR') {
						this.classList.remove('or');
						this.classList.add('and');
						condition.mode = 'AND';
					}
					else {
						this.classList.remove('and');
						this.classList.add('or');
						condition.mode = 'OR';
					}
				}
			}
		}
	);
	condition_li.appendChild(child_conditions);

	if(!condition.conditions.isEmpty()) {
		for(let i = 0; i < condition.conditions.length; i++) {
			const property = selected_study.getAllRuleDefinitionProperty(entity, property_select.value);
			const target = Config.Enums.RuleEntities[property.target];
			child_conditions.appendChild(draw_condition(target, condition.conditions[i]));
		}
	}

	Effects.Sortable(
		child_conditions,
		function() {
			condition.conditions = this.children.map(c => c.condition);
		},
		'img[data-sort]'
	);

	return condition_li;
}

function draw_evaluation(evaluation) {
	const evaluation_li = document.createFullElement('li');
	evaluation_li.evaluation = evaluation;

	function get_entity(condition_id) {
		//condition can be a root entity
		if(Config.Enums.RuleEntities.hasOwnProperty(condition_id)) {
			return Config.Enums.RuleEntities[condition_id];
		}
		//or a specific one depending on condition
		else {
			const condition = selected_constraint.getCondition(condition_id);
			return condition.getRuleEntity();
		}
	}

	function condition_change() {
		const condition_id = condition_select.value;
		if(condition_id) {
			const entity = get_entity(condition_id);
			//retrieve only properties which are not jump
			const properties = entity.properties.filter(function(property) {
				return property.hasOwnProperty('type');
			});
			property_select.fillObjects(properties, 'id', 'label', true, evaluation.property);
			property_select.style.display = 'inline';
		}
		else {
			property_select.style.display = 'none';
		}
	}

	function property_change() {
		const condition_id = condition_select.value;
		const property_id = property_select.value;
		if(condition_id && property_id) {
			const entity = get_entity(condition_id);
			const property = entity.getProperty(property_id);
			//update model
			evaluation.property = property.id;
			//update operators
			let operators;
			if(property.type) {
				//build operators list
				const type = Config.Enums.DataType[property.type];
				operators = {};
				for(let i = 0; i < type.operators.length; i++) {
					const operator = type.operators[i];
					operators[operator.name] = operator;
				}
				//update ui
				operator_select.style.display = 'block';
			}
			else {
				operators = Config.Enums.Operator;
				//update ui
				operator_select.style.display = 'none';
			}
			FormHelpers.FillSelectEnum(operator_select, operators, true, evaluation.operator);
			//update ui
			value_global_container.style.display = 'none';
		}
	}

	function operator_change() {
		//update model
		evaluation.operator = operator_select.value || undefined;
		//clear values
		value_container.empty();
		//update ui
		if(operator_select.value && Config.Enums.Operator[operator_select.value].has_value) {
			//create initial value if there is no one
			if(evaluation.values.isEmpty()) {
				evaluation.values.push('');
			}
			evaluation.values.map(draw_value).forEach(Node.prototype.appendChild, value_container);
			value_global_container.style.display = 'block';
		}
		else {
			value_global_container.style.display = 'none';
		}
	}

	function draw_value(value) {
		const condition_id = condition_select.value;
		const entity = get_entity(condition_id);
		const property = entity.getProperty(property_select.value);
		const value_p = document.createFullElement('p', {style: 'margin: 0 5px 3px;'});

		//select
		if(['EQUALS', 'NOT_EQUALS'].includes(operator_select.value) && (property.configurationEntity || property.options)) {
			const select = document.createFullElement('select', {name: 'value'});
			if(property.configurationEntity) {
				try {
					//retrieve available nodes for this condition
					const condition_elements = selected_constraint.getCondition(condition_id).getResults();
					const configuration_entity = property.getConfigurationEntity();
					let elements;
					//retrieve condition configuration entity
					if(configuration_entity === entity.getConfigurationEntity()) {
						elements = condition_elements;
					}
					else {
						elements = [];
						elements = elements.concat.apply(elements, condition_elements.map(e => e.getChildren(Entities[configuration_entity])));
					}
					elements.sort((a, b) => a.getLocalizedFullLabel(Languages.GetLanguage()).compareTo(b.getLocalizedFullLabel(Languages.GetLanguage())));
					FormHelpers.FillSelect(select, elements, true, value, {label_property: 'getLocalizedFullLabel'});
				}
				catch(exception) {
					console.log(exception);
				}
			}
			else {
				select.fill(property.options, false, value);
			}
			select.addEventListener('mouseover', update_select_title);
			select.addEventListener('change', update_select_title);
			update_select_title.call(select);
			value_p.appendChild(select);
		}
		//input
		else {
			const datalist_id = `${condition_id}_datalist`;
			value_p.appendChild(document.createFullElement('input', {name: 'value', value: value || '', style: 'width: 150px;', list: datalist_id}));
			const datalist = document.createFullElement('datalist', {id: datalist_id, 'class': 'datalist_condition'});
			refresh_datalist(datalist);
			value_p.appendChild(datalist);
		}

		const remove_value_button = document.createFullElement('img', {src: 'images/delete.png', alt: 'Delete value', title: 'Delete value', style: 'margin-left: 0.5rem;'});
		remove_value_button.addEventListener(
			'click',
			function() {
				if(this.parentNode.parentNode.children.length > 1) {
					this.parentNode.parentNode.removeChild(this.parentNode);
				}
			}
		);
		value_p.appendChild(remove_value_button);
		return value_p;
	}

	//condition select
	const condition_select = document.createFullElement('select', {name: 'condition', 'class': 'rule_condition', style: 'float: left; width: 160px; margin-left: 0.5rem;'});
	condition_select.fill(selected_constraint.getAllConditionsIds(), true, evaluation.conditionId);
	condition_select.addEventListener(
		'change',
		function() {
			evaluation.property = undefined;
			evaluation.operator = undefined;
			evaluation.value = undefined;
			condition_change();
		}
	);
	evaluation_li.appendChild(condition_select);

	//property select
	const property_select = document.createFullElement('select', {name: 'property', style: 'float: left; margin: 0 0.5rem; display: none;'});
	property_select.addEventListener(
		'change',
		function() {
			evaluation.operator = undefined;
			property_change();
		}
	);
	evaluation_li.appendChild(property_select);

	//operator select
	const operator_select = document.createFullElement('select', {name: 'operator', style: 'float: left; margin: 0 0.5rem; display: none;'});
	operator_select.addEventListener('change', operator_change);
	evaluation_li.appendChild(operator_select);

	//values
	const value_global_container = document.createFullElement('div', {style: 'float: left; display: none;'});
	const value_container = document.createFullElement('div', {style: 'float: left;'});
	value_global_container.appendChild(value_container);
	const add_value_button = document.createFullElement('img', {src: 'images/add.png', alt: 'Add value', title: 'Add value', style: 'float: right;'});
	add_value_button.addEventListener(
		'click',
		function() {
			value_container.appendChild(draw_value());
		}
	);
	value_global_container.appendChild(add_value_button);

	evaluation_li.appendChild(value_global_container);

	//set up initial state
	condition_change();
	property_change();
	operator_change();

	//delete button
	const delete_button = document.createFullElement('img', {src: 'images/cross.png', alt: 'Delete', title: 'Delete', 'class': 'rule_evaluation_action'});
	delete_button.addEventListener(
		'click',
		function(event) {
			event.stop();
			UI.Validate('Are you sure you want to delete this evaluation?').then(confirmed => {
				if(confirmed) {
					//update model
					selected_constraint.evaluations.removeElement(evaluation);
					//update ui
					evaluation_li.parentNode.removeChild(evaluation_li);
				}
			});
		}
	);
	evaluation_li.appendChild(delete_button);

	//separator
	evaluation_li.appendChild(document.createElement('hr'));

	return evaluation_li;
}

function draw_action(action, index) {
	const action_li = document.createFullElement('li');
	action_li.action = action;

	const action_properties = document.createFullElement('div', {style: 'margin-bottom: 1rem;'});
	action_li.appendChild(action_properties);

	function optional_change() {
		if(action_optional.checked) {
			id_paragraph.style.display = 'inline';
			label_paragraph.style.display = 'inline';
		}
		else {
			id_paragraph.style.display = 'none';
			label_paragraph.style.display = 'none';
		}
	}

	const optional_paragraph = document.createFullElement('p', {style: 'display: inline;'});
	action_properties.appendChild(optional_paragraph);
	optional_paragraph.appendChild(document.createFullElement('label', {'for': `action_optional_${index}`, style: 'margin-right: 1rem;'}, 'Optional'));
	const action_optional_properties = {name: 'action_optional', type: 'checkbox', style: 'vertical-align: sub;'};
	if(action.optional) {
		action_optional_properties.checked = 'checked';
	}
	const action_optional = document.createFullElement('input', action_optional_properties);
	action_optional.addEventListener('change', optional_change);
	optional_paragraph.appendChild(action_optional);

	const id_paragraph = document.createFullElement('p', {style: 'display: inline; margin-left: 20px;'});
	id_paragraph.appendChild(document.createFullElement('label', {'for': `action_id_${index}`, style: 'margin-right: 1rem;'}, 'Id'));
	id_paragraph.appendChild(document.createFullElement('input', {name: 'action_id', value: action.id || ''}));
	action_properties.appendChild(id_paragraph);

	const label_paragraph = document.createFullElement('p', {style: 'display: inline; margin-left: 20px;'});
	action_properties.appendChild(label_paragraph);
	label_paragraph.appendChild(document.createFullElement('label', {'for': `action_label_${index}`, style: 'margin-right: 1rem;'}, 'Label'));
	const action_label = document.createFullElement('app-localized-input', {name: 'action_label'});
	label_paragraph.appendChild(action_label);
	FormHelpers.FillLocalizedInput(action_label, action.rule.rulable.getStudy().languages, action.label);

	const selection_container = document.createFullElement('div', {style: 'float: left; width: 500px;'});
	action_li.appendChild(selection_container);

	optional_change();

	function type_change() {
		//clean parameters
		parameters_container.empty();
		switch(type.value) {
			case 'ENTITY_ACTION' :
				selection_static_container.style.display = 'none';
				selection_configuration_container.style.display = 'none';
				selection_condition_container.style.display = 'inline';
				break;
			case 'STATIC_ACTION' :
				selection_configuration_container.style.display = 'none';
				selection_condition_container.style.display = 'none';
				selection_static_container.style.display = 'inline';
				break;
			case 'CONFIGURATION_ACTION' :
				selection_condition_container.style.display = 'none';
				selection_static_container.style.display = 'none';
				selection_configuration_container.style.display = 'inline';
				break;
			default :
				selection_configuration_container.style.display = 'none';
				selection_condition_container.style.display = 'none';
				selection_static_container.style.display = 'none';
		}
	}

	function condition_change() {
		//clean parameters
		parameters_container.empty();
		const condition_id = condition_select.value;
		if(condition_id) {
			let entity;
			//condition can be a root entity
			if(Config.Enums.RuleEntities.hasOwnProperty(condition_id)) {
				entity = Config.Enums.RuleEntities[condition_id];
			}
			//or a specific one depending on condition
			else {
				const condition = selected_constraint.getCondition(condition_id);
				entity = condition.getRuleEntity();
			}
			condition_action_select.fillObjects(selected_study.getAllRuleDefinitionActions(entity), 'id', 'label', true, action.actionId);
			condition_action_select.style.display = 'inline';
		}
		else {
			condition_action_select.style.display = 'none';
		}
	}

	function draw_parameters(parameter_definitions) {
		//manage parameters
		if(parameter_definitions) {
			parameter_definitions.forEach(function(parameter_definition) {
				//initialize parameter in model if needed
				let parameter;
				try {
					parameter = action.getParameter(parameter_definition.id);
				}
				catch {
					parameter = new RuleActionParameter({id: parameter_definition.id});
					parameter.action = action;
					action.parameters.push(parameter);
				}
				//generate parameter id
				const id = `${index}_${parameter_definition.id}`;
				//parameter container
				const parameter_p = document.createFullElement('p', {'class': 'rule_action_parameter'});
				parameters_container.appendChild(parameter_p);
				parameter_p.dataset.parameterId = parameter_definition.id;
				//parameter label
				parameter_p.appendChild(document.createFullElement('label', {'for': id}, parameter_definition.label));
				//parameter field
				let field;
				//parameter is a condition
				if(parameter_definition.dataEntity) {
					parameter_p.dataset.condition = 'true';
					field = document.createFullElement('select', {id: id, name: parameter_definition.id, 'class': 'rule_condition'});
					const data_entity = parameter_definition.getDataEntity();
					field.entity = data_entity;
					field.fill(selected_constraint.getAllConditionsIdsForEntity(data_entity), true, parameter.rulableEntity || parameter.conditionId);
					parameter_p.appendChild(field);
				}
				//parameter is an entity from configuration
				else if(parameter_definition.configurationEntity) {
					field = document.createFullElement('select', {id: id, name: parameter_definition.id});
					const elements = action.rule.rulable.getStudy().getDescendants(parameter_definition.getConfigurationEntity());
					elements.sort(function(a, b) {
						return a.getLocalizedFullLabel(Languages.GetLanguage()).compareTo(b.getLocalizedFullLabel(Languages.GetLanguage()));
					});
					FormHelpers.FillSelect(field, elements, false, parameter.value, {label_property: 'getLocalizedFullLabel'});
					parameter_p.appendChild(field);
				}
				//parameter has options
				else if(parameter_definition.options) {
					field = document.createFullElement('select', {id: id, name: parameter_definition.id});
					field.fill(parameter_definition.options, false, parameter.value);
					parameter_p.appendChild(field);
				}
				else if(parameter_definition.type === 'TEXT') {
					field = document.createFullElement('textarea', {id: id, name: parameter_definition.id});
					field.appendChild(document.createTextNode(parameter.value || ''));
					parameter_p.appendChild(field);
				}
				else {
					//autocomplete field because parameter can be a formula
					//create field
					field = document.createFullElement('input', {id: id, name: parameter_definition.id, value: parameter.value || '', autocomplete: 'off'});
					parameter_p.appendChild(field);
					ConstraintHelpers.EnhanceValueInput(selected_constraint, field);
				}
				if(!parameter_definition.optional) {
					field.setAttribute('required', 'required');
				}
			});
		}
	}

	function static_action_change() {
		//clean parameters
		parameters_container.empty();
		//retrieve static action
		const action_id = static_action_select.value;
		if(action_id) {
			const rule_action = Config.Enums.StaticActions[action_id];
			draw_parameters(rule_action.parameters);
		}
	}

	function condition_action_change() {
		//clean parameters
		parameters_container.empty();
		//retrieve condition action
		const condition_id = condition_select.value;
		const action_id = condition_action_select.value;
		if(condition_id && action_id) {
			let entity;
			//condition can be a root entity
			if(Config.Enums.RuleEntities.hasOwnProperty(condition_id)) {
				entity = Config.Enums.RuleEntities[condition_id];
			}
			//or a specific one depending on condition
			else {
				const condition = selected_constraint.getCondition(condition_id);
				entity = condition.getRuleEntity();
			}
			const rule_action = selected_study.getAllRuleDefinitionAction(entity, action_id);
			draw_parameters(rule_action.parameters);
		}
	}

	function configuration_workflow_change() {
		const workflow_id = configuration_workflow_select.value;
		if(workflow_id) {
			const workflow = action.rule.rulable.getStudy().getWorkflow(configuration_workflow_select.value);
			FormHelpers.FillSelect(configuration_action_select, workflow.actions, true, action.configurationActionId);
			configuration_action_select.style.display = 'inline';
		}
		else {
			configuration_action_select.style.display = 'none';
		}
	}

	/**@type {[string,string][]}*/
	const action_types = [
		['ENTITY_ACTION', 'On your selection'],
		['STATIC_ACTION', 'Using a pre-configured action'],
		['CONFIGURATION_ACTION', 'Trigger an other action']
	];
	const type = document.createFullElement('select', {name: 'type', required: 'required', style: 'width: 120px;'});
	const type_value = action.configurationActionId ? 'CONFIGURATION_ACTION' : action.staticActionId ? 'STATIC_ACTION' : action.rulableEntity || action.conditionId ? 'ENTITY_ACTION' : '';
	type.fill(action_types, true, type_value);
	type.addEventListener(
		'change',
		function() {
			action.staticActionId = undefined;
			action.configurationWorkflowId = undefined;
			action.configurationActionId = undefined;
			action.conditionId = undefined;
			action.actionId = undefined;
			action.parameters = [];
			type_change();
		}
	);
	selection_container.appendChild(type);

	//rule condition action
	const selection_condition_container = document.createFullElement('span', {style: 'display: none;'});
	selection_container.appendChild(selection_condition_container);

	const condition_select = document.createFullElement('select', {name: 'condition', 'class': 'rule_condition', style: 'width: 160px; margin-left: 0.5rem;'});
	condition_select.fill(selected_constraint.getAllConditionsIds(), true, action.rulableEntity || action.conditionId);
	condition_select.addEventListener(
		'change',
		function() {
			action.actionId = undefined;
			action.parameters = [];
			condition_change();
		}
	);
	selection_condition_container.appendChild(condition_select);

	const condition_action_select = document.createFullElement('select', {name: 'condition_action', style: 'width: 160px; margin-left: 0.5rem; display: none;'});
	condition_action_select.addEventListener(
		'change',
		function() {
			action.parameters = [];
			condition_action_change();
		}
	);
	selection_condition_container.appendChild(condition_action_select);

	//static action
	const selection_static_container = document.createFullElement('span', {style: 'display: none;'});
	selection_container.appendChild(selection_static_container);

	const static_action_select = document.createFullElement('select', {name: 'static_action', style: 'width: 160px; margin-left: 0.5rem;'});
	FormHelpers.FillSelectEnum(static_action_select, Config.Enums.StaticActions, true, action.staticActionId);
	static_action_select.addEventListener(
		'change',
		function() {
			action.parameters = [];
			static_action_change();
		}
	);
	selection_static_container.appendChild(static_action_select);

	//other workflow action
	const selection_configuration_container = document.createFullElement('span', {style: 'display: none;'});
	selection_container.appendChild(selection_configuration_container);

	const configuration_workflow_select = document.createFullElement('select', {name: 'workflow', style: 'width: 160px; margin-left: 0.5rem;'});
	FormHelpers.FillSelect(configuration_workflow_select, action.rule.rulable.getStudy().workflows, true, action.configurationWorkflowId);
	configuration_workflow_select.addEventListener(
		'change',
		function() {
			action.configurationActionId = undefined;
			configuration_workflow_change();
		}
	);
	selection_configuration_container.appendChild(configuration_workflow_select);

	const configuration_action_select = document.createFullElement('select', {name: 'workflow_action', style: 'width: 160px; margin-left: 0.5rem; display: none;'});
	selection_configuration_container.appendChild(configuration_action_select);

	const parameters_container = document.createFullElement('div', {style: 'float: left; margin-left: 0.5rem;'});
	action_li.appendChild(parameters_container);

	//set up initial state
	type_change();
	if('ENTITY_ACTION' === type.value) {
		condition_change();
		condition_action_change();
	}
	else if('STATIC_ACTION' === type.value) {
		static_action_change();
	}
	else {
		configuration_workflow_change();
	}

	//delete button
	const delete_button = document.createFullElement('img', {src: 'images/cross.png', alt: 'Delete', title: 'Delete', 'class': 'rule_action_action'});
	delete_button.addEventListener(
		'click',
		function(event) {
			event.stop();
			UI.Validate('Are you sure you want to delete this action?').then(confirmed => {
				if(confirmed) {
					//update model
					selected_rule.actions.removeElement(this.parentNode.action);
					//update ui
					this.parentNode.parentNode.removeChild(this.parentNode);
				}
			});
		}
	);
	action_li.appendChild(delete_button);

	//move button
	const move_button = document.createFullElement('img', {src: 'images/arrows_up_down.png', 'data-sort': 'sort', alt: 'Sort condition', title: 'Sort condition', 'class': 'rule_action_action'});
	action_li.appendChild(move_button);

	//separator
	action_li.appendChild(document.createElement('hr'));

	return action_li;
}

//TODO improve this
let selected_study;

let selected_rule;
let selected_constraint;
let selected_conditions;

export const Rule = {
	Init: function() {

		//manage rule tags
		const tags_provider = function(text) {
			const filter = text.toLowerCase();
			return selected_rule.rulable.getStudy().ruleTags.filter(t => t.toLowerCase().includes(filter));
		};

		//autocomplete
		const tag_drawer = function(tag, value) {
			const regexp = new RegExp(`(${value})`, 'gi');
			const tag_li = document.createFullElement('li', {'data-value': tag});
			const tag_span = document.createElement('span');
			tag_span.innerHTML = tag.replace(regexp, '<span class="highlight">$1</span>');
			tag_li.appendChild(tag_span);
			return tag_li;
		};

		Forms.Tagger(
			document.getElementById('rule_tags_field'),
			document.getElementById('rule_tags_choices'),
			document.getElementById('rule_tags_selected'),
			document.getElementById('rule_tags'),
			tags_provider,
			tag_drawer
		);

		//find rule entity for node entity
		/*function find_matching_rule_entity(node_entity) {
			for(var rule_entity_id in Config.Enums.RuleEntities) {
				if(Config.Enums.RuleEntities.hasOwnProperty(rule_entity_id)) {
					if(Config.Enums.RuleEntities[rule_entity_id].configuration_entity === node_entity) {
						return Config.Enums.RuleEntities[rule_entity_id];
					}
				}
			}
		}*/

		let prevent_submission = false;

		//add condition on entity from drag&drop
		function entity_dragover(event) {
			let allow_drop = false;
			if(event.dataTransfer.types.includes(MediaTypes.RULE_CONDITION_ID)) {
				//BUG some browsers do not allow to check what is dragged during drag https://bugs.webkit.org/show_bug.cgi?id=58206 or http://code.google.com/p/chromium/issues/detail?id=50009
				//check if drop is possible only on browsers that allow sniffing data
				if(event.dataTransfer.getData(MediaTypes.RULE_CONDITION_ID)) {
					const condition = selected_constraint.getCondition(event.dataTransfer.getData(MediaTypes.RULE_CONDITION_ID));
					const entity = this.parentNode.entity;
					if(condition.parent.getRuleEntity() === entity) {
						allow_drop = true;
					}
				}
				//SHORTCUT allow drop for browsers that don't allow sniffing data
				else {
					console.warn('Unable to sniff data, allowing item drop');
					allow_drop = true;
				}
				//ENDSHORTCUT
			}
			if(allow_drop) {
				event.preventDefault();
				this.classList.add('highlight');
			}
		}

		function entity_dragleave() {
			this.classList.remove('highlight');
		}

		function entity_drop(event) {
			event.preventDefault();
			entity_dragleave.call(this);
			if(event.dataTransfer.types.includes(MediaTypes.RULE_CONDITION_ID)) {
				const condition = selected_constraint.getCondition(event.dataTransfer.getData(MediaTypes.RULE_CONDITION_ID));
				const entity = this.parentNode.entity;
				if(condition.parent.getRuleEntity() === entity) {
					event.preventDefault();
					//update model
					condition.parent.conditions.removeElement(condition);
					condition.parent = selected_conditions[entity.name];
					selected_conditions[entity.name].conditions.push(condition);
					//update ui
					const condition_li = document.getElementById(condition.id);
					condition_li.parentNode.removeChild(condition_li);
					this.nextElementSibling.appendChild(condition_li);
					update_and_check_condition(condition);
				}
			}
		}

		//conditions
		//create handle for each entity
		const entity_list = document.getElementById('rule_conditions');
		Object.keys(Config.Enums.RuleEntities).forEach(function(entity_id) {
			const entity = Config.Enums.RuleEntities[entity_id];
			const entity_li = document.createElement('li');
			entity_li.entity = entity;
			//title
			const entity_div = document.createFullElement('div');
			entity_div.addEventListener('dragenter', entity_dragover);
			entity_div.addEventListener('dragover', entity_dragover);
			entity_div.addEventListener('dragleave', entity_dragleave);
			entity_div.addEventListener('drop', entity_drop);
			entity_li.appendChild(entity_div);
			//label
			entity_div.appendChild(document.createTextNode(entity.label));
			//add
			entity_div.appendChild(document.createFullElement('img', {alt: 'Add a condition', title: 'Add a condition', src: 'images/control_play.png', style: 'cursor: pointer; vertical-align: middle; margin: 0 3px;'}));
			/*var entity_add_input = document.createFullElement('input', {placeholder : 'quick add...'});
			var entity_add_results = document.createFullElement('ul', {'class' : 'node_autocomplete'});
			entity_div.appendChild(entity_add_input);
			entity_div.appendChild(entity_add_results);
			NodeTools.ManageAutocomplete(
				entity_add_input,
				entity_add_results,
				function(node) {
					return [Entities.EventModel, Entities.Document, Entities.Attribute, Entities.FormModel, Entities.Workflow].includes(node.getEntity());
				},
				function(node) {
					prevent_submission = true;
				},
				function(node) {
					//clean input
					entity_add_input.value = '';
					//retrieve node ancestors
					var node_ancestors = [];
					var rule_entities_ancestors = [];
					var rule_entity;
					var parent = node;
					while(parent.getParent) {
						node_ancestors.unshift(parent);
						rule_entity = find_matching_rule_entity(parent.constructor);
						rule_entities_ancestors.unshift(rule_entity);
						if(rule_entity === entity) {
							break;
						}
						parent = parent.getParent();
					}
					//create root
					var current_condition;
					var current_entity = entity;
					var ancestor;
					for(var i = 0; i < rule_entities_ancestors.length; i++) {
						ancestor = node_ancestors[i];
						rule_entity = rule_entities_ancestors[i];
						//condition
						var condition = new RuleCondition();
						//criterion
						var criterion = new RuleConditionCriterion();
						criterion.property = current_entity.properties.find(e => e.target === rule_entity).id;
						condition.criterion = criterion;
						criterion.condition = condition;
						//id condition
						var id_condition = new RuleCondition();
						//id criterion
						var id_criterion = new RuleConditionCriterion();
						id_criterion.property = 'ID';
						id_criterion.operator = 'EQUALS'
						id_criterion.values = [ancestor.id];
						id_condition.criterion = id_criterion;
						id_criterion.condition = id_condition;
						//link relation condition and id condition
						condition.conditions.push(id_condition);
						//add root condition
						if(!current_condition) {
							condition.parent = selected_conditions[entity_id].conditions;
							selected_conditions[entity_id].conditions.push(condition);
							condition_list.appendChild(draw_condition(entity, condition));
						}
						//add following conditions
						else {
							current_condition.conditions.push(condition);
							//this.parentNode.nextElementSibling.appendChild(draw_condition(entity, condition));
						}
						current_condition = id_criterion;
						current_entity = rule_entity;
						//rule_entity = find_matching_rule_entity(ancestor.constructor);
						refresh_condition_selects();
					}
					prevent_submission = false;
				}
			);*/
			//id
			entity_div.appendChild(document.createFullElement('span', {'class': 'rule_condition_id', style: 'margin-right: 70px;'}, entity_id));
			//conditions list
			const condition_list = document.createFullElement('ul', {'class': 'rule_condition'});
			condition_list.addEventListener(
				'click',
				function(event) {
					if(event.target === this) {
						const position = this.getBoundingClientRect();
						const entity_id = this.entity.name;
						if(event.clientX < position.left + 5) {
							//update model and ui
							if(selected_conditions[entity_id].mode === 'OR') {
								this.classList.remove('or');
								this.classList.add('and');
								selected_conditions[entity_id].mode = 'AND';
							}
							else {
								this.classList.remove('and');
								this.classList.add('or');
								selected_conditions[entity_id].mode = 'OR';
							}
						}
					}
				}
			);
			condition_list.entity = entity;
			entity.ui = condition_list;

			Effects.Sortable(
				condition_list,
				function() {
					selected_conditions[entity_id].conditions = this.children.map(c => c.condition);
				},
				'img[data-sort]'
			);

			entity_li.appendChild(condition_list);
			entity_list.appendChild(entity_li);
		});

		function exit_rule() {
			//retrieve parent node
			//const node = selected_rule ? selected_rule.rulable : selected_constraint.constrainable;
			selected_rule = undefined;
			selected_constraint = undefined;
			selected_conditions = undefined;
			//return to rulable parent
			//Router.SelectNode(node);
			//return to previous page
			window.history.back();
		}

		document.getElementById('rule_formulas').addEventListener(
			'click',
			function(event) {
				event.preventDefault();
				FormulasHelp.Open();
			}
		);

		//manage submission
		document.getElementById('rule').addEventListener(
			'submit',
			function(event) {
				event.preventDefault();

				//do not submit from autocomplete selection
				if(!prevent_submission) {
					//save rule properties
					if(selected_rule) {
						selected_rule.description = document.getElementById('rule_description').value;
						const rule_message = document.getElementById('rule_message').value;
						selected_rule.message = rule_message ? JSON.parse(rule_message) : {};
						selected_rule.tags = JSON.parse(document.getElementById('rule_tags').value);
						//update tags dictionary in study
						const study = selected_rule.rulable.getStudy();
						selected_rule.tags.forEach(function(tag) {
							if(!study.ruleTags.includes(tag)) {
								study.ruleTags.push(tag);
							}
						});
					}

					//save conditions
					let entity_id, entity, i;
					for(entity_id in selected_conditions) {
						if(selected_conditions.hasOwnProperty(entity_id)) {
							entity = Config.Enums.RuleEntities[entity_id];
							for(i = selected_conditions[entity_id].conditions.length - 1; i >= 0; i--) {
								update_condition(entity, selected_conditions[entity_id].conditions[i]);
							}
						}
					}

					//save evaluations
					const evaluations = document.getElementById('rule_evaluations').children;
					for(let i = evaluations.length - 1; i >= 0; i--) {
						update_evaluation_from_container(evaluations[i].evaluation, evaluations[i]);
					}

					//save actions if needed
					const actions = document.getElementById('rule_actions').children;
					for(let i = actions.length - 1; i >= 0; i--) {
						update_action_from_container(actions[i].action, actions[i]);
					}

					//notify user
					let body;
					//a rule has been edited
					if(selected_rule) {
						body = `Rule for ${selected_rule.rulable.getEntity().label.toLowerCase()} ${selected_rule.rulable.getLocalizedLabel(Languages.GetLanguage()).toLowerCase()} updated.`;
					}
					//a constraint has been edited
					else {
						body = `Constraint for ${selected_constraint.constrainable.getEntity().label.toLowerCase()} ${selected_constraint.constrainable.getLocalizedLabel(Languages.GetLanguage()).toLowerCase()} updated.`;
					}
					UI.Notify('Rule saved successfully', {
						tag: 'info',
						icon: 'images/notifications_icons/done.svg',
						body: body
					});
					exit_rule();
				}
				else {
					prevent_submission = false;
				}
			}
		);

		//manage cancel
		document.getElementById('rule_cancel').addEventListener(
			'click',
			function(event) {
				event.preventDefault();
				exit_rule();
			}
		);

		//evaluations
		document.getElementById('rule_evaluation_add').addEventListener(
			'click',
			function(event) {
				event.preventDefault();
				const evaluation = new RuleEvaluation();
				selected_constraint.evaluations.push(evaluation);
				document.getElementById('rule_evaluations').appendChild(draw_evaluation(evaluation));
			}
		);

		//actions
		document.getElementById('rule_action_add').addEventListener(
			'click',
			function(event) {
				event.preventDefault();
				const action = new RuleAction();
				action.rule = selected_rule;
				selected_rule.actions.push(action);
				document.getElementById('rule_actions').appendChild(draw_action(action, selected_rule.actions.length - 1));
			}
		);

		//sort actions
		Effects.Sortable(
			document.getElementById('rule_actions'),
			function() {
				const actions = this.children.map(function(action_li) {return action_li.action;});
				selected_rule.actions.sort(function(action_1, action_2) {
					return actions.indexOf(action_1) - actions.indexOf(action_2);
				});
			},
			'img[data-sort]'
		);

		//add action or evaluation from condition drop
		function action_evaluation_dragover(event) {
			if(event.dataTransfer.types.includes(MediaTypes.RULE_CONDITION_ID)) {
				this.classList.add('highlight');
				event.preventDefault();
			}
		}

		function action_evaluation_dragleave() {
			this.classList.remove('highlight');
		}

		function condition_action_drop(event) {
			event.preventDefault();
			action_evaluation_dragleave.call(this);
			if(event.dataTransfer.types.includes(MediaTypes.RULE_CONDITION_ID)) {
				const action = new RuleAction();
				action.rulableEntity = event.dataTransfer.getData(MediaTypes.RULE_CONDITION_ID);
				selected_rule.actions.push(action);
				document.getElementById('rule_actions').appendChild(draw_action(action, selected_rule.actions.length - 1));
			}
		}

		function condition_evaluation_drop(event) {
			event.preventDefault();
			action_evaluation_dragleave.call(this);
			if(event.dataTransfer.types.includes(MediaTypes.RULE_CONDITION_ID)) {
				const evaluation = new RuleEvaluation();
				evaluation.conditionId = event.dataTransfer.getData(MediaTypes.RULE_CONDITION_ID);
				selected_constraint.evaluations.push(evaluation);
				document.getElementById('rule_evaluations').appendChild(draw_evaluation(evaluation));
			}
		}

		document.getElementById('rule_actions_container').addEventListener('dragenter', action_evaluation_dragover);
		document.getElementById('rule_actions_container').addEventListener('dragover', action_evaluation_dragover);
		document.getElementById('rule_actions_container').addEventListener('dragleave', action_evaluation_dragleave);
		document.getElementById('rule_actions_container').addEventListener('drop', condition_action_drop);

		document.getElementById('rule_evaluations_container').addEventListener('dragenter', action_evaluation_dragover);
		document.getElementById('rule_evaluations_container').addEventListener('dragover', action_evaluation_dragover);
		document.getElementById('rule_evaluations_container').addEventListener('dragleave', action_evaluation_dragleave);
		document.getElementById('rule_evaluations_container').addEventListener('drop', condition_evaluation_drop);

		bus_ui.register({
			onLoadStudy: function() {
				bus.register({
					onChangeRuleConditionId: function(event) {
						if(event.node.getConstraint() === selected_constraint) {
							//update values in conditions
							document.getElementById('rule_conditions_container').querySelectorAll('input[name="value"]').forEach(function(input) {
								if(input.value.indexOf('=') === 0) {
									const formula = input.value.substring(1);
									input.value = `=${formula.replace(`${event.oldValue}:`, `${event.newValue}:`)}`;
								}
							});
							//update actions selects while keeping selection for actions applying on this condition
							document.getElementById('rule_actions_container').querySelectorAll('select[class="rule_condition"]').forEach(function(select) {
								//retrieve current value or use new value
								const value = select.value === event.oldValue ? event.newValue : select.value;
								const blank_option = !select.firstElementChild.value;
								const condition_ids = select.entity ? selected_constraint.getAllConditionsIdsForEntity(select.entity) : selected_constraint.getAllConditionsIds();
								//refresh this particular select to be able to select new condition id
								select.fill(condition_ids, blank_option, value);
							});
							//refresh all select and datalists
							refresh_condition_selects();
						}
					}
				});
			}
		});
	},
	Draw: function(rule, entities) {
		//keep handle on rule and actions
		selected_rule = rule;

		//conditions
		this.DrawConstraint(rule.constraint, entities);

		//properties
		//display this section before enhancing/updating its content
		//this is because some customization like "tagger" requires HTML to be rendered
		document.getElementById('rule_properties').style.display = 'table';

		const rule_description = document.getElementById('rule_description');
		rule_description.value = rule.description || '';
		rule_description.removeAttribute('disabled');

		const rule_message = document.getElementById('rule_message');
		rule_message.removeAttribute('disabled');
		FormHelpers.FillLocalizedInput(rule_message, rule.rulable.getStudy().languages, rule.message);

		const rule_tags = document.getElementById('rule_tags');
		rule_tags.value = JSON.stringify(rule.tags);
		const rule_tags_change = new UIEvent('change', {bubbles: true, cancelable: true});
		rule_tags.dispatchEvent(rule_tags_change);

		//actions
		document.getElementById('rule_actions_container').style.display = 'block';
		const rule_actions = document.getElementById('rule_actions');
		rule.actions.map(draw_action).forEach(Node.prototype.appendChild, rule_actions);
	},
	DrawConstraint: function(constraint, entities) {
		selected_study = constraint.constrainable.getStudy();
		//keep handle on entities and conditions
		selected_constraint = constraint;
		selected_conditions = constraint.conditions;

		//disabled and hide properties
		document.getElementById('rule_description').setAttribute('disabled', 'disabled');
		document.getElementById('rule_message').setAttribute('disabled', 'disabled');
		document.getElementById('rule_properties').style.display = 'none';

		//manage available entities and clean previous conditions
		document.querySelectorAll('#rule_conditions > li').forEach(function(element) {
			element.lastElementChild.empty();

			const add_condition_button = element.firstElementChild.firstElementChild;
			if(!entities || entities.includes(element.entity)) {
				element.classList.remove('disabled');
				add_condition_button.style.cursor = 'pointer';
				add_condition_button.addEventListener('click', add_condition);
			}
			else {
				element.classList.add('disabled');
				add_condition_button.style.cursor = 'auto';
				add_condition_button.removeEventListener('click', add_condition);
			}
		});

		for(const entity_id in constraint.conditions) {
			if(constraint.conditions.hasOwnProperty(entity_id)) {
				const entity = Config.Enums.RuleEntities[entity_id];
				//manage condition operator
				entity.ui.classList.remove('or');
				entity.ui.classList.remove('and');
				entity.ui.classList.add(constraint.conditions[entity_id].mode.toLowerCase());
				//draw conditions
				for(let i = 0; i < constraint.conditions[entity_id].conditions.length; i++) {
					entity.ui.appendChild(draw_condition(entity, constraint.conditions[entity_id].conditions[i]));
				}
			}
		}

		//clear evaluations
		const rule_evaluations = document.getElementById('rule_evaluations');
		rule_evaluations.empty();
		//constraint.evaluations.map(draw_evaluation).forEach(Node.prototype.appendChild, rule_evaluations);

		//clear and hide and actions
		document.getElementById('rule_actions_container').style.display = 'none';
		document.getElementById('rule_actions').empty();

		//RuleDSL.DrawConditions(node, entities, conditions);
	}
};
