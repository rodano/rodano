package ch.rodano.core.services.bll;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.workflow.WorkflowStatusSearch;
import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowableEntity;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.payment.PaymentDAOService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;
import ch.rodano.test.TestHelperService;

import static ch.rodano.core.model.jooq.Tables.WORKFLOW_STATUS;
import static ch.rodano.core.model.jooq.Tables.WORKFLOW_STATUS_AUDIT;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
@Transactional
public class WorkflowStatusServiceTest extends DatabaseTest {

	@Autowired
	private DSLContext create;

	@Autowired
	private ScopeService scopeService;

	@Autowired
	private WorkflowStatusService workflowStatusService;

	@Autowired
	private EventService eventService;

	@Autowired
	private FieldService fieldService;

	@Autowired
	private FormService formService;

	@Autowired
	private PaymentDAOService paymentDAOService;

	@Autowired
	private TestHelperService testHelperService;

	@Test
	@DisplayName("Create workflow")
	public void createWorkflowTest() {
		final var workflow = studyService.getStudy().getWorkflow("CENTER_STATUS");
		final var center = testHelperService.createCenter(context);
		final var family = new DataFamily(center);

		workflow.setInitialStateId("OPEN");
		final var openWorkflowStatus = workflowStatusService.create(family, center, workflow, null, context, TEST_RATIONALE);
		assertEquals(openWorkflowStatus.getStateId(), "OPEN");

		workflow.setInitialStateId("PENDING");
		final var pendingWorkflowStatus = workflowStatusService.create(family, center, workflow, null, context, TEST_RATIONALE);
		assertEquals(pendingWorkflowStatus.getStateId(), "PENDING");
	}

	@Test
	@DisplayName("Delete workflow status")
	public void deleteWorkflowTest() {
		final var workflow = studyService.getStudy().getWorkflow("CENTER_STATUS");

		// Create a scope and count the number of workflow statuses on the scope
		final var center = testHelperService.createCenter(context);
		final var family = new DataFamily(center);
		final var workflowsOnScope = workflowStatusService.getAll(center).size();

		// Create a workflow on the scope
		final var workflowStatus = workflowStatusService.create(
			family,
			center,
			workflow,
			null,
			context,
			TEST_RATIONALE
		);
		assertEquals(workflowsOnScope + 1, workflowStatusService.getAll(center).size());

		// Delete the created workflow
		workflowStatusService.delete(
			family,
			workflowStatus,
			context,
			TEST_RATIONALE
		);

		//verify that it has been deleted correctly
		final var workflowStatuses = workflowStatusService.getAll(center);
		assertAll(
			() -> assertEquals(workflowsOnScope, workflowStatuses.size()),
			() -> assertTrue(workflowStatuses.stream().noneMatch(ws -> ws.getPk().equals(workflowStatus.getPk())))
		);
	}

