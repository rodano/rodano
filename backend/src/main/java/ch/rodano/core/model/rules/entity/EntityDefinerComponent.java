package ch.rodano.core.model.rules.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This annotation is used to mask the complexity of using both annotation {@link Component} and {@link Order} when defining an entity definer in a study!
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Order
public @interface EntityDefinerComponent {
}
