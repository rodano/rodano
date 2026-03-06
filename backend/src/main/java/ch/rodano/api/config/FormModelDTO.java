package ch.rodano.api.config;

import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.configuration.model.form.FormModel;

public class FormModelDTO {
	@NotNull
	private UUID formModelId;
	@NotBlank
	private String id;
	@NotNull
	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;
	private SortedMap<String, String> printButtonLabel;

	private boolean optional;

	private List<UUID> workflowIds;

	/**
	 * Default constructor, needed by some serializer
	 */
	public FormModelDTO() {

	}

	public FormModelDTO(final FormModel formModel) {
		formModelId = formModel.getFormModelId();
		this.id = formModel.getId();
		this.shortname = formModel.getShortname();
	}

	public UUID getFormModelId() {
		return formModelId;
	}

	public void setFormModelId(final UUID formModelId) {
		this.formModelId = formModelId;
	}

	public SortedMap<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	public SortedMap<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	public SortedMap<String, String> getDescription() {
		return description;
	}

	public void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public SortedMap<String, String> getPrintButtonLabel() {
		return printButtonLabel;
	}

	public void setPrintButtonLabel(final SortedMap<String, String> printButtonLabel) {
		this.printButtonLabel = printButtonLabel;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(final boolean optional) {
		this.optional = optional;
	}

	public List<UUID> getWorkflowIds() {
		return workflowIds;
	}

	public void setWorkflowIds(final List<UUID> workflowIds) {
		this.workflowIds = workflowIds;
	}
}
