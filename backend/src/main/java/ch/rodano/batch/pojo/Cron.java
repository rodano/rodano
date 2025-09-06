package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Cron {

	private String id;
	private Map<String, String> description;

	private Integer interval;
	private String intervalUnit;

	private List<Rule> rules;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getDescription() {
		return description;
	}

	public void setDescription(final Map<String, String> description) {
		this.description = description;
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(final Integer interval) {
		this.interval = interval;
	}

	public String getIntervalUnit() {
		return intervalUnit;
	}

	public void setIntervalUnit(final String intervalUnit) {
		this.intervalUnit = intervalUnit;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(final List<Rule> rules) {
		this.rules = rules;
	}

	@Override
	public String toString() {
		return "Cron{" +
			"id='" + id + '\'' +
			", description=" + description +
			", interval=" + interval +
			", intervalUnit='" + intervalUnit + '\'' +
			", rules=" + rules +
			'}';
	}
}
