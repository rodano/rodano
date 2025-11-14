package ch.rodano.core.helpers.configuration;

import java.io.Serial;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Converter;

import tools.jackson.databind.ObjectMapper;

import ch.rodano.core.model.scope.ScopeData;

public class StringScopeDataConverter implements Converter<String, ScopeData> {
	@Serial
	private static final long serialVersionUID = -1001806323843788265L;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Override
	public ScopeData from(final String string) {
		if(StringUtils.isNotBlank(string)) {
			return MAPPER.readValue(string, ScopeData.class);
		}
		return new ScopeData();
	}

	@Override
	public String to(final ScopeData data) {
		if(data != null) {
			return MAPPER.writeValueAsString(data);
		}
		return null;
	}

	@Override
	public Class<String> fromType() {
		return String.class;
	}

	@Override
	public Class<ScopeData> toType() {
		return ScopeData.class;
	}
}
