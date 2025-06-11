package ch.rodano.api.config;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.core.configuration.core.Environment;

public class PublicStudyDTO {
	@NotBlank
	String id;
	@NotNull
	SortedMap<String, String> shortname;
	@NotBlank
	String color;
	String logo;

	@NotBlank
	String url;
	@NotNull
	MenuDTO homePage;

	@NotNull
	boolean eproEnabled;

	String introductionText;
	String copyright;

	@NotNull
	List<LanguageDTO> activatedLanguages = new ArrayList<>();
	@NotNull
	LanguageDTO defaultLanguage;

	@NotNull
	Environment environment;
	@NotNull
	ScopeModelDTO leafScopeModel;

	@Nullable
	 ProfileDTO eproProfile;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public SortedMap<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	public String getColor() {
		return color;
	}

	public void setColor(final String color) {
		this.color = color;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(final String logo) {
		this.logo = logo;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public MenuDTO getHomePage() {
		return homePage;
	}

	public void setHomePage(final MenuDTO homePage) {
		this.homePage = homePage;
	}

	public boolean isEproEnabled() {
		return eproEnabled;
	}

	public void setEproEnabled(final boolean eproEnabled) {
		this.eproEnabled = eproEnabled;
	}

	public String getIntroductionText() {
		return introductionText;
	}

	public void setIntroductionText(final String introductionText) {
		this.introductionText = introductionText;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(final String copyright) {
		this.copyright = copyright;
	}

	public List<LanguageDTO> getActivatedLanguages() {
		return activatedLanguages;
	}

	public void setActivatedLanguages(final List<LanguageDTO> activatedLanguages) {
		this.activatedLanguages = activatedLanguages;
	}

	public LanguageDTO getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(final LanguageDTO defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(final Environment environment) {
		this.environment = environment;
	}

	public ScopeModelDTO getLeafScopeModel() {
		return leafScopeModel;
	}

	public void setLeafScopeModel(final ScopeModelDTO leafScopeModel) {
		this.leafScopeModel = leafScopeModel;
	}

	@Nullable
	public ProfileDTO getEproProfile() {
		return eproProfile;
	}

	public void setEproProfile(@Nullable final ProfileDTO eproProfile) {
		this.eproProfile = eproProfile;
	}
}
