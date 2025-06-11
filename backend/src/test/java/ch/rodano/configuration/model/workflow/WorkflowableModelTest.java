package ch.rodano.configuration.model.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import ch.rodano.configuration.builder.StudyBuilder;

@Tag("configuration")
public class WorkflowableModelTest {
	@Test
	@DisplayName("Get important states")
	public void getImportantStates() {
		final var state1 = new WorkflowState();
		state1.setId("1");
		state1.setImportant(false);

		final var state2 = new WorkflowState();
		state2.setId("2");
		state2.setImportant(false);

		final var state3 = new WorkflowState();
		state3.setId("3");
		state3.setImportant(true);

		final var workflow = new Workflow();
		workflow.setId("100");
		workflow.setStates(Arrays.asList(state1, state2, state3));

		//Create study
		final var study = StudyBuilder.buildSimpleStudy().getStudy();

		workflow.setStudy(study);
		study.setWorkflows(new TreeSet<>(Collections.singletonList(workflow)));

		final List<WorkflowState> expected = new ArrayList<>();
		expected.add(state3);

		assertEquals(1, workflow.getStatesImportant().size());
		assertEquals(expected, workflow.getStatesImportant());
	}

	@Test
	@DisplayName("Retrieve initial state")
	public void retrieveInitialState() {
		final var state1 = new WorkflowState();
		state1.setId("1");

		final var state2 = new WorkflowState();
		state2.setId("2");

		final var workflow = new Workflow();
		workflow.setId("100");
		workflow.setStates(Arrays.asList(state1, state2));

		workflow.setInitialStateId("1");
		assertEquals(workflow.getInitialState().getId(), "1");
		workflow.setInitialStateId("2");
		assertEquals(workflow.getInitialState().getId(), "2");
	}
}

