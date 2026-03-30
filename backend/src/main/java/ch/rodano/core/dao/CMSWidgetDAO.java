package ch.rodano.core.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.cms.CMSWidget;
import ch.rodano.configuration.model.cms.ScopeCriterionRight;
import ch.rodano.configuration.model.cms.WidgetWidth;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.jooq.tables.records.MenuLayoutSectionWidgetRecord;

import static ch.rodano.core.model.jooq.tables.Feature.FEATURE;
import static ch.rodano.core.model.jooq.tables.FormModel.FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.MenuLayoutSectionWidget.MENU_LAYOUT_SECTION_WIDGET;
import static ch.rodano.core.model.jooq.tables.MenuLayoutSectionWidgetParameter.MENU_LAYOUT_SECTION_WIDGET_PARAMETER;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;

@Repository
public class CMSWidgetDAO {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;

	public CMSWidgetDAO(final DSLContext dslContext, final MappingHelper mappingHelper) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
	}

	public List<CMSWidget> findBySection(final UUID sectionId) {
		return dslContext.selectFrom(MENU_LAYOUT_SECTION_WIDGET)
			.where(MENU_LAYOUT_SECTION_WIDGET.MENU_SECTION_ID.eq(sectionId))
			.orderBy(MENU_LAYOUT_SECTION_WIDGET.WIDGET_ORDER)
			.fetch(this::mapToModel);
	}

	private CMSWidget mapToModel(final MenuLayoutSectionWidgetRecord record) {
		if(record == null) {
			return null;
		}

		final CMSWidget model = new CMSWidget();

		model.setMenuWidgetId(record.getMenuWidgetId());

		model.setType(record.getType());

		model.setWidth(mappingHelper.parseEnum(WidgetWidth.class, record.getWidth(), "width"));

		if(record.getTextBefore() != null && !record.getTextBefore().isBlank()) {
			model.setTextBefore(record.getTextBefore());
		}

		model.setParameters(loadWidgetParameters(record.getMenuWidgetId()));

		if(record.getRequiredFeatureId() != null) {
			model.setRequiredFeature(getFeatureCode(record.getRequiredFeatureId()));
		}

		if(record.getRightEntity() != null && record.getRightValue() != null) {
			final ScopeCriterionRight scopeRight = buildScopeCriterionRight(
				record.getRightEntity(),
				record.getRightValue(),
				record.getRightTargetId()
			);
			if(scopeRight != null && scopeRight.isValid()) {
				model.setRequiredRight(scopeRight);
			}
		}

		return model;
	}

	private ScopeCriterionRight buildScopeCriterionRight(final String rightEntityStr, final String rightValue, final UUID rightTargetId) {
		try {
			final ScopeCriterionRight scopeRight = new ScopeCriterionRight();

			final Entity rightEntity = Entity.valueOf(rightEntityStr);
			scopeRight.setRightEntity(rightEntity);

			final Rights right = Rights.valueOf(rightValue);
			scopeRight.setRight(right);

			if(rightTargetId != null) {
				final String targetCode = getEntityCode(rightEntity, rightTargetId);
				scopeRight.setId(targetCode);
			}

			return scopeRight;
		}
		catch(IllegalArgumentException e) {
			return null;
		}
	}

	private String getEntityCode(final Entity entity, final UUID entityId) {
		return switch(entity) {
			case SCOPE_MODEL -> dslContext.select(SCOPE_MODEL.CODE)
				.from(SCOPE_MODEL)
				.where(SCOPE_MODEL.SCOPE_MODEL_ID.eq(entityId))
				.fetchOne(SCOPE_MODEL.CODE);
			case FORM_MODEL -> dslContext.select(FORM_MODEL.CODE)
				.from(FORM_MODEL)
				.where(FORM_MODEL.FORM_MODEL_ID.eq(entityId))
				.fetchOne(FORM_MODEL.CODE);
			default -> null;
		};
	}

	private Map<String, Object> loadWidgetParameters(final UUID widgetId) {
		final var paramRecords = dslContext
			.selectFrom(MENU_LAYOUT_SECTION_WIDGET_PARAMETER)
			.where(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.MENU_WIDGET_ID.eq(widgetId))
			.orderBy(MENU_LAYOUT_SECTION_WIDGET_PARAMETER.WIDGET_ORDER)
			.fetch();

		final Map<String, Object> parameters = new HashMap<>();

		for(var paramRecord : paramRecords) {
			addParameterIfNotNull(parameters, "scopeModelId", paramRecord.getScopeModelId());
			addParameterIfNotNull(parameters, "workflow", paramRecord.getWorkflowWidgetId());
			addParameterIfNotNull(parameters, "summary", paramRecord.getWorkflowSummaryId());
			addParameterIfNotNull(parameters, "chart", paramRecord.getChartId());
			addParameterIfNotNull(parameters, "category", paramRecord.getCategoryId());
			addParameterIfNotNull(parameters, "title", paramRecord.getTitle());
		}

		return parameters;
	}

	private void addParameterIfNotNull(final Map<String, Object> parameters, final String key, final Object value) {
		if(value != null) {
			parameters.put(key, value);
		}
	}

	private String getFeatureCode(final UUID featureId) {
		return dslContext.select(FEATURE.CODE)
			.from(FEATURE)
			.where(FEATURE.FEATURE_ID.eq(featureId))
			.fetchOne(FEATURE.CODE);
	}

	private String getScopeModelCode(final UUID scopeModelId) {
		return dslContext.select(SCOPE_MODEL.CODE)
			.from(SCOPE_MODEL)
			.where(SCOPE_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetchOne(SCOPE_MODEL.CODE);
	}
}
