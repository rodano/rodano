package ch.rodano.core.services.bll.event;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.exceptions.NoRespectForConfigurationException;
import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.workflow.WorkflowAction;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.event.Progression;
import ch.rodano.core.model.exception.LockedObjectException;
import ch.rodano.core.model.exception.MissingDataException;
import ch.rodano.core.model.exception.WrongDataConditionException;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.rules.data.ConstraintEvaluationService;
import ch.rodano.core.model.rules.data.DataEvaluation;
import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.event.EventDAOService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.rule.RuleService;
import ch.rodano.core.utils.ACL;
import ch.rodano.core.utils.Utils;
import ch.rodano.core.utils.UtilsService;

@Service
public class EventServiceImpl implements EventService {
	private static final Pattern REGEXP = Pattern.compile("\\$\\{datasetModelId:(\\w+)-fieldModelId:(\\w+)}");
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

	private final ScopeService scopeService;
	private final EventDAOService eventDAOService;
	private final FieldService fieldService;
	private final FieldDAOService fieldDAOService;
	private final RuleService ruleService;
	private final FormService formService;
	private final DatasetService datasetService;
	private final StudyService studyService;
	private final WorkflowStatusService workflowStatusService;
	private final ConstraintEvaluationService constraintEvaluationService;
	private final UtilsService utilsService;

	public EventServiceImpl(
		@Lazy final ScopeService scopeService,
		final EventDAOService eventDAOService,
		final FieldService fieldService,
		final FieldDAOService fieldDAOService,
		final RuleService ruleService,
		final FormService formService,
		final DatasetService datasetService,
		final StudyService studyService,
		final WorkflowStatusService workflowStatusService,
		final ConstraintEvaluationService constraintEvaluationService,
		final UtilsService utilsService
	) {
		this.scopeService = scopeService;
		this.eventDAOService = eventDAOService;
		this.fieldService = fieldService;
		this.fieldDAOService = fieldDAOService;
		this.ruleService = ruleService;
		this.formService = formService;
		this.datasetService = datasetService;
		this.studyService = studyService;
		this.workflowStatusService = workflowStatusService;
		this.constraintEvaluationService = constraintEvaluationService;
		this.utilsService = utilsService;
	}

	/**
	 * This method performs the necessary checks before event addition to scope.
	 * @param scope The scope
	 * @param existingEvents All existing events of the scope
	 * @param eventModel The concerned event model
	 */
	private Optional<RuntimeException> verifyScopePreRequisites(final Scope scope, final List<Event> existingEvents, final EventModel eventModel) {
		//check that scope is not locked
		if(scope.getLocked()) {
			return Optional.of(new LockedObjectException(scope));
		}

		//check that the event model is allowed for the scope model
		if(!scope.getScopeModel().getEventModels().contains(eventModel)) {
			final var message = String.format("Event model %s is not allowed in scope model %s", eventModel.getId(), scope.getScopeModelId());
			return Optional.of(new NoRespectForConfigurationException(message));
		}

		// check if there is not a blocking event group
		for(final var blockingEventModel : eventModel.getBlockingEventModels()) {
			if(existingEvents.stream().anyMatch(e -> e.getEventModelId().equals(blockingEventModel.getId()))) {
				final var message = String.format("Unable to add event %s to scope %s as it already has the blocking event %s", eventModel.getId(), scope.getCode(), blockingEventModel.getId());
				return Optional.of(new WrongDataConditionException(message));
			}
		}

		//filter existing events with the same event model
		final var sameEvents = existingEvents.stream()
			.filter(e -> e.getEventModelId().equals(eventModel.getId()))
			.sorted()
			.toList();

		//check if max occurrence has not been reached
		if(eventModel.getMaxOccurrence() > 0) {
			if(sameEvents.size() >= eventModel.getMaxOccurrence()) {
				final var message = String.format("Max occurrence for event %s has been reached for scope %s", eventModel.getId(), scope.getCode());
				return Optional.of(new WrongDataConditionException(message));
			}
		}

		//constraints are respected
		if(eventModel.getConstraint() != null) {
			final var state = new DataState(scope);
			final var evaluation = new DataEvaluation(state, eventModel.getConstraint());
			constraintEvaluationService.evaluate(evaluation);
			if(!evaluation.isValid()) {
				final var message = String.format("Unable to add event group %s to scope %s because of a rule", eventModel.getId(), scope.getCode());
				return Optional.of(new WrongDataConditionException(message));
			}
		}

		return Optional.empty();
	}

