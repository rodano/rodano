package ch.rodano.batch.helper;

import java.util.UUID;

import org.jooq.DSLContext;

import ch.rodano.core.model.jooq.enums.RuleConstraintOwnerType;

import static ch.rodano.core.model.jooq.tables.Chart.CHART;
import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.EventGroup.EVENT_GROUP;
import static ch.rodano.core.model.jooq.tables.EventModel.EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.Feature.FEATURE;
import static ch.rodano.core.model.jooq.tables.FieldModel.FIELD_MODEL;
import static ch.rodano.core.model.jooq.tables.FieldPossibleValue.FIELD_POSSIBLE_VALUE;
import static ch.rodano.core.model.jooq.tables.FormLayoutCell.FORM_LAYOUT_CELL;
import static ch.rodano.core.model.jooq.tables.FormModel.FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.Menu.MENU;
import static ch.rodano.core.model.jooq.tables.PaymentPlan.PAYMENT_PLAN;
import static ch.rodano.core.model.jooq.tables.Profile.PROFILE;
import static ch.rodano.core.model.jooq.tables.Report.REPORT;
import static ch.rodano.core.model.jooq.tables.ResourceCategory.RESOURCE_CATEGORY;
import static ch.rodano.core.model.jooq.tables.RuleCondition.RULE_CONDITION;
import static ch.rodano.core.model.jooq.tables.RuleConditionList.RULE_CONDITION_LIST;
import static ch.rodano.core.model.jooq.tables.RuleConstraint.RULE_CONSTRAINT;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;
import static ch.rodano.core.model.jooq.tables.TimelineGraph.TIMELINE_GRAPH;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSection.TIMELINE_GRAPH_SECTION;
import static ch.rodano.core.model.jooq.tables.Validator.VALIDATOR;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;
import static ch.rodano.core.model.jooq.tables.WorkflowAction.WORKFLOW_ACTION;
import static ch.rodano.core.model.jooq.tables.WorkflowState.WORKFLOW_STATE;
import static ch.rodano.core.model.jooq.tables.WorkflowSummary.WORKFLOW_SUMMARY;
import static ch.rodano.core.model.jooq.tables.WorkflowWidget.WORKFLOW_WIDGET;

public class ModelResolvers {

	public static UUID resolveScopeModelId(final DSLContext tx, final UUID projectId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(SCOPE_MODEL.SCOPE_MODEL_ID)
			.from(SCOPE_MODEL)
			.where(SCOPE_MODEL.PROJECT_ID.eq(projectId)
				.and(SCOPE_MODEL.CODE.eq(code.trim())))
			.fetchOne(SCOPE_MODEL.SCOPE_MODEL_ID);
	}

	public static UUID resolveWorkflowId(final DSLContext tx, final UUID projectId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(WORKFLOW.WORKFLOW_ID)
			.from(WORKFLOW)
			.where(WORKFLOW.PROJECT_ID.eq(projectId)
				.and(WORKFLOW.CODE.eq(code.trim())))
			.fetchOne(WORKFLOW.WORKFLOW_ID);
	}

	public static UUID resolveWorkflowActionId(final DSLContext tx, final UUID projectId, final UUID workflowId, final String code) {
		if(workflowId == null || code == null || code.isBlank()) {
			return null;
		}
		return tx.select(WORKFLOW_ACTION.WORKFLOW_ACTION_ID)
			.from(WORKFLOW_ACTION)
			.where(WORKFLOW_ACTION.PROJECT_ID.eq(projectId)
				.and(WORKFLOW_ACTION.WORKFLOW_ID.eq(workflowId))
				.and(WORKFLOW_ACTION.CODE.eq(code)))
			.fetchOne(WORKFLOW_ACTION.WORKFLOW_ACTION_ID);
	}

