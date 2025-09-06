package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrivacyPolicy {

	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;
	private Map<String, String> content;

	private List<String> profileIds;

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

	public Map<String, String> getContent() {
		return content;
	}

	public void setContent(final Map<String, String> content) {
		this.content = content;
	}

	public List<String> getProfileIds() {
		return profileIds;
	}

	public void setProfileIds(final List<String> profileIds) {
		this.profileIds = profileIds;
	}

	@Override
	public String toString() {
		return "PrivacyPolicy{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", content=" + content +
			", profileIds=" + profileIds +
			'}';
	}
}
