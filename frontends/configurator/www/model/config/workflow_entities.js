import {Entities} from './entities.js';

export const WorkflowEntities = Object.freeze({
	SCOPE: {
		configurationEntity: Entities.ScopeModel,
		label: 'Scope',
		container_entities: [
			'SCOPE'
		]
	},
	EVENT: {
		configurationEntity: Entities.EventModel,
		label: 'Event',
		container_entities: [
			'SCOPE',
			'EVENT'
		]
	},
	FORM: {
		configurationEntity: Entities.FormModel,
		label: 'Form',
		container_entities: [
			'SCOPE',
			'EVENT',
			'FORM'
		]
	},
	FIELD: {
		configurationEntity: Entities.FieldModel,
		label: 'Field',
		container_entities: [
			'SCOPE',
			'EVENT',
			'FORM',
			'FIELD'
		]
	}
});
