package ch.rodano.configuration.model.layout;

import java.util.Collection;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class ColumnHeader implements Node {
	private static final long serialVersionUID = -6496770259697992993L;

	private String cssCode;

	public final String getCssCode() {
		return cssCode;
	}

	public final void setCssCode(final String cssCode) {
		this.cssCode = cssCode;
	}

	@Override
	public final Entity getEntity() {
		return Entity.COLUM;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}

}
