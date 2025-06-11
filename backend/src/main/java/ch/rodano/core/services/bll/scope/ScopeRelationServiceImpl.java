package ch.rodano.core.services.bll.scope;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.exception.LockedObjectException;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeRelation;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.dao.scope.ScopeRelationDAOService;
import ch.rodano.core.utils.UtilsService;

@Service
public class ScopeRelationServiceImpl implements ScopeRelationService {
	private final ScopeRelationDAOService scopeRelationDAOService;
	private final ScopeDAOService scopeDAOService;

	public ScopeRelationServiceImpl(
		final ScopeRelationDAOService scopeRelationDAOService,
		final ScopeDAOService scopeDAOService
	) {
		this.scopeRelationDAOService = scopeRelationDAOService;
		this.scopeDAOService = scopeDAOService;
	}

	@Override
	public ScopeRelation getRelationByPk(final Long pk) {
		return scopeRelationDAOService.getScopeRelationByPk(pk);
	}

	@Override
	public ScopeRelation getActiveRelation(final Scope scope, final Scope parent) {
		return scopeRelationDAOService.getActiveScopeRelation(scope.getPk(), parent.getPk());
	}

	@Override
	public void saveScopeRelation(final ScopeRelation relation, final DatabaseActionContext context, final String rationale) {
		scopeRelationDAOService.saveScopeRelation(relation, context, rationale);
	}

	@Override
	public ScopeRelation createRelation(
		final Scope scope,
		final Scope parent,
		final ZonedDateTime startDate,
		final Optional<ZonedDateTime> endDate,
		final DatabaseActionContext context,
		final String rationale
	) {
		// Perform the necessary checks
		checkLockedOrDeleted(scope, parent);

		// If the two scopes already have an active relation, no relation can be created
		if(isChildOf(scope, parent)) {
			throw new ScopeRelationException(
				String.format(
					"Scope %s is already a child of scope %s",
					scope.getCode(),
					parent.getCode()
				)
			);
		}

		// Check that there are no date overlaps with the already existing parent relations
		verifyNoRelationOverlap(scope, parent, startDate, endDate);

		// The relation between the two scopes must respect the scope model configuration
		//TODO should this throw a NoRespectForConfig exception ?
		if(!scope.getScopeModel().isChildOf(parent.getScopeModel())) {
			throw new ScopeRelationException(
				String.format(
					"Scope model %s is not a child of scope model %s",
					scope.getScopeModel().getDefaultLocalizedShortname(),
					parent.getScopeModel().getDefaultLocalizedShortname()
				)
			);
		}

		final var relation = new ScopeRelation();
		relation.setScopeFk(scope.getPk());
		relation.setParentFk(parent.getPk());
		relation.setStartDate(startDate);
		endDate.ifPresent(relation::setEndDate);
		relation.setDefault(getNonDeletedParentRelations(scope).isEmpty());

		// save to database
		scopeRelationDAOService.saveScopeRelation(relation, context, rationale);

		return relation;
	}

	@Override
	public void endRelation(
		final ScopeRelation relation,
		final ZonedDateTime endDate,
		final DatabaseActionContext context,
		final String rationale
	) {
		final var scope = scopeDAOService.getScopeByPk(relation.getScopeFk());
		final var parent = scopeDAOService.getScopeByPk(relation.getParentFk());

		// Perform the necessary checks
		checkLockedOrDeleted(scope, parent);

		// If the relation is the default relation, it can not be ended
		if(relation.getDefault()) {
			throw new ScopeRelationException("Unable to remove a default relation");
		}

		// If the given end date is before the start date of the relation, throw error
		if(endDate.isBefore(relation.getStartDate())) {
			throw new ScopeRelationException("The end date of the relation must be before the start date");
		}

		// If the given end date is past the already set end date, reject the operation.
		if(relation.getEndDate() != null && relation.getEndDate().isBefore(endDate)) {
			throw new ScopeRelationException("This relation has already ended");
		}

		// End the relationship
		if(isVirtual(relation)) {
			// If the relationship is virtual, HARD DELETE the relation
			scopeRelationDAOService.deleteScopeRelation(relation);
		}
		else {
			// If the relationship is not virtual, set the end date of the relationship
			relation.setEndDate(endDate);
			// Save the relationship in database
			scopeRelationDAOService.saveScopeRelation(relation, context, rationale);
		}
	}

