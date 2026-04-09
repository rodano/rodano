package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.FormModel;
import ch.rodano.batch.pojo.FormModelLayout;
import ch.rodano.batch.pojo.FormModelLayoutCell;
import ch.rodano.batch.pojo.FormModelLayoutLine;
import ch.rodano.batch.pojo.Rule;
import ch.rodano.core.model.jooq.enums.RuleConstraintConstraintType;
import ch.rodano.core.model.jooq.enums.RuleEntityType;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveFormModelId;
import static ch.rodano.batch.helper.RuleHelper.insertConstraintForOwner;
import static ch.rodano.batch.helper.RuleHelper.insertRuleActions;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.Rule.RULE;

public class FormModelRuleWriter extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(FormModelRuleWriter.class);

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<FormModel> wrapped = (ProjectScoped<FormModel>) raw;
				final UUID projectId = wrapped.getProjectId();
				final FormModel formModel = wrapped.getPayload();

				final UUID formModelId = resolveFormModelId(tx, projectId, formModel.getId());
				if(formModelId == null) {
					LOGGER.warn("Form model not found: {}", formModel.getId());
					continue;
				}

				if(formModel.getRules() != null && !formModel.getRules().isEmpty()) {
					for(int idx = 0; idx < formModel.getRules().size(); idx++) {
						final Rule rule = formModel.getRules().get(idx);
						final UUID ruleId = deterministic(projectId, "FORM_MODEL_RULE", formModel.getId() + "|" + idx);

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
							insertConstraintForOwner(tx, projectId, "RULE", ruleId,
								rule.getConstraint(), RuleConstraintConstraintType.RULE);
						}
						if(rule.getActions() != null && !rule.getActions().isEmpty()) {
							insertRuleActions(tx, projectId, ruleId, rule.getActions());
						}
					}
				}

				if(formModel.getLayouts() != null) {
					for(FormModelLayout layout : formModel.getLayouts()) {
						final UUID layoutId = deterministic(projectId, "FORM_LAYOUT",
							formModel.getId() + "|" + layout.getId());

						if(layout.getConstraint() != null) {
							insertConstraintForOwner(tx, projectId, "FORM_LAYOUT", layoutId,
								layout.getConstraint(), RuleConstraintConstraintType.DEFAULT);
						}

						if(layout.getLines() != null) {
							int lineOrder = 0;
							for(FormModelLayoutLine line : layout.getLines()) {
								if(line.getCells() != null) {
									for(FormModelLayoutCell cell : line.getCells()) {
										final String cellCode = cell.getId();
										final UUID cellId = deterministic(projectId, "FORM_LAYOUT_CELL",
											formModel.getId() + "|" + layout.getId() + "|" +
												(cellCode != null ? cellCode : ("line" + lineOrder)));

										if(cell.getConstraint() != null) {
											insertConstraintForOwner(tx, projectId, "FORM_LAYOUT_CELL", cellId,
												cell.getConstraint(), RuleConstraintConstraintType.DEFAULT);
										}
									}
								}
								lineOrder++;
							}
						}
					}
				}
			}
		});
	}
}
