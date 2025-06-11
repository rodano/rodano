package ch.rodano.core.plugins;

import java.util.List;

import org.springframework.context.annotation.Lazy;

import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityAttribute;
import ch.rodano.core.model.rules.entity.EntityDefinerComponent;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.plugins.rules.scope.ScopeAttributeEntityDefiner;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.dao.field.FieldDAOService;

@EntityDefinerComponent
public final class TestScopeAttributeEntityDefiner extends ScopeAttributeEntityDefiner {

	public TestScopeAttributeEntityDefiner(@Lazy final ScopeService scopeService, @Lazy final ScopeRelationService scopeRelationService, final FieldDAOService fieldDAOService) {
		super(scopeService, scopeRelationService, fieldDAOService);
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
					return "CODE OVERRIDEN BY PLUGIN";
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
				public Object getValue(final Evaluable evaluable) {
					return "";
				}

				@Override
				public OperandType getType() {
					return OperandType.STRING;
				}

				@Override
				public String getId() {
					return "TOTO";
				}
			}
		);
	}
}
