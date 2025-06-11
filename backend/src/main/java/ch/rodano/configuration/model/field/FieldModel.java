package ch.rodano.configuration.model.field;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.exceptions.NoNodeException;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.common.SuperDisplayable;
import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.form.FormModel;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.configuration.model.rules.Rule;
import ch.rodano.configuration.model.rules.RuleConstraint;
import ch.rodano.configuration.model.validator.DateValidator;
import ch.rodano.configuration.model.validator.InvalidOtherPossibleValue;
import ch.rodano.configuration.model.validator.InvalidPossibleValue;
import ch.rodano.configuration.model.validator.NumberValidator;
import ch.rodano.configuration.model.validator.TextValidator;
import ch.rodano.configuration.model.validator.Validator;
import ch.rodano.configuration.model.validator.ValueCheck;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowableModel;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class FieldModel implements WorkflowableModel, SuperDisplayable, Serializable, Node, Comparable<FieldModel> {
	private static final long serialVersionUID = -3499790065637274737L;

	private static Comparator<FieldModel> DEFAULT_COMPARATOR = Comparator.comparing(FieldModel::getDatasetModel)
		.thenComparing(Comparator.comparing(FieldModel::getExportOrder))
		.thenComparing(FieldModel::getId);

	public static final int MAX_LENGTH = 400;

	private String id;
	private DatasetModel datasetModel;

	private FieldModelType type;
	private OperandType dataType;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private List<PossibleValue> possibleValues;
	private String dictionary;
	private String possibleValuesProvider;
	private String possibleValuesProviderDescription;

	private List<String> validatorIds;
	private List<String> workflowIds;

	private boolean plugin;
	private String valueFormula;
	private RuleConstraint valueConstraint;

	private boolean readOnly;

	//export
	private boolean exportable;
	private int exportOrder;
	private String exportLabel;

	private boolean searchable;

	//sizable
	private Integer maxLength;

	//string
	private String matcher;
	private Map<String, String> matcherMessage;

	//help
	private String inlineHelp;
	private SortedMap<String, String> advancedHelp;

	//number
	private int maxIntegerDigits;
	private int maxDecimalDigits;

	private Double minValue;
	private Double maxValue;

	//date
	private boolean allowDateInFuture;
	private Integer minYear;
	private Integer maxYear;

	private boolean withYears;
	private boolean withMonths;
	private boolean withDays;
	private boolean withHours;
	private boolean withMinutes;
	private boolean withSeconds;

	//date select
	private boolean yearsMandatory;
	private boolean monthsMandatory;
	private boolean daysMandatory;
	private boolean hoursMandatory;
	private boolean minutesMandatory;
	private boolean secondsMandatory;

	//modification rules
	private List<Rule> rules;

	public FieldModel() {
		type = FieldModelType.STRING;
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		advancedHelp = new TreeMap<>();
		possibleValues = new ArrayList<>();
		validatorIds = new ArrayList<>();
		workflowIds = new ArrayList<>();
		matcherMessage = new HashMap<>();
		rules = new ArrayList<>();
	}

	@Override
	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@JsonBackReference
	public final void setDatasetModel(final DatasetModel datasetModel) {
		this.datasetModel = datasetModel;
	}

	@JsonBackReference
	public final DatasetModel getDatasetModel() {
		return datasetModel;
	}

	@JsonManagedReference
	public final List<PossibleValue> getPossibleValues() {
		return possibleValues;
	}

	@JsonManagedReference
	public final void setPossibleValues(final List<PossibleValue> possibleValues) {
		this.possibleValues = possibleValues;
	}

	public String getDictionary() {
		return dictionary;
	}

	public void setDictionary(final String dictionary) {
		this.dictionary = dictionary;
	}

	public String getPossibleValuesProvider() {
		return possibleValuesProvider;
	}

	public void setPossibleValuesProvider(final String possibleValuesProvider) {
		this.possibleValuesProvider = possibleValuesProvider;
	}

	public String getPossibleValuesProviderDescription() {
		return possibleValuesProviderDescription;
	}

	public void setPossibleValuesProviderDescription(final String possibleValuesProviderDescription) {
		this.possibleValuesProviderDescription = possibleValuesProviderDescription;
	}

	@JsonIgnore
	public final boolean hasPossibleValuesProvider() {
		return StringUtils.isNotBlank(possibleValuesProvider);
	}

	@JsonIgnore
	public final PossibleValue getPossibleValue(final String possibleValueId) {
		return possibleValues.stream()
			.filter(e -> e.getId().equalsIgnoreCase(possibleValueId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.POSSIBLE_VALUE, possibleValueId));
	}

	@JsonIgnore
	public final List<String> getPossibleValueIds() {
		return possibleValues.stream().map(PossibleValue::getId).toList();
	}

	@JsonIgnore
	public final boolean hasPossibleValuesOther() {
		return possibleValues.stream().anyMatch(PossibleValue::isSpecify);
	}

	@JsonIgnore
	public final Optional<PossibleValue> getPossibleValuesOther() {
		return getPossibleValues().stream().filter(PossibleValue::isSpecify).findAny();
	}

	public final void setValidatorIds(final List<String> validatorIds) {
		this.validatorIds = new ArrayList<>(validatorIds);
	}

	public final List<String> getValidatorIds() {
		return validatorIds;
	}

	@JsonIgnore
	public final Set<Validator> getValidators() {
		final var study = datasetModel.getStudy();
		return validatorIds.stream().map(study::getValidator).collect(Collectors.toSet());
	}

	public void setWorkflowIds(final List<String> workflowIds) {
		this.workflowIds = workflowIds;
	}

	@Override
	public List<String> getWorkflowIds() {
		return workflowIds;
	}

	public final FieldModelType getType() {
		return type;
	}

	public final void setType(final FieldModelType type) {
		this.type = type;
	}

	@JsonIgnore
	public Integer getMaxLengthOrDefault() {
		//use max length only for strings and numbers
		if((OperandType.STRING.equals(dataType) || OperandType.NUMBER.equals(dataType)) && maxLength != null && maxLength > 0) {
			return maxLength;
		}
		//use type max length
		return type.hasDefaultMaxLength() ? type.getDefaultMaxLength() : null;
	}

	@JsonIgnore
	public String getSQLType() {
		return type.toSQL(getMaxLengthOrDefault());
	}

	public OperandType getDataType() {
		return dataType;
	}

	public void setDataType(final OperandType dataType) {
		this.dataType = dataType;
	}

	public final Integer getMaxLength() {
		return maxLength;
	}

	public final void setMaxLength(final Integer maxLength) {
		this.maxLength = maxLength;
	}

	public boolean isPlugin() {
		return plugin;
	}

	public void setPlugin(final boolean plugin) {
		this.plugin = plugin;
	}

	public String getValueFormula() {
		return valueFormula;
	}

	public void setValueFormula(final String valueFormula) {
		this.valueFormula = valueFormula;
	}

	public RuleConstraint getValueConstraint() {
		return valueConstraint;
	}

	public void setValueConstraint(final RuleConstraint valueConstraint) {
		this.valueConstraint = valueConstraint;
	}

	public final String getMatcher() {
		return matcher;
	}

	public final void setMatcher(final String matcher) {
		this.matcher = matcher;
	}

	public final Map<String, String> getMatcherMessage() {
		return matcherMessage;
	}

	public final void setMatcherMessage(final Map<String, String> matcherMessage) {
		this.matcherMessage = matcherMessage;
	}


	public int getMaxIntegerDigits() {
		return maxIntegerDigits;
	}

	public void setMaxIntegerDigits(final int maxIntegerDigits) {
		this.maxIntegerDigits = maxIntegerDigits;
	}

	public int getMaxDecimalDigits() {
		return maxDecimalDigits;
	}

	public void setMaxDecimalDigits(final int maxDecimalDigits) {
		this.maxDecimalDigits = maxDecimalDigits;
	}


	public final Double getMinValue() {
		return minValue;
	}

	public final void setMinValue(final Double minValue) {
		this.minValue = minValue;
	}

	public final Double getMaxValue() {
		return maxValue;
	}

	public final void setMaxValue(final Double maxValue) {
		this.maxValue = maxValue;
	}

	public final String getInlineHelp() {
		return inlineHelp;
	}

	public final void setInlineHelp(final String inlineHelp) {
		this.inlineHelp = inlineHelp;
	}

	public final Map<String, String> getAdvancedHelp() {
		return advancedHelp;
	}

	public final void setAdvancedHelp(final SortedMap<String, String> advancedHelp) {
		this.advancedHelp = advancedHelp;
	}

	public boolean isAllowDateInFuture() {
		return allowDateInFuture;
	}

	public void setAllowDateInFuture(final boolean allowDateInFuture) {
		this.allowDateInFuture = allowDateInFuture;
	}

	public final List<Rule> getRules() {
		return rules;
	}

	public final void setRules(final List<Rule> rules) {
		this.rules = rules;
	}

	@Override
	public final Map<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	@Override
	public final Map<String, String> getLongname() {
		return longname;
	}

	public final void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	@Override
	public final Map<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public final boolean isReadOnly() {
		return readOnly;
	}

	public final void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
	}

	public final boolean isExportable() {
		return exportable;
	}

	public final void setExportable(final boolean exportable) {
		this.exportable = exportable;
	}

	public final Integer getExportOrder() {
		return exportOrder;
	}

	public final void setExportOrder(final int exportOrder) {
		this.exportOrder = exportOrder;
	}

	public final String getExportLabel() {
		return exportLabel;
	}

	public final void setExportLabel(final String exportLabel) {
		this.exportLabel = exportLabel;
	}

	public final boolean isSearchable() {
		return searchable;
	}

	public final void setSearchable(final boolean searchable) {
		this.searchable = searchable;
	}

	@JsonIgnore
	public final String getRealExportLabel() {
		return StringUtils.defaultIfBlank(exportLabel, id);
	}

	@JsonIgnore
	@Override
	public final List<Workflow> getWorkflows() {
		return datasetModel.getStudy().getNodesFromIds(Entity.WORKFLOW, getWorkflowIds());
	}

	@JsonIgnore
	public List<FormModel> getFormModels() {
		return datasetModel.getStudy().getFormModels().stream()
			.filter(f -> f.containsFieldModel(this))
			.toList();
	}

	@JsonIgnore
	public final String getLocalizedLabel(final String... languages) {
		if(isRequired()) {
			final var label = new StringBuilder(getLocalizedShortname(languages));
			label.append(" *");
			return label.toString();
		}
		return getLocalizedShortname(languages);
	}

	@JsonIgnore
	public final boolean isRequired() {
		return getValidators().stream().anyMatch(Validator::isRequired);
	}

	@JsonIgnore
	public final String getDefaultLocalizedShortname() {
		return getLocalizedLabel(getDatasetModel().getStudy().getDefaultLanguage().getId());
	}

	@JsonIgnore
	public final String getDefaultLocalizedLongname() {
		return getLocalizedLongname(getDatasetModel().getStudy().getDefaultLanguage().getId());
	}

	@JsonIgnore
	public final String getOperatorLabel() {
		final var operator = new StringBuilder(type.toString());
		operator.append(".");
		operator.append(id);
		return operator.toString();
	}

	@Override
	@JsonIgnore
	public int compareTo(final FieldModel otherFieldModel) {
		return DEFAULT_COMPARATOR.compare(this, otherFieldModel);
	}

	@Override
	public final Entity getEntity() {
		return Entity.FIELD_MODEL;
	}

	@JsonIgnore
	public final String getExportAfter() {
		return getId();
	}

	@JsonIgnore
	public final boolean isRepeatedInEventModels() {
		return datasetModel.isRepeatedInEventModels();
	}

	@JsonIgnore
	public final boolean hasValueFormula() {
		return StringUtils.isNotEmpty(valueFormula);
	}

	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		switch(entity) {
			case VALIDATOR:
				return Collections.unmodifiableSet(getValidators());
			default:
				return Collections.emptyList();
		}
	}

	public final Integer getMinYear() {
		return minYear;
	}

	public final void setMinYear(final Integer minYear) {
		this.minYear = minYear;
	}

	public final Integer getMaxYear() {
		return maxYear;
	}

	public final void setMaxYear(final Integer maxYear) {
		this.maxYear = maxYear;
	}

	public final boolean isWithDays() {
		return withDays;
	}

	public final void setWithDays(final boolean withDays) {
		this.withDays = withDays;
	}

	public final boolean isWithMonths() {
		return withMonths;
	}

	public final void setWithMonths(final boolean withMonths) {
		this.withMonths = withMonths;
	}

	public final boolean isWithYears() {
		return withYears;
	}

	public final void setWithYears(final boolean withYears) {
		this.withYears = withYears;
	}

	public boolean isWithHours() {
		return withHours;
	}

	public void setWithHours(final boolean withHours) {
		this.withHours = withHours;
	}

	public boolean isWithMinutes() {
		return withMinutes;
	}

	public void setWithMinutes(final boolean withMinutes) {
		this.withMinutes = withMinutes;
	}

	public boolean isWithSeconds() {
		return withSeconds;
	}

	public void setWithSeconds(final boolean withSeconds) {
		this.withSeconds = withSeconds;
	}

	public final boolean isDaysMandatory() {
		return daysMandatory;
	}

	public final void setDaysMandatory(final boolean daysMandatory) {
		this.daysMandatory = daysMandatory;
	}

	public final boolean isMonthsMandatory() {
		return monthsMandatory;
	}

	public final void setMonthsMandatory(final boolean monthsMandatory) {
		this.monthsMandatory = monthsMandatory;
	}

	public final boolean isYearsMandatory() {
		return yearsMandatory;
	}

	public final void setYearsMandatory(final boolean yearsMandatory) {
		this.yearsMandatory = yearsMandatory;
	}

	public boolean isHoursMandatory() {
		return hoursMandatory;
	}

	public void setHoursMandatory(final boolean hoursMandatory) {
		this.hoursMandatory = hoursMandatory;
	}

	public boolean isMinutesMandatory() {
		return minutesMandatory;
	}

	public void setMinutesMandatory(final boolean minutesMandatory) {
		this.minutesMandatory = minutesMandatory;
	}

	public boolean isSecondsMandatory() {
		return secondsMandatory;
	}

	public void setSecondsMandatory(final boolean secondsMandatory) {
		this.secondsMandatory = secondsMandatory;
	}

	@JsonIgnore
	public Comparator<String> getPossibleValueComparator() {
		final var possibleValueIds = getPossibleValueIds();
		return (o1, o2) -> {
			final var index1 = possibleValueIds.indexOf(o1);
			final var index2 = possibleValueIds.indexOf(o2);
			//o1 is "other" option
			if(index1 == -1) {
				return 1;
			}
			//o2 is "other" option
			if(index2 == -1) {
				return -1;
			}
			return index1 - index2;
		};
	}

	private String getPossibleValueLabel(final List<PossibleValue> actualPossibleValues, final String value, final String... languages) {
		return actualPossibleValues.stream()
			.filter(p -> p.getId().equals(value))
			.findAny().map(p -> p.getLocalizedShortname(languages))
			.orElse(value);
	}

	/**
	 * Transform a field value into a nice label, based on the possible values described in the configuration
	 * This does not work for fields with a custom possible values provider
	 * However, this works most and the time and is required where the custom possible values are not available
	 * @param value the value to transform into a nice label
	 * @param languages the languages to use for the label
	 * @return a nice label
	 */
	public String valueToLabel(final String value, final String... languages) {
		return valueToLabel(possibleValues,  value, languages);
	}

	public String valueToLabel(final List<PossibleValue> actualPossibleValues, final String value, final String... languages) {
		if(StringUtils.isBlank(value)) {
			return "";
		}

		//multiple choice value
		if(type.isMultipleChoice()) {
			//values list
			if(FieldModelType.CHECKBOX_GROUP.equals(type)) {
				//transform value into list
				return Arrays.asList(value.split(",")).stream()
					.map(v -> getPossibleValueLabel(actualPossibleValues, v, languages))
					.collect(Collectors.joining(", "));
			}
			//other multiple choice
			return getPossibleValueLabel(actualPossibleValues, value, languages);
		}

		//normal value
		return value;
	}

	public Object stringToObject(final String value) {
		//empty values
		if(StringUtils.isBlank(value)) {
			return type.hasMultipleValues() ? new ArrayList<String>() : null;
		}

		//date select
		if(FieldModelType.DATE_SELECT.equals(type)) {
			return PartialDate.of(value);
		}

		//multiple values
		if(type.hasMultipleValues()) {
			return Arrays.asList(value.split(","));
		}

		//date
		if(FieldModelType.DATE.equals(type)) {
			//format may have changed after value has been saved in database
			//therefore, value must be reformatted
			final var formatter = getDateTimeFormatter();
			final var localDate = LocalDateTime.parse(value, formatter);
			final var date = ZonedDateTime.of(localDate, ZoneOffset.UTC);
			return PartialDate.of(date);
		}

		//number
		if(FieldModelType.NUMBER.equals(type)) {
			if(isInteger()) {
				return Integer.valueOf(value);
			}
			return Double.valueOf(value);
		}

		//other numeric values
		if(OperandType.NUMBER.equals(getDataType())) {
			return Float.parseFloat(value);
		}

		return value;
	}

	@SuppressWarnings("unchecked")
	public String objectToString(final Object value) {
		//empty values
		if(value == null) {
			return "";
		}

		//date select
		if(FieldModelType.DATE_SELECT.equals(type)) {
			//value can be a ZonedDateTime or a PartialDate
			//in any case, we need a PartialDate at the end
			final var date = value instanceof ZonedDateTime ? PartialDate.of((ZonedDateTime) value) : (PartialDate) value;
			return date.format(withYears, withMonths, withDays);
		}

		//multiple values
		if(type.hasMultipleValues()) {
			return StringUtils.join((Collection<String>) value, ",");
		}

		//date
		if(FieldModelType.DATE.equals(type)) {
			//value can be a ZonedDateTime or a PartialDate
			//in any case, we need a ZonedDateTime at the end
			final ZonedDateTime date;
			if(value instanceof PartialDate) {
				final var partialDate = (PartialDate) value;
				//if partial date is not anchored in time, it is not possible to transform it into a real date
				if(!partialDate.isAnchoredInTime()) {
					return "";
				}
				date = partialDate.toZonedDateTime().get();
			}
			else {
				date = (ZonedDateTime) value;
			}
			return date.format(getDateTimeFormatter());
		}

		//number
		if(FieldModelType.NUMBER.equals(type)) {
			return getNumberFormatter().format(value);
		}

		return value.toString();
	}

	public ValueCheck checkAndSanitizeValue(final List<PossibleValue> actualPossibleValues, final String value) {
		//do some specific checks if value is not empty
		if(StringUtils.isNotBlank(value)) {
			//validate data type
			//for data type return only if errors are detected and execute other validators otherwise
			//data string must not be longer than max length and match the provided regexp
			if(OperandType.STRING.equals(dataType)) {
				final var valueCheck = validateText(value);
				if(valueCheck.hasError()) {
					return valueCheck;
				}
			}
			//validate fieldModel type
			//number must be parsable as a double
			if(FieldModelType.NUMBER.equals(type)) {
				return validateNumber(value);
			}

			if(FieldModelType.DATE.equals(type) || FieldModelType.DATE_SELECT.equals(type)) {
				return validateDate(value);
			}

			//select and radio must have their value among possible values
			if(FieldModelType.SELECT.equals(type) || FieldModelType.RADIO.equals(type)) {
				final var possibleValue = actualPossibleValues.stream()
					.filter(p -> p.getId().equals(value))
					.findAny();
				if(possibleValue.isEmpty()) {
					return InvalidPossibleValue.impossibleValue(value);
				}
			}

			//checkbox group and palette must have their values among possible values and eventually an other value
			if(FieldModelType.CHECKBOX_GROUP.equals(type)) {
				final var possibleValuesOther = getPossibleValuesOther();
				//blank possible value may only happen for "other" value and it's not allowed
				if(value.startsWith(",") || value.endsWith(",")) {
					return InvalidOtherPossibleValue.specifyCanNotBeBlank(possibleValuesOther.get().getDefaultLocalizedShortname());
				}
				final var values = new TreeSet<>(getPossibleValueComparator());
				values.addAll(Arrays.asList(value.split(",")));
				//check possible values and other option
				final var possibleValueIds = getPossibleValueIds();
				var otherValue = false;
				for(final var partialValue : values) {
					if(!possibleValueIds.contains(partialValue)) {
						if(possibleValuesOther.isEmpty()) {
							return InvalidPossibleValue.impossibleValue(value);
						}
						//other value can only appear once
						if(otherValue) {
							return InvalidPossibleValue.otherOptionRequested(value);
						}
						otherValue = true;
					}
				}
				return new ValueCheck(StringUtils.join(values, ","));
			}
		}
		return new ValueCheck(value);
	}

	private ValueCheck validateText(final String value) {
		final var validator = new TextValidator(this);
		final var sanitizedValue = validator.sanitizeValue(value);
		return validator.validate(sanitizedValue);
	}

	private ValueCheck validateNumber(final String value) {
		final var validator = new NumberValidator(this);
		final var sanitizedValue = validator.sanitizeValue(value);
		return validator.validate(sanitizedValue);
	}

	private ValueCheck validateDate(final String value) {
		final var validator = new DateValidator(this);
		final var sanitizedValue = validator.sanitizeValue(value);
		return validator.validate(sanitizedValue);
	}

	@JsonIgnore
	public boolean isDate() {
		return withYears || withMonths || withDays;
	}

	@JsonIgnore
	public boolean isTime() {
		return withHours || withMinutes || withSeconds;
	}

	@JsonIgnore
	public boolean isDecimal() {
		return maxDecimalDigits > 0;
	}

	@JsonIgnore
	public boolean isInteger() {
		return maxIntegerDigits > 0 && maxDecimalDigits == 0;
	}

	@JsonIgnore
	public String getInlineHelpOrFormat() {
		if(!StringUtils.isBlank(inlineHelp)) {
			return inlineHelp;
		}
		if(FieldModelType.DATE.equals(type)) {
			return getDateTimeFormat().toLowerCase();
		}
		if(FieldModelType.NUMBER.equals(type)) {
			return getNumberFormat().toLowerCase();
		}
		return "";
	}

	@JsonIgnore
	public String getNumberFormat() {
		final var format = new StringBuilder("#");
		if(maxDecimalDigits > 0) {
			format.append(".#");
		}
		return format.toString();
	}

	@JsonIgnore
	public DecimalFormat getNumberFormatter() {
		return new DecimalFormat(getNumberFormat());
	}

	@JsonIgnore
	public String getDateTimeFormat() {
		final StringBuilder format = new StringBuilder();
		if(withDays) {
			format.append("dd");
		}
		if(withMonths) {
			if(!format.isEmpty()) {
				format.append(".");
			}
			format.append("MM");
		}
		if(withYears) {
			if(!format.isEmpty()) {
				format.append(".");
			}
			format.append("yyyy");
		}
		if(withHours) {
			if(!format.isEmpty()) {
				format.append(" ");
			}
			format.append("HH");
		}
		if(withMinutes) {
			if(!format.isEmpty()) {
				format.append(":");
			}
			format.append("mm");
		}
		if(withSeconds) {
			if(!format.isEmpty()) {
				format.append(":");
			}
			format.append("ss");
		}
		return format.toString();
	}
	@JsonIgnore
	public DateTimeFormatter getDateTimeFormatter() {
		final var formatterBuilder = new DateTimeFormatterBuilder().appendPattern(getDateTimeFormat());
		PartialDate.DEFAULT_FIELD_VALUES.forEach(formatterBuilder::parseDefaulting);

		return formatterBuilder
			.toFormatter()
			.withZone(ZoneId.systemDefault())
			.withResolverStyle(ResolverStyle.STRICT);
	}

	@JsonIgnore
	public final String getExportColumnLabel() {
		return StringUtils.defaultIfBlank(exportLabel, id);
	}
}
