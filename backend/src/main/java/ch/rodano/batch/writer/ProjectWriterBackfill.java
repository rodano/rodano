package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Project;

import static ch.rodano.batch.helper.ModelResolvers.resolveProfileId;
import static ch.rodano.core.model.jooq.tables.Project.PROJECT;

public class ProjectWriterBackfill extends BaseWriter {

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<Project> wrapped = (ProjectScoped<Project>) raw;
				final UUID projectId = wrapped.getProjectId();
				final Project project = wrapped.getPayload();

				if(project.getEproProfileId() == null || project.getEproProfileId().isBlank()) {
					continue;
				}

				final UUID resolved = resolveProfileId(tx, projectId, project.getEproProfileId());
				if(resolved == null) {
					continue;
				}

				tx.update(PROJECT)
					.set(PROJECT.EPRO_PROFILE_ID, resolved)
					.where(PROJECT.PROJECT_ID.eq(projectId))
					.execute();
			}
		});
	}
}
