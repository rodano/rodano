import '../../basic-tools/extension.js';

/**
 * A number, or a string containing a number.
 * @typedef {object} Entity
 * @property {string} id - The id of the entity
 * @property {string} name - The associated class name for the entity
 * @property {string} label - The label of the entity
 * @property {string} plural_label - The plural label of the entity
 * @property {string} configuration_name - The id of the entity as referenced by other entities
 * @property {string|Function} [icon] - The name of an image file used as an icon for the entity or a function that returns the same thing
 * @property {boolean} comparison_structural - When this entity is referenced multiple times by other entities, this flag determines if the order is important
 * @property {Function} [representation] - When this entity is referenced multiple times by other entities, this flag determines if the order is important
 * @property {{[key:string]: any}} children - Dictionary of the children or this entity
 * @property {{[key:string]: any}} relations - Dictionary of the relations of this entity
 */

const Entities = Object.freeze({
	/**@type {Entity}*/
	Study: {
		name: 'Study',
		children: {},
		relations: {},
		id: 'study',
		label: 'Study',
		plural_label: 'Studies',
		icon: 'book_open.png',
		configuration_name: 'STUDY',
		comparison_structural: true
	},
	/**@type {Entity}*/
	Language: {
		name: 'Language',
		children: {},
		relations: {},
		id: 'language',
		label: 'Language',
		plural_label: 'Languages',
		icon: 'comments.png',
		configuration_name: 'LANGUAGE',
		comparison_structural: true
	},

	//data
	/**@type {Entity}*/
	ScopeModel: {
		name: 'ScopeModel',
		children: {},
		relations: {},
		id: 'scope_model',
		label: 'Scope model',
		plural_label: 'Scope models',
		icon: 'group.png',
		configuration_name: 'SCOPE_MODEL',
		comparison_structural: true
	},
	/**@type {Entity}*/
	EventGroup: {
		name: 'EventGroup',
		children: {},
		relations: {},
		id: 'event_group',
		label: 'Event group',
		plural_label: 'Event groups',
		icon: 'hourglass_link.png',
		configuration_name: 'EVENT_GROUP',
		comparison_structural: true
	},
	/**@type {Entity}*/
	EventModel: {
		name: 'EventModel',
		children: {},
		relations: {},
		id: 'event_model',
		label: 'Event model',
		plural_label: 'Event models',
		icon: 'time.png',
		configuration_name: 'EVENT_MODEL',
		comparison_structural: true
	},
	/**@type {Entity}*/
	DatasetModel: {
		name: 'DatasetModel',
		children: {},
		relations: {},
		id: 'dataset_model',
		label: 'Dataset model',
		plural_label: 'Dataset models',
		icon: 'folder.png',
		representation: function(link) {
			enhance_usable.call(this, link);
			const previous_customization = link.querySelector('img[data-ui-customization]');
			if(this.multiple) {
				if(!previous_customization) {
					link.appendChild(document.createFullElement('img', {src: 'images/bullet_yellow.png', alt: 'Multiple', title: 'Multiple', 'data-ui-customization': 'true'}));
				}
			}
			else if(previous_customization) {
				previous_customization.remove();
			}
		},
		configuration_name: 'DATASET_MODEL',
		comparison_structural: true
	},
	/**@type {Entity}*/
	FieldModel: {
		name: 'FieldModel',
		children: {},
		relations: {},
		id: 'field_model',
		label: 'Field model',
		plural_label: 'Field models',
		icon: 'page_white_wrench.png',
		representation: function(link) {
			enhance_usable.call(this, link);
			const previous_customization = link.querySelector('img[data-ui-customization]');
			if(this.dynamic) {
				if(!previous_customization) {
					link.appendChild(document.createFullElement('img', {src: 'images/bullet_wrench.png', alt: 'Plugin', title: 'Plugin', 'data-ui-customization': 'true'}));
				}
			}
			else if(previous_customization) {
				previous_customization.remove();
			}
		},
		configuration_name: 'FIELD_MODEL',
		comparison_structural: true
	},
	/**@type {Entity}*/
	PossibleValue: {
		name: 'PossibleValue',
		children: {},
		relations: {},
		id: 'possible_value',
		label: 'Possible value',
		plural_label: 'Possible values',
		icon: 'page_white_wrench.png',
		configuration_name: 'POSSIBLE_VALUE',
		comparison_structural: true
	},
	/**@type {Entity}*/
	Validator: {
		name: 'Validator',
		children: {},
		relations: {},
		id: 'validator',
		label: 'Validator',
		plural_label: 'Validators',
		icon: 'accept.png',
		representation: function(link) {
			enhance_usable.call(this, link);
			const previous_customization = link.querySelector('img[data-ui-customization]');
			if(!this.workflowId) {
				if(!previous_customization) {
					link.appendChild(document.createFullElement('img', {src: 'images/bullet_red.png', alt: 'Blocking', title: 'Blocking', 'data-ui-customization': 'true'}));
				}
			}
			else if(previous_customization) {
				previous_customization.remove();
			}
		},
		configuration_name: 'VALIDATOR',
		comparison_structural: true
	},
	/**@type {Entity}*/
	ValueSource: {
		name: 'ValueSource',
		children: {},
		relations: {},
		id: 'value_source',
		label: 'Value source',
		plural_label: 'Value sources',
		configuration_name: 'VALUE_SOURCE',
		comparison_structural: true
	},
	/**@type {Entity}*/
	ValueSourceCriteria: {
		name: 'ValueSourceCriteria',
		children: {},
		relations: {},
		id: 'value_source_criteria',
		label: 'Value source criterion',
		plural_label: 'Value source criteria',
		configuration_name: 'VALUE_SOURCE_CRITERIA',
		comparison_structural: true
	},
	/**@type {Entity}*/
	AttributeCriterion: {
		name: 'AttributeCriterion',
		children: {},
		relations: {},
		id: 'attribute_criterion',
		label: 'Value source criterion',
		plural_label: 'Value source criteria',
		configuration_name: 'ATTRIBUTE_CRITERION',
		comparison_structural: true
	},

	//ui
	/**@type {Entity}*/
	FormModel: {
		name: 'FormModel',
		children: {},
		relations: {},
		id: 'form_model',
		label: 'Form model',
		plural_label: 'Form models',
		icon: 'page.png',
		configuration_name: 'FORM_MODEL',
		comparison_structural: true
	},
	/**@type {Entity}*/
	Layout: {
		name: 'Layout',
		children: {},
		relations: {},
		id: 'layout',
		label: 'Layout',
		plural_label: 'Layouts',
		icon: 'page_edit.png',
		configuration_name: 'LAYOUT',
		comparison_structural: true
	},
	/**@type {Entity}*/
	Column: {
		name: 'Column',
		children: {},
		relations: {},
		id: 'column',
		label: 'Column',
		plural_label: 'Columns',
		icon: 'page_edit.png',
		configuration_name: 'COLUMN',
		comparison_structural: true
	},
	/**@type {Entity}*/
	Line: {
		name: 'Line',
		children: {},
		relations: {},
		id: 'line',
		label: 'Line',
		plural_label: 'Lines',
		icon: 'page_edit.png',
		configuration_name: 'LINE',
		comparison_structural: true
	},
	/**@type {Entity}*/
	Cell: {
		name: 'Cell',
		children: {},
		relations: {},
		id: 'cell',
		label: 'Cell',
		plural_label: 'Cells',
		icon: 'page_edit.png',
		configuration_name: 'CELL',
		comparison_structural: true
	},
	/**@type {Entity}*/
	VisibilityCriteria: {
		name: 'VisibilityCriteria',
		children: {},
		relations: {},
		id: 'visibility_criteria',
		label: 'Visibility criteria',
		plural_label: 'Visibility criteria',
		icon: 'script_link.png',
		configuration_name: 'VISIBILITY_CRITERIA',
		comparison_structural: true
	},

	//workflow
	/**@type {Entity}*/
	Workflow: {
		name: 'Workflow',
		children: {},
		relations: {},
		id: 'workflow',
		label: 'Workflow',
		plural_label: 'Workflows',
		icon: 'cog.png',
		configuration_name: 'WORKFLOW',
		comparison_structural: true
	},
	/**@type {Entity}*/
	WorkflowState: {
		name: 'WorkflowState',
		children: {},
		relations: {},
		id: 'workflow_state',
		label: 'State',
		plural_label: 'States',
		icon: 'cog_edit.png',
		configuration_name: 'WORKFLOW_STATE',
		comparison_structural: true
	},
	/**@type {Entity}*/
	Action: {
		name: 'Action',
		children: {},
		relations: {},
		id: 'action',
		label: 'Action',
		plural_label: 'Actions',
		icon: 'bullet_wrench.png',
		configuration_name: 'ACTION',
		comparison_structural: true
	},

	//rights
	/**@type {Entity}*/
	Profile: {
		name: 'Profile',
		children: {},
		relations: {},
		id: 'profile',
		label: 'Profile',
		plural_label: 'Profiles',
		icon: 'user.png',
		configuration_name: 'PROFILE',
		comparison_structural: true
	},
	/**@type {Entity}*/
	Right: {
		name: 'Right',
		children: {},
		relations: {},
		id: 'right',
		label: 'Right',
		plural_label: 'Rights',
		configuration_name: 'RIGHT',
		comparison_structural: true
	},
	/**@type {Entity}*/
	ProfileRight: {
		name: 'ProfileRight',
		children: {},
		relations: {},
		id: 'profile_right',
		label: 'Profile right',
		plural_label: 'Profile rights',
		configuration_name: 'PROFILE_RIGHT',
		comparison_structural: true
	},
	/**@type {Entity}*/
	Feature: {
		name: 'Feature',
		children: {},
		relations: {},
		id: 'feature',
		label: 'Feature',
		plural_label: 'Features',
		icon: function(node) {
			return node?.staticNode ? 'key_delete.png' : 'key.png';
		},
		configuration_name: 'FEATURE',
		comparison_structural: true
	},

	//payment
	/**@type {Entity}*/
	PaymentPlan: {
		name: 'PaymentPlan',
		children: {},
		relations: {},
		id: 'payment_plan',
		label: 'Payment plan',
		plural_label: 'Payment plans',
		icon: 'money.png',
		configuration_name: 'PAYMENT_PLAN',
		comparison_structural: true
	},
	/**@type {Entity}*/
	PaymentStep: {
		name: 'PaymentStep',
		children: {},
		relations: {},
		id: 'payment_step',
		label: 'Step',
		plural_label: 'Steps',
		icon: 'money.png',
		configuration_name: 'PAYMENT_STEP',
		comparison_structural: true
	},
	/**@type {Entity}*/
	PaymentDistribution: {
		name: 'PaymentDistribution',
		children: {},
		relations: {},
		id: 'payment_distribution',
		label: 'Payment distribution',
		plural_label: 'Payment distributions',
		configuration_name: 'PAYMENT_DISTRIBUTION',
		comparison_structural: true
	},

	//ui
	/**@type {Entity}*/
	Menu: {
		name: 'Menu',
		children: {},
		relations: {},
		id: 'menu',
		label: 'Menu',
		plural_label: 'Menus',
		icon: 'layout_sidebar.png',
		configuration_name: 'MENU',
		comparison_structural: true
	},
	/**@type {Entity}*/
	ResourceCategory: {
		name: 'ResourceCategory',
		children: {},
		relations: {},
		id: 'resource_category',
		label: 'Resource category',
		plural_label: 'Resource categories',
		icon: function(node) {
			return node?.staticNode ? 'tag_blue_delete.png' : 'tag_blue.png';
		},
		configuration_name: 'RESOURCE_CATEGORY',
		comparison_structural: true
	},
	/**@type {Entity}*/
	PrivacyPolicy: {
		name: 'PrivacyPolicy',
		children: {},
		relations: {},
		id: 'privacy_policy',
		label: 'Privacy policy',
		plural_label: 'Privacy policies',
		icon: 'report_key.png',
		configuration_name: 'PRIVACY_POLICY',
		comparison_structural: false
	},
	/**@type {Entity}*/
	Changelog: {
		name: 'Changelog',
		children: {},
		relations: {},
		id: 'changelog',
		label: 'Changelog',
		plural_label: 'Changelogs',
		configuration_name: 'CHANGELOG',
		comparison_structural: true
	},

	//report
	/**@type {Entity}*/
	Report: {
		name: 'Report',
		children: {},
		relations: {},
		id: 'report',
		label: 'Report',
		plural_label: 'Reports',
		icon: 'page_white_excel.png',
		configuration_name: 'REPORT',
		comparison_structural: true
	},
	/**@type {Entity}*/
	Cron: {
		name: 'Cron',
		children: {},
		relations: {},
		id: 'cron',
		label: 'Cron',
		plural_label: 'Crons',
		icon: 'clock.png',
		configuration_name: 'CRON',
		comparison_structural: true
	},

	//workflow widgets
	/**@type {Entity}*/
	WorkflowWidget: {
		name: 'WorkflowWidget',
		children: {},
		relations: {},
		id: 'workflow_widget',
		label: 'Workflow widget',
		plural_label: 'Workflow widgets',
		icon: 'application_view_list.png',
		configuration_name: 'WORKFLOW_WIDGET',
		comparison_structural: false
	},
	/**@type {Entity}*/
	WorkflowStatesSelector: {
		name: 'WorkflowStatesSelector',
		children: {},
		relations: {},
		id: 'workflow_states_selector',
		label: 'Workflow states selector',
		plural_label: 'Workflow state selectors',
		configuration_name: 'WORKFLOW_STATES_SELECTOR',
		comparison_structural: true
	},
	/**@type {Entity}*/
	WorkflowWidgetColumn: {
		name: 'WorkflowWidgetColumn',
		children: {},
		relations: {},
		id: 'workflow_widget_column',
		label: 'Workflow widget column',
		plural_label: 'Workflow widget columns',
		configuration_name: 'WORKFLOW_WIDGET_COLUMN',
		comparison_structural: true
	},
	/**@type {Entity}*/
	WorkflowSummary: {
		name: 'WorkflowSummary',
		children: {},
		relations: {},
		id: 'workflow_summary',
		label: 'Workflow summary',
		plural_label: 'Workflow summaries',
		icon: 'application_view_list.png',
		configuration_name: 'WORKFLOW_SUMMARY',
		comparison_structural: false
	},
	/**@type {Entity}*/
	WorkflowSummaryColumn: {
		name: 'WorkflowSummaryColumn',
		children: {},
		relations: {},
		id: 'workflow_summary_column',
		label: 'Workflow summary column',
		plural_label: 'Workflow summary columns',
		configuration_name: 'WORKFLOW_SUMMARY_COLUMN',
		comparison_structural: true
	},

	//graphs
	/**@type {Entity}*/
	Chart: {
		name: 'Chart',
		children: {},
		relations: {},
		id: 'chart',
		label: 'Chart',
		plural_label: 'Charts',
		icon: 'chart_bar.png',
		configuration_name: 'CHART',
		comparison_structural: true
	},
	/**@type {Entity}*/
	ChartRange: {
		name: 'ChartRange',
		children: {},
		relations: {},
		id: 'chart_range',
		label: 'Range',
		plural_label: 'Ranges',
		icon: 'chart_bar_edit.png',
		configuration_name: 'CHART_RANGE',
		comparison_structural: true
	},
	/**@type {Entity}*/
	ChartRequest: {
		name: 'ChartRequest',
		children: {},
		relations: {},
		id: 'chart_request',
		label: 'Request',
		plural_label: 'Requests',
		icon: 'chart_bar_link.png',
		configuration_name: 'CHART_REQUEST',
		comparison_structural: true
	},

	//timeline graph
	/**@type {Entity}*/
	TimelineGraph: {
		name: 'TimelineGraph',
		children: {},
		relations: {},
		id: 'timeline_graph',
		label: 'Timeline',
		plural_label: 'Timelines',
		icon: 'chart_curve.png',
		configuration_name: 'TIMELINE_GRAPH',
		comparison_structural: true
	},
	/**@type {Entity}*/
	TimelineGraphSection: {
		name: 'TimelineGraphSection',
		children: {},
		relations: {},
		id: 'timeline_graph_section',
		label: 'Timeline section',
		plural_label: 'Timeline sections',
		icon: 'chart_curve_edit.png',
		configuration_name: 'TIMELINE_GRAPH_SECTION',
		comparison_structural: true
	},
	/**@type {Entity}*/
	TimelineGraphSectionScale: {
		name: 'TimelineGraphSectionScale',
		children: {},
		relations: {},
		id: 'timeline_graph_section_scale',
		label: 'Timeline section scale',
		plural_label: 'Timeline section scales',
		configuration_name: 'TIMELINE_GRAPH_SECTION_SCALE',
		comparison_structural: true
	},
	/**@type {Entity}*/
	TimelineGraphSectionPosition: {
		name: 'TimelineGraphSectionPosition',
		children: {},
		relations: {},
		id: 'timeline_graph_section_position',
		label: 'Timeline section position',
		plural_label: 'Timeline section positions',
		configuration_name: 'TIMELINE_GRAPH_SECTION_POSITION',
		comparison_structural: true
	},
	/**@type {Entity}*/
	TimelineGraphSectionReference: {
		name: 'TimelineGraphSectionReference',
		children: {},
		relations: {},
		id: 'timeline_graph_section_reference',
		label: 'Timeline section reference',
		plural_label: 'Timeline section references',
		icon: 'tag_blue.png',
		configuration_name: 'TIMELINE_GRAPH_SECTION_REFERENCE',
		comparison_structural: true
	},
	/**@type {Entity}*/
	TimelineGraphSectionReferenceEntry: {
		name: 'TimelineGraphSectionReferenceEntry',
		children: {},
		relations: {},
		id: 'timeline_graph_section_reference_entry',
		label: 'Timeline section reference entry',
		plural_label: 'Timeline section reference entries',
		configuration_name: 'TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY',
		comparison_structural: true
	},

	//cms
	/**@type {Entity}*/
	CMSLayout: {
		name: 'CMSLayout',
		children: {},
		relations: {},
		id: 'cms_layout',
		label: 'CMS layout',
		plural_label: 'CMS layouts',
		icon: 'layout_sidebar.png',
		configuration_name: 'CMS_LAYOUT',
		comparison_structural: true
	},
	/**@type {Entity}*/
	CMSSection: {
		name: 'CMSSection',
		children: {},
		relations: {},
		id: 'cms_section',
		label: 'CMS section',
		plural_label: 'CMS sections',
		icon: 'layout_sidebar.png',
		configuration_name: 'CMS_SECTION',
		comparison_structural: true
	},
	/**@type {Entity}*/
	CMSWidget: {
		name: 'CMSWidget',
		children: {},
		relations: {},
		id: 'cms_widget',
		label: 'CMS widget',
		plural_label: 'CMS widgets',
		icon: 'layout_sidebar.png',
		configuration_name: 'CMS_WIDGET',
		comparison_structural: true
	},
	/**@type {Entity}*/
	CMSAction: {
		name: 'CMSAction',
		children: {},
		relations: {},
		id: 'cms_action',
		label: 'CMS action',
		plural_label: 'CMS actions',
		configuration_name: 'CMS_ACTION',
		comparison_structural: true
	},
	/**@type {Entity}*/
	ScopeCriterionRight: {
		name: 'ScopeCriterionRight',
		children: {},
		relations: {},
		id: 'scope_criterion_right',
		label: 'Scope criterion right',
		plural_label: 'Scope criterion rights',
		configuration_name: 'SCOPE_CRITERION_RIGHT',
		comparison_structural: true
	},

	//rule
	/**@type {Entity}*/
	RuleDefinitionProperty: {
		name: 'RuleDefinitionProperty',
		children: {},
		relations: {},
		id: 'rule_definition_property',
		label: 'Rule definition property',
		plural_label: 'Rule definition properties',
		icon: 'bricks.png',
		configuration_name: 'RULE_DEFINITION_PROPERTY',
		comparison_structural: true
	},
	/**@type {Entity}*/
	RuleDefinitionAction: {
		name: 'RuleDefinitionAction',
		children: {},
		relations: {},
		id: 'rule_definition_action',
		label: 'Rule definition action',
		plural_label: 'Rule definition actions',
		icon: 'bricks.png',
		configuration_name: 'RULE_DEFINITION_ACTION',
		comparison_structural: true
	},
	/**@type {Entity}*/
	RuleDefinitionActionParameter: {
		name: 'RuleDefinitionActionParameter',
		children: {},
		relations: {},
		id: 'rule_definition_action_parameter',
		label: 'Rule definition action parameter',
		plural_label: 'Rule definition action parameters',
		configuration_name: 'RULE_DEFINITION_ACTION_PARAMETER',
		comparison_structural: true
	},
	/**@type {Entity}*/
	Rule: {
		name: 'Rule',
		children: {},
		relations: {},
		id: 'rule',
		label: 'Rule',
		plural_label: 'Rules',
		icon: 'bricks.png',
		configuration_name: 'RULE',
		comparison_structural: true
	},
	/**@type {Entity}*/
	RuleConstraint: {
		name: 'RuleConstraint',
		children: {},
		relations: {},
		id: 'rule_constraint',
		label: 'Constraint',
		plural_label: 'Constraints',
		icon: 'bricks.png',
		configuration_name: 'RULE_CONSTRAINT',
		comparison_structural: true
	},
	/**@type {Entity}*/
	RuleEvaluation: {
		name: 'RuleEvaluation',
		children: {},
		relations: {},
		id: 'rule_evaluation',
		label: 'Rule evaluation',
		plural_label: 'Rule evaluations',
		configuration_name: 'RULE_EVALUATION',
		comparison_structural: true
	},
	/**@type {Entity}*/
	RuleConditionList: {
		name: 'RuleConditionList',
		children: {},
		relations: {},
		id: 'rule_condition_list',
		label: 'Condition list',
		plural_label: 'Condition lists',
		icon: 'bricks.png',
		configuration_name: 'RULE_CONDITION_LIST',
		comparison_structural: true
	},
	/**@type {Entity}*/
	RuleCondition: {
		name: 'RuleCondition',
		children: {},
		relations: {},
		id: 'rule_condition',
		label: 'Condition',
		plural_label: 'Conditions',
		icon: 'bricks.png',
		configuration_name: 'RULE_CONDITION',
		comparison_structural: true
	},
	/**@type {Entity}*/
	RuleConditionCriterion: {
		name: 'RuleConditionCriterion',
		children: {},
		relations: {},
		id: 'rule_condition_criterion',
		label: 'Rule condition criterion',
		plural_label: 'Rule condition criterions',
		configuration_name: 'RULE_CONDITION_CRITERION',
		comparison_structural: true
	},
	/**@type {Entity}*/
	RuleAction: {
		name: 'RuleAction',
		children: {},
		relations: {},
		id: 'rule_action',
		label: 'Rule action',
		plural_label: 'Rule actions',
		configuration_name: 'RULE_ACTION',
		comparison_structural: true
	},
	/**@type {Entity}*/
	RuleActionParameter: {
		name: 'RuleActionParameter',
		children: {},
		relations: {},
		id: 'rule_action_parameter',
		label: 'Rule action_parameter',
		plural_label: 'Rule action_parameters',
		configuration_name: 'RULE_ACTION_PARAMETER',
		comparison_structural: true
	},

	//other
	/**@type {Entity}*/
	EventConfigurationHook: {
		name: 'EventConfigurationHook',
		children: {},
		relations: {},
		id: 'event_configuration_hook',
		label: 'Event configuration hook',
		plural_label: 'Event configuration hooks',
		configuration_name: 'EVENT_CONFIGURATION_HOOK',
		comparison_structural: true
	},
	/**@type {Entity}*/
	SelectionNode: {
		name: 'SelectionNode',
		children: {},
		relations: {},
		id: 'selection_node',
		label: 'Selection node',
		plural_label: 'Selection nodes',
		configuration_name: 'SELECTION_NODE',
		comparison_structural: true
	},
});

