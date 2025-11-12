package ch.rodano.core.services.bll.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
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

	private List<Dataset> getDatasets(final Optional<Event> event, final List<Dataset> scopeDatasets, final List<Dataset> eventDatasets, final UUID datasetModelId) {
		final boolean isEventDataset = event.isPresent() && event.get().getEventModel().getDatasetModelUuids().contains(datasetModelId);
		final var datasets = isEventDataset ? eventDatasets : scopeDatasets;
		return datasets.stream().filter(d -> d.getDatasetModelId().equals(datasetModelId)).toList();
	}

	public FormContent generateFormContent(final Scope scope, final Optional<Event> event, final Form form) {
		final List<Dataset> scopeDatasets = datasetDAOService.getAllDatasetsByScopePk(scope.getPk());
		final List<Dataset> eventDatasets = event.map(Event::getPk).map(datasetDAOService::getAllDatasetsByEventPk).orElse(Collections.emptyList());
		final var fields = fieldDAOService.getFieldsRelatedToEvent(scope.getPk(), event.map(Event::getPk));
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
		final var fieldsByDataset = fields.stream()
			.collect(Collectors.groupingBy(Field::getDatasetFk, Collectors.mapping(Function.identity(), Collectors.toSet())));

		final List<LayoutContent> singleLayouts = new ArrayList<>();
		final List<LayoutContent> repeatableLayouts = new ArrayList<>();

		final List<Layout> layouts = form.getFormModel().getLayouts().stream()
			.filter(l -> isLayoutValid(l, form, scope, event))
			.toList();

		for(final Layout layout : layouts) {
			final var cells = layout.getCells().stream()
				.filter(Cell::hasFieldModel)
				.filter(c -> isCellValid(c, form, scope, event))
				.toList();

			//add entire datasets if the layout is a multiple layout
			if(layout.getType().isRepeatable()) {
				//retrieve all field models in this document (hence in this layout)
				final List<UUID> fieldModelIds = cells.stream()
					.filter(Cell::hasFieldModel)
					.map(Cell::getFieldModelUuid)
					.toList();
				//retrieve all datasets for this layout
				final List<Dataset> datasets = getDatasets(event, scopeDatasets, eventDatasets, layout.getDatasetModelUuid());
				//gather all fields in this layout
				final var datasetsFields = new ArrayList<Pair<Dataset, Field>>();
				for(final Dataset dataset : datasets) {
					fieldsByDataset.get(dataset.getPk()).stream()
						.filter(f -> fieldModelIds.contains(f.getFieldModelId()))
						.map(f -> Pair.of(dataset, f))
						.forEach(datasetsFields::add);
				}
				repeatableLayouts.add(new LayoutContent(layout, datasetsFields));
			}
			//retrieve datasets and fields if it's a non multiple layout
			else {
				final var datasetsFields = new ArrayList<Pair<Dataset, Field>>();
				for(final Cell cell : cells) {
					final UUID datasetModelId = cell.getDatasetModelUuid();
					//retrieve the datasets for this cell
					final Dataset dataset = getDatasets(event, scopeDatasets, eventDatasets, datasetModelId).getFirst();
					//gather the fields in this cell
					final var field = fieldsByDataset.get(dataset.getPk()).stream()
						.filter(f -> f.getFieldModelId().equals(cell.getFieldModelUuid()))
						.findAny().orElseThrow();
					datasetsFields.add(Pair.of(dataset, field));
				}
				singleLayouts.add(new LayoutContent(layout, datasetsFields));
			}
		}

		return new FormContent(singleLayouts, repeatableLayouts);
	}
}
