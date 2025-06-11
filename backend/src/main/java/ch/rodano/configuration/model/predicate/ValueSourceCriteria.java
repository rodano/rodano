package ch.rodano.configuration.model.predicate;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ch.rodano.configuration.model.rules.Operator;

@JsonInclude(Include.NON_NULL)
public class ValueSourceCriteria extends ValueSource implements Serializable {
	private static final long serialVersionUID = -3083751665455750851L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ValueSourceCriteria.class);

	protected Operator operator;
	protected String value;
	private Set<String> values;

	public ValueSourceCriteria() {
		values = new TreeSet<>();
	}

	public final Operator getOperator() {
		return operator;
	}

	public final void setOperator(final Operator operator) {
		this.operator = operator;
	}

	public final String getValue() {
		return value;
	}

	public final void setValue(final String value) {
		this.value = value;
	}

	public final Set<String> getValues() {
		return values;
	}

	public final void setValues(final Set<String> values) {
		this.values = values;
	}

	@Override
	public String toString() {
		final var label = new StringBuilder(super.toString());
		label.append(" / operator : ");
		label.append(operator.toString());
		label.append(" / value : ");
		label.append(value);
		return label.toString();
	}

	@Override
	public String getEntity() {
		return "VALUE_SOURCE_CRITERIA";
	}

	@Override
	@JsonAnySetter
	public void setAnySetter(final String key, final Object value) {
		if(!"entity".equals(key)) {
			final var message = new StringBuilder("ValueSourceCriteria - Unknown property : ");
			message.append(key);
			message.append(" - ");
			message.append(value);
			LOGGER.error(message.toString());
		}
	}
}
