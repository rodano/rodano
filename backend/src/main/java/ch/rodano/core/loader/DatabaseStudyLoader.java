package ch.rodano.core.loader;

import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import org.springframework.stereotype.Component;

import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.configuration.model.cron.Cron;
import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.feature.Feature;
import ch.rodano.configuration.model.form.FormModel;
import ch.rodano.configuration.model.menu.Menu;
import ch.rodano.configuration.model.payment.PaymentPlan;
import ch.rodano.configuration.model.policy.PrivacyPolicy;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.reports.Report;
import ch.rodano.configuration.model.reports.WorkflowSummary;
import ch.rodano.configuration.model.reports.WorkflowWidget;
import ch.rodano.configuration.model.resource.ResourceCategory;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.timelinegraph.TimelineGraph;
import ch.rodano.configuration.model.validator.Validator;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.core.dao.ChartDAO;
import ch.rodano.core.dao.CronDAO;
import ch.rodano.core.dao.DatasetModelDAO;
import ch.rodano.core.dao.FeatureDAO;
import ch.rodano.core.dao.FormModelDAO;
import ch.rodano.core.dao.MenuDAO;
import ch.rodano.core.dao.PaymentPlanDAO;
import ch.rodano.core.dao.PrivacyPolicyDAO;
import ch.rodano.core.dao.ProfileDAO;
import ch.rodano.core.dao.ProjectDAO;
import ch.rodano.core.dao.ReportDAO;
import ch.rodano.core.dao.ResourceCategoryDAO;
import ch.rodano.core.dao.ScopeModelDAO;
import ch.rodano.core.dao.TimelineGraphDAO;
import ch.rodano.core.dao.ValidatorDAO;
import ch.rodano.core.dao.WorkflowDAO;
import ch.rodano.core.dao.WorkflowSummaryDAO;
import ch.rodano.core.dao.WorkflowWidgetDAO;

@Component
public class DatabaseStudyLoader {

	private final ProjectDAO projectDAO;
	private final ScopeModelDAO scopeModelDAO;
	private final DatasetModelDAO datasetModelDAO;
	private final FormModelDAO formModelDAO;
	private final WorkflowDAO workflowDAO;
	private final FeatureDAO featureDAO;
	private final PaymentPlanDAO paymentPlanDAO;
	private final PrivacyPolicyDAO privacyPolicyDAO;
	private final ReportDAO reportDAO;
	private final WorkflowSummaryDAO workflowSummaryDAO;
	private final WorkflowWidgetDAO workflowWidgetDAO;
	private final ResourceCategoryDAO resourceCategoryDAO;
	private final TimelineGraphDAO timelineGraphDAO;
	private final CronDAO cronDAO;
	private final MenuDAO menuDAO;
	private final ChartDAO chartDAO;
	private final ProfileDAO profileDAO;
	private final ValidatorDAO validatorDAO;

	public DatabaseStudyLoader(final ProjectDAO projectDAO,
							   final ScopeModelDAO scopeModelDAO,
							   final DatasetModelDAO datasetModelDAO,
							   final FormModelDAO formModelDAO,
							   final WorkflowDAO workflowDAO,
							   final FeatureDAO featureDAO,
							   final PaymentPlanDAO paymentPlanDAO,
							   final PrivacyPolicyDAO privacyPolicyDAO,
							   final ReportDAO reportDAO,
							   final WorkflowSummaryDAO workflowSummaryDAO,
							   final WorkflowWidgetDAO workflowWidgetDAO,
							   final ResourceCategoryDAO resourceCategoryDAO,
							   final TimelineGraphDAO timelineGraphDAO,
							   final CronDAO cronDAO,
							   final MenuDAO menuDAO,
							   final ChartDAO chartDAO,
							   final ProfileDAO profileDAO,
							   final ValidatorDAO validatorDAO) {
		this.projectDAO = projectDAO;
		this.scopeModelDAO = scopeModelDAO;
		this.datasetModelDAO = datasetModelDAO;
		this.formModelDAO = formModelDAO;
		this.workflowDAO = workflowDAO;
		this.featureDAO = featureDAO;
		this.paymentPlanDAO = paymentPlanDAO;
		this.privacyPolicyDAO = privacyPolicyDAO;
		this.reportDAO = reportDAO;
		this.workflowSummaryDAO = workflowSummaryDAO;
		this.workflowWidgetDAO = workflowWidgetDAO;
		this.resourceCategoryDAO = resourceCategoryDAO;
		this.timelineGraphDAO = timelineGraphDAO;
		this.cronDAO = cronDAO;
		this.menuDAO = menuDAO;
		this.chartDAO = chartDAO;
		this.profileDAO = profileDAO;
		this.validatorDAO = validatorDAO;
	}

