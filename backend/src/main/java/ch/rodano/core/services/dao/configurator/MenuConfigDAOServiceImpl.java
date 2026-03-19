package ch.rodano.core.services.dao.configurator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.config.MenuActionConfigDTO;
import ch.rodano.api.config.MenuConfigDTO;
import ch.rodano.core.model.jooq.tables.records.MenuRecord;

import static ch.rodano.core.model.jooq.tables.Menu.MENU;

@Repository
public class MenuConfigDAOServiceImpl implements MenuConfigDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public MenuConfigDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "menus", key = "#projectId.toString()")
	public List<MenuConfigDTO> getMenus(final UUID projectId) {
		final var records = dslContext
			.selectFrom(MENU)
			.where(MENU.PROJECT_ID.eq(projectId))
			.orderBy(MENU.SORT_ORDER.asc())
			.fetch();

		if(records.isEmpty()) {
			return List.of();
		}

		final var menuIds = records.map(MenuRecord::getMenuId);
		final var submenuMap = loadSubmenuIds(projectId, menuIds);

		return records.map(record -> mapToDTO(record, submenuMap));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "menu", key = "#projectId.toString() + ':' + #menuId.toString()")
	public MenuConfigDTO getMenu(final UUID projectId, final UUID menuId) {
		final var record = dslContext
			.selectFrom(MENU)
			.where(MENU.PROJECT_ID.eq(projectId))
			.and(MENU.MENU_ID.eq(menuId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		final var submenuMap = loadSubmenuIds(projectId, List.of(menuId));
		return mapToDTO(record, submenuMap);
	}

	@Override
	@Transactional
	@CacheEvict(value = "menus", key = "#projectId.toString()")
	public MenuConfigDTO createMenu(final UUID projectId, final MenuConfigDTO dto) {
		final var menuId = dto.getMenuId() != null ? dto.getMenuId() : UUID.randomUUID();

		dslContext.insertInto(MENU)
			.set(MENU.MENU_ID, menuId)
			.set(MENU.PROJECT_ID, projectId)
			.set(MENU.PARENT_MENU_ID, dto.getParentMenuId())
			.set(MENU.CODE, dto.getId())
			.set(MENU.SORT_ORDER, dto.getSortOrder())
			.set(MENU.ORDER_BY, dto.getOrderBy())
			.set(MENU.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(MENU.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(MENU.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(MENU.IS_PUBLIC, dto.isPublic())
			.set(MENU.IS_HOME_PAGE, dto.isHomePage())
			.set(MENU.ACTION_PAGE, dto.getAction() != null ? dto.getAction().getPage() : null)
			.set(MENU.ACTION_CONTEXT, jsonMapperService.toJson(dto.getAction() != null ? dto.getAction().getContext() : null))
			.set(MENU.ACTION_PARAMS, jsonMapperService.toJson(dto.getAction() != null ? dto.getAction().getParameters() : null))
			.execute();

		return getMenu(projectId, menuId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "menus", key = "#projectId.toString()"),
		@CacheEvict(value = "menu", key = "#projectId.toString() + ':' + #menuId.toString()")
	})
	public MenuConfigDTO updateMenu(final UUID projectId, final UUID menuId, final MenuConfigDTO dto) {
		dslContext.update(MENU)
			.set(MENU.PARENT_MENU_ID, dto.getParentMenuId())
			.set(MENU.CODE, dto.getId())
			.set(MENU.SORT_ORDER, dto.getSortOrder())
			.set(MENU.ORDER_BY, dto.getOrderBy())
			.set(MENU.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(MENU.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(MENU.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(MENU.IS_PUBLIC, dto.isPublic())
			.set(MENU.IS_HOME_PAGE, dto.isHomePage())
			.set(MENU.ACTION_PAGE, dto.getAction() != null ? dto.getAction().getPage() : null)
			.set(MENU.ACTION_CONTEXT, jsonMapperService.toJson(dto.getAction() != null ? dto.getAction().getContext() : null))
			.set(MENU.ACTION_PARAMS, jsonMapperService.toJson(dto.getAction() != null ? dto.getAction().getParameters() : null))
			.where(MENU.PROJECT_ID.eq(projectId))
			.and(MENU.MENU_ID.eq(menuId))
			.execute();

		return getMenu(projectId, menuId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "menus", key = "#projectId.toString()"),
		@CacheEvict(value = "menu", key = "#projectId.toString() + ':' + #menuId.toString()")
	})
	public void deleteMenu(final UUID projectId, final UUID menuId) {
		final var record = dslContext
			.selectFrom(MENU)
			.where(MENU.PROJECT_ID.eq(projectId))
			.and(MENU.MENU_ID.eq(menuId))
			.fetchOne();

		if(record != null) {
			dslContext.update(MENU)
				.set(MENU.PARENT_MENU_ID, record.getParentMenuId())
				.where(MENU.PROJECT_ID.eq(projectId))
				.and(MENU.MENU_ID.eq(menuId))
				.execute();
		}

		dslContext.deleteFrom(MENU)
			.where(MENU.PROJECT_ID.eq(projectId))
			.and(MENU.MENU_ID.eq(menuId))
			.execute();
	}

	private Map<UUID, List<UUID>> loadSubmenuIds(final UUID projectId, final List<UUID> menuIds) {
		if(menuIds == null || menuIds.isEmpty()) {
			return Map.of();
		}

		final var rows = dslContext
			.select(MENU.PARENT_MENU_ID, MENU.MENU_ID)
			.from(MENU)
			.where(MENU.PROJECT_ID.eq(projectId))
			.and(MENU.PARENT_MENU_ID.in(menuIds))
			.orderBy(MENU.SORT_ORDER.asc())
			.fetch();

		final Map<UUID, List<UUID>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2());
		}
		return result;
	}

	private MenuConfigDTO mapToDTO(final MenuRecord record, final Map<UUID, List<UUID>> submenuMap) {
		final var dto = new MenuConfigDTO();
		dto.setMenuId(record.getMenuId());
		dto.setParentMenuId(record.getParentMenuId());
		dto.setId(record.getCode());
		dto.setSortOrder(record.getSortOrder());
		dto.setOrderBy(record.getOrderBy());
		dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setLongname(jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setPublic(record.getIsPublic() != null && record.getIsPublic());
		dto.setHomePage(record.getIsHomePage() != null && record.getIsHomePage());
		dto.setSubmenuIds(submenuMap.getOrDefault(record.getMenuId(), List.of()));

		if(record.getActionPage() != null) {
			final var action = new MenuActionConfigDTO();
			action.setPage(record.getActionPage());
			action.setContext(jsonMapperService.fromJson(record.getActionContext(), new TypeReference<>() {
			}));
			action.setParameters(jsonMapperService.fromJson(record.getActionParams(), new TypeReference<>() {
			}));
			dto.setAction(action);
		}

		return dto;
	}
}
