package ch.rodano.configuration.model.workflow;

import ch.rodano.configuration.model.common.Entity;

public enum WorkflowableEntity {
	SCOPE {
		@Override
		public Entity getConfigurationEntity() {
			return Entity.SCOPE_MODEL;
		}
	},
	EVENT {
		@Override
		public Entity getConfigurationEntity() {
			return Entity.EVENT_MODEL;
		}
	},
	FORM {
		@Override
		public Entity getConfigurationEntity() {
			return Entity.FORM_MODEL;
		}
	},
	FIELD {
		@Override
		public Entity getConfigurationEntity() {
			return Entity.FIELD_MODEL;
		}
	};

	public abstract Entity getConfigurationEntity();
}
