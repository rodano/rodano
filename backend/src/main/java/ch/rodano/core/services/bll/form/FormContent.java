package ch.rodano.core.services.bll.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import ch.rodano.configuration.model.layout.Cell;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.field.Field;

public class FormContent {
	private final List<LayoutContent> singleLayouts;
	private final List<LayoutContent> multipleLayouts;

	public FormContent(
		final List<LayoutContent> singleLayouts,
		final List<LayoutContent> multipleLayouts
	) {
		this.multipleLayouts = multipleLayouts;
		this.singleLayouts = singleLayouts;
	}

	public List<Dataset> getMultipleDatasets() {
		return multipleLayouts.stream()
			.flatMap(l -> l.getDatasets().stream())
			.toList();
	}

	public Map<Dataset, List<Field>> getFieldsNotInMultiple() {
		return singleLayouts.stream()
			.flatMap(l -> l.datasetsFields().stream())
			.collect(Collectors.groupingBy(Pair::getLeft, Collectors.mapping(Pair::getRight, Collectors.toList())));
	}

	public List<Field> getAllFields() {
		final List<Field> fields = new ArrayList<>();
		multipleLayouts.stream().flatMap(l -> l.getFields().stream()).forEach(fields::add);
		singleLayouts.stream().flatMap(l -> l.getFields().stream()).forEach(fields::add);
		return fields;
	}

	public List<Field> getAllNonDeletedFields() {
		final List<Field> fields = new ArrayList<>();
		multipleLayouts.stream().flatMap(l -> l.getNonDeletedFields().stream()).forEach(fields::add);
		singleLayouts.stream().flatMap(l -> l.getNonDeletedFields().stream()).forEach(fields::add);
		return fields;
	}

	public List<Field> getFieldsForCell(final Cell cell) {
		if(!cell.hasFieldModel()) {
			return Collections.emptyList();
		}
		return getAllFields().stream()
			.filter(f -> f.getDatasetModelId().equals(cell.getDatasetModelId()) && f.getFieldModelId().equals(cell.getFieldModelId()))
			.toList();
	}
}
