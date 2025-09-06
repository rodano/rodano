package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.FormCellVisibilityCriteria;
import ch.rodano.batch.pojo.FormModel;
import ch.rodano.batch.pojo.FormModelLayout;
import ch.rodano.batch.pojo.FormModelLayoutCell;
import ch.rodano.batch.pojo.FormModelLayoutColumn;
import ch.rodano.batch.pojo.FormModelLayoutLine;
import ch.rodano.batch.pojo.Rule;
import ch.rodano.core.model.jooq.enums.RuleEntityType;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveDatasetModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveFieldModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveFormModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolvePossibleValueIdForCell;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowId;
import static ch.rodano.batch.helper.RuleHelper.insertConstraintForOwner;
import static ch.rodano.batch.helper.RuleHelper.insertRuleActions;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
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
import static ch.rodano.core.model.jooq.tables.Rule.RULE;

public class FormModelWriter extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(FormModelWriter.class);

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<FormModel> wrapped = (ProjectScoped<FormModel>) raw;
				final UUID projectId = wrapped.getProjectId();
				final FormModel formModel = wrapped.getPayload();

				final var existing = resolveFormModelId(tx, projectId, formModel.getId());
				final UUID formModelId = existing != null
					? existing
					: deterministic(projectId, "FORM_MODEL", formModel.getId());

				tx.insertInto(FORM_MODEL)
					.set(FORM_MODEL.PROJECT_ID, projectId)
					.set(FORM_MODEL.FORM_MODEL_ID, formModelId)
					.set(FORM_MODEL.CODE, formModel.getId())
					.set(FORM_MODEL.OPTIONAL, formModel.isOptional())
					.set(FORM_MODEL.SHORTNAME, toJson(formModel.getShortname()))
					.set(FORM_MODEL.LONGNAME, toJson(formModel.getLongname()))
					.set(FORM_MODEL.DESCRIPTION, toJson(formModel.getDescription()))
					.set(FORM_MODEL.PRINT_BUTTON_LABEL, toJson(formModel.getPrintButtonLabel()))
					.onDuplicateKeyUpdate()
					.set(FORM_MODEL.OPTIONAL, formModel.isOptional())
					.set(FORM_MODEL.SHORTNAME, toJson(formModel.getShortname()))
					.set(FORM_MODEL.LONGNAME, toJson(formModel.getLongname()))
					.set(FORM_MODEL.DESCRIPTION, toJson(formModel.getDescription()))
					.set(FORM_MODEL.PRINT_BUTTON_LABEL, toJson(formModel.getPrintButtonLabel()))
					.execute();

				if(formModel.getWorkflowIds() != null && !formModel.getWorkflowIds().isEmpty()) {
					for(String workflowCode : formModel.getWorkflowIds()) {

						final var existingWorkflowId = resolveWorkflowId(tx, projectId, workflowCode);
						final UUID workflowId = existingWorkflowId != null
							? existingWorkflowId
							: deterministic(projectId, "WORKFLOW", workflowCode);

						tx.insertInto(FORM_MODEL_WORKFLOW)
							.set(FORM_MODEL_WORKFLOW.PROJECT_ID, projectId)
							.set(FORM_MODEL_WORKFLOW.FORM_MODEL_ID, formModelId)
							.set(FORM_MODEL_WORKFLOW.WORKFLOW_ID, workflowId)
							.onDuplicateKeyIgnore()
							.execute();
					}
				}

				putFormRules(tx, projectId, formModel.getId(), formModelId, formModel.getRules());

				final List<PendingTarget> pendingTargets = new java.util.ArrayList<>();

				if(formModel.getLayouts() != null && !formModel.getLayouts().isEmpty()) {
					for(FormModelLayout layout : formModel.getLayouts()) {
						final String layoutCode = layout.getId();
						final UUID layoutId = deterministic(projectId, "FORM_LAYOUT", formModel.getId() + "|" + layoutCode);

						final UUID resolvedDatasetModelId = resolveDatasetModelId(tx, projectId, layout.getDatasetModelId());
						final UUID resolvedFieldModelId = resolveFieldModelId(tx, projectId, resolvedDatasetModelId, layout.getDefaultSortFieldModelId());

						tx.insertInto(FORM_LAYOUT)
							.set(FORM_LAYOUT.PROJECT_ID, projectId)
							.set(FORM_LAYOUT.FORM_MODEL_ID, formModelId)
							.set(FORM_LAYOUT.FORM_LAYOUT_ID, layoutId)
							.set(FORM_LAYOUT.CODE, layoutCode)
							.set(FORM_LAYOUT.TYPE, layout.getType())
							.set(FORM_LAYOUT.CSS_CODE, layout.getCssCode())
							.set(FORM_LAYOUT.DATASET_MODEL_ID, resolvedDatasetModelId)
							.set(FORM_LAYOUT.DEFAULT_SORT_FIELD_MODEL_ID, resolvedFieldModelId)
							.set(FORM_LAYOUT.DESCRIPTION, toJson(layout.getDescription()))
							.set(FORM_LAYOUT.TEXT_BEFORE, toJson(layout.getTextBefore()))
							.set(FORM_LAYOUT.TEXT_AFTER, toJson(layout.getTextAfter()))
							.onDuplicateKeyUpdate()
							.set(FORM_LAYOUT.TYPE, layout.getType())
							.set(FORM_LAYOUT.CSS_CODE, layout.getCssCode())
							.set(FORM_LAYOUT.DATASET_MODEL_ID, resolvedDatasetModelId)
							.set(FORM_LAYOUT.DEFAULT_SORT_FIELD_MODEL_ID, resolvedFieldModelId)
							.set(FORM_LAYOUT.DESCRIPTION, toJson(layout.getDescription()))
							.set(FORM_LAYOUT.TEXT_BEFORE, toJson(layout.getTextBefore()))
							.set(FORM_LAYOUT.TEXT_AFTER, toJson(layout.getTextAfter()))
							.execute();

						if(layout.getConstraint() != null) {
							insertConstraintForOwner(tx, projectId, "FORM_LAYOUT", layoutId, layout.getConstraint());
						}

						if(layout.getColumns() != null && !layout.getColumns().isEmpty()) {
							int colOrder = 0;
							for(FormModelLayoutColumn column : layout.getColumns()) {
								tx.insertInto(FORM_LAYOUT_COLUMN)
									.set(FORM_LAYOUT_COLUMN.PROJECT_ID, projectId)
									.set(FORM_LAYOUT_COLUMN.FORM_MODEL_ID, formModelId)
									.set(FORM_LAYOUT_COLUMN.FORM_LAYOUT_ID, layoutId)
									.set(FORM_LAYOUT_COLUMN.COL_ORDER, colOrder++)
									.set(FORM_LAYOUT_COLUMN.CSS_CODE, column.getCssCode())
									.onDuplicateKeyUpdate()
									.set(FORM_LAYOUT_COLUMN.CSS_CODE, column.getCssCode())
									.execute();
							}
						}

						int lineOrder = 0;
						if(layout.getLines() != null && !layout.getLines().isEmpty()) {
							for(FormModelLayoutLine line : layout.getLines()) {

								final UUID lineId = deterministic(projectId, "FORM_LAYOUT_LINE",
									formModel.getId() + "|" + layoutCode + "|" + lineOrder);

								tx.insertInto(FORM_LAYOUT_LINE)
									.set(FORM_LAYOUT_LINE.FORM_LAYOUT_LINE_ID, lineId)
									.set(FORM_LAYOUT_LINE.PROJECT_ID, projectId)
									.set(FORM_LAYOUT_LINE.FORM_MODEL_ID, formModelId)
									.set(FORM_LAYOUT_LINE.FORM_LAYOUT_ID, layoutId)
									.set(FORM_LAYOUT_LINE.LINE_ORDER, lineOrder)
									.onDuplicateKeyIgnore()
									.execute();

								if(line.getCells() != null && !line.getCells().isEmpty()) {
									for(FormModelLayoutCell cell : line.getCells()) {
										final String cellCode = cell.getId();
										final UUID cellId = deterministic(projectId, "FORM_LAYOUT_CELL",
											formModel.getId() + "|" + layoutCode + "|" +
												(cellCode != null ? cellCode : ("line" + lineOrder)));

										final UUID resolvedDatasetModelIdCell = resolveDatasetModelId(tx, projectId, cell.getDatasetModelId());
										final UUID resolvedFieldModelIdCell = resolveFieldModelId(tx, projectId, resolvedDatasetModelIdCell, cell.getFieldModelId());

										tx.insertInto(FORM_LAYOUT_CELL)
											.set(FORM_LAYOUT_CELL.PROJECT_ID, projectId)
											.set(FORM_LAYOUT_CELL.FORM_MODEL_ID, formModelId)
											.set(FORM_LAYOUT_CELL.FORM_LAYOUT_ID, layoutId)
											.set(FORM_LAYOUT_CELL.FORM_LAYOUT_CELL_ID, cellId)
											.set(FORM_LAYOUT_CELL.FORM_LAYOUT_LINE_ID, lineId)
											.set(FORM_LAYOUT_CELL.LINE_ORDER, lineOrder)
											.set(FORM_LAYOUT_CELL.CODE, cellCode)
											.set(FORM_LAYOUT_CELL.DATASET_MODEL_ID, resolvedDatasetModelIdCell)
											.set(FORM_LAYOUT_CELL.FIELD_MODEL_ID, resolvedFieldModelIdCell)
											.set(FORM_LAYOUT_CELL.CSS_CODE_FOR_LABEL, cell.getCssCodeForLabel())
											.set(FORM_LAYOUT_CELL.CSS_CODE_FOR_INPUT, cell.getCssCodeForInput())
											.set(FORM_LAYOUT_CELL.DISPLAY_LABEL, cell.isDisplayLabel())
											.set(FORM_LAYOUT_CELL.DISPLAY_POSSIBLE_VALUE_LABELS, cell.isDisplayPossibleValueLabels())
											.set(FORM_LAYOUT_CELL.POSSIBLE_VALUES_COLUMN_NUMBER, cell.getPossibleValuesColumnNumber())
											.set(FORM_LAYOUT_CELL.POSSIBLE_VALUES_COLUMN_WIDTH, cell.getPossibleValuesColumnWidth())
											.set(FORM_LAYOUT_CELL.COLSPAN, cell.getColspan() == null ? 1 : cell.getColspan())
											.set(FORM_LAYOUT_CELL.TEXT_BEFORE, toJson(cell.getTextBefore()))
											.set(FORM_LAYOUT_CELL.TEXT_AFTER, toJson(cell.getTextAfter()))
											.onDuplicateKeyUpdate()
											.set(FORM_LAYOUT_CELL.DATASET_MODEL_ID, resolvedDatasetModelIdCell)
											.set(FORM_LAYOUT_CELL.FIELD_MODEL_ID, resolvedFieldModelIdCell)
											.set(FORM_LAYOUT_CELL.CSS_CODE_FOR_LABEL, cell.getCssCodeForLabel())
											.set(FORM_LAYOUT_CELL.CSS_CODE_FOR_INPUT, cell.getCssCodeForInput())
											.set(FORM_LAYOUT_CELL.DISPLAY_LABEL, cell.isDisplayLabel())
											.set(FORM_LAYOUT_CELL.DISPLAY_POSSIBLE_VALUE_LABELS, cell.isDisplayPossibleValueLabels())
											.set(FORM_LAYOUT_CELL.POSSIBLE_VALUES_COLUMN_NUMBER, cell.getPossibleValuesColumnNumber())
											.set(FORM_LAYOUT_CELL.POSSIBLE_VALUES_COLUMN_WIDTH, cell.getPossibleValuesColumnWidth())
											.set(FORM_LAYOUT_CELL.COLSPAN, cell.getColspan() == null ? 1 : cell.getColspan())
											.set(FORM_LAYOUT_CELL.TEXT_BEFORE, toJson(cell.getTextBefore()))
											.set(FORM_LAYOUT_CELL.TEXT_AFTER, toJson(cell.getTextAfter()))
											.execute();

										if(cell.getConstraint() != null) {
											insertConstraintForOwner(tx, projectId, "FORM_LAYOUT_CELL", cellId, cell.getConstraint());
										}

										final List<FormCellVisibilityCriteria> visCriteria = cell.getVisibilityCriteria();
										if(visCriteria != null && !visCriteria.isEmpty()) {
											int visCriteriaIndex = 0;
											for(FormCellVisibilityCriteria vis : visCriteria) {

												final UUID visCriteriaId = deterministic(projectId, "FORM_CELL_VISIBILITY_CRITERIA",
													formModel.getId() + "|" + layoutCode + "|" + cellCode + "|" + visCriteriaIndex);

												tx.insertInto(FORM_CELL_VISIBILITY_CRITERIA)
													.set(FORM_CELL_VISIBILITY_CRITERIA.FORM_CELL_VISIBLE_CRITERIA_ID, visCriteriaId)
													.set(FORM_CELL_VISIBILITY_CRITERIA.PROJECT_ID, projectId)
													.set(FORM_CELL_VISIBILITY_CRITERIA.FORM_LAYOUT_CELL_ID, cellId)
													.set(FORM_CELL_VISIBILITY_CRITERIA.LINE_ORDER, lineOrder)
													.set(FORM_CELL_VISIBILITY_CRITERIA.OPERATOR, vis.getOperator())
													.set(FORM_CELL_VISIBILITY_CRITERIA.ACTION, vis.getAction())
													.onDuplicateKeyUpdate()
													.set(FORM_CELL_VISIBILITY_CRITERIA.OPERATOR, vis.getOperator())
													.set(FORM_CELL_VISIBILITY_CRITERIA.ACTION, vis.getAction())
													.execute();

												visCriteriaIndex++;

												if(vis.getValues() != null && !vis.getValues().isEmpty()) {
													for(String value : vis.getValues()) {

														final UUID possibleValueId = resolvePossibleValueIdForCell(tx, projectId, cellId, value);

														if(possibleValueId == null) {
															LOGGER.warn("No possible value for project={}, cell={}, value={}", projectId, cellId, value);
															continue;
														}

														tx.insertInto(FORM_CELL_VISIBILITY_CRITERIA_VALUE)
															.set(FORM_CELL_VISIBILITY_CRITERIA_VALUE.PROJECT_ID, projectId)
															.set(FORM_CELL_VISIBILITY_CRITERIA_VALUE.FORM_CELL_VISIBLE_CRITERIA_ID, visCriteriaId)
															.set(FORM_CELL_VISIBILITY_CRITERIA_VALUE.FORM_LAYOUT_CELL_ID, cellId)
															.set(FORM_CELL_VISIBILITY_CRITERIA_VALUE.LINE_ORDER, lineOrder)
															.set(FORM_CELL_VISIBILITY_CRITERIA_VALUE.POSSIBLE_VALUE_ID, possibleValueId)
															.onDuplicateKeyIgnore()
															.execute();
													}
												}

												if(vis.getTargetCellIds() != null && !vis.getTargetCellIds().isEmpty()) {
													for(String targetCellCode : vis.getTargetCellIds()) {
														final UUID targetCellId = resolveCellId(tx, projectId, formModelId, layoutId, targetCellCode);

														if(targetCellId != null) {
															tx.insertInto(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL)
																.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.PROJECT_ID, projectId)
																.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.FORM_CELL_VISIBLE_CRITERIA_ID, visCriteriaId)
																.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.FORM_LAYOUT_CELL_ID, cellId)
																.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.LINE_ORDER, lineOrder)
																.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.TARGET_CELL_ID, targetCellId)
																.onDuplicateKeyIgnore()
																.execute();
														}
														else {
															pendingTargets.add(new PendingTarget(
																PendingTarget.Kind.CELL, projectId, formModelId, layoutId,
																targetCellCode, visCriteriaId, cellId, lineOrder
															));
														}
													}
												}

												if(vis.getTargetLayoutIds() != null && !vis.getTargetLayoutIds().isEmpty()) {
													for(String targetLayoutCode : vis.getTargetLayoutIds()) {
														final UUID targetLayoutId = resolveLayoutId(tx, projectId, formModelId, targetLayoutCode);

														if(targetLayoutId != null) {
															tx.insertInto(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT)
																.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.PROJECT_ID, projectId)
																.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.FORM_CELL_VISIBLE_CRITERIA_ID, visCriteriaId)
																.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.FORM_LAYOUT_CELL_ID, cellId)
																.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.LINE_ORDER, lineOrder)
																.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.TARGET_LAYOUT_ID, targetLayoutId)
																.onDuplicateKeyIgnore()
																.execute();
														}
														else {
															pendingTargets.add(new PendingTarget(
																PendingTarget.Kind.LAYOUT, projectId, formModelId, layoutId,
																targetLayoutCode, visCriteriaId, cellId, lineOrder
															));
														}
													}
												}
											}
										}
									}
								}
								lineOrder++;
							}
						}
					}
					for(PendingTarget p : pendingTargets) {
						switch(p.kind) {
							case CELL -> {
								final UUID targetCellId = resolveCellId(dsl, p.projectId, p.formModelId, p.layoutId, p.ref);
								if(targetCellId != null) {
									dsl.insertInto(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL)
										.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.PROJECT_ID, p.projectId)
										.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.FORM_CELL_VISIBLE_CRITERIA_ID, p.visCriteriaId)
										.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.FORM_LAYOUT_CELL_ID, p.sourceCellId)
										.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.LINE_ORDER, p.lineOrder)
										.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.TARGET_CELL_ID, targetCellId)
										.onDuplicateKeyIgnore()
										.execute();
								}
							}
							case LAYOUT -> {
								final UUID targetLayoutId = resolveLayoutId(dsl, p.projectId, p.formModelId, p.ref);
								if(targetLayoutId != null) {
									dsl.insertInto(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT)
										.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.PROJECT_ID, p.projectId)
										.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.FORM_CELL_VISIBLE_CRITERIA_ID, p.visCriteriaId)
										.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.FORM_LAYOUT_CELL_ID, p.sourceCellId)
										.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.LINE_ORDER, p.lineOrder)
										.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.TARGET_LAYOUT_ID, targetLayoutId)
										.onDuplicateKeyIgnore()
										.execute();
								}
							}
							default -> {
								System.out.println("DEFAULT");
							}
						}
					}
				}
			}
		});
	}

	private static void putFormRules(final DSLContext tx,
									 final UUID projectId,
									 final String formCode,
									 final UUID formModelId,
									 final List<Rule> rules) {

		if(rules == null || rules.isEmpty()) {
			return;
		}

		for(int idx = 0; idx < rules.size(); idx++) {
			final Rule rule = rules.get(idx);
			final UUID ruleId = deterministic(projectId, "FORM_MODEL_RULE", formCode + "|" + idx);

			tx.insertInto(RULE)
				.set(RULE.PROJECT_ID, projectId)
				.set(RULE.RULE_ID, ruleId)
				.set(RULE.ENTITY_TYPE, RuleEntityType.FORM_MODEL)
				.set(RULE.ENTITY_ID, formModelId)
				.set(RULE.RULE_TYPE, DSL.val((String) null))
				.set(RULE.DESCRIPTION, rule.getDescription())
				.set(RULE.MESSAGE, toJson(rule.getMessage()))
				.set(RULE.TAG, toJson(rule.getTags()))
				.onDuplicateKeyUpdate()
				.set(RULE.DESCRIPTION, rule.getDescription())
				.set(RULE.MESSAGE, toJson(rule.getMessage()))
				.set(RULE.TAG, toJson(rule.getTags()))
				.execute();

			if(rule.getConstraint() != null) {
				insertConstraintForOwner(tx, projectId, "RULE", ruleId, rule.getConstraint());
			}
			if(rule.getActions() != null && !rule.getActions().isEmpty()) {
				insertRuleActions(tx, projectId, ruleId, rule.getActions());
			}
		}
	}

	private static UUID resolveLayoutId(final DSLContext tx,
										final UUID projectId,
										final UUID defaultFormModelId,
										final String layoutRef) {

		if(layoutRef == null || layoutRef.isBlank()) {
			return null;
		}
		final String[] p = splitRef(layoutRef);
		UUID formId = defaultFormModelId;
		String layoutCode = null;

		if(p.length == 1) {
			layoutCode = p[0];
		}
		else if(p.length >= 2) {
			formId = resolveFormModelId(tx, projectId, p[0]);
			layoutCode = p[1];
		}

		if(formId == null || layoutCode == null || layoutCode.isBlank()) {
			return null;
		}

		return tx.select(FORM_LAYOUT.FORM_LAYOUT_ID)
			.from(FORM_LAYOUT)
			.where(FORM_LAYOUT.PROJECT_ID.eq(projectId)
				.and(FORM_LAYOUT.FORM_MODEL_ID.eq(formId))
				.and(FORM_LAYOUT.CODE.eq(layoutCode)))
			.fetchOne(FORM_LAYOUT.FORM_LAYOUT_ID);
	}

	private static UUID resolveCellId(final DSLContext tx,
									  final UUID projectId,
									  final UUID defaultFormModelId,
									  final UUID defaultLayoutId,
									  final String cellRef) {

		if(cellRef == null || cellRef.isBlank()) {
			return null;
		}
		final String[] p = splitRef(cellRef);
		UUID formId = defaultFormModelId;
		UUID layoutId = defaultLayoutId;
		final String cellCode;

		if(p.length == 1) {
			cellCode = p[0];
		}
		else if(p.length == 2) {
			layoutId = resolveLayoutId(tx, projectId, formId, p[0]);
			cellCode = p[1];
		}
		else {
			formId = resolveFormModelId(tx, projectId, p[0]);
			layoutId = resolveLayoutId(tx, projectId, formId, p[1]);
			cellCode = p[2];
		}

		if(formId == null || layoutId == null || cellCode == null || cellCode.isBlank()) {
			return null;
		}

		return tx.select(FORM_LAYOUT_CELL.FORM_LAYOUT_CELL_ID)
			.from(FORM_LAYOUT_CELL)
			.where(FORM_LAYOUT_CELL.PROJECT_ID.eq(projectId)
				.and(FORM_LAYOUT_CELL.FORM_MODEL_ID.eq(formId))
				.and(FORM_LAYOUT_CELL.FORM_LAYOUT_ID.eq(layoutId))
				.and(FORM_LAYOUT_CELL.CODE.eq(cellCode)))
			.fetchOne(FORM_LAYOUT_CELL.FORM_LAYOUT_CELL_ID);
	}

	private static String[] splitRef(final String ref) {
		final String r = ref == null ? "" : ref.trim();
		if(r.isEmpty()) {
			return new String[0];
		}
		return r.contains(":") ? r.split(":") : r.split("/");
	}

	private static final class PendingTarget {
		enum Kind {CELL, LAYOUT}

		final Kind kind;
		final UUID projectId;
		final UUID formModelId;
		final UUID layoutId;
		final String ref;
		final UUID visCriteriaId;
		final UUID sourceCellId;
		final int lineOrder;

		PendingTarget(final Kind kind, final UUID projectId, final UUID formModelId, final UUID layoutId,
					  final String ref, final UUID visCriteriaId, final UUID sourceCellId, final int lineOrder) {
			this.kind = kind;
			this.projectId = projectId;
			this.formModelId = formModelId;
			this.layoutId = layoutId;
			this.ref = ref;
			this.visCriteriaId = visCriteriaId;
			this.sourceCellId = sourceCellId;
			this.lineOrder = lineOrder;
		}
	}
}
