package ch.rodano.core.helpers.configuration;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ch.rodano.core.model.scope.ScopeData;

public class StringScopeDataConverter implements Converter<String, ScopeData> {
	private static final long serialVersionUID = -1001806323843788265L;

	private static final ObjectMapper MAPPER = new ObjectMapper();
	static {
		MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	}

	@Override
	public ScopeData from(final String string) {
		if(StringUtils.isNotBlank(string)) {
			try {
				return MAPPER.readValue(string, ScopeData.class);
			}
			catch(@SuppressWarnings("unused") final IOException e) {
				//let the empty scope data being generated
			}
		}
		return new ScopeData();
	}

	@Override
	public String to(final ScopeData data) {
		if(data != null) {
			try {
				return MAPPER.writeValueAsString(data);
			}
			catch(@SuppressWarnings("unused") final IOException e) {
				//let the empty scope data being generated
			}
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
