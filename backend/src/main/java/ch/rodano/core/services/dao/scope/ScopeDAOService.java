package ch.rodano.core.services.dao.scope;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.function.Function;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.ScopeAuditTrail;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeSearch;

public interface ScopeDAOService {

	/**
	 * Get all (including deleted) scopes
	 *
	 * @return All scopes
	 */
	List<Scope> getAllScopes();

	/**
	 * Get all (including deleted) scopes matching the provided scope model id
	 *
	 * @param scopeModelId The scope model id
	 * @return The scopes matching the scope model id
	 */
	List<Scope> getAllScopesByScopeModelId(String scopeModelId);

	/**
	 * Get the root scope
	 *
	 * @return The root scope
	 */
	Scope getRootScope();

	/**
	 * Get a scope by its pk
	 *
	 * @param pk The scope pk
	 * @return A scope
	 */
	Scope getScopeByPk(Long pk);

	List<Scope> getScopesByPks(Collection<Long> pks);

	Scope getScopeByCode(String code);

	List<Scope> getScopesByCodes(Collection<String> codes);

	Scope getScopeById(String id);

	List<Scope> getScopesByIds(Collection<String> ids);

	List<Scope> getVirtualScopes();

	List<Scope> getScopesByScopeModelId(String scopeModelId);

	Integer getScopesByScopeModelIdCount(String scopeModelId);

	List<Scope> getScopesByScopeModelIdHavingAncestor(Collection<String> scopeModelIds, Collection<Long> ancestorPks);

	List<Scope> getDescendants(Long scopePk);

	List<Scope> getDescendants(Long scopePk, String scopeModelId);

	List<Scope> getAllEnabledDescendants(Long scopePk, String scopeModelId);

	List<Scope> getEnabledDescendants(Long scopePk, String scopeModelId);

	List<Scope> getAllEnabledDescendants(Long scopePk);

	List<Scope> getEnabledDescendants(Long scopePk);

	Integer getEnabledDescendantsByScopeModelIdCount(String scopeModelId, Long scopePk);

	Map<Long, Integer> getEnabledDescendantsByScopeModelIdCount(String scopeModelId, Collection<Long> scopePks);

	List<Scope> getAncestors(Long scopePk);

	List<Scope> getEnabledAncestors(Long scopePk);

	List<Scope> getDefaultAncestors(Long scopePk);

	boolean hasDeletedDefaultAncestor(Long scopePk);

	boolean isDescendantOf(Long scopePk, Long potentialAncestorPk);

	List<Scope> getVirtualAncestors(Long scopePk);

	boolean isDescendantOfEnabled(Long scopePk, Long potentialAncestorPk);

	void saveScope(Scope scope, DatabaseActionContext context, String rationale);

	void deleteScope(Scope scope, DatabaseActionContext context, String rationale);

	void restoreScope(Scope scope, DatabaseActionContext context, String rationale);

	NavigableSet<ScopeAuditTrail> getAuditTrails(Scope scope, Optional<Timeframe> timeframe, Optional<Long> actorPk);

	NavigableSet<ScopeAuditTrail> getAuditTrailsForProperty(Scope scope, Optional<Timeframe> timeframe, Function<ScopeAuditTrail, Object> property);

	NavigableSet<ScopeAuditTrail> getAuditTrailsForProperties(Scope scope, Optional<Timeframe> timeframe, List<Function<ScopeAuditTrail, Object>> properties);

	PagedResult<Scope> search(ScopeSearch search);

}