	@Override
	public Event create(
		final Scope scope,
		final EventModel eventModel,
		final DatabaseActionContext context,
		final String rationale
	) {
		//retrieve all existing events for the scope
		final var existingEvents = new ArrayList<>(getAll(scope));

		final var check = verifyScopePreRequisites(scope, existingEvents, eventModel);
		if(check.isPresent()) {
			throw check.get();
		}

		//check if an empty event of the same type already exists (among not deleted event)
		//this check cannot be moved to verifyScopePreRequisites because it is too costly
		final var emptyEvent = existingEvents
			.stream()
			.filter(e -> e.getEventModelId().equals(eventModel.getId()))
			.map(Event::getPk)
			.filter(Predicate.not(fieldDAOService::doesEventHaveFieldsWithAValue))
			.findAny();
		if(emptyEvent.isPresent()) {
			throw new WrongDataConditionException(
				String.format(
					"An empty %s already exists for scope %s",
					eventModel.getLocalizedShortname(studyService.getStudy().getDefaultLanguage()),
					scope.getCode()
				)
			);
		}

		final var creationDate = ZonedDateTime.now();
		final var enhancedRationale = StringUtils.isBlank(rationale) ? "Create event" : "Create event: " + rationale;

		final List<Event> createdEvents = new ArrayList<>();

		//create event
		createdEvents.add(createInternalEvent(scope, eventModel, context, enhancedRationale, creationDate));

		//create all dependencies events
		for(final var impliedEvent : eventModel.getImpliedEventModels()) {
			createdEvents.add(createInternalEvent(scope, impliedEvent, context, enhancedRationale, creationDate));
		}

		//set all events date
		existingEvents.addAll(createdEvents);
		resetEventsDate(existingEvents, context, enhancedRationale);

		//execute rules for all created events
		final var triggerRules = studyService.getStudy().getEventActions().get(WorkflowAction.CREATE_EVENT);
		for(final Event event : createdEvents) {
			final var state = new DataState(scope, event);

			//execute event creation rules
			final var rules = event.getEventModel().getCreateRules();
			if(CollectionUtils.isNotEmpty(rules)) {
				ruleService.execute(state, rules, context);
			}

			//execute trigger rules
			if(CollectionUtils.isNotEmpty(triggerRules)) {
				ruleService.execute(state, triggerRules, context);
			}
		}

		return createdEvents.get(0);
	}

	@Override
	public List<Event> createAll(
		final Scope scope,
		final DatabaseActionContext context,
		final String rationale
	) {
		return scope.getScopeModel().getEventModels().stream()
			.filter(EventModel::isMandatory)
			.map(e -> create(scope, e, context, rationale))
			.toList();
	}

	@Override
	public void save(
		final Event event,
		final DatabaseActionContext context,
		final String rationale
	) {
		if(event.getLocked()) {
			throw new LockedObjectException(event);
		}
		eventDAOService.saveEvent(event, context, rationale);
	}

