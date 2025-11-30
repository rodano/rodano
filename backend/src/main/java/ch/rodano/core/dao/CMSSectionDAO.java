package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.cms.CMSSection;
import ch.rodano.configuration.model.cms.ScopeCriterionRight;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.jooq.tables.records.MenuLayoutSectionRecord;

import static ch.rodano.core.model.jooq.tables.Feature.FEATURE;
import static ch.rodano.core.model.jooq.tables.FormModel.FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.MenuLayoutSection.MENU_LAYOUT_SECTION;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;

@Repository
public class CMSSectionDAO {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final CMSWidgetDAO cmsWidgetDAO;

	public CMSSectionDAO(final DSLContext dslContext, final MappingHelper mappingHelper, final CMSWidgetDAO cmsWidgetDAO) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.cmsWidgetDAO = cmsWidgetDAO;
	}

	public List<CMSSection> findByMenu(final UUID menuId) {
		return dslContext.selectFrom(MENU_LAYOUT_SECTION)
			.where(MENU_LAYOUT_SECTION.MENU_ID.eq(menuId))
			.orderBy(MENU_LAYOUT_SECTION.SORT_ORDER)
			.fetch(this::mapToModel);
	}

	private CMSSection mapToModel(final MenuLayoutSectionRecord record) {
		if(record == null) {
			return null;
		}

		final CMSSection model = new CMSSection();

		model.setMenuSectionId(record.getMenuSectionId());
		model.setId(record.getCode());

		model.setLabels(mappingHelper.parseJsonToMap(record.getLabel()));

		model.setWidgets(cmsWidgetDAO.findBySection(record.getMenuSectionId()));

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

	private String getFeatureCode(final UUID featureId) {
		return dslContext.select(FEATURE.CODE)
			.from(FEATURE)
			.where(FEATURE.FEATURE_ID.eq(featureId))
			.fetchOne(FEATURE.CODE);
	}
}
