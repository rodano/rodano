export const CONFIGURATION_ENTITY_OPTIONS = [
	'Study', 'Language', 'ScopeModel', 'EventGroup', 'EventModel', 'DatasetModel', 'FieldModel', 'PossibleValue',
	'Validator', 'ValueSource', 'ValueSourceCriteria', 'AttributeCriterion', 'FormModel', 'Layout', 'Column', 'Line',
	'Cell', 'VisibilityCriteria', 'Workflow', 'WorkflowState', 'Action', 'Profile', 'Right', 'ProfileRight',
	'Feature', 'PaymentPlan', 'PaymentStep', 'PaymentDistribution', 'Menu', 'ResourceCategory', 'PrivacyPolicy', 'Changelog',
	'Report', 'Cron', 'WorkflowWidget', 'WorkflowStateSelector', 'WorkflowWidgetColumn', 'WorkflowSummary', 'WorkflowSummaryColumn',
	'Chart', 'ChartRange', 'TimelineGraph', 'TimelineGraphSection', 'TimelineGraphSectionScale', 'TimelineGraphSectionPosition',
	'TimelineGraphSectionReference', 'TimelineGraphSectionReferenceEntry', 'CMSLayout', 'CMSSection', 'CMSWidget', 'CMSAction',
	'ScopeCriterionRight', 'RuleDefinitionProperty', 'RuleDefinitionAction', 'RuleDefinitionActionParameter', 'Rule',
	'RuleConstraint', 'RuleEvaluation', 'RuleConditionList', 'RuleCondition', 'RuleConditionCriterion', 'RuleAction',
	'RuleActionParameter', 'EventConfigurationHook', 'SelectionNode'
] as const;
export type ConfigurationEntityOptions = typeof CONFIGURATION_ENTITY_OPTIONS[number];
