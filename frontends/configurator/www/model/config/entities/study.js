import '../../../basic-tools/extension.js';

import {EntitiesHooks} from '../entities_hooks.js';
import {Utils} from '../utils.js';
import {Report} from '../report.js';
import {DisplayableNode} from '../node_displayable.js';
import {Entities} from '../entities.js';
import {Trigger} from '../trigger.js';
import {Assignables} from '../entities_categories.js';

function menu_home_public(menu) {
	return menu.public && menu.homePage;
}

function menu_home_private(menu) {
	return !menu.public && menu.homePage;
}

export class Study extends DisplayableNode {
	static getProperties() {
		return {
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			url: {type: 'string'},
			email: {type: 'string'},
			color: {type: 'string'},
			logo: {type: 'string'},
			introductionText: {type: 'string'},
			welcomeText: {type: 'string'},
			loginText: {type: 'string'},
			smtpServer: {type: 'string'},
			smtpPort: {type: 'number'},
			smtpTLS: {type: 'boolean'},
			smtpLogin: {type: 'string'},
			smtpPassword: {type: 'string'},
			passwordStrong: {type: 'boolean'},
			passwordLength: {type: 'number'},
			passwordValidityDuration: {type: 'number'},
			passwordUniqueness: {type: 'boolean'},
			eproEnabled: {type: 'boolean'},
			eproProfileId: {type: 'string'},
			client: {type: 'string'},
			clientEmail: {type: 'string'},
			protocolNo: {type: 'string'},
			versionNumber: {type: 'string'},
			versionDate: {type: 'string'},
			configVersion: {type: 'number'},
			configDate: {type: 'number'},
			configUser: {type: 'string'},
			configChangelogs: {type: 'array'},
			languageIds: {type: 'array'},
			defaultLanguageId: {type: 'string'},
			exportVisitsLabel: {type: 'string'},
			ruleTags: {type: 'array'},
			ruleDefinitionProperties: {type: 'array'},
			ruleDefinitionActions: {type: 'array'},
			eventActions: {type: 'object'},
			languages: {type: 'array', subtype: Entities.Language.name},
			scopeModels: {type: 'array', subtype: Entities.ScopeModel.name},
			datasetModels: {type: 'array', subtype: Entities.DatasetModel.name},
			validators: {type: 'array', subtype: Entities.Validator.name},
			formModels: {type: 'array', subtype: Entities.FormModel.name},
			workflows: {type: 'array', subtype: Entities.Workflow.name},
			profiles: {type: 'array', subtype: Entities.Profile.name},
			features: {type: 'array', subtype: Entities.Feature.name},
			paymentPlans: {type: 'array', subtype: Entities.PaymentPlan.name},
			menus: {type: 'array', subtype: Entities.Menu.name},
			privacyPolicies: {type: 'array', subtype: Entities.PrivacyPolicy.name},
			resourceCategories: {type: 'array', subtype: Entities.ResourceCategory.name},
			reports: {type: 'array', subtype: Entities.Report.name},
			charts: {type: 'array', subtype: Entities.Chart.name},
			timelineGraphs: {type: 'array', subtype: Entities.TimelineGraph.name},
			workflowWidgets: {type: 'array', subtype: Entities.WorkflowWidget.name},
			workflowSummaries: {type: 'array', subtype: Entities.WorkflowSummary.name},
			crons: {type: 'array', subtype: Entities.Cron.name},
			selections: {type: 'array', subtype: Entities.SelectionNode.name}
		};
	}

