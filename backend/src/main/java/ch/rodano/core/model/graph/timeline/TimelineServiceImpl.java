package ch.rodano.core.model.graph.timeline;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.PartialDate;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.timelinegraph.TimelineGraph;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;

import static ch.rodano.core.model.jooq.Tables.DATASET;
import static ch.rodano.core.model.jooq.Tables.EVENT;
import static ch.rodano.core.model.jooq.Tables.FIELD;
import static ch.rodano.core.model.jooq.Tables.FORM;

@Service
public class TimelineServiceImpl implements TimelineService {
	private static final JavaTimeModule TIME_MODULE = new JavaTimeModule();
	private static final ObjectMapper MAPPER = new ObjectMapper();

	static {
		MAPPER.registerModule(TIME_MODULE);
	}

	private final DSLContext create;
	private final ActorService actorService;
	private final EventService eventService;
	private final ScopeDAOService scopeDAOService;
	private final ScopeRelationService scopeRelationService;

	public TimelineServiceImpl(
		final ActorService actorService,
		final EventService eventService,
		final ScopeRelationService scopeRelationService,
		final ScopeDAOService scopeDAOService,
		final DSLContext create
	) {
		this.create = create;
		this.actorService = actorService;
		this.eventService = eventService;
		this.scopeDAOService = scopeDAOService;
		this.scopeRelationService = scopeRelationService;
	}

	@Override
	public List<TimelineGraphData> getTimelineGraphs(final Actor actor, final List<Role> roles, final Scope scope, final Study study) {
		final var languages = actorService.getLanguages(actor);
		return getGraphConfigs(roles, scope).stream()
			.map(graph -> generateTimelineGraphData(graph, scope, languages, study))
			.toList();
	}

	private SortedSet<TimelineGraph> getGraphConfigs(final List<Role> roles, final Scope scope) {
		return roles.stream().map(Role::getProfile)
			.flatMap(profile -> profile.getGraphConfigs().stream())
			.filter(config -> config.getScopeModelId().equals(scope.getScopeModelId()))
			.collect(Collectors.toCollection(TreeSet::new));
	}

	private Optional<Event> getFirstEventOfType(final List<Event> events, final String eventModelId) {
		if(StringUtils.isNotBlank(eventModelId)) {
			for(final Event v : events) {
				if(v.getEventModelId().equals(eventModelId)) {
					return Optional.of(v);
				}
			}
		}
		return Optional.empty();
	}

