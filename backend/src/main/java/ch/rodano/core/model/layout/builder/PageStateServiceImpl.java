package ch.rodano.core.model.layout.builder;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.layout.Layout;
import ch.rodano.configuration.model.layout.VisibilityCriterionAction;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.rules.data.ConstraintEvaluationService;
import ch.rodano.core.model.rules.data.DataEvaluation;
import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.field.FieldService;

@Service
public class PageStateServiceImpl implements PageStateService {

	private final DatasetService datasetService;
	private final FieldService fieldService;
	private final ConstraintEvaluationService constraintEvaluationService;

	public PageStateServiceImpl(
		final DatasetService datasetService,
		final FieldService fieldService,
		final ConstraintEvaluationService constraintEvaluationService
	) {
		this.datasetService = datasetService;
		this.fieldService = fieldService;
		this.constraintEvaluationService = constraintEvaluationService;
	}

	@Override
	public PageState createPageState(final Scope scope, final Optional<Event> event, final Form form) {
		final var pageState = new PageState(scope, event, form.getFormModel());

		for(final var layout : form.getFormModel().getLayouts()) {
			var valid = true;
			if(layout.getConstraint() != null) {
				final var state = new DataState(scope, event, form);
				final var evaluation = new DataEvaluation(state, layout.getConstraint());
				constraintEvaluationService.evaluate(evaluation);
				if(!evaluation.isValid()) {
					valid = false;
				}
			}

			if(valid) {
				pageState.addLayoutGroupState(createLayoutGroup(scope, event, form, layout, pageState));
			}
		}

		return pageState;
	}

	public LayoutGroupState createLayoutGroup(final Scope scope, final Optional<Event> event, final Form form, final Layout layout, final PageState pageState) {
		final var layoutGroupState = new LayoutGroupState(pageState, layout);

		//create layout states
		//MULTIPLE
		if(layout.getType().isRepeatable()) {
			for(final var dataset : getDatasets(scope, event, layout.getDatasetModel())) {
				layoutGroupState.addLayoutState(createLayout(scope, event, form, layoutGroupState, Collections.singletonMap(dataset.getDatasetModelId(), dataset)));
			}
		}
		//SINGLE
		else {
			final SortedMap<String, Dataset> datasets = new TreeMap<>();
			for(final var line : layout.getLines()) {
				for(final var cell : line.getCells()) {
					if(cell.hasFieldModel()) {
						if(!datasets.containsKey(cell.getDatasetModelId())) {
							datasets.put(cell.getDatasetModelId(), getDataset(scope, event, cell.getDatasetModel()));
						}
					}
				}
			}
			layoutGroupState.addLayoutState(createLayout(scope, event, form, layoutGroupState, datasets));
		}
		return layoutGroupState;
	}

	public LayoutState createLayout(final Scope scope, final Optional<Event> event, final Form form, final LayoutGroupState layoutGroupState, final Map<String, Dataset> datasets) {
		final var layoutState = new LayoutState(layoutGroupState, datasets);

		//create cell states
		for(final var cell : layoutGroupState.getLayout().getCells()) {
			Dataset dataset = null;
			Field field = null;
			if(cell.hasFieldModel()) {
				dataset = datasets.get(cell.getDatasetModelId());
				field = fieldService.get(dataset, cell.getFieldModel());
			}
			//build cell state
			final var cellState = new CellState(layoutState, cell, dataset, field);
			//test conditions
			var valid = true;
			if(cell.getConstraint() != null) {
				final var state = new DataState(scope, event, dataset, field, form);
				final var evaluation = new DataEvaluation(state, cell.getConstraint());
				constraintEvaluationService.evaluate(evaluation);
				if(!evaluation.isValid()) {
					valid = false;
				}
			}

			if(valid) {
				layoutState.addCellState(cellState);
			}
		}
		return layoutState;
	}

	private List<Dataset> getDatasets(final Scope scope, final Optional<Event> event, final DatasetModel datasetModel) {
		//check if datasets are on the event or the scope
		if(event.isPresent() && event.get().getEventModel().getDatasetModelIds().contains(datasetModel.getId())) {
			return datasetService.getAllIncludingRemoved(event.get(), Collections.singleton(datasetModel));
		}
		return datasetService.getAllIncludingRemoved(scope, Collections.singleton(datasetModel));
	}

	private Dataset getDataset(final Scope scope, final Optional<Event> event, final DatasetModel datasetModel) {
		//check if datasets are on the event or the scope
		if(event.isPresent() && event.get().getEventModel().getDatasetModelIds().contains(datasetModel.getId())) {
			return datasetService.get(event.get(), datasetModel);
		}
		return datasetService.get(scope, datasetModel);
	}

