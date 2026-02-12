package ch.rodano.configuration.model.node;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import ch.rodano.configuration.model.reports.WorkflowWidgetColumn;

@Tag("configuration")
public class NodeTest {
	@Test
	public void isStatic() {
		final var widget = new WorkflowWidgetColumn();
		assertFalse(widget.isStatic());
	}
}