	@Override
	public void delete(
		final Scope scope,
		final Event event,
		final DatabaseActionContext context,
		final String rationale
	) {
		utilsService.checkNotDeleted(scope);
		utilsService.checkNotLocked(event);

		final var eventModel = event.getEventModel();

		final var baseRationale = "Event removed";
		final var enhancedRationale = StringUtils.isBlank(rationale) ? baseRationale : String.format("%s: %s", baseRationale, rationale);

		eventDAOService.deleteEvent(event, context, enhancedRationale);

		resetDates(scope, context, rationale);

		final var state = new DataState(scope, event);

		//execute event removal rules
		var rules = eventModel.getRemoveRules();
		if(CollectionUtils.isNotEmpty(rules)) {
			ruleService.execute(state, rules, context);
		}

		//execute global event removal rules
		rules = studyService.getStudy().getEventActions().get(WorkflowAction.REMOVE_EVENT);
		if(CollectionUtils.isNotEmpty(rules)) {
			ruleService.execute(state, rules, context);
		}
	}

	@Override
	public void restore(
		final Scope scope,
		final Event event,
		final DatabaseActionContext context,
		final String rationale
	) {
		utilsService.checkNotDeleted(scope);
		utilsService.checkNotLocked(event);

		//retrieve all existing events
		final var existingEvents = getAll(scope);

		final var eventModel = event.getEventModel();

		// check if max occurrence has not been reached
		if(eventModel.getMaxOccurrence() > 0) {
			//retrieve number of existing events (among non deleted events)
			final var sameEventsCount = existingEvents
				.stream()
				.filter(e -> e.getEventModelId().equals(eventModel.getId()))
				.count();
			if(sameEventsCount >= eventModel.getMaxOccurrence()) {
				throw new WrongDataConditionException(String.format("Max occurrence for event %s has been reached for scope %s", eventModel.getId(), scope.getCode()));
			}
		}

		//reset the event date if it is a planned event model
		if(eventModel.isPlanned()) {
			event.setExpectedDate(getDateTheoretical(existingEvents, event));
		}

		final var baseRationale = "Event restored";
		final var enhancedRationale = StringUtils.isBlank(rationale) ? baseRationale : String.format("%s: %s", baseRationale, rationale);
		eventDAOService.restoreEvent(event, context, enhancedRationale);

		resetDates(scope, context, rationale);

		validate(scope, event, context, enhancedRationale);

		final var state = new DataState(scope, event);

		//execute event restoration rules
		var rules = eventModel.getRestoreRules();
		if(CollectionUtils.isNotEmpty(rules)) {
			ruleService.execute(state, rules, context);
		}

		//execute global event restoration rules
		rules = studyService.getStudy().getEventActions().get(WorkflowAction.RESTORE_EVENT);
		if(CollectionUtils.isNotEmpty(rules)) {
			ruleService.execute(state, rules, context);
		}
	}

	@Override
	public void lock(
		final Scope scope,
		final Event event,
		final DatabaseActionContext context,
		final String rationale
	) {
		utilsService.checkNotDeleted(scope);
		utilsService.checkNotDeleted(event);

		event.setLocked(true);
		eventDAOService.saveEvent(event, context, rationale);
	}

	@Override
	public void unlock(
		final Scope scope,
		final Event event,
		final DatabaseActionContext context,
		final String rationale
	) {
		utilsService.checkNotDeleted(scope);
		utilsService.checkNotDeleted(event);

		event.setLocked(false);
		eventDAOService.saveEvent(event, context, rationale);
	}

	@Override
	public void validate(
		final Scope scope,
		final Event event,
		final DatabaseActionContext context,
		final String rationale
	) {
		for(final var dataset : datasetService.getAll(event)) {
			datasetService.validateFieldsOnDataset(scope, Optional.of(event), dataset, context, rationale);
		}
	}

	@Override
	public Event updateDate(
		final Scope scope,
		final Event event,
		final ZonedDateTime date,
		final DatabaseActionContext context,
		final String rationale
	) {
		//do not consider inceptive event
		//this event is now used to store data directly related to scope but its date is not relevant in the timeline of the scope
		if(date != null && !event.getEventModel().isInceptive() && !scope.getVirtual()) {
			//move oldest physical start date if necessary
			final var baseRationale = "Oldest physical date of the scope aligned to its oldest event";
			final var enhancedRationale = StringUtils.isBlank(rationale) ? baseRationale : String.format("%s: %s", baseRationale, rationale);
			scopeService.moveBackPhysicalDate(scope, date, context, enhancedRationale);
		}

		event.setDate(date);
		resetDates(scope, context, rationale);
		eventDAOService.saveEvent(event, context, rationale);

		return event;
	}