	public Study loadStudy(final UUID projectId) {
		final Study study = projectDAO.findById(projectId)
			.orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

		System.out.println("   ✓ Loaded project: " + study.getId());

		loadScopeModels(study, projectId);
		loadDatasetModels(study, projectId);
		loadFormModels(study, projectId);
		loadWorkflows(study, projectId);
		loadFeatures(study, projectId);
		loadPaymentPlans(study, projectId);
		loadPrivacyPolicies(study, projectId);
		loadReports(study, projectId);
		loadWorkflowSummaries(study, projectId);
		loadWorkflowWidgets(study, projectId);
		loadResourceCategories(study, projectId);
		loadTimelineGraphs(study, projectId);
		loadCrons(study, projectId);
		loadMenus(study, projectId);
		loadCharts(study, projectId);
		loadProfiles(study, projectId);
		loadValidators(study, projectId);

		wireRelationships(study);

		return study;
	}

	private void loadScopeModels(final Study study, final UUID projectId) {
		final List<ScopeModel> scopeModels = scopeModelDAO.findByProject(projectId);
		study.setScopeModels(new TreeSet<>(scopeModels));
		System.out.println("   ✓ Loaded " + scopeModels.size() + " scope models");
	}

	private void loadDatasetModels(final Study study, final UUID projectId) {
		final List<DatasetModel> datasetModels = datasetModelDAO.findByProject(projectId);
		study.setDatasetModels(new TreeSet<>(datasetModels));
		System.out.println("   ✓ Loaded " + datasetModels.size() + " dataset models");
	}

	private void loadFormModels(final Study study, final UUID projectId) {
		final List<FormModel> formModels = formModelDAO.findByProject(projectId);
		study.setFormModels(new TreeSet<>(formModels));
		System.out.println("   ✓ Loaded " + formModels.size() + " form models");
	}

	private void loadWorkflows(final Study study, final UUID projectId) {
		final List<Workflow> workflows = workflowDAO.findByProject(projectId);
		study.setWorkflows(new TreeSet<>(workflows));
		System.out.println("   ✓ Loaded " + workflows.size() + " workflows");
	}

	private void loadFeatures(final Study study, final UUID projectId) {
		final List<Feature> features = featureDAO.findByProject(projectId);
		study.setFeatures(new TreeSet<>(features));
		System.out.println("   ✓ Loaded " + features.size() + " features");
	}

	private void loadPaymentPlans(final Study study, final UUID projectId) {
		final List<PaymentPlan> paymentPlans = paymentPlanDAO.findByProject(projectId);
		study.setPaymentPlans(new TreeSet<>(paymentPlans));
		System.out.println("   ✓ Loaded " + paymentPlans.size() + " payment plans");
	}

	private void loadPrivacyPolicies(final Study study, final UUID projectId) {
		final List<PrivacyPolicy> privacyPolicies = privacyPolicyDAO.findByProject(projectId);
		study.setPrivacyPolicies(new TreeSet<>(privacyPolicies));
		System.out.println("   ✓ Loaded " + privacyPolicies.size() + " privacy policies");
	}

	private void loadReports(final Study study, final UUID projectId) {
		final List<Report> reports = reportDAO.findByProject(projectId);
		study.setReports(new TreeSet<>(reports));
		System.out.println("   ✓ Loaded " + reports.size() + " reports");
	}

	private void loadWorkflowSummaries(final Study study, final UUID projectId) {
		final List<WorkflowSummary> workflowSummaries = workflowSummaryDAO.findByProject(projectId);
		study.setWorkflowSummaries(new TreeSet<>(workflowSummaries));
		System.out.println("   ✓ Loaded " + workflowSummaries.size() + " workflow summaries");
	}

	private void loadWorkflowWidgets(final Study study, final UUID projectId) {
		final List<WorkflowWidget> workflowWidgets = workflowWidgetDAO.findByProject(projectId);
		study.setWorkflowWidgets(new TreeSet<>(workflowWidgets));
		System.out.println("   ✓ Loaded " + workflowWidgets.size() + " workflow widgets");
	}

	private void loadResourceCategories(final Study study, final UUID projectId) {
		final List<ResourceCategory> resourceCategories = resourceCategoryDAO.findByProject(projectId);
		study.setResourceCategories(new TreeSet<>(resourceCategories));
		System.out.println("   ✓ Loaded " + resourceCategories.size() + " resource categories");
	}

