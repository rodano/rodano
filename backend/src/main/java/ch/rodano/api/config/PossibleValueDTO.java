package ch.rodano.api.config;

import java.util.Map;
import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.configuration.model.field.PossibleValue;

public class PossibleValueDTO {
	@NotNull
	private UUID possibleValueId;
	@NotBlank
	private String id;
	@NotNull
	private Map<String, String> shortname;
	@NotNull
	private boolean specify;

	private UUID fieldModelId;

	private String exportLabel;
	private Integer sortOrder;

	/**
	 * Default constructor, needed by some serializer
	 */
	public PossibleValueDTO() {

	}

	public PossibleValueDTO(final PossibleValue possibleValue) {
		this.possibleValueId = possibleValue.getPossibleValueId();
		this.id = possibleValue.getId();
		this.shortname = possibleValue.getShortname();
		this.specify = possibleValue.isSpecify();
	}

	public UUID getPossibleValueId() {
		return possibleValueId;
	}

	public void setPossibleValueId(final UUID possibleValueId) {
		this.possibleValueId = possibleValueId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public UUID getFieldModelId() {
		return fieldModelId;
	}

	public void setFieldModelId(final UUID fieldModelId) {
		this.fieldModelId = fieldModelId;
	}

	public Map<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	public boolean isSpecify() {
		return specify;
	}

	public void setSpecify(final boolean specify) {
		this.specify = specify;
	}

	public String getExportLabel() {
		return exportLabel;
	}

	public void setExportLabel(final String exportLabel) {
		this.exportLabel = exportLabel;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(final Integer sortOrder) {
		this.sortOrder = sortOrder;
	}
}
