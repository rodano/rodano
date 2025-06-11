import './basic-tools/extension.js';

import {Hash} from './basic-tools/hash.js';
import {UI} from './tools/ui.js';
import {Config} from './model_config.js';
import {Languages} from './languages.js';
import {bus_ui} from './bus_ui.js';
import {NodeTools} from './node_tools.js';
import {StudyHandler} from './study_handler.js';
import {StudyTree} from './study_tree.js';
import {Layout} from './layout.js';
import {Dashboards} from './dashboards.js';
import {Rule} from './rule.js';
import {RuleDefinitions} from './rule_definitions.js';
import {Comparator} from './comparator.js';
import {Templates} from './templates.js';
import {Changelogs} from './changelogs.js';
import {DocumentationSelection} from './documentation_selection.js';
import {Hooks} from './hooks.js';
import {ContextualMenu} from './contextual_menu.js';
import {Matrices} from './matrices.js';
import {Reports} from './reports.js';
import {EntitiesForms} from './entities_forms.js';
import {FormStaticActions} from './form_static_actions.js';
import {EntitiesPlaceholders} from './entities_placeholders.js';
import {Entities} from './model/config/entities.js';
import {Importer} from './importer.js';
import {Attributables, RightAssignables} from './model/config/entities_categories.js';

function hashchange(data) {
	const study = StudyHandler.GetStudy();
	//close open dialogs
	UI.CloseDialogs();
	//node
	if(data.hasOwnProperty('node')) {
		//retrieve node
		try {
			const node = study.getNode(data.node);
			//entity
			if(data.hasOwnProperty('entity')) {
				Router.SelectEntity(node, Entities[data.entity]);
			}
			//section
			else if(data.hasOwnProperty('section')) {
				//conditions
				if(data.section === 'condition') {
					//node constraint may not exist (do not forget than a bookmarked link can land here)
					if(node.constraint) {
						Router.selectedNode = undefined;
						Router.SelectConstraint(node, node.constructor.RuleEntities, node.constraint);
					}
					else {
						//fallback on selecting node
						Router.SelectNode(node, data.tab);
						UI.Notify('Unable to select conditions', {
							tag: 'navigation',
							icon: 'images/notifications_icons/error.svg',
							body: 'You may need to reload the page.'
						});
					}
				}
			}
			else {
				Router.SelectNode(node, data.tab);
			}
		}
		catch(error) {
			console.error(error);
			//node cannot be found in study
			UI.Notify('Unable to select node', {
				tag: 'navigation',
				icon: 'images/notifications_icons/error.svg',
				body: 'You may need to reload the page.'
			});
			Router.SelectHelp();
		}
	}
	//trigger
	else if(data.hasOwnProperty('trigger')) {
		Router.SelectTrigger(data.trigger);
	}
	//matrices
	else if(data.hasOwnProperty('matrix')) {
		const entity = Entities[data.entity];
		Router.SelectMatrix(entity);
	}
	//reports
	else if(data.hasOwnProperty('report')) {
		const report = data.report;
		//retrieve parameters, which are the rest of the hash
		delete data.report;
		Router.SelectReport(report, data);
	}
	//tools
	else if(data.hasOwnProperty('tool')) {
		Router.SelectTool(data.tool);
	}
	//help otherwise
	else {
		Router.SelectHelp();
	}
}

let prevent_hashchange = false;
let navigation_timeout;

//TODO rewrite this
let selected_tab;

function draw_value(value) {
	if(value === undefined) {
		return document.createFullElement('span', {style: 'opacity: 0.8; font-style: italic;'}, 'undefined');
	}
	if(value === '') {
		return document.createFullElement('span', {style: 'opacity: 0.8; font-style: italic;'}, 'blank');
	}
	return document.createFullElement('span', {}, value);
}

function draw_unsaved_data(data) {
	const element = document.createElement('li');
	element.appendChild(document.createTextNode(`Property ${data.element.name} changed from `));
	element.appendChild(draw_value(data.objectValue));
	element.appendChild(document.createTextNode(' to '));
	element.appendChild(draw_value(data.formValue));
	return element;
}

