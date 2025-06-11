package ch.rodano.core.services.bll;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.helpers.ScopeCreatorService;
import ch.rodano.core.helpers.builder.ScopeBuilder;
import ch.rodano.core.model.exception.LockedObjectException;
import ch.rodano.core.model.exception.WrongDataConditionException;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.form.FormContentService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.plugin.validator.exception.BadlyFormattedValue;
import ch.rodano.core.services.plugin.validator.exception.InvalidValueException;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
@Transactional
public class EventServiceTest extends DatabaseTest {

	@Autowired
	private ScopeService scopeService;

	@Autowired
	private ScopeDAOService scopeDAOService;

	@Autowired
	private EventService eventService;

	@Autowired
	private DatasetService datasetService;

	@Autowired
	private FieldService fieldService;

	@Autowired
	private FormService formService;

	@Autowired
	private FormContentService formContentService;

	@Autowired
	private ScopeCreatorService scopeCreatorService;

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	// TODO transform into a event update test
	@Test
	@DisplayName("Event update works")
	public void blockingEvents() {
		final var patient = createPatient();

		final var event = eventService.get(patient, getVisit6Event(), 0);
		event.setBlocking(true);
		final var newDate = ZonedDateTime.now();
		event.setExpectedDate(newDate);
		eventService.save(event, context, TEST_RATIONALE);
		assertTrue(event.getBlocking());
		assertEquals(newDate, event.getExpectedDate());

		event.setBlocking(false);
		event.setDate(newDate);
		eventService.save(event, context, TEST_RATIONALE);
		assertFalse(event.getBlocking());
		assertEquals(newDate, event.getDate());
	}

	@Test
	@DisplayName("Event lock prevents event modifications")
	public void eventLockWorks() {
		// create a patient and get its event
		final var patient = createPatient();
		final var event = eventService.get(patient, getVisit6Event(), 0);

		// lock the event
		eventService.lock(patient, event, context, TEST_RATIONALE);

		// try to modify it
		event.setDate(ZonedDateTime.now());

		// the operation must fail
		assertThrows(
			LockedObjectException.class,
			() -> eventService.save(event, context, TEST_RATIONALE),
			"Event could be modified, even though it was locked"
		);

		// unlock the event
		eventService.unlock(patient, event, context, TEST_RATIONALE);

		// try to modify it
		final var newDate = ZonedDateTime.now();
		event.setDate(newDate);
		eventService.save(event, context, TEST_RATIONALE);

		// must work
		assertEquals(newDate, event.getDate());
	}

	@Test
	@DisplayName("All the mandatory events are created upon patient creation")
	public void mandatoryEventsAreCreatedUponScopeCreation() {
		final var patientModel = studyService.getStudy().getScopeModel("PATIENT");
		final var center = scopeDAOService.getScopeByCode("FR-01");

		final var patientCode = scopeService.getNextCode(patientModel, center);
		final var patient = scopeCreatorService.createScope(new ScopeBuilder(context).createScope(patientModel, center, patientCode));

		//check that baseline event cannot be added again
		assertFalse(eventService.getEventModels(patient).contains(getBaselineEvent()));
		assertThrows(
			WrongDataConditionException.class,
			() -> eventService.create(patient, getBaselineEvent(), context, "Test"), "We manage to add two times the same event group whereas its max occurrence is 1"
		);

		assertAll(
			() -> assertEquals(5, eventService.getAll(patient).size()),
			() -> assertEquals("BASELINE", eventService.get(patient, getBaselineEvent(), 0).getEventModelId()),
			() -> assertEquals("TERMINATION_VISIT", eventService.get(patient, getTerminationEvent(), 0).getEventModelId())
		);
	}

	@Test
	@DisplayName("Event plannification (expected events) works")
	public void eventExpected() {
		final var center = scopeDAOService.getScopeByCode("FR-01");
		final var patientModel = studyService.getStudy().getScopeModel("PATIENT");

		final var patientCode = scopeService.getNextCode(patientModel, center);
		final var patient = scopeCreatorService.createScope(new ScopeBuilder(context).createScope(patientModel, center, patientCode));

		//baseline event has a date when created because it has no deadline
		final var baselineEvent = eventService.get(patient, getBaselineEvent(), 0);
		assertFalse(baselineEvent.isExpected(), "Baseline event is not expected");

		//6-months event has no date when created because it has no deadline
		final var event6Months = eventService.get(patient, getVisit6Event(), 0);
		assertTrue(event6Months.isExpected(), "Event 6 months is expected");
	}

