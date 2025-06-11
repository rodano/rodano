package ch.rodano.configuration.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.opencsv.CSVWriter;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.configuration.model.export.DocumentHelper;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.form.FormModel;
import ch.rodano.configuration.model.layout.Cell;
import ch.rodano.configuration.model.layout.ColumnHeader;
import ch.rodano.configuration.model.layout.Layout;
import ch.rodano.configuration.model.payment.PaymentDistribution;
import ch.rodano.configuration.model.payment.PaymentPlan;
import ch.rodano.configuration.model.payment.PaymentStep;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.validator.Validator;

public final class ExportableUtils {
	private static final String DECIMAL = "Decimal";
	private static final String INTEGER = "Integer";

	private static final DecimalFormat NUMBER_FORMATTER;

	static {
		NUMBER_FORMATTER = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
		NUMBER_FORMATTER.applyPattern("0.0");
	}

	public static long getWidthInMillimeters(final String css) {
		//default width
		var width = 250d;

		if(StringUtils.isNotBlank(css)) {
			final var attributes = StringUtils.split(css, ';');
			for(final var attribute : attributes) {
				//find width field model
				final var value = StringUtils.split(attribute, ':');

				if(value[0].trim().toLowerCase().equals("width")) {
					width = Double.parseDouble(value[1].replaceAll("px", "").trim());
				}
			}
		}
		return Math.round(width / 950 * 270 * 1.1);
	}

	public static Element getExportForXml(final org.w3c.dom.Document doc, final Study study, final String... languages) {
		final var element = DocumentHelper.createElement(doc, "study", Map.of(
			"id", study.getId(),
			"shortname", study.getLocalizedShortname(languages),
			"longname", study.getLocalizedLongname(languages)));

		final var children = new HashMap<String, String>();
		children.put("description", study.getLocalizedDescription(languages));
		children.put("url", study.getUrl());
		children.put("client", study.getClient());
		children.put("clientEmail", study.getClientEmail());
		children.put("versionNumber", study.getVersionNumber());
		children.put("versionDate", study.getVersionDate());
		children.put("protocolNo", study.getProtocolNo());
		DocumentHelper.appendSimpleChildren(element, children);

		return element;
	}

	public static Element getExportForXml(final org.w3c.dom.Document doc, final ScopeModel scopeModel, final String... languages) {
		final var element = DocumentHelper.createElement(doc, "scopeModel", Map.of(
			"id", scopeModel.getId(),
			"shortname", scopeModel.getLocalizedShortname(languages),
			"longname", scopeModel.getLocalizedLongname(languages)));

		final var children = new HashMap<String, String>();
		children.put("description", scopeModel.getLocalizedDescription(languages));
		children.put("expectedNumber", Objects.toString(scopeModel.getExpectedNumber(), ""));
		children.put("maxNumber", Objects.toString(scopeModel.getMaxNumber(), ""));
		DocumentHelper.appendSimpleChildren(element, children);

		return element;
	}

	public static Element getExportForXml(final org.w3c.dom.Document doc, final EventModel eventModel, final String... languages) {
		final var element = DocumentHelper.createElement(doc, "eventModel", Map.of(
			"id", eventModel.getId(),
			"shortname", eventModel.getLocalizedShortname(languages),
			"longname", eventModel.getLocalizedLongname(languages)));

		final var children = new HashMap<String, String>();
		children.put("description", eventModel.getLocalizedDescription(languages));
		children.put("number", Objects.toString(eventModel.getNumber(), ""));
		children.put("deadline", Objects.toString(eventModel.getDeadline(), ""));
		children.put("deadlineUnit", eventModel.getDeadlineUnit() != null ? eventModel.getDeadlineUnit().toString() : "");
		DocumentHelper.appendSimpleChildren(element, children);

		return element;
	}

	public static Element getExportForXml(final org.w3c.dom.Document doc, final DatasetModel datasetModel, final String... languages) {
		final var element = DocumentHelper.createElement(doc, "datasetModel", Map.of(
			"id", datasetModel.getId(),
			"shortname", datasetModel.getLocalizedShortname(languages),
			"longname", datasetModel.getLocalizedLongname(languages)));

		final var children = new HashMap<String, String>();
		children.put("description", datasetModel.getLocalizedDescription(languages));
		children.put("family", StringUtils.defaultString(datasetModel.getFamily()));
		children.put("master", Objects.toString(datasetModel.getMaster(), ""));
		DocumentHelper.appendSimpleChildren(element, children);

		//field models
		final var fieldModelsElement = doc.createElement("fieldModels");
		element.appendChild(fieldModelsElement);
		for(final var fieldModel : datasetModel.getFieldModels()) {
			if(!fieldModel.isPlugin()) {
				fieldModelsElement.appendChild(getExportForXml(doc, fieldModel, languages));
			}
		}

		return element;
	}