export const Router = {
	selectedNode: undefined, //current selected node
	selectedOverlayNode: undefined, //current selected node in overlay panel
	GetSelectedNode: function() {
		return Router.selectedOverlayNode || Router.selectedNode;
	},
	Init: function() {
		window.addEventListener(
			'hashchange',
			function(event) {
				//if hash changes, reset all pending navigation
				clearTimeout(navigation_timeout);
				//retrieve data encoded in hash
				const data = Hash.Decode(location.hash);
				//unload current view
				if(!prevent_hashchange && event.oldURL) {
					const hash = event.oldURL.substring(event.oldURL.indexOf('#'));
					//retrieve data encoded in hash
					const old_data = Hash.Decode(hash);
					if(Router.selectedNode && old_data.hasOwnProperty('node') && !old_data.hasOwnProperty('entity')) {
						const entity = Router.selectedNode.getEntity();
						//if any, execute node specific operations
						if(EntitiesForms.HasForm(entity)) {
							const unsaved_data = EntitiesForms.Close(entity, Router.selectedNode, selected_tab);
							//node must be identifiable with a label
							if(!Router.selectedNode.getLocalizedLabel(Languages.GetLanguage())) {
								const entity_label = entity.label.toLowerCase();
								UI.Validate(
									`You cannot keep a ${entity_label} without a label. You must add a label or delete the ${entity_label}.`,
									`Delete ${entity_label}`,
									`Continue editing the ${entity_label}`
								).then(confirmed => {
									//user decided to leave the page, delete the node
									if(confirmed) {
										Router.selectedNode.delete();
									}
									//user decided to stay on the page, restore previous hash
									else {
										prevent_hashchange = true;
										location.hash = hash;
									}
								});
								return;
							}
							else if(!unsaved_data.isEmpty()) {
								const entity_label = entity.label.toLowerCase();
								const unsaved_ui = document.createFullElement('div');
								unsaved_ui.appendChild(document.createFullElement('p', {}, 'There is unsaved data in current page:'));
								const unsaved_list = document.createFullElement('ul', {style: 'list-style-type: disc; padding: 0 2rem;'});
								unsaved_ui.appendChild(unsaved_list);
								unsaved_data.map(draw_unsaved_data).forEach(Node.prototype.appendChild, unsaved_list);
								unsaved_ui.appendChild(document.createFullElement('p', {}, 'Are you sure you want to leave this page and discard modifications?'));
								UI.Validate(
									unsaved_ui,
									'Leave page and discard modifications',
									`Continue editing the ${entity_label}`
								).then(confirmed => {
									//user decided to leave the page, do stuff according to new hash
									if(confirmed) {
										hashchange(data);
									}
									//user decided to stay on the page, restore previous hash
									else {
										prevent_hashchange = true;
										location.hash = hash;
									}
								});
								return;
							}
						}
					}
				}
				hashchange(data);
				prevent_hashchange = false;
			}
		);

		bus_ui.register({
			onDelete: function(event) {
				//reset ui if needed
				if(Router.selectedNode === event.node) {
					Router.Reset();
				}
			}
		});
	},
	//this allows to navigate to a special hash with a delay
	//this is used by the tree where focusing an element selects the element but after a small delay
	NavigateTo: function(hash, delay) {
		if(navigation_timeout) {
			clearTimeout(navigation_timeout);
		}
		//do not call the router directly but simulate navigation instead
		//this allows the router to work properly (especially with the "unsaved data" popup)
		navigation_timeout = setTimeout(() => location.hash = hash, delay);
	},
	Clean: function() {
		//hide edition
		const edition = document.getElementById('edition');
		edition.children.forEach(e => e.style.display = 'none');
		edition.style.display = 'none';
		//hide edition
		const edition_overlay = document.getElementById('edition_overlay');
		edition_overlay.children.forEach(e => e.style.display = 'none');
		//hide nodes
		const nodes = document.getElementById('nodes');
		nodes.querySelectorAll('.content > *').forEach(e => e.style.display = 'none');
		nodes.style.display = 'none';
		//hide matrix
		const matrix = document.getElementById('matrix');
		matrix.empty();
		matrix.style.display = 'none';
		//hide layout
		document.getElementById('layout').style.display = 'none';
		//hide rules
		document.getElementById('rule').style.display = 'none';
		//hide trigger
		document.getElementById('trigger').style.display = 'none';
		//hide help
		document.getElementById('help').style.display = 'none';
	},
	Reset: function() {
		Router.selectedNode = undefined;
		Router.selectedOverlayNode = undefined;
		Router.Clean();
	},
	SelectHelp: function() {
		Router.Reset();
		document.getElementById('help').style.display = 'block';
		//push state if necessary
		if(location.hash !== '' && location.hash !== '#') {
			history.pushState({}, 'KVConfig', '#');
		}
	},
	SelectNode: function(wished_node, tab) {
		const study = StudyHandler.GetStudy();
		//find editable node and overlay node
		//editable node is the first ancestor which has an edition form
		//overlay node is displayed over a regular node and is not displayed in the tree
		let node = wished_node;
		let overlay_node;
		while(!StudyTree.HasNode(node) && ![Entities.Layout, Entities.Column, Entities.Cell, Entities.Rule, Entities.RuleConstraint, Entities.CMSLayout].includes(node.getEntity())) {
			const entity = node.getEntity();
			if(!overlay_node && entity.id && EntitiesForms.HasForm(entity)) {
				overlay_node = node;
			}
			node = node.getParent();
		}

		//clean page and prepare display
		Router.Clean();

		//select closest node in tree
		//tree node is the first ancestor which is displayed on the tree
		StudyTree.SelectClosestNode(node);

		//decide if the page must me reloaded
		//the first time a node is selected, information on the page must be refreshed
		//if the node is already selected, restore the display to show the good page
		let reload = false;
		if(Router.selectedNode !== node || selected_tab !== tab) {
			//update selected node and selected tab
			Router.selectedNode = node;
			selected_tab = tab;
			//store that the page must be reloaded
			reload = true;
		}

		switch(node.getEntity().name) {
			case Entities.Rule.name:
				document.getElementById('rule').style.display = 'block';
				if(reload) {
					let entities, title_text;
					//retrieve title and entities depending on rulable entity
					if(node.rulable.getEntity() === Entities.Study) {
						//retrieve trigger
						let trigger_id;
						for(trigger_id in study.eventActions) {
							if(study.eventActions.hasOwnProperty(trigger_id)) {
								if(study.eventActions[trigger_id].includes(node)) {
									break;
								}
							}
						}
						title_text = Config.Enums.Trigger[trigger_id].shortname[Languages.GetLanguage()];
						entities = Config.Enums.Trigger[trigger_id].rule_entities;
					}
					else {
						title_text = 'Rule';
						entities = node.rulable.constructor.RuleEntities;
					}

					NodeTools.UpdateTitle(document.querySelector('#rule h2'), node.rulable, undefined, title_text);
					Rule.Draw(node, entities);
				}
				break;
			case Entities.RuleConstraint.name:
				document.getElementById('rule').style.display = 'block';
				if(reload) {
					NodeTools.UpdateTitle(document.querySelector('#rule h2'), node.constrainable, undefined, 'Constraint');
					Rule.DrawConstraint(node, node.constrainable.constructor.RuleEntities);
				}
				break;
			case Entities.CMSLayout.name:
				document.getElementById('layout').style.display = 'block';
				if(reload) {
					NodeTools.UpdateTitle(document.querySelector('#layout h2'), node.layoutable, undefined, 'Layout');
					Layout.Draw(node);
				}
				break;
			default:
				EntitiesForms.Open(node.getEntity(), node, tab, reload);
		}

		//do additional stuff for overlay nodes
		if(overlay_node) {
			const entity = overlay_node.getEntity();
			//manage overlay node selection i.e. the first time an overlay node is selected
			if(overlay_node !== Router.selectedOverlayNode) {
				//select node
				Router.selectedOverlayNode = overlay_node;

				//load form if any and execute node specific operations
				if(EntitiesForms.HasForm(entity)) {
					EntitiesForms.OpenOverlay(node, entity, overlay_node);
				}
			}
		}
		else {
			Router.selectedOverlayNode = undefined;
			UI.CloseDialogs();
		}

		//generate state
		const state = {node: wished_node.getGlobalId()};
		if(tab) {
			state.tab = tab;
		}
		const hash = Hash.Encode(state);
		//push state if necessary
		if(location.hash !== hash) {
			history.pushState(state, `KVConfig ${wished_node.id}`, hash);
		}
	},
	SelectTrigger: function(trigger_id) {
		const study = StudyHandler.GetStudy();
		//unselect any node
		Router.selectedNode = undefined;
		const trigger = Config.Enums.Trigger[trigger_id];

		//create event action rules if necessary
		if(!study.eventActions[trigger_id]) {
			study.eventActions[trigger_id] = [];
		}

		//clean ui
		Router.Clean();
		const trigger_ui = document.getElementById('trigger');

		//update title
		NodeTools.UpdateTitle(trigger_ui.querySelector('h2'), study, undefined, trigger.shortname[Languages.GetLanguage()]);

		//draw rules list
		FormStaticActions.DrawRules(study, study.eventActions[trigger_id], trigger.rule_entities, document.getElementById('trigger_rules'));
		trigger_ui.style.display = 'block';

		//generate state
		const state = {trigger: trigger_id};
		const hash = Hash.Encode(state);
		//push state if necessary
		if(location.hash !== hash) {
			history.pushState(state, `KVConfig ${trigger_id}`, hash);
		}
	},
	SelectEntity: async function(node, entity) {
		//select node in tree
		const tree_node = StudyTree.GetTree().find(node);
		tree_node.childEntities.find(e => e.entity === entity).highlight();

		//clean page
		Router.Reset();

		//update title
		NodeTools.UpdateTitle(document.querySelector('#nodes h2'), node, undefined, entity.plural_label);

		//display nodes section before loading the content to allow SVG content to work properly
		document.getElementById('nodes').style.display = 'block';

		//try to load custom placeholder
		if(EntitiesPlaceholders.HasForm(entity)) {
			await EntitiesPlaceholders.Open(entity, node);
		}
		else {
			//fallback to a simple list of all child nodes for the entity
			node.getChildren(entity).map(node => {
				const node_li = document.createElement('li');
				const node_link = NodeTools.Draw(node, undefined, true);
				node_link.addEventListener('contextmenu', event => {
					event.preventDefault();
					ContextualMenu.OpenNodeMenu(event, node);
				});
				node_li.appendChild(node_link);
				return node_li;
			}).forEach(Node.prototype.appendChild, document.getElementById('nodes_child_nodes').empty());
			document.getElementById('nodes_child_nodes').style.display = 'block';
		}

		//generate state
		const state = {node: node.getGlobalId(), entity: entity.name};
		const hash = Hash.Encode(state);
		//push state if necessary
		if(location.hash !== hash) {
			history.pushState(state, 'KVConfig', hash);
		}
	},
	SelectMatrix: function(entity) {
		//clean page
		Router.Reset();

		const study = StudyHandler.GetStudy();

		const matrix = document.getElementById('matrix');
		matrix.style.display = 'block';
		let content;
		if(Attributables.includes(entity)) {
			content = Matrices.DrawAttributableMatrix(study, entity);
		}
		else if(RightAssignables.includes(entity)) {
			content = Matrices.DrawRightAssignableMatrix(study, entity);
		}
		else {
			content = Matrices.DrawAssignableMatrix(study, entity);
		}
		matrix.appendChild(content);

		//generate state
		const state = {matrix: 'profile', entity: entity.name};
		const hash = Hash.Encode(state);
		//push state if necessary
		if(location.hash !== hash) {
			history.pushState(state, `KVConfig - ${entity.label} matrix`, hash);
		}
	},
	SelectReport: function(report, parameters) {
		const study = StudyHandler.GetStudy();
		switch(report) {
			case 'consistency_check' : Reports.ShowConsistencyCheck(study); break;
			case 'event_models_form_models' : Reports.DrawEventModelsFormModels(study); break;
			case 'workflows_field_models' : Reports.DrawWorkflowsFieldModels(study); break;
			case 'rules_dashboard' : Dashboards.ShowRules(study, parameters); break;
			case 'constraints_dashboard' : Dashboards.ShowConstraints(study); break;
		}
		//generate state
		const state = Object.assign({report: report}, parameters);
		const hash = Hash.Encode(state);
		//push state if necessary
		if(location.hash !== hash) {
			history.pushState(state, 'KVConfig', hash);
		}
	},
	SelectTool: function(tool) {
		switch(tool) {
			case 'rule_definitions' : RuleDefinitions.Open(); break;
			case 'comparator' : Comparator.Compare(); break;
			case 'templates' : Templates.OpenManageDialog(); break;
			case 'changelog' : Changelogs.Open(); break;
			case 'selection' : DocumentationSelection.Open(); break;
			case 'hooks' : Hooks.Open(); break;
			case 'importer' : Importer.Open(); break;
		}
	},
	CloseReport: function() {
		const data = Hash.Decode(window.location.hash);
		delete data.report;
		window.location.href = Hash.Encode(data);
	},
	CloseTool: function() {
		const data = Hash.Decode(window.location.hash);
		delete data.tool;
		window.location.href = Hash.Encode(data);
	}
};

/*
window.addEventListener(
	'popstate',
	function(event) {
		if(event.state) {}
	}
);
*/
