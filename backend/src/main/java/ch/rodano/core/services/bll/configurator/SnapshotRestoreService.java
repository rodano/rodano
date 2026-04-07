package ch.rodano.core.services.bll.configurator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.TableField;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static ch.rodano.core.model.jooq.tables.Chart.CHART;
import static ch.rodano.core.model.jooq.tables.ChartColor.CHART_COLOR;
import static ch.rodano.core.model.jooq.tables.ChartRange.CHART_RANGE;
import static ch.rodano.core.model.jooq.tables.ChartStateFilter.CHART_STATE_FILTER;
import static ch.rodano.core.model.jooq.tables.Cron.CRON;
import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.EventGroup.EVENT_GROUP;
import static ch.rodano.core.model.jooq.tables.EventModel.EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelBlockedEvent.EVENT_MODEL_BLOCKED_EVENT;
import static ch.rodano.core.model.jooq.tables.EventModelDatasetModel.EVENT_MODEL_DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelDeadlineReference.EVENT_MODEL_DEADLINE_REFERENCE;
import static ch.rodano.core.model.jooq.tables.EventModelFormModel.EVENT_MODEL_FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelImpliedEvent.EVENT_MODEL_IMPLIED_EVENT;
import static ch.rodano.core.model.jooq.tables.EventModelWorkflow.EVENT_MODEL_WORKFLOW;
import static ch.rodano.core.model.jooq.tables.Feature.FEATURE;
import static ch.rodano.core.model.jooq.tables.FieldModel.FIELD_MODEL;
import static ch.rodano.core.model.jooq.tables.FieldModelValidator.FIELD_MODEL_VALIDATOR;
import static ch.rodano.core.model.jooq.tables.FieldModelWorkflow.FIELD_MODEL_WORKFLOW;
import static ch.rodano.core.model.jooq.tables.FieldPossibleValue.FIELD_POSSIBLE_VALUE;
import static ch.rodano.core.model.jooq.tables.FormCellVisibilityCriteria.FORM_CELL_VISIBILITY_CRITERIA;
import static ch.rodano.core.model.jooq.tables.FormCellVisibilityCriteriaTargetCell.FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL;
import static ch.rodano.core.model.jooq.tables.FormCellVisibilityCriteriaTargetLayout.FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT;
import static ch.rodano.core.model.jooq.tables.FormCellVisibilityCriteriaValue.FORM_CELL_VISIBILITY_CRITERIA_VALUE;
import static ch.rodano.core.model.jooq.tables.FormLayout.FORM_LAYOUT;
import static ch.rodano.core.model.jooq.tables.FormLayoutCell.FORM_LAYOUT_CELL;
import static ch.rodano.core.model.jooq.tables.FormLayoutColumn.FORM_LAYOUT_COLUMN;
import static ch.rodano.core.model.jooq.tables.FormLayoutLine.FORM_LAYOUT_LINE;
import static ch.rodano.core.model.jooq.tables.FormModel.FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.FormModelWorkflow.FORM_MODEL_WORKFLOW;
import static ch.rodano.core.model.jooq.tables.Menu.MENU;
import static ch.rodano.core.model.jooq.tables.MenuLayoutSection.MENU_LAYOUT_SECTION;
import static ch.rodano.core.model.jooq.tables.MenuLayoutSectionWidget.MENU_LAYOUT_SECTION_WIDGET;
import static ch.rodano.core.model.jooq.tables.MenuLayoutSectionWidgetParameter.MENU_LAYOUT_SECTION_WIDGET_PARAMETER;
import static ch.rodano.core.model.jooq.tables.PaymentPlan.PAYMENT_PLAN;
import static ch.rodano.core.model.jooq.tables.PaymentStep.PAYMENT_STEP;
import static ch.rodano.core.model.jooq.tables.PaymentStepDistribution.PAYMENT_STEP_DISTRIBUTION;
import static ch.rodano.core.model.jooq.tables.PrivacyPolicy.PRIVACY_POLICY;
import static ch.rodano.core.model.jooq.tables.PrivacyPolicyProfile.PRIVACY_POLICY_PROFILE;
import static ch.rodano.core.model.jooq.tables.Profile.PROFILE;
import static ch.rodano.core.model.jooq.tables.ProfileCategoryGrants.PROFILE_CATEGORY_GRANTS;
import static ch.rodano.core.model.jooq.tables.ProfileDatasetModelRights.PROFILE_DATASET_MODEL_RIGHTS;
import static ch.rodano.core.model.jooq.tables.ProfileEventModelRights.PROFILE_EVENT_MODEL_RIGHTS;
import static ch.rodano.core.model.jooq.tables.ProfileFeatureGrants.PROFILE_FEATURE_GRANTS;
import static ch.rodano.core.model.jooq.tables.ProfileFormModelRights.PROFILE_FORM_MODEL_RIGHTS;
import static ch.rodano.core.model.jooq.tables.ProfileMenuGrants.PROFILE_MENU_GRANTS;
import static ch.rodano.core.model.jooq.tables.ProfilePaymentModelRights.PROFILE_PAYMENT_MODEL_RIGHTS;
import static ch.rodano.core.model.jooq.tables.ProfileProfileRights.PROFILE_PROFILE_RIGHTS;
import static ch.rodano.core.model.jooq.tables.ProfileReportGrants.PROFILE_REPORT_GRANTS;
import static ch.rodano.core.model.jooq.tables.ProfileScopeModelRights.PROFILE_SCOPE_MODEL_RIGHTS;
import static ch.rodano.core.model.jooq.tables.ProfileTimelineGraphGrants.PROFILE_TIMELINE_GRAPH_GRANTS;
import static ch.rodano.core.model.jooq.tables.ProfileWorkflowActionRights.PROFILE_WORKFLOW_ACTION_RIGHTS;
import static ch.rodano.core.model.jooq.tables.ProfileWorkflowRights.PROFILE_WORKFLOW_RIGHTS;
import static ch.rodano.core.model.jooq.tables.ProjectLanguage.PROJECT_LANGUAGE;
import static ch.rodano.core.model.jooq.tables.ProjectRuleTag.PROJECT_RULE_TAG;
import static ch.rodano.core.model.jooq.tables.Report.REPORT;
import static ch.rodano.core.model.jooq.tables.ReportField.REPORT_FIELD;
import static ch.rodano.core.model.jooq.tables.ResourceCategory.RESOURCE_CATEGORY;
import static ch.rodano.core.model.jooq.tables.Rule.RULE;
import static ch.rodano.core.model.jooq.tables.RuleAction.RULE_ACTION;
import static ch.rodano.core.model.jooq.tables.RuleActionParameter.RULE_ACTION_PARAMETER;
import static ch.rodano.core.model.jooq.tables.RuleCondition.RULE_CONDITION;
import static ch.rodano.core.model.jooq.tables.RuleConditionList.RULE_CONDITION_LIST;
import static ch.rodano.core.model.jooq.tables.RuleConstraint.RULE_CONSTRAINT;
import static ch.rodano.core.model.jooq.tables.RuleCriterion.RULE_CRITERION;
import static ch.rodano.core.model.jooq.tables.RuleCriterionValue.RULE_CRITERION_VALUE;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelDatasetModel.SCOPE_MODEL_DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelFormModel.SCOPE_MODEL_FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelParent.SCOPE_MODEL_PARENT;
import static ch.rodano.core.model.jooq.tables.ScopeModelWorkflow.SCOPE_MODEL_WORKFLOW;
import static ch.rodano.core.model.jooq.tables.ScopeModelWorkflowStateSelector.SCOPE_MODEL_WORKFLOW_STATE_SELECTOR;
import static ch.rodano.core.model.jooq.tables.TimelineGraph.TIMELINE_GRAPH;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSection.TIMELINE_GRAPH_SECTION;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionEvent.TIMELINE_GRAPH_SECTION_EVENT;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionMetaField.TIMELINE_GRAPH_SECTION_META_FIELD;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionReference.TIMELINE_GRAPH_SECTION_REFERENCE;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionReferenceEntry.TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY;
import static ch.rodano.core.model.jooq.tables.Validator.VALIDATOR;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;
import static ch.rodano.core.model.jooq.tables.WorkflowAction.WORKFLOW_ACTION;
import static ch.rodano.core.model.jooq.tables.WorkflowState.WORKFLOW_STATE;
import static ch.rodano.core.model.jooq.tables.WorkflowStatePossibleAction.WORKFLOW_STATE_POSSIBLE_ACTION;
import static ch.rodano.core.model.jooq.tables.WorkflowSummary.WORKFLOW_SUMMARY;
import static ch.rodano.core.model.jooq.tables.WorkflowSummaryColumn.WORKFLOW_SUMMARY_COLUMN;
import static ch.rodano.core.model.jooq.tables.WorkflowSummaryColumnState.WORKFLOW_SUMMARY_COLUMN_STATE;
import static ch.rodano.core.model.jooq.tables.WorkflowSummaryFilterEventModel.WORKFLOW_SUMMARY_FILTER_EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.WorkflowSummaryWorkflow.WORKFLOW_SUMMARY_WORKFLOW;
import static ch.rodano.core.model.jooq.tables.WorkflowWidget.WORKFLOW_WIDGET;
import static ch.rodano.core.model.jooq.tables.WorkflowWidgetColumn.WORKFLOW_WIDGET_COLUMN;
import static ch.rodano.core.model.jooq.tables.WorkflowWidgetStateSelector.WORKFLOW_WIDGET_STATE_SELECTOR;

