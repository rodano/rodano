package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentStep {

	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;

	private Boolean repeatable;
	private String workflowable;

	private List<PaymentStepDistribution> distributions;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final Map<String, String> shortname) {
		this.shortname = shortname;
	}

	public Map<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final Map<String, String> longname) {
		this.longname = longname;
	}

	public Map<String, String> getDescription() {
		return description;
	}

	public void setDescription(final Map<String, String> description) {
		this.description = description;
	}

	public Boolean getRepeatable() {
		return repeatable;
	}

	public void setRepeatable(final Boolean repeatable) {
		this.repeatable = repeatable;
	}

	public String getWorkflowable() {
		return workflowable;
	}

	public void setWorkflowable(final String workflowable) {
		this.workflowable = workflowable;
	}

	public List<PaymentStepDistribution> getDistributions() {
		return distributions;
	}

	public void setDistributions(final List<PaymentStepDistribution> distributions) {
		this.distributions = distributions;
	}

	@Override
	public String toString() {
		return "PaymentStep{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", repeatable=" + repeatable +
			", workflowable='" + workflowable + '\'' +
			", distributions=" + distributions +
			'}';
	}
}
