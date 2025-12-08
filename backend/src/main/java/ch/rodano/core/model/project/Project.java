package ch.rodano.core.model.project;

import java.util.Map;
import java.util.UUID;

public class Project {

	private UUID projectId;
	private String code;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;
	private String url;
	private String email;
	private String color;
	private String introductionText;

	public UUID getProjectId() {
		return projectId;
	}

	public void setProjectId(final UUID projectId) {
		this.projectId = projectId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
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

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public String getColor() {
		return color;
	}

	public void setColor(final String color) {
		this.color = color;
	}

	public String getIntroductionText() {
		return introductionText;
	}

	public void setIntroductionText(final String introductionText) {
		this.introductionText = introductionText;
	}
}
