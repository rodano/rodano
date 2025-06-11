package ch.rodano.core.model.robot;

import java.util.Optional;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.core.model.common.PaginatedSearch;

public class RobotSearch extends PaginatedSearch<RobotSearch> {
	public static final RobotSortBy DEFAULT_SORT_BY = RobotSortBy.name;
	public static final boolean DEFAULT_SORT_ASCENDING = true;

	@Schema(description = "Text search on name")
	private Optional<String> name = Optional.empty();

	@Schema(description = "Profile id")
	private Optional<String> profileId = Optional.empty();

	@Schema(description = "Include the deleted resources?")
	private boolean includeDeleted;

	@Schema(description = "Sort by")
	private RobotSortBy sortBy = DEFAULT_SORT_BY;

	public RobotSearch() {
		super();
		sortAscending = DEFAULT_SORT_ASCENDING;
	}

	public Optional<String> getName() {
		return name;
	}

	public RobotSearch setName(final Optional<String> name) {
		this.name = name;
		return this;
	}

	public Optional<String> getProfileId() {
		return profileId;
	}

	public RobotSearch setProfileId(final Optional<String> profileId) {
		this.profileId = profileId;
		return this;
	}

	public boolean getIncludeDeleted() {
		return includeDeleted;
	}

	public RobotSearch setIncludeDeleted(final boolean includeDeleted) {
		this.includeDeleted = includeDeleted;
		return this;
	}

	public RobotSortBy getSortBy() {
		return sortBy;
	}

	public RobotSearch setSortBy(final RobotSortBy sortBy) {
		this.sortBy = sortBy;
		return this;
	}

}
