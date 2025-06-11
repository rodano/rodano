package ch.rodano.core.database.migrations.scripts;

import java.util.List;
import java.util.Map;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.database.migrations.MigrationBean;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.audit.AuditActionService;

@MigrationBean
public class DBUpdate2000 extends DBUpdateConfig {

	public DBUpdate2000(
		final AuditActionService auditActionService,
		final ScopeService scopeService,
		final EventService eventService,
		final StudyService studyService,
		final DatasetService datasetService,
		final FieldService fieldService
	) {
		super(auditActionService, scopeService, eventService, studyService, datasetService, fieldService);
	}

	private static final String DESCRIPTION = "Fake implementation of the migration that creates events, datasets and/or fields";
	private static final String CONTEXT = "Add new events, datasets and fields following the update of the protocol";

	@Override
	protected Double migrationTaskNumber() {
		return 2000D;
	}

	@Override
	protected String description() {
		return DESCRIPTION;
	}

	@Override
	protected String context() {
		return CONTEXT;
	}

	@Override
	protected Map<ScopeModel, List<String>> eventsToAddByScope() {
		return Map.of(study.getScopeModel("PATIENT"), List.of("TELEPHONE_VISIT"));
	}

	@Override
	protected Map<EventModel, List<String>> datasetsToAddByEvent() {
		return Map.of(study.getScopeModel("PATIENT").getEventModel("BASELINE"), List.of("EQ5D"));
	}

	@Override
	protected Map<DatasetModel, List<String>> fieldsToAddByDataset() {
		return Map.of(study.getDatasetModel("VISIT_DOCUMENTATION"), List.of("EDSS_SCORE", "EDSS_STATUS"));
	}
}
