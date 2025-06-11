package ch.rodano.api.config;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.layout.Cell;
import ch.rodano.configuration.model.layout.Layout;
import ch.rodano.configuration.model.layout.Line;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.rules.data.ConstraintEvaluationService;
import ch.rodano.core.model.rules.data.DataEvaluation;
import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.utils.ACL;

@Service
public class ConfigDTOServiceImpl implements ConfigDTOService {

	private final ActorService actorService;
	private final ConstraintEvaluationService constraintEvaluationService;

	public ConfigDTOServiceImpl(
		final ActorService actorService,
		final ConstraintEvaluationService constraintEvaluationService
	) {
		this.actorService = actorService;
		this.constraintEvaluationService = constraintEvaluationService;
	}

	@Override
	public ScopeModelDTO createScopeModelDTO(final ScopeModel scopeModel, final ACL acl) {
		final var dto = new ScopeModelDTO();
		dto.id = scopeModel.getId();
		dto.shortname = scopeModel.getShortname();
		dto.longname = scopeModel.getLongname();
		dto.pluralShortname = scopeModel.getPluralShortname();
		dto.description = scopeModel.getDescription();

		dto.parentIds = scopeModel.getParentIds();
		dto.defaultParentId = scopeModel.getDefaultParentId();
		dto.childScopeModelIds = scopeModel.getStudy().getScopeModels().stream()
			.filter(s -> s.getParentIds().contains(scopeModel.getId()))
			.map(ScopeModel::getId)
			.toList();

		dto.root = scopeModel.isRoot();
		dto.leaf = scopeModel.isLeaf();
		dto.virtual = scopeModel.isVirtual();

		dto.eventGroups = scopeModel.getEventGroups().stream()
			.map(EventGroupDTO::new)
			.toList();
		dto.eventModels = scopeModel.getEventModels().stream()
			.filter(e -> acl.hasRight(e, Rights.READ))
			.map(EventModelDTO::new)
			.toList();
		dto.datasetModelIds = scopeModel.getDatasetModelIds();
		dto.formModelIds = scopeModel.getFormModelIds();
		dto.workflowIds = scopeModel.getWorkflowIds();

		dto.defaultProfileId = scopeModel.getDefaultProfileId();

		return dto;
	}

	@Override
	public DatasetModelDTO createDatasetModelDTO(final DatasetModel datasetModel, final ACL acl) {
		final var languages = actorService.getLanguages(acl.actor());
		final var dto = new DatasetModelDTO();

		dto.id = datasetModel.getId();
		dto.shortname = datasetModel.getShortname();
		dto.multiple = datasetModel.isMultiple();
		dto.exportable = datasetModel.isExportable();
		dto.scopeDocumentation = datasetModel.isScopeDocumentation();
		dto.expandedLabelPattern = datasetModel.getExpandedLabelPattern();
		dto.collapsedLabelPattern = datasetModel.getCollapsedLabelPattern();
		dto.fieldModels = datasetModel.getFieldModels().stream()
			.sorted()
			.map(f -> new FieldModelDTO(f, languages))
			.toList();
		dto.canWrite = acl.hasRight(datasetModel, Rights.WRITE);

		return dto;
	}

	@Override
	public LayoutDTO createLayoutDTO(
		final Scope scope,
		final Optional<Event> event,
		final Form form,
		final Layout layout,
		final ACL acl,
		final String[] languages
	) {
		final var dto = new LayoutDTO();

		dto.id = layout.getId();
		dto.formModelId = layout.getFormModel().getId();
		dto.description = layout.getDescription();

		dto.type = layout.getType();
		if(StringUtils.isNotBlank(layout.getDatasetModelId())) {
			dto.datasetModel = createDatasetModelDTO(layout.getDatasetModel(), acl);
			dto.defaultSortFieldModelId = layout.getDefaultSortFieldModelId();
		}

		dto.contribution = layout.isContribution();
		dto.columns = layout.getColumns().stream().map(c -> new ColumnHeaderDTO(c.getCssCode())).toList();
		dto.textBefore = layout.getTextBefore();
		dto.textAfter = layout.getTextAfter();
		dto.cssCode = layout.getCssCode();
		dto.scopePk = scope.getPk();
		dto.eventPk = form.getEventFk();
		dto.lines = layout.getLines().stream().map(l -> createLineDTO(scope, event, form, l, languages)).toList();
		return dto;
	}

	@Override
	public List<LayoutDTO> createLayoutDTOs(
		final Scope scope,
		final Optional<Event> event,
		final Form form,
		final ACL acl,
		final String[] languages
	) {
		return form.getFormModel().getLayouts().stream()
			.filter(l -> isLayoutValid(l, form, scope, event))
			.map(l -> createLayoutDTO(scope, event, form, l, acl, languages))
			.toList();
	}

	private LayoutLineDTO createLineDTO(final Scope scope, final Optional<Event> event, final Form form, final Line line, final String[] languages) {
		final var dto = new LayoutLineDTO();
		dto.cells = line.getCells().stream()
			.filter(c -> isCellValid(c, form, scope, event))
			.map(c -> createCellDTO(c, languages))
			.toList();
		return dto;
	}

	private CellDTO createCellDTO(final Cell cell, final String[] languages) {
		final var dto = new CellDTO();
		dto.id = cell.getId();
		dto.datasetModelId = cell.getDatasetModelId();
		dto.fieldModelId = cell.getFieldModelId();

		dto.textBefore = cell.getTextBefore();
		dto.textAfter = cell.getTextAfter();

		dto.visibilityCriteria = cell.getVisibilityCriteria().stream()
			.map(VisibilityCriteriaDTO::new)
			.toList();

		dto.displayLabel = cell.getDisplayLabel();
		dto.displayPossibleValueLabels = cell.getDisplayPossibleValueLabels();

		dto.possibleValuesColumnNumber = cell.getPossibleValuesColumnNumber();
		dto.possibleValuesColumnWidth = cell.getPossibleValuesColumnWidth();

		dto.colspan = cell.getColspan();

		if(cell.hasFieldModel()) {
			final var cellFieldModel = cell.getFieldModel();
			dto.fieldModel = new FieldModelDTO(cellFieldModel, languages);
		}

		dto.cssCodeForInput = StringUtils.defaultIfBlank(cell.getCssCodeForInput(), null);
		dto.cssCodeForLabel = StringUtils.defaultIfBlank(cell.getCssCodeForLabel(), null);

		return dto;
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
}
