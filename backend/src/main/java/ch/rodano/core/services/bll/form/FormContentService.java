package ch.rodano.core.services.bll.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.layout.Cell;
import ch.rodano.configuration.model.layout.Layout;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.rules.data.ConstraintEvaluationService;
import ch.rodano.core.model.rules.data.DataEvaluation;
import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.dao.dataset.DatasetDAOService;
import ch.rodano.core.services.dao.field.FieldDAOService;

@Service
public class FormContentService {

	private final DatasetDAOService datasetDAOService;
	private final FieldDAOService fieldDAOService;
	private final ConstraintEvaluationService constraintEvaluationService;

	public FormContentService(
		final DatasetDAOService datasetDAOService,
		final FieldDAOService fieldDAOService,
		final ConstraintEvaluationService constraintEvaluationService
	) {
		this.datasetDAOService = datasetDAOService;
		this.fieldDAOService = fieldDAOService;
		this.constraintEvaluationService = constraintEvaluationService;
	}

	private boolean isLayoutValid(final Layout layout, final Form form, final Scope scope, final Optional<Event> event) {
		if(layout.getConstraint() == null) {
			return true;
		}
		final var state = new DataState(scope, event, form);
		final var evaluation = new DataEvaluation(state, layout.getConstraint());
		constraintEvaluationService.evaluate(evaluation);
		return evaluation.isValid();
	}

	private boolean isCellValid(final Cell cell, final Form form, final Scope scope, final Optional<Event> event) {
		if(cell.getConstraint() == null) {
			return true;
		}
		final var state = new DataState(scope, event, form);
		final var evaluation = new DataEvaluation(state, cell.getConstraint());
		constraintEvaluationService.evaluate(evaluation);
		return evaluation.isValid();
	}

	private List<Dataset> getDatasets(final Optional<Event> event, final List<Dataset> scopeDatasets, final List<Dataset> eventDatasets, final String datasetModelId) {
		final boolean isEventDataset = event.isPresent() && event.get().getEventModel().getDatasetModelIds().contains(datasetModelId);
		final var datasets = isEventDataset ? eventDatasets : scopeDatasets;
		return datasets.stream().filter(d -> d.getDatasetModelId().equals(datasetModelId)).toList();
	}

	public List<FormContent> generateFormContents(final Scope scope, final Optional<Event> event, final List<Form> forms) {
		final List<Dataset> scopeDatasets = datasetDAOService.getAllDatasetsByScopePk(scope.getPk());
		final List<Dataset> eventDatasets = event.map(Event::getPk).map(datasetDAOService::getAllDatasetsByEventPk).orElse(Collections.emptyList());
		final var datasetPks = Stream.concat(scopeDatasets.stream(), eventDatasets.stream()).map(Dataset::getPk).toList();
		final var fields = fieldDAOService.getFieldsByDatasetPks(datasetPks);
		return forms.stream()
			.map(form -> generateFormContent(scope, event, form, scopeDatasets, eventDatasets, fields))
			.toList();
	}

	public FormContent generateFormContent(final Scope scope, final Optional<Event> event, final Form form) {
		final List<Dataset> scopeDatasets = datasetDAOService.getAllDatasetsByScopePk(scope.getPk());
		final List<Dataset> eventDatasets = event.map(Event::getPk).map(datasetDAOService::getAllDatasetsByEventPk).orElse(Collections.emptyList());
		final var datasetPks = Stream.concat(scopeDatasets.stream(), eventDatasets.stream()).map(Dataset::getPk).toList();
		final var fields = fieldDAOService.getFieldsByDatasetPks(datasetPks);
		return generateFormContent(scope, event, form, scopeDatasets, eventDatasets, fields);
	}

	public FormContent generateFormContent(
		final Scope scope,
		final Optional<Event> event,
		final Form form,
		final List<Dataset> scopeDatasets,
		final List<Dataset> eventDatasets,
		final List<Field> fields
	) {
		//index fields by their parent dataset for fast lookups while building layout contents
		final var fieldsByDatasetPk = fields.stream()
			.collect(Collectors.groupingBy(Field::getDatasetFk, Collectors.toSet()));

		final List<LayoutContent> singleLayouts = new ArrayList<>();
		final List<LayoutContent> repeatableLayouts = new ArrayList<>();

		final List<Layout> layouts = form.getFormModel().getLayouts().stream()
			.filter(l -> isLayoutValid(l, form, scope, event))
			.toList();

		for(final Layout layout : layouts) {
			final List<Cell> cells = layout.getCells().stream()
				.filter(Cell::hasFieldModel)
				.filter(c -> isCellValid(c, form, scope, event))
				.toList();

			final Map<Dataset, Set<Field>> layoutFields = new LinkedHashMap<>();

			//a repeatable layout produces one row per dataset
			if(layout.getType().isRepeatable()) {
				final List<String> fieldModelIds = cells.stream().map(Cell::getFieldModelId).toList();
				final List<Dataset> datasets = getDatasets(event, scopeDatasets, eventDatasets, layout.getDatasetModelId());

				for(final Dataset dataset : datasets) {
					final var datasetFields = fieldsByDatasetPk.get(dataset.getPk()).stream()
						.filter(f -> fieldModelIds.contains(f.getFieldModelId()))
						.collect(Collectors.toSet());
					layoutFields.put(dataset, datasetFields);
				}
				repeatableLayouts.add(new LayoutContent(layout, layoutFields));
			}
			//a single layout resolves each cell to exactly one (dataset, field) pair
			else {
				for(final Cell cell : cells) {
					final Dataset dataset = getDatasets(event, scopeDatasets, eventDatasets, cell.getDatasetModelId()).getFirst();
					final Field field = fieldsByDatasetPk.get(dataset.getPk()).stream()
						.filter(f -> f.getFieldModelId().equals(cell.getFieldModelId()))
						.findAny()
						.orElseThrow();
					layoutFields.computeIfAbsent(dataset, _ -> new HashSet<>()).add(field);
				}
				singleLayouts.add(new LayoutContent(layout, layoutFields));
			}
		}

		return new FormContent(form, singleLayouts, repeatableLayouts);
	}
}
