import '../basic-tools/extension.js';

import {SVG} from '../basic-tools/svg.js';
import {Geometry} from '../tools/geometry.js';
import {Languages} from '../languages.js';
import {Config} from '../model_config.js';
import {NodeTools} from '../node_tools.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {Entities} from '../model/config/entities.js';

function manage_mandatory() {
	const workflow_action_id = /**@type {HTMLSelectElement}*/ (document.getElementById('workflow_action_id'));

	if(/**@type {HTMLInputElement}*/ (document.getElementById('workflow_mandatory')).checked) {
		//reset and hide field
		workflow_action_id.value = '';
		workflow_action_id.parentElement.style.display = 'none';
	}
	else {
		workflow_action_id.parentElement.style.display = 'block';
	}
}

function manage_aggregate_workflow() {
	const workflow_mandatory = /**@type {HTMLInputElement}*/ (document.getElementById('workflow_mandatory'));
	const workflow_initial_state_id = /**@type {HTMLSelectElement}*/ (document.getElementById('workflow_initial_state_id'));
	const workflow_unique = /**@type {HTMLInputElement}*/ (document.getElementById('workflow_unique'));

	if(/**@type {HTMLSelectElement}*/ (document.getElementById('workflow_aggregate_workflow_id')).value) {
		//reset and hide fields
		workflow_mandatory.checked = false;
		workflow_mandatory.parentElement.style.display = 'none';
		workflow_initial_state_id.value = '';
		workflow_initial_state_id.parentElement.style.display = 'none';
		workflow_unique.checked = true;
		workflow_unique.parentElement.style.display = 'none';
	}
	else {
		workflow_mandatory.parentElement.style.display = 'block';
		workflow_initial_state_id.parentElement.style.display = 'block';
		workflow_unique.parentElement.style.display = 'block';
	}
}

class WorkflowReport {
	constructor(workflow) {
		this.workflow = workflow;
		this.setters = []; //all setters that initialize the workflow
		this.unknown = []; //rules that interact with the workflow
		this.states = workflow.states.map(s => new WorkflowStateReport(s));
	}
	isEmpty() {
		return this.states.isEmpty();
	}
	getStateReport(state) {
		return this.states.find(s => s.state === state);
	}
	addInitializationAction(node, action) {
		let setter = this.setters.find(s => s.node === node);
		if(!setter) {
			setter = new WorkflowSetter(node);
			this.setters.push(setter);
		}
		setter.actions.push(action);
	}
	addUnknownAction(node, action) {
		let unknown = this.unknown.find(s => s.node === node);
		if(!unknown) {
			unknown = new WorkflowSetter(node);
			this.unknown.push(unknown);
		}
		unknown.actions.push(action);
	}
	addStateSetter(node, state_id, action) {
		//find workflow state report
		const report = this.getStateReport(this.workflow.getState(state_id));
		//create setter matching source
		let setter = report.setters.find(s => s.node === node);
		if(!setter) {
			setter = new WorkflowSetter(node);
			report.setters.push(setter);
		}
		setter.actions.push(action);
	}
	static fromWorkflow(workflow) {
		const report = new WorkflowReport(workflow);
		function analyze_action(action) {
			const rule = action.rule;
			switch(action.actionId) {
				case 'INITIALIZE_WORKFLOW':
					if(action.getParameter('WORKFLOW').value === workflow.id) {
						report.addInitializationAction(action.rule.rulable, action);
					}
					break;
				case 'CHANGE_STATUS': {
					//TODO this is useless as CHANGE_STATUS actions only occur on workflows
					if(action.getRuleEntity() === Config.Enums.RuleEntities.WORKFLOW) {
						let valid = false;
						//check if rule action is inside an action of the workflow
						if(action.rulableEntity) {
							valid = rule.rulable.getEntity() === Entities.Action && rule.rulable.workflow === workflow;
						}
						//check if rule action is related to the workflow
						else {
							const condition = rule.constraint.getCondition(action.conditionId);
							const condition_results = condition.getResults();
							if(condition_results.length === 1 && condition_results[0] === workflow || condition.criterion.values.includes(workflow.id)) {
								valid = true;
							}
						}
						if(valid) {
							const state = action.getParameter('STATUS').value;
							//status may be a real status or a formula
							if(state.startsWith('=')) {
								report.addUnknownAction(action.rule.rulable, action);
							}
							else {
								report.addStateSetter(action.rule.rulable, state, action);
							}
						}
					}
				}
			}
		}
		function analyze_rule(rule) {
			rule.actions.forEach(analyze_action);
		}
		function analyze_node(node) {
			//retrieve rules
			const relation = node.getEntity().children[Entities.Rule.name];
			if(relation.size > 1) {
				for(let i = 0; i < relation.size; i++) {
					node.getChildren(Entities.Rule, i).forEach(analyze_rule);
				}
			}
			else {
				node.getChildren(Entities.Rule).forEach(analyze_rule);
			}
		}
		//find all rules
		analyze_node(workflow.study);
		const entities = [Entities.EventModel, Entities.DatasetModel, Entities.FieldModel, Entities.Workflow, Entities.Action];
		Array.prototype.concat.apply([], entities.map(e => workflow.study.getDescendants(e))).forEach(analyze_node);
		return report;
	}
}

