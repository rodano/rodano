package ch.rodano.core.model.resource;

public class ResourceRecord {

	protected boolean removed;

	protected Long userFk;
	protected Long scopeFk;
	protected String uuid;
	protected String title;
	protected String description;

	protected String categoryId;

	protected Boolean publicResource;

	protected String filename;

	protected ResourceRecord() {
		setRemoved(false);
	}

	public boolean isRemoved() {
		return removed;
	}

	public void setRemoved(final boolean removed) {
		this.removed = removed;
	}

	public Long getUserFk() {
		return userFk;
	}

	public void setUserFk(final Long userFk) {
		this.userFk = userFk;
	}

	public Long getScopeFk() {
		return scopeFk;
	}

	public void setScopeFk(final Long scopeFk) {
		this.scopeFk = scopeFk;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

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

	public String getFilename() {
		return filename;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(final String categoryId) {
		this.categoryId = categoryId;
	}

	public Boolean getPublicResource() {
		return publicResource;
	}

	public void setPublicResource(final Boolean publicResource) {
		this.publicResource = publicResource;
	}
}
