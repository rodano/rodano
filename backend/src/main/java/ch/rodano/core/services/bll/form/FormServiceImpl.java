package ch.rodano.core.services.bll.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.exceptions.NoRespectForConfigurationException;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.form.FormModel;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.exception.InconsistentStateDetectedException;
import ch.rodano.core.model.exception.MissingDataException;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.form.FormDAOService;
import ch.rodano.core.utils.ACL;
import ch.rodano.core.utils.UtilsService;

@Service
public class FormServiceImpl implements FormService {
	private final StudyService studyService;
	private final FormDAOService formDAOService;
	private final DatasetService datasetService;
	private final FieldService fieldService;
	private final WorkflowStatusService workflowStatusService;
	private final FormContentService formContentService;
	private final UtilsService utilsService;

	public FormServiceImpl(
		final StudyService studyService,
		final FormDAOService formDAOService,
		final DatasetService datasetService,
		final FieldService fieldService,
		@Lazy final WorkflowStatusService workflowStatusService,
		final FormContentService formContentService,
		final UtilsService utilsService
	) {
		this.studyService = studyService;
		this.formDAOService = formDAOService;
		this.datasetService = datasetService;
		this.fieldService = fieldService;
		this.workflowStatusService = workflowStatusService;
		this.formContentService = formContentService;
		this.utilsService = utilsService;
	}

	@Override
	public List<Form> createAll(final Scope scope, final DatabaseActionContext context, final String rationale) {
		return scope.getScopeModel().getFormModels().stream()
			.filter(f -> !f.isOptional())
			.map(f -> create(scope, f, context, rationale))
			.toList();
	}

	@Override
	public List<Form> createAll(final Scope scope, final Event event, final DatabaseActionContext context, final String rationale) {
		return event.getEventModel().getFormModels().stream()
			.filter(f -> !f.isOptional())
			.map(f -> create(scope, event, f, context, rationale))
			.toList();
	}

	@Override
	public Form create(final Scope scope, final FormModel formModel, final DatabaseActionContext context, final String rationale) {
		return create(scope, Optional.empty(), formModel, context, rationale);
	}

	@Override
	public Form create(final Scope scope, final Event event, final FormModel formModel, final DatabaseActionContext context, final String rationale) {
		return create(scope, Optional.of(event), formModel, context, rationale);
	}

	private Form create(final Scope scope, final Optional<Event> event, final FormModel formModel, final DatabaseActionContext context, final String rationale) {
		utilsService.checkNotDeleted(scope, event);
		utilsService.checkNotLocked(scope, event);

		if(event.isEmpty()) {
			//check that the form model is allowed for the scope model
			if(!scope.getScopeModel().getFormModelIds().contains(formModel.getId())) {
				throw new NoRespectForConfigurationException(
					String.format(
						"Form model %s is not allowed for the scope model %s", formModel.getId(), scope.getScopeModelId()
					)
				);
			}
		}
		else {
			//check that the form model is allowed for the event model
			if(!event.get().getEventModel().getFormModelIds().contains(formModel.getId())) {
				throw new NoRespectForConfigurationException(
					String.format(
						"Form model %s is not allowed for the event model %s", formModel.getId(), event.get().getEventModelId()
					)
				);
			}
		}

		final var form = new Form();
		form.setFormModel(formModel);
		form.setScopeFk(scope.getPk());
		event.map(Event::getPk).ifPresent(form::setEventFk);

		final var enhancedRationale = StringUtils.isBlank(rationale) ? "Create form" : "Create form: " + rationale;

		formDAOService.saveForm(form, context, enhancedRationale);
		final var family = new DataFamily(scope, event, form);
		workflowStatusService.createAll(family, form, Collections.emptyMap(), context, enhancedRationale);

		return form;
	}

