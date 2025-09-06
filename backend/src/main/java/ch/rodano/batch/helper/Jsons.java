package ch.rodano.batch.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class Jsons {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private Jsons() {

	}

	public static String write(final Object obj) {
		if(obj == null) {
			return null;
		}
		try {
			return MAPPER.writeValueAsString(obj);
		}
		catch(JsonProcessingException e) {
			throw new RuntimeException("Failed to serialize object to JSON", e);
		}
	}
}
