package ch.rodano.core.model.rules.data;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.configuration.model.rules.RuleCondition;
import ch.rodano.configuration.model.rules.RuleConstraint;
import ch.rodano.core.model.common.TimestampableObject;
import ch.rodano.core.model.rules.Evaluable;

public class DataEvaluation implements Serializable {
	private static final long serialVersionUID = -6415893882665841504L;

	private final DataState initialState;
	private final RuleConstraint constraint;

	//result of the evaluation
	Boolean valid;
	List<Evaluable> dependencies = new ArrayList<Evaluable>();

	private final SortedMap<String, DataState> states = new TreeMap<>();

	public DataEvaluation(final DataState state, final RuleConstraint constraint) {
		this.initialState = state;
		this.constraint = constraint;

		//pre-fill states map with initial state
		Arrays.stream(RulableEntity.values())
			.filter(entity -> !state.getEvaluables(entity).isEmpty())
			.forEach(entity -> states.put(entity.name(), state.withOnly(entity)));
	}

	public DataState getInitialState() {
		return initialState;
	}

	public RuleConstraint getConstraint() {
		return constraint;
	}

	public final SortedMap<String, DataState> getStates() {
		return states;
	}

	public List<RuleCondition> getEvaluationsConditions() {
		if(constraint != null) {
			return constraint.getConditions().entrySet().stream()
				.flatMap(entry -> entry.getValue().getConditions().stream())
				.flatMap(ruleCondition -> ruleCondition.getEvaluations().stream())
				.toList();
		}
		return Collections.emptyList();
	}

	public List<RuleCondition> getDependenciesConditions() {
		if(constraint != null) {
			return constraint.getConditions().entrySet().stream()
				.flatMap(entry -> entry.getValue().getConditions().stream())
				.flatMap(ruleCondition -> ruleCondition.getDependencies().stream())
				.toList();
		}
		return Collections.emptyList();
	}

	private void checkEvaluated() {
		if(valid == null) {
			throw new UnsupportedOperationException("Evaluation has not been done yet");
		}
	}

	public boolean isValid() {
		checkEvaluated();
		return valid;
	}

	public List<Evaluable> getDependencies() {
		checkEvaluated();
		return dependencies;
	}

	public boolean getDependenciesHaveChangedSince(final ZonedDateTime date) {
		checkEvaluated();
		return dependencies.stream().anyMatch(d -> hasChangedSince(d, date));
	}

	private boolean hasChangedSince(final TimestampableObject object, final ZonedDateTime date) {
		return object.getLastUpdateTime().equals(date) || object.getLastUpdateTime().isAfter(date);
	}
}