	public static Element getExportForXml(final org.w3c.dom.Document doc, final FormModel formModel, final String... languages) {
		final var element = DocumentHelper.createElement(doc, "formModel", Map.of(
			"id", formModel.getId(),
			"shortname", formModel.getLocalizedShortname(languages),
			"longname", formModel.getLocalizedLongname(languages)));

		final var children = new HashMap<String, String>();
		children.put("description", formModel.getLocalizedDescription(languages));
		DocumentHelper.appendSimpleChildren(element, children);

		return element;
	}

	public static Element getExportForXml(final org.w3c.dom.Document doc, final Layout layout, final String... languages) {
		final var element = DocumentHelper.createElement(doc, "layout", Map.of(
			"id", layout.getId(),
			"shortname", layout.getLocalizedShortname(languages)));

		final var children = new HashMap<String, String>();
		children.put("type", layout.getType().name());
		children.put("repeatable", Boolean.toString(layout.getType().isRepeatable()));
		if(layout.getType().isRepeatable()) {
			children.put("addLabel", layout.getDatasetModel().getLocalizedShortname(languages));
		}

		//add condition
		layout.getFormModel().getLayouts().stream()
		.flatMap(l -> l.getCells().stream())
		.flatMap(c -> c.getVisibilityCriteria().stream())
		.filter(c -> c.getTargetLayoutIds().contains(layout.getId()))
		.findAny()
		.ifPresent(c -> children.put("condition", c.getDescription(languages)));

		//add all children
		DocumentHelper.appendSimpleChildren(element, children);

		final var textBefore = layout.getLocalizedTextBefore(languages);
		if(!StringUtils.isBlank(textBefore)) {
			element.appendChild(DocumentHelper.textAsNode(doc, "textBefore", textBefore));
		}
		final var textAfter = layout.getLocalizedTextAfter(languages);
		if(!StringUtils.isBlank(textAfter)) {
			element.appendChild(DocumentHelper.textAsNode(doc, "textAfter", textAfter));
		}

		//columns
		final var columnsElement = doc.createElement("columns");
		element.appendChild(columnsElement);
		layout.getColumns().forEach(column -> {
			columnsElement.appendChild(getExportForXml(doc, column, languages));
		});

		//lines and cells
		final var linesElement = doc.createElement("lines");
		element.appendChild(linesElement);
		for(final var line : layout.getLines()) {
			final var lineElement = doc.createElement("line");
			linesElement.appendChild(lineElement);
			for(final var cell : line.getCells()) {
				final var cellElement = getExportForXml(doc, cell, languages);
				lineElement.appendChild(cellElement);
			}
		}

		return element;
	}

	public static Element getExportForXml(final org.w3c.dom.Document doc, final ColumnHeader column, final String... languages) {
		final var element = DocumentHelper.createElement(doc, "column");
		DocumentHelper.appendSimpleChildren(element, Map.of(
			"width", String.valueOf(getWidthInMillimeters(column.getCssCode()))));
		return element;
	}

	public static Element getExportForXml(final org.w3c.dom.Document doc, final Cell cell, final String... languages) {
		final var element = DocumentHelper.createElement(doc, "cell", Map.of("id", cell.getId()));

		final var children = new HashMap<String, String>();
		children.put("colspan", cell.getColspan() > 0 ? String.valueOf(cell.getColspan()) : "1");
		children.put("labelWidth", String.valueOf(getWidthInMillimeters(cell.getCssCodeForLabel())));
		if(cell.hasFieldModel()) {
			children.put("datasetModelId", cell.getDatasetModelId());
			children.put("fieldModelId", cell.getFieldModelId());
			children.put("displayLabel", Boolean.toString(cell.getDisplayLabel()));
		}

		//add condition
		cell.getLine().getLayout().getCells().stream()
		.flatMap(c -> c.getVisibilityCriteria().stream())
		.filter(c -> c.getTargetCellIds().contains(cell.getId()))
		.findAny()
		.ifPresent(c -> children.put("condition", c.getDescription(languages)));

		//add all children
		DocumentHelper.appendSimpleChildren(element, children);

		final var textBefore = cell.getLocalizedTextBefore(languages);
		if(!StringUtils.isBlank(textBefore)) {
			element.appendChild(DocumentHelper.textAsNode(doc, "textBefore", textBefore));
		}
		final var textAfter = cell.getLocalizedTextAfter(languages);
		if(!StringUtils.isBlank(textAfter)) {
			element.appendChild(DocumentHelper.textAsNode(doc, "textAfter", textAfter));
		}

		//field model
		if(cell.hasFieldModel()) {
			final var fieldModel = cell.getFieldModel();
			element.appendChild(getExportForXml(doc, fieldModel, languages));
		}

		return element;
	}

