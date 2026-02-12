package ch.rodano.configuration.model.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class RuleEvaluation {
	private final Logger logger = LoggerFactory.getLogger(RuleEvaluation.class);

	private String conditionId;
	private Operator operator;
	private String value;

	public final String getConditionId() {
		return conditionId;
	}

	public final void setConditionId(final String conditionId) {
		this.conditionId = conditionId;
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

	@JsonAnySetter
	public final void setAnySetter(final String key, final Object value) {
		if(!"entity".equals(key)) {
			logger.error("Unknown property {} (value {}) in class {}", key, value, getClass().getSimpleName());
		}
	}

}
