package ch.rodano.core.services.rule;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.rodano.configuration.model.rules.Rule;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.rules.data.DataState;

public interface RuleService {
	/**
	 * Execute rules
	 */
	List<Map<String, String>> execute(DataState state, List<Rule> rules, DatabaseActionContext context);

	/**
	 * Execute rules
	 */
	List<Map<String, String>> execute(DataState state, List<Rule> rules, DatabaseActionContext context, String message);

	/**
	 * Execute rules
	 */
	List<Map<String, String>> execute(DataState state, List<Rule> rules, DatabaseActionContext context, String message, Map<String, Object> data);

	/**
	 * Execute rules
	 */
	List<Map<String, String>> execute(DataState state, List<Rule> rules, DatabaseActionContext context, String message, Map<String, Object> data, Set<String> blockedActions);

}
