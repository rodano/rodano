package ch.rodano.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.TemplateException;

import ch.rodano.api.configuration.interceptor.MustChangePasswordInterceptor;
import ch.rodano.api.configuration.interceptor.RequestContextInterceptor;
import ch.rodano.api.configuration.interceptor.TransactionCacheHandlerInterceptor;

@Profile({ "api", "test" })
@Configuration
@EnableWebMvc
public class WebConfigurer implements WebMvcConfigurer {
	private final ObjectMapper objectMapper;
	private final MustChangePasswordInterceptor mustChangePasswordInterceptor;
	private final TransactionCacheHandlerInterceptor transactionCacheHandlerInterceptor;
	private final RequestContextInterceptor requestContextInterceptor;

	private final Integer corePoolSize;
	private final Integer maxPoolSize;
	private final Integer poolQueueCapacity;
	private final String poolName;
	private final Boolean poolTimeoutActive;
	private final Integer poolTimeoutDuration; // In seconds

	public WebConfigurer(
		final ObjectMapper objectMapper,
		final MustChangePasswordInterceptor mustChangePasswordInterceptor,
		final TransactionCacheHandlerInterceptor transactionCacheHandlerInterceptor,
		final RequestContextInterceptor requestContextInterceptor,
		@Value("${rodano.controller.pool.core-size:-1}") final Integer corePoolSize,
		@Value("${rodano.controller.pool.max-size:40}") final Integer maxPoolSize,
		@Value("${rodano.controller.pool.queue-capacity:15}") final Integer poolQueueCapacity,
		@Value("${rodano.controller.pool.name:controller-pool}") final String poolName,
		@Value("${rodano.controller.pool.timeout:true}") final Boolean poolTimeoutActive,
		@Value("${rodano.controller.pool.timeout.duration:120}") final Integer poolTimeoutDuration
	) {
		this.objectMapper = objectMapper;
		this.mustChangePasswordInterceptor = mustChangePasswordInterceptor;
		this.transactionCacheHandlerInterceptor = transactionCacheHandlerInterceptor;
		this.requestContextInterceptor = requestContextInterceptor;
		this.corePoolSize = corePoolSize;
		this.maxPoolSize = maxPoolSize;
		this.poolQueueCapacity = poolQueueCapacity;
		this.poolName = poolName;
		this.poolTimeoutActive = poolTimeoutActive;
		this.poolTimeoutDuration = poolTimeoutDuration;
	}

	/**
	 * Add Spring MVC lifecycle interceptors for pre- and post-processing of
	 * controller method invocations. Interceptors can be registered to apply
	 * to all requests or be limited to a subset of URL patterns.
	 * <p><strong>Note</strong> that interceptors registered here only apply to
	 * controllers and not to resource handler requests. To intercept requests for
	 * static resources either declare a
	 * {@link MappedInterceptor MappedInterceptor}
	 * bean or switch to advanced configuration mode by extending
	 * {@link WebMvcConfigurationSupport
	 * WebMvcConfigurationSupport} and then override {@code resourceHandlerMapping}.
	 *
	 */
	@Override
	public void addInterceptors(final InterceptorRegistry registry) {
		registry.addInterceptor(mustChangePasswordInterceptor).excludePathPatterns("/auth/password/change", "/me", "/config/study", "/config/public-study");
		registry.addInterceptor(transactionCacheHandlerInterceptor);
		registry.addInterceptor(requestContextInterceptor);
	}

	/**
	 * A hook for extending or modifying the list of converters after it has been
	 * configured. This may be useful for example to allow default converters to
	 * be registered and then insert a custom converter through this method.
	 *
	 * @param converters the list of configured converters to extend.
	 * @since 4.1.3
	 */
	@Override
	public void extendMessageConverters(final List<HttpMessageConverter<?>> converters) {
		converters.stream().filter(converter -> converter instanceof MappingJackson2HttpMessageConverter).forEach(
			converter -> ((MappingJackson2HttpMessageConverter) converter).setObjectMapper(objectMapper)
		);
	}

	/**
	 * Configure asynchronous request handling options.
	 *
	 */
	@Override
	public void configureAsyncSupport(final AsyncSupportConfigurer configurer) {
		final var executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(corePoolSize > 0 ? corePoolSize : Runtime.getRuntime().availableProcessors());
		executor.setMaxPoolSize(maxPoolSize);
		executor.setQueueCapacity(poolQueueCapacity);
		executor.setThreadNamePrefix(poolName);
		executor.setAllowCoreThreadTimeOut(poolTimeoutActive);
		executor.setKeepAliveSeconds(poolTimeoutDuration);
		executor.initialize();

		configurer.setTaskExecutor(executor);
	}

	/**
	 * Configure the character encoding filter that should be used by default by the api
	 *
	 * @return The default character encoding filter
	 */
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public CharacterEncodingFilter characterEncodingFilter() {
		final var filter = new CharacterEncodingFilter();
		filter.setEncoding(StandardCharsets.UTF_8.name());
		filter.setForceEncoding(true);
		return filter;
	}

	@Bean
	public FreeMarkerConfigurer freemarkerConfig() throws IOException, TemplateException {
		//remove default FreeMarker default configuration
		//FreeMarker loader will be set each time it is used to be able to render files or plain text
		final FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
		final var configuration = freeMarkerConfigurer.createConfiguration();
		configuration.unsetTemplateLoader();
		freeMarkerConfigurer.setConfiguration(configuration);
		return freeMarkerConfigurer;
	}
}