	public static UUID resolveWorkflowStateId(final DSLContext tx, final UUID projectId, final UUID workflowId, final String code) {
		if(workflowId == null || code == null || code.isBlank()) {
			return null;
		}
		return tx.select(WORKFLOW_STATE.WORKFLOW_STATE_ID)
			.from(WORKFLOW_STATE)
			.where(WORKFLOW_STATE.PROJECT_ID.eq(projectId)
				.and(WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId))
				.and(WORKFLOW_STATE.CODE.eq(code)))
			.fetchOne(WORKFLOW_STATE.WORKFLOW_STATE_ID);
	}

	public static UUID resolveWorkflowWidgetId(final DSLContext tx, final UUID projectId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(WORKFLOW_WIDGET.WORKFLOW_WIDGET_ID)
			.from(WORKFLOW_WIDGET)
			.where(WORKFLOW_WIDGET.PROJECT_ID.eq(projectId)
				.and(WORKFLOW_WIDGET.CODE.eq(code)))
			.fetchOne(WORKFLOW_WIDGET.WORKFLOW_WIDGET_ID);
	}

	public static UUID resolveWorkflowSummaryId(final DSLContext tx, final UUID projectId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(WORKFLOW_SUMMARY.WORKFLOW_SUMMARY_ID)
			.from(WORKFLOW_SUMMARY)
			.where(WORKFLOW_SUMMARY.PROJECT_ID.eq(projectId)
				.and(WORKFLOW_SUMMARY.CODE.eq(code)))
			.fetchOne(WORKFLOW_SUMMARY.WORKFLOW_SUMMARY_ID);
	}

	public static UUID resolveProfileId(final DSLContext tx, final UUID projectId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(PROFILE.PROFILE_ID)
			.from(PROFILE)
			.where(PROFILE.PROJECT_ID.eq(projectId)
				.and(PROFILE.CODE.eq(code.trim())))
			.fetchOne(PROFILE.PROFILE_ID);
	}

	public static UUID resolveEventModelId(final DSLContext tx, final UUID projectId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(EVENT_MODEL.EVENT_MODEL_ID)
			.from(EVENT_MODEL)
			.where(EVENT_MODEL.PROJECT_ID.eq(projectId)
				.and(EVENT_MODEL.CODE.eq(code.trim())))
			.fetchOne(EVENT_MODEL.EVENT_MODEL_ID);
	}

	public static UUID resolveEventGroupId(final DSLContext tx, final UUID projectId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(EVENT_GROUP.EVENT_GROUP_ID)
			.from(EVENT_GROUP)
			.where(EVENT_GROUP.PROJECT_ID.eq(projectId)
				.and(EVENT_GROUP.CODE.eq(code.trim())))
			.fetchOne(EVENT_GROUP.EVENT_GROUP_ID);
	}

	public static UUID resolveEventGroupIdByScope(final DSLContext tx, final UUID projectId, final UUID scopeModelId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(EVENT_GROUP.EVENT_GROUP_ID)
			.from(EVENT_GROUP)
			.where(EVENT_GROUP.PROJECT_ID.eq(projectId)
				.and(EVENT_GROUP.SCOPE_MODEL_ID.eq(scopeModelId))
				.and(EVENT_GROUP.CODE.eq(code.trim())))
			.fetchOne(EVENT_GROUP.EVENT_GROUP_ID);
	}

	public static UUID resolveFormModelId(final DSLContext tx, final UUID projectId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(FORM_MODEL.FORM_MODEL_ID)
			.from(FORM_MODEL)
			.where(FORM_MODEL.PROJECT_ID.eq(projectId)
				.and(FORM_MODEL.CODE.eq(code)))
			.fetchOne(FORM_MODEL.FORM_MODEL_ID);
	}

	public static UUID resolveDatasetModelId(final DSLContext tx, final UUID projectId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(DATASET_MODEL.DATASET_MODEL_ID)
			.from(DATASET_MODEL)
			.where(DATASET_MODEL.PROJECT_ID.eq(projectId)
				.and(DATASET_MODEL.CODE.eq(code)))
			.fetchOne(DATASET_MODEL.DATASET_MODEL_ID);
	}

	public static UUID resolveFieldModelId(final DSLContext tx, final UUID projectId, final UUID datasetModelId, final String code) {
		if(datasetModelId == null || code == null || code.isBlank()) {
			return null;
		}
		return tx.select(FIELD_MODEL.FIELD_MODEL_ID)
			.from(FIELD_MODEL)
			.where(FIELD_MODEL.PROJECT_ID.eq(projectId)
				.and(FIELD_MODEL.DATASET_MODEL_ID.eq(datasetModelId))
				.and(FIELD_MODEL.CODE.eq(code)))
			.fetchOne(FIELD_MODEL.FIELD_MODEL_ID);
	}

	public static UUID resolvePossibleValueIdForCell(final DSLContext tx, final UUID projectId, final UUID formLayoutCellId, final String code) {
		if(formLayoutCellId == null || code == null || code.isBlank()) {
			return null;
		}

		final UUID fieldModelId = tx.select(FORM_LAYOUT_CELL.FIELD_MODEL_ID)
			.from(FORM_LAYOUT_CELL)
			.where(FORM_LAYOUT_CELL.PROJECT_ID.eq(projectId)
				.and(FORM_LAYOUT_CELL.FORM_LAYOUT_CELL_ID.eq(formLayoutCellId)))
			.fetchOne(FORM_LAYOUT_CELL.FIELD_MODEL_ID);

		if(fieldModelId == null) {
			return null;
		}

		final String trimmedCode = code.trim();
		return tx.select(FIELD_POSSIBLE_VALUE.POSSIBLE_VALUE_ID)
			.from(FIELD_POSSIBLE_VALUE)
			.where(FIELD_POSSIBLE_VALUE.PROJECT_ID.eq(projectId)
				.and(FIELD_POSSIBLE_VALUE.FIELD_MODEL_ID.eq(fieldModelId))
				.and(FIELD_POSSIBLE_VALUE.CODE.eq(trimmedCode)))
			.fetchOne(FIELD_POSSIBLE_VALUE.POSSIBLE_VALUE_ID);
	}

	public static UUID resolveResourceCategoryId(final DSLContext tx, final UUID projectId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(RESOURCE_CATEGORY.CATEGORY_ID)
			.from(RESOURCE_CATEGORY)
			.where(RESOURCE_CATEGORY.PROJECT_ID.eq(projectId)
				.and(RESOURCE_CATEGORY.CODE.eq(code)))
			.fetchOne(RESOURCE_CATEGORY.CATEGORY_ID);
	}

	public static UUID resolveMenuId(final DSLContext tx, final UUID projectId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(MENU.MENU_ID)
			.from(MENU)
			.where(MENU.PROJECT_ID.eq(projectId)
				.and(MENU.CODE.eq(code)))
			.fetchOne(MENU.MENU_ID);
	}

	public static UUID resolveFeatureId(final DSLContext tx, final UUID projectId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(FEATURE.FEATURE_ID)
			.from(FEATURE)
			.where(FEATURE.PROJECT_ID.eq(projectId)
				.and(FEATURE.CODE.eq(code)))
			.fetchOne(FEATURE.FEATURE_ID);
	}

	public static UUID resolvePaymentId(final DSLContext tx, final UUID projectId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(PAYMENT_PLAN.PAYMENT_PLAN_ID)
			.from(PAYMENT_PLAN)
			.where(PAYMENT_PLAN.PROJECT_ID.eq(projectId)
				.and(PAYMENT_PLAN.CODE.eq(code)))
			.fetchOne(PAYMENT_PLAN.PAYMENT_PLAN_ID);
	}

	public static UUID resolveTimelineGraphId(final DSLContext tx, final UUID projectId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(TIMELINE_GRAPH.TIMELINE_GRAPH_ID)
			.from(TIMELINE_GRAPH)
			.where(TIMELINE_GRAPH.PROJECT_ID.eq(projectId)
				.and(TIMELINE_GRAPH.CODE.eq(code)))
			.fetchOne(TIMELINE_GRAPH.TIMELINE_GRAPH_ID);
	}

	public static UUID resolveSectionId(final DSLContext tx, final UUID projectId, final UUID graphId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(TIMELINE_GRAPH_SECTION.GRAPH_SECTION_ID)
			.from(TIMELINE_GRAPH_SECTION)
			.where(TIMELINE_GRAPH_SECTION.PROJECT_ID.eq(projectId)
				.and(TIMELINE_GRAPH_SECTION.TIMELINE_GRAPH_ID.eq(graphId))
				.and(TIMELINE_GRAPH_SECTION.CODE.eq(code)))
			.fetchOne(TIMELINE_GRAPH_SECTION.GRAPH_SECTION_ID);
	}

	public static UUID resolveReportId(final DSLContext tx, final UUID projectId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(REPORT.REPORT_ID)
			.from(REPORT)
			.where(REPORT.PROJECT_ID.eq(projectId)
				.and(REPORT.CODE.eq(code)))
			.fetchOne(REPORT.REPORT_ID);
	}

	public static UUID resolveValidatorId(final DSLContext tx, final UUID projectId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(VALIDATOR.VALIDATOR_ID)
			.from(VALIDATOR)
			.where(VALIDATOR.PROJECT_ID.eq(projectId)
				.and(VALIDATOR.CODE.eq(code)))
			.fetchOne(VALIDATOR.VALIDATOR_ID);
	}

	public static UUID resolveChartId(final DSLContext tx, final UUID projectId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}
		return tx.select(CHART.CHART_ID)
			.from(CHART)
			.where(CHART.PROJECT_ID.eq(projectId)
				.and(CHART.CODE.eq(code)))
			.fetchOne(CHART.CHART_ID);
	}

	public static UUID resolveRuleConditionId(final DSLContext tx, final UUID projectId, final UUID ruleId, final String code) {
		if(code == null || code.isBlank()) {
			return null;
		}

		return tx.select(RULE_CONDITION.CONDITION_ID)
			.from(RULE_CONDITION)
			.join(RULE_CONDITION_LIST)
			.on(RULE_CONDITION.CONDITION_LIST_ID.eq(RULE_CONDITION_LIST.CONDITION_LIST_ID)
				.and(RULE_CONDITION_LIST.PROJECT_ID.eq(projectId)))
			.join(RULE_CONSTRAINT)
			.on(RULE_CONDITION_LIST.CONSTRAINT_ID.eq(RULE_CONSTRAINT.CONSTRAINT_ID)
				.and(RULE_CONSTRAINT.PROJECT_ID.eq(projectId)))
			.where(RULE_CONSTRAINT.OWNER_TYPE.eq(RuleConstraintOwnerType.RULE))
			.and(RULE_CONSTRAINT.OWNER_ID.eq(ruleId))
			.and(RULE_CONDITION.CODE.eq(code))
			.fetchOne(RULE_CONDITION.CONDITION_ID);
	}
}