for(const [name, entity] of Object.entries(Entities)) {
	if(entity.name !== name) {
		throw new Error(`Entity name ${name} does not match its id`);
	}
}

Entities.Action.children[Entities.Rule.name] = {size: 1};

Entities.Action.relations[Entities.WorkflowState.name] = {structuring: true};
Entities.Action.relations[Entities.Workflow.name] = {structuring: true};
Entities.Action.relations[Entities.Action.name] = {structuring: true};

Entities.FieldModel.children[Entities.PossibleValue.name] = {size: 1};
Entities.FieldModel.children[Entities.Rule.name] = {size: 1};
Entities.FieldModel.children[Entities.RuleConstraint.name] = {size: 1};
Entities.FieldModel.relations[Entities.FormModel.name] = {structuring: true};
Entities.FieldModel.relations[Entities.Workflow.name] = {structuring: true};
Entities.FieldModel.relations[Entities.TimelineGraphSection.name] = {structuring: true};

Entities.Cell.children[Entities.VisibilityCriteria.name] = {size: 1};
Entities.Cell.children[Entities.RuleConstraint.name] = {size: 1};
Entities.Cell.relations[Entities.DatasetModel.name] = {structuring: false};
Entities.Cell.relations[Entities.FieldModel.name] = {structuring: false};

