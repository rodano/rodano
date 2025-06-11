package ch.rodano.test;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.core.helpers.ScopeCreatorService;
import ch.rodano.core.helpers.builder.ScopeBuilder;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;

@Service
public class TestHelperService {

	private final StudyService studyService;
	private final ScopeService scopeService;
	private final ScopeCreatorService scopeCreatorService;

	public TestHelperService(
		final StudyService studyService,
		final ScopeService scopeService,
		final ScopeCreatorService scopeCreatorService
	) {
		this.studyService = studyService;
		this.scopeService = scopeService;
		this.scopeCreatorService = scopeCreatorService;
	}

	public Scope createCountry(final DatabaseActionContext context) {
		final var study = studyService.getStudy();
		final var countryScopeModel = study.getScopeModel("COUNTRY");
		final var rootScope = scopeService.getRootScope();
		final var scopeCode = scopeService.getNextCode(countryScopeModel, rootScope);
		return scopeCreatorService.createScope(new ScopeBuilder(context).createScope(countryScopeModel, rootScope, scopeCode));
	}

	public Scope createCenter(final Scope parentScope, final DatabaseActionContext context) {
		final var study = studyService.getStudy();
		final var centerScopeModel = study.getScopeModel("CENTER");
		final var scopeCode = scopeService.getNextCode(centerScopeModel, parentScope);
		return scopeCreatorService.createScope(new ScopeBuilder(context).createScope(centerScopeModel, parentScope, scopeCode));
	}

	public Scope createCenter(final DatabaseActionContext context) {
		final var study = studyService.getStudy();
		final var centerScopeModel = study.getScopeModel("CENTER");
		final var parentModel = centerScopeModel.getDefaultParent();
		final var anyParent = scopeService.getAll(parentModel).get(0);

		return createCenter(anyParent, context);
	}

	public Scope createPatient(final Scope parentScope, final DatabaseActionContext context) {
		final var study = studyService.getStudy();

		final var patientScopeModel = study.getScopeModel("PATIENT");
		final var scopeCode = scopeService.getNextCode(patientScopeModel, parentScope);
		return scopeCreatorService.createScope(new ScopeBuilder(context).createScope(patientScopeModel, parentScope, scopeCode));
	}

	public List<Scope> createPatients(
		final int quantity,
		final Scope parentScope,
		final DatabaseActionContext context
	) {
		return IntStream.range(0, quantity)
			.mapToObj(_ -> createPatient(parentScope, context))
			.toList();
	}

	public EventModel getBaselineEventModel() {
		return studyService.getStudy().getScopeModel("PATIENT").getEventModel("BASELINE");
	}
}
