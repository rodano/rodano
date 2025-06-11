package ch.rodano.core.services.bll.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

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
import ch.rodano.core.model.exception.LockedObjectException;
import ch.rodano.core.model.exception.MissingDataException;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.form.FormDAOService;
import ch.rodano.core.utils.ACL;
import ch.rodano.core.utils.UtilsService;

@Service
public class FormServiceImpl implements FormService {
	private final FormDAOService formDAOService;
	private final DatasetService datasetService;
	private final FieldService fieldService;
	private final WorkflowStatusService workflowStatusService;
	private final FormContentService formContentService;
	private final UtilsService utilsService;

	public FormServiceImpl(
		final FormDAOService formDAOService,
		final DatasetService datasetService,
		final FieldService fieldService,
		@Lazy final WorkflowStatusService workflowStatusService,
		final FormContentService formContentService,
		final UtilsService utilsService
	) {
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
		//check that scope is not locked
		if(scope.getLocked()) {
			throw new LockedObjectException(scope);
		}

		//check that the form model is allowed for the scope model
		if(!scope.getScopeModel().getFormModelIds().contains(formModel.getId())) {
			throw new NoRespectForConfigurationException(
				String.format(
					"Form model %s is not allowed for the scope model %s", formModel.getId(), scope.getScopeModelId()
				)
			);
		}

		final var form = new Form();
		form.setFormModel(formModel);
		form.setScopeFk(scope.getPk());

		final var enhancedRationale = StringUtils.isBlank(rationale) ? "Create form" : "Create form: " + rationale;

		formDAOService.saveForm(form, context, enhancedRationale);
		final var family = new DataFamily(scope, form);
		workflowStatusService.createAll(family, form, Collections.emptyMap(), context, enhancedRationale);

		return form;
	}

	@Override
	public Form create(final Scope scope, final Event event, final FormModel formModel, final DatabaseActionContext context, final String rationale) {
		//check that scope and event are not locked
		if(scope.getLocked()) {
			throw new LockedObjectException(scope);
		}
		if(event.getLocked()) {
			throw new LockedObjectException(event);
		}

		//check that the form model is allowed for the event model
		if(!event.getEventModel().getFormModelIds().contains(formModel.getId())) {
			throw new NoRespectForConfigurationException(
				String.format(
					"Form model %s is not allowed for the event model %s", formModel.getId(), event.getEventModelId()
				)
			);
		}

		final var form = new Form();
		form.setFormModel(formModel);
		form.setEventFk(event.getPk());

		final var enhancedRationale = StringUtils.isBlank(rationale) ? "Create form" : "Create form: " + rationale;

		formDAOService.saveForm(form, context, enhancedRationale);
		final var family = new DataFamily(scope, event, form);
		workflowStatusService.createAll(family, form, Collections.emptyMap(), context, enhancedRationale);

		return form;
	}

	@Override
	public void delete(final Scope scope, final Optional<Event> event, final Form form, final DatabaseActionContext context, final String rationale) {
		utilsService.checkNotDeleted(scope);
		event.ifPresent(utilsService::checkNotDeleted);

		utilsService.checkNotLocked(scope);
		event.ifPresent(utilsService::checkNotLocked);

		final var baseRationale = "Form removed";
		final var enhancedRationale = StringUtils.isBlank(rationale) ? baseRationale : String.format("%s: %s", baseRationale, rationale);
		formDAOService.deleteForm(form, context, enhancedRationale);

		final var formContent = formContentService.generateFormContent(scope, event, form);

		//reset workflow status on the form its fields
		final var family = new DataFamily(scope, event, form);
		workflowStatusService.resetMandatoryAndDeleteTheRest(family, form, context, enhancedRationale);
		for(final Entry<Dataset, List<Field>> entry : formContent.getFieldsNotInMultiple().entrySet()) {
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
		for(final Entry<Dataset, List<Field>> entry : formContent.getFieldsNotInMultiple().entrySet()) {
			//reset only non-blank values
			if(event.isPresent() && eventDatasetModels.contains(entry.getKey().getDatasetModelId()) || event.isEmpty() && scopeModelDatasetModels.contains(entry.getKey().getDatasetModelId())) {
				for(final Field field : entry.getValue()) {
					if(!field.getFieldModel().isPlugin() && !field.isNull()) {
						fieldService.reset(scope, event, entry.getKey(), field, context, fieldRationale);
					}
				}
			}
		}
	}

	@Override
	public void restore(final Scope scope, final Optional<Event> event, final Form form, final DatabaseActionContext context, final String rationale) {
		utilsService.checkNotDeleted(scope);
		event.ifPresent(utilsService::checkNotDeleted);

		utilsService.checkNotLocked(scope);
		event.ifPresent(utilsService::checkNotLocked);

		final var baseRationale = "Form restored";
		final var enhancedRationale = StringUtils.isBlank(rationale) ? baseRationale : String.format("%s: %s", baseRationale, rationale);

		formDAOService.restoreForm(form, context, enhancedRationale);
	}

	@Override
	public List<Form> search(final Scope scope, final Optional<Event> event, final ACL acl) {
		final var includeDeleted = acl.hasRight(FeatureStatic.MANAGE_DELETED_DATA);
		final List<Form> forms;
		if(includeDeleted) {
			forms = event.isPresent() ? getAllIncludingRemoved(event.get()) : getAllIncludingRemoved(scope);
		}
		else {
			forms = event.isPresent() ? getAll(event.get()) : getAll(scope);
		}
		return forms.stream()
			//TOOD add a date
			.filter(f -> acl.hasRight(f.getFormModel(), Rights.READ))
			.sorted()
			.toList();
	}

	@Override
	public List<Form> getAllIncludingRemoved(final Scope scope) {
		return formDAOService.getFormsByScopePkIncludingRemoved(scope.getPk());
	}

	@Override
	public List<Form> getAll(final Scope scope) {
		return formDAOService.getFormsByScopePk(scope.getPk());
	}

	@Override
	public Form get(final Scope scope, final String formId) {
		final var form = formDAOService.getFormByScopePkAndFormModelId(scope.getPk(), formId);
		if(formId != null) {
			return form;
		}
		throw new MissingDataException(String.format("No form with id %s for scope %s", formId, scope.getCode()));
	}

	@Override
	public List<Form> getAllIncludingRemoved(final Event event) {
		return formDAOService.getFormsByEventPkIncludingRemoved(event.getPk());
	}

	@Override
	public List<Form> getAll(final Event event) {
		return formDAOService.getFormsByEventPk(event.getPk());
	}

	@Override
	public Form get(final Event event, final String formId) {
		final var form = formDAOService.getFormByEventPkAndFormModelId(event.getPk(), formId);
		if(formId != null) {
			return form;
		}
		throw new MissingDataException(String.format("No form with id %s for event %s", formId, event.getEventModelId()));
	}

	@Override
	public void save(final Form form, final DatabaseActionContext context, final String rationale) {
		formDAOService.saveForm(form, context, rationale);
	}

	@Override
	public Optional<Form> get(final WorkflowStatus workflowStatus) {
		//workflow status may not be linked to a form
		return Optional.ofNullable(workflowStatus.getFormFk()).map(this.formDAOService::getFormByPk);
	}
}
