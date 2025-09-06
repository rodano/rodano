package ch.rodano.batch.pojo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EventAction {

	private final Map<String, List<Rule>> eventActions = new LinkedHashMap<>();

	public Map<String, List<Rule>> getEventActions() {
		return eventActions;
	}

	@JsonAnySetter
	public void put(final String actionCode, final List<Rule> rules) {
		eventActions.put(actionCode, rules);
	}

	public boolean isEmpty() {
		return eventActions.isEmpty();
	}

	@Override
	public String toString() {
		return "EventAction{" +
			"eventActions=" + eventActions +
			'}';
	}
}
