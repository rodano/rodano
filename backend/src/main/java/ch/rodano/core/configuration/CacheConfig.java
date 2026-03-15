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
			"projects",
			"project",
			"draftVersion",
			"activeVersion",
			"projectVersions",
			"projectVersion",
			"configSnapshot",

			"scopeModels",
			"scopeModel",

			"datasetModels",
			"datasetModel",

			"eventModels",
			"eventModel",

			"eventGroups",
			"eventGroup",
			"eventGroupsByScopeModel",

			"fieldModels",
			"fieldModel",
			"fieldModelsByDatasetModel",

			"validators",
			"validator",

			"workflows",
			"workflow",
			"workflowStates",
			"workflowState",
			"workflowActions",
			"workflowAction",

			"profiles",
			"profile",

			"features",
			"feature",

			"privacy-policies",
			"privacy-policy",

			"resource-categories",
			"resource-category",

			"reports",
			"report",

			"charts",
			"chart",

			"formModels",
			"formModel",
			"layouts",
			"layout",

			"timelineGraphs",
			"timelineGraph",
			"timelineGraphSections",
			"timelineGraphSection",

			"workflowWidgets",
			"workflowWidget",

			"workflowSummaries",
			"workflowSummary"
		);
	}
}
