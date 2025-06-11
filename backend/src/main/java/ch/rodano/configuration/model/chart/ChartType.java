package ch.rodano.configuration.model.chart;


public enum ChartType {
	ENROLLMENT_BY_SCOPE {
		@Override
		public String toString() {
			return "Enrollment by scope";
		}
	},
	ENROLLMENT {
		@Override
		public String toString() {
			return "Enrollment";
		}
	},
	STATISTICS {
		@Override
		public String toString() {
			return "Statistics";
		}
	},
	WORKFLOW_STATUS {
		@Override
		public String toString() {
			return "Workflow status";
		}
	}
}
