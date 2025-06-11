package ch.rodano.core.plugins.rules.scope;

import java.util.Comparator;
import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityAttribute;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.plugin.entity.EntityType;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ScopeAttributeEntityDefiner extends AbstractScopeEntityDefiner {
	protected final ScopeService scopeService;
	protected final ScopeRelationService scopeRelationService;
	protected final FieldDAOService fieldDAOService;

	public ScopeAttributeEntityDefiner(@Lazy final ScopeService scopeService, @Lazy final ScopeRelationService scopeRelationService, final FieldDAOService fieldDAOService) {
		this.scopeService = scopeService;
		this.scopeRelationService = scopeRelationService;
		this.fieldDAOService = fieldDAOService;
	}

	/**
	 * Get the related entity type
	 *
	 * @return An entity type
	 */
	@Override
	public EntityType getRelatedEntityType() {
		return EntityType.ATTRIBUTE;
	}

	/**
	 * Get all registered identifiable entity beans
	 *
	 * @return Registered identifiable entity beans
	 */
	@Override
	public List<IdentifiableEntity> getRegisteredBeans() {
		return List.of(
			new EntityAttribute() {
				@Override
				public String getValue(final Evaluable evaluable) {
					return ((Scope) evaluable).getId();
				}

				@Override
				public OperandType getType() {
					return OperandType.STRING;
				}

				@Override
				public String getId() {
					return "ID";
				}
			},
			new EntityAttribute() {
				@Override
				public String getValue(final Evaluable evaluable) {
					return ((Scope) evaluable).getCode();
				}

				@Override
				public OperandType getType() {
					return OperandType.STRING;
				}

				@Override
				public String getId() {
					return "CODE";
				}
			},
			new EntityAttribute() {
				@Override
				public String getValue(final Evaluable evaluable) {
					return ((Scope) evaluable).getScopeModelId();
				}

				@Override
				public OperandType getType() {
					return OperandType.STRING;
				}

				@Override
				public String getId() {
					return "MODEL";
				}
			},
			new EntityAttribute() {
				@Override
				public Boolean getValue(final Evaluable evaluable) {
					return ((Scope) evaluable).getDeleted();
				}

				@Override
				public OperandType getType() {
					return OperandType.BOOLEAN;
				}

				@Override
				public String getId() {
					return "REMOVED";
				}
			},
			new EntityAttribute() {
				@Override
				public Integer getValue(final Evaluable evaluable) {
					final var scope = (Scope) evaluable;
					final var defaultParent = scopeRelationService.getDefaultParent(scope);
					final var siblings = scopeRelationService.getAllChildren(defaultParent);
					return siblings.stream().filter(s -> !s.getDeleted()).sorted(Comparator.comparing(Scope::getCreationTime)).toList().indexOf(scope);
				}

				@Override
				public OperandType getType() {
					return OperandType.NUMBER;
				}

				@Override
				public String getId() {
					return "SCOPE_NUMBER_IN_PARENT";
				}
			},
			new EntityAttribute() {
				@Override
				public Integer getValue(final Evaluable evaluable) {
					final var scope = (Scope) evaluable;
					return scopeService.getLeafCount(scope);
				}

				@Override
				public OperandType getType() {
					return OperandType.NUMBER;
				}

				@Override
				public String getId() {
					return "NUMBER_OF_LEAF";
				}
			},
			new EntityAttribute() {
				@Override
				public Boolean getValue(final Evaluable evaluable) {
					final var scope = (Scope) evaluable;
					return fieldDAOService.doesScopeHaveFieldsWithAValue(scope.getPk());
				}

				@Override
				public OperandType getType() {
					return OperandType.BOOLEAN;
				}

				@Override
				public String getId() {
					return "CONTAINS_DATA";
				}
			}
		);
	}
}