	@Test
	@DisplayName("Event ordering and navigation work")
	public void eventOrderingAndNavigation() {
		final var center = scopeDAOService.getScopeByCode("FR-01");
		final var patientModel = studyService.getStudy().getScopeModel("PATIENT");

		final var patientCode = scopeService.getNextCode(patientModel, center);
		final var patient = scopeCreatorService.createScope(new ScopeBuilder(context).createScope(patientModel, center, patientCode));

		assertEquals(eventService.get(patient, getVisit6Event(), 0).getEventModelId(), "VISIT_6");

		//first event
		final var firstEvent = eventService.get(patient, getBaselineEvent(), 0);

		//try to retrieve the event that precedes the first event
		assertTrue(eventService.getPrevious(firstEvent).isEmpty());

		final var secondEvent = eventService.getNext(firstEvent).get();
		assertEquals("VISIT_6", secondEvent.getEventModelId(), "Check next in group");

		final var thirdEvent = eventService.getNext(secondEvent).get();
		assertEquals("VISIT_12", thirdEvent.getEventModelId(), "Check next next id");

		assertEquals(firstEvent.getId(), eventService.getPrevious(secondEvent).get().getId(), "Check previous");

		//last event
		final var last = eventService.get(patient, getTerminationEvent(), 0);

		//try to retrieve the event that follows the last event
		assertTrue(eventService.getNext(last).isEmpty());
		assertEquals("TERMINATION_VISIT", last.getEventModelId());
	}

	@Test
	@DisplayName("Event labels are generated correctly")
	public void eventLabels() throws InvalidValueException, BadlyFormattedValue {
		final var center = scopeDAOService.getScopeByCode("FR-01");
		final var patientModel = studyService.getStudy().getScopeModel("PATIENT");
		final var eventModel = getBaselineEvent();
		final var datasetModel = studyService.getStudy().getDatasetModel("STUDY_ENTRY");
		final var fieldModel = datasetModel.getFieldModel("DATE_OF_ENROLLMENT");

		final var patientCode = scopeService.getNextCode(patientModel, center);
		final var patient = scopeCreatorService.createScope(new ScopeBuilder(context).createScope(patientModel, center, patientCode));

		// Get the baseline event
		final var event = eventService.get(patient, eventModel, 0);
		assertEquals("Baseline visit", eventService.getLabel(patient, event));

		//TODO it's not a good idea to change the test configuration, that is a singleton
		eventModel.setLabelPattern("${shortname} - ${date}");
		assertEquals(String.format("Baseline visit - %s", event.getDateOrExpectedDate().format(DATE_FORMAT)), eventService.getLabel(patient, event));

		eventModel.setLabelPattern("${shortname} / ${groupNumber}");
		assertEquals("Baseline visit / 1", eventService.getLabel(patient, event));

		eventModel.setLabelPattern("Test event ${}");
		assertEquals("Test event ${}", eventService.getLabel(patient, event));

		eventModel.setLabelPattern("Test event ${");
		assertEquals("Test event ${", eventService.getLabel(patient, event));

		eventModel.setLabelPattern("Enrollment - ${datasetModelId:STUDY_ENTRY-fieldModelId:DATE_OF_ENROLLMENT}");
		assertEquals("Enrollment - ", eventService.getLabel(patient, event));

		final var dataset = datasetService.get(event, datasetModel);
		final var field = fieldService.get(dataset, fieldModel);
		fieldService.updateValue(patient, Optional.of(event), dataset, field, "03.01.2024", context, "Test");
		assertEquals("Enrollment - 03.01.2024", eventService.getLabel(patient, event));
	}

