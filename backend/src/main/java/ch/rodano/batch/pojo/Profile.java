package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Profile {

	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;

	private Integer oderBy;
	private String workflowIdOfInterest;

	private List<String> grantedReportIds;
	private List<String> grantedTimelineGraphIds;
	private List<String> grantedCategoryIds;
	private List<String> grantedMenuIds;
	private List<String> grantedFeatureIds;

	private Map<String, List<String>> grantedProfileIdRights;
	private Map<String, List<String>> grantedDatasetModelIdRights;
	private Map<String, List<String>> grantedScopeModelIdRights;
	private Map<String, List<String>> grantedPaymentIdRights;
	private Map<String, List<String>> grantedEventModelIdRights;
	private Map<String, List<String>> grantedFormModelIdRights;

	private Map<String, WorkflowRights> grantedWorkflowIds;

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

	public Integer getOderBy() {
		return oderBy;
	}

	public void setOderBy(final Integer oderBy) {
		this.oderBy = oderBy;
	}

	public String getWorkflowIdOfInterest() {
		return workflowIdOfInterest;
	}

	public void setWorkflowIdOfInterest(final String workflowIdOfInterest) {
		this.workflowIdOfInterest = workflowIdOfInterest;
	}

	public List<String> getGrantedReportIds() {
		return grantedReportIds;
	}

	public void setGrantedReportIds(final List<String> grantedReportIds) {
		this.grantedReportIds = grantedReportIds;
	}

	public List<String> getGrantedTimelineGraphIds() {
		return grantedTimelineGraphIds;
	}

	public void setGrantedTimelineGraphIds(final List<String> grantedTimelineGraphIds) {
		this.grantedTimelineGraphIds = grantedTimelineGraphIds;
	}

	public List<String> getGrantedCategoryIds() {
		return grantedCategoryIds;
	}

	public void setGrantedCategoryIds(final List<String> grantedCategoryIds) {
		this.grantedCategoryIds = grantedCategoryIds;
	}

	public List<String> getGrantedMenuIds() {
		return grantedMenuIds;
	}

	public void setGrantedMenuIds(final List<String> grantedMenuIds) {
		this.grantedMenuIds = grantedMenuIds;
	}

	public List<String> getGrantedFeatureIds() {
		return grantedFeatureIds;
	}

	public void setGrantedFeatureIds(final List<String> grantedFeatureIds) {
		this.grantedFeatureIds = grantedFeatureIds;
	}

	public Map<String, List<String>> getGrantedProfileIdRights() {
		return grantedProfileIdRights;
	}

	public void setGrantedProfileIdRights(final Map<String, List<String>> grantedProfileIdRights) {
		this.grantedProfileIdRights = grantedProfileIdRights;
	}

	public Map<String, List<String>> getGrantedDatasetModelIdRights() {
		return grantedDatasetModelIdRights;
	}

	public void setGrantedDatasetModelIdRights(final Map<String, List<String>> grantedDatasetModelIdRights) {
		this.grantedDatasetModelIdRights = grantedDatasetModelIdRights;
	}

	public Map<String, List<String>> getGrantedScopeModelIdRights() {
		return grantedScopeModelIdRights;
	}

	public void setGrantedScopeModelIdRights(final Map<String, List<String>> grantedScopeModelIdRights) {
		this.grantedScopeModelIdRights = grantedScopeModelIdRights;
	}

	public Map<String, List<String>> getGrantedPaymentIdRights() {
		return grantedPaymentIdRights;
	}

	public void setGrantedPaymentIdRights(final Map<String, List<String>> grantedPaymentIdRights) {
		this.grantedPaymentIdRights = grantedPaymentIdRights;
	}

	public Map<String, List<String>> getGrantedEventModelIdRights() {
		return grantedEventModelIdRights;
	}

	public void setGrantedEventModelIdRights(final Map<String, List<String>> grantedEventModelIdRights) {
		this.grantedEventModelIdRights = grantedEventModelIdRights;
	}

	public Map<String, List<String>> getGrantedFormModelIdRights() {
		return grantedFormModelIdRights;
	}

	public void setGrantedFormModelIdRights(final Map<String, List<String>> grantedFormModelIdRights) {
		this.grantedFormModelIdRights = grantedFormModelIdRights;
	}

	public Map<String, WorkflowRights> getGrantedWorkflowIds() {
		return grantedWorkflowIds;
	}

	public void setGrantedWorkflowIds(final Map<String, WorkflowRights> gramtedWorkflowIds) {
		this.grantedWorkflowIds = gramtedWorkflowIds;
	}

	@Override
	public String toString() {
		return "Profile{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", oderBy=" + oderBy +
			", workflowIdOfInterest='" + workflowIdOfInterest + '\'' +
			", grantedReportIds=" + grantedReportIds +
			", grantedTimelineGraphIds=" + grantedTimelineGraphIds +
			", grantedCategoryIds=" + grantedCategoryIds +
			", grantedMenuIds=" + grantedMenuIds +
			", grantedFeatureIds=" + grantedFeatureIds +
			", grantedProfileIdRights=" + grantedProfileIdRights +
			", grantedDatasetModelIdRights=" + grantedDatasetModelIdRights +
			", grantedScopeModelIdRights=" + grantedScopeModelIdRights +
			", grantedPaymentIdRights=" + grantedPaymentIdRights +
			", grantedEventModelIdRights=" + grantedEventModelIdRights +
			", grantedFormModelIdRights=" + grantedFormModelIdRights +
			", grantedWorkflowIds=" + grantedWorkflowIds +
			'}';
	}
}