	@Override
	public ScopeRelation makeDefault(
		final ScopeRelation relation,
		final DatabaseActionContext context,
		final String rationale
	) {
		final var scope = getChild(relation);
		final var parent = getParent(relation);

		// Perform the necessary checks
		checkLockedOrDeleted(scope, parent);

		if(isVirtual(relation)) {
			throw new ScopeRelationException("A virtual relation cannot be made default");
		}

		if(relation.getStartDate().isAfter(ZonedDateTime.now())) {
			throw new ScopeRelationException("A relation that starts in the future cannot be default");
		}

		// Make all the current default relations not default
		getNonDeletedParentRelations(scope).stream()
			.filter(ScopeRelation::getDefault)
			.forEach(rel -> {
				rel.setDefault(false);
				scopeRelationDAOService.saveScopeRelation(rel, context, rationale);
			});

		// Make the given relation default
		relation.setDefault(true);
		scopeRelationDAOService.saveScopeRelation(relation, context, rationale);

		return relation;
	}

	@Override
	public ScopeRelation transfer(
		final Scope scope,
		final Scope newParent,
		final ZonedDateTime transferDate,
		final DatabaseActionContext context
	) {
		// Perform the necessary checks
		checkLockedOrDeleted(scope, newParent);

		//invalidate current default relation
		final var oldParentRelation = getDefaultParentRelation(scope);
		oldParentRelation.setDefault(false);
		oldParentRelation.setEndDate(transferDate);
		scopeRelationDAOService.saveScopeRelation(oldParentRelation, context, "Scope transfer");

		// create new relation
		final var newParentRelation = createRelation(scope, newParent, transferDate, Optional.empty(), context, "Scope transfer");
		newParentRelation.setDefault(true);
		scopeRelationDAOService.saveScopeRelation(newParentRelation, context, "Scope transfer");

		return newParentRelation;
	}

	@Override
	public List<Scope> getAncestors(final Scope scope) {
		return scopeDAOService.getAncestors(scope.getPk());
	}

	@Override
	public List<Scope> getDefaultAncestors(final Scope scope) {
		return scopeDAOService.getDefaultAncestors(scope.getPk());
	}

	@Override
	public List<Scope> getEnabledAncestors(final Scope scope) {
		return scopeDAOService.getEnabledAncestors(scope.getPk());
	}

	@Override
	public List<Scope> getEnabledAncestors(final Scope scope, final ScopeModel scopeModel) {
		return this.getEnabledAncestors(scope).stream()
			.filter(s -> s.getScopeModelId().equals(scopeModel.getId()))
			.toList();
	}

	@Override
	public Scope getParent(final ScopeRelation scopeRelation) {
		return scopeDAOService.getScopeByPk(scopeRelation.getParentFk());
	}

	@Override
	public List<Scope> getParents(final Scope scope) {
		return this.getNonDeletedParentRelations(scope).stream()
			.map(this::getParent)
			.toList();
	}

	@Override
	public List<Scope> getAllParents(final Scope scope) {
		return this.getAllParentRelations(scope).stream()
			.map(this::getParent)
			.toList();
	}

	@Override
	public List<Scope> getEnabledParents(final Scope scope, final ZonedDateTime date) {
		return this.getActiveParentRelations(scope, date).stream()
			.map(this::getParent)
			.toList();
	}

	@Override
	public Scope getDefaultParent(final Scope scope) {
		return this.getAllParentRelations(scope).stream()
			.filter(ScopeRelation::getDefault)
			.findFirst()
			.map(this::getParent)
			.orElseThrow(() -> new ScopeRelationException("No default parent found for " + scope.getLocalizedShortname()));
	}

	@Override
	public List<ScopeRelation> getAllParentRelations(final Scope scope) {
		return scopeRelationDAOService.getParentRelations(scope.getPk());
	}

	@Override
	public List<ScopeRelation> getNonDeletedParentRelations(final Scope scope) {
		return getNonDeletedParentRelationsStream(scope).toList();
	}

	@Override
	public List<ScopeRelation> getActiveParentRelations(final Scope scope, final ZonedDateTime date) {
		return getNonDeletedParentRelationsStream(scope)
			.filter(rel -> isEnabled(rel, date))
			.toList();
	}

	@Override
	public ScopeRelation getDefaultParentRelation(final Scope scope) {
		return getNonDeletedParentRelations(scope).stream()
			.filter(ScopeRelation::getDefault)
			.findFirst()
			.orElseThrow(() -> new ScopeRelationException("No default parent relation found for " + scope.getLocalizedShortname()));
	}

	@Override
	public Scope getChild(final ScopeRelation scopeRelation) {
		return scopeDAOService.getScopeByPk(scopeRelation.getScopeFk());
	}

	@Override
	public List<Scope> getChildren(final Scope scope) {
		return this.getNonDeletedChildRelations(scope).stream()
			.map(this::getChild)
			.toList();
	}

	@Override
	public List<Scope> getAllChildren(final Scope scope) {
		return this.getChildRelations(scope).stream()
			.map(this::getChild)
			.toList();
	}

