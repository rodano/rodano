package ch.rodano.core.services.dao.configurator;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JsonMapperService {

	private final ObjectMapper objectMapper;

	public JsonMapperService(final ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String toJson(final Object obj) {
		if(obj == null) {
			return null;
		}
		try {
			return objectMapper.writeValueAsString(obj);
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to serialize to JSON", e);
		}
	}

	public <T> T fromJson(final String json, final TypeReference<T> typeRef) {
		if(json == null || json.isEmpty()) {
			return null;
		}
		try {
			return objectMapper.readValue(json, typeRef);
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to deserialize JSON", e);
		}
	}
}
