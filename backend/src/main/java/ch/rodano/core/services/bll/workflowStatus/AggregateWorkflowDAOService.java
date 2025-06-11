package ch.rodano.core.services.bll.workflowStatus;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jooq.CaseConditionStep;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.GroupConcatOrderByStep;
import org.jooq.GroupField;
import org.jooq.Record16;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.workflow.StateMatcher;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowState;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.AggregateWorkflowStatus;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.Tables.DATASET;
import static ch.rodano.core.model.jooq.Tables.EVENT;
import static ch.rodano.core.model.jooq.Tables.FIELD;
import static ch.rodano.core.model.jooq.Tables.FORM;
import static ch.rodano.core.model.jooq.Tables.WORKFLOW_STATUS;

@Service
public class AggregateWorkflowDAOService {

	private final DSLContext create;
	private final DAOStrategy strategy;

	private final WorkflowStatusService workflowStatusService;

	private final List<Workflow> aggregatorScopeWorkflows;
	private final List<Workflow> aggregatorEventWorkflows;

	public AggregateWorkflowDAOService(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService,
		final WorkflowStatusService workflowStatusService
	) {
		this.create = create;
		this.strategy = strategy;
		this.workflowStatusService = workflowStatusService;

		aggregatorScopeWorkflows = studyService.getStudy().getWorkflows().stream()
			.filter(Workflow::isAggregator)
			.filter(w -> w.getWorkflowableEntities().contains(Entity.SCOPE_MODEL))
			.toList();
		aggregatorEventWorkflows = studyService.getStudy().getWorkflows().stream()
			.filter(Workflow::isAggregator)
			.filter(w -> w.getWorkflowableEntities().contains(Entity.EVENT_MODEL))
			.toList();
	}

	private final GroupConcatOrderByStep STATE_IDS = DSL.groupConcatDistinct(WORKFLOW_STATUS.STATE_ID);

	private Field<String> constructWorkflowField(final List<Workflow> workflows) {
		if(workflows.size() == 1) {
			return DSL.inline(workflows.get(0).getId());
		}
		final var values = workflows.stream().collect(Collectors.toMap(Workflow::getAggregateWorkflowId, Workflow::getId));
		return DSL.case_(WORKFLOW_STATUS.WORKFLOW_ID).mapValues(values);
	}

	private Field<String> constructStateCase(final Workflow workflow) {
		CaseConditionStep<String> matcher = DSL.case_().when(DSL.falseCondition(), DSL.inline(""));

		final var matcherAllStates = workflow.getStates().stream()
			.filter(s -> s.getAggregateStateMatcher().equals(StateMatcher.ALL))
			.toList();
		for(final WorkflowState state : matcherAllStates) {
			matcher = matcher.when(STATE_IDS.eq(state.getAggregateStateId()), DSL.inline(state.getId()));
		}

		final var matcherOneStates = workflow.getStates().stream()
			.filter(s -> s.getAggregateStateMatcher().equals(StateMatcher.ONE))
			.toList();

		for(final WorkflowState state : matcherOneStates) {
			final var check = DSL.function("find_in_set", Integer.class, DSL.inline(state.getAggregateStateId()), STATE_IDS).greaterThan(0);
			matcher = matcher.when(check, DSL.inline(state.getId()));
		}

		final CaseConditionStep<String> m = matcher;
		workflow.getStates().stream()
			.filter(s -> s.getAggregateStateMatcher().equals(StateMatcher.DEFAULT))
			.findFirst()
			.ifPresent(s -> m.otherwise(s.getId()));

		return m;
	}

	private Field<String> constructStateCases(final List<Workflow> workflows) {
		final var values = workflows.stream().collect(Collectors.toMap(w -> DSL.inline(w.getAggregateWorkflowId()), w -> constructStateCase(w)));
		return DSL.case_(WORKFLOW_STATUS.WORKFLOW_ID).mapFields(values);
	}

