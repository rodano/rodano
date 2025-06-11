package ch.rodano.configuration.services.node;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ch.rodano.configuration.model.common.Node;

public class NodeCopyService {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	static {
		MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		MAPPER.configure(SerializationFeature.INDENT_OUTPUT, false);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> T copy(final T node, final String newId) throws IOException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		//serialize string
		final var src = MAPPER.writeValueAsString(node);

		//change property directly in serialized string
		//Pattern regex = Pattern.compile(String.format("\"id\" ?: ?\"%s\"", node.getId()));
		//String dest = regex.matcher(src).replaceFirst(String.format("\"id\":\"%s\"", newId));
		//deserialize string
		final var newNode = (T) MAPPER.readValue(src, node.getClass());

		//change property using reflexion
		final var field = newNode.getClass().getDeclaredField("id");
		field.setAccessible(true);
		field.set(newNode, newId);
		field.setAccessible(false);

		return newNode;
	}
}
