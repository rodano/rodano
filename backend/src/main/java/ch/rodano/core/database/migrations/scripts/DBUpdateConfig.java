package ch.rodano.core.database.migrations.scripts;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.core.database.migrations.AbstractDatabaseMigration;
import ch.rodano.core.database.migrations.MigrationBean;
import ch.rodano.core.database.migrations.MigrationTask;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.audit.AuditActionService;

/**
 * This migration script must be extended to create new events / datasets or fields
 * TODO It may be improved into an automatic script that update a database according to the current configuration
 * TODO In this case, it would be more efficient to proceed scope by scope
 */
@MigrationBean
public abstract class DBUpdateConfig extends AbstractDatabaseMigration {

	private final AuditActionService auditActionService;
	private final ScopeService scopeService;
	private final EventService eventService;
	private final DatasetService datasetService;
	private final FieldService fieldService;

	protected final Study study;

	public DBUpdateConfig(
		final AuditActionService auditActionService,
		final ScopeService scopeService,
		final EventService eventService,
		final StudyService studyService,
		final DatasetService datasetService,
		final FieldService fieldService
	) {
		this.auditActionService = auditActionService;
		this.scopeService = scopeService;
		this.eventService = eventService;
		this.datasetService = datasetService;
		this.fieldService = fieldService;
		study = studyService.getStudy();
	}

	@Override
	protected abstract Double migrationTaskNumber();

	protected abstract String description();

	@Override
	protected abstract String context();

	protected abstract Map<ScopeModel, List<String>> eventsToAddByScope();

	protected abstract Map<EventModel, List<String>> datasetsToAddByEvent();

	protected abstract Map<DatasetModel, List<String>> fieldsToAddByDataset();

	@Override
	protected List<MigrationTask> tasks() {
		//context must be created in this method as only this method is @transactional
		final var context = auditActionService.createAuditActionAndGenerateContext(Actor.SYSTEM, context());
		return List.of(
			addEvents(context),
			addDatasets(context),
			addFields(context)
		);
	}

	private void addEventsToScope(final DatabaseActionContext context, final Scope scope, final List<EventModel> eventModels) {
		for(final EventModel eventModel : eventModels) {
			eventService.create(scope, eventModel, context, context());
		}
	}

	private void addDatasetsToEvent(final DatabaseActionContext context, final Scope scope, final Event event, final List<DatasetModel> datasetModels) {
		for(final DatasetModel datasetModel : datasetModels) {
			datasetService.create(scope, event, datasetModel, context, context());
		}
	}

	private void addFieldsToDataset(final DatabaseActionContext context, final Scope scope, final Optional<Event> event, final Dataset dataset, final List<FieldModel> attributes) {
		for(final FieldModel attribute : attributes) {
			fieldService.create(scope, event, dataset, attribute, context, context());
		}
	}

	private MigrationTask addEvents(final DatabaseActionContext context) {
		return new MigrationTask() {
			@Override
			public boolean run() {
				for(final var entry : eventsToAddByScope().entrySet()) {
					final var scopeModel = entry.getKey();
					final var scopes = scopeService.getAllIncludingRemoved(scopeModel);
					final var eventModels = entry.getValue().stream().map(scopeModel::getEventModel).toList();
					System.out.println(String.format("Adding event %s to scopes %s", entry.getValue(), entry.getKey()));
					for(final Scope scope : scopes) {
						addEventsToScope(context, scope, eventModels);
					}
				}
				return true;
			}

			@Override
			public String description() {
				return DBUpdateConfig.this.description();
			}
		};
	}

	private MigrationTask addDatasets(final DatabaseActionContext context) {
		return new MigrationTask() {
			@Override
			public boolean run() {
				for(final var entry : datasetsToAddByEvent().entrySet()) {
					final var eventModel = entry.getKey();
					final var events = eventService.getAllIncludingRemoved(eventModel);
					final var datasetModels = entry.getValue().stream().map(study::getDatasetModel).toList();
					System.out.println(String.format("Adding dataset %s to events %s", entry.getValue(), entry.getKey()));
					for(final Event event : events) {
						final var scope = scopeService.get(event);
						addDatasetsToEvent(context, scope, event, datasetModels);
					}
				}
				return true;
			}

			@Override
			public String description() {
				return DBUpdateConfig.this.description();
			}
		};
	}

	private MigrationTask addFields(final DatabaseActionContext context) {
		return new MigrationTask() {
			@Override
			public boolean run() {
				for(final var entry : fieldsToAddByDataset().entrySet()) {
					final var datasetModel = entry.getKey();
					final var datasets = datasetService.getAllIncludingRemoved(Collections.singleton(datasetModel));
					final var attributes = entry.getValue().stream().map(datasetModel::getFieldModel).toList();
					System.out.println(String.format("Adding field %s to dataset %s", entry.getValue(), entry.getKey()));
					for(final Dataset dataset : datasets) {
						final var scope = scopeService.get(dataset);
						final var event = eventService.get(dataset);
						addFieldsToDataset(context, scope, event, dataset, attributes);
					}
				}
				return true;
			}

			@Override
			public String description() {
				return DBUpdateConfig.this.description();
			}
		};
	}

}