@Component
public class SnapshotRestoreService {

	private final DSLContext dslContext;

	private static final List<Table<?>> TABLE_ORDER = List.of(
		FEATURE, WORKFLOW, WORKFLOW_STATE, WORKFLOW_ACTION,
		WORKFLOW_STATE_POSSIBLE_ACTION, SCOPE_MODEL, EVENT_GROUP,
		EVENT_MODEL, EVENT_MODEL_DATASET_MODEL, EVENT_MODEL_FORM_MODEL,
		EVENT_MODEL_WORKFLOW, EVENT_MODEL_BLOCKED_EVENT,
		EVENT_MODEL_IMPLIED_EVENT, EVENT_MODEL_DEADLINE_REFERENCE,
		SCOPE_MODEL_PARENT, SCOPE_MODEL_DATASET_MODEL,
		SCOPE_MODEL_FORM_MODEL, SCOPE_MODEL_WORKFLOW,
		SCOPE_MODEL_WORKFLOW_STATE_SELECTOR,
		DATASET_MODEL, FIELD_MODEL, FIELD_POSSIBLE_VALUE,
		FIELD_MODEL_VALIDATOR, FIELD_MODEL_WORKFLOW,
		FORM_MODEL, FORM_MODEL_WORKFLOW, FORM_LAYOUT,
		FORM_LAYOUT_COLUMN, FORM_LAYOUT_LINE, FORM_LAYOUT_CELL,
		FORM_CELL_VISIBILITY_CRITERIA,
		FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL,
		FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT,
		FORM_CELL_VISIBILITY_CRITERIA_VALUE,
		VALIDATOR, PROFILE,
		PROFILE_SCOPE_MODEL_RIGHTS, PROFILE_DATASET_MODEL_RIGHTS,
		PROFILE_EVENT_MODEL_RIGHTS, PROFILE_FORM_MODEL_RIGHTS,
		PROFILE_WORKFLOW_RIGHTS, PROFILE_WORKFLOW_ACTION_RIGHTS,
		PROFILE_FEATURE_GRANTS, PROFILE_MENU_GRANTS,
		PROFILE_REPORT_GRANTS, PROFILE_TIMELINE_GRAPH_GRANTS,
		PROFILE_CATEGORY_GRANTS, PROFILE_PAYMENT_MODEL_RIGHTS,
		PROFILE_PROFILE_RIGHTS,
		PAYMENT_PLAN, PAYMENT_STEP, PAYMENT_STEP_DISTRIBUTION,
		PRIVACY_POLICY, PRIVACY_POLICY_PROFILE,
		RESOURCE_CATEGORY, REPORT, REPORT_FIELD,
		WORKFLOW_SUMMARY, WORKFLOW_SUMMARY_WORKFLOW,
		WORKFLOW_SUMMARY_FILTER_EVENT_MODEL, WORKFLOW_SUMMARY_COLUMN,
		WORKFLOW_SUMMARY_COLUMN_STATE,
		WORKFLOW_WIDGET, WORKFLOW_WIDGET_STATE_SELECTOR,
		WORKFLOW_WIDGET_COLUMN,
		CHART, CHART_COLOR, CHART_STATE_FILTER, CHART_RANGE,
		TIMELINE_GRAPH, TIMELINE_GRAPH_SECTION,
		TIMELINE_GRAPH_SECTION_EVENT, TIMELINE_GRAPH_SECTION_META_FIELD,
		TIMELINE_GRAPH_SECTION_REFERENCE,
		TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY,
		MENU, MENU_LAYOUT_SECTION, MENU_LAYOUT_SECTION_WIDGET,
		MENU_LAYOUT_SECTION_WIDGET_PARAMETER,
		CRON, RULE, RULE_CONSTRAINT, RULE_CONDITION_LIST,
		RULE_CONDITION, RULE_CRITERION, RULE_CRITERION_VALUE,
		RULE_ACTION, RULE_ACTION_PARAMETER,
		PROJECT_LANGUAGE, PROJECT_RULE_TAG
	);