	public Select<Record16<Long, ZonedDateTime, ZonedDateTime, Boolean, Long, Long, Long, Long, Long, Long, String, String, String, String, String, String>> generateScopeQuery(
		final Optional<Workflow> workflow,
		final Optional<Collection<Long>> scopePks
	) {
		final var workflows = workflow.map(Collections::singletonList).orElse(aggregatorScopeWorkflows);
		final var workflowCase = constructWorkflowField(workflows);
		final var stateCases = constructStateCases(workflows);

		//query conditions
		final var conditions = new ArrayList<Condition>();
		conditions.add(WORKFLOW_STATUS.DELETED.isFalse());
		//select only workflow that could be aggregated
		conditions.add(WORKFLOW_STATUS.WORKFLOW_ID.in(workflows.stream().map(Workflow::getAggregateWorkflowId).toList()));

		if(scopePks.isPresent()) {
			conditions.add(WORKFLOW_STATUS.SCOPE_FK.in(scopePks.get()));
		}

		//exclude workflow states that are attached directly on a scope (we want only workflow states on fields, forms or events included in a scope)
		conditions.add(WORKFLOW_STATUS.EVENT_FK.isNotNull().or(WORKFLOW_STATUS.FIELD_FK.isNotNull()).or(WORKFLOW_STATUS.FORM_FK.isNotNull()));
		//exclude deleted events
		conditions.add(WORKFLOW_STATUS.EVENT_FK.isNull().or(EVENT.DELETED.isFalse()));
		//exclude deleted forms
		conditions.add(WORKFLOW_STATUS.FORM_FK.isNull().or(FORM.DELETED.isFalse()));
		//exclude deleted datasets
		conditions.add(WORKFLOW_STATUS.FIELD_FK.isNull().or(DATASET.DELETED.isFalse()));
		//exclude fields that have null value
		conditions.add(WORKFLOW_STATUS.FIELD_FK.isNull().or(FIELD.VALUE.isNotNull()));

		final var groupByFields = new ArrayList<GroupField>();
		groupByFields.add(WORKFLOW_STATUS.SCOPE_FK);
		//it's required to group by workflow ids only if the query is done for multiple workflows
		if(workflows.size() > 1) {
			groupByFields.add(WORKFLOW_STATUS.WORKFLOW_ID);
		}

		return create.select(
			//select min(ws.pk) as aggregated workflow state pk
			//the goal is to be able to identify aggregated workflow status with a stable identifier
			//for example, this allows to use "distinct" on queries the aggregated workflow table
			//the columns are aliases as workflow status native columns to make the resulting rows look like rows from the real workflow status table
			DSL.min(WORKFLOW_STATUS.PK).as(WORKFLOW_STATUS.PK),
			DSL.min(WORKFLOW_STATUS.CREATION_TIME).as(WORKFLOW_STATUS.CREATION_TIME),
			DSL.max(WORKFLOW_STATUS.LAST_UPDATE_TIME).as(WORKFLOW_STATUS.LAST_UPDATE_TIME),
			DSL.inline(false, WORKFLOW_STATUS.DELETED).as(WORKFLOW_STATUS.DELETED),
			WORKFLOW_STATUS.SCOPE_FK,
			DSL.inline(null, WORKFLOW_STATUS.EVENT_FK).as(WORKFLOW_STATUS.EVENT_FK),
			DSL.inline(null, WORKFLOW_STATUS.FORM_FK).as(WORKFLOW_STATUS.FORM_FK),
			DSL.inline(null, WORKFLOW_STATUS.FIELD_FK).as(WORKFLOW_STATUS.FIELD_FK),
			DSL.inline(null, WORKFLOW_STATUS.USER_FK).as(WORKFLOW_STATUS.USER_FK),
			DSL.inline(null, WORKFLOW_STATUS.ROBOT_FK).as(WORKFLOW_STATUS.ROBOT_FK),
			DSL.inline(null, WORKFLOW_STATUS.PROFILE_ID).as(WORKFLOW_STATUS.PROFILE_ID),
			stateCases.as("state_id"),
			workflowCase.as(WORKFLOW_STATUS.WORKFLOW_ID),
			DSL.inline(null, WORKFLOW_STATUS.ACTION_ID).as(WORKFLOW_STATUS.ACTION_ID),
			DSL.inline(null, WORKFLOW_STATUS.VALIDATOR_ID).as(WORKFLOW_STATUS.VALIDATOR_ID),
			DSL.inline(null, WORKFLOW_STATUS.TRIGGER_MESSAGE).as(WORKFLOW_STATUS.TRIGGER_MESSAGE)
		)
			.from(WORKFLOW_STATUS)
			.leftJoin(EVENT).on(WORKFLOW_STATUS.EVENT_FK.eq(EVENT.PK))
			.leftJoin(FIELD).on(WORKFLOW_STATUS.FIELD_FK.eq(FIELD.PK))
			.leftJoin(DATASET).on(FIELD.DATASET_FK.eq(DATASET.PK))
			.leftJoin(FORM).on(WORKFLOW_STATUS.FORM_FK.eq(FORM.PK))
			.where(DSL.and(conditions))
			.groupBy(groupByFields)
			.having(DSL.field("state_id").isNotNull());
	}

