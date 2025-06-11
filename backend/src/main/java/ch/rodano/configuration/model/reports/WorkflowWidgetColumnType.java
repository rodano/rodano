package ch.rodano.configuration.model.reports;

import ch.rodano.configuration.model.rules.OperandType;

public enum WorkflowWidgetColumnType {
	WORKFLOW_LABEL {
		@Override
		public OperandType getType() {
			return OperandType.STRING;
		}

		@Override
		public int getDefaultWidth() {
			return 80;
		}
	},
	WORKFLOW_TRIGGER_MESSAGE {
		@Override
		public OperandType getType() {
			return OperandType.STRING;
		}

		@Override
		public int getDefaultWidth() {
			return 80;
		}
	},
	STATUS_LABEL {
		@Override
		public OperandType getType() {
			return OperandType.STRING;
		}

		@Override
		public int getDefaultWidth() {
			return 80;
		}
	},
	STATUS_DATE {
		@Override
		public OperandType getType() {
			return OperandType.DATE;
		}

		@Override
		public int getDefaultWidth() {
			return 80;
		}
	},
	PARENT_SCOPE_CODE {
		@Override
		public OperandType getType() {
			return OperandType.STRING;
		}
	},
	SCOPE_CODE {
		@Override
		public OperandType getType() {
			return OperandType.STRING;
		}
	},
	EVENT_LABEL {
		@Override
		public OperandType getType() {
			return OperandType.STRING;
		}
	},
	EVENT_DATE {
		@Override
		public OperandType getType() {
			return OperandType.DATE;
		}
	},
	FORM_LABEL {
		@Override
		public OperandType getType() {
			return OperandType.STRING;
		}
	},
	FORM_DATE {
		@Override
		public OperandType getType() {
			return OperandType.DATE;
		}
	},
	FIELD_LABEL {
		@Override
		public OperandType getType() {
			return OperandType.STRING;
		}
	},
	FIELD_DATE {
		@Override
		public OperandType getType() {
			return OperandType.DATE;
		}
	};

	public abstract OperandType getType();

	public int getDefaultWidth() {
		return 100;
	}
}