class WorkflowStateReport {
	constructor(state) {
		this.state = state;
		this.setters = [];
	}
	//internal setters are setters linked to actions that are linked to the workflow
	getInternalSetters() {
		return this.setters.filter(s => s.node.isDescendantOf(this.state.workflow));
	}
	getExternalSetters() {
		return this.setters.filter(s => !s.node.isDescendantOf(this.state.workflow));
	}
	isEmpty() {
		return this.setters.isEmpty();
	}
}

class WorkflowSetter {
	constructor(node) {
		this.node = node;
		this.actions = [];
	}
}

function build_workflow_action_label(workflow, action) {
	//customize display by action
	switch(action.actionId) {
		case 'INITIALIZE_WORKFLOW':
			return 'Initialize workflow';
		case 'CHANGE_STATUS': {
			//find status parameter
			const state_id = action.parameters.find(p => p.id === 'STATUS').value;
			if(!state_id.startsWith('=')) {
				return `Change status to ${workflow.getState(state_id).getLocalizedLabel(Languages.GetLanguage())}`;
			}
			//TODO improve message for formulas
			else {
				return `Change status with formula ${state_id}`;
			}
		}
		default: {
			const action_type = Config.Enums.RuleEntities.WORKFLOW.actions.find(a => a.id === action.actionId);
			const action_parameters = action.parameters.map(p => p.value);
			return `${action_type.label} with parameters ${action_parameters.join(',')}`;
		}
	}
}

function draw_workflow_setter(workflow, setter) {
	const node_li = document.createElement('li');
	node_li.appendChild(document.createTextNode(`${setter.node.getEntity().label} `));
	node_li.appendChild(NodeTools.Draw(setter.node));
	const node_rules = document.createFullElement('ul');
	node_li.appendChild(node_rules);
	setter.actions.map(a => draw_workflow_action(workflow, a)).forEach(Node.prototype.appendChild, node_rules);
	return node_li;
}

function draw_workflow_action(workflow, action) {
	const rule_li = document.createElement('li');
	rule_li.appendChild(NodeTools.Draw(action.rule));
	action.rule.tags.map(t => document.createFullElement('span', {'class': 'tag'}, t)).forEach(Node.prototype.appendChild, rule_li);
	rule_li.appendChild(document.createFullElement('span', {style: 'margin-left: 1rem;'}, build_workflow_action_label(workflow, action)));
	return rule_li;
}

