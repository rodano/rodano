package ch.rodano.api.dto.paging;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * It is currently not possible to annotate this object with a @Schema because of a swagger-core behaviour that does not
 * play nice with generic classes. See more at <a href="https://github.com/swagger-api/swagger-core/issues/3323#issuecomment-917043167">...</a>
 */
public class PagedResult<T> {
	@Schema(description = "Objects of the page")
	@NotNull
	private List<T> objects;
	@Schema(description = "Paging information")
	@NotNull
	private Paging paging;

	public PagedResult() {}

	public PagedResult(final List<T> objects, final Optional<Integer> pageSize, final Optional<Integer> pageIndex, final int total) {
		this.objects = objects;
		this.paging = new Paging(pageSize.orElse(objects.size()), pageIndex.orElse(0), total);
	}

	public PagedResult(final List<T> objects, final int pageSize, final int pageIndex, final int total) {
		this.objects = objects;
		this.paging = new Paging(pageSize, pageIndex, total);
	}

	public PagedResult(final List<T> objects) {
		this.objects = objects;
		this.paging = new Paging(objects.size(), 0, objects.size());
	}

	public <R> PagedResult<R> withObjectTransformation(final Function<T, R> transformer) {
		final var transformedObjects = objects.stream().map(transformer).toList();
		return new PagedResult<R>(transformedObjects, paging.pageSize(), paging.pageIndex(), paging.total());
	}

	public <R> PagedResult<R> withObjectsTransformation(final Function<List<T>, List<R>> transformer) {
		final var transformedObjects = transformer.apply(objects);
		return new PagedResult<R>(transformedObjects, paging.pageSize(), paging.pageIndex(), paging.total());
	}

	public List<T> getObjects() {
		return objects;
	}

	public void setObjects(final List<T> objects) {
		this.objects = objects;
	}

	public Paging getPaging() {
		return paging;
	}

	public void setPaging(final Paging paging) {
		this.paging = paging;
	}
}