	@Test
	@DisplayName("Search on scopes")
	public void searchOnScopesTest() {
		//create a few random scopes
		final var center = testHelperService.createCenter(context);
		final var scopes = new ArrayList<>(
			testHelperService.createPatients(2, center, context)
		);
		scopes.add(center);

		//find every scope-related workflow statuses created by this operation
		final List<WorkflowStatus> recordedWorkflowStatuses = new ArrayList<>();
		for(final Scope scope : scopes) {
			for(final var form : formService.getAll(scope)) {
				recordedWorkflowStatuses.addAll(workflowStatusService.getAll(form));
			}
			recordedWorkflowStatuses.addAll(workflowStatusService.getAll(scope));
			for(final var event : eventService.getAll(scope)) {
				recordedWorkflowStatuses.addAll(workflowStatusService.getAll(event));
				for(final var form : formService.getAll(event)) {
					recordedWorkflowStatuses.addAll(workflowStatusService.getAll(form));
				}
				final var fields = fieldService.getAll(scope, Optional.of(event));
				for(final var field : fields) {
					recordedWorkflowStatuses.addAll(workflowStatusService.getAll(field));
				}
			}
		}

		//search for workflow statuses related to the scopes
		final var allWorkflowIds = studyService.getStudy().getWorkflows().stream()
			.map(Workflow::getId)
			.toList();
		final var scopePks = scopes.stream()
			.map(Scope::getPk)
			.toList();
		final var predicate = new WorkflowStatusSearch()
			.setAncestorScopePks(List.of(center.getPk()))
			.setWorkflowIds(allWorkflowIds)
			.setScopePks(Optional.of(scopePks))
			.setPageSize(Optional.of(1000))
			.setPageIndex(Optional.of(0));
		final var foundWorkflowStatuses = workflowStatusService.search(predicate);

		//the results should be the same
		assertEquals(recordedWorkflowStatuses.size(), foundWorkflowStatuses.getPaging().total());

		final var recordedWorkflowStatusPks = recordedWorkflowStatuses.stream()
			.map(WorkflowStatus::getPk)
			.sorted()
			.toArray();
		final var foundWorkflowStatusPks = foundWorkflowStatuses.getObjects().stream()
			.map(WorkflowStatus::getPk)
			.sorted()
			.toArray();
		assertArrayEquals(recordedWorkflowStatusPks, foundWorkflowStatusPks);
	}

	@Test
	@DisplayName("Search on events")
	public void searchOnEventsTest() {
		final var center = testHelperService.createCenter(context);
		final var scopes = testHelperService.createPatients(2, center, context);

		final var eventPks = new ArrayList<Long>();

		//find every event-related workflow statuses created by this operation
		final List<WorkflowStatus> recordedWorkflowStatuses = new ArrayList<>();
		for(final Scope scope : scopes) {
			for(final var event : eventService.getAll(scope)) {
				eventPks.add(event.getPk());
				recordedWorkflowStatuses.addAll(workflowStatusService.getAll(event));
				for(final var form : formService.getAll(event)) {
					recordedWorkflowStatuses.addAll(workflowStatusService.getAll(form));
				}
				final var fields = fieldService.getAll(scope, Optional.of(event));
				for(final var field : fields) {
					recordedWorkflowStatuses.addAll(workflowStatusService.getAll(field));
				}
			}
		}

		//search for workflow statuses related to the events
		final var allWorkflowIds = studyService.getStudy().getWorkflows().stream()
			.map(Workflow::getId)
			.toList();
		final var predicate = new WorkflowStatusSearch()
			.setAncestorScopePks(List.of(center.getPk()))
			.setWorkflowIds(allWorkflowIds)
			.setEventPks(Optional.of(eventPks))
			.setPageSize(Optional.of(1000))
			.setPageIndex(Optional.of(0));
		final var foundWorkflowStatuses = workflowStatusService.search(predicate);

		//the results should be the same
		assertEquals(recordedWorkflowStatuses.size(), foundWorkflowStatuses.getPaging().total());

		final var recordedWorkflowStatusPks = recordedWorkflowStatuses.stream()
			.map(WorkflowStatus::getPk)
			.sorted()
			.toArray();
		final var foundWorkflowStatusPks = foundWorkflowStatuses.getObjects().stream()
			.map(WorkflowStatus::getPk)
			.sorted()
			.toArray();
		assertArrayEquals(recordedWorkflowStatusPks, foundWorkflowStatusPks);
	}