	@Override
	public void resetDates(
		final Scope scope,
		final DatabaseActionContext context,
		final String rationale
	) {
		if(scope.getLocked()) {
			throw new LockedObjectException(scope);
		}
		resetEventsDate(getAll(scope), context, rationale);
	}

	@Override
	public List<Event> getAll(final Scope scope) {
		return eventDAOService.getEventsByScopePk(scope.getPk());
	}

	@Override
	public List<Event> getAll(final Scope scope, final EventModel eventModel) {
		return eventDAOService.getEventsByScopePkAndEventModelId(scope.getPk(), eventModel.getId());
	}

	@Override
	public Optional<Event> get(final Dataset dataset) {
		//dataset may not be linked to a event
		return Optional.ofNullable(dataset.getEventFk()).map(eventDAOService::getEventByPk);
	}

	@Override
	public Optional<Event> get(final Form form) {
		//form may not be linked to a event
		return Optional.ofNullable(form.getEventFk()).map(eventDAOService::getEventByPk);
	}

	@Override
	public Optional<Event> get(final WorkflowStatus workflowStatus) {
		//workflow status may not be linked to a event
		return Optional.ofNullable(workflowStatus.getEventFk()).map(eventDAOService::getEventByPk);
	}

	@Override
	public Event get(
		final Scope scope,
		final EventModel eventModel,
		final int eventNumber
	) {
		return eventDAOService.getEventByScopePkAndEventModelIdAndEventNumber(scope.getPk(), eventModel.getId(), eventNumber);
	}

	@Override
	public List<Event> search(final Scope scope, final ACL acl) {
		final var includeDeleted = acl.hasRight(FeatureStatic.MANAGE_DELETED_DATA);
		final var events = includeDeleted ? getAllIncludingRemoved(scope) : getAll(scope);
		return events.stream()
			.filter(e -> acl.hasRight(e.getDateOrExpectedDate(), e.getEventModel(), Rights.READ))
			.sorted()
			.toList();
	}

	@Override
	public List<Event> getAllIncludingRemoved(final EventModel eventModel) {
		return eventDAOService.getAllEventsByEventModelId(eventModel.getId());
	}

	@Override
	public List<Event> getAllIncludingRemoved(final Scope scope) {
		return eventDAOService.getAllEventsByScopePk(scope.getPk());
	}

	@Override
	public List<Event> getAllIncludingRemoved(final Scope scope, final EventModel eventModel) {
		return eventDAOService.getAllEventsByScopePkAndEventModelId(scope.getPk(), eventModel.getId());
	}

	@Override
	public Event getInceptive(final Scope scope) {
		return getAll(scope).stream()
			.filter(e -> e.getEventModel().isInceptive()).findFirst()
			.orElseThrow(() -> new MissingDataException(String.format("No inceptive event for scope %s", scope.getCode())));
	}

	@Override
	public ZonedDateTime getTheoreticalDate(final Scope scope, final Event event) {
		return getDateTheoretical(getAll(scope), event);
	}

	@Override
	public Progression getProgression(final Event event) {
		final var progression = new Progression();
		final var fields = fieldDAOService.getFieldsByEventPk(event.getPk());
		progression.setTotal(fields.size());
		progression.setProgress(Math.toIntExact(fields.stream().filter(f -> !f.getFieldModel().isPlugin() && !f.isNull()).count()));
		return progression;
	}

	@Override
	public Optional<Event> getPrevious(final Event event) {
		final var events = new TreeSet<>(eventDAOService.getEventsByScopePk(event.getScopeFk()));
		return Optional.ofNullable(events.lower(event));
	}

