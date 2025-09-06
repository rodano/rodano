package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuLayoutSection {

	private String id;
	private Map<String, String> labels;

	private String requiredFeature;
	private ScopeCriterionRight requiredRight;
	private List<MenuLayoutSectionWidget> widgets;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(final Map<String, String> labels) {
		this.labels = labels;
	}

	public String getRequiredFeature() {
		return requiredFeature;
	}

	public void setRequiredFeature(final String requiredFeature) {
		this.requiredFeature = requiredFeature;
	}

	public ScopeCriterionRight getRequiredRight() {
		return requiredRight;
	}

	public void setRequiredRight(final ScopeCriterionRight requiredRight) {
		this.requiredRight = requiredRight;
	}

	public List<MenuLayoutSectionWidget> getWidgets() {
		return widgets;
	}

	public void setWidgets(final List<MenuLayoutSectionWidget> widgets) {
		this.widgets = widgets;
	}

	@Override
	public String toString() {
		return "MenuLayoutSection{" +
			"id='" + id + '\'' +
			", labels=" + labels +
			", requiredFeature='" + requiredFeature + '\'' +
			", requiredRight=" + requiredRight +
			", widgets=" + widgets +
			'}';
	}
}
