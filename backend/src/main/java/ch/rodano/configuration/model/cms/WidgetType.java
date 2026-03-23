package ch.rodano.configuration.model.cms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.rodano.configuration.model.common.Entity;

public enum WidgetType {
	CHART {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			return Map.of("graph", new CMSComponentParameter(String.class).setEntity(Entity.CHART));
		}
	},
	GENERAL_INFORMATIONS,
	LOCK_SUMMARY {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			return Map.of("scopeModelId", new CMSComponentParameter(String.class).setEntity(Entity.SCOPE_MODEL));
		}
	},
	RESOURCE {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			return Map.of("category", new CMSComponentParameter(String.class));
		}
	},
	SCOPE_OVERDUE {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			return Map.of(
				"OVERDUE_TYPE", new CMSComponentParameter(String.class),
				"SPECIFIC_COLUMN_NAME", new CMSComponentParameter(String.class)
			);
		}
	},
	WELCOME_TEXT,
	WORKFLOW {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			return Map.of("workflow", new CMSComponentParameter(String.class).setEntity(Entity.WORKFLOW));
		}
	},
	WORKFLOWS_SUMMARY {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			final Map<String, CMSComponentParameter> parameters = new HashMap<>();
			parameters.put("summary", new CMSComponentParameter(String.class).setEntity(Entity.WORKFLOW_SUMMARY));
			return parameters;
		}
	};

	public Map<String, CMSComponentParameter> getParameters() {
		return Collections.emptyMap();
	}
}