	private Optional<Event> getLastEventOfType(final List<Event> events, final String eventModelId) {
		if(StringUtils.isNotBlank(eventModelId)) {
			for(final Event v : events) {
				if(v.getEventModelId().equals(eventModelId)) {
					return Optional.of(v);
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public TimelineGraphData generateTimelineGraphData(final TimelineGraph config, final Scope scope, final String[] languages, final Study study) {
		final var instance = new TimelineGraphData(config);
		//retrieve information that may be used by one or more sections
		final List<Event> allEvents = new ArrayList<>(eventService.getAll(scope));
		allEvents.sort(Event.DEFAULT_COMPARATOR);
		//retrieve latest done event
		final Predicate<Event> expectedPredicate = Event::isExpected;
		final Optional<Event> latestDoneEvent = allEvents.stream().filter(expectedPredicate.negate()).max(Event::compareTo);

		//retrieve scope forms by form model id
		final Map<String, Long> formsByFormModelId = create.select(FORM.FORM_MODEL_ID, FORM.PK)
			.from(FORM)
			.where(FORM.SCOPE_FK.eq(scope.getPk()))
			.fetchMap(FORM.FORM_MODEL_ID, FORM.PK);

		//retrieve forms by events and form model ids
		final Map<Long, Map<String, Long>> formsByEventPkAndFormModelId = create.select(
			FORM.EVENT_FK,
			DSL.multisetAgg(FORM.FORM_MODEL_ID, FORM.PK).convertFrom(r -> r.map(rec -> Map.entry(rec.value1(), rec.value2())))
		).from(FORM)
			.join(EVENT).on(FORM.EVENT_FK.eq(EVENT.PK))
			.where(EVENT.SCOPE_FK.eq(scope.getPk()))
			.groupBy(FORM.EVENT_FK)
			.fetchMap(FORM.EVENT_FK, r -> r.value2().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue)));

		//set study period if any
		if(!allEvents.isEmpty()) {
			final var period = new TimelineGraphDataPeriod();
			final var startEvent = getFirstEventOfType(allEvents, config.getStudyStartEventModelId()).orElse(allEvents.get(0));
			final var stopEvent = getLastEventOfType(allEvents, config.getStudyStopEventModelId()).orElse(allEvents.get(allEvents.size() - 1));
			//sort these events because in some cases, the stop event may be before the start event
			final var dates = List.of(startEvent.getDateOrExpectedDate(), stopEvent.getDateOrExpectedDate()).stream().sorted().toList();
			if(!dates.get(0).equals(dates.get(1))) {
				period.setStartDate(dates.get(0));
				period.setStopDate(dates.get(1));
				period.setDefault(config.isStudyPeriodIsDefault());
				period.setLabel("Study");
				instance.getPeriods().add(period);
			}
		}
		for(final var section : config.getSections()) {
			final var graphData = new TimelineGraphDataSection(section);
			graphData.setId(section.getId());
			instance.getDataSections().add(graphData);

			//representation of scope path dates
			if(section.isUseScopePaths()) {
				for(final var relation : scopeRelationService.getAllParentRelations(scope)) {
					//build graph value
					final var graphValue = new TimelineGraphDataValue();
					//label
					final var parent = scopeDAOService.getScopeByPk(relation.getParentFk());
					graphValue.setLabel(parent.getCode());
					//start date
					graphValue.setDate(relation.getStartDate());
					//end date if any
					final var endDate = relation.getEndDate();
					if(endDate != null) {
						graphValue.setEndDate(endDate);
					}
					else {
						final var lastEventDate = latestDoneEvent.map(Event::getDateOrExpectedDate).orElse(relation.getStartDate());
						graphValue.setEndDate(lastEventDate.isAfter(relation.getStartDate()) ? lastEventDate : relation.getStartDate());
						graphValue.setOngoing(true);
					}
					graphData.getValues().add(graphValue);
				}
			}
			else {
				List<Event> events = new ArrayList<>(allEvents);
				//filter events on event group
				if(!section.getEventModelIds().isEmpty()) {
					events = events.stream().filter(e -> section.getEventModelIds().contains(e.getEventModelId())).toList();
				}
				//filter event on expected criteria
				if(section.isHideExpectedEvent()) {
					events = events.stream().filter(expectedPredicate.negate()).toList();
				}
				if(section.isHideDoneEvent()) {
					events = events.stream().filter(expectedPredicate).toList();
				}

				//action
				if(section.getDatasetModelId() == null) {
					for(final var event : events) {
						final var graphValue = new TimelineGraphDataValue();
						graphValue.setLabel(eventService.getLabel(scope, event, languages));
						graphValue.setDate(event.getDateOrExpectedDate());
						if(!event.isExpected()) {
							graphValue.setLink(String.format("/crf/%d/event/%d", scope.getPk(), event.getPk()));
						}
						graphData.getValues().add(graphValue);
					}
				}
				else {
					final var datasetModel = study.getDatasetModel(section.getDatasetModelId());
					final var eventPks = events.stream().map(Event::getPk).toList();
					final var fieldModelIds = new ArrayList<String>();
					fieldModelIds.add(section.getDateFieldModelId());
					fieldModelIds.add(section.getEndDateFieldModelId());
					fieldModelIds.add(section.getValueFieldModelId());
					fieldModelIds.add(section.getLabelFieldModelId());
					fieldModelIds.addAll(section.getMetaFieldModelIds());
					final var sectionFieldModelIds = fieldModelIds.stream().filter(StringUtils::isNotBlank).toList();

					//retrieve fields
					final var query = create.select(DATASET.PK, FIELD.FIELD_MODEL_ID, FIELD.VALUE, EVENT.PK, EVENT.DATE, EVENT.EXPECTED_DATE, EVENT.END_DATE)
						.from(DATASET)
						.leftJoin(FIELD).on(FIELD.DATASET_FK.eq(DATASET.PK))
						.leftJoin(EVENT).on(DATASET.EVENT_FK.eq(EVENT.PK))
						.where(
							DATASET.DATASET_MODEL_ID.eq(section.getDatasetModelId())
								.and(DATASET.SCOPE_FK.eq(scope.getPk()).or(DATASET.EVENT_FK.in(eventPks)))
								.and(DATASET.DELETED.isFalse())
								.and(FIELD.FIELD_MODEL_ID.isNull().or(FIELD.FIELD_MODEL_ID.in(sectionFieldModelIds)))
						)
						.orderBy(DATASET.PK);
					final var results = query.fetchGroups(DATASET.PK);
					for(final var entry : results.entrySet()) {
						//retrieve event properties
						final var eventPk = entry.getValue().getValue(0, EVENT.PK);

						FieldModel referenceFieldModel = null;
						final var graphValue = new TimelineGraphDataValue();

						final var recordByFieldModelId = entry.getValue().intoMap(FIELD.FIELD_MODEL_ID);

						//date
						if(StringUtils.isNotBlank(section.getDateFieldModelId())) {
							referenceFieldModel = datasetModel.getFieldModel(section.getDateFieldModelId());
							final var value = recordByFieldModelId.get(section.getDateFieldModelId()).get(FIELD.VALUE);
							//unable to add date to graph if it is totally unknown
							if(value == null) {
								continue;
							}
							final var date = PartialDate.of(value);
							final Optional<ZonedDateTime> zonedDate = date.toZonedDateTime();
							if(zonedDate.isEmpty()) {
								continue;
							}
							graphValue.setDate(zonedDate.get());
						}
						else if(eventPk != null) {
							final var eventDate = entry.getValue().getValue(0, EVENT.DATE);
							final var eventExpectedDate = entry.getValue().getValue(0, EVENT.EXPECTED_DATE);
							graphValue.setDate(eventDate != null ? eventDate : eventExpectedDate);
						}

						//end date
						//find end date in the selected field or use the end date of the event
						ZonedDateTime endDate = null;
						if(StringUtils.isNotBlank(section.getEndDateFieldModelId())) {
							final var value = recordByFieldModelId.get(section.getEndDateFieldModelId()).get(FIELD.VALUE);
							if(value != null) {
								final var date = PartialDate.of(value);
								final Optional<ZonedDateTime> zonedDate = date.toZonedDateTime();
								if(zonedDate.isPresent()) {
									endDate = zonedDate.get();
								}
							}
						}
						else if(eventPk != null) {
							endDate = entry.getValue().getValue(0, EVENT.END_DATE);
						}
						//set end date if it is not null
						if(endDate != null) {
							graphValue.setEndDate(endDate);
						}
						else {
							//use date of last non expected event as stop date
							endDate = latestDoneEvent.map(Event::getDateOrExpectedDate).orElse(graphValue.getDate());
							//in some cases, this date may be before the start date of the beginning of the period
							//in these cases, re-use start date as end date
							graphValue.setEndDate(endDate.isAfter(graphValue.getDate()) ? endDate : graphValue.getDate());
							graphValue.setOngoing(true);
						}

						//value
						if(StringUtils.isNotBlank(section.getValueFieldModelId())) {
							referenceFieldModel = datasetModel.getFieldModel(section.getValueFieldModelId());
							final var value = recordByFieldModelId.get(referenceFieldModel.getId()).get(FIELD.VALUE);

							//display only non empty value
							if(StringUtils.isBlank(value)) {
								continue;
							}

							graphValue.setValue(Double.valueOf(value));
						}

						//label
						if(StringUtils.isNotBlank(section.getLabelFieldModelId())) {
							referenceFieldModel = datasetModel.getFieldModel(section.getLabelFieldModelId());
							final var value = recordByFieldModelId.get(referenceFieldModel.getId()).get(FIELD.VALUE);
							//display only non empty value
							if(StringUtils.isNotBlank(value)) {
								//TODO find a proper way to calculate the actual label based on custom possible values
								graphValue.setLabel(referenceFieldModel.valueToLabel(value, languages));
							}
						}

						//add link
						if(referenceFieldModel != null) {
							//find form model
							final var formModel = referenceFieldModel.getFormModels().stream()
								.findFirst();
							if(formModel.isPresent()) {
								final var formModelId = formModel.get().getId();
								final var baseLink = String.format("/crf/%s", scope.getPk());
								//find form
								if(eventPk == null) {
									final var formPk = formsByFormModelId.get(formModelId);
									graphValue.setLink(String.format("%s/form/%d", baseLink, formPk));
								}
								else {
									final var formPk = formsByEventPkAndFormModelId.get(eventPk).get(formModelId);
									graphValue.setLink(String.format("%s/event/%d/form/%d", baseLink, eventPk, formPk));
								}
							}
						}
						if(graphValue.getLink() == null && eventPk != null) {
							graphValue.setLink(String.format("/%d", eventPk));
						}

						//add meta data
						section.getMetaFieldModelIds().forEach(fieldModelId -> {
							final var value = recordByFieldModelId.get(fieldModelId).get(FIELD.VALUE);
							graphValue.getMetadata().put(fieldModelId, value);
						});

						graphData.getValues().add(graphValue);
					}
				}
			}
		}
		return instance;
	}

}
