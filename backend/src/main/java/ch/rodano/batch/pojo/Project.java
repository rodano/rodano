package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {

	private UUID projectId;
	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;
	private String url;
	private String email;
	private String color;
	private String introductionText;
	private boolean smtpTLS;
	private boolean passwordStrong;
	private Integer passwordLength;
	private Integer passwordValidityDuration;
	private boolean passwordUniqueness;
	private boolean eproEnabled;
	private String eproProfileId;
	private String client;
	private String clientEmail;
	private String protocolNo;
	private String versionNumber;
	private String versionDate;
	private Integer configVersion;
	private Long configDate;
	private String configUser;
	private List<String> configChangelogs;
	private List<String> languageIds;
	private String defaultLanguageId;
	private List<String> ruleTags;

	public UUID getProjectId() {
		return projectId;
	}

	public void setProjectId(final UUID projectId) {
		this.projectId = projectId;
	}

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

	public boolean isSmtpTLS() {
		return smtpTLS;
	}

	public void setSmtpTLS(final boolean smtpTLS) {
		this.smtpTLS = smtpTLS;
	}

	public boolean isPasswordStrong() {
		return passwordStrong;
	}

	public void setPasswordStrong(final boolean passwordStrong) {
		this.passwordStrong = passwordStrong;
	}

	public Integer getPasswordLength() {
		return passwordLength;
	}

	public void setPasswordLength(final Integer passwordLength) {
		this.passwordLength = passwordLength;
	}

	public Integer getPasswordValidityDuration() {
		return passwordValidityDuration;
	}

	public void setPasswordValidityDuration(final Integer passwordValidityDuration) {
		this.passwordValidityDuration = passwordValidityDuration;
	}

	public boolean isPasswordUniqueness() {
		return passwordUniqueness;
	}

	public void setPasswordUniqueness(final boolean passwordUniqueness) {
		this.passwordUniqueness = passwordUniqueness;
	}

	public boolean isEproEnabled() {
		return eproEnabled;
	}

	public void setEproEnabled(final boolean eproEnabled) {
		this.eproEnabled = eproEnabled;
	}

	public String getEproProfileId() {
		return eproProfileId;
	}

	public void setEproProfileId(final String eproProfileId) {
		this.eproProfileId = eproProfileId;
	}

	public String getClient() {
		return client;
	}

	public void setClient(final String client) {
		this.client = client;
	}

	public String getClientEmail() {
		return clientEmail;
	}

	public void setClientEmail(final String clientEmail) {
		this.clientEmail = clientEmail;
	}

	public String getProtocolNo() {
		return protocolNo;
	}

	public void setProtocolNo(final String protocolNo) {
		this.protocolNo = protocolNo;
	}

	public String getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(final String versionNumber) {
		this.versionNumber = versionNumber;
	}

	public String getVersionDate() {
		return versionDate;
	}

	public void setVersionDate(final String versionDate) {
		this.versionDate = versionDate;
	}

	public Integer getConfigVersion() {
		return configVersion;
	}

	public void setConfigVersion(final Integer configVersion) {
		this.configVersion = configVersion;
	}

	public Long getConfigDate() {
		return configDate;
	}

	public void setConfigDate(final Long configDate) {
		this.configDate = configDate;
	}

	public String getConfigUser() {
		return configUser;
	}

	public void setConfigUser(final String configUser) {
		this.configUser = configUser;
	}

	public List<String> getConfigChangelogs() {
		return configChangelogs;
	}

	public void setConfigChangelogs(final List<String> configChangelogs) {
		this.configChangelogs = configChangelogs;
	}

	public List<String> getLanguageIds() {
		return languageIds;
	}

	public void setLanguageIds(final List<String> languageIds) {
		this.languageIds = languageIds;
	}

	public String getDefaultLanguageId() {
		return defaultLanguageId;
	}

	public void setDefaultLanguageId(final String defaultLanguageId) {
		this.defaultLanguageId = defaultLanguageId;
	}

	public List<String> getRuleTags() {
		return ruleTags;
	}

	public void setRuleTags(final List<String> ruleTags) {
		this.ruleTags = ruleTags;
	}

	@Override
	public String toString() {
		return "Project{" +
			"projectId=" + projectId +
			", id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", url='" + url + '\'' +
			", email='" + email + '\'' +
			", color='" + color + '\'' +
			", introductionText='" + introductionText + '\'' +
			", smtpTLS=" + smtpTLS +
			", passwordStrong=" + passwordStrong +
			", passwordLength=" + passwordLength +
			", passwordValidityDuration=" + passwordValidityDuration +
			", passwordUniqueness=" + passwordUniqueness +
			", eproEnabled=" + eproEnabled +
			", eproProfileId='" + eproProfileId + '\'' +
			", client='" + client + '\'' +
			", clientEmail='" + clientEmail + '\'' +
			", protocolNo='" + protocolNo + '\'' +
			", versionNumber='" + versionNumber + '\'' +
			", versionDate='" + versionDate + '\'' +
			", configVersion=" + configVersion +
			", configDate=" + configDate +
			", configUser='" + configUser + '\'' +
			", configChangelogs=" + configChangelogs +
			", languageIds=" + languageIds +
			", defaultLanguageId='" + defaultLanguageId + '\'' +
			", ruleTags=" + ruleTags +
			'}';
	}
}
