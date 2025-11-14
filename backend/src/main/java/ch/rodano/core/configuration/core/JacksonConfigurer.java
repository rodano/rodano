package ch.rodano.core.configuration.core;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.ConstructorDetector;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;

@Configuration
public class JacksonConfigurer {

	@Bean
	public JsonMapperBuilderCustomizer jacksonCustomizer() {
		return builder -> {
			builder.disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING);
			builder.disable(EnumFeature.READ_ENUMS_USING_TO_STRING);
			builder.enable(DateTimeFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
			builder.disable(MapperFeature.INFER_CREATOR_FROM_CONSTRUCTOR_PROPERTIES);
			builder.constructorDetector(ConstructorDetector.EXPLICIT_ONLY.withRequireAnnotation(true));
		};
	}
}
