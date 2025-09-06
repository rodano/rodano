package ch.rodano.batch.pojo;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowRights {

	private Boolean right;
	private Map<String, ProfileRight> childRights;

	public Boolean getRight() {
		return right;
	}

	public void setRight(final Boolean right) {
		this.right = right;
	}

	public Map<String, ProfileRight> getChildRights() {
		return childRights;
	}

	public void setChildRights(final Map<String, ProfileRight> childRights) {
		this.childRights = childRights;
	}

	@Override
	public String toString() {
		return "WorkflowRights{" +
			"right=" + right +
			", childRights=" + childRights +
			'}';
	}
}
