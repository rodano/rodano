package ch.rodano.batch.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileRight {

	private Boolean system;
	private List<String> profileIds;

	public Boolean getSystem() {
		return system;
	}

	public void setSystem(final Boolean system) {
		this.system = system;
	}

	public List<String> getProfileIds() {
		return profileIds;
	}

	public void setProfileIds(final List<String> profileIds) {
		this.profileIds = profileIds;
	}

	@Override
	public String toString() {
		return "ProfileRight{" +
			"system=" + system +
			", profileIds=" + profileIds +
			'}';
	}
}
