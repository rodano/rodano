import '../../basic-tools/extension.js';

const CURRENT_VERSION = 119;

class ApplicationOutdatedError extends Error {
	constructor(version) {
		super(`Application is outdated. Config version is ${version}, application version is ${CURRENT_VERSION}`);
		this.version = version;
	}
}

class MissingMigrationError extends Error {
	constructor(version) {
		super(`Missing migration script for version ${version}`);
		this.version = version;
	}
}

class MigrationError extends Error {
	constructor(version, source) {
		super(`Error during migration to version ${version}: ${source.message}`);
		this.version = version;
		this.source = source;
	}
}

const Migrations = {
	migrate_116: {
		description: 'Update to latest version of Angular',
		migration: function(config) {
			function update_menu(menu) {
				if(menu.action.page === 'scope-page') {
					menu.action.page = `scopes/${menu.action.context[0].toUpperCase()}`;
					menu.action.context = [];
					return true;
				}
				else if(menu.action.page === 'contacts') {
					menu.action.page = 'users';
					return true;
				}
				else if(menu.action.page === 'crf') {
					menu.action.page = 'search';
					menu.action.context = [];
					return true;
				}
				else if(menu.action.page === 'extract') {
					menu.action.page = 'extracts';
					return true;
				}
				else if(menu.action.page === 'send-mail') {
					menu.action.page = 'send-test-mail';
					return true;
				}
				return false;
			}
			const menus = [];
			config.menus.forEach(function(menu) {
				if(update_menu(menu)) {
					menus.push(menu);
				}
				menus.pushAll(menu.submenus.filter(update_menu));
			});
			return menus;
		}
	},
	migrate_117: {
		description: 'Migrate date and number format',
		migration: function(config) {
			function update_field_model(field_model) {
				if(field_model.type === 'DATE') {
					field_model.minYear = field_model.yearsStart;
					field_model.maxYear = field_model.yearsStop;
					field_model.withYears = field_model.format.includes('yyyy');
					field_model.withMonths = field_model.format.includes('MM');
					field_model.withDays = field_model.format.includes('dd');
					field_model.withHours = field_model.format.includes('HH');
					field_model.withMinutes = field_model.format.includes('mm');
					field_model.withSeconds = field_model.format.includes('ss');
				}
				else if(field_model.type === 'DATE_SELECT') {
					field_model.minYear = field_model.yearsStart;
					field_model.maxYear = field_model.yearsStop;
					field_model.withYears = field_model.displayYears;
					field_model.withMonths = field_model.displayMonths;
					field_model.withDays = field_model.displayDays;
				}
				else if(field_model.type === 'NUMBER') {
					if(field_model.format.includes('.')) {
						const parts = field_model.format.split('.');
						field_model.maxIntegerDigits = parts[0].length;
						field_model.maxDecimalDigits = parts[1].length;
					}
					else {
						field_model.maxIntegerDigits = field_model.format.length;
					}
				}
				delete field_model.yearsStart;
				delete field_model.yearsStop;
				delete field_model.displayYears;
				delete field_model.displayMonths;
				delete field_model.displayDays;
				delete field_model.format;
				return true;
			}
			const field_models = [];
			config.datasetModels.flatMap(d => d.fieldModels).forEach(field_model => {
				if(update_field_model(field_model)) {
					field_models.push(field_model);
				}
			});
			return field_models;
		}
	},
	migrate_118: {
		description: 'Rename and remove configuration options',
		migration: function(config) {
			config.scopeModels.forEach(scope_model => {
				scope_model.parentIds = scope_model.parents;
				delete scope_model.parents;
			});
			config.datasetModels.flatMap(d => d.fieldModels).forEach(field_model => {
				delete field_model.helpHover;
				field_model.inlineHelp = field_model.forDisplay;
				delete field_model.forDisplay;
				field_model.advancedHelp = field_model.helpText;
				delete field_model.helpText;
				delete field_model.size;
			});
		}
	}
};