Entities.CMSLayout.children[Entities.CMSSection.name] = {size: 1};

Entities.CMSSection.children[Entities.CMSWidget.name] = {size: 1};

Entities.Cron.children[Entities.Rule.name] = {size: 1};

Entities.DatasetModel.children[Entities.FieldModel.name] = {size: 1};
Entities.DatasetModel.children[Entities.Rule.name] = {size: 2, names: ['Delete rules', 'Restore rules']};
Entities.DatasetModel.relations[Entities.ScopeModel.name] = {structuring: true};
Entities.DatasetModel.relations[Entities.EventModel.name] = {structuring: true};

Entities.EventModel.children[Entities.Rule.name] = {size: 3, names: ['Create rules', 'Delete rules', 'Restore rules']};
Entities.EventModel.children[Entities.RuleConstraint.name] = {size: 1};
Entities.EventModel.relations[Entities.EventGroup.name] = {structuring: false};
Entities.EventModel.relations[Entities.DatasetModel.name] = {structuring: false};
Entities.EventModel.relations[Entities.FormModel.name] = {structuring: false};
Entities.EventModel.relations[Entities.Workflow.name] = {structuring: false};

Entities.EventGroup.relations[Entities.EventModel.name] = {structuring: true};

Entities.Layout.children[Entities.Line.name] = {size: 1};
Entities.Layout.children[Entities.Column.name] = {size: 1};
Entities.Layout.children[Entities.RuleConstraint.name] = {size: 1};

