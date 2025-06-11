package ch.rodano.core.helpers.configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Converter;

/**
 * do not try to type the set (Set<String>) for now
 * jOOQ does not work with generics -->
 *
 */
@SuppressWarnings("rawtypes")
public class StringSetConverter implements Converter<String, Set> {
	private static final long serialVersionUID = 2883930587007849214L;

	@Override
	public Set<String> from(final String string) {
		if(string == null) {
			return null;
		}
		if(StringUtils.isBlank(string)) {
			return Collections.emptySet();
		}
		return new LinkedHashSet<>(Arrays.asList(string.split(",")));
	}

	@SuppressWarnings("unchecked")
	@Override
	public String to(final Set set) {
		if(set == null) {
			return null;
		}
		return (String) set.stream().collect(Collectors.joining(","));
	}

	@Override
	public Class<String> fromType() {
		return String.class;
	}

	@Override
	public Class<Set> toType() {
		return Set.class;
	}
}
