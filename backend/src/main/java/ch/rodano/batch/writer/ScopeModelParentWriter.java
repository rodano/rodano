package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.ScopeModel;

import static ch.rodano.batch.helper.ModelResolvers.resolveScopeModelId;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelParent.SCOPE_MODEL_PARENT;

public class ScopeModelParentWriter extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScopeModelParentWriter.class);

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<ScopeModel> wrapped = (ProjectScoped<ScopeModel>) raw;

				final UUID projectId = wrapped.getProjectId();
				final ScopeModel scopeModel = wrapped.getPayload();

				if(scopeModel == null || scopeModel.getId() == null || scopeModel.getId().isBlank()) {
					LOGGER.warn("Skipping scope model with blank id");
					continue;
				}

				final String scopeModelCode = scopeModel.getId();
				final UUID scopeModelId = resolveScopeModelId(tx, projectId, scopeModelCode);

				if(scopeModelId == null) {
					LOGGER.warn("Scope model {} not found", scopeModelCode);
					continue;
				}

				final UUID defaultParentScopeModelId = scopeModel.getDefaultParentId() != null
					? resolveScopeModelId(tx, projectId, scopeModel.getDefaultParentId())
					: null;

				if(scopeModel.getDefaultParentId() != null && defaultParentScopeModelId == null) {
					LOGGER.warn("Default parent {} not found for scope model {}", scopeModel.getDefaultParentId(), scopeModelCode);
				}

				if(defaultParentScopeModelId != null) {
					tx.update(SCOPE_MODEL)
						.set(SCOPE_MODEL.DEFAULT_PARENT_ID, defaultParentScopeModelId)
						.where(SCOPE_MODEL.PROJECT_ID.eq(projectId)
							.and(SCOPE_MODEL.SCOPE_MODEL_ID.eq(scopeModelId)))
						.execute();
				}

				if(scopeModel.getParentIds() != null && !scopeModel.getParentIds().isEmpty()) {
					for(String parentCode : scopeModel.getParentIds()) {
						final UUID parentScopeModelId = resolveScopeModelId(tx, projectId, parentCode);

						if(parentScopeModelId == null) {
							LOGGER.warn("Parent scope model {} not found for scope model {}", parentCode, scopeModelCode);
							continue;
						}

						final boolean isDefault = parentScopeModelId.equals(defaultParentScopeModelId);

						tx.insertInto(SCOPE_MODEL_PARENT)
							.set(SCOPE_MODEL_PARENT.PROJECT_ID, projectId)
							.set(SCOPE_MODEL_PARENT.CHILD_SCOPE_MODEL_ID, scopeModelId)
							.set(SCOPE_MODEL_PARENT.PARENT_SCOPE_MODEL_ID, parentScopeModelId)
							.set(SCOPE_MODEL_PARENT.IS_DEFAULT, isDefault)
							.onDuplicateKeyUpdate()
							.set(SCOPE_MODEL_PARENT.IS_DEFAULT, isDefault)
							.execute();
					}
				}
			}
		});
	}
}
