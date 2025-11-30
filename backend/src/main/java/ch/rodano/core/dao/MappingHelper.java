package ch.rodano.core.dao;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MappingHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(MappingHelper.class);

	private final ObjectMapper objectMapper;

	public MappingHelper(final ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public SortedMap<String, String> parseJsonToMap(final String json) {
		if(json == null || json.isBlank()) {
			return new TreeMap<>();
		}
		try {
			final Map<String, String> map = objectMapper.readValue(json, new TypeReference<>() {
			});
			return new TreeMap<>(map);
		}
		catch(JsonProcessingException e) {
			LOGGER.warn("Failed to parse JSON, returning empty map. Input: {}", json.length() > 100 ? json.substring(0, 100) + "..." : json);
			return new TreeMap<>();
		}
	}

	public <T> T parseJson(final String json, final Class<T> clazz) {
		if(json == null || json.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(json, clazz);
		}
		catch(JsonProcessingException e) {
			return null;
		}
	}

	public <T> T parseJson(final String json, final TypeReference<T> typeReference) {
		if(json == null || json.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(json, typeReference);
		}
		catch(JsonProcessingException e) {
			return null;
		}
	}

	public <T extends Enum<T>> T parseEnum(final Class<T> enumClass, final String value, final String fieldName) {
		if(value == null || value.isBlank()) {
			return null;
		}

		try {
			return Enum.valueOf(enumClass, value);
		}
		catch(IllegalArgumentException e) {
			LOGGER.warn("Invalid {} value '{}' for field {}", enumClass.getSimpleName(), value, fieldName);
			return null;
		}
	}
}
