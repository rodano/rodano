package ch.rodano.configuration.model.study;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.exceptions.NoNodeException;
import ch.rodano.configuration.exceptions.NoRespectForConfigurationException;
import ch.rodano.configuration.model.changelog.Changelog;
import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.common.SuperDisplayable;
import ch.rodano.configuration.model.cron.Cron;
import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.configuration.model.export.ExportFormat;
import ch.rodano.configuration.model.export.SelectionNode;
import ch.rodano.configuration.model.feature.Feature;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.form.FormModel;
import ch.rodano.configuration.model.language.Language;
import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.model.menu.Menu;
import ch.rodano.configuration.model.payment.PaymentPlan;
import ch.rodano.configuration.model.policy.PrivacyPolicy;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.reports.Report;
import ch.rodano.configuration.model.reports.WorkflowSummary;
import ch.rodano.configuration.model.reports.WorkflowWidget;
import ch.rodano.configuration.model.resource.ResourceCategory;
import ch.rodano.configuration.model.rules.Rule;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.timelinegraph.TimelineGraph;
import ch.rodano.configuration.model.validator.Validator;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowAction;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public final class Study implements Serializable, SuperDisplayable, Node, Comparable<Study> {
	private static final long serialVersionUID = -5790468283716242383L;

	private static final Logger LOGGER = LoggerFactory.getLogger(Study.class);

	public static final Comparator<Study> DEFAULT_COMPARATOR = Comparator.comparing(Study::getId);

	private static final int PASSWORD_LENGTH = 4;
	private static final String DEFAULT_PASSWORD_LENGTH_MESSAGE = "Password is not strong enough: it must contains at least %d characters";
	private static final SortedMap<String, String> PASSWORD_LENGTH_MESSAGES = new TreeMap<>();
	private static final String DEFAULT_PASSWORD_STRENGTH_MESSAGE = ", one uppercase character, one number and one of the following special characters: ! \\ \" # $ % & ' ( ) * + , - . / : ; < = > ? @ [ ] ^ _ `";
	private static final SortedMap<String, String> PASSWORD_STRENGTH_MESSAGES = new TreeMap<>();

	static {
		PASSWORD_LENGTH_MESSAGES.put(LanguageStatic.en.name(), DEFAULT_PASSWORD_LENGTH_MESSAGE);
		PASSWORD_LENGTH_MESSAGES.put(LanguageStatic.fr.name(), "Le mot de passe n'est pas assez robuste: il doit contenir au moins %d charactères");
		PASSWORD_STRENGTH_MESSAGES.put(LanguageStatic.en.name(), DEFAULT_PASSWORD_STRENGTH_MESSAGE);
		PASSWORD_STRENGTH_MESSAGES.put(LanguageStatic.fr.name(), ", une majuscule, un chiffre et un des caractères parmi la liste suivante : ! \\ \" # $ % & ' ( ) * + , - . / : ; < = > ? @ [ ] ^ _ `");
	}

	private String id;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private String url;

	private String email;
	private String color;
	private String logo;
	private String introductionText;
	private String welcomeText;
	private String loginText;

	//smtp configuration
	private String smtpServer;
	private Integer smtpPort;
	private boolean smtpTLS;
	private String smtpLogin;
	private String smtpPassword;

	//password configuration
	private boolean passwordStrong;
	private int passwordLength;
	private int passwordValidityDuration;
	private boolean passwordUniqueness;

	//epro
	private boolean eproEnabled;
	private String eproProfileId;

	//administrative information
	private String client;
	private String clientEmail;
	private String protocolNo;
	private String versionDate;
	private String versionNumber;

	//filled by kvconfig
	private int configVersion;
	private Date configDate;
	private String configUser;

	private SortedSet<Changelog> configChangelogs;

	private SortedSet<String> ruleTags;

	private SortedSet<Language> languages;

	private SortedSet<Feature> features;
	private SortedSet<Profile> profiles;

	private SortedSet<ScopeModel> scopeModels;
	private SortedSet<DatasetModel> datasetModels;
	private SortedSet<FormModel> formModels;
	private SortedSet<Validator> validators;
	private SortedSet<Workflow> workflows;

	private SortedSet<Menu> menus;

	private SortedSet<ResourceCategory> resourceCategories;
	private SortedSet<PrivacyPolicy> privacyPolicies;
	private SortedSet<PaymentPlan> paymentPlans;

	private SortedSet<Report> reports;
	private SortedSet<WorkflowWidget> workflowWidgets;
	private SortedSet<WorkflowSummary> workflowSummaries;

	private SortedSet<TimelineGraph> timelineGraphs;
	private SortedSet<Chart> charts;

	private Map<WorkflowAction, List<Rule>> eventActions;
	private List<Cron> crons;

	private List<SelectionNode> selections;

	private List<String> languageIds;
	private String defaultLanguageId;

	private String exportVisitsLabel;

	public Study() {
		passwordLength = PASSWORD_LENGTH;
		configChangelogs = new TreeSet<>();
		ruleTags = new TreeSet<>();
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		languages = new TreeSet<>();
		features = new TreeSet<>();
		profiles = new TreeSet<>();
		scopeModels = new TreeSet<>();
		datasetModels = new TreeSet<>();
		formModels = new TreeSet<>();
		validators = new TreeSet<>();
		workflows = new TreeSet<>();
		menus = new TreeSet<>();
		resourceCategories = new TreeSet<>();
		privacyPolicies = new TreeSet<>();
		paymentPlans = new TreeSet<>();
		reports = new TreeSet<>();
		workflowWidgets = new TreeSet<>();
		workflowSummaries = new TreeSet<>();
		timelineGraphs = new TreeSet<>();
		charts = new TreeSet<>();
		eventActions = new TreeMap<>();
		languageIds = new ArrayList<>();
		defaultLanguageId = LanguageStatic.en.name();
	}

	public void init() {
		//add static language and features
		features.addAll(getFeaturesStatic());
		languages.addAll(getLanguagesStatic());
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@Override
	public final String getId() {
		return id;
	}

	@Override
	public final SortedMap<String, String> getLongname() {
		return longname;
	}

	public final void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	@Override
	public final SortedMap<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	@Override
	public final SortedMap<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	@JsonIgnore
	public final String getDefaultLocalizedShortname() {
		return getLocalizedShortname(getDefaultLanguageId());
	}

	@JsonIgnore
	public final String getDefaultLocalizedLongname() {
		return getLocalizedLongname(getDefaultLanguageId());
	}

	public final String getProtocolNo() {
		return protocolNo;
	}

	public final void setProtocolNo(final String protocolNo) {
		this.protocolNo = protocolNo;
	}

	public final String getSmtpServer() {
		return smtpServer;
	}

	public final void setSmtpServer(final String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public final Integer getSmtpPort() {
		return smtpPort;
	}

	public final void setSmtpPort(final Integer smtpPort) {
		this.smtpPort = smtpPort;
	}

	public final boolean getSmtpTLS() {
		return smtpTLS;
	}

	public void setSmtpTLS(final boolean smtpTLS) {
		this.smtpTLS = smtpTLS;
	}

	public final String getSmtpLogin() {
		return smtpLogin;
	}

	public final void setSmtpLogin(final String smtpLogin) {
		this.smtpLogin = smtpLogin;
	}

	public final String getSmtpPassword() {
		return smtpPassword;
	}

	public final void setSmtpPassword(final String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	public final String getEmail() {
		return email;
	}

	public final void setEmail(final String email) {
		this.email = email;
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

	public final boolean isPasswordStrong() {
		return passwordStrong;
	}

	public final void setPasswordStrong(final boolean passwordStrong) {
		this.passwordStrong = passwordStrong;
	}

	public final int getPasswordLength() {
		return passwordLength;
	}

	public final void setPasswordLength(final int passwordLength) {
		this.passwordLength = Math.max(passwordLength, 4);
	}

	public final int getPasswordValidityDuration() {
		return passwordValidityDuration;
	}

	public final void setPasswordValidityDuration(final int passwordValidityDuration) {
		this.passwordValidityDuration = passwordValidityDuration;
	}

	public final boolean getPasswordUniqueness() {
		return passwordUniqueness;
	}

	public final void setPasswordUniqueness(final boolean passwordUniqueness) {
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

	@JsonIgnore
	public Profile getEproProfile() {
		return getProfile(eproProfileId);
	}

	public final String getIntroductionText() {
		return introductionText;
	}

	public final void setIntroductionText(final String introductionText) {
		this.introductionText = introductionText;
	}

	public String getWelcomeText() {
		return welcomeText;
	}

	public void setWelcomeText(final String welcomeText) {
		this.welcomeText = welcomeText;
	}

	public final String getLoginText() {
		return loginText;
	}

	public final void setLoginText(final String loginText) {
		this.loginText = loginText;
	}

	public final String getUrl() {
		return url;
	}

	public final void setUrl(final String url) {
		this.url = url;
	}

	public String getClientEmail() {
		return clientEmail;
	}

	public void setClientEmail(final String clientEmail) {
		this.clientEmail = clientEmail;
	}

	public String getClient() {
		return client;
	}

	public void setClient(final String client) {
		this.client = client;
	}

	public String getVersionDate() {
		return versionDate;
	}

	public void setVersionDate(final String versionDate) {
		this.versionDate = versionDate;
	}

	public String getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(final String versionNumber) {
		this.versionNumber = versionNumber;
	}

	public final int getConfigVersion() {
		return configVersion;
	}

	public final void setConfigVersion(final int configVersion) {
		this.configVersion = configVersion;
	}

	public final Date getConfigDate() {
		return configDate;
	}

	public final void setConfigDate(final Date configDate) {
		this.configDate = configDate;
	}

	public final String getConfigUser() {
		return configUser;
	}

	public final void setConfigUser(final String configUser) {
		this.configUser = configUser;
	}

	public final SortedSet<String> getRuleTags() {
		return ruleTags;
	}

	public final void setRuleTags(final SortedSet<String> ruleTags) {
		this.ruleTags = ruleTags;
	}

	public final SortedSet<Changelog> getConfigChangelogs() {
		return configChangelogs;
	}

	public final void setConfigChangelogs(final SortedSet<Changelog> configChangelogs) {
		this.configChangelogs = configChangelogs;
	}

	public String getDefaultLanguageId() {
		return defaultLanguageId;
	}

	public void setDefaultLanguageId(final String defaultLanguageId) {
		this.defaultLanguageId = defaultLanguageId;
	}

	public String getExportVisitsLabel() {
		return exportVisitsLabel;
	}

	public void setExportVisitsLabel(final String exportVisitsLabel) {
		this.exportVisitsLabel = exportVisitsLabel;
	}

	public final List<String> getLanguageIds() {
		return languageIds;
	}

	public final void setLanguageIds(final List<String> languageIds) {
		this.languageIds = languageIds;
	}

	@JsonManagedReference
	public void setScopeModels(final SortedSet<ScopeModel> scopeModels) {
		this.scopeModels = scopeModels;
	}

	@JsonManagedReference
	public SortedSet<ScopeModel> getScopeModels() {
		return scopeModels;
	}

	@JsonIgnore
	public ScopeModel getScopeModel(final String scopeModelId) {
		return scopeModels.stream()
			.filter(s -> s.getId().equalsIgnoreCase(scopeModelId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.SCOPE_MODEL, scopeModelId));
	}

	@JsonManagedReference("study")
	public final void setMenus(final SortedSet<Menu> menus) {
		this.menus = menus;
	}

	@JsonManagedReference("study")
	public final SortedSet<Menu> getMenus() {
		return menus;
	}

	@JsonIgnore
	public Menu getMenu(final String menuId) {
		return menus.stream()
			.filter(m -> m.getId().equalsIgnoreCase(menuId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.MENU, menuId));
	}

	@JsonIgnore
	public Menu getAllMenu(final String menuId) {
		return getAllMenus().stream()
			.filter(m -> m.getId().equalsIgnoreCase(menuId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.MENU, menuId));
	}

	@JsonIgnore
	public List<EventModel> getEventModels() {
		return scopeModels.stream()
			.flatMap(s -> s.getEventModels().stream())
			.toList();
	}

	@JsonManagedReference
	public final void setWorkflows(final SortedSet<Workflow> workflows) {
		this.workflows = workflows;
	}

	@JsonManagedReference
	public SortedSet<Workflow> getWorkflows() {
		return workflows;
	}

	@JsonIgnore
	public Workflow getWorkflow(final String workflowId) {
		return workflows.stream()
			.filter(w -> w.getId().equalsIgnoreCase(workflowId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.WORKFLOW, workflowId));
	}

	@JsonManagedReference
	public final void setWorkflowWidgets(final SortedSet<WorkflowWidget> workflowWidgets) {
		this.workflowWidgets = workflowWidgets;
	}

	@JsonManagedReference
	public final SortedSet<WorkflowWidget> getWorkflowWidgets() {
		return workflowWidgets;
	}

	@JsonIgnore
	public WorkflowWidget getWorkflowWidget(final String workflowWidgetId) {
		return workflowWidgets.stream()
			.filter(w -> w.getId().equalsIgnoreCase(workflowWidgetId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.WORKFLOW_WIDGET, workflowWidgetId));
	}

	@JsonManagedReference
	public final SortedSet<WorkflowSummary> getWorkflowSummaries() {
		return workflowSummaries;
	}

	@JsonManagedReference
	public final void setWorkflowSummaries(final SortedSet<WorkflowSummary> workflowSummaries) {
		this.workflowSummaries = workflowSummaries;
	}

	@JsonIgnore
	public WorkflowSummary getWorkflowSummary(final String workflowSummaryId) {
		return workflowSummaries.stream()
			.filter(w -> w.getId().equalsIgnoreCase(workflowSummaryId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.WORKFLOW_SUMMARY, workflowSummaryId));
	}

	@JsonManagedReference
	public final void setCharts(final SortedSet<Chart> charts) {
		this.charts = charts;
	}

	@JsonManagedReference
	public final SortedSet<Chart> getCharts() {
		return charts;
	}

	@JsonIgnore
	public Chart getChart(final String chartId) {
		return charts.stream()
			.filter(c -> c.getId().equalsIgnoreCase(chartId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.CHART, chartId));
	}

	@JsonManagedReference
	public final void setTimelineGraphs(final SortedSet<TimelineGraph> graphConfigs) {
		timelineGraphs = graphConfigs;
	}

	@JsonManagedReference
	public final SortedSet<TimelineGraph> getTimelineGraphs() {
		return timelineGraphs;
	}

	@JsonIgnore
	public TimelineGraph getTimelineGraph(final String timelineGraphId) {
		return timelineGraphs.stream()
			.filter(t -> t.getId().equalsIgnoreCase(timelineGraphId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.TIMELINE_GRAPH, timelineGraphId));
	}

	@JsonManagedReference
	public final void setReports(final SortedSet<Report> reports) {
		this.reports = reports;
	}

	@JsonManagedReference
	public final SortedSet<Report> getReports() {
		return reports;
	}

	@JsonIgnore
	public Report getReport(final String reportId) {
		return reports.stream()
			.filter(r -> r.getId().equalsIgnoreCase(reportId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.REPORT, reportId));
	}

	@JsonProperty("languages")
	public void setLanguages(final SortedSet<Language> languages) {
		this.languages = languages;
	}

	@JsonIgnore
	public SortedSet<Language> getLanguages() {
		return languages;
	}

	@JsonIgnore
	public Language getLanguage(final String languageId) {
		return languages.stream()
			.filter(l -> l.getId().equalsIgnoreCase(languageId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.LANGUAGE, languageId));
	}

	@JsonProperty("languages")
	public SortedSet<Language> getLanguagesDynamic() {
		return getLanguages().stream().filter(language -> !language.isStatic()).collect(Collectors.toCollection(TreeSet::new));
	}

	@JsonIgnore
	public List<Language> getLanguagesStatic() {
		return Arrays.stream(LanguageStatic.values())
			.map(ls -> {
				final var language = new Language();
				language.setStudy(this);
				language.setId(ls.name());
				language.setName(ls.getShortname().get(LanguageStatic.en.name()));
				language.setStatic(true);
				return language;
			})
			.toList();
	}

	@JsonIgnore
	public Language getLanguageStatic(final LanguageStatic languageStatic) {
		return getLanguage(languageStatic.name());
	}

	@JsonProperty("resourceCategories")
	public void setResourceCategories(final SortedSet<ResourceCategory> resourceCategories) {
		this.resourceCategories = resourceCategories;
	}

	@JsonManagedReference
	public SortedSet<ResourceCategory> getResourceCategories() {
		return resourceCategories;
	}

	@JsonIgnore
	public ResourceCategory getResourceCategory(final String resourceCategoryId) {
		return resourceCategories.stream()
			.filter(r -> r.getId().equalsIgnoreCase(resourceCategoryId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.RESOURCE_CATEGORY, resourceCategoryId));
	}

	@JsonManagedReference
	public final void setPrivacyPolicies(final SortedSet<PrivacyPolicy> privacyPolicies) {
		this.privacyPolicies = privacyPolicies;
	}

	@JsonManagedReference
	public final SortedSet<PrivacyPolicy> getPrivacyPolicies() {
		return privacyPolicies;
	}

	@JsonIgnore
	public PrivacyPolicy getPrivacyPolicy(final String privacyPolicyId) {
		return privacyPolicies.stream()
			.filter(p -> p.getId().equalsIgnoreCase(privacyPolicyId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.PRIVACY_POLICY, privacyPolicyId));
	}

	@JsonManagedReference
	public final void setPaymentPlans(final SortedSet<PaymentPlan> paymentPlans) {
		this.paymentPlans = paymentPlans;
	}

	@JsonManagedReference
	public final SortedSet<PaymentPlan> getPaymentPlans() {
		return paymentPlans;
	}

	@JsonIgnore
	public PaymentPlan getPaymentPlan(final String paymentPlanId) {
		return paymentPlans.stream()
			.filter(p -> p.getId().equalsIgnoreCase(paymentPlanId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.PAYMENT_PLAN, paymentPlanId));
	}

	public final List<Cron> getCrons() {
		return crons;
	}

	public final void setCrons(final List<Cron> crons) {
		this.crons = crons;
	}

	public List<SelectionNode> getSelections() {
		return selections;
	}

	public void setSelections(final List<SelectionNode> selections) {
		this.selections = selections;
	}

	/**
	 *
	 * @return A list containing all ancestors of a scope model and the scope model it-self sorted from the root to the specified scope model
	 */
	@JsonIgnore
	public List<ScopeModel> getLineageOfScopeModels(final ScopeModel baseScopeModel) {
		final List<ScopeModel> lineageScopeModels = new ArrayList<>();
		lineageScopeModels.add(baseScopeModel);
		lineageScopeModels.addAll(baseScopeModel.getDefaultAncestors());
		Collections.reverse(lineageScopeModels);
		return lineageScopeModels;
	}

	/**
	 *
	 * @return A list containing all ancestors of the leaf scope model and the leaf scope model it-self sorted from the root to the leaf
	 */
	@JsonIgnore
	public List<ScopeModel> getLineageOfScopeModels() {
		return getLineageOfScopeModels(getLeafScopeModel());
	}

	@JsonIgnore
	public ScopeModel getRootScopeModel() {
		return scopeModels.stream().filter(s -> s.isRoot()).findFirst().orElseThrow();
	}

	@JsonIgnore
	public List<ScopeModel> getLeafScopeModels() {
		return scopeModels.stream()
			.filter(s -> s.isLeaf())
			.sorted(ScopeModel.COMPARATOR_DEPTH)
			.toList();
	}

	@JsonIgnore
	public ScopeModel getLeafScopeModel() {
		return scopeModels.stream()
			.filter(s -> s.isLeaf())
			.max(ScopeModel.COMPARATOR_DEPTH)
			.orElseThrow();
	}

	@JsonIgnore
	public List<FieldModel> getFieldModels() {
		return datasetModels.stream()
			.flatMap(d -> d.getFieldModels().stream())
			.toList();
	}

	@JsonManagedReference
	public final void setFormModels(final SortedSet<FormModel> formModels) {
		this.formModels = formModels;
	}

	@JsonManagedReference
	public final SortedSet<FormModel> getFormModels() {
		return formModels;
	}

	@JsonIgnore
	public FormModel getFormModel(final String formModelId) {
		return formModels.stream()
			.filter(p -> p.getId().equalsIgnoreCase(formModelId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.FORM_MODEL, formModelId));
	}

	@JsonManagedReference
	public final void setDatasetModels(final SortedSet<DatasetModel> datasetModels) {
		this.datasetModels = datasetModels;
	}

	@JsonManagedReference
	public final SortedSet<DatasetModel> getDatasetModels() {
		return datasetModels;
	}

	@JsonIgnore
	public DatasetModel getDatasetModel(final String datasetModelId) {
		return datasetModels.stream()
			.filter(d -> d.getId().equalsIgnoreCase(datasetModelId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.DATASET_MODEL, datasetModelId));
	}

	@JsonProperty("features")
	public void setFeatures(final SortedSet<Feature> features) {
		this.features = features;
	}

	@JsonIgnore
	public SortedSet<Feature> getFeatures() {
		return features;
	}

	@JsonIgnore
	public Feature getFeature(final String featureId) {
		return features.stream()
			.filter(f -> f.getId().equalsIgnoreCase(featureId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.FEATURE, featureId));
	}

	@JsonProperty("features")
	public SortedSet<Feature> getFeaturesDynamic() {
		return getFeatures().stream()
			.filter(feature -> !feature.isStatic())
			.collect(Collectors.toCollection(TreeSet::new));
	}

	@JsonIgnore
	public List<Feature> getFeaturesStatic() {
		return Arrays.stream(FeatureStatic.values())
			.map(fs -> {
				final var feature = new Feature();
				feature.setStudy(this);
				feature.setId(fs.name());
				feature.setOptional(fs.isOptional());
				feature.setShortname(fs.getShortname());
				feature.setDescription(fs.getDescription());
				feature.setStatic(true);
				return feature;
			})
			.toList();
	}

	@JsonIgnore
	public Optional<Feature> getFeatureStatic(final FeatureStatic featureStatic) {
		return Optional.of(getFeature(featureStatic.name()));
	}

	@JsonManagedReference
	public final void setProfiles(final SortedSet<Profile> profiles) {
		this.profiles = profiles;
	}

	@JsonManagedReference
	public final SortedSet<Profile> getProfiles() {
		return profiles;
	}

	@JsonIgnore
	public Profile getProfile(final String profileId) {
		return profiles.stream()
			.filter(p -> p.getId().equalsIgnoreCase(profileId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.PROFILE, profileId));
	}

	@JsonManagedReference
	public final void setValidators(final SortedSet<Validator> validators) {
		this.validators = validators;
	}

	@JsonManagedReference
	public final SortedSet<Validator> getValidators() {
		return validators;
	}

	@JsonIgnore
	public Validator getValidator(final String validatorId) {
		return validators.stream()
			.filter(v -> v.getId().equalsIgnoreCase(validatorId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.VALIDATOR, validatorId));
	}

	@JsonIgnore
	public Language getDefaultLanguage() {
		return getLanguage(getDefaultLanguageId());
	}

	@JsonIgnore
	public void setDefaultLanguage(final Language language) {
		defaultLanguageId = language.getId();
	}

	@JsonIgnore
	public List<Language> getActivatedLanguages() {
		return getLanguageIds().stream().map(this::getLanguage).toList();
	}

	@JsonIgnore
	public boolean hasPublicPage() {
		return menus.stream().anyMatch(Menu::isPublic);
	}

	@JsonIgnore
	public Menu getPublicHomePage() throws NoRespectForConfigurationException {
		for(final var menu : getMenus()) {
			for(final var submenu : menu.getSubmenus()) {
				if(submenu.isPublic() && submenu.isHomePage()) {
					return submenu;
				}
			}
			//there is no submenu
			if(menu.isPublic() && menu.isHomePage()) {
				return menu;
			}
		}

		throw new NoRespectForConfigurationException("No public home page");
	}

	@JsonIgnore
	public Menu getPrivateHomePage() throws NoRespectForConfigurationException {
		for(final var menu : getMenus()) {
			for(final var submenu : menu.getSubmenus()) {
				if(!submenu.isPublic() && submenu.isHomePage()) {
					return submenu;
				}
			}
			//there is no submenu
			if(!menu.isPublic() && menu.isHomePage()) {
				return menu;
			}
		}
		throw new NoRespectForConfigurationException("No private home page");
	}

	@JsonIgnore
	public SortedSet<Menu> getMenusPublic() {
		return getMenus().stream()
			.filter(menu -> menu.isPublic())
			.collect(Collectors.toCollection(TreeSet::new));
	}

	@JsonIgnore
	public List<FieldModel> getSearchableFieldModels() {
		return getFieldModels().stream()
			.filter(FieldModel::isSearchable)
			.toList();
	}

	@JsonIgnore
	public Set<Menu> getAllMenus() {
		final Set<Menu> allMenus = new HashSet<>();

		getMenus().forEach(menu -> {
			allMenus.add(menu);
			allMenus.addAll(menu.getSubmenus());
		});

		return allMenus;
	}

	public final Map<WorkflowAction, List<Rule>> getEventActions() {
		return eventActions;
	}

	public final void setEventActions(final Map<WorkflowAction, List<Rule>> eventActions) {
		this.eventActions = eventActions;
	}

	@Override
	public final Entity getEntity() {
		return Entity.STUDY;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public final <T> List<T> getNodesFromIds(final Entity entity, final Collection<String> nodeIds) {
		return nodeIds.stream().map(i -> (T) getChild(entity, i)).toList();
	}

	@JsonIgnore
	public final <T extends Node> List<T> getNodesFromIds(final Entity entity, final String... ids) {
		return getNodesFromIds(entity, Arrays.asList(ids));
	}

	@JsonIgnore
	public final Node getChild(final Entity entity, final String nodeId) {
		switch(entity) {
			case DATASET_MODEL:
				return getDatasetModel(nodeId);
			case VALIDATOR:
				return getValidator(nodeId);
			case FORM_MODEL:
				return getFormModel(nodeId);
			case WORKFLOW:
				return getWorkflow(nodeId);
			case PROFILE:
				return getProfile(nodeId);
			case FEATURE:
				return getFeature(nodeId);
			case PAYMENT_PLAN:
				return getPaymentPlan(nodeId);
			case REPORT:
				return getReport(nodeId);
			case PRIVACY_POLICY:
				return getPrivacyPolicy(nodeId);
			case WORKFLOW_WIDGET:
				return getWorkflowWidget(nodeId);
			case RESOURCE_CATEGORY:
				return getResourceCategory(nodeId);
			case LANGUAGE:
				return getLanguage(nodeId);
			case MENU:
				return getMenu(nodeId);
			case SCOPE_MODEL:
				return getScopeModel(nodeId);
			case TIMELINE_GRAPH:
				return getTimelineGraph(nodeId);
			case CHART:
				return getChart(nodeId);
			default:
				throw new UnsupportedOperationException(String.format("No entity %s", entity.getId()));
		}
	}

	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		switch(entity) {
			case DATASET_MODEL:
				return Collections.unmodifiableSet(datasetModels);
			case VALIDATOR:
				return Collections.unmodifiableSet(validators);
			case FORM_MODEL:
				return Collections.unmodifiableSet(formModels);
			case WORKFLOW:
				return Collections.unmodifiableSet(workflows);
			case PROFILE:
				return Collections.unmodifiableSet(profiles);
			case FEATURE:
				return Collections.unmodifiableSet(features);
			case PAYMENT_PLAN:
				return Collections.unmodifiableSet(paymentPlans);
			case REPORT:
				return Collections.unmodifiableSet(reports);
			case PRIVACY_POLICY:
				return Collections.unmodifiableSet(privacyPolicies);
			case WORKFLOW_WIDGET:
				return Collections.unmodifiableSet(workflowWidgets);
			case RESOURCE_CATEGORY:
				return Collections.unmodifiableSet(resourceCategories);
			case LANGUAGE:
				return Collections.unmodifiableSet(languages);
			case MENU:
				return Collections.unmodifiableSet(menus);
			case SCOPE_MODEL:
				return Collections.unmodifiableSet(scopeModels);
			case TIMELINE_GRAPH:
				return Collections.unmodifiableSet(timelineGraphs);
			case CHART:
				return Collections.unmodifiableSet(charts);
			default:
				return Collections.emptyList();
		}
	}

	@JsonIgnore
	public boolean hasVirtualScopeModel() {
		return getScopeModels().stream().anyMatch(ScopeModel::isVirtual);
	}

	public String geLocalizedPasswordLength(final String[] langs) {
		return Arrays.stream(langs).filter(PASSWORD_LENGTH_MESSAGES::containsKey).findFirst().map(PASSWORD_LENGTH_MESSAGES::get).orElse(DEFAULT_PASSWORD_LENGTH_MESSAGE);
	}

	public String getLocalizedPasswordStrong(final String[] langs) {
		return Arrays.stream(langs).filter(PASSWORD_STRENGTH_MESSAGES::containsKey).findFirst().map(PASSWORD_STRENGTH_MESSAGES::get).orElse(DEFAULT_PASSWORD_STRENGTH_MESSAGE);
	}

	@JsonIgnore
	public String getLocalizedPasswordMessage(final String... langs) {
		final var message = String.format(geLocalizedPasswordLength(langs), getPasswordLength());

		if(isPasswordStrong()) {
			return String.format("%s%s", message, getLocalizedPasswordStrong(langs));
		}

		return message;
	}

	@Override
	public int compareTo(final Study otherStudy) {
		return DEFAULT_COMPARATOR.compare(this, otherStudy);
	}

	@Override
	@JsonAnySetter
	public void setAnySetter(final String key, final Object value) {
		if(!"className".equals(key) && !"ruleDefinitionProperties".equals(key) && !"ruleDefinitionActions".equals(key)) {
			LOGGER.error("Unknown property {} (value {}) in class {}", key, value, getClassName());
		}
	}

	@JsonIgnore
	public String generateFilename(final String filename) {
		final var studyLabel = StringUtils.replace(getDefaultLocalizedShortname().toLowerCase(), " ", "_");
		final var date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		return String.format("%s_%s_%s", studyLabel, filename, date);
	}

	@JsonIgnore
	public String generateFilename(final String filename, final ExportFormat format) {
		return String.format("%s.%s", generateFilename(filename), format.getExtension());
	}
}
