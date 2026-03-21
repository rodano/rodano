package ch.rodano.core.services.dao.configurator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.WorkflowRightsDTO;

import static ch.rodano.core.model.jooq.tables.ProfileWorkflowActionRights.PROFILE_WORKFLOW_ACTION_RIGHTS;
import static ch.rodano.core.model.jooq.tables.ProfileWorkflowRights.PROFILE_WORKFLOW_RIGHTS;

@Repository
public class WorkflowRightsDAOServiceImpl implements WorkflowRightsDAOService {

	private final DSLContext dslContext;

	public WorkflowRightsDAOServiceImpl(final DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "workflowRights", key = "#projectId.toString()")
	public WorkflowRightsDTO getWorkflowRights(final UUID projectId) {
		final Map<UUID, List<UUID>> workflowRights = new HashMap<>();
		dslContext.selectFrom(PROFILE_WORKFLOW_RIGHTS)
			.where(PROFILE_WORKFLOW_RIGHTS.PROJECT_ID.eq(projectId))
			.and(PROFILE_WORKFLOW_RIGHTS.HAS_RIGHT.isTrue())
			.fetch()
			.forEach(row -> workflowRights
				.computeIfAbsent(row.getProfileId(), _ -> new ArrayList<>())
				.add(row.getWorkflowId()));

		final Map<UUID, List<UUID>> actionRights = new HashMap<>();
		dslContext.selectFrom(PROFILE_WORKFLOW_ACTION_RIGHTS)
			.where(PROFILE_WORKFLOW_ACTION_RIGHTS.PROJECT_ID.eq(projectId))
			.fetch()
			.forEach(row -> actionRights
				.computeIfAbsent(row.getWorkflowActionId(), _ -> new ArrayList<>())
				.add(row.getProfileId()));

		return new WorkflowRightsDTO(workflowRights, actionRights);
	}

	@Override
	@Transactional
	@CacheEvict(value = "workflowRights", key = "#projectId.toString()")
	public void saveWorkflowRights(final UUID projectId, final WorkflowRightsDTO dto) {
		dslContext.deleteFrom(PROFILE_WORKFLOW_RIGHTS)
			.where(PROFILE_WORKFLOW_RIGHTS.PROJECT_ID.eq(projectId))
			.execute();

		dslContext.deleteFrom(PROFILE_WORKFLOW_ACTION_RIGHTS)
			.where(PROFILE_WORKFLOW_ACTION_RIGHTS.PROJECT_ID.eq(projectId))
			.execute();

		if(dto.workflowRights() != null) {
			dto.workflowRights().forEach((profileId, workflowIds) ->
				workflowIds.forEach(workflowId ->
					dslContext.insertInto(PROFILE_WORKFLOW_RIGHTS)
						.set(PROFILE_WORKFLOW_RIGHTS.PROJECT_ID, projectId)
						.set(PROFILE_WORKFLOW_RIGHTS.PROFILE_ID, profileId)
						.set(PROFILE_WORKFLOW_RIGHTS.WORKFLOW_ID, workflowId)
						.set(PROFILE_WORKFLOW_RIGHTS.HAS_RIGHT, true)
						.execute()
				)
			);
		}

		if(dto.actionRights() != null) {
			dto.actionRights().forEach((actionId, profileIds) ->
				profileIds.forEach(profileId ->
					dslContext.insertInto(PROFILE_WORKFLOW_ACTION_RIGHTS)
						.set(PROFILE_WORKFLOW_ACTION_RIGHTS.PROJECT_ID, projectId)
						.set(PROFILE_WORKFLOW_ACTION_RIGHTS.WORKFLOW_ACTION_ID, actionId)
						.set(PROFILE_WORKFLOW_ACTION_RIGHTS.PROFILE_ID, profileId)
						.set(PROFILE_WORKFLOW_ACTION_RIGHTS.GRANTED_BY_SYSTEM, true)
						.execute()
				)
			);
		}
	}
}
