package ch.rodano.core.services.dao.scope;

import java.util.List;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.scope.ScopeRelation;

public interface ScopeRelationDAOService {

	ScopeRelation getScopeRelationByPk(Long pk);

	ScopeRelation getActiveScopeRelation(Long scopePk, Long parentPk);

	/**
	 * Get the parent relations for the given scope
	 * @param scopePk   Scope pk
	 * @return          Parent relations of the given scope
	 */
	List<ScopeRelation> getParentRelations(Long scopePk);

	/**
	 * Get the children relations for the given scope
	 * @param parentPk  Parent's pk
	 * @return          Children relations of the parent scope
	 */
	List<ScopeRelation> getChildrenRelations(Long parentPk);

	/**
	 * Create or update a scope relation
	 *
	 * @param relation The relation
	 * @param context  The context
	 * @param rationale The rationale for the operation
	 */
	void saveScopeRelation(ScopeRelation relation, DatabaseActionContext context, String rationale);

	void deleteScopeRelation(ScopeRelation relation);

	/**
	 * Get parent and children relations of the given scope
	 *
	 * @param scopePk The scope's PK
	 */
	List<ScopeRelation> getScopeRelations(Long scopePk);
}
