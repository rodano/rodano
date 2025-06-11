package ch.rodano.configuration.model.document;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import ch.rodano.configuration.builder.StudyBuilder;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.form.FormModel;
import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.model.layout.Cell;
import ch.rodano.configuration.model.layout.ColumnHeader;
import ch.rodano.configuration.model.layout.Layout;
import ch.rodano.configuration.model.layout.Line;

@Tag("configuration")
public class DatasetModelTest {
	private static final String LANGUAGE_ID = LanguageStatic.en.name();

	private static final String DATSET_MODEL_ID = "DATASET_MODEL";
	private static final String FIELD_MODEL_DATE_ID = "FIELD_MODEL_DATE";
	private static final String FIELD_MODEL_NUMBER_ID = "FIELD_MODEL_NUMBER";
	private static final String FIELD_MODEL_PLUGIN_ID = "FIELD_MODEL_PLUGIN";

	@Test
	@DisplayName("Create dataset model")
	public void createDatasetModel() {
		//Create dataset model
		final var datasetModel = StudyBuilder.buildSimpleDatasetModel(DATSET_MODEL_ID);

		//Create study and add dataset model
		final var study = StudyBuilder.buildSimpleStudy()
			.addDatasetModel(datasetModel)
			.getStudy();

		assertAll("Create dataset model",
			() -> assertEquals(1, study.getDatasetModels().size()),
			() -> assertEquals(0, datasetModel.getFieldModels().size())
			);
	}

	@Test
	@DisplayName("Create date field model")
	public void createDateFieldModel() {
		//Create field model
		final var fieldModel = new FieldModel();
		fieldModel.setId(FIELD_MODEL_DATE_ID);
		fieldModel.setType(FieldModelType.DATE);
		fieldModel.setWithYears(true);
		fieldModel.setWithMonths(true);
		fieldModel.setReadOnly(false);
		fieldModel.setExportable(false);
		fieldModel.setExportOrder(1);

		//Create dataset model and add the field model to it
		final var datasetModel = StudyBuilder.buildSimpleDatasetModel(DATSET_MODEL_ID, fieldModel);

		//Create study and add the dataset model to it
		final var study = StudyBuilder.buildSimpleStudy()
			.addDatasetModel(datasetModel)
			.getStudy();

		assertAll("Get document field models",
			() -> assertEquals(1, study.getDatasetModel(DATSET_MODEL_ID).getFieldModels().size()),
			() -> assertEquals(FieldModelType.DATE, study.getDatasetModel(DATSET_MODEL_ID).getFieldModel(FIELD_MODEL_DATE_ID).getType()),
			() -> assertTrue(study.getDatasetModel(DATSET_MODEL_ID).getFieldModel(FIELD_MODEL_DATE_ID).isWithYears()),
			() -> assertFalse(study.getDatasetModel(DATSET_MODEL_ID).getFieldModel(FIELD_MODEL_DATE_ID).isWithDays()),
			() -> assertEquals("datetime", study.getDatasetModel(DATSET_MODEL_ID).getFieldModel(FIELD_MODEL_DATE_ID).getSQLType())
			);

		//export
		assertEquals(0, datasetModel.getFieldModelsExportables().size());
		fieldModel.setExportable(true);
		assertEquals(1, datasetModel.getFieldModelsExportables().size());
	}

	@Test
	@DisplayName("Create number field model")
	public void createNumberFieldModel() {
		//Create first field model
		final var fieldModel1 = StudyBuilder.buildSimpleFieldModel(FIELD_MODEL_DATE_ID, FieldModelType.DATE);

		//Create second field model
		final var fieldModel2 = StudyBuilder.buildSimpleFieldModel(FIELD_MODEL_NUMBER_ID, FieldModelType.NUMBER, false);

		//Create dataset model and add the field models
		final var datasetModel = StudyBuilder.buildSimpleDatasetModel(DATSET_MODEL_ID, fieldModel1, fieldModel2);

		//Create study and add document
		final var study = StudyBuilder.buildSimpleStudy()
			.addDatasetModel(datasetModel)
			.getStudy();

		assertAll("Create number field model",
			() -> assertEquals(2, datasetModel.getFieldModels().size()),
			() -> assertEquals("double", study.getDatasetModel(DATSET_MODEL_ID).getFieldModel(FIELD_MODEL_NUMBER_ID).getSQLType())
			);
	}

