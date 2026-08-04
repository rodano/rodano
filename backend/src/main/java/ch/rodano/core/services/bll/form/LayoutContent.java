package ch.rodano.core.services.bll.form;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ch.rodano.configuration.model.layout.Layout;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.field.Field;

public record LayoutContent(
	Layout layout,
	Map<Dataset, Set<Field>> fieldsByDataset
) {

	public Set<Dataset> getDatasets() {
		return fieldsByDataset.keySet();
	}

	public Set<Field> getFields() {
		return fieldsByDataset.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
	}

	public Set<Field> getNonDeletedFields() {
		return fieldsByDataset.entrySet().stream()
			.filter(e -> !e.getKey().isRemoved())
			.flatMap(e -> e.getValue().stream())
			.collect(Collectors.toSet());
	}

}
