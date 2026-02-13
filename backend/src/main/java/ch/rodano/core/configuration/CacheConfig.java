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
			"fieldModelsByDatasetModel"
		);
	}
}
