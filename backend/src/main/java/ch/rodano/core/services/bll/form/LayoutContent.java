package ch.rodano.core.services.bll.form;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import ch.rodano.configuration.model.layout.Layout;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.field.Field;

public record LayoutContent(
	Layout layout,
	List<Pair<Dataset, Field>> datasetsFields
) {

	public List<Dataset> getDatasets() {
		return datasetsFields.stream().map(Pair::getLeft).toList();
	}

	public List<Field> getFields() {
		return datasetsFields.stream().map(Pair::getRight).toList();
	}

	public List<Field> getNonDeletedFields() {
		return datasetsFields.stream().filter(p -> !p.getLeft().getDeleted()).map(Pair::getRight).toList();
	}

}
