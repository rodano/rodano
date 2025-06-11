package ch.rodano.api.scope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.ConfigDTOService;
import ch.rodano.api.workflow.WorkflowDTOService;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeData;
import ch.rodano.core.model.scope.ScopeExtension;
import ch.rodano.core.model.user.UserSearch;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.workflowStatus.AggregateWorkflowDAOService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.core.services.dao.workflow.WorkflowStatusDAOService;
import ch.rodano.core.utils.ACL;
import ch.rodano.core.utils.RightsService;

@Service
@Transactional(readOnly = true)
public class ScopeDTOServiceImpl implements ScopeDTOService {

	private final ConfigDTOService configDTOService;
	private final UserDAOService userDAOService;
	private final ScopeService scopeService;
	private final WorkflowStatusDAOService workflowStatusDAOService;
	private final AggregateWorkflowDAOService aggregateWorkflowDAOService;
	private final RightsService rightsService;
	private final WorkflowDTOService workflowDTOService;
	private final ScopeRelationService scopeRelationService;
	private final StudyService studyService;
	private final FieldDAOService fieldDAOService;

	public ScopeDTOServiceImpl(
		final ConfigDTOService configDTOService,
		final UserDAOService userDAOService,
		final ScopeService scopeService,
		final WorkflowStatusDAOService workflowStatusDAOService,
		final AggregateWorkflowDAOService aggregateWorkflowDAOService,
		final RightsService rightsService,
		final WorkflowDTOService workflowDTOService,
		final ScopeRelationService scopeRelationService,
		final StudyService studyService,
		final FieldDAOService fieldDAOService
	) {
		this.configDTOService = configDTOService;
		this.userDAOService = userDAOService;
		this.scopeService = scopeService;
		this.workflowStatusDAOService = workflowStatusDAOService;
		this.aggregateWorkflowDAOService = aggregateWorkflowDAOService;
		this.rightsService = rightsService;
		this.workflowDTOService = workflowDTOService;
		this.scopeRelationService = scopeRelationService;
		this.studyService = studyService;
		this.fieldDAOService = fieldDAOService;
	}

	private List<FieldModel> getSearchableFieldModels() {
		// get the searchable fields for the selected scopes
		return studyService.getStudy()
			.getLeafScopeModel().getDatasetModels().stream()
			.flatMap(datasetModel -> datasetModel.getFieldModels().stream())
			.filter(FieldModel::isSearchable)
			.toList();
	}

	@Override
	public Scope generateScope(final ScopeCandidateDTO scopeCandidateDTO) {
		final var scope = new Scope();

		scope.setCode(scopeCandidateDTO.code());
		scope.setShortname(scopeCandidateDTO.shortname());
		scope.setStartDate(scopeCandidateDTO.startDate());
		scope.setStopDate(scopeCandidateDTO.stopDate());

		final var scopeData = new ScopeData();
		scope.setData(scopeData);

		return scope;
	}

	@Override
	public void updateScope(final Scope scope, final ScopeDTO scopeDTO) {
		scope.setCode(scopeDTO.getCode());
		scope.setShortname(scopeDTO.getShortname());
		scope.setLongname(scopeDTO.getLongname());

		scope.setStartDate(scopeDTO.getStartDate());
		scope.setStopDate(scopeDTO.getStopDate());

		final var scopeData = new ScopeData();
		scopeData.setDescription(scopeDTO.getDescription());
		scopeData.setEnrollmentTargets(scopeDTO.getEnrollmentTargets());
		scopeData.setEnrollmentModel(scopeDTO.getEnrollmentModel());

		scope.setData(scopeData);

		scope.setExpectedNumber(scopeDTO.getExpectedNumber());
		scope.setMaxNumber(scopeDTO.getMaxNumber());
	}

	@Override
	public List<ScopeDTO> createDTOs(final Collection<Scope> scopes, final ACL acl) {
		if(scopes.isEmpty()) {
			return Collections.emptyList();
		}
		final var scopePks = scopes.stream().map(Scope::getPk).toList();
		final var workflowStatuses = new ArrayList<WorkflowStatus>();
		//retrieve all workflow statuses for the selected scopes
		workflowStatuses.addAll(workflowStatusDAOService.getWorkflowStatusesByScopePks(scopePks));
		//retrieve all aggregated workflow statuses for the selected scopes
		workflowStatuses.addAll(aggregateWorkflowDAOService.getAggregateWorkflowStatusByScopes(scopes));
		//group all workflow statuses by scope pks
		final var workflowStatusesByScopePk = workflowStatuses.stream().collect(Collectors.groupingBy(WorkflowStatus::getScopeFk));

		final var leavesCountByScope = scopeService.getLeafCount(scopes);

		// filter the scopes to only keep those that have searchable fields
		// and group the fields by scope pk
		final var fieldByScopePk = scopes.stream()
			.filter(
				scope -> scope.getScopeModel().getDatasetModels().stream().anyMatch(
					datasetModel -> datasetModel.getFieldModels().stream()
						.anyMatch(FieldModel::isSearchable)
				)
			)
			.collect(
				Collectors.toMap(
					Scope::getPk,
					scope -> fieldDAOService.getFieldsByScopePk(scope.getPk()).stream()
						.filter(
							field -> getSearchableFieldModels().stream()
								.anyMatch(fieldModel -> fieldModel.getDatasetModel().equals(field.getDatasetModel()) && fieldModel.getId().equals(field.getFieldModelId()))
						)
						.toList()
				)
			);

		final List<ScopeDTO> dtos = new ArrayList<>();
		for(final Scope scope : scopes) {
			//recalculate ACL for each scope
			final var scopeACL = rightsService.getACL(acl.actor(), scope);
			if(scopeACL.hasRight(scope.getScopeModel(), Rights.READ)) {
				final var dto = createDTO(
					scope,
					scopeACL,
					workflowStatusesByScopePk.getOrDefault(scope.getPk(), Collections.emptyList()),
					fieldByScopePk.getOrDefault(scope.getPk(), Collections.emptyList()),
					leavesCountByScope.getOrDefault(scope, 0)
				);
				dtos.add(dto);
			}
		}
		return dtos;
	}

