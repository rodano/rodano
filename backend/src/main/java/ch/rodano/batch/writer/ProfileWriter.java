package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Profile;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveProfileId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowId;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.Profile.PROFILE;

public class ProfileWriter extends BaseWriter {

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<Profile> wrapped = (ProjectScoped<Profile>) raw;
				final UUID projectId = wrapped.getProjectId();
				final Profile profile = wrapped.getPayload();

				final String profileCode = profile.getId();
				final UUID existing = resolveProfileId(tx, projectId, profileCode);
				final UUID profileId = existing != null ? existing : deterministic(projectId, "PROFILE", profileCode);

				final UUID workflowId = resolveWorkflowId(tx, projectId, profile.getWorkflowIdOfInterest());

				tx.insertInto(PROFILE)
					.set(PROFILE.PROJECT_ID, projectId)
					.set(PROFILE.PROFILE_ID, profileId)
					.set(PROFILE.CODE, profileCode)
					.set(PROFILE.ORDER_BY, profile.getOderBy())
					.set(PROFILE.WORKFLOW_OF_INTEREST_ID, workflowId)
					.set(PROFILE.SHORTNAME, toJson(profile.getShortname()))
					.set(PROFILE.LONGNAME, toJson(profile.getLongname()))
					.set(PROFILE.DESCRIPTION, toJson(profile.getDescription()))
					.onDuplicateKeyUpdate()
					.set(PROFILE.ORDER_BY, profile.getOderBy())
					.set(PROFILE.WORKFLOW_OF_INTEREST_ID, workflowId)
					.set(PROFILE.SHORTNAME, toJson(profile.getShortname()))
					.set(PROFILE.LONGNAME, toJson(profile.getLongname()))
					.set(PROFILE.DESCRIPTION, toJson(profile.getDescription()))
					.execute();
			}
		});
	}
}