	constructor(values) {
		super();
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.url = undefined;
		this.email = undefined;
		this.color = undefined;
		this.logo = undefined;
		this.introductionText = undefined;
		this.welcomeText = undefined;
		this.loginText = undefined;
		this.smtpServer = undefined;
		this.smtpPort = undefined;
		this.smtpTLS = undefined;
		this.smtpLogin = undefined;
		this.smtpPassword = undefined;
		this.passwordStrong = undefined;
		this.passwordLength = 4;
		this.passwordValidityDuration = undefined;
		this.passwordUniqueness = undefined;
		this.eproEnabled = false;
		this.eproProfileId = undefined;
		this.client = undefined;
		this.clientEmail = undefined;
		this.protocolNo = undefined;
		this.versionNumber = undefined;
		this.versionDate = undefined;
		this.configVersion = 0;
		this.configDate = undefined;
		this.configUser = undefined;
		this.configChangelogs = [];
		this.languageIds = [];
		this.defaultLanguageId = undefined;
		this.exportVisitsLabel = undefined;
		this.ruleTags = [];
		this.ruleDefinitionProperties = [];
		this.ruleDefinitionActions = [];
		this.eventActions = {};
		this.languages = [];
		this.scopeModels = [];
		this.datasetModels = [];
		this.validators = [];
		this.formModels = [];
		this.workflows = [];
		this.profiles = [];
		this.features = [];
		this.paymentPlans = [];
		this.menus = [];
		this.privacyPolicies = [];
		this.resourceCategories = [];
		this.reports = [];
		this.charts = [];
		this.timelineGraphs = [];
		this.workflowWidgets = [];
		this.workflowSummaries = [];
		this.crons = [];
		this.selections = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getScopeModel(scope_model_id) {
		return Utils.getObjectById(this.scopeModels, scope_model_id);
	}
	getDatasetModel(dataset_model_id) {
		return Utils.getObjectById(this.datasetModels, dataset_model_id);
	}
	getValidator(validator_id) {
		return Utils.getObjectById(this.validators, validator_id);
	}
	getFormModel(form_model_id) {
		return Utils.getObjectById(this.formModels, form_model_id);
	}
	getWorkflow(workflow_id) {
		return Utils.getObjectById(this.workflows, workflow_id);
	}
	getProfile(profile_id) {
		return Utils.getObjectById(this.profiles, profile_id);
	}
	getLanguage(language_id) {
		return Utils.getObjectById(this.languages, language_id);
	}
	getMenu(menu_id) {
		return Utils.getObjectById(this.menus, menu_id);
	}
	getSelectedLanguages() {
		return this.languageIds.map(l => this.getLanguage(l));
	}
	getRootScopeModel() {
		return this.scopeModels.find(s => s.isRoot());
	}
	getLeafScopeModels() {
		return this.scopeModels.filter(scope_model => {
			return !this.scopeModels.some(s => s.parentIds.includes(scope_model.id));
		});
	}
	getLeafScopeModel() {
		const root = this.getRootScopeModel();
		return this.getLeafScopeModels()
			.map(s => ({model: s, depth: s.getScopeModelBranch(root)}))
			.reduce((leafest, leaf) => {
				if(!leafest) {
					return leaf;
				}
				return leafest.depth > leaf.depth ? leafest : leaf;
			}).model;
	}
	getEventModels() {
		return this.scopeModels.flatMap(s => s.eventModels);
	}
	getPublicHomePage() {
		let i, menu, submenu;
		for(i = this.menus.length - 1; i >= 0; i--) {
			menu = this.menus[i];
			//test submenus first
			submenu = menu.submenus.find(menu_home_public);
			if(submenu) {
				return submenu;
			}
			//test menu
			if(menu_home_public.call(undefined, menu)) {
				return menu;
			}
		}
		throw new Error('No public home page');
	}
	getPrivateHomePage() {
		let i, menu, submenu;
		for(i = this.menus.length - 1; i >= 0; i--) {
			menu = this.menus[i];
			//test submenus first
			submenu = menu.submenus.find(menu_home_private);
			if(submenu) {
				return submenu;
			}
			//test menu
			if(menu_home_private.call(undefined, menu)) {
				return menu;
			}
		}
		//private home page may be the same as public home page
		return this.getPublicHomePage();
	}
	getRuleDefinitionProperties(entity) {
		return this.ruleDefinitionProperties.filter(d => d.entityId === entity.name);
	}
	getRuleDefinitionProperty(entity, id) {
		return this.getRuleDefinitionProperties(entity).find(d => d.id === id);
	}
	getRuleDefinitionActions(entity) {
		return this.ruleDefinitionActions.filter(d => d.entityId === entity.name);
	}
	getRuleDefinitionAction(entity, id) {
		return this.getRuleDefinitionActions(entity).find(d => d.id === id);
	}
	getAllRuleDefinitionProperties(entity) {
		return [...entity.properties.slice(), ...this.getRuleDefinitionProperties(entity)];
	}
	getAllRuleDefinitionProperty(entity, property_id) {
		const properties = this.getAllRuleDefinitionProperties(entity);
		const property = properties.find(a => a.id === property_id);
		if(property) {
			return property;
		}
		else {
			throw new Error(`No property with id ${property_id} in entity ${entity.name}. Available properties are ${properties.map(p => p.id)}.`);
		}
	}
	getAllRuleDefinitionActions(entity) {
		return [...entity.actions.slice(), ...this.getRuleDefinitionActions(entity)];
	}
	getAllRuleDefinitionAction(entity, action_id) {
		const actions = this.getAllRuleDefinitionActions(entity);
		const action = actions.find(a => a.id === action_id);
		if(action) {
			return action;
		}
		else {
			throw new Error(`No action with id ${action_id} in entity ${entity.name}. Available actions are ${actions.map(a => a.id)}.`);
		}
	}

	//assignables
	getAssignables(entity) {
		switch(entity) {
			case Entities.Feature:
				return this.features.slice();
			case Entities.TimelineGraph:
				return this.timelineGraphs.slice();
			case Entities.ResourceCategory:
				return this.resourceCategories.slice();
			case Entities.Menu:
				return this.menus.flatMap(m => [m, ...m.submenus]);
			case Entities.Report:
				return this.reports.slice();
		}
		throw new Error(`Entity ${entity.name} is not an assignable`);
	}
	getAssignable(entity, assignable_id) {
		return Utils.getObjectById(this.getAssignables(entity), assignable_id);
	}

	//right assignables
	getRightAssignables(entity) {
		switch(entity) {
			case Entities.ScopeModel:
				return this.scopeModels.slice();
			case Entities.EventModel:
				return this.getEventModels().slice();
			case Entities.DatasetModel:
				return this.datasetModels.slice();
			case Entities.FormModel:
				return this.formModels.slice();
			case Entities.Profile:
				return this.profiles.slice();
			case Entities.PaymentPlan:
				return this.paymentPlans.slice();
		}
		throw new Error(`Entity ${entity.name} is not a right assignable`);
	}

	//rulable and layoutable
	getStudy() {
		return this;
	}

	//tree
	getChildren(entity, index) {
		switch(entity) {
			case Entities.ScopeModel:
				return this.scopeModels.slice();
			case Entities.DatasetModel:
				return this.datasetModels.slice();
			case Entities.Validator:
				return this.validators.slice();
			case Entities.FormModel:
				return this.formModels.slice();
			case Entities.Workflow:
				return this.workflows.slice();
			case Entities.Profile:
				return this.profiles.slice();
			case Entities.Feature:
				return this.features.slice();
			case Entities.Language:
				return this.languages.slice();
			case Entities.Menu:
				return this.menus.slice();
			case Entities.PaymentPlan:
				return this.paymentPlans.slice();
			case Entities.Chart:
				return this.charts.slice();
			case Entities.TimelineGraph:
				return this.timelineGraphs.slice();
			case Entities.ResourceCategory:
				return this.resourceCategories.slice();
			case Entities.PrivacyPolicy:
				return this.privacyPolicies.slice();
			case Entities.Report:
				return this.reports.slice();
			case Entities.WorkflowWidget:
				return this.workflowWidgets.slice();
			case Entities.WorkflowSummary:
				return this.workflowSummaries.slice();
			case Entities.RuleDefinitionProperty:
				return this.ruleDefinitionProperties.slice();
			case Entities.RuleDefinitionAction:
				return this.ruleDefinitionActions.slice();
			case Entities.Cron:
				return this.crons.slice();
			case Entities.SelectionNode:
				return this.selections.slice();
			case Entities.Rule: {
				const triggers = Object.keys(Trigger).sort();
				return this.eventActions[triggers[index]] || [];
			}
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child, index) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.ScopeModel:
				this.scopeModels.push(child);
				child.study = this;
				break;
			case Entities.DatasetModel:
				this.datasetModels.push(child);
				child.study = this;
				break;
			case Entities.Validator:
				this.validators.push(child);
				child.study = this;
				break;
			case Entities.FormModel:
				this.formModels.push(child);
				child.study = this;
				break;
			case Entities.Workflow:
				this.workflows.push(child);
				child.study = this;
				break;
			case Entities.Profile:
				this.profiles.push(child);
				child.study = this;
				break;
			case Entities.Feature:
				this.features.push(child);
				child.study = this;
				break;
			case Entities.Language:
				this.languages.push(child);
				child.study = this;
				break;
			case Entities.Menu:
				this.menus.push(child);
				child.study = this;
				break;
			case Entities.PaymentPlan:
				this.paymentPlans.push(child);
				child.study = this;
				break;
			case Entities.Chart:
				this.charts.push(child);
				child.study = this;
				break;
			case Entities.TimelineGraph:
				this.timelineGraphs.push(child);
				child.study = this;
				break;
			case Entities.ResourceCategory:
				this.resourceCategories.push(child);
				child.study = this;
				break;
			case Entities.PrivacyPolicy:
				this.privacyPolicies.push(child);
				child.study = this;
				break;
			case Entities.Report:
				this.reports.push(child);
				child.study = this;
				break;
			case Entities.WorkflowWidget:
				this.workflowWidgets.push(child);
				child.study = this;
				break;
			case Entities.WorkflowSummary:
				this.workflowSummaries.push(child);
				child.study = this;
				break;
			case Entities.RuleDefinitionProperty:
				this.ruleDefinitionProperties.push(child);
				child.study = this;
				break;
			case Entities.RuleDefinitionAction:
				this.ruleDefinitionActions.push(child);
				child.study = this;
				break;
			case Entities.Cron:
				this.crons.push(child);
				child.study = this;
				break;
			case Entities.SelectionNode:
				this.selections.push(child);
				child.parent = this;
				break;
			case Entities.Rule: {
				const triggers = Object.keys(Trigger).sort();
				if(!this.eventActions[triggers[index]]) {
					this.eventActions[triggers[index]] = [];
				}
				this.eventActions[triggers[index]].push(child);
				child.rulable = this;
				break;
			}
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}

	//bus
	onChangeLanguageId(event) {
		super.onChangeLanguageId(event);
		this.languageIds.replace(event.oldValue, event.newValue);
	}
	onDeleteScopeModel(event) {
		this.scopeModels.removeElement(event.node);
	}
	onDeleteDatasetModel(event) {
		this.datasetModels.removeElement(event.node);
	}
	onDeleteValidator(event) {
		this.validators.removeElement(event.node);
	}
	onDeleteFormModel(event) {
		this.formModels.removeElement(event.node);
	}
	onDeleteWorkflow(event) {
		this.workflows.removeElement(event.node);
	}
	onDeleteProfile(event) {
		this.profiles.removeElement(event.node);
	}
	onDeleteFeature(event) {
		this.features.removeElement(event.node);
	}
	onDeleteLanguage(event) {
		super.onDeleteLanguage(event);
		this.languages.removeElement(event.node);
		this.languageIds.removeElement(event.node.id);
	}
	onMoveMenu(event) {
		if(event.newParent === this) {
			//the moved menu must be a submenu to be moved in the study, so it has to been removed it from its current parent
			event.node.parent.submenus.removeElement(event.node);
			event.node.parent = undefined;
			event.node.study = this;
			this.menus.push(event.node);
		}
	}
	onDeleteMenu(event) {
		this.menus.removeElement(event.node);
	}
	onDeletePaymentPlan(event) {
		this.paymentPlans.removeElement(event.node);
	}
	onDeleteChart(event) {
		this.charts.removeElement(event.node);
	}
	onDeleteTimelineGraph(event) {
		this.timelineGraphs.removeElement(event.node);
	}
	onDeleteResourceCategory(event) {
		this.resourceCategories.removeElement(event.node);
	}
	onDeletePrivacyPolicy(event) {
		this.privacyPolicies.removeElement(event.node);
	}
	onDeleteReport(event) {
		this.reports.removeElement(event.node);
	}
	onDeleteCron(event) {
		this.crons.removeElement(event.node);
	}
	onDeleteSelectionNode(event) {
		this.selections.removeElement(event.node);
	}
	onDeleteWorkflowWidget(event) {
		this.workflowWidgets.removeElement(event.node);
	}
	onDeleteWorkflowSummary(event) {
		this.workflowSummaries.removeElement(event.node);
	}
	onDeleteRuleDefinitionProperty(event) {
		this.ruleDefinitionProperties.removeElement(event.node);
	}
	onDeleteRuleDefinitionAction(event) {
		this.ruleDefinitionActions.removeElement(event.node);
	}
	onDeleteRule(event) {
		for(const trigger_id in this.eventActions) {
			if(this.eventActions.hasOwnProperty(trigger_id)) {
				this.eventActions[trigger_id].removeElement(event.node);
			}
		}
	}

	//report
	report(settings) {
		const report = super.report(settings);
		//home page
		try {
			this.getPrivateHomePage();
		}
		catch(exception) {
			report.addError(exception.message);
		}
		//rights
		Assignables.forEach(entity => {
			this.getAssignables(entity).forEach(assignable => {
				const used = this.profiles.some(p => p.isAssigned(entity, assignable.id));
				if(!used) {
					//no profile has right on node, but if the node is static it is understandable
					if(assignable.staticNode) {
						report.addInfo(`No profile has right on static ${entity.name} ${assignable.id}`);
					}
					else {
						report.addWarning(`No profile has right on ${entity.name} ${assignable.id}`);
					}
				}
			});
		});
		Report.checkLabel(report, this, 'introductionText');
		return report;
	}
}
