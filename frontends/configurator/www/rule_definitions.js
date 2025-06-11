import {UI} from './tools/ui.js';
import {Config} from './model_config.js';
import {Entities} from './model/config/entities.js';
import {StudyHandler} from './study_handler.js';
import {Router} from './router.js';
import {RuleDefinitionProperty} from './model/config/entities/rule_definition_property.js';
import {RuleDefinitionAction} from './model/config/entities/rule_definition_action.js';

function select_entity_listener() {
	if(!this.classList.contains('selected')) {
		select_entity(Config.Enums.RuleEntities[this.dataset.ruleEntity]);
	}
}

function select_entity(entity) {
	document.querySelectorAll('#rule_definitions_entities > li').forEach(function(tab) {
		if(tab.dataset.ruleEntityId === entity.name) {
			tab.classList.add('selected');
		}
		else {
			tab.classList.remove('selected');
		}
	});

	//update buttons with selected entity
	document.getElementById('rule_definitions_custom_property_add').dataset.ruleEntity = entity.name;
	document.getElementById('rule_definitions_custom_action_add').dataset.ruleEntity = entity.name;

	//draw definitions
	const study = StudyHandler.GetStudy();
	study.getRuleDefinitionProperties(entity)
		.map(draw_definition)
		.forEach(Node.prototype.appendChild, document.getElementById('rule_definitions_custom_properties').empty());
	study.getRuleDefinitionActions(entity)
		.map(draw_definition)
		.forEach(Node.prototype.appendChild, document.getElementById('rule_definitions_custom_actions').empty());
}

function add_definition_listener(event) {
	event.stop();
	const study = StudyHandler.GetStudy();
	const parameters = {
		study: study,
		entityId: this.dataset.ruleEntity
	};
	const entity = Entities[this.dataset.entity];
	const definition = entity === Entities.RuleDefinitionProperty ? new RuleDefinitionProperty(parameters) : new RuleDefinitionAction(parameters);
	study.addChild(definition);
	Router.SelectNode(definition);
}

function delete_definition_listener(event) {
	event.stop();
	UI.Validate('Are you sure you want to delete this definition?').then(confirmed => {
		if(confirmed) {
			const study = StudyHandler.GetStudy();
			const definition_ui = this.parentNode;
			const entity = Entities[definition_ui.dataset.entity];
			const definition = study.getChild(entity, 0, definition_ui.dataset.id);
			definition.delete();
			definition_ui.parentNode.removeChild(definition_ui);
		}
	});
}

function draw_definition(definition) {
	const definition_li = document.createFullElement('li', {'data-entity': definition.getEntity().name, 'data-id': definition.id}, definition.label);
	const delete_definition_button = document.createFullElement('img', {src: 'images/cross.png', alt: 'Delete definition', title: 'Delete definition'});
	delete_definition_button.addEventListener('click', delete_definition_listener);
	definition_li.appendChild(delete_definition_button);
	return definition_li;
}

export const RuleDefinitions = {
	Init: function() {
		const rule_definitions_entities = document.getElementById('rule_definitions_entities');
		Object.values(Config.Enums.RuleEntities)
			.map(e => document.createFullElement('li', {'data-rule-entity': e.name}, e.label, {'click': select_entity_listener}))
			.forEach(Node.prototype.appendChild, rule_definitions_entities);

		document.getElementById('rule_definitions_custom_property_add').addEventListener('click', add_definition_listener);
		document.getElementById('rule_definitions_custom_action_add').addEventListener('click', add_definition_listener);

		//rule definition dialog is managed by the application URL
		document.getElementById('rule_definitions').addEventListener('close', () => Router.CloseTool());

	},
	Open: function() {
		select_entity(Config.Enums.RuleEntities.SCOPE);
		/**@type {HTMLDialogElement}*/ (document.getElementById('rule_definitions')).showModal();
	}
};
