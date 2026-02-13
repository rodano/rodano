package ch.rodano.core.model.enrollment;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import ch.rodano.configuration.model.study.Study;
import ch.rodano.core.model.scope.EnrollmentType;
import ch.rodano.core.model.scope.FieldModelCriterion;

public class EnrollmentModel implements Serializable {
	@Serial
	private static final long serialVersionUID = 8815322291100222508L;

	private final Logger logger = LoggerFactory.getLogger(Study.class);

	private boolean system;
	private EnrollmentType type;
	private List<String> scopesContainerIds = new ArrayList<>();
	private List<FieldModelCriterion> criteria = new ArrayList<>();

	public EnrollmentModel() {}

	public final boolean isSystem() {
		return system;
	}

	public final void setSystem(final boolean system) {
		this.system = system;
	}

	public final List<String> getScopesContainerIds() {
		return scopesContainerIds;
	}

	public final void setScopesContainerIds(final List<String> scopesContainerIds) {
		this.scopesContainerIds = scopesContainerIds;
	}

	public final EnrollmentType getType() {
		return type;
	}

	public final void setType(final EnrollmentType type) {
		this.type = type;
	}

	public final List<FieldModelCriterion> getCriteria() {
		return criteria;
	}

	public final void setCriteria(final List<FieldModelCriterion> criteria) {
		this.criteria = criteria;
	}

	@JsonAnySetter
	public void setAnySetter(final String key, final Object value) {
		logger.error("Unknown property {} (value {}) in class {}", key, value, getClass().getSimpleName());
	}
}