	public static Element getExportForXml(final org.w3c.dom.Document doc, final FieldModel fieldModel, final String... languages) {
		final var element = DocumentHelper.createElement(doc, "fieldModel", Map.of(
			"id", fieldModel.getId(),
			"shortname", fieldModel.getLocalizedShortname(languages),
			"longname", fieldModel.getLocalizedLongname(languages)));

		final var children = new HashMap<String, String>();
		children.put("description", fieldModel.getLocalizedDescription(languages));
		children.put("type", fieldModel.getType().toString());
		children.put("label", fieldModel.getLocalizedLabel(languages));
		children.put("exportLabel", fieldModel.getExportColumnLabel());
		children.put("readOnly", Boolean.toString(fieldModel.isReadOnly()));
		children.put("otherOption", Boolean.toString(fieldModel.hasPossibleValuesOther()));
		children.put("maxLength", Objects.toString(fieldModel.getMaxLength(), ""));
		children.put("inlineHelp", fieldModel.getInlineHelpOrFormat());
		children.put("hasPossibleValuesProvider", Boolean.toString(fieldModel.hasPossibleValuesProvider()));
		children.put("possibleValuesProviderDescription", fieldModel.getPossibleValuesProviderDescription());
		DocumentHelper.appendSimpleChildren(element, children);

		//possible values
		final var pvsElement = doc.createElement("possibleValues");
		element.appendChild(pvsElement);
		fieldModel.getPossibleValues().forEach(possibleValue -> {
			final var pvElement = DocumentHelper.createElement(doc, "possibleValue", Map.of(
				"id", possibleValue.getId(),
				"shortname", possibleValue.getLocalizedShortname(languages)));
			DocumentHelper.appendSimpleChildren(pvElement, Map.of(
				"label", possibleValue.getLocalizedShortname(languages),
				"exportLabel", possibleValue.getExportColumnLabel(),
				"specify", Boolean.toString(possibleValue.isSpecify())));
			pvsElement.appendChild(pvElement);
		});

		//validators
		final var validatorsElement = doc.createElement("validators");
		element.appendChild(validatorsElement);
		for(final var validator : fieldModel.getValidators()) {
			validatorsElement.appendChild(getExportForXml(doc, validator, languages));
		}
		if(FieldModelType.DATE.equals(fieldModel.getType())) {
			final var notInFutureName = "Cannot be in the future";
			final var notInFuture = DocumentHelper.createElement(doc, "validator", Map.of(
				"id", "NOT_IN_THE_FUTURE",
				"shortname", notInFutureName,
				"longname", notInFutureName));

			DocumentHelper.appendSimpleChildren(notInFuture, Map.of("description", notInFutureName));
			validatorsElement.appendChild(notInFuture);

			final var notBefore1900Name = "Must be after 1900";
			final var notBefore1900 = DocumentHelper.createElement(doc, "validator", Map.of(
				"id", "NOT_BEFORE_1900",
				"shortname", notBefore1900Name,
				"longname", notBefore1900Name));
			DocumentHelper.appendSimpleChildren(notBefore1900, Map.of("description", notBefore1900Name));
			validatorsElement.appendChild(notBefore1900);
		}

		if(FieldModelType.DATE_SELECT.equals(fieldModel.getType())) {
			DocumentHelper.appendSimpleChildren(element, Map.of(
				"displayDays", Boolean.toString(fieldModel.isWithDays()),
				"displayMonths", Boolean.toString(fieldModel.isWithMonths()),
				"displayYears", Boolean.toString(fieldModel.isWithYears())));
		}

		return element;
	}

	public static Element getExportForXml(final org.w3c.dom.Document doc, final Validator validator, final String... languages) {
		final var element = DocumentHelper.createElement(doc, "validator", Map.of(
			"id", validator.getId(),
			"shortname", validator.getLocalizedShortname(languages),
			"longname", validator.getLocalizedLongname(languages)));

		final var children = new HashMap<String, String>();
		children.put("description", validator.getLocalizedDescription(languages));
		DocumentHelper.appendSimpleChildren(element, children);

		return element;
	}

