package ch.rodano.api.config;

import java.time.ZonedDateTime;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.api.workflow.WorkflowDTO;

public class StudyDTO extends PublicStudyDTO {
	@NotBlank
	@Email
	String email;
	@NotBlank
	@Email
	String clientEmail;

	@NotBlank
	String welcomeText;

	@NotBlank
	String configUser;
	@NotNull
	ZonedDateTime configDate;

	@NotNull
	List<ScopeModelDTO> scopeModels;
	@NotNull
	List<DatasetModelDTO> datasetModels;
	@NotNull
	List<FormModelDTO> formModels;
	@NotNull
	List<WorkflowDTO> workflows;

	@NotNull
	List<ProfileDTO> profiles;
	@NotNull
	List<MenuDTO> menus;

	@NotBlank
	String rootScopeModelId;
	@NotBlank
	String leafScopeId;
	@NotNull
	List<String> leafScopeModelIds;

	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public String getClientEmail() {
		return clientEmail;
	}

	public void setClientEmail(final String clientEmail) {
		this.clientEmail = clientEmail;
	}

	public String getWelcomeText() {
		return welcomeText;
	}

	public void setWelcomeText(final String welcomeText) {
		this.welcomeText = welcomeText;
	}

	public String getConfigUser() {
		return configUser;
	}

	public void setConfigUser(final String configUser) {
		this.configUser = configUser;
	}

	public ZonedDateTime getConfigDate() {
		return configDate;
	}

	public void setConfigDate(final ZonedDateTime configDate) {
		this.configDate = configDate;
	}

	public List<ScopeModelDTO> getScopeModels() {
		return scopeModels;
	}

	public void setScopeModels(final List<ScopeModelDTO> scopeModels) {
		this.scopeModels = scopeModels;
	}

	public List<DatasetModelDTO> getDatasetModels() {
		return datasetModels;
	}

	public void setDatasetModels(final List<DatasetModelDTO> datasetModels) {
		this.datasetModels = datasetModels;
	}

	public List<FormModelDTO> getFormModels() {
		return formModels;
	}

	public void setFormModels(final List<FormModelDTO> formModels) {
		this.formModels = formModels;
	}

	public List<WorkflowDTO> getWorkflows() {
		return workflows;
	}

	public void setWorkflows(final List<WorkflowDTO> workflows) {
		this.workflows = workflows;
	}

	public List<ProfileDTO> getProfiles() {
		return profiles;
	}

	public void setProfiles(final List<ProfileDTO> profiles) {
		this.profiles = profiles;
	}

	public List<MenuDTO> getMenus() {
		return menus;
	}

	public void setMenus(final List<MenuDTO> menus) {
		this.menus = menus;
	}

	public String getRootScopeModelId() {
		return rootScopeModelId;
	}

	public void setRootScopeModelId(final String rootScopeModelId) {
		this.rootScopeModelId = rootScopeModelId;
	}

	public String getLeafScopeId() {
		return leafScopeId;
	}

	public void setLeafScopeId(final String leafScopeId) {
		this.leafScopeId = leafScopeId;
	}

	public List<String> getLeafScopeModelIds() {
		return leafScopeModelIds;
	}

	public void setLeafScopeModelIds(final List<String> leafScopeModelIds) {
		this.leafScopeModelIds = leafScopeModelIds;
	}

}
