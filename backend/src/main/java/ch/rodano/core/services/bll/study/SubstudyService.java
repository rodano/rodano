package ch.rodano.core.services.bll.study;

import java.util.Collection;
import java.util.List;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.scope.FieldModelCriterion;
import ch.rodano.core.model.scope.Scope;

public interface SubstudyService {

	/**
	 * Find substudies that use the field model as a criteria
	 *
	 */
	List<Scope> getOpenSubstudiesByFieldModel(FieldModel fieldModel);

	/**
	 * Find potential substudies
	 *
	 */
	List<Scope> getOpenSubstudiesForCandidate(Scope candidate);

	/**
	 * Find potential scope pks according to a root scopes, a target scope model and attribute criteria
	 *
	 * @return A set of corresponding scope pks
	 */
	List<Scope> findPotentialScopes(Collection<Scope> rootScopes, ScopeModel targetScopeModel, List<FieldModelCriterion> substudyCriteria);

	/**
	 * Find potential scopes
	 *
	 */
	List<Scope> findPotentialScopes(Scope substudy);

	/**
	 * Enroll a scope in substudies
	 *
	 * @param candidate The candidate scope to be enrolled
	 * @param context   The context
	 */
	void enrollScopeInSubstudies(Scope candidate, Collection<Scope> substudies, DatabaseActionContext context, String rationale);

	/**
	 * Enroll scopes in a substudy
	 *
	 * @param substudy The substudy
	 * @param context  The context
	 * @return The enrolled scopes
	 */
	List<Scope> enrollScopesInSubstudy(Scope substudy, DatabaseActionContext context, String rationale);

	List<Scope> filterPotentialSubstudies(Collection<Scope> potentialSubstudies, Scope candidate);

}