	public static Element getExportForXml(final org.w3c.dom.Document doc, final PaymentPlan plan, final String... languages) {
		final var element = DocumentHelper.createElement(doc, "plan", Map.of(
			"id", plan.getId(),
			"shortname", plan.getLocalizedShortname(languages),
			"longname", plan.getLocalizedLongname(languages)));

		final var children = new HashMap<String, String>();
		children.put("description", plan.getLocalizedDescription(languages));
		children.put("currency", plan.getCurrency());
		children.put("entity", plan.getEntity().getLocalizedShortname(languages));
		children.put("workflow", plan.getWorkflow());
		children.put("state", plan.getState());
		DocumentHelper.appendSimpleChildren(element, children);

		//steps
		final var stepsElement = doc.createElement("steps");
		element.appendChild(stepsElement);
		for(final var step : plan.getSteps()) {
			stepsElement.appendChild(getExportForXml(doc, step, languages));
		}

		return element;
	}

	public static Element getExportForXml(final org.w3c.dom.Document doc, final PaymentStep step, final String... languages) {
		final var element = DocumentHelper.createElement(doc, "step", Map.of(
			"id", step.getId(),
			"shortname", step.getLocalizedShortname(languages),
			"longname", step.getLocalizedLongname(languages)));

		final var children = new HashMap<String, String>();
		children.put("description", step.getLocalizedDescription(languages));
		children.put("entity", step.getEntity().getLocalizedShortname(languages));
		children.put("amount", NUMBER_FORMATTER.format(step.getDistributions().stream().mapToDouble(PaymentDistribution::getValue).sum()));
		DocumentHelper.appendSimpleChildren(element, children);

		return element;
	}

	private static String getFormatSpecification(final FieldModel fieldModel) {
		switch(fieldModel.getType()) {
			case CHECKBOX:
			case CHECKBOX_GROUP:
			case DATE_SELECT:
			case RADIO:
			case SELECT:
			case TEXTAREA:
			case STRING:
				return "String";
			case NUMBER:
				return fieldModel.isDecimal() ? DECIMAL : INTEGER;
			default:
				return fieldModel.getInlineHelpOrFormat();
		}
	}

	private static String getPossibleValuesSpecification(final FieldModel fieldModel, final String... languages) {
		if(FieldModelType.CHECKBOX.equals(fieldModel.getType())) {
			return "true = true; false = false; \"blank\" = false;";
		}
		if(fieldModel.hasPossibleValuesProvider()) {
			return fieldModel.getPossibleValuesProviderDescription();
		}
		return fieldModel.getPossibleValues().stream().map(pv -> pv.getId() + " = " + pv.getLocalizedShortname(languages) + ";").collect(Collectors.joining(" "));
	}

