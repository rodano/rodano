package ch.rodano.configuration.jackson;

import java.lang.reflect.Method;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;

public class DeterministicUuidDeserializer extends JsonDeserializer<UUID> implements ContextualDeserializer {

	private final String namespace;
	private final String sourceProperty;

	public DeterministicUuidDeserializer() {
		this(null, "id");
	}

	private DeterministicUuidDeserializer(final String namespace, final String sourceProperty) {
		this.namespace = namespace;
		this.sourceProperty = sourceProperty;
	}

	@Override
	public UUID deserialize(final JsonParser p, final DeserializationContext ctxt) throws JsonMappingException {
		try {
			final String raw = p.getValueAsString();
			if(raw != null && !raw.isBlank()) {
				try {
					return UUID.fromString(raw.trim());
				}
				catch(IllegalArgumentException e) {
					ctxt.reportInputMismatch(UUID.class, "Invalid UUID format for value '%s'. Falling back to deterministic generation", raw);
				}
			}

			final Object parent = ctxt.getParser().getParsingContext().getCurrentValue();
			if(parent == null || namespace == null || namespace.isBlank()) {
				return null;
			}

			final String sourceValue = readStringProperty(parent, sourceProperty);
			if(sourceValue == null || sourceValue.isBlank()) {
				ctxt.reportInputMismatch(UUID.class, "Cannot compute deterministic UUID: missing source property '%s' in %s", sourceProperty, parent.getClass().getSimpleName());
			}
			return deterministic(null, namespace, sourceValue);
		}
		catch(Exception e) {
			ctxt.reportInputMismatch(e.getClass(), "Error deserializing deterministoc UUID: %s", e.getMessage());
			return null;
		}
	}

	@Override
	public JsonDeserializer<?> createContextual(final DeserializationContext ctxt, final BeanProperty property) {

		if(property == null) {
			return this;
		}

		final var ann = property.getAnnotation(DeterministicNamespace.class);
		final var namespace = ann != null ? ann.value() : null;
		final var sourceProperty = ann != null ? ann.sourceProperty() : "id";
		return new DeterministicUuidDeserializer(namespace, sourceProperty);

	}

	private static String readStringProperty(final Object bean, final String name) {
		if(bean == null || name == null || name.isBlank()) {
			return null;
		}

		try {
			final String getter = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
			final Method m = bean.getClass().getMethod(getter);
			final Object v = m.invoke(bean);
			return v != null ? v.toString() : null;
		}
		catch(Exception e) {
			try {
				final var f = bean.getClass().getDeclaredField(name);
				f.setAccessible(true);
				final Object v = f.get(bean);
				return v != null ? v.toString() : null;
			}
			catch(Exception ex) {
				return null;
			}
		}
	}
}
