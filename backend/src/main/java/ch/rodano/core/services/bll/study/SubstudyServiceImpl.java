package ch.rodano.core.services.bll.study;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ch.rodano.configuration.exceptions.NoRespectForConfigurationException;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.scope.EnrollmentType;
import ch.rodano.core.model.scope.FieldModelCriterion;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeSearch;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;

@Service
public class SubstudyServiceImpl implements SubstudyService {

	private final StudyService studyService;
	private final ScopeService scopeService;
	private final ScopeDAOService scopeDAOService;
	private final DatasetService datasetService;
	private final FieldService fieldService;
	private final ScopeRelationService scopeRelationService;

	public SubstudyServiceImpl(
		final StudyService studyService,
		final ScopeService scopeService,
		final ScopeDAOService scopeDAOService,
		final DatasetService datasetService,
		final FieldService fieldService,
		final ScopeRelationService scopeRelationService
	) {
		this.studyService = studyService;
		this.scopeService = scopeService;
		this.scopeDAOService = scopeDAOService;
		this.datasetService = datasetService;
		this.fieldService = fieldService;
		this.scopeRelationService = scopeRelationService;
	}

	private Boolean checkScope(final Scope candidate, final Scope substudy, final Set<String> candidateAncestorIds) {
		if(!candidate.getScopeModel().isDescendantOf(substudy.getScopeModel())) {
			return false;
		}
		final var enrollmentModel = substudy.getData().getEnrollmentModel();

		if(!enrollmentModel.isSystem()) {
			if(enrollmentModel.getScopesContainerIds() == null) {
				return false;
			}
			if(enrollmentModel.getScopesContainerIds().stream().noneMatch(candidateAncestorIds::contains)) {
				return false;
			}
		}

		for(final var criterion : enrollmentModel.getCriteria()) {
			if(criterion.isValid()) {
				final var datasetModel = studyService.getStudy().getDatasetModel(criterion.datasetModelId());
				final var fieldModel = datasetModel.getFieldModel(criterion.fieldModelId());
				final Dataset dataset = datasetService.get(candidate, datasetModel);
				final var field = fieldService.get(dataset, fieldModel);
				//convert values
				final var criterionValue = fieldModel.stringToObject(criterion.value());
				final var fieldValue = fieldModel.stringToObject(field.getValue());
				//test criterion
				final var operator = criterion.operator();
				final var test = operator.hasValue() ? operator.test(fieldModel.getDataType(), fieldValue, criterionValue) : operator.test(fieldModel.getDataType(), fieldValue);
				if(!test) {
					return false;
				}
			}
		}

		return true;
	}

	public List<Scope> getOpenSubstudies() {
		return scopeDAOService.getVirtualScopes().stream()
			.filter(s -> !s.getDeleted() && !s.isClosed() && s.getData().getEnrollmentModel() != null)
			.toList();

	}

	@Override
	public List<Scope> getOpenSubstudiesByFieldModel(final FieldModel fieldModel) {
		final Predicate<Scope> filterSubstudy = s -> s.getData().getEnrollmentModel().getCriteria().stream().anyMatch(
			c -> c.datasetModelId().equals(fieldModel.getDatasetModel().getId()) && c.fieldModelId().equals(fieldModel.getId())
		);
		return getOpenSubstudies().stream().filter(filterSubstudy).toList();
	}

	@Override
	public List<Scope> getOpenSubstudiesForCandidate(final Scope candidate) {
		return filterPotentialSubstudies(getOpenSubstudies(), candidate);
	}

	@Override
	public List<Scope> filterPotentialSubstudies(final Collection<Scope> potentialSubstudies, final Scope candidate) {
		final var candidateAncestorIds = scopeRelationService.getEnabledAncestors(candidate).stream().map(Scope::getId).collect(Collectors.toSet());
		return potentialSubstudies.stream()
			.filter(substudy -> !candidateAncestorIds.contains(substudy.getId()))
			.filter(substudy -> checkScope(candidate, substudy, candidateAncestorIds))
			.toList();
	}

	@Override
	public List<Scope> findPotentialScopes(final Collection<Scope> rootScopes, final ScopeModel targetScopeModel, final List<FieldModelCriterion> substudyCriteria) {

		final var scopePredicate = new ScopeSearch()
			.enforceAncestorPks(rootScopes.stream().map(Scope::getPk).toList())
			.enforceScopeModelId(targetScopeModel.getId())
			.enforceFieldModelCriteria(substudyCriteria);

		return scopeService.search(scopePredicate).getObjects();
	}

	@Override
	public List<Scope> findPotentialScopes(final Scope substudy) {
		//TODO search all types of scopes
		final var targetScopeModel = substudy.getScopeModel().getDescendantsScopeModel().get(0);
		final var enrollmentModel = substudy.getData().getEnrollmentModel();
		final var rootScopes = enrollmentModel.isSystem() ? Collections.singletonList(scopeService.getRootScope()) : scopeDAOService.getScopesByIds(enrollmentModel.getScopesContainerIds());
		final var scopes = findPotentialScopes(rootScopes, targetScopeModel, enrollmentModel.getCriteria());
		return scopes.stream()
			.filter(s -> !scopeRelationService.isDescendantOfEnabled(s.getPk(), substudy.getPk()))
			.toList();
	}

	@Override
	public void enrollScopeInSubstudies(final Scope candidate, final Collection<Scope> substudies, final DatabaseActionContext context, final String rationale) {
		for(final var substudy : substudies) {
			if(EnrollmentType.AUTOMATIC == substudy.getData().getEnrollmentModel().getType()) {
				scopeRelationService.createRelation(candidate, substudy, context, rationale);
			}
		}
	}

	@Override
	public List<Scope> enrollScopesInSubstudy(final Scope substudy, final DatabaseActionContext context, final String rationale) {
		final var enrollmentModel = substudy.getData().getEnrollmentModel();
		if(EnrollmentType.AUTOMATIC != enrollmentModel.getType()) {
			throw new NoRespectForConfigurationException(String.format("Unable to auto enroll for non automatic enrollment substudy %s", substudy.getId()));
		}

		final var candidates = findPotentialScopes(substudy);
		for(final var candidate : candidates) {
			// Save is done in addParent
			scopeRelationService.createRelation(candidate, substudy, context, rationale);
		}
		return candidates;
	}

}
