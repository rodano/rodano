package ch.rodano.core.model.enrollment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import ch.rodano.core.model.scope.EnrollmentType;
import ch.rodano.core.model.scope.FieldModelCriterion;

public class EnrollmentModel implements Serializable {
	private static final long serialVersionUID = 8815322291100222508L;

	private boolean draft;
	private boolean system;
	private EnrollmentType type;
	private Set<String> scopesContainerIds = new TreeSet<>();
	private List<FieldModelCriterion> criteria = new ArrayList<>();

	public EnrollmentModel() {
	}

	public final boolean isSystem() {
		return system;
	}

	public final void setSystem(final boolean system) {
		this.system = system;
	}

	public final Set<String> getScopesContainerIds() {
		return scopesContainerIds;
	}

	public final void setScopesContainerIds(final Set<String> scopesContainerIds) {
		this.scopesContainerIds = scopesContainerIds;
	}

	public final boolean isDraft() {
		return draft;
	}

	public void setDraft(final boolean draft) {
		this.draft = draft;
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
		final var message = new StringBuilder("EnrollmentModel - Unknown property : ");
		message.append(key);
		message.append(" - ");
		message.append(value);
		System.err.println(message);
	}
}
