import {Reviver} from '../../basic-tools/reviver.js';
import {bus} from './entities_hooks.js';

//manage static nodes
const static_config = {
	languages: [
		{
			id: 'en',
			className: 'Language',
			shortname: {
				en: 'English',
				fr: 'Anglais'
			}
		},
		{
			id: 'fr',
			className: 'Language',
			shortname: {
				en: 'French',
				fr: 'Français'
			}
		}
	],
	features: [
		{
			id: 'ADMIN',
			className: 'Feature',
			shortname: {
				en: 'Administrator',
				fr: 'Administrateur'
			},
			description: {
				en: 'Gives global administration rights',
				fr: 'Donne les droits d\'administration'
			},
			optional: false
		},
		{
			id: 'MANAGE_CONFIGURATION',
			className: 'Feature',
			shortname: {
				en: 'Manage configuration',
				fr: 'Gestion de la configuration'
			},
			description: {
				en: 'Allows to edit the configuration',
				fr: 'Permet l\'edition de la configuration'
			},
			optional: false
		},
		{
			id: 'MANAGE_DELETED_DATA',
			className: 'Feature',
			shortname: {
				en: 'Manage deleted data',
				fr: 'Gestion des données supprimées'
			},
			description: {
				en: 'Allows to view and manage deleted data',
				fr: 'Permet de voir et gérer les données supprimées'
			},
			optional: false
		},
		{
			id: 'MANAGE_MAILS',
			className: 'Feature',
			shortname: {
				en: 'Manage e-mails',
				fr: 'Gestion des e-mails'
			},
			description: {
				en: 'Allows to manage e-mails sent by system',
				fr: 'Permet de gérer les e-mails envoyées par le système'
			},
			optional: false
		},
		{
			id: 'MANAGE_RESOURCE',
			className: 'Feature',
			shortname: {
				en: 'Manage resources',
				fr: 'Gestion des ressources'
			},
			description: {
				en: 'Allows to manage resources',
				fr: 'Permet de gérer les e-mails envoyées par le système'
			},
			optional: false
		},
		{
			id: 'LOCK',
			className: 'Feature',
			shortname: {
				en: 'Lock scopes and events',
				fr: 'Verrouiller les scopes et les évènements'
			},
			description: {
				en: 'Allows to lock scopes and events',
				fr: 'Permet de verrouiller les scopes et les évènements'
			},

			optional: false
		},
		{
			id: 'DOCUMENTATION',
			className: 'Feature',
			shortname: {
				en: 'Documentation',
				fr: 'Documentation'
			},
			description: {
				en: 'Allows to export documentation files',
				fr: 'Permet d\'exporter les fichiers de documentation'
			},
			optional: false
		},
		{
			id: 'EXPORT',
			className: 'Feature',
			shortname: {
				en: 'Export',
				fr: 'Export'
			},
			description: {
				en: 'Allows to export data',
				fr: 'Permet d\'exporter les données'
			},
			optional: false
		},
		{
			id: 'NOTIFY_RESOURCE_PUBLISHED',
			className: 'Feature',
			shortname: {
				en: 'Get notified of resources published',
				fr: 'Être notifié lorsque des ressources sont publiées'
			},
			optional: true
		},
		{
			id: 'VIEW_AUDIT_TRAIL',
			className: 'Feature',
			shortname: {
				en: 'View audit trails',
				fr: 'Voir les audit trails'
			},
			optional: false
		}
	],
	className: 'Study'
};

const static_properties = ['languages', 'features'];

export function create_config_helpers(Config) {

	const ConfigHelpers = {};

	//add revived static nodes in configuration, used when the configuration is deserialized during edition
	ConfigHelpers.InsertRevivedStaticNodes = function(study) {
		function static_node_callback(node) {
			node.staticNode = true;
			node.study = study;
		}
		//revive static configuration
		const static_config_revived = ConfigHelpers.GetReviver(static_node_callback).revive(static_config);
		//add static nodes in study
		static_properties.forEach(static_property => {
			static_config_revived[static_property].forEach(node => {
				node.study = study;
				study[static_property].push(node);
			});
		});
	};

	//add raw static nodes in configuration, used when the configuration is deserialized the first time and has not been revived yet
	ConfigHelpers.InsertStaticNodes = function(config) {
		static_properties.forEach(function(static_property) {
			static_config[static_property].forEach(function(node) {
				node.staticNode = true;
				config[static_property].push(node);
			});
		});
		return config;
	};

	ConfigHelpers.RemoveStaticNodes = function(config) {
		static_properties.forEach(function(static_property) {
			static_config[static_property].forEach(function(node) {
				config[static_property].remove(config[static_property].findIndex(n => n.id === node.id));
			});
		});
		return config;
	};

	ConfigHelpers.CloneNode = function(original_node, properties, reviver_callback) {
		//TODO do not manage bus here
		//disable bus
		bus?.disable();
		const cloned_node = ConfigHelpers.GetReviver(reviver_callback).revive(Object.clone(original_node));
		//modify cloned node properties
		if(properties) {
			for(const property in properties) {
				if(properties.hasOwnProperty(property)) {
					cloned_node[property] = properties[property];
				}
			}
		}
		//modify cloned node id if it has an id and it has not been specified (using properties argument)
		if(original_node.id && (!properties || !properties.hasOwnProperty('id'))) {
			cloned_node.id = `COPY_${original_node.id}`;
		}
		//enable bus
		bus?.enable();
		return cloned_node;
	};

	/*function get_raw_entity() {
		const keys = Object.keys(entities_constructors_raw);
		for(let i = keys.length - 1; i >= 0; i--) {
			if(entities_constructors_raw[keys[i]] === entity) {
				return keys[i];
			}
		}
		throw new Error('No entity with id ' + entity);
	}*/

	//some requirements to revive config
	ConfigHelpers.GetConfigEntitiesConstructors = function(entity) {
		return Config.Entities[entity];
	};

	ConfigHelpers.GetConfigEntitiesProperties = function(entity) {
		return ConfigHelpers.GetConfigEntitiesConstructors(entity).getProperties();
	};

	ConfigHelpers.GetReviver = function(callback) {
		return new Reviver({
			entitiesConstructors: ConfigHelpers.GetConfigEntitiesConstructors,
			entitiesProperties: ConfigHelpers.GetConfigEntitiesProperties,
			preserveEntityProperty: true,
			enforceTypes: true,
			debug: true,
			callback: callback
		});
	};

	//revive a configuration and handle possible errors
	ConfigHelpers.Revive = function(object, callback) {
		const reviver = ConfigHelpers.GetReviver(callback);
		try {
			//TODO do not manage bus here
			//disable bus
			bus?.disable();
			return reviver.revive(object);
		}
		finally {
			//enable bus
			bus?.enable();
		}
	};

	return ConfigHelpers;
}
