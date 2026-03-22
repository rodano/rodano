package ch.rodano.core.configuration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager(
			"projects", "project", "draftVersion", "activeVersion", "projectVersions", "projectVersion",
			"configSnapshot",
			"scopeModels", "scopeModel", "scopeModelRights",
			"datasetModels", "datasetModel", "datasetModelRights",
			"eventModels", "eventModel", "eventModelRights",
			"eventGroups", "eventGroup", "eventGroupsByScopeModel",
			"fieldModels", "fieldModel", "fieldModelsByDatasetModel",
			"validators", "validator",
			"workflows", "workflow", "workflowStates", "workflowState", "workflowActions", "workflowAction", "workflowRights",
			"profiles", "profile", "profileRights",
			"features", "feature", "featureGrants",
			"privacy-policies", "privacy-policy",
			"resourceCategories", "resourceCategory", "resourceCategoryGrants",
			"reports", "report", "reportGrants",
			"charts", "chart",
			"formModels", "formModel", "layouts", "layout", "formModelRights",
			"timelineGraphs", "timelineGraph", "timelineGraphSections", "timelineGraphSection", "timelineGraphGrants",
			"workflowWidgets", "workflowWidget",
			"workflowSummaries", "workflowSummary",
			"ruleDefinitionProperties", "ruleDefinitionProperty",
			"ruleDefinitionActions", "ruleDefinitionAction",
			"crons", "cron",
			"menus", "menu", "menuGrants",
			"rules"
		);
	}
}
