package ch.rodano.core.configuration.openapi;

import java.util.Arrays;

import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import ch.rodano.configuration.model.common.Node;

@Profile("api")
@Configuration
public class OpenAPIConfig {

	@Autowired
	BuildProperties buildProperties;

	@Bean
	public OpenAPI customOpenAPI() {
		SpringDocUtils.getConfig()
			.addResponseTypeToIgnore(Node.class);

		//create dedicated types for enums https://springdoc.org/#how-can-i-apply-enumasref-true-to-all-enums
		//enums will get their own class in generated SDKs
		ModelResolver.enumsAsRef = true;

		//Swagger should use the same object mapper as the API, so API and OpenAPI specifications are in sync
		//unfortunately, springdoc does not provide the object mapper used in the API to Swagger
		//so we need to tweak the static mapper that is used by Swagger
		//its configuration must be in sync with the configuration in ch.rodano.api.configuration.jackson.JacksonConfigurer
		//this could be done using a custom model converter but it is easier to do it that way
		//Json31.mapper().configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, false);
		//Json31.mapper().configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false);
		//unfortunately, doing this will use the enum name for SecurityScheme.Type.HTTP, returning "HTTP" instead of "http"
		//this breaks the code generator
		//in the end, it's better not to override the toString method in enums, or use @JsonProperty
		//using @JsonProperty allows to restore the serialized value of an enum to its name

		return new OpenAPI()
			.components(
				new Components()
					.addSecuritySchemes(
						"bearer-token",
						new SecurityScheme()
							.type(SecurityScheme.Type.HTTP)
							.scheme("bearer")
					).addSecuritySchemes(
						"basic-auth",
						new SecurityScheme()
							.type(SecurityScheme.Type.HTTP)
							.scheme("basic")
					)
			)
			.info(
				new Info()
					.title("Rodano API")
					.version(buildProperties.getVersion())
					.license(new License().name("AGPLv3").url("https://www.gnu.org/licenses/agpl-3.0.en.html"))
			).addSecurityItem(
				new SecurityRequirement().addList("bearer-token", Arrays.asList("read", "write"))
			);
	}
}
