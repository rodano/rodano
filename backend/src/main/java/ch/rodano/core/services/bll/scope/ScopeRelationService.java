package ch.rodano.core.services.bll.scope;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeRelation;

public interface ScopeRelationService {

	ScopeRelation getRelationByPk(Long pk);

	/**
	 * Get an active relation between two scopes
	 * @param scope     Child scope
	 * @param parent    Parent scope
	 * @return          Active relation between them
	 */
	ScopeRelation getActiveRelation(Scope scope, Scope parent);

	/**
	 * Save scope relation
	 *
	 * @param relation The relation
	 * @param context  The context
	 * @param rationale The rationale for the operation
	 */
	void saveScopeRelation(ScopeRelation relation, DatabaseActionContext context, String rationale);

	/**
	 * Add a parent to a scope
	 *
	 * @param scope   The scope
	 * @param parent  The parent
	 * @param context The context
	 * @param rationale The rationale for the operation
	 * @return          The new relation
	 */
	default ScopeRelation createRelation(
		final Scope scope,
		final Scope parent,
		final DatabaseActionContext context,
		final String rationale
	) {
		return createRelation(scope, parent, ZonedDateTime.now(), Optional.empty(), context, rationale);
	}

	/**
	 * Add a parent to a scope
	 *
	 * @param scope     The scope
	 * @param parent    The parent
	 * @param startDate The start date
	 * @param context   The context
	 * @param rationale The rationale for the operation
	 * @return          The new relation
	 */
	ScopeRelation createRelation(
		Scope scope,
		Scope parent,
		ZonedDateTime startDate,
		Optional<ZonedDateTime> endDate,
		DatabaseActionContext context,
		String rationale
	);

	void endRelation(
		ScopeRelation relation,
		ZonedDateTime endDate,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Make a relation default
	 *
	 * @param relation  The relation
	 * @param context   The context
	 * @param rationale The rationale for the operation
	 */
	ScopeRelation makeDefault(
		ScopeRelation relation,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Transfer a scope from one parent to another
	 *
	 * @param scope         Scope to transfer
	 * @param newParent     The new parent scope
	 * @param transferDate  Date at which the new relation will start and the old one will end
	 * @param context       The context
	 * @return              The new parent relation
	 */
	ScopeRelation transfer(
		Scope scope,
		Scope newParent,
		ZonedDateTime transferDate,
		DatabaseActionContext context
	);

	List<Scope> getAncestors(Scope scope);

	List<Scope> getDefaultAncestors(Scope scope);

	List<Scope> getEnabledAncestors(Scope scope);

	List<Scope> getEnabledAncestors(Scope scope, ScopeModel scopeModel);

	Scope getParent(ScopeRelation scopeRelation);

	/**
	 * Get parents of a scope.
	 * @param scope Child scope
	 * @return      Scope's parents
	 */
	List<Scope> getParents(Scope scope);

	/**
	 * Get ALL the parents of a scope, even the deleted ones.
	 * @param scope Child scope
	 * @return      Scope's parents
	 */
	List<Scope> getAllParents(Scope scope);

	List<Scope> getEnabledParents(Scope scope, ZonedDateTime date);

	Scope getDefaultParent(Scope scope);

	List<ScopeRelation> getAllParentRelations(Scope scope);

	List<ScopeRelation> getNonDeletedParentRelations(Scope scope);

	List<ScopeRelation> getActiveParentRelations(Scope scope, ZonedDateTime date);

	ScopeRelation getDefaultParentRelation(Scope scope);

	Scope getChild(ScopeRelation scopeRelation);

	/**
	 * Get the children of a scope.
	 * @param scope Parent scope
	 * @return      Scope's children
	 */
	List<Scope> getChildren(Scope scope);

	/**
	 * Get ALL the children of a scope, even the deleted ones.
	 * @param scope Parent scope
	 * @return      Scope's children
	 */
	List<Scope> getAllChildren(Scope scope);

	/**
	 * Get the children whose relations are enabled at a specific date.
	 * @param scope Parent scope
	 * @param date  Date of the query
	 * @return      Scope's children whose relationships are enabled
	 */
	List<Scope> getEnabledChildren(Scope scope, ZonedDateTime date);

	List<ScopeRelation> getChildRelations(Scope scope);

	List<ScopeRelation> getNonDeletedChildRelations(Scope scope);

	List<ScopeRelation> getActiveChildRelations(Scope scope, ZonedDateTime date);

	List<Scope> getDescendants(Scope scope);

	List<Scope> getDescendants(Scope scope, ScopeModel scopeModel);

	List<Scope> getEnabledDescendants(Scope scope);

	List<Scope> getEnabledDescendants(Scope scope, ScopeModel scopeModel);

	/**
	 * Get all the enabled descendants (even the deleted ones)
	 * @param scope The parent scope
	 * @return descendants
	 */
	List<Scope> getAllEnabledDescendants(Scope scope);

	List<Scope> getAllEnabledDescendants(Scope scope, ScopeModel scopeModel);

	List<Scope> getAllSiblings(Scope scope);

	boolean areRelated(Long scopePk, Long potentialRelatedScopePk);

	boolean isAncestorOf(Long scopePk, Long potentialDescendantPk);

	boolean isParentOf(Scope scope, Scope potentialChild);

	boolean isChildOf(Scope scope, Scope potentialParent);

	boolean isDescendantOf(Long scopePk, Long potentialAncestorPk);

	boolean isDescendantOfEnabled(Long scopePk, Long potentialAncestorPk);

	boolean isDescendantOfEnabled(Collection<Scope> potentialAncestors, Scope scope);

	int getShortestDistanceBetween(Scope scope, Scope otherScope);

	boolean isVirtual(ScopeRelation relation);
}