	@Test
	@DisplayName("Event dates are set and calculated correctly")
	public void eventDatesSettingAndCalculation() {
		final var center = scopeDAOService.getScopeByCode("FR-01");
		final var patientModel = studyService.getStudy().getScopeModel("PATIENT");

		final var patientCode = scopeService.getNextCode(patientModel, center);
		final var patient = scopeCreatorService.createScope(new ScopeBuilder(context).createScope(patientModel, center, patientCode));

		// Get the baseline and the termination events
		final var baselineVisit = eventService.get(patient, getBaselineEvent(), 0);
		final var followUpVisit = eventService.getNext(baselineVisit).get();
		final var terminationVisit = eventService.get(patient, getTerminationEvent(), 0);

		// Verify that the next event after baseline is set correctly according to the deadline
		// In the case of test study, we round off the dates to the first day of the month. If we do not do this, the difference in months could
		// be interpreted incorrectly when 6 months are added to the 31st of the month.
		// e.g. The date of the first event is 2022.03.31, the date of the second event is 6 months later and is calculated as 2022.09.30
		// BUT ChronoUnit.MONTHS.between them is calculated as 5, which makes the test case fail.
		final var followUpVisitEventModel = followUpVisit.getEventModel();
		final var baselineDate = baselineVisit.getDateOrExpectedDate();
		final var followUpVisitDate = followUpVisit.getDateOrExpectedDate();
		assertEquals(
			followUpVisitEventModel.getDeadline().longValue(),
			followUpVisitEventModel.getDeadlineUnit().between(
				baselineDate.withDayOfMonth(1),
				followUpVisitDate.withDayOfMonth(1)
			)
		);

		// Set date of the baseline event to five years ago
		final var fiveYearsAgo = ZonedDateTime.now().minusYears(5).withNano(0);
		eventService.updateDate(patient, baselineVisit, fiveYearsAgo, context, TEST_RATIONALE);
		assertNull(baselineVisit.getEndDate());
		assertEquals(fiveYearsAgo, baselineVisit.getDateOrExpectedDate());

		// Reset dates of all other visits
		eventService.resetDates(patient, context, TEST_RATIONALE);

		// Check that everything stays correct after the event date reset
		assertFalse(baselineVisit.isExpected());
		assertTrue(terminationVisit.isExpected());

		// We use the same date trick as before
		final var newBaselineDate = baselineVisit.getDate();
		final var newFollowUpDate = followUpVisit.getExpectedDate();
		assertEquals(
			followUpVisitEventModel.getDeadline().longValue(),
			followUpVisitEventModel.getDeadlineUnit().between(
				newBaselineDate.withDayOfMonth(1),
				newFollowUpDate.withDayOfMonth(1)
			)
		);
		assertEquals(newFollowUpDate, fiveYearsAgo.plusMonths(6));
	}

	//TODO move this in the dataset service test
	@Test
	@DisplayName("Dataset creation and retrieval work")
	public void datasets() {
		final var study = studyService.getStudy();

		final var center = scopeDAOService.getScopeByCode("FR-01");
		final var patientModel = study.getScopeModel("PATIENT");
		final var visitDocModel = study.getDatasetModel("VISIT_DOCUMENTATION");

		final var patientCode = scopeService.getNextCode(patientModel, center);
		final var patient = scopeCreatorService.createScope(new ScopeBuilder(context).createScope(patientModel, center, patientCode));

		final var first = eventService.get(patient, getBaselineEvent(), 0);

		//datasets are created
		assertAll(
			"Datasets are already created",
			() -> assertEquals(7, datasetService.getAll(first).size())
		);

		assertEquals(1, datasetService.getAllIncludingRemoved(first, Collections.singleton(visitDocModel)).size());

		//delete automatically created dataset
		datasetService.delete(patient, Optional.of(first), datasetService.get(first, visitDocModel), context, "Delete from tests");

		assertAll(
			"There are no more datasets",
			() -> assertEquals(6, datasetService.getAll(first).size())
		);

		//create dataset again, this time manually
		datasetService.create(patient, first, visitDocModel, context, TEST_RATIONALE);
		assertEquals(7, datasetService.getAll(first).size());
	}

