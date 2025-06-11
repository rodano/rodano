import './tools/custom_extension.js';

import {UI} from './tools/ui.js';
import {Effects} from './tools/effects.js';
import {ConfigHelpers} from './model_config.js';
import {bus} from './model/config/entities_hooks.js';
import {Languages} from './languages.js';
import {BusEventSaveNode, bus_ui} from './bus_ui.js';
import {Router} from './router.js';
import {StudyTree} from './study_tree.js';
import {NodeTools} from './node_tools.js';
import {FormHelpers} from './form_helpers.js';
import {RuleConstraint} from './model/config/entities/rule_constraint.js';
import {CMSLayout} from './model/config/entities/cms_layout.js';
import {CMSSection} from './model/config/entities/cms_section.js';
import {Rule} from './model/config/entities/rule.js';

//layout
function create_blank_layout(layoutable) {
	const layout = new CMSLayout();
	layout.layoutable = layoutable;
	const section = new CMSSection({id: 'SECTION_1'});
	section.layout = layout;
	layout.sections.push(section);
	return layout;
}

export const FormStaticActions = {
	Init: function() {
		//node tabs
		function bus_register() {
			bus.register({
				onChange: function(event) {
					if(event.property === 'id' && event.node === Router.selectedNode) {
						//manage editor
						const editor = document.getElementById(`edit_${event.node.getEntity().id}`);
						//manage tab
						const tabs = editor.querySelector('ul.tabs');
						if(tabs) {
							const node_global_id = event.node.getGlobalId();
							//update tabs links
							tabs.children.forEach(function(item) {
								const link = item.firstElementChild;
								link.setAttribute('href', `#node=${node_global_id}&tab=${link.dataset.tab}`);
							});
						}
					}
				},
			});
		}
		//register hook when a study is loaded because the bus will be reset at that time
		bus_ui.register({onLoadStudy: bus_register});
		bus_register();
	},
	//manage generic actions on forms
	EnhanceForm: function(root) {
		//allow copy from one property to an other
		root.querySelectorAll('.copy_property').forEach(function(element) {
			element.addEventListener(
				'click',
				function() {
					const destination = document.getElementById(this.dataset.destination);
					destination.value = document.getElementById(this.dataset.source).value;
					//trigger event on destination input
					const event = new UIEvent('change', {bubbles: true, cancelable: true});
					destination.dispatchEvent(event);
				}
			);
		});
		//generate automatically id from shortname
		const id = root.querySelector('input[name="id"]');
		const shortname = root.querySelector('app-localized-input[name="shortname"]');
		//displayable may not have its field
		if(id && shortname) {
			shortname.addEventListener(
				'change',
				function() {
					if(!id.value) {
						id.value = this.getValues()[Languages.GetLanguage()].idify();
					}
				}
			);
		}
	},
	CheckId: function(node, id) {
		//a node must have a unique id among its siblings
		const sibling_ids = node.getSiblings().map(s => s.id);
		if(sibling_ids.includes(id)) {
			UI.Notify('Invalid id', {
				tag: 'error',
				icon: 'images/notifications_icons/warning.svg',
				body: `Unable to save. Id ${id} already exists.`
			});
			return false;
		}
		return true;
	},
	SubmitEditionForm: function(event) {
		event.stop();
		if(FormStaticActions.CheckId(Router.selectedNode, this['id'].value)) {
			FormHelpers.UpdateObject(Router.selectedNode, this);
			FormStaticActions.AfterSubmission(Router.selectedNode);
		}
	},
	AfterSubmission: function(node) {
		//update tree (root scope does not have any parent)
		if(StudyTree.HasNode(node) && node.hasParent()) {
			StudyTree.GetTree().find(node).parent.sort();
		}
		const entity = node.getEntity();
		//send event on bus
		bus_ui.dispatch(new BusEventSaveNode(entity.name, node));
		//notify
		const node_label = node.getLocalizedLabel(Languages.GetLanguage());
		UI.Notify('Modifications saved', {
			tag: 'info',
			icon: 'images/notifications_icons/done.svg',
			body: `${entity.label} ${node_label ? node_label.toLowerCase() : ''} updated.`
		});
	},
	//layout
	ManageLayoutEdition: function(add_button, edit_button, delete_button) {
		add_button.addEventListener('click', function(event) {
			event.preventDefault();
			//update model
			Router.GetSelectedNode().layout = create_blank_layout(Router.GetSelectedNode());
			//update ui
			delete_button.style.display = '';
			add_button.style.display = 'none';
			edit_button.setAttribute('href', `#node=${Router.GetSelectedNode().layout.getGlobalId()}`);
			edit_button.style.display = '';
			edit_button.click();
		});

		delete_button.addEventListener('click', function(event) {
			event.stop();
			UI.Validate('Are you sure you want to delete layout?').then(confirmed => {
				if(confirmed) {
					//update model
					Router.GetSelectedNode().layout = undefined;
					//update ui
					delete_button.style.display = 'none';
					add_button.style.display = '';
					edit_button.style.display = 'none';
				}
			});
		});

		bus_ui.register({
			onChange: (event) => {
				//link must be updated when the id of the node is updated
				if(event.node === Router.GetSelectedNode() && event.property === 'id') {
					FormStaticActions.UpdateLayoutEdition(event.node, add_button, edit_button, delete_button);
				}
			}
		});
	},
	UpdateLayoutEdition: function(node, add_button, edit_button, delete_button) {
		if(node.layout) {
			edit_button.setAttribute('href', `#node=${node.layout.getGlobalId()}`);
			edit_button.style.display = '';
			add_button.style.display = 'none';
			delete_button.style.display = '';
		}
		else {
			add_button.style.display = '';
			edit_button.style.display = 'none';
			delete_button.style.display = 'none';
		}
	},
	//constraint
	ManageConstraintEdition: function(property, add_button, edit_button, delete_button) {
		add_button.addEventListener('click', function(event) {
			event.preventDefault();
			//update model
			const node = Router.GetSelectedNode();
			node[property] = new RuleConstraint({constrainable: node});
			//update ui
			delete_button.style.display = '';
			add_button.style.display = 'none';
			edit_button.setAttribute('href', `#node=${node[property].getGlobalId()}`);
			edit_button.style.display = '';
			edit_button.click();
		});

		delete_button.addEventListener('click', function(event) {
			event.stop();
			UI.Validate('Are you sure you want to delete constraint?').then(confirmed => {
				if(confirmed) {
					//update model
					Router.GetSelectedNode()[property] = undefined;
					//update ui
					delete_button.style.display = 'none';
					add_button.style.display = '';
					edit_button.style.display = 'none';
				}
			});
		});

		bus_ui.register({
			onChange: (event) => {
				//link must be updated when the id of the node is updated
				if(event.node === Router.GetSelectedNode() && event.property === 'id') {
					FormStaticActions.UpdateConstraintEdition(event.node, property, add_button, edit_button, delete_button);
				}
			}
		});
	},
	UpdateConstraintEdition: function(node, property, add_button, edit_button, delete_button) {
		if(node[property]) {
			edit_button.setAttribute('href', `#node=${node[property].getGlobalId()}`);
			edit_button.style.display = '';
			add_button.style.display = 'none';
			delete_button.style.display = '';
		}
		else {
			add_button.style.display = '';
			edit_button.style.display = 'none';
			delete_button.style.display = 'none';
		}
	},
	//rules
	DrawRules: function(rulable, rules, entities, container, title) {

		function draw_rule(rule) {
			const rule_li = document.createElement('li');
			rule_li.rule = rule;

			//edit
			rule_li.appendChild(NodeTools.Draw(rule));

			//delete
			const node_rule_delete = document.createFullElement('img', {src: 'images/cross.png', alt: 'Delete rule', title: 'Delete rule'});
			node_rule_delete.addEventListener(
				'click',
				function(event) {
					event.stopPropagation();
					UI.Validate('Are you sure you want to delete this rule?').then(confirmed => {
						if(confirmed) {
							//update model
							this.parentNode.rule.delete();
							//update ui
							this.parentNode.parentNode.removeChild(this.parentNode);
						}
					});
				}
			);
			rule_li.appendChild(node_rule_delete);

			//duplicate
			const node_rule_duplicate = document.createFullElement('img', {src: 'images/page_white_copy.png', alt: 'Duplicate rule', title: 'Duplicate rule'});
			node_rule_duplicate.addEventListener(
				'click',
				function() {
					const rule = ConfigHelpers.CloneNode(this.parentNode.rule, {
						rulable: this.parentNode.rule.rulable,
						description: `${this.parentNode.rule.description} (copy)`
					});
					rules.push(rule);
					Router.SelectNode(rule);
					rule_list.appendChild(draw_rule(rule));
				}
			);
			rule_li.appendChild(node_rule_duplicate);

			//sort
			rule_li.appendChild(document.createFullElement('img', {src: 'images/arrows_up_down.png', alt: 'Sort rule', title: 'Sort rule'}));

			return rule_li;
		}

		container.empty();
		//title
		container.appendChild(document.createFullElement('h3', {}, title || 'Rules'));
		//list
		const rule_list = document.createFullElement('ul');
		rules.map(draw_rule).forEach(Node.prototype.appendChild, rule_list);
		container.appendChild(rule_list);

		//sort
		Effects.Sortable(
			rule_list,
			function() {
				rules.length = 0;
				rules.pushAll(rule_list.children.map(c => c.rule));
				//update rule links
				rule_list.children.forEach(function(rule_li) {
					NodeTools.UpdateLink(rule_li.querySelector('a'), rule_li.rule);
				});
			},
			'img:last-child'
		);
		//button
		const add_rule_button = document.createFullElement('button', {type: 'button'}, 'Add rule');
		add_rule_button.addEventListener(
			'click',
			function(event) {
				event.preventDefault();
				const rule = new Rule({
					rulable: rulable,
					description: `Rule ${rules.length + 1}`
				});
				rule.constraint = new RuleConstraint({constrainable: rule});
				rules.push(rule);
				Router.SelectNode(rule);
				rule_list.appendChild(draw_rule(rule));
			}
		);
		container.appendChild(add_rule_button);
	}
};