/*function migrate_layouts(config, section_migrator, widget_migrator) {

	function migrate_layout(layout) {
		if(layout) {
			if(layout.sections) {
				layout.sections.forEach(section => {
					if(section_migrator) {
						section_migrator.call(undefined, section);
					}
					section.widgets.forEach(widget => {
						if(widget_migrator) {
							widget_migrator.call(undefined, widget);
						}
					});
				});
			}
		}
	}

	const nodes = [];
	config.menus.forEach(menu => {
		migrate_layout(menu.layout);
		nodes.push(menu);
		menu.submenus.forEach(submenu => {
			migrate_layout(submenu.layout);
			nodes.push(submenu);
		});
	});
	config.scopeModels.forEach(scope_model => {
		migrate_layout(scope_model.layout);
		nodes.push(scope_model);
	});
	return nodes;
}

function migrate_rules(config, relation_criterion_replacements, parameter_replacements, action_replacements) {
	function migrate_condition(condition) {
		let migrated = false;
		//update relations
		if(!condition.criterion.operator) {
			if(relation_criterion_replacements.hasOwnProperty(condition.criterion.property)) {
				condition.criterion.property = relation_criterion_replacements[condition.criterion.property];
				migrated = true;
			}
		}
		condition.conditions.forEach(migrate_condition);
		return migrated;
	}
	function migrate_parameter(parameter) {
		let migrated = false;
		if(relation_criterion_replacements.hasOwnProperty(parameter.rulableEntity)) {
			parameter.rulableEntity = relation_criterion_replacements[parameter.rulableEntity];
			migrated = true;
		}
		if(parameter_replacements.hasOwnProperty(parameter.id)) {
			parameter.id = parameter_replacements[parameter.id];
			migrated = true;
		}
		return migrated;
	}
	function migrate_action(action) {
		let migrated = false;
		if(action_replacements.hasOwnProperty(action.actionId)) {
			action.actionId = action_replacements[action.actionId];
			migrated = true;
		}
		if(relation_criterion_replacements.hasOwnProperty(action.rulableEntity)) {
			action.rulableEntity = relation_criterion_replacements[action.rulableEntity];
			migrated = true;
		}
		if(!action.parameters.filter(migrate_parameter).isEmpty()) {
			migrated = true;
		}
		return migrated;
	}
	function migrate_rule(rule) {
		let migrated = false;
		if(migrate_conditionable(rule)) {
			migrated = true;
		}
		if(!rule.actions.filter(migrate_action).isEmpty()) {
			migrated = true;
		}
		return migrated;
	}
	function migrate_conditionable(conditionable) {
		return migrate_constraint(conditionable.constraint);
	}
	function migrate_constraint(constraint) {
		let migrated = false;
		if(constraint?.conditions && !Object.keys(constraint.conditions).isEmpty()) {
			if(constraint.conditions.hasOwnProperty('VISIT')) {
				constraint.conditions['EVENT'] = constraint.conditions['VISIT'];
				delete constraint.conditions['VISIT'];
				migrated = true;
			}
			for(const entity in constraint.conditions) {
				if(constraint.conditions.hasOwnProperty(entity)) {
					constraint.conditions[entity].conditions.forEach(migrate_condition);
				}
			}
		}
		return migrated;
	}
	//triggers
	for(const trigger_id in config.eventActions) {
		if(config.eventActions.hasOwnProperty(trigger_id)) {
			const rules = config.eventActions[trigger_id];
			rules.forEach(migrate_rule);
		}
	}
	//scope models
	config.scopeModels.forEach(scope_model => {
		scope_model.createRules.forEach(migrate_rule);
		scope_model.removeRules.forEach(migrate_rule);
		scope_model.restoreRules.forEach(migrate_rule);
	});
	//event models
	config.eventModels.forEach(event_model => {
		migrate_conditionable(event_model);
		event_model.createRules.forEach(migrate_rule);
		event_model.removeRules.forEach(migrate_rule);
		event_model.restoreRules.forEach(migrate_rule);
	});
	//form models, layouts and cells
	config.formModels.forEach(form_model => {
		migrate_conditionable(form_model);
		form_model.rules.forEach(migrate_rule);
		form_model.layouts.forEach(migrate_conditionable);
		form_model.layouts.flatMap(l => l.lines).flatMap(l => l.cells).forEach(migrate_conditionable);
	});
	//dataset models and field models
	config.datasetModels.forEach(dataset_model => {
		dataset_model.deleteRules.forEach(migrate_rule);
		dataset_model.restoreRules.forEach(migrate_rule);
		dataset_model.fieldModels.forEach(field_model => {
			migrate_constraint(field_model.valueConstraint);
			field_model.rules.filter(migrate_rule);
		});
	});
	//workflows and actions
	config.workflows.forEach(workflow => {
		workflow.rules.forEach(migrate_rule);
		workflow.actions.forEach(action => {
			action.rules.forEach(migrate_rule);
		});
	});
	//validators
	config.validators.forEach(migrate_conditionable);
	//crons
	config.crons.forEach(c => c.rules.forEach(migrate_rule));
}

function generate_node_id() {
	const args = Array.prototype.slice.call(arguments);
	return {
		id: args.last().id,
		globalId: args.map(function(argument) {
			return `${argument.entity}:${argument.id}`;
		}).join('|')
	};
}*/

const Migrator = {
	GetCurrentVersion: function() {
		return CURRENT_VERSION;
	},
	IsUpToDate: function(config) {
		return config.configVersion === CURRENT_VERSION;
	},
	Migrate: function(config) {
		//application is outdated
		if(config.configVersion > CURRENT_VERSION) {
			throw new ApplicationOutdatedError(config.configVersion);
		}
		//migration is possible
		const migration_reports = [];
		while(config.configVersion < CURRENT_VERSION) {
			//retrieve migration
			const migration_id = `migrate_${config.configVersion}`;
			if(!Migrations.hasOwnProperty(migration_id)) {
				throw new MissingMigrationError(config.configVersion);
			}
			//apply migration
			const migration = Migrations[migration_id];
			try {
				const nodes = migration.migration.call(undefined, config);
				config.configVersion = config.configVersion + 1;
				//manage migration report
				const migration_report = {
					description: migration.description,
					instructions: migration.instructions,
					nodes: nodes
				};
				//log migration
				migration_reports.push(migration_report);
			}
			catch(exception) {
				console.log(exception);
				throw new MigrationError(config.configVersion, exception);
			}
		}
		return migration_reports;
	}
};

export {ApplicationOutdatedError, MigrationError, MissingMigrationError, Migrator};