	@Override
	public void delete(final Scope scope, final Optional<Event> event, final Form form, final DatabaseActionContext context, final String rationale) {
		utilsService.checkNotDeleted(scope, event);
		utilsService.checkNotLocked(scope, event);

		final var baseRationale = "Form removed";
		final var enhancedRationale = StringUtils.isBlank(rationale) ? baseRationale : String.format("%s: %s", baseRationale, rationale);
		formDAOService.deleteForm(form, context, enhancedRationale);

		final var formContent = formContentService.generateFormContent(scope, event, form);

		//reset workflow status on the form its fields
		final var family = new DataFamily(scope, event, form);
		workflowStatusService.resetMandatoryAndDeleteTheRest(family, form, context, enhancedRationale);
		for(final Entry<Dataset, Set<Field>> entry : formContent.getFieldsNotInMultiple().entrySet()) {
			for(final Field field : entry.getValue()) {
				final var fieldFamily = new DataFamily(scope, event, entry.getKey(), field);
				workflowStatusService.resetMandatoryAndDeleteTheRest(fieldFamily, field, context, enhancedRationale);
			}
		}

		final var scopeModelDatasetModels = scope.getScopeModel().getDatasetModelIds();
		final var eventDatasetModels = event.isPresent() ? event.get().getEventModel().getDatasetModelIds() : new ArrayList<>();

		//delete multiple datasets that are not directly attached to the scope or event
		final var datasetRationale = String.format("Record deleted: %s", baseRationale);
		for(final var dataset : formContent.getMultipleDatasets()) {
			if(event.isPresent() && eventDatasetModels.contains(dataset.getDatasetModelId()) || event.isEmpty() && scopeModelDatasetModels.contains(dataset.getDatasetModelId())) {
				datasetService.delete(scope, event, dataset, context, datasetRationale);
			}
		}

		//reset fields that are not on scope
		final var fieldRationale = String.format("Field reset: %s", baseRationale);
		for(final Entry<Dataset, Set<Field>> entry : formContent.getFieldsNotInMultiple().entrySet()) {
			//reset only non-blank values
			if(event.isPresent() && eventDatasetModels.contains(entry.getKey().getDatasetModelId()) || event.isEmpty() && scopeModelDatasetModels.contains(entry.getKey().getDatasetModelId())) {
				for(final Field field : entry.getValue()) {
					if(!field.getFieldModel().isPlugin() && field.isNotNull()) {
						fieldService.reset(scope, event, entry.getKey(), field, context, fieldRationale);
					}
				}
			}
		}
	}

	@Override
	public void restore(final Scope scope, final Optional<Event> event, final Form form, final DatabaseActionContext context, final String rationale) {
		utilsService.checkNotDeleted(scope, event);
		utilsService.checkNotLocked(scope, event);

		final var baseRationale = "Form restored";
		final var enhancedRationale = StringUtils.isBlank(rationale) ? baseRationale : String.format("%s: %s", baseRationale, rationale);

		formDAOService.restoreForm(form, context, enhancedRationale);
	}

	@Override
	public List<Form> search(final Scope scope, final Optional<Event> event, final ACL acl) {
		final var formModelIds = this.studyService.getStudy().getFormModels().stream().filter(fm -> acl.hasRight(fm, Rights.READ)).map(FormModel::getId).toList();
		if(formModelIds.isEmpty()) {
			return Collections.emptyList();
		}
		final var includeDeleted = acl.hasRight(FeatureStatic.MANAGE_DELETED_DATA);
		final var forms = new ArrayList<>(
			formDAOService.search(
				scope.getPk(),
				event.map(Event::getPk),
				includeDeleted,
				Optional.of(formModelIds)
			)
		);
		Collections.sort(forms);
		return forms;
	}

	@Override
	public List<Form> getAllIncludingRemoved(final Scope scope) {
		return formDAOService.getAllFormsByScopePk(scope.getPk());
	}

	@Override
	public List<Form> getAll(final Scope scope) {
		return formDAOService.getFormsByScopePk(scope.getPk());
	}

	@Override
	public Form get(final Scope scope, final FormModel formModel) {
		final var forms = formDAOService.getFormsByScopePkAndFormModelIds(scope.getPk(), Collections.singletonList(formModel.getId()));
		if(forms.isEmpty()) {
			throw new MissingDataException(String.format("No form with model id %s in scope %s", formModel.getId(), scope.getCode()));
		}
		if(forms.size() > 1) {
			throw new InconsistentStateDetectedException(String.format("Only one form with model id %s should exist in scope %s", formModel.getId(), scope.getCode()));
		}
		return forms.getFirst();
	}

	@Override
	public List<Form> getAllIncludingRemoved(final Event event) {
		return formDAOService.getAllFormsByEventPk(event.getPk());
	}

	@Override
	public List<Form> getAll(final Event event) {
		return formDAOService.getFormsByEventPk(event.getPk());
	}

	@Override
	public Form get(final Event event, final FormModel formModel) {
		final var forms = formDAOService.getFormsByEventPkAndFormModelIds(event.getPk(), Collections.singletonList(formModel.getId()));
		if(forms.isEmpty()) {
			throw new MissingDataException(String.format("No form with model id %s in event %s", formModel.getId(), event.getEventModelId()));
		}
		return forms.get(0);
	}

	@Override
	public void save(final Scope scope, final Optional<Event> event, final Form form, final DatabaseActionContext context, final String rationale) {
		utilsService.checkNotDeleted(scope, event);
		utilsService.checkNotLocked(scope, event);

		formDAOService.saveForm(form, context, rationale);
	}

	@Override
	public Optional<Form> get(final WorkflowStatus workflowStatus) {
		//workflow status may not be linked to a form
		return Optional.ofNullable(workflowStatus.getFormFk()).map(this.formDAOService::getFormByPk);
	}
}
