package ch.rodano.api.config;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.rules.OperandType;

public class FieldModelDTO implements Comparable<FieldModelDTO> {
	@NotNull
	private UUID fieldModelId;
	@Schema(description = "Field type")
	@NotNull
	private FieldModelType type;
	@Schema(description = "Field value type for the operands")
	@NotNull
	private OperandType dataType;
	@NotNull
	private UUID datasetModelId;
	@NotBlank
	private String id;

	private List<UUID> validatorIds;
	private List<UUID> workflowIds;

	@NotNull
	private Map<String, String> shortname;
	@NotNull
	private Map<String, String> longname;
	private Map<String, String> description;

	private Map<String, String> matcherMessage;

	@Schema(description = "Possible values of the field, if the field type is a multiple")
	@NotNull
	private List<PossibleValueDTO> possibleValues;
	private String possibleValuesProvider;
	private String possibleValuesProviderDescription;

	private String dictionary;
	private String matcher;
	private String valueFormula;

	@NotNull
	private boolean readOnly;

	@NotNull
	private boolean dynamic;

	private Integer order;
	@Schema(description = "Can the field be found in search")
	@NotNull
	private boolean searchable;

	private boolean plugin;

	//export
	@Schema(description = "Is the field model included in the exports")
	@NotNull
	private boolean exportable;

	private Integer exportOrder;

	@NotNull
	private boolean required;

	//sizable
	private Integer maxLength;

	//help
	private String inlineHelp;
	private Map<String, String> advancedHelp;

	//validation
	private Double minValue;
	private Double maxValue;
	private Integer minYear;
	private Integer maxYear;

	//number
	private int maxIntegerDigits;
	private int maxDecimalDigits;

	//date
	@NotNull
	private boolean allowDateInFuture;
	@NotNull
	private boolean withDays;
	@NotNull
	private boolean withMonths;
	@NotNull
	private boolean withYears;
	@NotNull
	private boolean withHours;
	@NotNull
	private boolean withMinutes;
	@NotNull
	private boolean withSeconds;

	@NotNull
	private boolean yearsMandatory;
	@NotNull
	private boolean monthsMandatory;
	@NotNull
	private boolean daysMandatory;
	@NotNull
	private boolean hoursMandatory;
	@NotNull
	private boolean minutesMandatory;
	@NotNull
	private boolean secondsMandatory;

	/**
	 * Default constructor, needed by some serializer
	 */
	public FieldModelDTO() {

	}

	public FieldModelDTO(final FieldModel fieldModel, final String[] languages) {
		fieldModelId = fieldModel.getFieldModelId();
		datasetModelId = fieldModel.getDatasetModel().getDatasetModelId();
		id = fieldModel.getId();

		type = fieldModel.getType();
		dataType = fieldModel.getDataType();

		shortname = fieldModel.getShortname();
		longname = fieldModel.getLongname();
		description = fieldModel.getDescription();

		possibleValues = fieldModel.getPossibleValues().stream()
			.map(PossibleValueDTO::new)
			.toList();
		dictionary = fieldModel.getDictionary();

		readOnly = fieldModel.isReadOnly();
		dynamic = fieldModel.isPlugin();

		order = fieldModel.getExportOrder();
		searchable = fieldModel.isSearchable();

		exportable = fieldModel.isExportable();

		required = fieldModel.isRequired();

		//sizable
		maxLength = fieldModel.getMaxLength();

		inlineHelp = fieldModel.getInlineHelp();
		advancedHelp = fieldModel.getAdvancedHelp();

		//number
		minValue = fieldModel.getMinValue();
		maxValue = fieldModel.getMaxValue();

		maxIntegerDigits = fieldModel.getMaxIntegerDigits();
		maxDecimalDigits = fieldModel.getMaxDecimalDigits();

		//date
		allowDateInFuture = fieldModel.isAllowDateInFuture();

		//date select
		withDays = fieldModel.isWithDays();
		withMonths = fieldModel.isWithMonths();
		withYears = fieldModel.isWithYears();
		withHours = fieldModel.isWithHours();
		withMinutes = fieldModel.isWithMinutes();
		withSeconds = fieldModel.isWithSeconds();

		yearsMandatory = fieldModel.isYearsMandatory();
		monthsMandatory = fieldModel.isMonthsMandatory();
		daysMandatory = fieldModel.isDaysMandatory();
		hoursMandatory = fieldModel.isHoursMandatory();
		minutesMandatory = fieldModel.isMinutesMandatory();
		secondsMandatory = fieldModel.isSecondsMandatory();

		minYear = fieldModel.getMinYear();
		maxYear = fieldModel.getMaxYear();
	}

	@Override
	public int compareTo(final FieldModelDTO o) {
		return id.compareTo(o.id);
	}

	public UUID getFieldModelId() {
		return fieldModelId;
	}

	public void setFieldModelId(final UUID fieldModelId) {
		this.fieldModelId = fieldModelId;
	}

	public FieldModelType getType() {
		return type;
	}

