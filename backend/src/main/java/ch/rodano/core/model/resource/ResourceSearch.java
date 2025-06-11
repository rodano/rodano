package ch.rodano.core.model.resource;

import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.core.model.common.PaginatedSearch;

public class ResourceSearch extends PaginatedSearch<ResourceSearch> {
	public static final ResourceSortBy DEFAULT_SORT_BY = ResourceSortBy.title;
	public static final boolean DEFAULT_SORT_ASCENDING = true;

	@Schema(description = "Allow only public")
	private Optional<Boolean> onlyPublic = Optional.empty();

	@Schema(description = "Reference scope pks")
	private Optional<List<Long>> referenceScopePks = Optional.empty();

	@Schema(description = "Category id")
	private Optional<String> categoryId = Optional.empty();

	@Schema(description = "Text search on title and description")
	private Optional<String> fullText = Optional.empty();

	@Schema(description = "Include the deleted resources?")
	private boolean includeDeleted;

	@Schema(description = "Sort by")
	private ResourceSortBy sortBy = DEFAULT_SORT_BY;

	public ResourceSearch() {
		super();
		sortAscending = DEFAULT_SORT_ASCENDING;
	}

	public Optional<Boolean> getOnlyPublic() {
		return onlyPublic;
	}

	public ResourceSearch setOnlyPublic(final Optional<Boolean> onlyPublic) {
		this.onlyPublic = onlyPublic;
		return this;
	}

	public Optional<List<Long>> getReferenceScopePks() {
		return referenceScopePks;
	}

	public ResourceSearch setReferenceScopePks(final Optional<List<Long>> referenceScopePks) {
		this.referenceScopePks = referenceScopePks;
		return this;
	}

	public Optional<String> getCategoryId() {
		return categoryId;
	}

	public ResourceSearch setCategoryId(final Optional<String> categoryId) {
		this.categoryId = categoryId;
		return this;
	}

	public Optional<String> getFullText() {
		return fullText;
	}

	public ResourceSearch setFullText(final Optional<String> fullText) {
		this.fullText = fullText;
		return this;
	}

	public boolean getIncludeDeleted() {
		return includeDeleted;
	}

	public ResourceSearch setIncludeDeleted(final boolean includeDeleted) {
		this.includeDeleted = includeDeleted;
		return this;
	}

	public ResourceSortBy getSortBy() {
		return sortBy;
	}

	public ResourceSearch setSortBy(final ResourceSortBy sortBy) {
		this.sortBy = sortBy;
		return this;
	}

}
