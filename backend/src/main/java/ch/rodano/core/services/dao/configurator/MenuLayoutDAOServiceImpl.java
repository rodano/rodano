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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.config.SectionDTO;
import ch.rodano.api.config.WidgetDTO;
import ch.rodano.api.config.WidgetLayoutDTO;

import static ch.rodano.core.model.jooq.tables.MenuLayoutSection.MENU_LAYOUT_SECTION;
import static ch.rodano.core.model.jooq.tables.MenuLayoutSectionWidget.MENU_LAYOUT_SECTION_WIDGET;
import static ch.rodano.core.model.jooq.tables.MenuLayoutSectionWidgetParameter.MENU_LAYOUT_SECTION_WIDGET_PARAMETER;

@Repository
public class MenuLayoutDAOServiceImpl implements MenuLayoutDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;
	private final TransactionTemplate transactionTemplate;

	public MenuLayoutDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService, final PlatformTransactionManager transactionManager) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@Override
	@Cacheable(value = "menuLayout", key = "#projectId.toString() + ':' + #menuId.toString() + ':layout'")
	public WidgetLayoutDTO getLayout(final UUID projectId, final UUID menuId) {
		final var sectionRecords = dslContext.selectFrom(MENU_LAYOUT_SECTION)
			.where(MENU_LAYOUT_SECTION.PROJECT_ID.eq(projectId))
			.and(MENU_LAYOUT_SECTION.MENU_ID.eq(menuId))
			.orderBy(MENU_LAYOUT_SECTION.SORT_ORDER)
			.fetch();

		final List<SectionDTO> sections = new ArrayList<>();
		for(final var sr : sectionRecords) {
			final var widgetRecords = dslContext.selectFrom(MENU_LAYOUT_SECTION_WIDGET)
				.where(MENU_LAYOUT_SECTION_WIDGET.PROJECT_ID.eq(projectId))
				.and(MENU_LAYOUT_SECTION_WIDGET.MENU_SECTION_ID.eq(sr.getMenuSectionId()))
				.orderBy(MENU_LAYOUT_SECTION_WIDGET.WIDGET_ORDER)
				.fetch();

			final List<WidgetDTO> widgets = new ArrayList<>();
			for(final var wr : widgetRecords) {
				final var paramRecords = dslContext.selectFrom(MENU_LAYOUT_SECTION_WIDGET_PARAMETER)
					.where(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.MENU_WIDGET_ID.eq(wr.getMenuWidgetId()))
					.fetch();

				final Map<String, String> params = new HashMap<>();
				for(final var pr : paramRecords) {
					if(pr.getChartId() != null) {
						params.put("chart", pr.getChartId().toString());
					}
					if(pr.getWorkflowWidgetId() != null) {
						params.put("workflow", pr.getWorkflowWidgetId().toString());
					}
					if(pr.getWorkflowSummaryId() != null) {
						params.put("summary", pr.getWorkflowSummaryId().toString());
					}
					if(pr.getCategoryId() != null) {
						params.put("category", pr.getCategoryId().toString());
					}
					if(pr.getScopeModelId() != null) {
						params.put("scopeModelId", pr.getScopeModelId().toString());
					}
					if(pr.getTitle() != null) {
						params.put("title", pr.getTitle());
					}
					if(pr.getWidth() != null) {
						params.put("width", pr.getWidth().toString());
					}
					if(pr.getDisplayAddResponse() != null) {
						params.put("displayAddResponse", pr.getDisplayAddResponse().toString());
					}
					if(pr.getRemoveProfileSelector() != null) {
						params.put("removeProfileSelector", pr.getRemoveProfileSelector().toString());
					}
					if(pr.getOverdueType() != null) {
						params.put("overdueType", pr.getOverdueType());
					}
					if(pr.getSpecificColumnName() != null) {
						params.put("specificColumnName", pr.getSpecificColumnName());
					}
				}

				widgets.add(new WidgetDTO(
					wr.getMenuWidgetId(),
					wr.getType(),
					wr.getWidth(),
					wr.getWidgetOrder(),
					wr.getTextBefore(),
					wr.getTextAfter(),
					wr.getRequiredFeatureId(),
					wr.getRightEntity(),
					wr.getRightValue(),
					wr.getRightTargetId(),
					params
				));
			}

			final TreeMap<String, String> label = jsonMapperService.fromJson(
				sr.getLabel(), new TypeReference<>() {
				}
			);

			sections.add(new SectionDTO(
				sr.getMenuSectionId(),
				sr.getCode(),
				label,
				sr.getSortOrder() != null ? sr.getSortOrder() : 0,
				sr.getRequiredFeatureId(),
				sr.getRightEntity(),
				sr.getRightValue(),
				sr.getRightTargetId(),
				widgets
			));
		}

		return new WidgetLayoutDTO(sections);
	}

	@Override
	@Caching(evict = {
		@CacheEvict(value = "menuLayout", key = "#projectId.toString() + ':' + #menuId.toString() + ':layout'"),
		@CacheEvict(value = "menus", key = "#projectId.toString()"),
		@CacheEvict(value = "menu", key = "#projectId.toString() + ':' + #menuId.toString()")
	})
	public WidgetLayoutDTO saveLayout(final UUID projectId, final UUID menuId, final WidgetLayoutDTO dto) {
		int attempt = 0;
		while(true) {
			try {
				transactionTemplate.executeWithoutResult(_ -> doSaveLayout(projectId, menuId, dto));
				return getLayout(projectId, menuId);
			}
			catch(Exception e) {
				final String msg = e.getMessage() != null ? e.getMessage() : "";
				if(attempt++ >= 5 || !msg.contains("1213") && !msg.contains("certification") && !msg.contains("Deadlock")) {
					throw e;
				}
				try {
					Thread.sleep((long) (100 * Math.pow(2, attempt)));
				}
				catch(InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void doSaveLayout(final UUID projectId, final UUID menuId, final WidgetLayoutDTO dto) {
		final var sectionIds = dslContext
			.select(MENU_LAYOUT_SECTION.MENU_SECTION_ID)
			.from(MENU_LAYOUT_SECTION)
			.where(MENU_LAYOUT_SECTION.PROJECT_ID.eq(projectId))
			.and(MENU_LAYOUT_SECTION.MENU_ID.eq(menuId))
			.fetch(MENU_LAYOUT_SECTION.MENU_SECTION_ID);

		for(final UUID sectionId : sectionIds) {
			final var widgetIds = dslContext
				.select(MENU_LAYOUT_SECTION_WIDGET.MENU_WIDGET_ID)
				.from(MENU_LAYOUT_SECTION_WIDGET)
				.where(MENU_LAYOUT_SECTION_WIDGET.MENU_SECTION_ID.eq(sectionId))
				.fetch(MENU_LAYOUT_SECTION_WIDGET.MENU_WIDGET_ID);

			for(final UUID widgetId : widgetIds) {
				dslContext.deleteFrom(MENU_LAYOUT_SECTION_WIDGET_PARAMETER)
					.where(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.MENU_WIDGET_ID.eq(widgetId))
					.execute();
			}

			dslContext.deleteFrom(MENU_LAYOUT_SECTION_WIDGET)
				.where(MENU_LAYOUT_SECTION_WIDGET.MENU_SECTION_ID.eq(sectionId))
				.execute();
		}

		dslContext.deleteFrom(MENU_LAYOUT_SECTION)
			.where(MENU_LAYOUT_SECTION.PROJECT_ID.eq(projectId))
			.and(MENU_LAYOUT_SECTION.MENU_ID.eq(menuId))
			.execute();

		for(int i = 0; i < dto.sections().size(); i++) {
			final SectionDTO section = dto.sections().get(i);
			final UUID sectionId = section.sectionId() != null ? section.sectionId() : UUID.randomUUID();

			dslContext.insertInto(MENU_LAYOUT_SECTION)
				.set(MENU_LAYOUT_SECTION.MENU_SECTION_ID, sectionId)
				.set(MENU_LAYOUT_SECTION.PROJECT_ID, projectId)
				.set(MENU_LAYOUT_SECTION.MENU_ID, menuId)
				.set(MENU_LAYOUT_SECTION.CODE, section.id())
				.set(MENU_LAYOUT_SECTION.SORT_ORDER, i)
				.set(MENU_LAYOUT_SECTION.LABEL, jsonMapperService.toJson(section.label()))
				.set(MENU_LAYOUT_SECTION.REQUIRED_FEATURE_ID, section.requiredFeatureId())
				.set(MENU_LAYOUT_SECTION.RIGHT_ENTITY, section.rightEntity())
				.set(MENU_LAYOUT_SECTION.RIGHT_VALUE, section.rightValue())
				.set(MENU_LAYOUT_SECTION.RIGHT_TARGET_ID, section.rightTargetId())
				.execute();

			for(int j = 0; j < section.widgets().size(); j++) {
				final WidgetDTO widget = section.widgets().get(j);
				final UUID widgetId = widget.widgetId() != null ? widget.widgetId() : UUID.randomUUID();

				dslContext.insertInto(MENU_LAYOUT_SECTION_WIDGET)
					.set(MENU_LAYOUT_SECTION_WIDGET.MENU_WIDGET_ID, widgetId)
					.set(MENU_LAYOUT_SECTION_WIDGET.PROJECT_ID, projectId)
					.set(MENU_LAYOUT_SECTION_WIDGET.MENU_ID, menuId)
					.set(MENU_LAYOUT_SECTION_WIDGET.MENU_SECTION_ID, sectionId)
					.set(MENU_LAYOUT_SECTION_WIDGET.WIDGET_ORDER, j)
					.set(MENU_LAYOUT_SECTION_WIDGET.TYPE, widget.type())
					.set(MENU_LAYOUT_SECTION_WIDGET.WIDTH, widget.width())
					.set(MENU_LAYOUT_SECTION_WIDGET.TEXT_BEFORE, widget.textBefore())
					.set(MENU_LAYOUT_SECTION_WIDGET.TEXT_AFTER, widget.textAfter())
					.set(MENU_LAYOUT_SECTION_WIDGET.REQUIRED_FEATURE_ID, widget.requiredFeatureId())
					.set(MENU_LAYOUT_SECTION_WIDGET.RIGHT_ENTITY, widget.rightEntity())
					.set(MENU_LAYOUT_SECTION_WIDGET.RIGHT_VALUE, widget.rightValue())
					.set(MENU_LAYOUT_SECTION_WIDGET.RIGHT_TARGET_ID, widget.rightTargetId())
					.execute();

				if(widget.parameters() != null && !widget.parameters().isEmpty()) {
					insertWidgetParameters(projectId, menuId, sectionId, widgetId, j, widget.parameters());
				}
			}
		}
	}

	private void insertWidgetParameters(
		final UUID projectId, final UUID menuId, final UUID sectionId,
		final UUID widgetId, final int widgetOrder, final Map<String, String> params
	) {
		var insert = dslContext.insertInto(MENU_LAYOUT_SECTION_WIDGET_PARAMETER)
			.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.PROJECT_ID, projectId)
			.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.MENU_ID, menuId)
			.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.MENU_SECTION_ID, sectionId)
			.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.MENU_WIDGET_ID, widgetId)
			.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.WIDGET_ORDER, widgetOrder);

		final String chartId = params.get("chart");
		if(chartId != null) {
			insert = insert.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.CHART_ID, UUID.fromString(chartId));
		}

		final String workflowId = params.get("workflow");
		if(workflowId != null) {
			insert = insert.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.WORKFLOW_WIDGET_ID, UUID.fromString(workflowId));
		}

		final String summaryId = params.get("summary");
		if(summaryId != null) {
			insert = insert.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.WORKFLOW_SUMMARY_ID, UUID.fromString(summaryId));
		}

		final String categoryId = params.get("category");
		if(categoryId != null) {
			insert = insert.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.CATEGORY_ID, UUID.fromString(categoryId));
		}

		final String scopeModelId = params.get("scopeModelId");
		if(scopeModelId != null) {
			insert = insert.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.SCOPE_MODEL_ID, UUID.fromString(scopeModelId));
		}

		final String title = params.get("title");
		if(title != null) {
			insert = insert.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.TITLE, title);
		}

		final String width = params.get("width");
		if(width != null) {
			insert = insert.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.WIDTH, Integer.parseInt(width));
		}

		final String displayAddResponse = params.get("displayAddResponse");
		if(displayAddResponse != null) {
			insert = insert.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.DISPLAY_ADD_RESPONSE,
				Boolean.parseBoolean(displayAddResponse));
		}

		final String removeProfileSelector = params.get("removeProfileSelector");
		if(removeProfileSelector != null) {
			insert = insert.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.REMOVE_PROFILE_SELECTOR,
				Boolean.parseBoolean(removeProfileSelector));
		}

		final String overdueType = params.get("overdueType");
		if(overdueType != null) {
			insert = insert.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.OVERDUE_TYPE, overdueType);
		}

		final String specificColumnName = params.get("specificColumnName");
		if(specificColumnName != null) {
			insert = insert.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.SPECIFIC_COLUMN_NAME, specificColumnName);
		}

		insert.execute();
	}
}