	@SuppressWarnings("null")
	public static void getDataStructure(final OutputStream out, final Collection<DatasetModel> datasetModels, final boolean withModificationDate, final String... languages) throws IOException {
		@SuppressWarnings("resource")
		final var writer = new CSVWriter(new OutputStreamWriter(out));

		//static columns
		writer.writeNext(new String[] {
			"DATASET_ID",
			"DATASET_LABEL",
			"PAGES",
			"FIELD_ID",
			"FIELD_LABEL",
			"DESCRIPTION",
			"TYPE",
			"MAX_LENGTH",
			"FORMAT",
			"POSSIBLE_VALUES_CALCULATED",
			"POSSIBLE_VALUES",
			"DATA_CALCULATED"
		});

		for(final var datasetModel : datasetModels) {
			//retrieve scope models and event models that use the dataset model
			final var scopeModels = datasetModel.getScopeModels();
			final var eventModels = datasetModel.getEventModels();

			//bypass dataset models that are not used
			if(scopeModels.isEmpty() && eventModels.isEmpty()) {
				continue;
			}
			final var scopeModel = !scopeModels.isEmpty() ? scopeModels.get(0) : eventModels.get(0).getScopeModel();
			final var parentScopeModel = scopeModel.isRoot() ? null : scopeModel.getDefaultParent();

			//create first rows for informations columns
			if(!scopeModel.isRoot()) {
				//parent scope id
				writer.writeNext(new String[] {
					datasetModel.getId(),
					datasetModel.getLocalizedShortname(languages),
					"", //form models
					String.format("%s_ID", parentScopeModel.getId().toUpperCase()),
					String.format("%s primary key", StringUtils.capitalize(parentScopeModel.getId().toLowerCase())),
					String.format("Unique %s identifier", parentScopeModel.getLocalizedShortname(languages).toLowerCase()),
					"NUMBER",
					"11",
					"Integer",
					"No",
					"",
					"No"
				});

				//parent scope code
				writer.writeNext(new String[] {
					datasetModel.getId(),
					datasetModel.getLocalizedShortname(languages),
					"", //form models
					parentScopeModel.getId().toUpperCase(),
					String.format("%s label", parentScopeModel.getLocalizedShortname(languages)),
					"",
					"STRING",
					"64",
					"String",
					"No",
					"",
					"No"
				});
			}

			//scope id
			writer.writeNext(new String[] {
				datasetModel.getId(),
				datasetModel.getLocalizedShortname(languages),
				"", //form models
				String.format("%s_ID", scopeModel.getId().toUpperCase()),
				String.format("%s primary key", StringUtils.capitalize(scopeModel.getId().toLowerCase())),
				String.format("Unique %s identifier", scopeModel.getLocalizedShortname(languages).toLowerCase()),
				"NUMBER",
				"11",
				"Integer",
				"No",
				"",
				"No"
			});

			//scope code
			writer.writeNext(new String[] {
				datasetModel.getId(),
				datasetModel.getLocalizedShortname(languages),
				"", //form models
				scopeModel.getId().toUpperCase(),
				String.format("%s label", scopeModel.getLocalizedShortname(languages)),
				"",
				"STRING",
				"64",
				"String",
				"No",
				"",
				"No"
			});

			if(!datasetModel.isScopeDocumentation()) {
				//event id
				writer.writeNext(new String[] {
					datasetModel.getId(),
					datasetModel.getLocalizedShortname(languages),
					"", //form models
					"EVENT_ID",
					"Event primary key",
					"Unique event identifier",
					"NUMBER",
					"11",
					"Integer",
					"No",
					"",
					"No"
				});

				//event
				writer.writeNext(new String[] {
					datasetModel.getId(),
					datasetModel.getLocalizedShortname(languages),
					"", //form models
					"EVENT",
					"Event label",
					"",
					"STRING",
					"200",
					"String",
					"No",
					"",
					"No"
				});

				//event date
				/*writer.writeNext(new String[] {
					document.getId(),
					document.getLocalizedShortname(languages),
					"", //form models
					"EVENT_DATE",
					"Event date",
					"",
					"DATE",
					"25",
					"ISO 8601",
					"No",
					"",
					"No"
				});

				//event end date
				writer.writeNext(new String[] {
					document.getId(),
					document.getLocalizedShortname(languages),
					"", //form models
					"EVENT_END_DATE",
					"Event end date",
					"",
					"DATE",
					"25",
					"ISO 8601",
					"No",
					"",
					"No"
				});*/
			}

			//dataset id
			writer.writeNext(new String[] {
				datasetModel.getId(),
				datasetModel.getLocalizedShortname(languages),
				"", //form models
				"DATASET_ID",
				"Dataset primary key",
				"Unique dataset identifier",
				"NUMBER",
				"11",
				"Integer",
				"No",
				"",
				"No"
			});

			//field model rows
			for(final var fieldModel : datasetModel.getFieldModelsExportables()) {

				final var fieldModelFormModels = new HashSet<>(fieldModel.getFormModels()).stream().map(page -> page.getLocalizedShortname(languages)).collect(Collectors.joining(", "));
				final var maxLength = fieldModel.getMaxLengthOrDefault();
				final var maxLenghtLabel = maxLength != null ? Integer.toString(maxLength) : "";

				writer.writeNext(new String[] {
					//dataset model columns
					datasetModel.getId(),
					datasetModel.getLocalizedShortname(languages),

					//form models column
					fieldModelFormModels,

					//field model columns
					fieldModel.getExportColumnLabel(),
					StringUtils.defaultString(fieldModel.getLocalizedShortname(languages)),
					StringUtils.defaultString(fieldModel.getLocalizedDescription(languages)),

					fieldModel.getType().name(),
					maxLenghtLabel,

					getFormatSpecification(fieldModel),
					fieldModel.hasPossibleValuesProvider() ? "Yes" : "No",
						getPossibleValuesSpecification(fieldModel, languages),

						fieldModel.isPlugin() ? "Yes" : "No"
				});

				if(withModificationDate) {
					writer.writeNext(new String[] {
						//dataset model columns
						datasetModel.getId(),
						datasetModel.getLocalizedShortname(languages),

						//form models column
						fieldModelFormModels,

						//fieldModel columns
						fieldModel.getExportColumnLabel() + "_MODIFICATION_DATE",
						StringUtils.defaultString("Modification date of " + fieldModel.getLocalizedShortname(languages)),
						StringUtils.defaultString("Modification date of " + fieldModel.getId() + " field"),

						FieldModelType.DATE.name(),
						"25",
						"No",
						"ISO 8601",
						"",
						"No"
					});
				}
			}
		}

		// Flush the writer to the OutputStream
		writer.flush();
	}
}