	public SnapshotRestoreService(final DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	public Map<String, String> captureProjectData(final UUID projectId) {
		final Map<String, String> tables = new LinkedHashMap<>();
		for(final Table<?> table : TABLE_ORDER) {
			final var projectIdField = (TableField<?, UUID>) table.field("project_id");
			if(projectIdField == null) {
				continue;
			}
			final String json = dslContext.selectFrom(table)
				.where(projectIdField.eq(projectId))
				.fetch()
				.formatJSON();
			tables.put(table.getName(), json);
		}
		return tables;
	}

	@Transactional
	public void restoreProjectData(final UUID projectId, final Map<String, String> tables) {
		deleteAll(projectId);
		dslContext.execute("SET FOREIGN_KEY_CHECKS = 0");
		try {
			for(final Table<?> table : TABLE_ORDER) {
				final String json = tables.get(table.getName());
				if(json == null) {
					continue;
				}
				dslContext.loadInto(table)
					.loadJSON(json)
					.fields(table.fields())
					.execute();
			}
		}
		catch(IOException e) {
			throw new RuntimeException("Failed to restore snapshot", e);
		}
		finally {
			dslContext.execute("SET FOREIGN_KEY_CHECKS = 1");
		}
	}

	private void deleteAll(final UUID projectId) {
		final var reversed = new ArrayList<>(TABLE_ORDER);
		Collections.reverse(reversed);
		dslContext.execute("SET FOREIGN_KEY_CHECKS = 0");
		try {
			for(final Table<?> table : reversed) {
				final var projectIdField = table.field("project_id", UUID.class);
				if(projectIdField == null) {
					continue;
				}
				dslContext.deleteFrom(table)
					.where(projectIdField.eq(projectId))
					.execute();
			}
		}
		finally {
			dslContext.execute("SET FOREIGN_KEY_CHECKS = 1");
		}
	}
}
