package ch.rodano.core.model.scope;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.core.model.common.PaginatedSearch;

@Schema(description = "PaginatedSearch for scope search")
@SuppressWarnings("hiding")
public class ScopeSearch extends PaginatedSearch<ScopeSearch> {
	public static final ScopeSortBy DEFAULT_SORT_BY = ScopeSortBy.code;
	public static final boolean DEFAULT_SORT_ASCENDING = true;

	@Schema(description = "Text search on code, shortname and longname")
	public Optional<String> fullText = Optional.empty();

	@Schema(description = "Scope code")
	public Optional<String> code = Optional.empty();

	@Schema(description = "Allowed scope ids")
	public Optional<List<String>> ids = Optional.empty();

	@Schema(description = "Allowed scope pks")
	public Optional<List<Long>> pks = Optional.empty();

	@Schema(description = "Allowed scope model id")
	public Optional<String> scopeModelId = Optional.empty();

	@Schema(description = "Allowed parent pks")
	public Optional<List<Long>> parentPks = Optional.empty();

	@Schema(description = "Allowed ancestor pks")
	public Optional<List<Long>> ancestorPks = Optional.empty();

	@Schema(description = "Allowed ancestor pks by scope models ids")
	public Optional<Map<String, List<Long>>> scopeModelAncestorPks = Optional.empty();

	@Schema(description = "A list of field model criteria for filtering")
	public Optional<List<FieldModelCriterion>> fieldModelCriteria = Optional.empty();

	@Schema(description = "Allowed workflow states map")
	public Optional<Map<String, List<String>>> workflowStates = Optional.empty();

	@Schema(description = "Include the leaf scope")
	public Optional<Boolean> leaf = Optional.empty();

	@Schema(description = "Include the removed scopes?")
	public boolean includeDeleted;

	@Schema(description = "Sort by")
	public ScopeSortBy sortBy = DEFAULT_SORT_BY;

	public ScopeSearch() {
		super();
		sortAscending = DEFAULT_SORT_ASCENDING;
	}

	public Optional<String> getFullText() {
		return fullText;
	}

	public ScopeSearch setFullText(final Optional<String> fullText) {
		this.fullText = fullText;
		return this;
	}

	public Optional<String> getCode() {
		return code;
	}

	public ScopeSearch setCode(final Optional<String> code) {
		this.code = code;
		return this;
	}

	public Optional<List<String>> getIds() {
		return ids;
	}

	public ScopeSearch setIds(final Optional<List<String>> ids) {
		this.ids = ids;
		return this;
	}

	public Optional<List<Long>> getPks() {
		return pks;
	}

	public ScopeSearch setPks(final Optional<List<Long>> pks) {
		this.pks = pks;
		return this;
	}

	public Optional<String> getScopeModelId() {
		return scopeModelId;
	}

	public ScopeSearch setScopeModelId(final Optional<String> scopeModelId) {
		this.scopeModelId = scopeModelId;
		return this;
	}

	public ScopeSearch enforceScopeModelId(final String scopeModelId) {
		this.scopeModelId = Optional.of(scopeModelId);
		return this;
	}

	public Optional<List<Long>> getParentPks() {
		return parentPks;
	}

	public ScopeSearch setParentPks(final Optional<List<Long>> parentPks) {
		this.parentPks = parentPks;
		return this;
	}

	public Optional<List<Long>> getAncestorPks() {
		return ancestorPks;
	}

	public ScopeSearch setAncestorPks(final Optional<List<Long>> ancestorPks) {
		this.ancestorPks = ancestorPks;
		return this;
	}

	public ScopeSearch enforceAncestorPks(final List<Long> ancestorPks) {
		this.ancestorPks = Optional.of(ancestorPks);
		return this;
	}

	public Optional<Map<String, List<Long>>> getScopeModelAncestorPks() {
		return scopeModelAncestorPks;
	}

	public ScopeSearch setScopeModelAncestorPks(final Optional<Map<String, List<Long>>> scopeModelAncestorPks) {
		this.scopeModelAncestorPks = scopeModelAncestorPks;
		return this;
	}

	public Optional<List<FieldModelCriterion>> getFieldModelCriteria() {
		return fieldModelCriteria;
	}

	public ScopeSearch setFieldModelCriteria(final Optional<List<FieldModelCriterion>> fieldModelCriteria) {
		this.fieldModelCriteria = fieldModelCriteria;
		return this;
	}

	public ScopeSearch enforceFieldModelCriteria(final List<FieldModelCriterion> fieldModelCriteria) {
		this.fieldModelCriteria = Optional.of(fieldModelCriteria);
		return this;
	}

	public Optional<Map<String, List<String>>> getWorkflowStates() {
		return workflowStates;
	}

	public ScopeSearch setWorkflowStates(final Optional<Map<String, List<String>>> workflowStates) {
		this.workflowStates = workflowStates;
		return this;
	}

	public Optional<Boolean> getLeaf() {
		return leaf;
	}

	public ScopeSearch setLeaf(final Optional<Boolean> leaf) {
		this.leaf = leaf;
		return this;
	}

	public boolean getIncludeDeleted() {
		return includeDeleted;
	}

	public ScopeSearch setIncludeDeleted(final boolean includeDeleted) {
		this.includeDeleted = includeDeleted;
		return this;
	}

	public ScopeSortBy getSortBy() {
		return sortBy;
	}

	public ScopeSearch setSortBy(final ScopeSortBy sortBy) {
		this.sortBy = sortBy;
		return this;
	}

}
