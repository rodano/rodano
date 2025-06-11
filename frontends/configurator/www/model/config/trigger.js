import {RuleEntities} from './rule_entities.js';

//WARNING when editing the triggers, do not forget to update the number of relations between the entities Study and Rule in the file entities.js
export const Trigger = Object.freeze({
	CREATE_SCOPE: {
		rule_entities: [
			RuleEntities.SCOPE
		],
		shortname: {
			en: 'Scope created',
			fr: 'Scope créé'
		}
	},
	REMOVE_SCOPE: {
		rule_entities: [
			RuleEntities.SCOPE
		],
		shortname: {
			en: 'Scope removed',
			fr: 'Scope supprimée'
		}
	},
	RESTORE_SCOPE: {
		rule_entities: [
			RuleEntities.SCOPE
		],
		shortname: {
			en: 'Scope restored',
			fr: 'Scope restaurée'
		}
	},
	CREATE_EVENT: {
		rule_entities: [
			RuleEntities.SCOPE,
			RuleEntities.EVENT
		],
		shortname: {
			en: 'Event created',
			fr: 'Évènement créé'
		}
	},
	REMOVE_EVENT: {
		rule_entities: [
			RuleEntities.SCOPE,
			RuleEntities.EVENT
		],
		shortname: {
			en: 'Event removed',
			fr: 'Évènement supprimé'
		}
	},
	RESTORE_EVENT: {
		rule_entities: [
			RuleEntities.SCOPE,
			RuleEntities.EVENT
		],
		shortname: {
			en: 'Event restored',
			fr: 'Évènement restauré'
		}
	},
	CREATE_DATASET: {
		rule_entities: [
			RuleEntities.SCOPE,
			RuleEntities.EVENT,
			RuleEntities.DATASET
		],
		shortname: {
			en: 'Dataset created',
			fr: 'Dataset créé'
		}
	},
	REMOVE_DATASET: {
		rule_entities: [
			RuleEntities.SCOPE,
			RuleEntities.EVENT,
			RuleEntities.DATASET
		],
		shortname: {
			en: 'Dataset removed',
			fr: 'Dataset supprimé'
		}
	},
	RESTORE_DATASET: {
		rule_entities: [
			RuleEntities.SCOPE,
			RuleEntities.EVENT,
			RuleEntities.DATASET
		],
		shortname: {
			en: 'Dataset restored',
			fr: 'Dataset restauré'
		}
	},
	CREATE_WORKFLOW_STATUS: {
		rule_entities: [
			RuleEntities.SCOPE,
			RuleEntities.EVENT,
			RuleEntities.DATASET,
			RuleEntities.FIELD,
			RuleEntities.FORM,
			RuleEntities.WORKFLOW
		],
		shortname: {
			en: 'Workflow status created',
			fr: 'Statut de workflow créé'
		}
	},
	UPDATE_VALUE: {
		rule_entities: [
			RuleEntities.SCOPE,
			RuleEntities.EVENT,
			RuleEntities.DATASET,
			RuleEntities.FIELD
		],
		shortname: {
			en: 'Field updated',
			fr: 'Champ mise à jour'
		}
	},
	SAVE_FORM: {
		rule_entities: [
			RuleEntities.SCOPE,
			RuleEntities.EVENT,
			RuleEntities.FORM
		],
		shortname: {
			en: 'Form saved',
			fr: 'Formulaire sauvé'
		}
	},
	ROLE_CREATE: {
		rule_entities: [
			RuleEntities.SCOPE
		],
		shortname: {
			en: 'Role created',
			fr: 'Rôle créé'
		}
	},
	ROLE_ENABLE: {
		rule_entities: [
			RuleEntities.SCOPE
		],
		shortname: {
			en: 'Role enabled',
			fr: 'Rôle activé'
		}
	},
	ROLE_DISABLE: {
		rule_entities: [
			RuleEntities.SCOPE
		],
		shortname: {
			en: 'Role disabled',
			fr: 'Rôle désactivé'
		}
	},
	USER_LOGIN: {
		rule_entities: [
			RuleEntities.SCOPE
		],
		shortname: {
			en: 'User logged in',
			fr: 'Utilisation connecté'
		}
	}
});