Entities.Line.children[Entities.Cell.name] = {size: 1};

Entities.FormModel.children[Entities.Layout.name] = {size: 1};
Entities.FormModel.children[Entities.Rule.name] = {size: 1};
Entities.FormModel.children[Entities.RuleConstraint.name] = {size: 1};
Entities.FormModel.relations[Entities.ScopeModel.name] = {structuring: true};
Entities.FormModel.relations[Entities.EventModel.name] = {structuring: true};
Entities.FormModel.relations[Entities.Workflow.name] = {structuring: false};
Entities.FormModel.relations[Entities.DatasetModel.name] = {structuring: false};
Entities.FormModel.relations[Entities.FieldModel.name] = {structuring: false};

Entities.PaymentPlan.children[Entities.PaymentStep.name] = {size: 1};

Entities.PaymentStep.children[Entities.PaymentDistribution.name] = {size: 1};

Entities.Menu.children[Entities.Menu.name] = {size: 1};
Entities.Menu.children[Entities.CMSLayout.name] = {size: 1};

Entities.RuleAction.children[Entities.RuleActionParameter.name] = {size: 1};

Entities.RuleConditionList.children[Entities.RuleCondition.name] = {size: 1};

Entities.RuleCondition.children[Entities.RuleCondition.name] = {size: 1};