	@Override
	public void initPage(final PageState pageState, final Optional<ZonedDateTime> date, final boolean clean) {
		for(final var layoutGroupState : pageState.getLayoutGroupStates()) {
			for(final var layoutState : layoutGroupState.getLayoutStates()) {
				for(final var cellState : layoutState.getCellStates()) {
					//nothing to do if cell has already been hidden during an initialization or if it does not have any visibility criteria
					//all cells are initialized with a visibility equals to true
					//a cell which is hidden has necessary been hidden by an other cell during the initialization of the page representation and must not change visibility of its own targets
					if(cellState.isVisible() && !cellState.getCell().getVisibilityCriteria().isEmpty()) {
						//visibility criteria on cell
						final var fieldModel = cellState.getCell().getFieldModel();
						for(final var vc : cellState.getCell().getVisibilityCriteria()) {
							var valid = false;
							if(FieldModelType.CHECKBOX_GROUP.equals(fieldModel.getType())) {
								final Collection<String> values = (Collection<String>) fieldService.getValueObject(cellState.getField(), date);
								for(final var value : values) {
									if(vc.getValues().contains(value)) {
										valid = true;
										break;
									}
								}
							}
							else {
								//retrieve value in provided values or get real value
								//provided map of values may not contains all values (read only values are not submitted by Tapestry and won't be in this map)
								final var value = fieldService.getLatestValue(cellState.getField(), date);
								valid = vc.getValues().contains(value);
							}
							if(valid) {
								for(final var targetId : vc.getTargetLayoutIds()) {
									final var target = cellState.getTargetLayout(targetId);
									//target may not exist in case of conditionable layouts
									if(target.isPresent()) {
										if(vc.getAction().equals(VisibilityCriterionAction.HIDE)) {
											setLayoutGroupVisible(pageState, target.get(), false, clean);
										}
										else if(vc.getAction().equals(VisibilityCriterionAction.SHOW)) {
											setLayoutGroupVisible(pageState, target.get(), true, clean);
										}
									}
								}
								for(final var targetId : vc.getTargetCellIds()) {
									final var target = cellState.getTargetCell(targetId);
									//target may not exist in case of conditionable cells
									if(target.isPresent()) {
										if(vc.getAction().equals(VisibilityCriterionAction.HIDE)) {
											setCellVisible(pageState, target.get(), false, clean);
										}
										else if(vc.getAction().equals(VisibilityCriterionAction.SHOW)) {
											setCellVisible(pageState, target.get(), true, clean);
										}
									}
								}
							}
							else {
								for(final var targetId : vc.getTargetLayoutIds()) {
									final var target = cellState.getTargetLayout(targetId);
									//target may not exist in case of conditionable layouts
									if(target.isPresent()) {
										if(vc.getAction().equals(VisibilityCriterionAction.SHOW)) {
											setLayoutGroupVisible(pageState, target.get(), false, clean);
										}
										else if(vc.getAction().equals(VisibilityCriterionAction.HIDE)) {
											setLayoutGroupVisible(pageState, target.get(), true, clean);
										}
									}
								}
								for(final var targetId : vc.getTargetCellIds()) {
									final var target = cellState.getTargetCell(targetId);
									//target may not exist in case of conditionable cells
									if(target.isPresent()) {
										if(vc.getAction().equals(VisibilityCriterionAction.SHOW)) {
											setCellVisible(pageState, target.get(), false, clean);
										}
										else if(vc.getAction().equals(VisibilityCriterionAction.HIDE)) {
											setCellVisible(pageState, target.get(), true, clean);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public final void setLayoutGroupVisible(final PageState pageState, final LayoutGroupState layoutGroupState, final boolean visible, final boolean clean) {
		layoutGroupState.setVisible(visible);
		if(clean && !visible) {
			//delete all layout states if is is a repeatable layout
			if(layoutGroupState.getLayout().getType().isRepeatable()) {
				if(layoutGroupState.getLayout().getDatasetModel().getScopeModels().isEmpty()) {
					for(final var layoutState : layoutGroupState.getLayoutStates()) {
						final var dataset = layoutState.getReferenceDataset();
						datasetService.delete(pageState.getScope(), pageState.getVisit(), dataset, null, "Dataset reset");
					}
				}
			}
			else {
				for(final var layoutState : layoutGroupState.getLayoutStates()) {
					for(final var cellState : layoutState.getCellStates()) {
						setCellVisible(pageState, cellState, visible, clean);
					}
				}
			}
		}
	}

	public final void setCellVisible(final PageState pageState, final CellState cellState, final boolean visible, final boolean clean) {
		cellState.setVisible(visible);
		if(!visible) {
			//reset value only if asked
			/*if(clean && cellState.hasField()) {
				fieldService.resetField(getField(), null, "Hide cell");
			}*/

			//when a cell is reset, all its targets must be reset too
			for(final var vc : cellState.getCell().getVisibilityCriteria()) {
				for(final var targetId : vc.getTargetLayoutIds()) {
					final var target = cellState.getTargetLayout(targetId);
					if(target.isPresent()) {
						setLayoutGroupVisible(pageState, target.get(), false, clean);
					}
				}
				for(final var targetId : vc.getTargetCellIds()) {
					final var target = cellState.getTargetCell(targetId);
					if(target.isPresent()) {
						setCellVisible(pageState, target.get(), false, clean);
					}
				}
			}
		}
	}
}