	public Select<Record16<Long, ZonedDateTime, ZonedDateTime, Boolean, Long, Long, Long, Long, Long, Long, String, String, String, String, String, String>> generateEventQuery(
		final Optional<Workflow> workflow,
		final Optional<Collection<Long>> eventPks
	) {
		final var workflows = workflow.map(Collections::singletonList).orElse(aggregatorEventWorkflows);
		final var workflowCase = constructWorkflowField(workflows);
		final var stateCases = constructStateCases(workflows);

		//query conditions
		final var conditions = new ArrayList<Condition>();
		conditions.add(WORKFLOW_STATUS.DELETED.isFalse());
		//select only workflow that could be aggregated
		conditions.add(WORKFLOW_STATUS.WORKFLOW_ID.in(workflows.stream().map(Workflow::getAggregateWorkflowId).toList()));

		if(eventPks.isPresent()) {
			conditions.add(WORKFLOW_STATUS.EVENT_FK.in(eventPks.get()));
		}

		//exclude workflow states on scope (hence those with a null event fk)
		conditions.add(WORKFLOW_STATUS.EVENT_FK.isNotNull());
		//exclude workflow states that are attached directly on an event (we want only workflow states on fields or forms included in a event)
		conditions.add(WORKFLOW_STATUS.FIELD_FK.isNotNull().or(WORKFLOW_STATUS.FORM_FK.isNotNull()));
		//exclude deleted forms
		conditions.add(WORKFLOW_STATUS.FORM_FK.isNull().or(FORM.DELETED.isFalse()));
		//exclude deleted datasets
		conditions.add(WORKFLOW_STATUS.FIELD_FK.isNull().or(DATASET.DELETED.isFalse()));
		//exclude fields that have null value
		conditions.add(WORKFLOW_STATUS.FIELD_FK.isNull().or(FIELD.VALUE.isNotNull()));

		final var groupByFields = new ArrayList<GroupField>();
		groupByFields.add(WORKFLOW_STATUS.EVENT_FK);
		//it's required to group by workflow ids only if the query is done for multiple workflows
		if(workflows.size() > 1) {
			groupByFields.add(WORKFLOW_STATUS.WORKFLOW_ID);
		}

		return create.select(
			//select min(ws.pk) as aggregated workflow state pk
			//the goal is to be able to identify aggregated workflow status with a stable identifier
			//for example, this allows to use "distinct" on queries the aggregated workflow table
			//the columns are aliases as workflow status native columns to make the resulting rows look like rows from the real workflow status table
			DSL.min(WORKFLOW_STATUS.PK).as(WORKFLOW_STATUS.PK),
			DSL.min(WORKFLOW_STATUS.CREATION_TIME).as(WORKFLOW_STATUS.CREATION_TIME),
			DSL.max(WORKFLOW_STATUS.LAST_UPDATE_TIME).as(WORKFLOW_STATUS.LAST_UPDATE_TIME),
			DSL.inline(false, WORKFLOW_STATUS.DELETED).as(WORKFLOW_STATUS.DELETED),
			WORKFLOW_STATUS.SCOPE_FK,
			WORKFLOW_STATUS.EVENT_FK,
			DSL.inline(null, WORKFLOW_STATUS.FORM_FK).as(WORKFLOW_STATUS.FORM_FK),
			DSL.inline(null, WORKFLOW_STATUS.FIELD_FK).as(WORKFLOW_STATUS.FIELD_FK),
			DSL.inline(null, WORKFLOW_STATUS.USER_FK).as(WORKFLOW_STATUS.USER_FK),
			DSL.inline(null, WORKFLOW_STATUS.ROBOT_FK).as(WORKFLOW_STATUS.ROBOT_FK),
			DSL.inline(null, WORKFLOW_STATUS.PROFILE_ID).as(WORKFLOW_STATUS.PROFILE_ID),
			stateCases.as("state_id"),
			workflowCase.as(WORKFLOW_STATUS.WORKFLOW_ID),
			DSL.inline(null, WORKFLOW_STATUS.ACTION_ID).as(WORKFLOW_STATUS.ACTION_ID),
			DSL.inline(null, WORKFLOW_STATUS.VALIDATOR_ID).as(WORKFLOW_STATUS.VALIDATOR_ID),
			DSL.inline(null, WORKFLOW_STATUS.TRIGGER_MESSAGE).as(WORKFLOW_STATUS.TRIGGER_MESSAGE)
		)
			.from(WORKFLOW_STATUS)
			.leftJoin(FIELD).on(WORKFLOW_STATUS.FIELD_FK.eq(FIELD.PK))
			.leftJoin(DATASET).on(FIELD.DATASET_FK.eq(DATASET.PK))
			.leftJoin(FORM).on(WORKFLOW_STATUS.FORM_FK.eq(FORM.PK))
			.where(DSL.and(conditions))
			.groupBy(groupByFields)
			.having(DSL.field("state_id").isNotNull());
	}