function draw_text_report(workflow, report) {
	const workflow_div = document.createFullElement('div');

	//initialization rules
	if(!report.setters.isEmpty()) {
		workflow_div.appendChild(document.createFullElement('h3', {}, 'Initialization rules'));
		const workflow_initialization_rules_ul = document.createFullElement('ul');
		workflow_div.appendChild(workflow_initialization_rules_ul);
		report.setters.map(s => draw_workflow_setter(workflow, s)).forEach(Node.prototype.appendChild, workflow_initialization_rules_ul);
	}

	//states rules
	workflow.states.forEach(function(state) {
		const workflow_state_title = document.createElement('h3');
		workflow_state_title.appendChild(NodeTools.Draw(state));
		workflow_div.appendChild(workflow_state_title);

		const workflow_state_rules_ul = document.createFullElement('ul');
		workflow_div.appendChild(workflow_state_rules_ul);
		report.getStateReport(state).setters.map(s => draw_workflow_setter(workflow, s)).forEach(Node.prototype.appendChild, workflow_state_rules_ul);
	});

	//unknown rules
	if(!report.unknown.isEmpty()) {
		workflow_div.appendChild(document.createFullElement('h3', {}, 'Unknown rules'));
		const workflow_unknown_rules_ul = document.createFullElement('ul');
		workflow_div.appendChild(workflow_unknown_rules_ul);
		report.unknown.map(s => draw_workflow_setter(workflow, s)).forEach(Node.prototype.appendChild, workflow_unknown_rules_ul);
	}

	return workflow_div;
}