Entities.RuleConstraint.children[Entities.RuleConditionList.name] = {size: 1};
Entities.RuleConstraint.children[Entities.RuleEvaluation.name] = {size: 1};

Entities.RuleDefinitionAction.children[Entities.RuleDefinitionActionParameter.name] = {size: 1};
Entities.RuleDefinitionAction.relations[Entities.Rule.name] = {structuring: true};

Entities.RuleDefinitionProperty.relations[Entities.Rule.name] = {structuring: true};

Entities.Rule.children[Entities.RuleConstraint.name] = {size: 1};
Entities.Rule.children[Entities.RuleAction.name] = {size: 1};

Entities.ScopeModel.children[Entities.EventModel.name] = {size: 1};
Entities.ScopeModel.children[Entities.EventGroup.name] = {size: 1};
Entities.ScopeModel.children[Entities.Rule.name] = {size: 3, names: ['Create rules', 'Delete rules', 'Restore rules']};
Entities.ScopeModel.children[Entities.CMSLayout.name] = {size: 1};
Entities.ScopeModel.relations[Entities.DatasetModel.name] = {structuring: false};
Entities.ScopeModel.relations[Entities.FormModel.name] = {structuring: false};
Entities.ScopeModel.relations[Entities.Workflow.name] = {structuring: false};

