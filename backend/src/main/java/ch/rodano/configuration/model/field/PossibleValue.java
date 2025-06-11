package ch.rodano.configuration.model.field;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.utils.DisplayableUtils;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class PossibleValue implements Node {
	private static final long serialVersionUID = -10498351329495800L;

	private String id;
	private FieldModel fieldModel;

	private Map<String, String> shortname;

	private String exportLabel;

	private boolean specify;

	public PossibleValue() {
		shortname = new TreeMap<>();
	}

	public PossibleValue(final String id, final Map<String, String> shortname) {
		this.id = id;
		this.shortname = shortname;
	}

	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@JsonBackReference
	public final FieldModel getFieldModel() {
		return fieldModel;
	}

	@JsonBackReference
	public final void setFieldModel(final FieldModel fieldModel) {
		this.fieldModel = fieldModel;
	}

	public final String getExportLabel() {
		return exportLabel;
	}

	public final void setExportLabel(final String exportLabel) {
		this.exportLabel = exportLabel;
	}

	public final Map<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final Map<String, String> shortname) {
		this.shortname = shortname;
	}

	public final boolean isSpecify() {
		return specify;
	}

	public final void setSpecify(final boolean specify) {
		this.specify = specify;
	}

	@JsonIgnore
	public String getLocalizedShortname(final String... languages) {
		return DisplayableUtils.getLocalizedMapWithDefault(shortname, "", languages);
	}

	@JsonIgnore
	public final String getDefaultLocalizedShortname() {
		return getLocalizedShortname(getFieldModel().getDatasetModel().getStudy().getDefaultLanguageId());
	}

	@JsonIgnore
	public final String getExportColumnLabel() {
		return StringUtils.defaultIfBlank(exportLabel, id);
	}

	@Override
	public final Entity getEntity() {
		return Entity.POSSIBLE_VALUE;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