	@Test
	@DisplayName("Search on ancestor scopes")
	public void searchAncestorScopesTest() {
		//create a center and several patients on it
		final var center = testHelperService.createCenter(context);
		final var patients = testHelperService.createPatients(3, center, context);

		//get all the workflows on entities descending from this scope
		final var patientPks = patients.stream()
			.map(Scope::getPk)
			.toList();
		final var queriedWorkflowStatuses = create
			.select(WORKFLOW_STATUS.PK)
			.from(WORKFLOW_STATUS)
			.where(
				WORKFLOW_STATUS.SCOPE_FK.eq(center.getPk())
					.or(WORKFLOW_STATUS.SCOPE_FK.in(patientPks))
			)
			.orderBy(WORKFLOW_STATUS.PK)
			.fetch(WORKFLOW_STATUS.PK);

		//search for workflow statuses
		final var allWorkflowIds = studyService.getStudy().getWorkflows().stream()
			.map(Workflow::getId)
			.toList();
		final var predicate = new WorkflowStatusSearch()
			.setAncestorScopePks(Collections.singletonList(center.getPk()))
			.setWorkflowIds(allWorkflowIds)
			.setPageSize(Optional.of(1000))
			.setPageIndex(Optional.of(0));
		final var foundWorkflowStatuses = workflowStatusService.search(predicate);

		//check that the found workflow status are correct
		assertEquals(queriedWorkflowStatuses.size(), foundWorkflowStatuses.getPaging().total());

		final var foundWorkflowStatusPks = foundWorkflowStatuses.getObjects().stream()
			.map(WorkflowStatus::getPk)
			.sorted()
			.toArray();
		assertArrayEquals(queriedWorkflowStatuses.toArray(), foundWorkflowStatusPks);
	}

	@Test
	@DisplayName("Filter expected events when searching")
	public void filterExpectedEventsInSearchTest() {
		final var center = testHelperService.createCenter(context);
		final var patient = testHelperService.createPatient(center, context);
		final var events = eventService.getAll(patient).stream()
			.sorted()
			.toList();

		//get the baseline event
		final var baselineEvent = events.stream()
			.filter(event -> event.getEventModelId().equals("BASELINE"))
			.findFirst()
			.orElseThrow();

		//set the date for the first event
		final var baselineEventWithDate = eventService.updateDate(
			patient,
			baselineEvent,
			ZonedDateTime.now(),
			context,
			TEST_RATIONALE
		);

		//search for the event workflows and filter the expected events
		final var allWorkflowIds = studyService.getStudy().getWorkflows().stream()
			.map(Workflow::getId)
			.toList();
		final var eventPKs = events.stream()
			.map(Event::getPk)
			.toList();
		final var predicate = new WorkflowStatusSearch()
			.setAncestorScopePks(Collections.singletonList(center.getPk()))
			.setWorkflowIds(allWorkflowIds)
			.setEventPks(Optional.of(eventPKs))
			.setFilterExpectedEvents(Optional.of(true));
		final var foundWorkflowStatuses = workflowStatusService.search(predicate);

		//verify that the workflows returned belong only to the already-happened event
		assertAll(
			() -> assertEquals(6, foundWorkflowStatuses.getPaging().total()),
			() -> assertTrue(
				foundWorkflowStatuses.getObjects().stream()
					.allMatch(ws -> Objects.equals(ws.getEventFk(), baselineEventWithDate.getPk()))
			)
		);
	}

	@Test
	@DisplayName("Create all workflow statuses on a workflowable")
	public void createAllWSTest() {
		//create a scope and strip it of all its workflow statuses
		final var centerScope = testHelperService.createCenter(context);
		final var patientScope = testHelperService.createPatient(centerScope, context);
		create
			.delete(WORKFLOW_STATUS_AUDIT)
			.where(WORKFLOW_STATUS_AUDIT.SCOPE_FK.eq(patientScope.getPk()))
			.execute();
		create
			.delete(WORKFLOW_STATUS)
			.where(WORKFLOW_STATUS.SCOPE_FK.eq(patientScope.getPk()))
			.execute();

		//create all workflow statuses on scope
		final var family = new DataFamily(patientScope);
		workflowStatusService.createAll(
			family,
			patientScope,
			null,
			context,
			TEST_RATIONALE
		);

		//verify that all the mandatory workflow statuses have been created
		final var scopeWorkflows = studyService.getStudy().getScopeModel(patientScope.getScopeModelId()).getWorkflows();
		final var scopeWorkflowIds = scopeWorkflows.stream()
			.filter(Workflow::isMandatory)
			.map(Workflow::getId)
			.toArray();
		final var createdWSIds = workflowStatusService.getAll(patientScope).stream()
			.map(WorkflowStatus::getWorkflowId)
			.toArray();

		assertArrayEquals(scopeWorkflowIds, createdWSIds);
	}

