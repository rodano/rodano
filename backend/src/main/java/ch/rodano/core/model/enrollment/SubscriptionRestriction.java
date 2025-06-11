package ch.rodano.core.model.enrollment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class SubscriptionRestriction implements Serializable {
	private static final long serialVersionUID = -1749507570862464575L;

	private String model;
	private List<String> scopeIds;

	private List<String> values;

	public String getModel() {
		return model;
	}

	public void setModel(final String model) {
		this.model = model;
	}

	public List<String> getScopeIds() {
		if(scopeIds == null) {
			scopeIds = new ArrayList<>();
		}
		return scopeIds;
	}

	public void setScopeIds(final List<String> scopeIds) {
		this.scopeIds = scopeIds;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(final List<String> values) {
		this.values = values;
	}

	@Override
	public boolean equals(final Object o) {
		if(this == o) {
			return true;
		}
		if(o == null || getClass() != o.getClass()) {
			return false;
		}
		final var other = (SubscriptionRestriction) o;

		return new EqualsBuilder()
			.append(model, other.model)
			.append(scopeIds, other.scopeIds)
			.append(values, other.values)
			.isEquals();
	}
}
