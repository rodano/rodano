package ch.rodano.api.resource;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class ResourceSubmissionDTO {
	@NotNull
	protected String title;
	protected String description;
	@NotNull
	protected UUID categoryId;

	@NotNull
	protected Long scopePk;
	@NotNull
	protected Boolean publicResource;

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public UUID getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(final UUID categoryId) {
		this.categoryId = categoryId;
	}

	public Long getScopePk() {
		return scopePk;
	}

	public void setScopePk(final Long scopePk) {
		this.scopePk = scopePk;
	}

	public Boolean isPublicResource() {
		return publicResource;
	}

	public void setPublicResource(final Boolean publicResource) {
		this.publicResource = publicResource;
	}
}
