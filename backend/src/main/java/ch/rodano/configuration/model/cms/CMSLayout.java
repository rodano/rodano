package ch.rodano.configuration.model.cms;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.exceptions.NoNodeException;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class CMSLayout implements Node {
	private static final long serialVersionUID = 7253496770406322390L;

	protected List<CMSSection> sections;

	public final void setSections(final List<CMSSection> sections) {
		this.sections = sections;
	}

	public final List<CMSSection> getSections() {
		return sections;
	}

	@JsonIgnore
	public final CMSSection getSection(final String sectionId) throws Exception {
		return sections.stream()
			.filter(s -> s.getId().equalsIgnoreCase(sectionId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.CMS_SECTION, sectionId));
	}

	@Override
	public final Entity getEntity() {
		return Entity.CMS_LAYOUT;
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
		final var layout = (CMSLayout) obj;
		return new EqualsBuilder()
			.append(sections, layout.sections)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(sections)
			.hashCode();
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		switch(entity) {
			case CMS_SECTION:
				return Collections.unmodifiableList(sections);
			default:
				return Collections.emptyList();
		}
	}
}