	//refrain from creating a similar method for forms
	//never forget that there is no link between forms and fields in database
	//this means there is no way to aggregate fields workflow statuses on forms using the database
	//public Select<Record16<Long, ZonedDateTime, ZonedDateTime, Boolean, Long, Long, Long, Long, Long, Long, String, String, String, String, String, String>> generateFormQuery()

	public List<AggregateWorkflowStatus> getAggregateWorkflowStatusByScopes(final Collection<Scope> scopes) {
		if(aggregatorScopeWorkflows.isEmpty()) {
			return Collections.emptyList();
		}
		final var scopeByPks = scopes.stream().collect(Collectors.toMap(Scope::getPk, Function.identity()));
		final List<AggregateWorkflowStatus> statuses = new ArrayList<>();
		final var query = generateScopeQuery(Optional.empty(), Optional.of(scopeByPks.keySet())).coerce(WORKFLOW_STATUS);
		//when retrieving aggregate workflow statuses for multiple scopes at once, the query may return workflow statuses that do not exist for all scopes
		//filter these workflow statuses according to the scope model
		for(final var status : strategy.executeQuery(query, AggregateWorkflowStatus.class)) {
			final var scope = scopeByPks.get(status.getScopeFk());
			if(scope.getScopeModel().getWorkflowIds().contains(status.getWorkflowId())) {
				statuses.add(status);
			}
		}
		return statuses;
	}

	public List<AggregateWorkflowStatus> getAggregateWorkflowStatusByScope(final Scope scope) {
		return getAggregateWorkflowStatusByScopes(Collections.singleton(scope));
	}

