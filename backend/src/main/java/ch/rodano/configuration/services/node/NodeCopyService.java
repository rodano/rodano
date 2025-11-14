package ch.rodano.configuration.services.node;

import tools.jackson.databind.json.JsonMapper;

import ch.rodano.configuration.model.common.Node;

public class NodeCopyService {

	@SuppressWarnings("unchecked")
	public static <T extends Node> T copy(final T node, final String newId) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		final var mapper = new JsonMapper();

		//serialize string
		final var src = mapper.writeValueAsString(node);

		//change property directly in serialized string
		//Pattern regex = Pattern.compile(String.format("\"id\" ?: ?\"%s\"", node.getId()));
		//String dest = regex.matcher(src).replaceFirst(String.format("\"id\":\"%s\"", newId));
		//deserialize string
		final var newNode = (T) mapper.readValue(src, node.getClass());

		//change property using reflexion
		final var field = newNode.getClass().getDeclaredField("id");
		field.setAccessible(true);
		field.set(newNode, newId);
		field.setAccessible(false);

		return newNode;
	}
}
