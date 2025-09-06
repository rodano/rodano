package ch.rodano.batch;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import ch.rodano.batch.reader.JsonPointerItemReader;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.test.SpringTestConfiguration;
import ch.rodano.test.StatelessDatabaseTest;

import static ch.rodano.core.model.jooq.Tables.MENU_LAYOUT_SECTION_WIDGET;
import static ch.rodano.core.model.jooq.Tables.PROFILE_DATASET_MODEL_RIGHTS;
import static ch.rodano.core.model.jooq.Tables.PROFILE_EVENT_MODEL_RIGHTS;
import static ch.rodano.core.model.jooq.Tables.PROFILE_FORM_MODEL_RIGHTS;
import static ch.rodano.core.model.jooq.Tables.PROFILE_PAYMENT_MODEL_RIGHTS;
import static ch.rodano.core.model.jooq.Tables.PROFILE_PROFILE_RIGHTS;
import static ch.rodano.core.model.jooq.Tables.PROFILE_WORKFLOW_RIGHTS;
import static ch.rodano.core.model.jooq.tables.Chart.CHART;
import static ch.rodano.core.model.jooq.tables.ChartRange.CHART_RANGE;
import static ch.rodano.core.model.jooq.tables.Cron.CRON;
import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.EventGroup.EVENT_GROUP;
import static ch.rodano.core.model.jooq.tables.EventModel.EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.Feature.FEATURE;
import static ch.rodano.core.model.jooq.tables.FieldModel.FIELD_MODEL;
import static ch.rodano.core.model.jooq.tables.FieldPossibleValue.FIELD_POSSIBLE_VALUE;
import static ch.rodano.core.model.jooq.tables.FormCellVisibilityCriteria.FORM_CELL_VISIBILITY_CRITERIA;
import static ch.rodano.core.model.jooq.tables.FormLayout.FORM_LAYOUT;
import static ch.rodano.core.model.jooq.tables.FormLayoutCell.FORM_LAYOUT_CELL;
import static ch.rodano.core.model.jooq.tables.FormLayoutColumn.FORM_LAYOUT_COLUMN;
import static ch.rodano.core.model.jooq.tables.FormLayoutLine.FORM_LAYOUT_LINE;
import static ch.rodano.core.model.jooq.tables.FormModel.FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.Menu.MENU;
import static ch.rodano.core.model.jooq.tables.MenuLayoutSection.MENU_LAYOUT_SECTION;
import static ch.rodano.core.model.jooq.tables.PaymentPlan.PAYMENT_PLAN;
import static ch.rodano.core.model.jooq.tables.PaymentStep.PAYMENT_STEP;
import static ch.rodano.core.model.jooq.tables.PrivacyPolicy.PRIVACY_POLICY;
import static ch.rodano.core.model.jooq.tables.Profile.PROFILE;
import static ch.rodano.core.model.jooq.tables.ProfileScopeModelRights.PROFILE_SCOPE_MODEL_RIGHTS;
import static ch.rodano.core.model.jooq.tables.Report.REPORT;
import static ch.rodano.core.model.jooq.tables.ResourceCategory.RESOURCE_CATEGORY;
import static ch.rodano.core.model.jooq.tables.RuleDefinitionAction.RULE_DEFINITION_ACTION;
import static ch.rodano.core.model.jooq.tables.RuleDefinitionActionParameter.RULE_DEFINITION_ACTION_PARAMETER;
import static ch.rodano.core.model.jooq.tables.RuleDefinitionProperty.RULE_DEFINITION_PROPERTY;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;
import static ch.rodano.core.model.jooq.tables.SelectionNode.SELECTION_NODE;
import static ch.rodano.core.model.jooq.tables.TimelineGraph.TIMELINE_GRAPH;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSection.TIMELINE_GRAPH_SECTION;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionReference.TIMELINE_GRAPH_SECTION_REFERENCE;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionReferenceEntry.TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY;
import static ch.rodano.core.model.jooq.tables.Validator.VALIDATOR;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;
import static ch.rodano.core.model.jooq.tables.WorkflowAction.WORKFLOW_ACTION;
import static ch.rodano.core.model.jooq.tables.WorkflowState.WORKFLOW_STATE;
import static ch.rodano.core.model.jooq.tables.WorkflowSummary.WORKFLOW_SUMMARY;
import static ch.rodano.core.model.jooq.tables.WorkflowSummaryColumn.WORKFLOW_SUMMARY_COLUMN;
import static ch.rodano.core.model.jooq.tables.WorkflowWidget.WORKFLOW_WIDGET;
import static ch.rodano.core.model.jooq.tables.WorkflowWidgetColumn.WORKFLOW_WIDGET_COLUMN;
import static ch.rodano.core.model.jooq.tables.WorkflowWidgetStateSelector.WORKFLOW_WIDGET_STATE_SELECTOR;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EnabledIfEnvironmentVariable(named = "RUN_BATCH_TESTS", matches = "true")
@SpringTestConfiguration
@Transactional
public class BatchImportVerificationTest extends StatelessDatabaseTest {

