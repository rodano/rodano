package ch.rodano.core.services.dao.resource;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.ResourceRecord;
import ch.rodano.core.model.resource.Resource;
import ch.rodano.core.model.resource.ResourceSearch;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AbstractDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.Tables.RESOURCE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;

@Service
public class ResourceDAOServiceImpl extends AbstractDAOService<Resource, ResourceRecord> implements ResourceDAOService {

	public ResourceDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService
	) {
		super(create, strategy, studyService);
	}

	@Override
	protected Table<ResourceRecord> getTable() {
		return Tables.RESOURCE;
	}

	@Override
	protected Class<Resource> getDAOClass() {
		return Resource.class;
	}

	@Override
	public PagedResult<Resource> search(final ResourceSearch search) {
		//remember: user may not have the right to see his own resources if his rights have been updated
		final var saAbove = SCOPE_ANCESTOR.as("above");
		final var saUnder = SCOPE_ANCESTOR.as("under");

		final List<Condition> conditions = new ArrayList<>();

		search.getReferenceScopePks().ifPresent(scopePks -> {
			final var condition = RESOURCE.SCOPE_FK.in(scopePks) //resource attached to user root scopes
				.or(saAbove.ANCESTOR_FK.in(scopePks)) //resource attached to scopes that are under user root scopes
				.or(saUnder.SCOPE_FK.in(scopePks)); //resource that spread and attached to scopes that are above user root scopes
			conditions.add(condition);
		});

		if(search.getOnlyPublic().isPresent()) {
			conditions.add(RESOURCE.PUBLIC_RESOURCE.eq(true));
		}

		search.getCategoryId().ifPresent(categoryId -> {
			conditions.add(RESOURCE.CATEGORY_ID.eq(categoryId));
		});

		search.getFullText().ifPresent(fullText -> {
			conditions.add(RESOURCE.TITLE.containsIgnoreCase(fullText).or(RESOURCE.DESCRIPTION.containsIgnoreCase(fullText)));
		});

		if(!search.getIncludeDeleted()) {
			conditions.add(RESOURCE.DELETED.isFalse());
		}

		final var query = create.selectDistinct(RESOURCE.asterisk(), DSL.count().over().as("total")).from(RESOURCE)
			.leftJoin(saAbove).on(RESOURCE.SCOPE_FK.eq(saAbove.SCOPE_FK))
			.leftJoin(saUnder).on(RESOURCE.SCOPE_FK.eq(saUnder.ANCESTOR_FK))
			.where(conditions)
			.groupBy(RESOURCE.PK)
			.orderBy(search.getSortBy().getField().sort(search.getOrder()))
			.limit(search.getLimitField())
			.offset(search.getOffsetField());

		final var result = query.fetch();
		var total = 0;
		if(result.size() > 0) {
			total = result.get(0).getValue("total", Integer.class);
		}

		final var resources = result.into(Resource.class);
		return new PagedResult<>(resources, search.getPageSize(), search.getPageIndex(), total);
	}

	@Override
	public List<Resource> getAllResources() {
		final var query = create.selectFrom(RESOURCE);
		return find(query);
	}

	@Override
	public void deleteResource(final Resource resource, final DatabaseActionContext context, final String rationale) {
		delete(resource, context, rationale);
	}

	@Override
	public void restoreResource(final Resource resource, final DatabaseActionContext context, final String rationale) {
		restore(resource, context, rationale);
	}

	@Override
	public Resource getResourceByPk(final Long pk) {
		final var query = create.selectFrom(RESOURCE).where(RESOURCE.PK.eq(pk));
		return findUnique(query);
	}

	@Override
	public void saveResource(final Resource resource, final DatabaseActionContext context, final String rationale) {
		save(resource, context, rationale);
	}

}
