package ch.rodano.configuration.model.entity;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import ch.rodano.configuration.model.common.Entity;

@Tag("node")
public class EntityTest {
	@Test
	@DisplayName("Test entities")
	public void testEntities() {
		assertAll(
			() -> assertTrue(Entity.STUDY.getChildrenEmbedded().contains(Entity.VALIDATOR)),
			() -> assertTrue(Entity.STUDY.getChildrenEmbedded().contains(Entity.DATASET_MODEL)),

			() -> assertTrue(Entity.LANGUAGE.getChildrenEmbedded().isEmpty()),
			() -> assertTrue(Entity.LANGUAGE.getChildren().isEmpty()),

			() -> assertTrue(Entity.FEATURE.getChildrenEmbedded().isEmpty()),
			() -> assertTrue(Entity.FEATURE.getChildren().isEmpty()),

			() -> assertTrue(Entity.PROFILE.getChildrenEmbedded().isEmpty()),
			() -> assertTrue(Entity.PROFILE.getChildren().isEmpty()),

			() -> assertTrue(Entity.SCOPE_MODEL.getChildrenEmbedded().isEmpty()),
			() -> assertEquals(1, Entity.SCOPE_MODEL.getChildren().size()),

			() -> assertEquals(1, Entity.DATASET_MODEL.getChildrenEmbedded().size()),
			() -> assertTrue(Entity.DATASET_MODEL.getChildrenEmbedded().contains(Entity.FIELD_MODEL)),

			() -> assertTrue(Entity.FIELD_MODEL.getChildrenEmbedded().isEmpty()),
			() -> assertEquals(1, Entity.FIELD_MODEL.getChildren().size()),
			() -> assertTrue(Entity.FIELD_MODEL.getChildren().contains(Entity.VALIDATOR)),

			() -> assertTrue(Entity.VALIDATOR.getChildrenEmbedded().isEmpty()),
			() -> assertTrue(Entity.VALIDATOR.getChildren().isEmpty()),

			() -> assertEquals(1, Entity.FORM_MODEL.getChildrenEmbedded().size()),
			() -> assertEquals(1, Entity.FORM_MODEL.getChildren().size()),

			() -> assertTrue(Entity.LAYOUT.getChildrenEmbedded().isEmpty()),
			() -> assertTrue(Entity.LAYOUT.getChildren().isEmpty())
			);
	}
}

