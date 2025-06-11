package ch.rodano.configuration.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import ch.rodano.configuration.builder.StudyBuilder;
import ch.rodano.configuration.model.language.LanguageStatic;

@Tag("configuration")
public class EventTest {
	private static final String LANGUAGE_ID = LanguageStatic.en.name();

	private static final String EVENT_MODEL_FIRST_ID = "EVENT_FIRST";
	private static final String EVENT_MODEL_SECOND_ID = "EVENT_SECOND";
	private static final String EVENT_MODEL_THIRD_ID = "EVENT_THIRD";

	@Test
	@DisplayName("Create event models")
	public void createEventModels() {
		//Create a study
		final var study = StudyBuilder.buildSimpleStudy()
			.addScopeModel(StudyBuilder.SCOPE_MODEL_PATIENT_ID, StudyBuilder.SCOPE_MODEL_PATIENT_ID, null)
			.getStudy();

		//create a scope model
		final var scopeModel = study.getScopeModel(StudyBuilder.SCOPE_MODEL_PATIENT_ID);

		final var eventModel1 = new EventModel();
		eventModel1.setId(EVENT_MODEL_FIRST_ID);
		eventModel1.getShortname().put(LANGUAGE_ID, EVENT_MODEL_FIRST_ID);
		eventModel1.getDescription().put(LANGUAGE_ID, EVENT_MODEL_FIRST_ID);
		eventModel1.setDeadline(0);

		eventModel1.setScopeModel(scopeModel);
		scopeModel.getEventModels().add(eventModel1);

		final var eventModel2 = new EventModel();
		eventModel2.setId(EVENT_MODEL_SECOND_ID);
		eventModel2.getShortname().put(LANGUAGE_ID, EVENT_MODEL_SECOND_ID);
		eventModel2.getDescription().put(LANGUAGE_ID, EVENT_MODEL_SECOND_ID);
		eventModel2.setDeadline(7);
		eventModel2.setDeadlineUnit(ChronoUnit.DAYS);
		eventModel2.setDeadlineReferenceEventModelIds(Collections.singletonList(EVENT_MODEL_FIRST_ID));
		eventModel2.setDeadlineAggregationFunction(DateAggregationFunction.MAX);

		eventModel2.setScopeModel(scopeModel);
		scopeModel.getEventModels().add(eventModel2);

		final var eventModel3 = new EventModel();
		eventModel3.setId(EVENT_MODEL_THIRD_ID);
		eventModel3.getShortname().put(LANGUAGE_ID, EVENT_MODEL_THIRD_ID);
		eventModel3.getDescription().put(LANGUAGE_ID, EVENT_MODEL_THIRD_ID);
		eventModel3.setDeadline(7);
		eventModel3.setDeadlineUnit(ChronoUnit.DAYS);
		eventModel3.setDeadlineReferenceEventModelIds(Collections.singletonList(EVENT_MODEL_SECOND_ID));
		eventModel3.setDeadlineAggregationFunction(DateAggregationFunction.MAX);

		eventModel3.setScopeModel(scopeModel);
		scopeModel.getEventModels().add(eventModel3);

		assertThrows(Exception.class, () -> scopeModel.getEventModel("TOTO"), "No such event TOTO");
		assertEquals(EVENT_MODEL_SECOND_ID, scopeModel.getEventModel(EVENT_MODEL_SECOND_ID).getId());

		final var oneweek = Duration.ofDays(7);
		assertEquals(oneweek.toMillis(), eventModel2.getDeadlineInMilliSeconds());
		assertEquals(oneweek.toMillis(), eventModel3.getDeadlineInMilliSeconds());
	}
}