	@Override
	public ScopeDTO createDTO(final Scope scope, final ACL acl) {
		final var workflowStatuses = new ArrayList<WorkflowStatus>();
		workflowStatuses.addAll(workflowStatusDAOService.getWorkflowStatusesByScopePk(scope.getPk()));
		workflowStatuses.addAll(aggregateWorkflowDAOService.getAggregateWorkflowStatusByScope(scope));

		final var fields = fieldDAOService.getFieldsByScopePk(scope.getPk()).stream()
			.filter(field -> field.getFieldModel().isSearchable())
			.toList();

		return createDTO(scope, acl, workflowStatuses, fields, scopeService.getLeafCount(scope));
	}

	private ScopeDTO createDTO(
		final Scope scope,
		final ACL acl,
		final List<WorkflowStatus> workflowStatuses,
		final List<Field> fields,
		final Integer leaves
	) {
		final var model = scope.getScopeModel();

		final var dto = new ScopeDTO();

		dto.pk = scope.getPk();
		dto.id = scope.getId();
		dto.code = scope.getCode();

		dto.creationTime = scope.getCreationTime();
		dto.lastUpdateTime = scope.getLastUpdateTime();

		dto.canWrite = acl.hasRight(model, Rights.WRITE);
		dto.canBeRemoved = dto.canWrite && !scope.getLocked();

		dto.shortname = scope.getShortname();
		dto.longname = scope.getLongname();
		dto.modelId = scope.getScopeModelId();
		dto.virtual = scope.getVirtual();
		dto.startDate = scope.getStartDate();
		dto.stopDate = scope.getStopDate();

		dto.model = configDTOService.createScopeModelDTO(model, acl);
		dto.root = scopeService.isRootScope(scope);

		dto.locked = scope.getLocked();
		dto.removed = scope.getDeleted();
		dto.expectedNumber = scope.getExpectedNumber();
		dto.maxNumber = scope.getMaxNumber();

		final var scopeData = scope.getData();
		dto.description = scopeData.getDescription();
		dto.enrollmentModel = scopeData.getEnrollmentModel();
		dto.enrollmentTargets = scopeData.getEnrollmentTargets();

		dto.leaves = leaves;

		//workflows
		dto.workflowStatuses = new ArrayList<>();
		dto.possibleWorkflows = new ArrayList<>();

		final var family = new DataFamily(scope);
		final var workflowComparator = Workflow.getWorkflowableComparator(model);
		dto.workflowStatuses = workflowStatuses
			.stream()
			.filter(w -> acl.hasRight(w.getWorkflow()))
			.sorted(WorkflowStatus.proxyComparator(workflowComparator))
			.map(ws -> workflowDTOService.createWorkflowStatusDTO(family, ws, acl))
			.toList();

		//workflow that can be created
		dto.possibleWorkflows = model.getWorkflows()
			.stream()
			.filter(w -> !w.isMandatory() && w.getActionId() != null)
			.filter(w -> !w.isUnique() || dto.workflowStatuses.stream().noneMatch(ws -> ws.getWorkflowId().equals(w.getId())))
			.filter(w -> acl.hasRight(w.getAction()))
			.sorted(workflowComparator)
			.map(w -> workflowDTOService.createWorkflowDTO(w, acl))
			.toList();

		//retrieve user linked to this scope
		//used in scope list to display the user in charge of the scope
		dto.defaultProfileId = scope.getScopeModel().getDefaultProfileId();
		if(StringUtils.isNotBlank(dto.defaultProfileId)) {
			final var search = new UserSearch()
				.enforceScopePks(Collections.singleton(scope.getPk()))
				.enforceProfileIds(Collections.singleton(dto.defaultProfileId))
				.enforceExtension(ScopeExtension.BRANCH)
				.setEnabled(Optional.of(true));

			final var users = userDAOService.search(search).getObjects();
			if(!users.isEmpty()) {
				final var mainUser = users.iterator().next();
				dto.mainUserPk = mainUser.getPk();
				dto.mainUserName = mainUser.getName();
			}
		}

		dto.subscriptionRestrictions = scopeData.getSubscriptionRestrictions();

		// Handle the scope is root scope
		if(scopeService.isRootScope(scope)) {
			dto.root = true;
			return dto;
		}

		final var parentScope = new ScopeTinyDTO(scopeRelationService.getDefaultParent(scope));
		dto.setParentScope(parentScope);

		dto.searchableFields = new HashMap<>();
		for(final var field : fields) {
			final var fieldModel = field.getFieldModel();
			if(dto.searchableFields.containsKey(fieldModel.getDatasetModel().getId())) {
				dto.searchableFields.get(fieldModel.getDatasetModel().getId()).put(field.getId(), field.getValue());
			}
			else {
				final var map = new HashMap<String, String>();
				map.put(fieldModel.getId(), field.getValue());
				dto.searchableFields.put(field.getDatasetModel().getId(), map);
			}
		}

		return dto;
	}

}
