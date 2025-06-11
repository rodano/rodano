package ch.rodano.core.plugins.rules.dataset;

import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.services.plugin.entity.EntityType;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatasetActionEntityDefiner extends AbstractDatasetEntityDefiner {

	@Override
	public EntityType getRelatedEntityType() {
		return EntityType.ACTION;
	}

	@Override
	public List<IdentifiableEntity> getRegisteredBeans() {
		return List.of();
	}
}
