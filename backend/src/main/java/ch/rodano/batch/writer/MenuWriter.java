package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Menu;
import ch.rodano.batch.pojo.MenuAction;
import ch.rodano.batch.pojo.MenuLayout;
import ch.rodano.batch.pojo.MenuLayoutSection;
import ch.rodano.batch.pojo.MenuLayoutSectionWidget;
import ch.rodano.batch.pojo.MenuLayoutSectionWidgetParameter;
import ch.rodano.batch.pojo.ScopeCriterionRight;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveChartId;
import static ch.rodano.batch.helper.ModelResolvers.resolveFeatureId;
import static ch.rodano.batch.helper.ModelResolvers.resolveMenuId;
import static ch.rodano.batch.helper.ModelResolvers.resolveResourceCategoryId;
import static ch.rodano.batch.helper.ModelResolvers.resolveScopeModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowSummaryId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowWidgetId;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.Menu.MENU;
import static ch.rodano.core.model.jooq.tables.MenuLayoutSection.MENU_LAYOUT_SECTION;
import static ch.rodano.core.model.jooq.tables.MenuLayoutSectionWidget.MENU_LAYOUT_SECTION_WIDGET;
import static ch.rodano.core.model.jooq.tables.MenuLayoutSectionWidgetParameter.MENU_LAYOUT_SECTION_WIDGET_PARAMETER;

public class MenuWriter extends BaseWriter {

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<Menu> wrapped = (ProjectScoped<Menu>) raw;
				final UUID projectId = wrapped.getProjectId();
				final Menu root = wrapped.getPayload();