	@Override
	public List<Scope> getEnabledChildren(final Scope scope, final ZonedDateTime date) {
		return this.getActiveChildRelations(scope, date).stream()
			.map(this::getChild)
			.toList();
	}

	@Override
	public List<ScopeRelation> getChildRelations(final Scope scope) {
		return scopeRelationDAOService.getChildrenRelations(scope.getPk());
	}

	@Override
	public List<ScopeRelation> getNonDeletedChildRelations(final Scope scope) {
		return getNonDeletedChildRelationsStream(scope)
			.toList();
	}

	@Override
	public List<ScopeRelation> getActiveChildRelations(final Scope scope, final ZonedDateTime date) {
		return getNonDeletedChildRelationsStream(scope)
			.filter(rel -> isEnabled(rel, date))
			.toList();
	}

	@Override
	public List<Scope> getDescendants(final Scope scope) {
		return scopeDAOService.getDescendants(scope.getPk());
	}

	@Override
	public List<Scope> getDescendants(final Scope scope, final ScopeModel scopeModel) {
		return scopeDAOService.getDescendants(scope.getPk(), scopeModel.getId());
	}

	@Override
	public List<Scope> getEnabledDescendants(final Scope scope) {
		return scopeDAOService.getEnabledDescendants(scope.getPk());
	}

	@Override
	public List<Scope> getEnabledDescendants(final Scope scope, final ScopeModel scopeModel) {
		return scopeDAOService.getEnabledDescendants(scope.getPk(), scopeModel.getId());
	}

	@Override
	public List<Scope> getAllEnabledDescendants(final Scope scope) {
		return scopeDAOService.getAllEnabledDescendants(scope.getPk());
	}

	@Override
	public List<Scope> getAllEnabledDescendants(final Scope scope, final ScopeModel scopeModel) {
		return scopeDAOService.getAllEnabledDescendants(scope.getPk(), scopeModel.getId());
	}

	@Override
	public List<Scope> getAllSiblings(final Scope scope) {
		return this.getAllParents(scope).stream()
			.flatMap(parent -> this.getAllChildren(parent).stream())
			.toList();
	}

	@Override
	public boolean areRelated(final Long scopePk, final Long potentialRelatedScopePk) {
		return scopePk.equals(potentialRelatedScopePk) || isDescendantOf(scopePk, potentialRelatedScopePk) || isDescendantOf(potentialRelatedScopePk, scopePk);
	}

	@Override
	public boolean isAncestorOf(final Long scopePk, final Long potentialDescendantPk) {
		return scopeDAOService.isDescendantOf(potentialDescendantPk, scopePk);
	}

	@Override
	public boolean isParentOf(final Scope scope, final Scope potentialChild) {
		return isChildOf(potentialChild, scope);
	}

	@Override
	public boolean isChildOf(final Scope scope, final Scope potentialParent) {
		return this.getEnabledChildren(potentialParent, ZonedDateTime.now()).stream()
			.anyMatch(child -> child.getPk().equals(scope.getPk()));
	}

	@Override
	public boolean isDescendantOf(final Long scopePk, final Long potentialAncestorPk) {
		return scopeDAOService.isDescendantOf(scopePk, potentialAncestorPk);
	}

	@Override
	public boolean isDescendantOfEnabled(final Long scopePk, final Long potentialAncestorPk) {
		return scopeDAOService.isDescendantOfEnabled(scopePk, potentialAncestorPk);
	}

	@Override
	public boolean isDescendantOfEnabled(final Collection<Scope> potentialAncestors, final Scope scope) {
		return potentialAncestors.stream()
			.anyMatch(potential -> isDescendantOfEnabled(scope.getPk(), potential.getPk()));
	}

	@Override
	public int getShortestDistanceBetween(final Scope scope, final Scope otherScope) {
		if(Objects.equals(scope.getPk(), otherScope.getPk())) {
			return 0;
		}

		final var ancestors = getShortestDistanceToAncestor(scope, otherScope);
		final var descendants = getShortestDistanceToDescendant(scope, otherScope);

		if(ancestors.isEmpty() && descendants.isEmpty()) {
			throw new ScopeRelationException(String.format("Scope %s is not related to scope %s", scope.getCode(), otherScope.getCode()));
		}
		if(ancestors.isEmpty()) {
			return descendants.get();
		}
		else if(descendants.isEmpty()) {
			return ancestors.get();
		}
		else {
			return Math.min(ancestors.get(), descendants.get());
		}
	}

	@Override
	public boolean isVirtual(final ScopeRelation relation) {
		final var parent = scopeDAOService.getScopeByPk(relation.getParentFk());
		return parent.getVirtual();
	}