Entities.Study.children[Entities.ScopeModel.name] = {size: 1};
Entities.Study.children[Entities.DatasetModel.name] = {size: 1};
Entities.Study.children[Entities.Validator.name] = {size: 1};
Entities.Study.children[Entities.FormModel.name] = {size: 1};
Entities.Study.children[Entities.Workflow.name] = {size: 1};
Entities.Study.children[Entities.Profile.name] = {size: 1};
Entities.Study.children[Entities.Feature.name] = {size: 1};
Entities.Study.children[Entities.Language.name] = {size: 1};
Entities.Study.children[Entities.Menu.name] = {size: 1};
Entities.Study.children[Entities.PaymentPlan.name] = {size: 1};
Entities.Study.children[Entities.Chart.name] = {size: 1};
Entities.Study.children[Entities.TimelineGraph.name] = {size: 1};
Entities.Study.children[Entities.ResourceCategory.name] = {size: 1};
Entities.Study.children[Entities.PrivacyPolicy.name] = {size: 1};
Entities.Study.children[Entities.Report.name] = {size: 1};
Entities.Study.children[Entities.WorkflowWidget.name] = {size: 1};
Entities.Study.children[Entities.WorkflowSummary.name] = {size: 1};
Entities.Study.children[Entities.RuleDefinitionProperty.name] = {size: 1};
Entities.Study.children[Entities.RuleDefinitionAction.name] = {size: 1};
Entities.Study.children[Entities.Cron.name] = {size: 1};
Entities.Study.children[Entities.Rule.name] = {size: 17};
Entities.Study.children[Entities.SelectionNode.name] = {size: 1};

