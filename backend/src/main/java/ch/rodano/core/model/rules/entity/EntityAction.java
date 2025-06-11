package ch.rodano.core.model.rules.entity;

import java.util.Map;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.rules.Evaluable;

public interface EntityAction extends IdentifiableEntity {
	void action(final Evaluable evaluable, final Map<String, Object> parameters, final DatabaseActionContext context, String message, Map<String, Object> data) throws Exception;
}
