import '../../basic-tools/extension.js';

import {Study} from './entities/study.js';
import {ScopeModel} from './entities/scope_model.js';
import {EventGroup} from './entities/event_group.js';
import {EventModel} from './entities/event_model.js';
import {DatasetModel} from './entities/dataset_model.js';
import {FieldModel} from './entities/field_model.js';
import {PossibleValue} from './entities/possible_value.js';
import {VisibilityCriteria} from './entities/visibility_criteria.js';
import {Validator} from './entities/validator.js';
import {ValueSource} from './entities/value_source.js';
import {ValueSourceCriteria} from './entities/value_source_criteria.js';
import {AttributeCriterion} from './entities/attribute_criterion.js';
import {FormModel} from './entities/form_model.js';
import {Layout} from './entities/layout.js';
import {Column} from './entities/column.js';
import {Line} from './entities/line.js';
import {Cell} from './entities/cell.js';
import {Language} from './entities/language.js';
import {Workflow} from './entities/workflow.js';
import {WorkflowState} from './entities/workflow_state.js';
import {Action} from './entities/action.js';
import {Profile} from './entities/profile.js';
import {Right} from './entities/right.js';
import {ProfileRight} from './entities/profile_right.js';
import {Feature} from './entities/feature.js';
import {PaymentPlan} from './entities/payment_plan.js';
import {PaymentStep} from './entities/payment_step.js';
import {PaymentDistribution} from './entities/payment_distribution.js';
import {Menu} from './entities/menu.js';
import {ResourceCategory} from './entities/resource_category.js';
import {PrivacyPolicy} from './entities/privacy_policy.js';
import {Report} from './entities/report.js';
import {WorkflowWidget} from './entities/workflow_widget.js';
import {WorkflowStatesSelector} from './entities/workflow_states_selector.js';
import {WorkflowSummary} from './entities/workflow_summary.js';
import {WorkflowWidgetColumn} from './entities/workflow_widget_column.js';
import {WorkflowSummaryColumn} from './entities/workflow_summary_column.js';
import {Chart} from './entities/chart.js';
import {ChartRange} from './entities/chart_range.js';
import {ChartRequest} from './entities/chart_request.js';
import {TimelineGraph} from './entities/timeline_graph.js';
import {TimelineGraphSection} from './entities/timeline_graph_section.js';
import {TimelineGraphSectionPosition} from './entities/timeline_graph_section_position.js';
import {TimelineGraphSectionScale} from './entities/timeline_graph_section_scale.js';
import {TimelineGraphSectionReference} from './entities/timeline_graph_section_reference.js';
import {TimelineGraphSectionReferenceEntry} from './entities/timeline_graph_section_reference_entry.js';
import {CMSLayout} from './entities/cms_layout.js';
import {CMSSection} from './entities/cms_section.js';
import {CMSWidget} from './entities/cms_widget.js';
import {CMSAction} from './entities/cms_action.js';
import {ScopeCriterionRight} from './entities/scope_criterion_right.js';
import {RuleDefinitionProperty} from './entities/rule_definition_property.js';
import {RuleDefinitionAction} from './entities/rule_definition_action.js';
import {RuleDefinitionActionParameter} from './entities/rule_definition_action_parameter.js';
import {Rule} from './entities/rule.js';
import {RuleConstraint} from './entities/rule_constraint.js';
import {RuleEvaluation} from './entities/rule_evaluation.js';
import {RuleConditionList} from './entities/rule_condition_list.js';
import {RuleCondition} from './entities/rule_condition.js';
import {RuleConditionCriterion} from './entities/rule_condition_criterion.js';
import {RuleAction} from './entities/rule_action.js';
import {RuleActionParameter} from './entities/rule_action_parameter.js';
import {EventConfigurationHook} from './entities/event_configuration_hook.js';
import {Changelog} from './entities/changelog.js';
import {Cron} from './entities/cron.js';
import {FieldModelType} from './field_model_type.js';
import {ChartType} from './chart_type.js';
import {WorkflowEntities} from './workflow_entities.js';
import {RuleEntities} from './rule_entities.js';
import {Trigger} from './trigger.js';
import {EventTimeUnit} from './event_time_unit.js';
import {DateAggregationFunction} from './date_aggregation_function.js';
import {WorkflowStateMatcher} from './workflow_state_matcher.js';
import {LabelType} from './label_type.js';
import {Operator} from './operator.js';
import {DataType} from './data_type.js';
import {VisibilityCriteriaAction} from './visibility_criteria_action.js';
import {LayoutType} from './layout_type.js';
import {TimelineSectionType} from './timeline_section_type.js';
import {TimelineSectionScalePosition} from './timeline_section_scale_position.js';
import {TimelineSectionMark} from './timeline_section_mark.js';
import {RuleConditionMode} from './rule_condition_mode.js';
import {WorkflowWidgetColumnType} from './workflow_widget_column_type.js';
import {ProfileRightType} from './profile_right_type.js';
import {StaticActions} from './static_actions.js';
import {Formulas} from './formulas.js';
import {NativeType} from './native_types.js';
import {WidgetTypes} from './widget_types_config.js';
import {SelectionNode} from './entities/selection_node.js';

