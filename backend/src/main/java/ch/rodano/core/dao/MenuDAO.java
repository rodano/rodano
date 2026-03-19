package ch.rodano.core.dao;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.configuration.model.cms.CMSAction;
import ch.rodano.configuration.model.cms.CMSLayout;
import ch.rodano.configuration.model.menu.Menu;
import ch.rodano.core.model.jooq.tables.records.MenuRecord;

import static ch.rodano.core.model.jooq.tables.Menu.MENU;

@Repository
public class MenuDAO implements BaseProjectDAO<Menu> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final CMSLayoutDAO cmsLayoutDAO;

	public MenuDAO(final DSLContext dslContext, final MappingHelper mappingHelper, final CMSLayoutDAO cmsLayoutDAO) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.cmsLayoutDAO = cmsLayoutDAO;
	}

	@Override
	public List<Menu> findByProject(final UUID projectId) {
		final var allMenus = dslContext.selectFrom(MENU)
			.where(MENU.PROJECT_ID.eq(projectId))
			.orderBy(MENU.ORDER_BY)
			.fetch(this::mapToModel);

		return allMenus.stream()
			.filter(menu -> {
				final var record = dslContext.selectFrom(MENU)
					.where(MENU.PROJECT_ID.eq(projectId))
					.and(MENU.CODE.eq(menu.getId()))
					.fetchOne();
				return record != null && record.getParentMenuId() == null;
			})
			.toList();
	}

	@Override
	public Menu findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(MENU)
			.where(MENU.PROJECT_ID.eq(projectId))
			.and(MENU.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public Menu findById(final UUID id) {
		return dslContext.selectFrom(MENU)
			.where(MENU.MENU_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public Menu save(final Menu entity) {
		return null;
	}

	@Override
	public void delete(final UUID id) {

	}

	private Menu mapToModel(final MenuRecord record) {
		if(record == null) {
			return null;
		}

		final Menu model = new Menu();

		model.setMenuId(record.getMenuId());
		model.setId(record.getCode());
		model.setOrderBy(record.getOrderBy() != null ? record.getOrderBy() : 0);
		model.setPublic(record.getIsPublic() != null ? record.getIsPublic() : false);
		model.setHomePage(record.getIsHomePage() != null ? record.getIsHomePage() : false);

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

		final CMSLayout layout = cmsLayoutDAO.findByMenu(record.getMenuId());
		if(layout != null) {
			model.setLayout(layout);
		}

		if(record.getActionPage() != null) {
			final CMSAction action = new CMSAction();
			action.setPage(record.getActionPage());
			if(record.getActionContext() != null && !record.getActionContext().isBlank()) {
				try {
					action.setContext(mappingHelper.parseJson(record.getActionContext(), new TypeReference<>() {
					}));
				}
				catch(Exception e) {
					action.setContext(List.of(record.getActionContext()));
				}
			}
			if(record.getActionParams() != null && !record.getActionParams().isBlank()) {
				action.setParameters(mappingHelper.parseJsonToMap(record.getActionParams()));
			}
			model.setAction(action);
		}

		model.setSubmenus(loadSubmenus(record.getMenuId(), record.getProjectId()));
		return model;
	}

	private SortedSet<Menu> loadSubmenus(final UUID parentMenuId, final UUID projectId) {
		return dslContext.selectFrom(MENU)
			.where(MENU.PARENT_MENU_ID.eq(parentMenuId))
			.and(MENU.PROJECT_ID.eq(projectId))
			.orderBy(MENU.ORDER_BY)
			.fetch(this::mapToModel)
			.stream()
			.collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
	}
}