	public List<AggregateWorkflowStatus> getAggregateWorkflowStatusByEvents(final Collection<Event> events) {
		if(aggregatorEventWorkflows.isEmpty()) {
			return Collections.emptyList();
		}
		final var eventByPks = events.stream().collect(Collectors.toMap(Event::getPk, Function.identity()));
		final List<AggregateWorkflowStatus> statuses = new ArrayList<>();
		final var query = generateEventQuery(Optional.empty(), Optional.of(eventByPks.keySet())).coerce(WORKFLOW_STATUS);
		//when retrieving aggregate workflow statuses for multiple events at once, the query may return workflow statuses that do not exist for all events
		//filter these workflow statuses according to the event model
		for(final var status : strategy.executeQuery(query, AggregateWorkflowStatus.class)) {
			final var event = eventByPks.get(status.getEventFk());
			if(event.getEventModel().getWorkflowIds().contains(status.getWorkflowId())) {
				statuses.add(status);
			}
		}
		return statuses;
	}

	public List<AggregateWorkflowStatus> getAggregateWorkflowStatusByEvent(final Event event) {
		return getAggregateWorkflowStatusByEvents(Collections.singleton(event));
	}

	//never forget that there is no link between forms and fields in database
	//this means there is no way to aggregate fields workflows on forms using the database
	public List<AggregateWorkflowStatus> getAggregateWorkflowStatusByForms(final Scope scope, final Optional<Event> event, final List<Form> forms) {
		final var containedStatuses = workflowStatusService.getContained(scope, event, forms);
		final var formStatuses = new ArrayList<AggregateWorkflowStatus>();
		for(final var form : forms) {
			formStatuses.addAll(getAggregateWorkflowStatusByForm(form, containedStatuses.get(form)));
		}
		return formStatuses;
	}

	public List<AggregateWorkflowStatus> getAggregateWorkflowStatusByForm(final Scope scope, final Optional<Event> event, final Form form) {
		return getAggregateWorkflowStatusByForms(scope, event, Collections.singletonList(form));
	}

	private Optional<WorkflowState> retriveState(final List<WorkflowStatus> statuses, final Workflow workflow) {
		if(!statuses.isEmpty()) {
			for(final var state : workflow.getStatesHavingMatcher(StateMatcher.ALL)) {
				if(statuses.stream().allMatch(s -> s.getStateId().equals(state.getAggregateStateId()))) {
					return Optional.of(state);
				}
			}
			for(final var state : workflow.getStatesHavingMatcher(StateMatcher.ONE)) {
				if(statuses.stream().anyMatch(s -> s.getStateId().equals(state.getAggregateStateId()))) {
					return Optional.of(state);
				}
			}
			final var defaultStates = workflow.getStatesHavingMatcher(StateMatcher.DEFAULT);
			if(defaultStates.size() > 0) {
				return Optional.of(defaultStates.get(0));
			}
		}
		return Optional.<WorkflowState> empty();
	}

	private List<AggregateWorkflowStatus> getAggregateWorkflowStatusByForm(final Form form, final List<WorkflowStatus> containedStatuses) {
		final List<AggregateWorkflowStatus> formStatuses = new ArrayList<>();
		for(final Workflow workflow : form.getFormModel().getWorkflows()) {
			if(workflow.isAggregator()) {
				final List<WorkflowStatus> statuses = containedStatuses.stream().filter(ws -> ws.getWorkflowId().equals(workflow.getAggregateWorkflowId())).toList();
				final Optional<WorkflowState> state = retriveState(statuses, workflow);
				if(state.isPresent()) {
					final var status = new AggregateWorkflowStatus();
					status.setWorkflow(workflow);
					status.setState(state.get());
					status.setScopeFk(form.getScopeFk());
					status.setEventFk(form.getEventFk());
					status.setFormFk(form.getPk());
					formStatuses.add(status);
				}
			}
		}
		return formStatuses;
	}

}