	private void checkLockedOrDeleted(final Scope scope, final Scope parent) {
		// If one of the scopes is deleted, no relation can be created
		if(scope.getDeleted() || parent.getDeleted()) {
			throw new ScopeRelationException("Can not add a relation to a scope that is deleted");
		}

		// If either of the scopes is locked, no relation can be created
		if(scope.getLocked()) {
			throw new LockedObjectException(scope);
		}

		if(parent.getLocked()) {
			throw new LockedObjectException(parent);
		}
	}

	/**
	 * Checks that there is no overlap in the relationships between the given scopes, otherwise throws an error.
	 * @param scope         Child scope
	 * @param parent        Parent scope
	 * @param startDate     Start date of the relation
	 * @param endDate       End date of the relation
	 * @throws ScopeRelationException Thrown if there is a relation overlap between the two scopes
	 */
	private void verifyNoRelationOverlap(
		final Scope scope,
		final Scope parent,
		final ZonedDateTime startDate,
		final Optional<ZonedDateTime> endDate
	) {
		final var parentRelations = this.getAllParentRelations(scope).stream()
			.filter(rel -> rel.getParentFk().equals(parent.getPk()))
			.toList();

		// If the new relation starts before an already existing one and does not have a defined end, it can not be added
		if(endDate.isEmpty()) {
			final var relationStartBeforeAnExistingOneAndDoesNotHaveAnEnd = parentRelations.stream()
				.anyMatch(rel -> startDate.isBefore(rel.getStartDate()));
			if(relationStartBeforeAnExistingOneAndDoesNotHaveAnEnd) {
				throw new ScopeRelationException(
					String.format(
						"A relation is already defined in the future between %s and %s",
						scope.getCode(),
						parent.getCode()
					)
				);
			}
		}
		else {
			// If a new relation already had a relation with the same parent, their date ranges must not overlap
			final var parentRelationDateOverlap = parentRelations.stream()
				.anyMatch(rel -> startDate.isBefore(rel.getEndDate()) && endDate.get().isAfter(rel.getStartDate()));
			if(parentRelationDateOverlap) {
				throw new ScopeRelationException(
					String.format(
						"%s already had an active relation with %s at %s",
						scope.getCode(),
						parent.getCode(),
						startDate.format(UtilsService.HUMAN_READABLE_DATE_TIME)
					)
				);
			}
		}
	}

	private boolean isEnabled(final ScopeRelation relation, final ZonedDateTime date) {
		return isVirtual(relation) || (relation.getStartDate().equals(date) || relation.getStartDate().isBefore(date)) && (relation.getEndDate() == null || relation.getEndDate().isAfter(date));
	}

	private Stream<ScopeRelation> getNonDeletedParentRelationsStream(final Scope scope) {
		return scopeRelationDAOService.getParentRelations(scope.getPk()).stream()
			.filter(relation -> {
				final var parentScope = scopeDAOService.getScopeByPk(relation.getParentFk());
				return !parentScope.getDeleted();
			});
	}

	private Stream<ScopeRelation> getNonDeletedChildRelationsStream(final Scope scope) {
		return scopeRelationDAOService.getChildrenRelations(scope.getPk()).stream()
			.filter(relation -> {
				final var childScope = scopeDAOService.getScopeByPk(relation.getScopeFk());
				return !childScope.getDeleted();
			});
	}

	private Optional<Integer> getShortestDistanceToAncestor(final Scope scope, final Scope ancestor) {
		final var parents = this.getAllParents(scope);

		// No parent = root
		if(parents.isEmpty()) {
			return Optional.empty();
		}

		Optional<Integer> min = Optional.empty();
		for(final var parent : parents) {
			if(parent == ancestor) {
				return Optional.of(1);
			}
			final var distance = getShortestDistanceToAncestor(parent, ancestor);
			if(getShortestDistanceToAncestor(parent, ancestor).isPresent()) {
				if(min.isEmpty() || distance.isPresent() && distance.get() < min.get()) {
					min = distance;
				}
			}
		}

		return min.map(integer -> integer + 1);
	}

	private Optional<Integer> getShortestDistanceToDescendant(final Scope scope, final Scope descendant) {
		final var children = this.getAllChildren(scope);

		// No parent = root
		if(children.isEmpty()) {
			return Optional.empty();
		}

		Optional<Integer> min = Optional.empty();
		for(final var child : children) {
			if(child == descendant) {
				return Optional.of(1);
			}
			final var distance = getShortestDistanceToDescendant(child, descendant);
			if(getShortestDistanceToDescendant(child, descendant).isPresent()) {
				if(min.isEmpty() || distance.isPresent() && distance.get() < min.get()) {
					min = distance;
				}
			}
		}

		return min.map(integer -> integer + 1);
	}

}