	@Test
	@DisplayName("Reset mandatory and delete the rest of the workflow statuses")
	public void resetMandatoryAndDeleteTheRestTest() {
		//create a patient scope
		final var centerScope = testHelperService.createCenter(context);
		final var patientScope = testHelperService.createPatient(centerScope, context);
		final var patientStatus = studyService.getStudy().getWorkflow("PATIENT_STATUS");

		//create a non-mandatory workflow status on the scope
		// TODO not possible to do right now since there are no scopes that have a mandatory and non-mandatory workflow models on it
		/*
		final var queryWorkflow = studyService.getStudy().getWorkflow("QUERY");
		final var query = workflowStatusService.create(
			patientScope,
			queryWorkflow,
			null,
			context,
			TEST_RATIONALE
		);
		 */

		//update the state of a mandatory workflow status
		final var patientStatusWS = workflowStatusService.getMostRecent(patientScope, patientStatus).orElseThrow();

		final var family = new DataFamily(patientScope);
		workflowStatusService.updateState(
			family,
			patientStatusWS,
			patientStatusWS.getWorkflow().getState("ONGOING"),
			null,
			context,
			TEST_RATIONALE
		);

		//reset mandatory and delete the rest
		workflowStatusService.resetMandatoryAndDeleteTheRest(
			family,
			patientScope,
			context,
			TEST_RATIONALE
		);

		//verify that the mandatory WS are reset and that other WS are deleted
		assertTrue(
			workflowStatusService.getAll(patientScope).stream()
				.allMatch(workflowStatus -> workflowStatus.getWorkflow().isMandatory() && workflowStatus.getStateId().equals(workflowStatus.getWorkflow().getInitialStateId()))
		);

		// TODO not possible to do right now since there are no scopes that have a mandatory and non-mandatory workflow models on it
		//assertTrue(workflowStatusService.getAll(patientScope, "QUERY").isEmpty()),
	}

	@Test
	@DisplayName("Update state of workflows")
	public void updateStateTest() {
		final var study = studyService.getStudy();

		// Retrieve workflows
		final var dataManagementStatus = study.getWorkflow("DATA_MANAGEMENT_STATUS");
		final var patientStatus = study.getWorkflow("PATIENT_STATUS");
		final var formReporting = study.getWorkflow("FORM_REPORTING");

		// Create new center and a new patient
		final var center = testHelperService.createCenter(context);
		final var patient = testHelperService.createPatient(center, context);

		final var currentWorkflowStatus = workflowStatusService.getMostRecent(patient, patientStatus);

		//scope
		assertAll(
			"Retrieve state and actions",
			() -> assertTrue(currentWorkflowStatus.isPresent()),
			() -> assertEquals(patientStatus.getInitialStateId(), currentWorkflowStatus.get().getStateId()),
			() -> assertTrue(currentWorkflowStatus.get().getState().getPossibleActions().isEmpty())
		);

		//event
		final var baselineVisit = eventService.get(patient, getBaselineEvent(), 0);
		final var family = new DataFamily(patient, baselineVisit);

		//retrieve state and actions
		assertEquals(dataManagementStatus.getInitialStateId(), workflowStatusService.getMostRecent(baselineVisit, dataManagementStatus).get().getStateId());

		//change state
		final var dataManagementWS = workflowStatusService.getMostRecent(baselineVisit, dataManagementStatus);
		workflowStatusService.updateState(family, dataManagementWS.get(), dataManagementStatus.getState("REVIEWED"), Collections.emptyMap(), context, "Change to reviewed");

		final var reviewedWorkflowStatus = workflowStatusService.getMostRecent(baselineVisit, dataManagementStatus);

		assertAll(
			"Change state",
			() -> assertTrue(reviewedWorkflowStatus.isPresent()),
			() -> assertEquals("REVIEWED", reviewedWorkflowStatus.get().getStateId()),
			() -> assertEquals(1, reviewedWorkflowStatus.get().getState().getPossibleActions().size())
		);

		workflowStatusService.updateState(family, dataManagementWS.get(), dataManagementStatus.getState("READY_TO_REVIEW"), Collections.emptyMap(), context, "Change to unreviewed");

		final var readyToReviewWorkflowStatus = workflowStatusService.getMostRecent(baselineVisit, dataManagementStatus);

		assertAll(
			() -> assertTrue(readyToReviewWorkflowStatus.isPresent()),
			() -> assertEquals("READY_TO_REVIEW", readyToReviewWorkflowStatus.get().getStateId()),
			() -> assertEquals(1, readyToReviewWorkflowStatus.get().getState().getPossibleActions().size())
		);

		//form
		final var form = formService.get(baselineVisit, "STUDY_ENTRY");

		//get state
		assertEquals(formReporting.getInitialStateId(), workflowStatusService.getMostRecent(form, formReporting).get().getStateId());
	}

