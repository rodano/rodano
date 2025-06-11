package ch.rodano.configuration.model.cms;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;


@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class CMSWidget implements Node, Comparable<CMSWidget> {
	private static final long serialVersionUID = 503573635425875692L;

	private CMSSection section;
	protected String type;

	protected String textBefore;
	protected String textAfter;
	protected WidgetWidth width;

	protected Map<String, Object> parameters;

	protected ScopeCriterionRight requiredRight;
	protected String requiredFeature;

	public CMSWidget() {
		parameters = new TreeMap<>();
	}

	@JsonBackReference
	public final CMSSection getSection() {
		return section;
	}

	@JsonBackReference
	public final void setSection(final CMSSection section) {
		this.section = section;
	}

	public final String getType() {
		return type;
	}

	public final void setType(final String type) {
		this.type = type;
	}

	public final String getTextBefore() {
		return textBefore;
	}

	public final void setTextBefore(final String textBefore) {
		this.textBefore = textBefore;
	}

	public final String getTextAfter() {
		return textAfter;
	}

	public final void setTextAfter(final String textAfter) {
		this.textAfter = textAfter;
	}

	public final WidgetWidth getWidth() {
		return width;
	}

	public final void setWidth(final WidgetWidth width) {
		this.width = width;
	}

	public final Map<String, Object> getParameters() {
		return parameters;
	}

	public final void setParameters(final Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public final Object getParameter(final String key) {
		return parameters.get(key);
	}

	public final boolean isParameter(final String key) {
		return parameters.containsKey(key);
	}

	public final String getRequiredFeature() {
		return requiredFeature;
	}

	public final void setRequiredFeature(final String requiredFeature) {
		this.requiredFeature = requiredFeature;
	}

	public final ScopeCriterionRight getRequiredRight() {
		return requiredRight != null && requiredRight.isValid() ? requiredRight : null;
	}

	public final void setRequiredRight(final ScopeCriterionRight requiredRight) {
		this.requiredRight = requiredRight;
	}

	@Override
	public final Entity getEntity() {
		return Entity.CMS_WIDGET;
	}

	@Override
	public final int compareTo(final CMSWidget o) {
		if(equals(o)) {
			return 0;
		}

		if(section == null) {
			return 1;
		}

		if(o.section == null) {
			return -1;
		}

		if(!section.equals(o.section)) {
			return section.compareTo(o.section);
		}

		return section.getWidgets().indexOf(this) - section.getWidgets().indexOf(o);
	}

	@Override
	public boolean equals(final Object obj) {
		if(obj == null) {
			return false;
		}

		if(obj == this) {
			return true;
		}

		if(obj.getClass() != getClass()) {
			return false;
		}

		final var widget = (CMSWidget) obj;
		return new EqualsBuilder()
			.append(type, widget.type)
			.append(parameters, widget.parameters)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(type)
			.append(parameters)
			.hashCode();
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
