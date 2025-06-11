package ch.rodano.core.model.user;

import java.util.Optional;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.core.model.common.PaginatedSearch;
import ch.rodano.core.model.role.RoleStatus;
import ch.rodano.core.model.scope.ScopeExtension;

@Schema
@SuppressWarnings("hiding")
public class UserSearch extends PaginatedSearch<UserSearch> {
	public static final UserSortBy DEFAULT_SORT_BY = UserSortBy.name;
	public static final boolean DEFAULT_SORT_ASCENDING = true;

	@Schema(description = "Scope pks")
	private Optional<Set<Long>> scopePks = Optional.empty();

	@Schema(description = "Scope extension")
	private Optional<ScopeExtension> extension = Optional.empty();

	@Schema(description = "Profile ids")
	private Optional<Set<String>> profileIds = Optional.empty();

	@Schema(description = "Feature id")
	private Optional<String> featureId = Optional.empty();

	@Schema(description = "Allowed role states")
	private Optional<Set<RoleStatus>> states = Optional.empty();

	@Schema(description = "Exact match of e-mail")
	private Optional<String> email = Optional.empty();

	@Schema(description = "Text search on name and e-mail")
	private Optional<String> fullText = Optional.empty();

	@Schema(description = "Is the user enabled?")
	private Optional<Boolean> enabled = Optional.empty();

	@Schema(description = "Is the user externally managed?")
	private Optional<Boolean> externallyManaged = Optional.empty();

	@Schema(description = "Include the deleted users?")
	private boolean includeDeleted;

	@Schema(description = "Sort by")
	private UserSortBy sortBy = DEFAULT_SORT_BY;

	public UserSearch() {
		super();
		sortAscending = DEFAULT_SORT_ASCENDING;
	}

	public Optional<Set<Long>> getScopePks() {
		return scopePks;
	}

	public UserSearch setScopePks(final Optional<Set<Long>> scopePks) {
		this.scopePks = scopePks;
		return this;
	}

	public UserSearch enforceScopePks(final Set<Long> scopePks) {
		this.scopePks = Optional.of(scopePks);
		return this;
	}

	public Optional<ScopeExtension> getExtension() {
		return extension;
	}

	public UserSearch setExtension(final Optional<ScopeExtension> extension) {
		this.extension = extension;
		return this;
	}

	public UserSearch enforceExtension(final ScopeExtension extension) {
		this.extension = Optional.of(extension);
		return this;
	}

	public Optional<Set<String>> getProfileIds() {
		return profileIds;
	}

	public UserSearch setProfileIds(final Optional<Set<String>> profileIds) {
		this.profileIds = profileIds;
		return this;
	}

	public UserSearch enforceProfileIds(final Set<String> profileIds) {
		this.profileIds = Optional.of(profileIds);
		return this;
	}

	public Optional<String> getFeatureId() {
		return featureId;
	}

	public UserSearch setFeatureId(final Optional<String> featureId) {
		this.featureId = featureId;
		return this;
	}

	public UserSearch enforceFeatureId(final String featureId) {
		this.featureId = Optional.of(featureId);
		return this;
	}

	public Optional<Set<RoleStatus>> getStates() {
		return states;
	}

	public UserSearch setStates(final Optional<Set<RoleStatus>> states) {
		this.states = states;
		return this;
	}

	public Optional<String> getEmail() {
		return email;
	}

	public UserSearch setEmail(final Optional<String> email) {
		this.email = email;
		return this;
	}

	public Optional<String> getFullText() {
		return fullText;
	}

	public UserSearch setFullText(final Optional<String> fullText) {
		this.fullText = fullText;
		return this;
	}

	public Optional<Boolean> getEnabled() {
		return enabled;
	}

	public UserSearch setEnabled(final Optional<Boolean> enabled) {
		this.enabled = enabled;
		return this;
	}

	public UserSearch enforceEnabled(final Boolean enabled) {
		this.enabled = Optional.of(enabled);
		return this;
	}

	public Optional<Boolean> getExternallyManaged() {
		return externallyManaged;
	}

	public UserSearch setExternallyManaged(final Optional<Boolean> externallyManaged) {
		this.externallyManaged = externallyManaged;
		return this;
	}

	public boolean getIncludeDeleted() {
		return includeDeleted;
	}

	public UserSearch setIncludeDeleted(final boolean includeDeleted) {
		this.includeDeleted = includeDeleted;
		return this;
	}

	public UserSortBy getSortBy() {
		return sortBy;
	}

	public UserSearch setSortBy(final UserSortBy sortBy) {
		this.sortBy = sortBy;
		return this;
	}

}