	@Override
	public Optional<Event> getNext(final Event event) {
		final var events = new TreeSet<>(eventDAOService.getEventsByScopePk(event.getScopeFk()));
		return Optional.ofNullable(events.higher(event));
	}

	@Override
	public List<EventModel> getEventModels(final Scope scope) {
		if(scope.getDeleted() || scope.getLocked()) {
			return Collections.emptyList();
		}
		//retrieve all existing events for the scope
		final var events = getAll(scope);

		return scope.getScopeModel().getEventModels()
			.stream()
			.filter(e -> !e.isMandatory())
			.filter(e -> verifyScopePreRequisites(scope, events, e).isEmpty())
			.toList();
	}

	@Override
	public String getLabel(final Scope scope, final Event event, final String... languages) {
		final var eventModel = event.getEventModel();

		if(StringUtils.isBlank(eventModel.getLabelPattern())) {
			return eventModel.getLocalizedShortname(languages);
		}

		var pattern = eventModel.getLabelPattern();

		long durationInDays = 0, durationInMonths = 0, durationInYears = 0, durationAuto = 0;
		if(event.getEndDate() != null) {
			durationInDays = Math.round((event.getEndDate().toInstant().toEpochMilli() - event.getDate().toInstant().toEpochMilli()) / 1000d / 60d / 60d / 24d);
			durationInMonths = Math.round(durationInDays / 30.5);
			durationInYears = Math.round(durationInDays / 365.5);

			durationAuto = durationInDays;
			if(durationInDays > 31) {
				durationAuto = durationInMonths;
			}
			if(durationInDays > 366) {
				durationAuto = durationInYears;
			}
		}

		final Map<String, String> replacements = new HashMap<>();
		replacements.put("id", eventModel.getId());
		replacements.put("shortname", eventModel.getLocalizedShortname(languages));
		replacements.put("longname", eventModel.getLocalizedLongname(languages));
		replacements.put("description", eventModel.getLocalizedDescription(languages));
		replacements.put("number", Integer.toString(eventModel.getNumber()));
		replacements.put("date", event.getDateOrExpectedDate().format(DATE_FORMAT));
		replacements.put("expectedDate", event.getExpectedDate() != null ? event.getExpectedDate().format(DATE_FORMAT) : "");
		replacements.put("actualDate", event.getDate() != null ? event.getDate().format(DATE_FORMAT) : "");
		replacements.put("endDate", event.getEndDate() != null ? event.getEndDate().format(DATE_FORMAT) : "");
		replacements.put("time", event.getDate() != null ? event.getDate().format(TIME_FORMAT) : "");
		replacements.put("durationInDays", Long.toString(durationInDays));
		replacements.put("durationInMonths", Long.toString(durationInMonths));
		replacements.put("durationInYears", Long.toString(durationInYears));
		replacements.put("durationAuto", Long.toString(durationAuto));
		replacements.put("rawGroupNumber", event.getEventGroupNumber() != null ? Integer.toString(event.getEventGroupNumber()) : "");
		replacements.put("groupNumber", event.getEventGroupNumber() != null ? Integer.toString(event.getEventGroupNumber() + 1) : "");
		if(eventModel.hasInterval()) {
			replacements.put("interval", Integer.toString(eventModel.getInterval()));
			final var unitLabel = eventModel.getIntervalUnit().toString().toLowerCase();
			replacements.put("intervalUnit", unitLabel);
			replacements.put("intervalText", event.isExpected() ? String.format("+/- %d %s", eventModel.getInterval(), unitLabel) : "");
		}
		else {
			replacements.put("interval", "");
			replacements.put("intervalUnit", "");
			replacements.put("intervalText", "");
		}
		replacements.put("deadline", eventModel.getDeadline() != null ? Integer.toString(eventModel.getDeadline()) : "");
		replacements.put("deadlineUnit", eventModel.getDeadlineUnit() != null ? eventModel.getDeadlineUnit().toString().toLowerCase() : "");

		//manage value in label //so cool
		if(pattern.contains("${datasetModelId:")) {
			final var attributePattern = new StringBuffer();
			final var regexMatcher = REGEXP.matcher(pattern);
			while(regexMatcher.find()) {
				var value = "NA";
				final var datasetModel = studyService.getStudy().getDatasetModel(regexMatcher.group(1));
				final var fieldModelId = regexMatcher.group(2);
				//retrieve dataset in event or scope
				final Dataset dataset = eventModel.getDatasetModelIds().contains(datasetModel.getId()) ? datasetService.get(event, datasetModel) : datasetService.get(scope, datasetModel);
				final var fieldModel = dataset.getDatasetModel().getFieldModel(fieldModelId);
				final var field = fieldService.get(dataset, fieldModel);
				//TODO find a proper way to calculate the actual label based on custom possible values
				value = field.getFieldModel().valueToLabel(field.getValue(), languages);
				regexMatcher.appendReplacement(attributePattern, value);
			}
			regexMatcher.appendTail(attributePattern);
			pattern = attributePattern.toString();
		}

		return Utils.replaceText(pattern, replacements);
	}