//create relation graph
function draw_graph_report(container, workflow, report) {
	//direct relations link consecutive states together
	const direct_relations = {};
	//jumping relations link non consecutive states
	const jumping_relations = {};
	const all_jumping_relations = [];
	let previous_state;
	workflow.states.forEach(function(state) {
		//initialize variables
		direct_relations[state.id] = [];
		jumping_relations[state.id] = [];

		report.getStateReport(state).getInternalSetters().forEach(function(setter) {
			if(setter.node.getEntity() === Entities.Action && setter.node.workflow === workflow) {
				setter.actions.forEach(function(action) {
					const relation = {action: action, target: state};
					relation.source = workflow.states.find(s => s.possibleActionIds.includes(setter.node.id));
					//if action is from previous state to this state, this is direct relation
					if(previous_state && previous_state === relation.source) {
						direct_relations[state.id].push(relation);
					}
					else {
						jumping_relations[state.id].push(relation);
						all_jumping_relations.push(relation);
					}
				});
			}
		});
		previous_state = state;
	});

	//sort jumping actions from closest to farthest
	//the goal is to make closest actions first because they need to be drawn first (for a better arrows layout)
	all_jumping_relations.sort(function(relation_1, relation_2) {
		const source_index_1 = workflow.states.indexOf(relation_1.source);
		const source_index_2 = workflow.states.indexOf(relation_2.source);
		const target_index_1 = workflow.states.indexOf(relation_1.target);
		const target_index_2 = workflow.states.indexOf(relation_2.target);
		const path_1 = target_index_1 - source_index_1;
		const path_2 = target_index_2 - source_index_2;
		const distance_1 = Math.abs(path_1);
		const distance_2 = Math.abs(path_2);
		//if distances are different, make closest state come first
		if(distance_1 !== distance_2) {
			return distance_1 - distance_2;
		}
		//if both relations have the same length, make the forward relation come first
		if(path_1 < 0 && path_2 > 0) {
			return 1;
		}
		if(path_2 < 0 && path_1 > 0) {
			return -1;
		}
		//if both relations are rollbacks or forwards, make the relation from the first state come first
		return source_index_1 - source_index_2;
	});

	//draw graph
	const middle = 500;
	const state_box_width = 160;
	const gap = 20;
	const arrow_path = 'l -3 -5 l 6 0 Z';

	const svg = SVG.Create({width: `${middle * 2}px`, height: `${report.setters.length * 15 + workflow.states.length * 100 + 50}px`, style: 'margin: 20px;'});
	container.appendChild(svg);

	const workflow_label = SVG.Text(middle, 10, workflow.getLocalizedLabel(Languages.GetLanguage()), {'text-anchor': 'middle'});
	svg.appendChild(workflow_label);

	//draw workflow states boxes
	const states_box = SVG.Element('g');
	svg.appendChild(states_box);

	//create state highlighting listeners
	function state_mouseover() {
		this.classList.add('highlight');
		svg.querySelectorAll(`[data-state-source-id="${this.dataset.stateId}"]`).forEach(g => g.classList.add('highlight'));
	}

	function state_mouseout() {
		this.classList.remove('highlight');
		svg.querySelectorAll(`[data-state-source-id="${this.dataset.stateId}"]`).forEach(g => g.classList.remove('highlight'));
	}

	let y = report.setters.length * 20 + 50;
	workflow.states.forEach(function(state) {
		//add space for arriving relations
		y += Math.max(40, jumping_relations[state.id].length * 30, report.getStateReport(state).setters.length * 15);
		const link = SVG.Link(`#node=${state.getGlobalId()}`, {'data-state-id': state.id});
		link.appendChild(SVG.Rectangle(middle - state_box_width / 2, y, state_box_width, 30));
		const state_text = SVG.Text(middle, y + 20, state.getLocalizedLabel(Languages.GetLanguage()), {'text-anchor': 'middle'});
		link.appendChild(state_text);
		link.addEventListener('mouseover', state_mouseover);
		link.addEventListener('mouseout', state_mouseout);
		states_box.appendChild(link);
		SVG.TextEllipsis(state_text, state_box_width);
		//add space for existing relations
		y += Math.max(40, Array.prototype.concat.apply([], Object.values(jumping_relations)).filter(r => r.source.id === state.id).length * 30);
	});

	//set height attribute for svg as we now know the final height
	svg.setAttribute('height', `${y + 50}px`);

	//create relation highlighting listeners
	function relation_mouseover() {
		//retrieve container
		let container = this;
		while(container.nodeName.toLowerCase() !== 'g' || !container.hasAttribute('data-state-target-id')) {
			container = container.parentNode;
		}
		container.classList.add('highlight');
		states_box.querySelector(`[data-state-id="${container.dataset.stateTargetId}"]`).classList.add('highlight');
		if(container.dataset.stateSourceId) {
			states_box.querySelector(`[data-state-id="${container.dataset.stateSourceId}"]`).classList.add('highlight');
		}
	}

	function relation_mouseout() {
		//retrieve container
		let container = this;
		while(container.nodeName.toLowerCase() !== 'g' || !container.hasAttribute('data-state-target-id')) {
			container = container.parentNode;
		}
		container.classList.remove('highlight');
		states_box.querySelector(`[data-state-id="${container.dataset.stateTargetId}"]`).classList.remove('highlight');
		if(container.dataset.stateSourceId) {
			states_box.querySelector(`[data-state-id="${container.dataset.stateSourceId}"]`).classList.remove('highlight');
		}
	}

	//draw initialization actions
	const relation_box = SVG.Element('g', {'data-state-target-id': workflow.states.first().id});
	svg.appendChild(relation_box);
	//draw line
	const y_stop = parseInt(states_box.querySelector(`[data-state-id="${workflow.states.first().id}"] rect`).getAttribute('y'));
	relation_box.appendChild(SVG.Line(middle, 50, middle, y_stop, {'class': 'relation'}));
	relation_box.appendChild(SVG.Circle(middle, 50, 5, {'class': 'symbol'}));
	report.setters.forEach(function(setter, index) {
		const label_box = SVG.Element('g');
		label_box.addEventListener('mouseover', relation_mouseover);
		label_box.addEventListener('mouseout', relation_mouseout);
		relation_box.appendChild(label_box);
		//draw node
		const link = SVG.Link(`#node=${setter.node.getGlobalId()}`);
		const y = 52 + index * 15;
		link.appendChild(SVG.Text(middle + 13, y, setter.node.getLocalizedLabel(Languages.GetLanguage()), {'text-anchor': 'begin'}));
		label_box.appendChild(link);
		//draw action
		setter.actions.forEach(function(action, action_index) {
			const rule_link = SVG.Link(`#node=${action.rule.getGlobalId()}`);
			label_box.appendChild(rule_link);
			const rule_text = `R${action_index + 1}`;
			const x = link.getBBox().x + link.getBBox().width + 15 + action_index * 20;
			rule_link.appendChild(SVG.Circle(x, y - 3, 8, {'class': 'rule'}));
			rule_link.appendChild(SVG.Text(x, y, rule_text, {'class': 'rule', 'text-anchor': 'middle'}));
			rule_link.appendChild(SVG.Title(action.rule.description));
		});
	});

	//draw direct relations
	workflow.states.forEach(function(state) {
		direct_relations[state.id].forEach(function(relation) {
			const relation_box = SVG.Element('g', {'data-state-source-id': relation.source.id, 'data-state-target-id': state.id});
			svg.appendChild(relation_box);
			const rulable = relation.action.rule.rulable;
			//draw arrow
			const y_start = parseInt(states_box.querySelector(`[data-state-id="${relation.source.id}"] rect`).getAttribute('y')) + 30;
			const y_stop = parseInt(states_box.querySelector(`[data-state-id="${state.id}"] rect`).getAttribute('y'));
			relation_box.appendChild(SVG.Line(middle, y_start, middle, y_stop, {'class': 'relation'}));
			relation_box.appendChild(SVG.Path(middle, y_stop - 1, arrow_path, {'class': 'symbol'}));
			//draw label
			const link = SVG.Link(`#node=${rulable.getGlobalId()}`);
			link.appendChild(SVG.Text(middle - 5, Geometry.Middle(y_stop, y_start), rulable.getLocalizedLabel(Languages.GetLanguage()), {'text-anchor': 'end'}));
			link.appendChild(SVG.Title(relation.action.rule.description));
			link.addEventListener('mouseover', relation_mouseover);
			link.addEventListener('mouseout', relation_mouseout);
			relation_box.appendChild(link);
		});
	});

	//draw jumping relations
	const states_offsets = {};
	let global_offset = middle + 150 + all_jumping_relations.length * 30;
	workflow.states.forEach(function(state) {
		states_offsets[state.id] = {
			bottom: middle + state_box_width / 2,
			below: 0,
			top: middle + state_box_width / 2,
			above: 0
		};
	});
	all_jumping_relations.reverse().forEach(function(relation) {
		const relation_box = SVG.Element('g', {'data-state-source-id': relation.source.id, 'data-state-target-id': relation.target.id});
		svg.appendChild(relation_box);
		const rulable = relation.action.rule.rulable;
		const target_offsets = states_offsets[relation.target.id];
		const source_offsets = states_offsets[relation.source.id];
		//manage source state offsets
		source_offsets.bottom -= gap;
		source_offsets.below += gap;
		//manage target state offsets
		target_offsets.top -= gap;
		target_offsets.above += gap;
		//draw arrow
		global_offset -= 30;
		const y_start = parseInt(states_box.querySelector(`[data-state-id="${relation.source.id}"] rect`).getAttribute('y')) + 30;
		const y_stop = parseInt(states_box.querySelector(`[data-state-id="${relation.target.id}"] rect`).getAttribute('y'));
		relation_box.appendChild(SVG.Polyline(
			[
				source_offsets.bottom, y_start,
				source_offsets.bottom, y_start + source_offsets.below,
				global_offset, y_start + source_offsets.below,
				global_offset, y_stop - target_offsets.above,
				target_offsets.top, y_stop - target_offsets.above,
				target_offsets.top, y_stop
			],
			{'class': 'relation'}
		));
		relation_box.appendChild(SVG.Path(target_offsets.top, y_stop - 1, arrow_path, {'class': 'symbol'}));
		//draw label
		const link = SVG.Link(`#node=${rulable.getGlobalId()}`);
		link.appendChild(SVG.Text(source_offsets.bottom + 2, y_start + source_offsets.below - 5, rulable.getLocalizedLabel(Languages.GetLanguage())));
		link.addEventListener('mouseover', relation_mouseover);
		link.addEventListener('mouseout', relation_mouseout);
		relation_box.appendChild(link);
	});

	//draw external relations
	workflow.states.forEach(function(state) {
		//external relations are relations that have not been identified as direct or jumping relations
		const state_external_setters = report.getStateReport(state).getExternalSetters();
		if(!state_external_setters.isEmpty()) {
			const relation_box = SVG.Element('g', {'data-state-target-id': state.id});
			svg.appendChild(relation_box);
			//draw arrow
			const y = parseInt(states_box.querySelector(`[data-state-id="${state.id}"] rect`).getAttribute('y'));
			const x = middle - state_box_width / 2 + 20;
			relation_box.appendChild(SVG.Polyline(
				[
					middle - 120, y - 15,
					x, y - 15,
					x, y
				],
				{'class': 'relation'}
			));
			relation_box.appendChild(SVG.Path(x, y - 1, arrow_path, {'class': 'symbol'}));
			//draw labels
			state_external_setters.forEach(function(setter, index) {
				const label_box = SVG.Element('g');
				label_box.addEventListener('mouseover', relation_mouseover);
				label_box.addEventListener('mouseout', relation_mouseout);
				relation_box.appendChild(label_box);
				//draw node
				const link = SVG.Link(`#node=${setter.node.getGlobalId()}`);
				const label_y = y - index * 15 - 12;
				link.appendChild(SVG.Text(middle - 125, label_y, setter.node.getLocalizedLabel(Languages.GetLanguage()), {'text-anchor': 'end'}));
				label_box.appendChild(link);
				//draw action
				const actions_length = setter.actions.length;
				setter.actions.forEach(function(action, action_index) {
					const rule_link = SVG.Link(`#node=${action.rule.getGlobalId()}`);
					label_box.appendChild(rule_link);
					const rule_text = `R${actions_length - action_index}`;
					const x = link.getBBox().x - 15 - action_index * 20;
					rule_link.appendChild(SVG.Circle(x, label_y - 3, 8, {'class': 'rule'}));
					rule_link.appendChild(SVG.Text(x, label_y, rule_text, {'class': 'rule', 'text-anchor': 'middle'}));
					rule_link.appendChild(SVG.Title(action.rule.description));
				});
			});
		}
	});
}