	private void loadTimelineGraphs(final Study study, final UUID projectId) {
		final List<TimelineGraph> timelineGraphs = timelineGraphDAO.findByProject(projectId);
		study.setTimelineGraphs(new TreeSet<>(timelineGraphs));
		System.out.println("   ✓ Loaded " + timelineGraphs.size() + " timeline graphs");
	}

	private void loadCrons(final Study study, final UUID projectId) {
		final List<Cron> crons = cronDAO.findByProject(projectId);
		study.setCrons(crons);
		System.out.println("   ✓ Loaded " + crons.size() + " crons");
	}

	private void loadMenus(final Study study, final UUID projectId) {
		final List<Menu> menus = menuDAO.findByProject(projectId);
		study.setMenus(new TreeSet<>(menus));
		System.out.println("   ✓ Loaded " + menus.size() + " menus");
	}

	private void loadCharts(final Study study, final UUID projectId) {
		final List<Chart> charts = chartDAO.findByProject(projectId);
		study.setCharts(new TreeSet<>(charts));
		System.out.println("   ✓ Loaded " + charts.size() + " charts");
	}

	private void loadProfiles(final Study study, final UUID projectId) {
		final List<Profile> profiles = profileDAO.findByProject(projectId);
		study.setProfiles(new TreeSet<>(profiles));
		System.out.println("   ✓ Loaded " + profiles.size() + " profiles");
	}

	private void loadValidators(final Study study, final UUID projectId) {
		final List<Validator> validators = validatorDAO.findByProject(projectId);
		study.setValidators(new TreeSet<>(validators));
		System.out.println("   ✓ Loaded " + validators.size() + " validators");
	}

	private void wireRelationships(final Study study) {
		wireStudyReferences(study);

		wireScopeModelRelationships(study);
		wireDatasetModelRelationships(study);
		wireFormModelRelationships(study);
		wireWorkflowRelationships(study);
		wireTimelineGraphRelationships(study);
	}

	private void wireStudyReferences(final Study study) {
		study.getScopeModels().forEach(sm -> sm.setStudy(study));
		study.getDatasetModels().forEach(dm -> dm.setStudy(study));
		study.getFormModels().forEach(fm -> fm.setStudy(study));
		study.getWorkflows().forEach(w -> w.setStudy(study));
		study.getFeatures().forEach(f -> f.setStudy(study));
		study.getPaymentPlans().forEach(pp -> pp.setStudy(study));
		study.getPrivacyPolicies().forEach(pp -> pp.setStudy(study));
		study.getReports().forEach(r -> r.setStudy(study));
		study.getWorkflowSummaries().forEach(ws -> ws.setStudy(study));
		study.getWorkflowWidgets().forEach(ww -> ww.setStudy(study));
		study.getResourceCategories().forEach(rc -> rc.setStudy(study));
		study.getTimelineGraphs().forEach(tg -> tg.setStudy(study));
		study.getCrons().forEach(c -> c.setStudy(study));
		study.getMenus().forEach(m -> m.setStudy(study));
		study.getCharts().forEach(c -> c.setStudy(study));
		study.getProfiles().forEach(p -> p.setStudy(study));
	}

	private void wireScopeModelRelationships(final Study study) {
		for(ScopeModel scopeModel : study.getScopeModels()) {
			scopeModel.getEventModels().forEach(em -> em.setScopeModel(scopeModel));
		}
	}

	private void wireDatasetModelRelationships(final Study study) {
		for(DatasetModel datasetModel : study.getDatasetModels()) {
			datasetModel.getFieldModels().forEach(fm -> fm.setDatasetModel(datasetModel));
		}
	}

	private void wireFormModelRelationships(final Study study) {
		for(FormModel formModel : study.getFormModels()) {
			formModel.getLayouts().forEach(layout -> {
				layout.setFormModel(formModel);

				layout.getLines().forEach(line -> {
					line.setLayout(layout);

					line.getCells().forEach(cell -> {
						cell.setLine(line);
					});
				});
			});
		}
	}

	private void wireWorkflowRelationships(final Study study) {
		for(Workflow workflow : study.getWorkflows()) {
			workflow.getStates().forEach(state -> state.setWorkflow(workflow));
			workflow.getActions().forEach(action -> action.setWorkflow(workflow));
		}
	}

	private void wireTimelineGraphRelationships(final Study study) {
		for(TimelineGraph timelineGraph : study.getTimelineGraphs()) {
			timelineGraph.getSections().forEach(section -> {
				section.setTimelineGraph(timelineGraph);
			});
		}
	}
}
