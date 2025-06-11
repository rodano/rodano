package ch.rodano.configuration.model.common;

import java.io.Serializable;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Node extends Serializable {
	final Logger LOGGER = LoggerFactory.getLogger(Node.class);

	@JsonIgnore
	default boolean isStatic() {
		return false;
	}

	@JsonIgnore
	Entity getEntity();

	@JsonIgnore
	Collection<Node> getChildrenWithEntity(Entity entity);

	default String getClassName() {
		return getClass().getSimpleName();
	}

	@JsonAnySetter
	default void setAnySetter(final String key, final Object value) {
		if(!"className".equals(key) && !"entity".equals(key)) {
			LOGGER.error("Unknown property {} (value {}) in class {}", key, value, getClassName());
		}
	}
}
