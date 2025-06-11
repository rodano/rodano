package ch.rodano.core.model.common;

import java.util.Optional;

import org.jooq.Field;
import org.jooq.SortOrder;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import io.swagger.v3.oas.annotations.media.Schema;

@SuppressWarnings("unchecked")
public abstract class PaginatedSearch<T extends PaginatedSearch<T>> {

	@Schema(description = "Page size")
	protected Optional<Integer> pageSize = Optional.empty();

	@Schema(description = "Page index")
	protected Optional<Integer> pageIndex = Optional.empty();

	@Schema(description = "Sort in ascending order")
	protected boolean sortAscending = true;

	public boolean isSortAscending() {
		return sortAscending;
	}

	public T setSortAscending(final boolean orderAscending) {
		this.sortAscending = orderAscending;
		return (T) this;
	}

	public Optional<Integer> getPageSize() {
		return pageSize;
	}

	public T setPageSize(final Optional<Integer> pageSize) {
		this.pageSize = pageSize;
		return (T) this;
	}

	public Optional<Integer> getPageIndex() {
		return pageIndex;
	}

	public T setPageIndex(final Optional<Integer> pageIndex) {
		this.pageIndex = pageIndex;
		return (T) this;
	}

	public SortOrder getOrder() {
		return isSortAscending() ? SortOrder.ASC : SortOrder.DESC;
	}

	public boolean isPagingPresent() {
		return pageSize.isPresent() && pageIndex.isPresent();
	}

	public Field<Integer> getLimitField() {
		return pageSize.map(v -> (Field<Integer>) DSL.val(v)).orElse(DSL.noField(SQLDataType.INTEGER));
	}

	public Optional<Integer> getOffset() {
		if(!isPagingPresent()) {
			return Optional.empty();
		}
		return Optional.of(pageSize.get() * pageIndex.get());
	}

	public Field<Integer> getOffsetField() {
		return getOffset().map(v -> (Field<Integer>) DSL.val(v)).orElse(DSL.noField(SQLDataType.INTEGER));
	}
}