	@Test
	@DisplayName("Workflowable is found correctly")
	public void getWorkflowableTest() {
		//retrieve workflows
		final var dataManagementStatus = studyService.getStudy().getWorkflow("DATA_MANAGEMENT_STATUS");
		final var formReporting = studyService.getStudy().getWorkflow("FORM_REPORTING");

		// Create a center and a patient
		final var center = testHelperService.createCenter(context);
		final var patient = testHelperService.createPatient(center, context);
		final var baselineEvent = eventService.get(patient, getBaselineEvent(), 0);

		final var dataManagementWS = workflowStatusService.getMostRecent(baselineEvent, dataManagementStatus);
		final var visitWorkflowable = workflowStatusService.getWorkflowable(dataManagementWS.get());

		assertAll(
			"Event workflowable relations",
			() -> assertEquals(baselineEvent, visitWorkflowable),
			() -> assertEquals(WorkflowableEntity.EVENT, dataManagementWS.get().getWorkflowableType()),
			() -> assertEquals(patient, scopeService.get(dataManagementWS.get())),
			() -> assertEquals(baselineEvent, eventService.get(dataManagementWS.get()).get()),
			() -> assertNull(dataManagementWS.get().getFormFk())
		);
		// this may not be good : former .getScope, .getVisit and .getForm WorkflowStatus methods had a cache & checked on non-null foreign key
		// upon empty cache and null key, returned null : ok behavior ???

		//form
		final var form = formService.get(baselineEvent, "STUDY_ENTRY");
		final var formReportingWS = workflowStatusService.getMostRecent(form, formReporting);
		final var formWorkflowable = workflowStatusService.getWorkflowable(formReportingWS.get());

		assertAll(
			"Form workflowable relations",
			() -> assertEquals(form, formWorkflowable),
			() -> assertEquals(WorkflowableEntity.FORM, formReportingWS.get().getWorkflowableType()),
			() -> assertEquals(baselineEvent, eventService.get(dataManagementWS.get()).get())
		);
	}

	@Test
	@DisplayName("Payments are correctly associated with workflows")
	public void workflowPaymentTest() {
		//retrieve workflows
		final var dataManagementStatus = studyService.getStudy().getWorkflow("DATA_MANAGEMENT_STATUS");

		final var center = testHelperService.createCenter(context);
		final var patient = testHelperService.createPatient(center, context);

		//event
		final var event = eventService.get(patient, getBaselineEvent(), 0);
		final var eventStatus = workflowStatusService.getMostRecent(event, dataManagementStatus);

		assertAll(
			() -> assertTrue(eventStatus.isPresent()),
			() -> assertEquals(paymentDAOService.getPaymentsByWorkflowStatusFk(eventStatus.get().getPk()).size(), 0)
		);
	}

	private EventModel getBaselineEvent() {
		return studyService.getStudy().getScopeModel("PATIENT").getEventModel("BASELINE");
	}
}