	private JsonNode configJson;

	@Value("${batch.import.config}")
	private String configPath;

	@Autowired
	private DSLContext dsl;

	@BeforeEach
	public void loadConfigJson() throws Exception {
		final var reader = new JsonPointerItemReader();

		setField(reader, "resource", configPath);
		setField(reader, "jsonPointer", "");
		setField(reader, "beanType", JsonNode.class.getName());

		reader.open(null);

		configJson = (JsonNode) reader.readItem();

		reader.close();
	}

	private void setField(Object target, String fieldName, Object value) throws Exception {
		var field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}

	@Test
	@DisplayName("Batch import creates correct number of scope models")
	public void scopeModelsImportedCorrectly() {
		final JsonNode scopeModelsNode = configJson.at("/scopeModels");
		final int expectedCount = scopeModelsNode.size();

		final long actualCount = dsl.selectCount()
			.from(SCOPE_MODEL)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d scope models from JSON, but found %d in scope_model table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of dataset models")
	public void datasetModelsImportedCorrectly() {
		final JsonNode datasetModelsNode = configJson.at("/datasetModels");
		final int expectedCount = datasetModelsNode.size();

		final long actualCount = dsl.selectCount()
			.from(DATASET_MODEL)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d dataset models from JSON, but found %d in dataset_model table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of form models")
	public void formModelsImportedCorrectly() {
		final JsonNode formModelsNode = configJson.at("/formModels");
		final int expectedCount = formModelsNode.size();

		final long actualCount = dsl.selectCount()
			.from(FORM_MODEL)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d form models from JSON, but found %d in form_model table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of form model layouts")
	public void formModelLayoutsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode formModelsNode = configJson.at("/formModels");
		for(JsonNode formModel : formModelsNode) {
			final JsonNode layouts = formModel.get("layouts");
			if(layouts != null && layouts.isArray()) {
				expectedCount += layouts.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(FORM_LAYOUT)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d form model layouts from JSON, but found %d in form_layout table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of form model layout columns")
	public void formModelLayoutColumnsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode formModelsNode = configJson.at("/formModels");
		for(JsonNode formModel : formModelsNode) {
			final JsonNode layouts = formModel.get("layouts");
			if(layouts != null && layouts.isArray()) {
				for(JsonNode layout : layouts) {
					final JsonNode columns = layout.get("columns");
					if(columns != null && columns.isArray()) {
						expectedCount += columns.size();
					}
				}
			}
		}

		final long actualCount = dsl.selectCount()
			.from(FORM_LAYOUT_COLUMN)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d form layout columns from JSON, but found %d in form_layout_column table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of form model layout lines")
	public void formModelLayoutLinesImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode formModelsNode = configJson.at("/formModels");
		for(JsonNode formModel : formModelsNode) {
			final JsonNode layouts = formModel.get("layouts");
			if(layouts != null && layouts.isArray()) {
				for(JsonNode layout : layouts) {
					final JsonNode lines = layout.get("lines");
					if(lines != null && lines.isArray()) {
						expectedCount += lines.size();
					}
				}
			}
		}

		final long actualCount = dsl.selectCount()
			.from(FORM_LAYOUT_LINE)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d form layout lines from JSON, but found %d in form_layout_line table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of form model layout line cells")
	public void formModelLayoutLineCellsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode formModelsNode = configJson.at("/formModels");
		for(JsonNode formModel : formModelsNode) {
			final JsonNode layouts = formModel.get("layouts");
			if(layouts != null && layouts.isArray()) {
				for(JsonNode layout : layouts) {
					final JsonNode lines = layout.get("lines");
					if(lines != null && lines.isArray()) {
						for(JsonNode line : lines) {
							final JsonNode cells = line.get("cells");
							if(cells != null && cells.isArray()) {
								expectedCount += cells.size();
							}
						}
					}
				}
			}
		}

		final long actualCount = dsl.selectCount()
			.from(FORM_LAYOUT_CELL)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d form layout cells from JSON, but found %d in form_layout_cell table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of form model layout line cell visibility criteria")
	public void formModelLayoutLineCellVisibilityCriteriaImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode formModelsNode = configJson.at("/formModels");
		for(JsonNode formModel : formModelsNode) {
			final JsonNode layouts = formModel.get("layouts");
			if(layouts != null && layouts.isArray()) {
				for(JsonNode layout : layouts) {
					final JsonNode lines = layout.get("lines");
					if(lines != null && lines.isArray()) {
						for(JsonNode line : lines) {
							final JsonNode cells = line.get("cells");
							if(cells != null && cells.isArray()) {
								for(JsonNode cell : cells) {
									final JsonNode criteria = cell.get("visibilityCriteria");
									if(criteria != null && criteria.isArray()) {
										expectedCount += criteria.size();
									}
								}
							}
						}
					}
				}
			}
		}

		final long actualCount = dsl.selectCount()
			.from(FORM_CELL_VISIBILITY_CRITERIA)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d form layout cell visibility criteria from JSON, but found %d in form_cell_visibility_criteria table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of field models")
	public void fieldModelsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode datasetModelsNode = configJson.at("/datasetModels");
		for(JsonNode datasetModel : datasetModelsNode) {
			final JsonNode fieldModels = datasetModel.get("fieldModels");
			if(fieldModels != null && fieldModels.isArray()) {
				expectedCount += fieldModels.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(FIELD_MODEL)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d field models from JSON, but found %d in field_model table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of field model possible values")
	public void fieldModelPossibleValuesImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode datasetModelsNode = configJson.at("/datasetModels");
		for(JsonNode datasetModel : datasetModelsNode) {
			final JsonNode fieldModels = datasetModel.get("fieldModels");
			if(fieldModels != null && fieldModels.isArray()) {
				for(JsonNode fieldModel : fieldModels) {
					final JsonNode possibleValues = fieldModel.get("possibleValues");
					if(possibleValues != null && possibleValues.isArray()) {
						expectedCount += possibleValues.size();
					}
				}
			}
		}

		final long actualCount = dsl.selectCount()
			.from(FIELD_POSSIBLE_VALUE)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d field model possible values from JSON, but found %d in field_possible_value table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of workflows")
	public void workflowsImportedCorrectly() {
		final JsonNode workflowsNode = configJson.at("/workflows");
		final int expectedCount = workflowsNode.size();

		final long actualCount = dsl.selectCount()
			.from(WORKFLOW)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d workflows from JSON, but found %d in workflow table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of workflow states")
	public void workflowStatesImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode workflowsNode = configJson.at("/workflows");
		for(JsonNode workflow : workflowsNode) {
			final JsonNode states = workflow.get("states");
			if(states != null && states.isArray()) {
				expectedCount += states.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(WORKFLOW_STATE)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d workflow states from JSON, but found %d in workflow_state table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of workflow actions")
	public void workflowActionsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode workflowsNode = configJson.at("/workflows");
		for(JsonNode workflow : workflowsNode) {
			final JsonNode actions = workflow.get("actions");
			if(actions != null && actions.isArray()) {
				expectedCount += actions.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(WORKFLOW_ACTION)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d workflow actions from JSON, but found %d in workflow_action table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of event models")
	public void eventModelsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode scopeModelsNode = configJson.at("/scopeModels");
		for(JsonNode scopeModel : scopeModelsNode) {
			final JsonNode eventModels = scopeModel.get("eventModels");
			if(eventModels != null && eventModels.isArray()) {
				expectedCount += eventModels.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(EVENT_MODEL)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d event models from JSON, but found %d in event_model table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of event groups")
	public void eventGroupsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode scopeModelsNode = configJson.at("/scopeModels");
		for(JsonNode scopeModel : scopeModelsNode) {
			final JsonNode eventGroups = scopeModel.get("eventGroups");
			if(eventGroups != null && eventGroups.isArray()) {
				expectedCount += eventGroups.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(EVENT_GROUP)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d event groups from JSON, but found %d in event_group table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of profiles")
	public void profilesImportedCorrectly() {
		final JsonNode profilesNode = configJson.at("/profiles");
		final int expectedCount = profilesNode.size();

		final long actualCount = dsl.selectCount()
			.from(PROFILE)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d profiles from JSON, but found %d in profile table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of granted profile rights")
	public void grantedProfileRightsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode profilesNode = configJson.at("/profiles");
		for(JsonNode profile : profilesNode) {
			final JsonNode grantedProfileIdRights = profile.get("grantedProfileIdRights");
			if(grantedProfileIdRights != null) {
				expectedCount += grantedProfileIdRights.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(PROFILE_PROFILE_RIGHTS)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d granted profile rights from JSON, but found %d in profile_profile_rights table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of granted dataset model rights")
	public void grantedDatasetModelRightsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode profilesNode = configJson.at("/profiles");
		for(JsonNode profile : profilesNode) {
			final JsonNode grantedDatasetModelIdRights = profile.get("grantedDatasetModelIdRights");
			if(grantedDatasetModelIdRights != null) {
				expectedCount += grantedDatasetModelIdRights.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(PROFILE_DATASET_MODEL_RIGHTS)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d granted dataset model rights from JSON, but found %d in profile_dataset_model_rights table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of granted scope model rights")
	public void grantedScopeModelRightsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode profilesNode = configJson.at("/profiles");
		for(JsonNode profile : profilesNode) {
			final JsonNode grantedScopeModelIdRights = profile.get("grantedScopeModelIdRights");
			if(grantedScopeModelIdRights != null) {
				expectedCount += grantedScopeModelIdRights.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(PROFILE_SCOPE_MODEL_RIGHTS)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d granted scope model rights from JSON, but found %d in profile_scope_model_rights table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of granted payment plan rights")
	public void grantedPaymentPlanRightsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode profilesNode = configJson.at("/profiles");
		for(JsonNode profile : profilesNode) {
			final JsonNode grantedPaymentIdRights = profile.get("grantedPaymentIdRights");
			if(grantedPaymentIdRights != null) {
				expectedCount += grantedPaymentIdRights.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(PROFILE_PAYMENT_MODEL_RIGHTS)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d granted payment plan rights from JSON, but found %d in profile_payment_model_rights table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of granted event model rights")
	public void grantedEventModelRightsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode profilesNode = configJson.at("/profiles");
		for(JsonNode profile : profilesNode) {
			final JsonNode grantedEventModelIdRights = profile.get("grantedEventModelIdRights");
			if(grantedEventModelIdRights != null) {
				expectedCount += grantedEventModelIdRights.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(PROFILE_EVENT_MODEL_RIGHTS)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d granted event model rights from JSON, but found %d in profile_event_model_rights table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of granted form model rights")
	public void grantedFormModelRightsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode profilesNode = configJson.at("/profiles");
		for(JsonNode profile : profilesNode) {
			final JsonNode grantedFormModelIdRights = profile.get("grantedFormModelIdRights");
			if(grantedFormModelIdRights != null) {
				expectedCount += grantedFormModelIdRights.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(PROFILE_FORM_MODEL_RIGHTS)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d granted form model rights from JSON, but found %d in profile_form_model_rights table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of granted workflow rights")
	public void grantedWorkflowRightsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode profilesNode = configJson.at("/profiles");
		for(JsonNode profile : profilesNode) {
			final JsonNode grantedWorkflowIds = profile.get("grantedWorkflowIds");
			if(grantedWorkflowIds != null) {
				expectedCount += grantedWorkflowIds.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(PROFILE_WORKFLOW_RIGHTS)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d granted workflow rights from JSON, but found %d in profile_workflow_rights table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of validators")
	public void validatorsImportedCorrectly() {
		final JsonNode validatorsNode = configJson.at("/validators");
		final int expectedCount = validatorsNode.size();

		final long actualCount = dsl.selectCount()
			.from(VALIDATOR)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d validators from JSON, but found %d in validator table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of menus")
	public void menusImportedCorrectly() {
		final JsonNode menusNode = configJson.at("/menus");
		final int expectedCount = countAllMenusRecursively(menusNode);

		final long actualCount = dsl.selectCount()
			.from(MENU)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d menus from JSON, but found %d in menu table",
				expectedCount, actualCount));
	}

	private int countAllMenusRecursively(JsonNode menusNode) {
		if(menusNode == null || !menusNode.isArray()) {
			return 0;
		}

		int count = 0;
		for(JsonNode menuNode : menusNode) {
			count++;

			JsonNode submenusNode = menuNode.get("submenus");
			if(submenusNode != null && submenusNode.isArray()) {
				count += countAllMenusRecursively(submenusNode);
			}
		}

		return count;
	}

	@Test
	@DisplayName("Batch import creates correct number of menu layout sections")
	public void menuLayoutSectionsImportedCorrectly() {
		final JsonNode menusNode = configJson.at("/menus");
		final int expectedCount = countAllSectionsRecursively(menusNode);

		final long actualCount = dsl.selectCount()
			.from(MENU_LAYOUT_SECTION)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d menu layout sections from JSON, but found %d in menu_layout_section table",
				expectedCount, actualCount));
	}

	private int countAllSectionsRecursively(JsonNode menusNode) {
		if(menusNode == null || !menusNode.isArray()) {
			return 0;
		}

		int count = 0;
		for(JsonNode menuNode : menusNode) {
			final JsonNode layoutNode = menuNode.get("layout");
			if(layoutNode != null && !layoutNode.isNull()) {
				final JsonNode sectionsNode = layoutNode.get("sections");
				if(sectionsNode != null && sectionsNode.isArray()) {
					count += sectionsNode.size();
				}
			}

			JsonNode submenusNode = menuNode.get("submenus");
			if(submenusNode != null && submenusNode.isArray()) {
				count += countAllSectionsRecursively(submenusNode);
			}
		}

		return count;
	}

	@Test
	@DisplayName("Batch import creates correct number of menu layout section widgets")
	public void menuLayoutSectionWidgetsImportedCorrectly() {
		final JsonNode menusNode = configJson.at("/menus");
		final int expectedCount = countAllWidgetsRecursively(menusNode);

		final long actualCount = dsl.selectCount()
			.from(MENU_LAYOUT_SECTION_WIDGET)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d menu layout section widgets from JSON, but found %d in menu_layout_section_widget table",
				expectedCount, actualCount));
	}

	private int countAllWidgetsRecursively(JsonNode menusNode) {
		if(menusNode == null || !menusNode.isArray()) {
			return 0;
		}

		int count = 0;
		for(JsonNode menuNode : menusNode) {
			final JsonNode layoutNode = menuNode.get("layout");
			if(layoutNode != null && !layoutNode.isNull()) {
				final JsonNode sectionsNode = layoutNode.get("sections");
				if(sectionsNode != null && sectionsNode.isArray()) {
					for(JsonNode sectionNode : sectionsNode) {
						final JsonNode widgetsNode = sectionNode.get("widgets");
						if(widgetsNode != null && widgetsNode.isArray()) {
							count += widgetsNode.size();
						}
					}
				}
			}

			JsonNode submenusNode = menuNode.get("submenus");
			if(submenusNode != null && submenusNode.isArray()) {
				count += countAllWidgetsRecursively(submenusNode);
			}
		}

		return count;
	}

	@Test
	@DisplayName("Batch import creates correct number of features")
	public void featuresImportedCorrectly() {
		final int staticFeatureCount = FeatureStatic.values().length;
		final JsonNode featuresNode = configJson.at("/features");
		final int expectedCount = featuresNode.size() + staticFeatureCount;

		final long actualCount = dsl.selectCount()
			.from(FEATURE)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d features from JSON, but found %d in feature table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of payment plans")
	public void paymentPlansImportedCorrectly() {
		final JsonNode paymentPlansNode = configJson.at("/paymentPlans");
		final int expectedCount = paymentPlansNode.size();

		final long actualCount = dsl.selectCount()
			.from(PAYMENT_PLAN)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d payment plans from JSON, but found %d in payment_plan table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of payment steps")
	public void paymentStepsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode paymentPlansNode = configJson.at("/paymentPlans");
		for(JsonNode paymentPlan : paymentPlansNode) {
			final JsonNode paymentSteps = paymentPlan.get("steps");
			if(paymentSteps != null && paymentSteps.isArray()) {
				expectedCount += paymentSteps.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(PAYMENT_STEP)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d payment steps from JSON, but found %d in payment_step table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of privacy policies")
	public void privacyPoliciesImportedCorrectly() {
		final JsonNode privacyPoliciesNode = configJson.at("/privacyPolicies");
		final int expectedCount = privacyPoliciesNode.size();

		final long actualCount = dsl.selectCount()
			.from(PRIVACY_POLICY)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d privacy policies from JSON, but found %d in privacy_policy table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of resource categories")
	public void resourceCategoriesImportedCorrectly() {
		final JsonNode resourceCategoriesNode = configJson.at("/resourceCategories");
		final int expectedCount = resourceCategoriesNode.size();

		final long actualCount = dsl.selectCount()
			.from(RESOURCE_CATEGORY)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d resource categories from JSON, but found %d in resource_category table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of reports")
	public void reportsImportedCorrectly() {
		final JsonNode reportsNode = configJson.at("/reports");
		final int expectedCount = reportsNode.size();

		final long actualCount = dsl.selectCount()
			.from(REPORT)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d reports from JSON, but found %d in report table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of charts")
	public void chartsImportedCorrectly() {
		final JsonNode chartsNode = configJson.at("/charts");
		final int expectedCount = chartsNode.size();

		final long actualCount = dsl.selectCount()
			.from(CHART)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d charts from JSON, but found %d in chart table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of chart ranges")
	public void chartRangesImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode chartsNode = configJson.at("/charts");
		for(JsonNode chart : chartsNode) {
			final JsonNode chartRanges = chart.get("ranges");
			if(chartRanges != null && chartRanges.isArray()) {
				expectedCount += chartRanges.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(CHART_RANGE)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d chart ranges from JSON, but found %d in chart_range table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of timeline graphs")
	public void timelineGraphsImportedCorrectly() {
		final JsonNode timelineGraphsNode = configJson.at("/timelineGraphs");
		final int expectedCount = timelineGraphsNode.size();

		final long actualCount = dsl.selectCount()
			.from(TIMELINE_GRAPH)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d timeline graphs from JSON, but found %d in timeline_graph table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of timeline graph sections")
	public void timelineGraphSectionsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode timelineGraphsNode = configJson.at("/timelineGraphs");
		for(JsonNode timelineGraph : timelineGraphsNode) {
			final JsonNode sections = timelineGraph.get("sections");
			if(sections != null && sections.isArray()) {
				expectedCount += sections.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(TIMELINE_GRAPH_SECTION)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d timeline graph sections from JSON, but found %d in timeline_graph_section table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of timeline graph section references")
	public void timelineGraphSectionReferencesImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode timelineGraphsNode = configJson.at("/timelineGraphs");
		for(JsonNode timelineGraph : timelineGraphsNode) {
			final JsonNode sections = timelineGraph.get("sections");
			if(sections != null && sections.isArray()) {
				for(JsonNode section : sections) {
					final JsonNode references = section.get("references");
					if(references != null && references.isArray()) {
						expectedCount += references.size();
					}
				}
			}
		}

		final long actualCount = dsl.selectCount()
			.from(TIMELINE_GRAPH_SECTION_REFERENCE)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d timeline graph section references from JSON, but found %d in timeline_graph_section_reference table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of timeline graph section reference entries")
	public void timelineGraphSectionReferenceEntriesImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode timelineGraphsNode = configJson.at("/timelineGraphs");
		for(JsonNode timelineGraph : timelineGraphsNode) {
			final JsonNode sections = timelineGraph.get("sections");
			if(sections != null && sections.isArray()) {
				for(JsonNode section : sections) {
					final JsonNode references = section.get("references");
					if(references != null && references.isArray()) {
						for(JsonNode reference : references) {
							final JsonNode entries = reference.get("entries");
							if(entries != null && entries.isArray()) {
								expectedCount += entries.size();
							}
						}
					}
				}
			}
		}

		final long actualCount = dsl.selectCount()
			.from(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d timeline graph section reference entries from JSON, but found %d in timeline_graph_section_reference_entry table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of workflow widgets")
	public void workflowWidgetsImportedCorrectly() {
		final JsonNode workflowWidgetsNode = configJson.at("/workflowWidgets");
		final int expectedCount = workflowWidgetsNode.size();

		final long actualCount = dsl.selectCount()
			.from(WORKFLOW_WIDGET)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d workflow widgets from JSON, but found %d in workflow_widget table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of workflow widget columns")
	public void workflowWidgetColumnsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode workflowWidgetsNode = configJson.at("/workflowWidgets");
		for(JsonNode workflowWidget : workflowWidgetsNode) {
			final JsonNode columns = workflowWidget.get("columns");
			if(columns != null && columns.isArray()) {
				expectedCount += columns.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(WORKFLOW_WIDGET_COLUMN)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d workflow widget columns from JSON, but found %d in workflow_widget_column table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of workflow widget state selectors")
	public void workflowWidgetStateSelectorsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode workflowWidgetsNode = configJson.at("/workflowWidgets");
		for(JsonNode workflowWidget : workflowWidgetsNode) {
			final JsonNode statesSelectors = workflowWidget.get("workflowStatesSelectors");
			if(statesSelectors != null && statesSelectors.isArray()) {
				expectedCount += statesSelectors.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(WORKFLOW_WIDGET_STATE_SELECTOR)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d workflow widget selectors from JSON, but found %d in workflow_widget_state_selector table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of workflow summaries")
	public void workflowSummariesImportedCorrectly() {
		final JsonNode workflowSummariesNode = configJson.at("/workflowSummaries");
		final int expectedCount = workflowSummariesNode.size();

		final long actualCount = dsl.selectCount()
			.from(WORKFLOW_SUMMARY)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d workflow summaries from JSON, but found %d in workflow_summary table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of workflow summary columns")
	public void workflowSummaryColumnsImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode workflowSummariesNode = configJson.at("/workflowSummaries");
		for(JsonNode workflowWidget : workflowSummariesNode) {
			final JsonNode columns = workflowWidget.get("columns");
			if(columns != null && columns.isArray()) {
				expectedCount += columns.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(WORKFLOW_SUMMARY_COLUMN)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d workflow summary columns from JSON, but found %d in workflow_summary_column table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of crons")
	public void cronsImportedCorrectly() {
		final JsonNode cronsNode = configJson.at("/crons");
		final int expectedCount = cronsNode.size();

		final long actualCount = dsl.selectCount()
			.from(CRON)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d crons from JSON, but found %d in cron table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of selections")
	public void selectionsImportedCorrectly() {
		final JsonNode selectionsNode = configJson.at("/selections");
		final int expectedCount = countAllSelectionsRecursively(selectionsNode);

		final long actualCount = dsl.selectCount()
			.from(SELECTION_NODE)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d selections from JSON, but found %d in selection_node table",
				expectedCount, actualCount));
	}

	private int countAllSelectionsRecursively(JsonNode selectionsNode) {
		if(selectionsNode == null || !selectionsNode.isArray()) {
			return 0;
		}

		int count = 0;
		for(JsonNode selectionNode : selectionsNode) {
			count++;

			JsonNode subSelectionsNode = selectionNode.get("selections");
			if(subSelectionsNode != null && subSelectionsNode.isArray()) {
				count += countAllSelectionsRecursively(subSelectionsNode);
			}
		}

		return count;
	}

	@Test
	@DisplayName("Batch import creates correct number of rule definition properties")
	public void ruleDefinitionPropertiesImportedCorrectly() {
		final JsonNode ruleDefinitionPropertiesNode = configJson.at("/ruleDefinitionProperties");
		final int expectedCount = ruleDefinitionPropertiesNode.size();

		final long actualCount = dsl.selectCount()
			.from(RULE_DEFINITION_PROPERTY)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d rule definition properties from JSON, but found %d in rule_definition_property table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of rule definition actions")
	public void ruleDefinitionActionsImportedCorrectly() {
		final JsonNode ruleDefinitionActionsNode = configJson.at("/ruleDefinitionActions");
		final int expectedCount = ruleDefinitionActionsNode.size();

		final long actualCount = dsl.selectCount()
			.from(RULE_DEFINITION_ACTION)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d rule definition actions from JSON, but found %d in rule_definition_action table",
				expectedCount, actualCount));
	}

	@Test
	@DisplayName("Batch import creates correct number of rule definition action parameters")
	public void ruleDefinitionActionParametersImportedCorrectly() {
		int expectedCount = 0;
		final JsonNode ruleDefinitionActionsNode = configJson.at("/ruleDefinitionActions");
		for(JsonNode action : ruleDefinitionActionsNode) {
			final JsonNode parameters = action.get("parameters");
			if(parameters != null && parameters.isArray()) {
				expectedCount += parameters.size();
			}
		}

		final long actualCount = dsl.selectCount()
			.from(RULE_DEFINITION_ACTION_PARAMETER)
			.fetchOne(0, Long.class);

		assertEquals(expectedCount, actualCount,
			String.format("Expected %d rule definition action parameters from JSON, but found %d in rule_definition_action_parameter table",
				expectedCount, actualCount));
	}
}