let selected_workflow;

export default {
	form: 'edit_workflow_form',
	init: function() {
		document.getElementById('edit_workflow_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);

		document.getElementById('workflow_aggregate_workflow_id').addEventListener('change', manage_aggregate_workflow);
		document.getElementById('workflow_mandatory').addEventListener('change', manage_mandatory);

		document.getElementById('workflow_report_rules_download').addEventListener(
			'click',
			function() {
				const report = WorkflowReport.fromWorkflow(selected_workflow);
				const text_report = [];
				text_report.push(`# ${selected_workflow.getLocalizedLabel(Languages.GetLanguage())}`);

				function draw_workflow_setter(workflow, setter) {
					const text_report_setter = [];
					text_report_setter.push(`* ${setter.node.getEntity().label} ${setter.node.getLocalizedLabel(Languages.GetLanguage())}`);
					text_report_setter.pushAll(setter.actions.map(a => draw_workflow_action(workflow, a)).map(t => `\t- ${t}`));
					return text_report_setter;
				}

				function draw_workflow_action(workflow, action) {
					let text_report_action = action.rule.getLocalizedLabel(Languages.GetLanguage());
					text_report_action += ' - ';
					text_report_action += build_workflow_action_label(workflow, action);
					return text_report_action;
				}

				//initialization rules
				if(!report.setters.isEmpty()) {
					text_report.push('## Initialization rules');
					report.setters.map(s => draw_workflow_setter(selected_workflow, s)).forEach(Array.prototype.pushAll, text_report);
				}

				//states rules
				selected_workflow.states.forEach(function(state) {
					text_report.push(`## ${state.getLocalizedLabel(Languages.GetLanguage())}`);
					report.getStateReport(state).setters.map(s => draw_workflow_setter(selected_workflow, s)).forEach(Array.prototype.pushAll, text_report);
				});

				//unknown rules
				if(!report.unknown.isEmpty()) {
					text_report.push('## Unknown rules');
					report.unknown.map(s => draw_workflow_setter(selected_workflow, s)).forEach(Array.prototype.pushAll, text_report);
				}

				const blob = new Blob([text_report.join('\n')], {type: 'text/markdown;charset=utf-8'});
				const filename = `${selected_workflow.id.toLowerCase()}_rules_report.md`;
				const file = new File([blob], filename, {type: 'image/octet-stream', lastModified: Date.now()});
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
			}
		);

		document.getElementById('workflow_report_diagram_download').addEventListener(
			'click',
			function() {
				const original_svg = document.querySelector('#workflow_report_diagram svg');
				//duplicate svg to be able to add custom styling
				const svg = /**@type {SVGElement}*/ (original_svg.cloneNode(true));
				const file_diagram = document.getElementById('workflow_report_file_diagram');
				file_diagram.empty();
				file_diagram.appendChild(svg);
				svg.setAttribute('style', 'background-color: white;');

				//copy some CSS rules as a style attribute
				const interesting_rules = ['font-size', 'stroke', 'stroke-width', 'fill'];
				//query selector preserves order
				const svg_nodes = svg.querySelectorAll('*');
				for(let i = 0; i < svg_nodes.length; i++) {
					const node = svg_nodes[i];
					const rules = getComputedStyle(node);
					let style = '';
					for(let j = 0; j < interesting_rules.length; j++) {
						const key = interesting_rules[j];
						const property_value = rules.getPropertyValue(key);
						const style_value = property_value.replace(/"/g, '\'');
						style += (`${key}: ${style_value};`);
					}
					node.setAttribute('style', style);
				}

				const svg_data = (new XMLSerializer()).serializeToString(svg);
				const svg_blob = new Blob([svg_data], {type: 'image/svg+xml;charset=utf-8'});
				const svg_url = window.URL.createObjectURL(svg_blob);

				function image_loaded() {
					canvas.width = image.width;
					canvas.height = image.height;
					context.drawImage(image, 0, 0);
					window.URL.revokeObjectURL(svg_url);
					canvas.toBlob(blob_generated, 'image/png');
				}

				function blob_generated(blob) {
					const filename = `${selected_workflow.id.toLowerCase()}_diagram_report.png`;
					const file = new File([blob], filename, {type: 'image/octet-stream', lastModified: Date.now()});
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
				}

				const canvas = /**@type {HTMLCanvasElement}*/ (document.getElementById('workflow_report_file_canvas'));
				const context = canvas.getContext('2d');
				const image = new Image();
				image.addEventListener('load', image_loaded);
				image.src = svg_url;
			}
		);
	},
	open: function(workflow, tab) {
		selected_workflow = workflow;

		switch(tab) {
			case 'edit_workflow_report': {
				//draw report
				const workflow_report_rules = document.getElementById('workflow_report_rules');
				workflow_report_rules.empty();

				const workflow_report_diagram = document.getElementById('workflow_report_diagram');
				workflow_report_diagram.empty();

				const workflow_report_diagram_download = document.getElementById('workflow_report_diagram_download');

				const report = WorkflowReport.fromWorkflow(workflow);
				//check if rule actions have been found for this workflow
				if(!report.isEmpty()) {
					workflow_report_rules.appendChild(draw_text_report(workflow, report));
					draw_graph_report(workflow_report_diagram, workflow, report);
					workflow_report_diagram_download.style.display = 'inline';
				}
				else {
					workflow_report_diagram_download.style.display = 'none';
				}
				break;
			}
			default: {
				FormHelpers.FillSelect(document.getElementById('workflow_initial_state_id'), workflow.states, true);
				FormHelpers.FillSelect(document.getElementById('workflow_action_id'), workflow.actions, true);
				FormHelpers.FillSelect(document.getElementById('workflow_aggregate_workflow_id'), workflow.study.workflows.filter(w => w !== workflow && w.aggregateWorkflowId === undefined), true);
				FormHelpers.FillLocalizedInput(document.getElementById('workflow_shortname'), workflow.study.languages);
				FormHelpers.FillLocalizedInput(document.getElementById('workflow_longname'), workflow.study.languages);
				FormHelpers.FillLocalizedInput(document.getElementById('workflow_description'), workflow.study.languages);
				FormHelpers.FillLocalizedInput(document.getElementById('workflow_message'), workflow.study.languages);
				FormHelpers.UpdateForm(document.getElementById('edit_workflow_form'), workflow);

				manage_mandatory();
				manage_aggregate_workflow();
				NodeTools.DrawUsage(workflow, document.getElementById('workflow_usage'));

				FormStaticActions.DrawRules(workflow, workflow.rules, workflow.constructor.RuleEntities, document.getElementById('edit_workflow_rules'));
			}
		}
	},
	close: function(workflow, tab) {
		switch(tab) {
			case 'edit_workflow_rules':
			case 'edit_workflow_report':
				return [];
			default:
				return document.getElementById(this.form).getUnsavedData(workflow);
		}
	}
};
