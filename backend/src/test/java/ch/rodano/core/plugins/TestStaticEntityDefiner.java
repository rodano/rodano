package ch.rodano.core.plugins;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.rules.entity.EntityDefinerComponent;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.model.rules.entity.StaticEntity;
import ch.rodano.core.plugins.entity.AbstractStaticEntityDefiner;

@EntityDefinerComponent
public final class TestStaticEntityDefiner extends AbstractStaticEntityDefiner {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Get all registered identifiable entity beans
	 *
	 * @return Registered identifiable entity beans
	 */
	@Override
	public List<IdentifiableEntity> getRegisteredBeans() {
		return List.of(
			new StaticEntity() {
				@Override
				public void action(final Map<String, Object> parameters, final DatabaseActionContext context) {
					logger.info("WRITE TO CONSOLE STATIC ACTION OVERRIDEN");
				}

				@Override
				public String getId() {
					return "WRITE_TO_CONSOLE";
				}
			},
			new StaticEntity() {
				@Override
				public void action(final Map<String, Object> parameters, final DatabaseActionContext context) {
					logger.info("TEST ACTION EXECUTED");
				}

				@Override
				public String getId() {
					return "TEST_ACTION";
				}
			});
	}
}
