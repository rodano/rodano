package ch.rodano.configuration.model.layout;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

public class ColumnHeader implements Node {
	@Serial
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