	public void setType(final FieldModelType type) {
		this.type = type;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(final Integer order) {
		this.order = order;
	}

	public OperandType getDataType() {
		return dataType;
	}

	public void setDataType(final OperandType dataType) {
		this.dataType = dataType;
	}

	public UUID getDatasetModelId() {
		return datasetModelId;
	}

	public void setDatasetModelId(final UUID datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public List<UUID> getValidatorIds() {
		return validatorIds;
	}

	public void setValidatorIds(final List<UUID> validatorIds) {
		this.validatorIds = validatorIds;
	}

	public List<UUID> getWorkflowIds() {
		return workflowIds;
	}

	public void setWorkflowIds(final List<UUID> workflowIds) {
		this.workflowIds = workflowIds;
	}

	public Map<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final Map<String, String> shortname) {
		this.shortname = shortname;
	}

	public Map<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final Map<String, String> longname) {
		this.longname = longname;
	}

	public Map<String, String> getDescription() {
		return description;
	}

	public void setDescription(final Map<String, String> description) {
		this.description = description;
	}

	public List<PossibleValueDTO> getPossibleValues() {
		return possibleValues;
	}

	public void setPossibleValues(final List<PossibleValueDTO> possibleValues) {
		this.possibleValues = possibleValues;
	}

	public String getDictionary() {
		return dictionary;
	}

	public void setDictionary(final String dictionary) {
		this.dictionary = dictionary;
	}

	public final boolean isRequired() {
		return required;
	}

	public final void setRequired(final boolean required) {
		this.required = required;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(final Integer maxLength) {
		this.maxLength = maxLength;
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

	public Double getMinValue() {
		return minValue;
	}

	public void setMinValue(final Double minValue) {
		this.minValue = minValue;
	}

	public Double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(final Double maxValue) {
		this.maxValue = maxValue;
	}

	public Integer getMinYear() {
		return minYear;
	}

	public void setMinYear(final Integer minYear) {
		this.minYear = minYear;
	}

	public Integer getMaxYear() {
		return maxYear;
	}

	public void setMaxYear(final Integer maxYear) {
		this.maxYear = maxYear;
	}

	public boolean isAllowDateInFuture() {
		return allowDateInFuture;
	}

	public void setAllowDateInFuture(final boolean allowDateInFuture) {
		this.allowDateInFuture = allowDateInFuture;
	}

	public boolean isWithDays() {
		return withDays;
	}

	public void setWithDays(final boolean withDays) {
		this.withDays = withDays;
	}

	public boolean isWithMonths() {
		return withMonths;
	}

	public void setWithMonths(final boolean withMonths) {
		this.withMonths = withMonths;
	}

	public boolean isWithYears() {
		return withYears;
	}

	public void setWithYears(final boolean withYears) {
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

	public boolean isDaysMandatory() {
		return daysMandatory;
	}

	public void setDaysMandatory(final boolean daysMandatory) {
		this.daysMandatory = daysMandatory;
	}

	public boolean isMonthsMandatory() {
		return monthsMandatory;
	}

	public void setMonthsMandatory(final boolean monthsMandatory) {
		this.monthsMandatory = monthsMandatory;
	}

	public boolean isYearsMandatory() {
		return yearsMandatory;
	}

	public void setYearsMandatory(final boolean yearsMandatory) {
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

	public boolean isSearchable() {
		return searchable;
	}

	public void setSearchable(final boolean searchable) {
		this.searchable = searchable;
	}

	public boolean isExportable() {
		return exportable;
	}

	public void setExportable(final boolean exportable) {
		this.exportable = exportable;
	}

	public boolean isDynamic() {
		return dynamic;
	}

	public void setDynamic(final boolean dynamic) {
		this.dynamic = dynamic;
	}

	public String getInlineHelp() {
		return inlineHelp;
	}

	public void setInlineHelp(final String inlineHelp) {
		this.inlineHelp = inlineHelp;
	}

	public Map<String, String> getAdvancedHelp() {
		return advancedHelp;
	}

	public void setAdvancedHelp(final Map<String, String> advancedHelp) {
		this.advancedHelp = advancedHelp;
	}

	public Map<String, String> getMatcherMessage() {
		return matcherMessage;
	}

	public void setMatcherMessage(final Map<String, String> matcherMessage) {
		this.matcherMessage = matcherMessage;
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

	public String getMatcher() {
		return matcher;
	}

	public void setMatcher(final String matcher) {
		this.matcher = matcher;
	}

	public String getValueFormula() {
		return valueFormula;
	}

	public void setValueFormula(final String valueFormula) {
		this.valueFormula = valueFormula;
	}

	public boolean isPlugin() {
		return plugin;
	}

	public void setPlugin(final boolean plugin) {
		this.plugin = plugin;
	}

	public Integer getExportOrder() {
		return exportOrder;
	}

	public void setExportOrder(final Integer exportOrder) {
		this.exportOrder = exportOrder;
	}
}