	@Test
	@DisplayName("Event progress is calculated correctly")
	public void progression() throws Exception {
		final var study = studyService.getStudy();

		final var center = scopeDAOService.getScopeByCode("FR-01");
		final var patientModel = study.getScopeModel("PATIENT");
		final var studyEntryDatasetModel = study.getDatasetModel("STUDY_ENTRY");

		final var patientCode = scopeService.getNextCode(patientModel, center);
		final var patient = scopeCreatorService.createScope(new ScopeBuilder(context).createScope(patientModel, center, patientCode));

		final var baselineVisit = eventService.get(patient, getBaselineEvent(), 0);
		final var dataset = datasetService.get(baselineVisit, studyEntryDatasetModel);

		//progression of baseline
		final var progression = eventService.getProgression(baselineVisit);
		//there are 2 default values in this event, but this does not count here
		assertEquals(0, progression.getProgress());
		assertTrue(progression.isNull());
		assertFalse(progression.isComplete());

		//progression of all empty visits
		for(final var v : eventService.getAllIncludingRemoved(patient)) {
			if(!v.equals(baselineVisit)) {
				final var visitProgression = eventService.getProgression(v);
				assertTrue(visitProgression.isNull());
				assertFalse(visitProgression.isComplete());
			}
		}

		//check number of fields in first form
		final var form = formService.get(baselineVisit, "STUDY_ENTRY");
		final var formContent = formContentService.generateFormContent(patient, Optional.of(baselineVisit), form);
		assertEquals(4, formContent.getAllNonDeletedFields().size());

		//update one field and check progression
		final var consentFieldModel = dataset.getDatasetModel().getFieldModel("CONSENT_CONFIRMED_BY");
		fieldService.updateValue(
			patient,
			Optional.of(baselineVisit),
			dataset,
			fieldService.get(dataset, consentFieldModel),
			"Doctor Jones",
			context,
			TEST_RATIONALE
		);

		final var progressAfterOneValue = eventService.getProgression(baselineVisit);
		assertEquals(47, progressAfterOneValue.getTotal());
		assertEquals(1, progressAfterOneValue.getProgress());
		assertFalse(progressAfterOneValue.isNull());
		assertFalse(progressAfterOneValue.isComplete());

		// update another field and check progress
		final var eligibilityFieldModel = dataset.getDatasetModel().getFieldModel("ELIGIBILITY_CRITERIA");
		fieldService.updateValue(
			patient,
			Optional.of(baselineVisit),
			dataset,
			fieldService.get(dataset, eligibilityFieldModel),
			"Y",
			context,
			TEST_RATIONALE
		);

		final var progressAfterTwoValues = eventService.getProgression(baselineVisit);
		assertEquals(47, progressAfterTwoValues.getTotal());
		assertEquals(2, progressAfterTwoValues.getProgress());
		assertFalse(progressAfterTwoValues.isNull());
		assertFalse(progressAfterTwoValues.isComplete());
	}

	@Test
	@DisplayName("Event addition pre-conditions verification works")
	public void addition() {
		final var center = scopeDAOService.getScopeByCode("FR-01");
		final var patientModel = studyService.getStudy().getScopeModel("PATIENT");

		final var patientCode = scopeService.getNextCode(patientModel, center);
		final var patient = scopeCreatorService.createScope(new ScopeBuilder(context).createScope(patientModel, center, patientCode));

		//check that telephone event can be added
		assertTrue(eventService.getEventModels(patient).contains(getTelephoneEvent()));

		//create telephone event group
		final var visit = eventService.create(patient, getTelephoneEvent(), context, TEST_RATIONALE);
		assertEquals(visit.getEventModelId(), "TELEPHONE_VISIT");

		//check that telephone event can be added
		assertTrue(eventService.getEventModels(patient).contains(getTelephoneEvent()));

		//change telephone date
		eventService.updateDate(patient, visit, ZonedDateTime.now().plusYears(2).plusMonths(6), context, TEST_RATIONALE);
	}

	private ScopeModel getPatientScopeModel() {
		return studyService.getStudy().getScopeModel("PATIENT");
	}

	private EventModel getBaselineEvent() {
		return getPatientScopeModel().getEventModel("BASELINE");
	}

	private EventModel getVisit6Event() {
		return getPatientScopeModel().getEventModel("VISIT_6");
	}

	private EventModel getTerminationEvent() {
		return getPatientScopeModel().getEventModel("TERMINATION_VISIT");
	}

	private EventModel getTelephoneEvent() {
		return getPatientScopeModel().getEventModel("TELEPHONE_VISIT");
	}

	private Scope createPatient() {
		final var center = scopeDAOService.getScopeByCode("FR-01");
		final var patientModel = getPatientScopeModel();

		final var patientCode = scopeService.getNextCode(patientModel, center);
		return scopeCreatorService.createScope(new ScopeBuilder(context).createScope(patientModel, center, patientCode));
	}
}