	@Test
	@DisplayName("Create plugins")
	public void createPlugins() {
		//Create field model
		final var fieldModel = StudyBuilder.buildSimpleFieldModel(FIELD_MODEL_PLUGIN_ID, FieldModelType.STRING, false);
		fieldModel.setPlugin(true);

		//Create dataset model and add field model
		final var document = StudyBuilder.buildSimpleDatasetModel(DATSET_MODEL_ID, fieldModel);

		//Create study and add dataset model
		final var study = StudyBuilder.buildSimpleStudy()
			.addDatasetModel(document)
			.getStudy();

		assertAll("Create plugins",
			() -> assertEquals(1, document.getFieldModels().size()),
			() -> assertEquals("varchar(400)", study.getDatasetModel(DATSET_MODEL_ID).getFieldModel(FIELD_MODEL_PLUGIN_ID).getSQLType())
			);
	}

	@Test
	@DisplayName("Create form model")
	public void createFormModel() {
		//Create field models
		final var fieldModel1 = StudyBuilder.buildSimpleFieldModel(FIELD_MODEL_DATE_ID, FieldModelType.DATE);
		final var fieldModel2 = StudyBuilder.buildSimpleFieldModel(FIELD_MODEL_NUMBER_ID, FieldModelType.NUMBER);

		//Create dataset model and add field model
		final var datasetModel = StudyBuilder.buildSimpleDatasetModel(DATSET_MODEL_ID, fieldModel1, fieldModel2);

		//Create study
		final var study = StudyBuilder.buildSimpleStudy()
			.addDatasetModel(datasetModel)
			.getStudy();

		final var formModel = new FormModel();
		formModel.setId("FIRST_PAGE");
		formModel.getShortname().put(LANGUAGE_ID, "First page");

		assertEquals("First page", formModel.getLocalizedShortname(LANGUAGE_ID));

		//add form model in study
		formModel.setStudy(study);
		study.getFormModels().add(formModel);

		assertNotNull(study.getFormModel("FIRST_PAGE"));

		//layout
		final var layout = new Layout();
		layout.setFormModel(formModel);
		layout.setId("LAYOUT");
		layout.getDescription().put(LANGUAGE_ID, "Layout 1");
		formModel.getLayouts().add(layout);

		assertEquals(1, formModel.getLayouts().size());

		assertEquals(layout, formModel.getLayout("LAYOUT"));
		try {
			assertEquals(layout, formModel.getLayout("LAYOUT_1"));
			fail("Layout LAYOUT_1 should not exist");
		}
		catch(final Exception ex2) {
			//success
		}

		final var header1 = new ColumnHeader();

		final var header2 = new ColumnHeader();

		layout.getColumns().add(header1);
		layout.getColumns().add(header2);

		assertEquals(2, layout.getColumns().size());
		assertNull(layout.getCssCode());

		final var line = new Line();
		line.setLayout(layout);
		layout.getLines().add(line);

		assertEquals(1, layout.getLines().size());

		final var cell1 = new Cell();
		cell1.setId(FIELD_MODEL_DATE_ID);
		cell1.setFieldModelId(FIELD_MODEL_DATE_ID);
		cell1.setDatasetModelId(DATSET_MODEL_ID);

		cell1.setLine(line);
		line.getCells().add(cell1);

		final var cell2 = new Cell();
		cell2.setId(FIELD_MODEL_NUMBER_ID);
		cell2.setFieldModelId(FIELD_MODEL_NUMBER_ID);
		cell2.setDatasetModelId(DATSET_MODEL_ID);

		cell2.setLine(line);
		line.getCells().add(cell2);

		assertEquals(2, line.getCells().size());

		assertAll("Check form model count",
			() -> assertEquals(1, study.getFormModels().size()),
			() -> assertEquals(1, study.getDatasetModel(DATSET_MODEL_ID).getFieldModel(FIELD_MODEL_DATE_ID).getFormModels().size())
			);

		assertAll("Check the cell",
			() -> assertEquals(cell1, layout.getCell(FIELD_MODEL_DATE_ID)),
			() -> assertEquals(2, layout.getCells().size()),
			() -> assertEquals(formModel, layout.getFormModel())
			);
	}

	@Test
	@DisplayName("Create other dataset model")
	public void createOtherDatasetModel() {

		//Create dataset model
		final var datasetModel = StudyBuilder.buildSimpleDatasetModel(DATSET_MODEL_ID);

		//Create another dataset model
		final var otherDatasetModel = StudyBuilder.buildSimpleDatasetModel(DATSET_MODEL_ID + "2");

		//Create study and add both dataset models
		final var study = StudyBuilder.buildSimpleStudy()
			.addDatasetModel(datasetModel)
			.addDatasetModel(otherDatasetModel)
			.getStudy();

		assertEquals(2, study.getDatasetModels().size());
		assertNotNull(study.getDatasetModel(DATSET_MODEL_ID + "2"));

		assertAll("Check the dataset models were created",
			() -> assertEquals(2, study.getDatasetModels().size()),
			() -> assertNotNull(study.getDatasetModel(DATSET_MODEL_ID + "2"))
			);
	}
}