	@Override
	public String getLabel(final Scope scope, final Event event) {
		return getLabel(scope, event, studyService.getStudy().getDefaultLanguageId());
	}

	private Event createInternalEvent(final Scope scope, final EventModel eventModel, final DatabaseActionContext context, final String rationale, final ZonedDateTime date) {
		//retrieve biggest event group number
		final var lastEventGroupNumber = getAllIncludingRemoved(scope, eventModel).stream().mapToInt(Event::getEventGroupNumber).max().orElse(-1) + 1;

		final var event = new Event();
		event.setScope(scope);
		event.setScopeModel(scope.getScopeModel());
		event.setEventModel(eventModel);
		event.setEventGroupNumber(lastEventGroupNumber);
		event.setNotDone(false);
		event.setBlocking(false);
		event.setLocked(false);

		if(event.getEventModel().isPlanned()) {
			event.setExpectedDate(date);
		}
		else {
			event.setDate(date);
		}

		eventDAOService.saveEvent(event, context, rationale);
		final var family = new DataFamily(scope, event);
		workflowStatusService.createAll(family, event, Collections.emptyMap(), context, rationale);

		//datasets and forms
		datasetService.createAll(scope, event, context, rationale);
		formService.createAll(scope, event, context, rationale);

		return event;
	}

	private ZonedDateTime getDateTheoretical(final List<Event> events, final Event event) {
		final var eventModel = event.getEventModel();
		final var deadlineReferenceEventIds = CollectionUtils.emptyIfNull(eventModel.getDeadlineReferenceEventModelIds());
		return events.stream()
			.filter(e -> deadlineReferenceEventIds.contains(e.getEventModelId()))
			.map(Event::getDateOrExpectedDate)
			.filter(Objects::nonNull)
			.reduce(eventModel.getDeadlineAggregationFunctionOrDefault().getAccumulator())
			.map(d -> d.plus(eventModel.getDeadline(), eventModel.getDeadlineUnit()))
			.orElse(null);
	}

	private void resetEventsDate(final List<Event> events, final DatabaseActionContext context, final String rationale) {
		final var baseRationale = "Date of event recalculated";
		final var enhancedRationale = StringUtils.isBlank(rationale) ? baseRationale : String.format("%s: %s", baseRationale, rationale);

		final var sortedEvents = new ArrayList<>(events);
		sortedEvents.sort(Event.COMPARATOR_CONFIG);
		for(final var event : sortedEvents) {
			final var eventModel = event.getEventModel();
			if(eventModel.isPlanned()) {
				final var theoreticalDate = getDateTheoretical(sortedEvents, event);
				if(theoreticalDate != null && !Objects.equals(theoreticalDate, event.getExpectedDate())) {
					event.setExpectedDate(theoreticalDate);
					eventDAOService.saveEvent(event, context, enhancedRationale);
				}
			}
		}
	}
}
