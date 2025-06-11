package ch.rodano.core.model.rules.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;

public record DataState(
	Set<Scope> scopes,
	Set<Event> events,
	Set<Dataset> datasets,
	Set<Field> fields,
	Set<Form> forms,
	Set<WorkflowStatus> workflows,
	RulableEntity reference
) {

	private static <T> Set<T> optionalToSet(final Optional<T> optional) {
		return optional.map(Collections::singleton).orElse(Collections.emptySet());
	}

	private static RulableEntity familyToRuleEntity(final DataFamily family) {
		return switch(family.getDeepestEntity().getWorkflowableEntity()) {
			case FIELD -> RulableEntity.FIELD;
			case FORM -> RulableEntity.FORM;
			case EVENT -> RulableEntity.EVENT;
			case SCOPE -> RulableEntity.SCOPE;
		};
	}

	public DataState(
		final Set<Scope> scopes,
		final Set<Event> events,
		final Set<Dataset> datasets,
		final Set<Field> fields,
		final Set<Form> forms,
		final Set<WorkflowStatus> workflows,
		final RulableEntity reference
	) {
		this.scopes = scopes;
		this.events = events;
		this.forms = forms;
		this.datasets = datasets;
		this.fields = fields;
		this.workflows = workflows;
		this.reference = reference;
	}

	public DataState(final DataState state) {
		this(
			new HashSet<>(state.scopes),
			new HashSet<>(state.events),
			new HashSet<>(state.datasets),
			new HashSet<>(state.fields),
			new HashSet<>(state.forms),
			new HashSet<>(state.workflows),
			state.reference
		);
	}

	public DataState(final DataFamily family) {
		this(
			Collections.singleton(family.scope()),
			optionalToSet(family.event()),
			optionalToSet(family.dataset()),
			optionalToSet(family.field()),
			optionalToSet(family.form()),
			Collections.emptySet(),
			familyToRuleEntity(family)
		);
	}

	public DataState(final DataFamily family, final WorkflowStatus workflowStatus) {
		this(
			Collections.singleton(family.scope()),
			optionalToSet(family.event()),
			optionalToSet(family.dataset()),
			optionalToSet(family.field()),
			optionalToSet(family.form()),
			Collections.singleton(workflowStatus),
			familyToRuleEntity(family)
		);
	}

	public DataState(final Scope scope) {
		this(
			Collections.singleton(scope),
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.emptySet(),
			RulableEntity.SCOPE
		);
	}

	public DataState(final Scope scope, final WorkflowStatus workflowStatus) {
		this(
			Collections.singleton(scope),
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.singleton(workflowStatus),
			RulableEntity.WORKFLOW
		);
	}

	public DataState(final Scope scope, final Event event) {
		this(
			Collections.singleton(scope),
			Collections.singleton(event),
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.emptySet(),
			RulableEntity.EVENT
		);
	}

	public DataState(final Scope scope, final Event event, final WorkflowStatus workflowStatus) {
		this(
			Collections.singleton(scope),
			Collections.singleton(event),
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.singleton(workflowStatus),
			RulableEntity.WORKFLOW
		);
	}

	public DataState(final Scope scope, final Optional<Event> event, final Form form) {
		this(
			Collections.singleton(scope),
			event.map(Collections::singleton).orElse(Collections.emptySet()),
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.singleton(form),
			Collections.emptySet(),
			RulableEntity.FORM
		);
	}

	public DataState(final Scope scope, final Optional<Event> event, final Form form, final WorkflowStatus workflowStatus) {
		this(
			Collections.singleton(scope),
			event.map(Collections::singleton).orElse(Collections.emptySet()),
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.singleton(form),
			Collections.singleton(workflowStatus),
			RulableEntity.WORKFLOW
		);
	}

	public DataState(final Scope scope, final Optional<Event> event, final Dataset dataset) {
		this(
			Collections.singleton(scope),
			event.map(Collections::singleton).orElse(Collections.emptySet()),
			Collections.singleton(dataset),
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.emptySet(),
			RulableEntity.DATASET
		);
	}

	public DataState(final Scope scope, final Optional<Event> event, final Dataset dataset, final Field field) {
		this(
			Collections.singleton(scope),
			event.map(Collections::singleton).orElse(Collections.emptySet()),
			Collections.singleton(dataset),
			Collections.singleton(field),
			Collections.emptySet(),
			Collections.emptySet(),
			RulableEntity.FIELD
		);
	}

	public DataState(final Scope scope, final Optional<Event> event, final Dataset dataset, final Field field, final WorkflowStatus workflowStatus) {
		this(
			Collections.singleton(scope),
			event.map(Collections::singleton).orElse(Collections.emptySet()),
			Collections.singleton(dataset),
			Collections.singleton(field),
			Collections.emptySet(),
			Collections.singleton(workflowStatus),
			RulableEntity.WORKFLOW
		);
	}

	public DataState(final Scope scope, final Optional<Event> event, final Dataset dataset, final Field field, final Form form) {
		this(
			Collections.singleton(scope),
			event.map(Collections::singleton).orElse(Collections.emptySet()),
			Collections.singleton(dataset),
			Collections.singleton(field),
			Collections.singleton(form),
			Collections.emptySet(),
			//in this case, the reference entity could be FORM or FIELD. FIELD seems to be a better choice because ot's more "data-oriented"
			//moreover, this method is only used when calculating constraint on cells, where the field seems to be the most important element in the DataState
			RulableEntity.FIELD
		);
	}

	public final boolean isValid() {
		return !getReferenceEvaluables().isEmpty();
	}

	public final Set<Evaluable> getReferenceEvaluables() {
		return getEvaluables(reference);
	}

	public final Set<Evaluable> getEvaluables(final RulableEntity entity) {
		return switch(entity) {
			case SCOPE -> new HashSet<>(scopes);
			case EVENT -> new HashSet<>(events);
			case DATASET -> new HashSet<>(datasets);
			case FIELD -> new HashSet<>(fields);
			case FORM -> new HashSet<>(forms);
			default -> new HashSet<>(workflows);
		};
	}

	public DataState withReference(final RulableEntity newReference) {
		return new DataState(scopes, events, datasets, fields, forms, workflows, newReference);
	}

	public DataState withScopes(final Set<Scope> newScopes) {
		return new DataState(Collections.unmodifiableSet(newScopes), events, datasets, fields, forms, workflows, reference);
	}

	public DataState withVisits(final Set<Event> newVisits) {
		return new DataState(scopes, Collections.unmodifiableSet(newVisits), datasets, fields, forms, workflows, reference);
	}

	public DataState withDatasets(final Set<Dataset> newDatasets) {
		return new DataState(scopes, events, Collections.unmodifiableSet(newDatasets), fields, forms, workflows, reference);
	}

	public DataState withFields(final Set<Field> newFields) {
		return new DataState(scopes, events, datasets, Collections.unmodifiableSet(newFields), forms, workflows, reference);
	}

	public DataState withForms(final Set<Form> newForms) {
		return new DataState(scopes, events, datasets, fields, Collections.unmodifiableSet(newForms), workflows, reference);
	}

	public DataState withWorkflows(final Set<WorkflowStatus> newWorkflows) {
		return new DataState(scopes, events, datasets, fields, forms, Collections.unmodifiableSet(newWorkflows), reference);
	}

	public final DataState withEvaluables(
		final RulableEntity entity,
		final Set<Evaluable> evaluables
	) {
		switch(entity) {
			case SCOPE -> {
				final var newScopes = new HashSet<Scope>();
				for(final var evaluable : evaluables) {
					newScopes.add((Scope) evaluable);
				}
				return withScopes(newScopes);
			}
			case EVENT -> {
				final var newVisits = new HashSet<Event>();
				for(final var evaluable : evaluables) {
					newVisits.add((Event) evaluable);
				}
				return withVisits(newVisits);
			}
			case DATASET -> {
				final var newDatasets = new HashSet<Dataset>();
				for(final var evaluable : evaluables) {
					newDatasets.add((Dataset) evaluable);
				}
				return withDatasets(newDatasets);
			}
			case FIELD -> {
				final var newFields = new HashSet<Field>();
				for(final var evaluable : evaluables) {
					newFields.add((Field) evaluable);
				}
				return withFields(newFields);
			}
			case FORM -> {
				final var newForms = new HashSet<Form>();
				for(final var evaluable : evaluables) {
					newForms.add((Form) evaluable);
				}
				return withForms(newForms);
			}
			default -> {
				final var newWorkflows = new HashSet<WorkflowStatus>();
				for(final var evaluable : evaluables) {
					newWorkflows.add((WorkflowStatus) evaluable);
				}
				return withWorkflows(newWorkflows);
			}
		}
	}

	public DataState withOnly(final RulableEntity entity) {
		return new DataState(
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.emptySet(),
			Collections.emptySet(),
			entity
		).withEvaluables(entity, getEvaluables(entity));
	}

	@Override
	public final String toString() {
		final var label = new StringBuilder("DataState : Scopes=[");
		label.append(scopes.stream().map(Scope::getCode).collect(Collectors.joining(",")));
		label.append("] Events=[");
		label.append(events.stream().map(Event::getEventModelId).collect(Collectors.joining(",")));
		label.append("] Datasets=[");
		label.append(datasets.stream().map(Dataset::getDatasetModelId).collect(Collectors.joining(",")));
		label.append("] Fields=[");
		label.append(fields.stream().map(Field::getFieldModelId).collect(Collectors.joining(",")));
		label.append("] Forms=[");
		label.append(forms.stream().map(Form::getFormModelId).collect(Collectors.joining(",")));
		label.append("] Workflow statuses=[");
		label.append(workflows.stream().map(WorkflowStatus::getId).collect(Collectors.joining(",")));
		label.append("] Reference entity=[");
		label.append(reference().name());
		label.append("]");
		return label.toString();
	}
}
