package ch.rodano.configuration.model.dataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.exceptions.NoNodeException;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.common.SuperDisplayable;
import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.layout.Cell;
import ch.rodano.configuration.model.layout.ColumnHeader;
import ch.rodano.configuration.model.layout.Layout;
import ch.rodano.configuration.model.layout.Line;
import ch.rodano.configuration.model.rights.RightAssignable;
import ch.rodano.configuration.model.rules.Rule;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class DatasetModel implements Serializable, SuperDisplayable, Node, RightAssignable<DatasetModel> {
	private static final long serialVersionUID = 3650259090775513476L;

	private static Comparator<DatasetModel> DEFAULT_COMPARATOR = Comparator
		.comparing(DatasetModel::getExportOrder)
		.thenComparing(DatasetModel::getId);

	public static final String EXPORT_TABLE_PREFIX = "export_";

	private String id;
	private Study study;

	private boolean multiple;
	private List<FieldModel> fieldModels;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private String collapsedLabelPattern;
	private String expandedLabelPattern;

	private String family;
	private boolean master;

	private boolean contribution;

	private boolean exportable;
	private int exportOrder;

	private List<Rule> deleteRules;
	private List<Rule> restoreRules;

	public DatasetModel() {
		multiple = false;
		fieldModels = new ArrayList<>();
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		contribution = false;
	}

	public DatasetModel(final DatasetModel datasetModel) {
		id = datasetModel.getId();
		study = datasetModel.getStudy();
		multiple = datasetModel.isMultiple();
		fieldModels = datasetModel.getFieldModels();
		shortname = datasetModel.getShortname();
		longname = datasetModel.getLongname();
		description = datasetModel.getDescription();
		collapsedLabelPattern = datasetModel.getCollapsedLabelPattern();
		expandedLabelPattern = datasetModel.getExpandedLabelPattern();
		family = datasetModel.getFamily();
		master = datasetModel.getMaster();
		contribution = datasetModel.isContribution();
		exportable = datasetModel.isExportable();
		exportOrder = datasetModel.getExportOrder();
		deleteRules = datasetModel.getDeleteRules();
		restoreRules = datasetModel.getRestoreRules();
	}

	@JsonBackReference
	public final void setStudy(final Study study) {
		this.study = study;
	}

	@JsonBackReference
	public final Study getStudy() {
		return study;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@Override
	public final String getId() {
		return id;
	}

	public final boolean isMultiple() {
		return multiple;
	}

	public final void setMultiple(final boolean multiple) {
		this.multiple = multiple;
	}

	@JsonManagedReference
	public final List<FieldModel> getFieldModels() {
		return fieldModels;
	}

	@JsonManagedReference
	public final void setFieldModels(final List<FieldModel> fieldModels) {
		this.fieldModels = fieldModels;
	}

	public final String getFamily() {
		return family;
	}

	public final void setFamily(final String family) {
		this.family = family;
	}

	public final boolean getMaster() {
		return master;
	}

	public final void setMaster(final boolean master) {
		this.master = master;
	}

	public final boolean isExportable() {
		return exportable;
	}

	public final void setExportable(final boolean exportable) {
		this.exportable = exportable;
	}

	@JsonIgnore
	public final boolean isContribution() {
		return contribution;
	}

	@JsonIgnore
	public final void setContribution(final boolean contribution) {
		this.contribution = contribution;
	}

	public final int getExportOrder() {
		return exportOrder;
	}

	public final void setExportOrder(final int exportOrder) {
		this.exportOrder = exportOrder;
	}

	@Override
	public final SortedMap<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	@Override
	public final SortedMap<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	@Override
	public final SortedMap<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	public String getCollapsedLabelPattern() {
		return collapsedLabelPattern;
	}

	public void setCollapsedLabelPattern(final String collapsedLabelPattern) {
		this.collapsedLabelPattern = collapsedLabelPattern;
	}

	public String getExpandedLabelPattern() {
		return expandedLabelPattern;
	}

	public void setExpandedLabelPattern(final String expandedLabelPattern) {
		this.expandedLabelPattern = expandedLabelPattern;
	}

	public final List<Rule> getDeleteRules() {
		return deleteRules;
	}

	public final void setDeleteRules(final List<Rule> deleteRules) {
		this.deleteRules = deleteRules;
	}

	public final List<Rule> getRestoreRules() {
		return restoreRules;
	}

	public final void setRestoreRules(final List<Rule> restoreRules) {
		this.restoreRules = restoreRules;
	}

	@JsonIgnore
	public final FieldModel getFieldModel(final String fieldModelId) {
		return fieldModels.stream()
			.filter(f -> f.getId().equalsIgnoreCase(fieldModelId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.FIELD_MODEL, fieldModelId));
	}

	@JsonIgnore
	public final boolean hasFieldModel(final String fieldModelId) {
		return fieldModels.stream().anyMatch(a -> a.getId().equalsIgnoreCase(fieldModelId));
	}

	@JsonIgnore
	public String getDefaultLocalizedShortname() {
		return getLocalizedShortname(getStudy().getDefaultLanguage().getId());
	}

	@Override
	@JsonIgnore
	public final String getAssignableDescription() {
		return getDefaultLocalizedShortname();
	}

	@Override
	public final Entity getEntity() {
		return Entity.DATASET_MODEL;
	}

	@JsonIgnore
	public final String getExportTableName() {
		return EXPORT_TABLE_PREFIX + getId().toLowerCase();
	}

	@JsonIgnore
	public final List<FieldModel> getFieldModelsExportables() {
		return fieldModels.stream().filter(FieldModel::isExportable).sorted().toList();
	}

	@JsonIgnore
	public boolean isInFamily() {
		return StringUtils.isNotBlank(family);
	}

	@JsonIgnore
	public boolean isInSameFamily(final DatasetModel datasetModel) {
		return isInFamily() && datasetModel.isInFamily() && getFamily().equals(datasetModel.getFamily());
	}

	@JsonIgnore
	public DatasetModel getMasterDatasetModel() {
		if(isInFamily() && !getMaster()) {
			return study.getDatasetModels()
				.stream()
				.filter(d -> isInSameFamily(d) && d.getMaster())
				.findFirst()
				.orElseThrow();
		}
		return this;
	}

	@JsonIgnore
	public List<DatasetModel> getSlaveDatasetModels() {
		if(!isInFamily() || !getMaster()) {
			return Collections.emptyList();
		}
		return study.getDatasetModels().stream().filter(d -> isInSameFamily(d) && !d.getMaster()).toList();
	}

	@Override
	@JsonIgnore
	public int compareTo(final DatasetModel otherDatasetModel) {
		return DEFAULT_COMPARATOR.compare(this, otherDatasetModel);
	}

	@JsonIgnore
	public boolean isRepeatedInEventModels() {
		return study.getEventModels().stream()
			.flatMap(e -> e.getDatasetModelIds().stream())
			.anyMatch(i -> id.equals(i));
	}

	@JsonIgnore
	public List<ScopeModel> getScopeModels() {
		return study.getScopeModels().stream().filter(s -> s.getDatasetModelIds().contains(id)).toList();
	}

	@JsonIgnore
	public List<EventModel> getEventModels() {
		return study.getEventModels().stream().filter(e -> e.getDatasetModelIds().contains(id)).toList();
	}

	@JsonIgnore
	public boolean isScopeDocumentation() {
		return !getScopeModels().isEmpty();
	}

	@JsonIgnore
	public String generateFieldModelId() {
		var fieldModelCount = getFieldModels().size();
		while(true) {
			final var fieldModelId = String.format("%s_%s", id, fieldModelCount);
			var isValid = true;
			for(final var fieldModel : getFieldModels()) {
				if(fieldModel.getId().equals(fieldModelId)) {
					fieldModelCount++;
					isValid = false;
					break;
				}
			}
			if(isValid) {
				return fieldModelId;
			}
		}
	}

	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		switch(entity) {
			case FIELD_MODEL:
				return Collections.unmodifiableList(fieldModels);
			default:
				return Collections.emptyList();
		}
	}

	@JsonIgnore
	public final Layout generateLayout() {
		final var layout = new Layout();
		layout.setId("DEFAULT_LAYOUT");
		layout.getDescription().put("en", "Default Layout");

		final var header = new ColumnHeader();
		layout.setColumns(Collections.singletonList(header));

		for(final var fieldModel : getFieldModels()) {
			//cell creation
			final var cell = new Cell();
			cell.setDatasetModelId(getId());
			cell.setFieldModelId(fieldModel.getId());
			cell.setId(fieldModel.getId());
			cell.setDisplayLabel(true);

			//line creation
			final var line = new Line();

			cell.setLine(line);
			line.setCells(Collections.singletonList(cell));

			line.setLayout(layout);
			layout.getLines().add(line);
		}
		return layout;
	}
}