				upsertMenuTree(tx, projectId, root, null, 0);
			}
		});
	}

	private void upsertMenuTree(final DSLContext tx,
								final UUID projectId,
								final Menu menu,
								final UUID parentMenuId,
								final int sortOrder) {

		final UUID menuId = upsertSingleMenu(tx, projectId, menu, parentMenuId, sortOrder);

		if(menu.getSubmenus() != null && !menu.getSubmenus().isEmpty()) {
			int childOrder = 0;
			for(Menu child : menu.getSubmenus()) {
				upsertMenuTree(tx, projectId, child, menuId, childOrder++);
			}
		}
	}

	private UUID upsertSingleMenu(final DSLContext tx,
								  final UUID projectId,
								  final Menu menu,
								  final UUID parentMenuId,
								  final int sortOrder) {

		final String menuCode = menu.getId();
		final UUID existingId = resolveMenuId(tx, projectId, menuCode);
		final UUID menuId = existingId != null ? existingId : deterministic(projectId, "MENU", menuCode);

		String actionPage = null;
		String actionContext = null;
		String actionParams = null;

		final MenuAction action = menu.getAction();
		if(action != null) {
			actionPage = action.getPage();

			List<String> resolvedContext = action.getContext();
			if("scopes".equals(action.getPage()) && action.getContext() != null && !action.getContext().isEmpty()) {
				resolvedContext = action.getContext().stream()
					.map(code -> {
						final UUID scopeModelId = resolveScopeModelId(tx, projectId, code);
						return scopeModelId != null ? scopeModelId.toString() : code;
					})
					.toList();
			}
			actionContext = toJson(resolvedContext);
			actionParams = toJson(action.getParameters());
		}

		tx.insertInto(MENU)
			.set(MENU.PROJECT_ID, projectId)
			.set(MENU.MENU_ID, menuId)
			.set(MENU.PARENT_MENU_ID, parentMenuId)
			.set(MENU.SORT_ORDER, sortOrder)
			.set(MENU.CODE, menuCode)
			.set(MENU.ORDER_BY, menu.getOrderBy())
			.set(MENU.IS_PUBLIC, menu.getPublic())
			.set(MENU.IS_HOME_PAGE, menu.getHomePage())
			.set(MENU.SHORTNAME, toJson(menu.getShortname()))
			.set(MENU.LONGNAME, toJson(menu.getLongname()))
			.set(MENU.DESCRIPTION, toJson(menu.getDescription()))
			.set(MENU.ACTION_PAGE, actionPage)
			.set(MENU.ACTION_CONTEXT, actionContext)
			.set(MENU.ACTION_PARAMS, actionParams)
			.onDuplicateKeyUpdate()
			.set(MENU.PARENT_MENU_ID, parentMenuId)
			.set(MENU.SORT_ORDER, sortOrder)
			.set(MENU.ORDER_BY, menu.getOrderBy())
			.set(MENU.IS_PUBLIC, menu.getPublic())
			.set(MENU.IS_HOME_PAGE, menu.getHomePage())
			.set(MENU.SHORTNAME, toJson(menu.getShortname()))
			.set(MENU.LONGNAME, toJson(menu.getLongname()))
			.set(MENU.DESCRIPTION, toJson(menu.getDescription()))
			.set(MENU.ACTION_PAGE, actionPage)
			.set(MENU.ACTION_CONTEXT, actionContext)
			.set(MENU.ACTION_PARAMS, actionParams)
			.execute();

		upsertSectionsAndWidgets(tx, projectId, menuId, menu);
		return menuId;
	}

	private void upsertSectionsAndWidgets(final DSLContext tx, final UUID projectId, final UUID menuId, final Menu menu) {
		final MenuLayout layout = menu.getLayout();
		if(layout == null || layout.getSections() == null || layout.getSections().isEmpty()) {
			return;
		}

		int sectionOrder = 0;
		for(MenuLayoutSection section : layout.getSections()) {
			final String sectionCode = section.getId();
			final UUID sectionId = deterministic(projectId, "MENU_SECTION", menu.getId() + "|" + nullSafe(sectionCode));

			final UUID featureId = resolveFeatureId(tx, projectId, section.getRequiredFeature());

			final ScopeCriterionRight sr = section.getRequiredRight();
			final String rightEntityLayout = sr == null ? null : sr.getRightEntity();
			final String rightValueLayout = sr == null ? null : sr.getRight();
			final UUID rightTargetIdLayout = sr == null
				? null
				: resolveScopeModelId(tx, projectId, sr.getId());

			tx.insertInto(MENU_LAYOUT_SECTION)
				.set(MENU_LAYOUT_SECTION.PROJECT_ID, projectId)
				.set(MENU_LAYOUT_SECTION.MENU_ID, menuId)
				.set(MENU_LAYOUT_SECTION.MENU_SECTION_ID, sectionId)
				.set(MENU_LAYOUT_SECTION.CODE, sectionCode)
				.set(MENU_LAYOUT_SECTION.SORT_ORDER, sectionOrder++)
				.set(MENU_LAYOUT_SECTION.REQUIRED_FEATURE_ID, featureId)
				.set(MENU_LAYOUT_SECTION.RIGHT_ENTITY, rightEntityLayout)
				.set(MENU_LAYOUT_SECTION.RIGHT_VALUE, rightValueLayout)
				.set(MENU_LAYOUT_SECTION.RIGHT_TARGET_ID, rightTargetIdLayout)
				.set(MENU_LAYOUT_SECTION.LABEL, toJson(section.getLabels()))
				.onDuplicateKeyUpdate()
				.set(MENU_LAYOUT_SECTION.CODE, sectionCode)
				.set(MENU_LAYOUT_SECTION.SORT_ORDER, sectionOrder - 1)
				.set(MENU_LAYOUT_SECTION.REQUIRED_FEATURE_ID, featureId)
				.set(MENU_LAYOUT_SECTION.RIGHT_ENTITY, rightEntityLayout)
				.set(MENU_LAYOUT_SECTION.RIGHT_VALUE, rightValueLayout)
				.set(MENU_LAYOUT_SECTION.RIGHT_TARGET_ID, rightTargetIdLayout)
				.set(MENU_LAYOUT_SECTION.LABEL, toJson(section.getLabels()))
				.execute();

			if(section.getWidgets() != null && !section.getWidgets().isEmpty()) {
				int widgetOrder = 0;
				for(MenuLayoutSectionWidget w : section.getWidgets()) {
					final UUID widgetId = deterministic(projectId, "MENU_WIDGET",
						menu.getId() + "|" + sectionCode + "|" + widgetOrder);

					final UUID reqFeatureId = resolveFeatureId(tx, projectId, w.getRequiredFeature());

					final ScopeCriterionRight wr = w.getRequiredRight();
					final String rightEntityWidget = wr == null ? null : wr.getRightEntity();
					final String rightValueWidget = wr == null ? null : wr.getRight();
					final UUID rightTargetIdWidget = wr == null
						? null
						: resolveScopeModelId(tx, projectId, wr.getId());

					tx.insertInto(MENU_LAYOUT_SECTION_WIDGET)
						.set(MENU_LAYOUT_SECTION_WIDGET.PROJECT_ID, projectId)
						.set(MENU_LAYOUT_SECTION_WIDGET.MENU_WIDGET_ID, widgetId)
						.set(MENU_LAYOUT_SECTION_WIDGET.MENU_ID, menuId)
						.set(MENU_LAYOUT_SECTION_WIDGET.MENU_SECTION_ID, sectionId)
						.set(MENU_LAYOUT_SECTION_WIDGET.WIDGET_ORDER, widgetOrder)
						.set(MENU_LAYOUT_SECTION_WIDGET.TYPE, w.getType())
						.set(MENU_LAYOUT_SECTION_WIDGET.WIDTH, w.getWidth())
						.set(MENU_LAYOUT_SECTION_WIDGET.TEXT_BEFORE, w.getTextBefore())
						.set(MENU_LAYOUT_SECTION_WIDGET.TEXT_AFTER, w.getTextAfter())
						.set(MENU_LAYOUT_SECTION_WIDGET.REQUIRED_FEATURE_ID, reqFeatureId)
						.set(MENU_LAYOUT_SECTION_WIDGET.RIGHT_ENTITY, rightEntityWidget)
						.set(MENU_LAYOUT_SECTION_WIDGET.RIGHT_VALUE, rightValueWidget)
						.set(MENU_LAYOUT_SECTION_WIDGET.RIGHT_TARGET_ID, rightTargetIdWidget)
						.onDuplicateKeyUpdate()
						.set(MENU_LAYOUT_SECTION_WIDGET.TYPE, w.getType())
						.set(MENU_LAYOUT_SECTION_WIDGET.WIDTH, w.getWidth())
						.set(MENU_LAYOUT_SECTION_WIDGET.TEXT_BEFORE, w.getTextBefore())
						.set(MENU_LAYOUT_SECTION_WIDGET.TEXT_AFTER, w.getTextAfter())
						.set(MENU_LAYOUT_SECTION_WIDGET.REQUIRED_FEATURE_ID, reqFeatureId)
						.set(MENU_LAYOUT_SECTION_WIDGET.RIGHT_ENTITY, rightEntityWidget)
						.set(MENU_LAYOUT_SECTION_WIDGET.RIGHT_VALUE, rightValueWidget)
						.set(MENU_LAYOUT_SECTION_WIDGET.RIGHT_TARGET_ID, rightTargetIdWidget)
						.execute();

					final MenuLayoutSectionWidgetParameter parameter = w.getParameters();
					if(parameter != null) {
						final UUID scopeModelId = resolveScopeModelId(tx, projectId, parameter.getScopeModelId());
						final UUID wfWidgetId = resolveWorkflowWidgetId(tx, projectId, parameter.getWorkflow());
						final UUID summaryId = resolveWorkflowSummaryId(tx, projectId, parameter.getSummary());
						final UUID chartId = resolveChartId(tx, projectId, parameter.getChart());
						final UUID categoryId = resolveResourceCategoryId(tx, projectId, parameter.getCategory());

						tx.insertInto(MENU_LAYOUT_SECTION_WIDGET_PARAMETER)
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.PROJECT_ID, projectId)
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.MENU_ID, menuId)
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.MENU_SECTION_ID, sectionId)
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.MENU_WIDGET_ID, widgetId)
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.WIDGET_ORDER, widgetOrder)
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.TITLE, parameter.getTitle())
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.DISPLAY_ADD_RESPONSE, parameter.getDisplayAddResponse())
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.REMOVE_PROFILE_SELECTOR, parameter.getRemoveProfileSelector())
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.OVERDUE_TYPE, parameter.getOverdueType())
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.SPECIFIC_COLUMN_NAME, parameter.getSpecificColumnName())
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.SCOPE_MODEL_ID, scopeModelId)
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.WORKFLOW_WIDGET_ID, wfWidgetId)
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.WORKFLOW_SUMMARY_ID, summaryId)
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.CHART_ID, chartId)
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.CATEGORY_ID, categoryId)
							.onDuplicateKeyUpdate()
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.TITLE, parameter.getTitle())
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.DISPLAY_ADD_RESPONSE, parameter.getDisplayAddResponse())
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.REMOVE_PROFILE_SELECTOR, parameter.getRemoveProfileSelector())
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.OVERDUE_TYPE, parameter.getOverdueType())
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.SPECIFIC_COLUMN_NAME, parameter.getSpecificColumnName())
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.SCOPE_MODEL_ID, scopeModelId)
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.WORKFLOW_WIDGET_ID, wfWidgetId)
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.WORKFLOW_SUMMARY_ID, summaryId)
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.CHART_ID, chartId)
							.set(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.CATEGORY_ID, categoryId)
							.execute();
					}
					widgetOrder++;
				}
			}
		}
	}

	private static String nullSafe(final String s) {
		return s == null ? "" : s;
	}
}
