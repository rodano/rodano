package ch.rodano.core.model.rules.entity;

import java.util.Map;

import ch.rodano.core.model.audit.DatabaseActionContext;

public interface StaticEntity extends IdentifiableEntity {
	/**
	 * Execute a static action
	 *
	 * @param parameters The parameters
	 */
	void action(Map<String, Object> parameters, DatabaseActionContext context);
}