Entities.TimelineGraphSectionReference.children[Entities.TimelineGraphSectionReferenceEntry.name] = {size: 1};

Entities.TimelineGraphSection.children[Entities.TimelineGraphSectionReference.name] = {size: 1};

Entities.TimelineGraph.children[Entities.TimelineGraphSection.name] = {size: 1};

Entities.Validator.children[Entities.RuleConstraint.name] = {size: 1};
Entities.Validator.relations[Entities.FieldModel.name] = {structuring: true};

Entities.Workflow.children[Entities.WorkflowState.name] = {size: 1};
Entities.Workflow.children[Entities.Action.name] = {size: 1};
Entities.Workflow.children[Entities.Rule.name] = {size: 1};
Entities.Workflow.relations[Entities.ScopeModel.name] = {structuring: true};
Entities.Workflow.relations[Entities.EventModel.name] = {structuring: true};
Entities.Workflow.relations[Entities.FormModel.name] = {structuring: true};
Entities.Workflow.relations[Entities.FieldModel.name] = {structuring: true};
Entities.Workflow.relations[Entities.Validator.name] = {structuring: true};
Entities.Workflow.relations[Entities.Workflow.name] = {structuring: true};

Entities.SelectionNode.children[Entities.SelectionNode.name] = {size: 1};

