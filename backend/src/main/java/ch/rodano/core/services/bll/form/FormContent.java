package ch.rodano.core.services.bll.form;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.form.Form;

public record FormContent(
	Form form,
	List<LayoutContent> singleLayouts,
	List<LayoutContent> multipleLayouts
) {

	public Set<Dataset> getDatasets() {
		return Stream.concat(singleLayouts.stream(), multipleLayouts.stream())
			.flatMap(l -> l.getDatasets().stream())
			.collect(Collectors.toSet());
	}

	public Set<Dataset> getMultipleDatasets() {
		return multipleLayouts.stream()
			.flatMap(l -> l.getDatasets().stream())
			.collect(Collectors.toSet());
	}

	public Map<Dataset, Set<Field>> getFieldsNotInMultiple() {
		return singleLayouts.stream()
			.flatMap(l -> l.fieldsByDataset().entrySet().stream())
			.collect(
				Collectors.groupingBy(
					Map.Entry::getKey,
					Collectors.flatMapping(e -> e.getValue().stream(), Collectors.toSet())
				)
			);
	}

	public Set<Field> getFields() {
		return Stream.concat(singleLayouts.stream(), multipleLayouts.stream())
			.flatMap(l -> l.getFields().stream())
			.collect(Collectors.toSet());
	}

	public Set<Field> getNonDeletedFields() {
		return Stream.concat(singleLayouts.stream(), multipleLayouts.stream())
			.flatMap(l -> l.getNonDeletedFields().stream())
			.collect(Collectors.toSet());
	}

	public Set<Field> getFilledInFields() {
		return getNonDeletedFields().stream().filter(Field::isNotNull).collect(Collectors.toSet());
	}
}
