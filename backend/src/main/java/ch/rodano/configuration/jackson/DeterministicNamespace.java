package ch.rodano.configuration.jackson;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface DeterministicNamespace {
	String value();

	String sourceProperty() default "id";
}
