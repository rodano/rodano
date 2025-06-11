package ch.rodano.configuration.model.node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.rodano.configuration.model.cron.Cron;
import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.model.reports.WorkflowWidgetColumn;

@Tag("configuration")
public class NodeTest {
	@Test
	public void isStatic() {
		final var widget = new WorkflowWidgetColumn();
		assertFalse(widget.isStatic());
	}

	@Test
	public void serializeProperly() throws JsonProcessingException {
		final var cron = new Cron();
		cron.setId("TEST");
		cron.getDescription().put(LanguageStatic.en.name(), "Test");
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		assertEquals("{\"id\":\"TEST\",\"description\":{\"en\":\"Test\"},\"rules\":[],\"className\":\"Cron\"}", mapper.writeValueAsString(cron));

		mapper.readValue("{\"id\":\"TEST\",\"description\":{\"en\":\"Test\"},\"rules\":[],\"className\":\"Cron\"}", Cron.class);
	}
}