function find_way_between_entities(property, source, target) {
	//keep a list of visited entities to avoid loop of hell
	const visited_entities = [];

	function find_way(current) {
		//check if entity as not already been visited
		if(visited_entities.includes(current)) {
			return [];
		}
		visited_entities.push(current);
		//check if target is a child / related entity
		if(current[property].hasOwnProperty(target.name)) {
			return [target];
		}
		//check in children / relation of all entities
		for(const entity_id in current[property]) {
			if(current[property].hasOwnProperty(entity_id)) {
				const entity = Entities[entity_id];
				const result = find_way(entity);
				if(!result.isEmpty()) {
					result.unshift(entity);
					return result;
				}
			}
		}
		return [];
	}

	return find_way(source);
}

function find_tree_way_between_entities(source, target) {
	return find_way_between_entities('children', source, target);
}

function find_relation_way_between_entities(source, target) {
	return find_way_between_entities('relations', source, target);
}

//default representation
function enhance_usable(link) {
	if(!this.isUsed()) {
		link.classList.add('unused');
	}
	else {
		link.classList.remove('unused');
	}
}

Object.values(Entities).forEach(entity => {
	//manage links between entities
	entity.getPath = function(other_entity) {
		return find_tree_way_between_entities(this, other_entity);
	};
	entity.isAncestorOf = function(other_entity) {
		return !find_tree_way_between_entities(this, other_entity).isEmpty();
	};
	entity.isDescendantOf = function(other_entity) {
		return other_entity.isAncestorOf(this);
	};
	entity.isDirectlyRelatedTo = function(other_entity) {
		return this.relations.hasOwnProperty(other_entity.name);
	};
	entity.isRelatedTo = function(other_entity) {
		return !find_relation_way_between_entities(this, other_entity).isEmpty();
	};
	if(entity) {
		if(!entity.representation && !Object.isEmpty(entity.relations)) {
			entity.representation = enhance_usable;
		}
	}
});

export {Entities};
