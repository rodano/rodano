package ch.rodano.configuration.model.cms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.rodano.configuration.model.common.Entity;

public enum WidgetType {
	ACTIVITY_LOG {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			final Map<String, CMSComponentParameter> parameters = new HashMap<>();
			parameters.put("displayAddResponse", new CMSComponentParameter(Boolean.class));
			parameters.put("removeProfileSelector", new CMSComponentParameter(Boolean.class));
			return parameters;
		}
	},
	RESOURCE {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			final Map<String, CMSComponentParameter> parameters = new HashMap<>();
			parameters.put("category", new CMSComponentParameter(String.class));
			parameters.put("status", new CMSComponentParameter(String.class));
			return parameters;
		}
	},
	GENERAL_INFORMATIONS {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			return new HashMap<>();
		}
	},
	WELCOME_TEXT {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			return new HashMap<>();
		}
	},
	WORKFLOWS_SUMMARY {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			final Map<String, CMSComponentParameter> parameters = new HashMap<>();
			parameters.put("summary", new CMSComponentParameter(String.class));
			return parameters;
		}
	},
	WORKFLOWABLES_SUMMARY {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			final Map<String, CMSComponentParameter> parameters = new HashMap<>();
			parameters.put("summary", new CMSComponentParameter(String.class));
			return parameters;
		}
	},
	EVENTS_SUMMARY {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			final Map<String, CMSComponentParameter> parameters = new HashMap<>();
			parameters.put("SCOPE", new CMSComponentParameter(String.class));
			parameters.put("DEPTH", new CMSComponentParameter(Integer.class));
			parameters.put("EVENT_GROUP", new CMSComponentParameter(String[].class).setEntity(Entity.EVENT_GROUP));
			parameters.put("WORKFLOW", new CMSComponentParameter(String.class).setEntity(Entity.WORKFLOW));
			parameters.put("SCOPE_WIDTH", new CMSComponentParameter(Integer.class));
			parameters.put("VIEW_STATUS", new CMSComponentParameter(Boolean.class));
			parameters.put("VIEW_COMPLEMENTS", new CMSComponentParameter(Boolean.class));
			parameters.put("VIEW_GLOBAL", new CMSComponentParameter(Boolean.class));
			parameters.put("VIEW_EVENTS", new CMSComponentParameter(Boolean.class));
			parameters.put("VIEW_TOTAL", new CMSComponentParameter(Boolean.class));
			parameters.put("VIEW_DUE", new CMSComponentParameter(Boolean.class));
			parameters.put("USE_EXPECTED", new CMSComponentParameter(Boolean.class));
			parameters.put("CUMULATIVE", new CMSComponentParameter(Map.class));
			parameters.put("TOTAL_LABEL", new CMSComponentParameter(Map.class));
			parameters.put("TOTAL_LEGEND", new CMSComponentParameter(Map.class));
			parameters.put("INTERVENTIONAL_RESPECT_INTERVAL", new CMSComponentParameter(String[].class));
			parameters.put("SCOPES_URL", new CMSComponentParameter(String.class));
			parameters.put("DISPLAY_LABEL", new CMSComponentParameter(Boolean.class));
			parameters.put("DISPLAY_PERCENT", new CMSComponentParameter(Boolean.class));
			parameters.put("DISPLAY_TARGET", new CMSComponentParameter(Boolean.class));
			parameters.put("ALLOW_EXPORT", new CMSComponentParameter(Boolean.class));
			parameters.put("ALLOW_EXPORT_BY_VISIT", new CMSComponentParameter(Boolean.class));
			parameters.put("VIEW_COMPLEMENTS_LABEL", new CMSComponentParameter(Boolean.class));
			parameters.put("DISPLAY_ICON", new CMSComponentParameter(Boolean.class));
			parameters.put("VIEW_LEGEND", new CMSComponentParameter(Boolean.class));
			return parameters;
		}
	},
	EVENT_SUMMARY {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			final Map<String, CMSComponentParameter> parameters = new HashMap<>();
			parameters.put("SCOPE", new CMSComponentParameter(String.class));
			parameters.put("DEPTH", new CMSComponentParameter(Integer.class));
			parameters.put("EVENT_GROUP", new CMSComponentParameter(String[].class).setEntity(Entity.EVENT_GROUP));
			parameters.put("GLOBAL_LABEL", new CMSComponentParameter(Map.class));
			parameters.put("DISPLAY_LABEL", new CMSComponentParameter(Boolean.class));
			parameters.put("VIEW_COMPLEMENTS", new CMSComponentParameter(Boolean.class));
			parameters.put("INTERVENTIONAL_RESPECT_INTERVAL", new CMSComponentParameter(Boolean.class));
			parameters.put("CUMULATIVE", new CMSComponentParameter(Map.class));
			return parameters;
		}
	},
	ENROLLMENT_HISTORY {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			return new HashMap<>();
		}
	},
	SCHEDULING_SUMMARY {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			final Map<String, CMSComponentParameter> parameters = new HashMap<>();
			parameters.put("SCOPE", new CMSComponentParameter(String.class));
			parameters.put("EXCLUDE_VISITS", new CMSComponentParameter(String.class));
			parameters.put("EVENT_GROUP", new CMSComponentParameter(String[].class).setEntity(Entity.EVENT_GROUP));
			parameters.put("WORKFLOW", new CMSComponentParameter(String.class).setEntity(Entity.WORKFLOW));
			parameters.put("VIEW_COMPLEMENTS", new CMSComponentParameter(Boolean.class));
			parameters.put("VIEW_GLOBAL", new CMSComponentParameter(Boolean.class));
			parameters.put("VIEW_TOTAL", new CMSComponentParameter(Boolean.class));
			parameters.put("VIEW_DUE", new CMSComponentParameter(Boolean.class));
			parameters.put("INTERVENTIONAL_RESPECT_INTERVAL", new CMSComponentParameter(String.class));
			parameters.put("USE_EXPECTED", new CMSComponentParameter(Boolean.class));
			parameters.put("CUMULATIVE", new CMSComponentParameter(Map.class));
			parameters.put("TOTAL_LABEL", new CMSComponentParameter(Map.class));
			parameters.put("TOTAL_LEGEND", new CMSComponentParameter(Map.class));
			parameters.put("DISPLAY_LABEL", new CMSComponentParameter(Boolean.class));
			parameters.put("DISPLAY_PERCENT", new CMSComponentParameter(Boolean.class));
			parameters.put("ALLOW_EXPORT", new CMSComponentParameter(Boolean.class));
			parameters.put("ALLOW_EXPORT_BY_VISIT", new CMSComponentParameter(Boolean.class));
			parameters.put("VIEW_COMPLEMENTS_LABEL", new CMSComponentParameter(Boolean.class));
			parameters.put("DISPLAY_ICON", new CMSComponentParameter(Boolean.class));
			parameters.put("VIEW_LEGEND", new CMSComponentParameter(Boolean.class));
			return parameters;
		}
	},
	FILE,
	WORKFLOW_JS {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			final Map<String, CMSComponentParameter> parameters = new HashMap<>();
			parameters.put("workflow", new CMSComponentParameter(String.class).setEntity(Entity.WORKFLOW));
			return parameters;
		}
	},
	WORKFLOW {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			final Map<String, CMSComponentParameter> parameters = new HashMap<>();
			parameters.put("workflow", new CMSComponentParameter(String.class).setEntity(Entity.WORKFLOW));
			return parameters;
		}

		@Override
		public boolean isUnicByLayout() {
			return true;
		}
	},
	VISITS_DUE_FOR_ACTIVE_CENTERS {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			final Map<String, CMSComponentParameter> parameters = new HashMap<>();
			parameters.put("workflow", new CMSComponentParameter(String.class).setEntity(Entity.WORKFLOW));
			return parameters;
		}
	},
	VISIT_DUE_INTERVENTIONNAL_UNKNOWN {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			final Map<String, CMSComponentParameter> parameters = new HashMap<>();
			parameters.put("events", new CMSComponentParameter(String[].class).setEntity(Entity.EVENT_MODEL));
			parameters.put("width", new CMSComponentParameter(Integer.class));
			return parameters;
		}
	},
	EVENT,
	REPORT,
	TIMELINE,
	HIGHCHART {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			final Map<String, CMSComponentParameter> parameters = new HashMap<>();
			parameters.put("graph", new CMSComponentParameter(Boolean.class));
			parameters.put("stats", new CMSComponentParameter(Boolean.class));
			parameters.put("display", new CMSComponentParameter(String.class));
			return parameters;
		}
	},
	CONTACTS {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			final Map<String, CMSComponentParameter> parameters = new HashMap<>();
			parameters.put("config", new CMSComponentParameter(String.class).setFile(true));
			return parameters;
		}
	},
	PAYMENT_BATCH_MANAGEMENT {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			final Map<String, CMSComponentParameter> parameters = new HashMap<>();
			parameters.put("plan", new CMSComponentParameter(String.class).setEntity(Entity.PAYMENT_PLAN));
			return parameters;
		}
	},
	PAYMENT_MANAGEMENT {
		@Override
		public Map<String, CMSComponentParameter> getParameters() {
			final Map<String, CMSComponentParameter> parameters = new HashMap<>();
			parameters.put("plan", new CMSComponentParameter(String.class).setEntity(Entity.PAYMENT_PLAN));
			parameters.put("SCOPE_FILTER", new CMSComponentParameter(String.class));
			return parameters;
		}
	},
	SCOPE_EDITION,
	PARAMETER_SELECTOR;

	public boolean isUnicByLayout() {
		return false;
	}

	public Map<String, CMSComponentParameter> getParameters() {
		return Collections.emptyMap();
	}
}
