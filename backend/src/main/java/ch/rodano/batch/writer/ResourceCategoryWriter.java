package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.ResourceCategory;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveResourceCategoryId;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.ResourceCategory.RESOURCE_CATEGORY;

public class ResourceCategoryWriter extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceCategoryWriter.class);

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<ResourceCategory> wrapped = (ProjectScoped<ResourceCategory>) raw;
				final UUID projectId = wrapped.getProjectId();
				final ResourceCategory category = wrapped.getPayload();

				final String categoryCode = category.getId();
				if(categoryCode == null || categoryCode.isBlank()) {
					LOGGER.warn("Skipping resource category with blank code");
					continue;
				}
				final var existing = resolveResourceCategoryId(tx, projectId, categoryCode);
				final UUID categoryId = existing != null
					? existing
					: deterministic(projectId, "RESOURCE_CATEGORY", categoryCode);

				tx.insertInto(RESOURCE_CATEGORY)
					.set(RESOURCE_CATEGORY.PROJECT_ID, projectId)
					.set(RESOURCE_CATEGORY.CATEGORY_ID, categoryId)
					.set(RESOURCE_CATEGORY.CODE, categoryCode)
					.set(RESOURCE_CATEGORY.ICON, category.getIcon())
					.set(RESOURCE_CATEGORY.SHORTNAME, toJson(category.getShortname()))
					.set(RESOURCE_CATEGORY.LONGNAME, toJson(category.getLongname()))
					.set(RESOURCE_CATEGORY.DESCRIPTION, toJson(category.getDescription()))
					.onDuplicateKeyUpdate()
					.set(RESOURCE_CATEGORY.ICON, category.getIcon())
					.set(RESOURCE_CATEGORY.SHORTNAME, toJson(category.getShortname()))
					.set(RESOURCE_CATEGORY.LONGNAME, toJson(category.getLongname()))
					.set(RESOURCE_CATEGORY.DESCRIPTION, toJson(category.getDescription()))
					.execute();
			}
		});
	}
}