//TODO remove this class when all references to entities are static
export function create_config() {

	const Config = {};

	Config.Entities = {
		Study: Study,

		Language: Language,

		//data
		ScopeModel: ScopeModel,
		EventGroup: EventGroup,
		EventModel: EventModel,
		DatasetModel: DatasetModel,
		FieldModel: FieldModel,
		PossibleValue: PossibleValue,
		Validator: Validator,
		ValueSource: ValueSource,
		ValueSourceCriteria: ValueSourceCriteria,
		AttributeCriterion: AttributeCriterion,

		//ui
		FormModel: FormModel,
		Layout: Layout,
		Column: Column,
		Line: Line,
		Cell: Cell,
		VisibilityCriteria: VisibilityCriteria,

		//workflow
		Workflow: Workflow,
		WorkflowState: WorkflowState,
		Action: Action,

		//rights
		Profile: Profile,
		Right: Right,
		ProfileRight: ProfileRight,
		Feature: Feature,

		//payment
		PaymentPlan: PaymentPlan,
		PaymentStep: PaymentStep,
		PaymentDistribution: PaymentDistribution,

		//ui
		Menu: Menu,
		ResourceCategory: ResourceCategory,
		PrivacyPolicy: PrivacyPolicy,
		Changelog: Changelog,

		//report
		Report: Report,
		Cron: Cron,

		//workflow widgets
		WorkflowWidget: WorkflowWidget,
		WorkflowStatesSelector: WorkflowStatesSelector,
		WorkflowWidgetColumn: WorkflowWidgetColumn,
		WorkflowSummary: WorkflowSummary,
		WorkflowSummaryColumn: WorkflowSummaryColumn,

		//graphs
		Chart: Chart,
		ChartRange: ChartRange,
		ChartRequest: ChartRequest,

		//timeline graph
		TimelineGraph: TimelineGraph,
		TimelineGraphSection: TimelineGraphSection,
		TimelineGraphSectionScale: TimelineGraphSectionScale,
		TimelineGraphSectionPosition: TimelineGraphSectionPosition,
		TimelineGraphSectionReference: TimelineGraphSectionReference,
		TimelineGraphSectionReferenceEntry: TimelineGraphSectionReferenceEntry,

		//cms
		CMSLayout: CMSLayout,
		CMSSection: CMSSection,
		CMSWidget: CMSWidget,
		CMSAction: CMSAction,
		ScopeCriterionRight: ScopeCriterionRight,

		//rule
		RuleDefinitionProperty: RuleDefinitionProperty,
		RuleDefinitionAction: RuleDefinitionAction,
		RuleDefinitionActionParameter: RuleDefinitionActionParameter,
		Rule: Rule,
		RuleConstraint: RuleConstraint,
		RuleEvaluation: RuleEvaluation,
		RuleConditionList: RuleConditionList,
		RuleCondition: RuleCondition,
		RuleConditionCriterion: RuleConditionCriterion,
		RuleAction: RuleAction,
		RuleActionParameter: RuleActionParameter,

		//other
		EventConfigurationHook: EventConfigurationHook,
		SelectionNode: SelectionNode
	};

	//enums
	Config.Enums = {
		WorkflowEntities: WorkflowEntities,
		RuleEntities: RuleEntities,
		StaticActions: StaticActions,
		Trigger: Trigger,
		ChartType: ChartType,
		VisibilityCriteriaAction: VisibilityCriteriaAction,
		EventTimeUnit: EventTimeUnit,
		DateAggregationFunction: DateAggregationFunction,
		WorkflowStateMatcher: WorkflowStateMatcher,
		LabelType: LabelType,
		LayoutType: LayoutType,
		TimelineSectionType: TimelineSectionType,
		TimelineSectionMark: TimelineSectionMark,
		TimelineSectionScalePosition: TimelineSectionScalePosition,
		RuleConditionMode: RuleConditionMode,
		ProfileRightType: ProfileRightType,
		Operator: Operator,
		NativeType: NativeType,
		DataType: DataType,
		FieldModelType: FieldModelType,
		WorkflowWidgetColumnType: WorkflowWidgetColumnType,
		Formulas: Formulas,
		WidgetTypes: WidgetTypes
	};

	//prototype a name method to all enums
	const enumerables = [
		NativeType,
		LabelType,
		DataType,
		Operator,
		RuleEntities,
		Trigger,
		EventTimeUnit,
		FieldModelType,
		WorkflowEntities,
		VisibilityCriteriaAction,
		LayoutType,
		TimelineSectionType,
		TimelineSectionMark,
		TimelineSectionScalePosition,
		RuleConditionMode,
		WorkflowWidgetColumnType,
		ProfileRightType,
		Formulas
	];
	enumerables.forEach(function(enumerable) {
		for(const [key, value] of Object.entries(enumerable)) {
			if(Object.isObject(value)) {
				value.name = key;
			}
		}
	});

	return Config;
}
