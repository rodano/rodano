package ch.rodano.batch.pojo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ch.rodano.core.model.jooq.enums.FieldModelDataType;
import ch.rodano.core.model.jooq.enums.FieldModelType;


@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldModel {

	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;
	private Map<String, String> matcherMessage;
	private Map<String, String> advancedHelp;

	private FieldModelType type;
	private FieldModelDataType dataType;
	private String valueFormula;
	private boolean plugin;
	private boolean searchable;
	private boolean readOnly;
	private boolean exportable;
	private boolean allowDateInFuture;
	private Integer exportOrder;
	private Integer maxLength;
	private Integer maxIntegerDigits;
	private Integer maxDecimalDigits;
	private BigDecimal minValue;
	private BigDecimal maxValue;
	private Integer minYear;
	private String dictionary;
	private String inlineHelp;
	private boolean withYears;
	private boolean withMonths;
	private boolean withDays;
	private boolean withHours;
	private boolean withMinutes;
	private boolean withSeconds;
	private boolean yearsMandatory;
	private boolean monthsMandatory;
	private boolean daysMandatory;
	private boolean hoursMandatory;
	private boolean minutesMandatory;
	private boolean secondsMandatory;
	private String possibleValuesProvider;
	private String possibleValuesProviderDescription;
	private String matcher;

	private List<PossibleValue> possibleValues;
	private List<String> validatorIds;
	private List<String> workflowIds;
	private List<Rule> rules;

	private RuleConstraint constraint;
	private RuleConstraint valueConstraint;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
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

	public Map<String, String> getMatcherMessage() {
		return matcherMessage;
	}

	public void setMatcherMessage(final Map<String, String> matcherMessage) {
		this.matcherMessage = matcherMessage;
	}

	public Map<String, String> getAdvancedHelp() {
		return advancedHelp;
	}

	public void setAdvancedHelp(final Map<String, String> advancedHelp) {
		this.advancedHelp = advancedHelp;
	}

	public FieldModelType getType() {
		return type;
	}

	public void setType(final FieldModelType type) {
		this.type = type;
	}

	public FieldModelDataType getDataType() {
		return dataType;
	}

	public void setDataType(final FieldModelDataType dataType) {
		this.dataType = dataType;
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

	public boolean isSearchable() {
		return searchable;
	}

	public void setSearchable(final boolean searchable) {
		this.searchable = searchable;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isExportable() {
		return exportable;
	}

	public void setExportable(final boolean exportable) {
		this.exportable = exportable;
	}

	public boolean isAllowDateInFuture() {
		return allowDateInFuture;
	}

	public void setAllowDateInFuture(final boolean allowDateInFuture) {
		this.allowDateInFuture = allowDateInFuture;
	}

	public Integer getExportOrder() {
		return exportOrder;
	}

	public void setExportOrder(final Integer exportOrder) {
		this.exportOrder = exportOrder;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(final Integer maxLength) {
		this.maxLength = maxLength;
	}

	public Integer getMaxIntegerDigits() {
		return maxIntegerDigits;
	}

	public void setMaxIntegerDigits(final Integer maxIntegerDigits) {
		this.maxIntegerDigits = maxIntegerDigits;
	}

	public Integer getMaxDecimalDigits() {
		return maxDecimalDigits;
	}

	public void setMaxDecimalDigits(final Integer maxDecimalDigits) {
		this.maxDecimalDigits = maxDecimalDigits;
	}

	public BigDecimal getMinValue() {
		return minValue;
	}

	public void setMinValue(final BigDecimal minValue) {
		this.minValue = minValue;
	}

	public BigDecimal getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(final BigDecimal maxValue) {
		this.maxValue = maxValue;
	}

	public Integer getMinYear() {
		return minYear;
	}

	public void setMinYear(final Integer minYear) {
		this.minYear = minYear;
	}

	public String getDictionary() {
		return dictionary;
	}

	public void setDictionary(final String dictionary) {
		this.dictionary = dictionary;
	}

	public String getInlineHelp() {
		return inlineHelp;
	}

	public void setInlineHelp(final String inlineHelp) {
		this.inlineHelp = inlineHelp;
	}

	public boolean isWithYears() {
		return withYears;
	}

	public void setWithYears(final boolean withYears) {
		this.withYears = withYears;
	}

	public boolean isWithMonths() {
		return withMonths;
	}

	public void setWithMonths(final boolean withMonths) {
		this.withMonths = withMonths;
	}

	public boolean isWithDays() {
		return withDays;
	}

	public void setWithDays(final boolean withDays) {
		this.withDays = withDays;
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

	public boolean isYearsMandatory() {
		return yearsMandatory;
	}

	public void setYearsMandatory(final boolean yearsMandatory) {
		this.yearsMandatory = yearsMandatory;
	}

	public boolean isMonthsMandatory() {
		return monthsMandatory;
	}

	public void setMonthsMandatory(final boolean monthsMandatory) {
		this.monthsMandatory = monthsMandatory;
	}

	public boolean isDaysMandatory() {
		return daysMandatory;
	}

	public void setDaysMandatory(final boolean daysMandatory) {
		this.daysMandatory = daysMandatory;
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

	public List<PossibleValue> getPossibleValues() {
		return possibleValues;
	}

	public void setPossibleValues(final List<PossibleValue> possibleValues) {
		this.possibleValues = possibleValues;
	}

	public List<String> getValidatorIds() {
		return validatorIds;
	}

	public void setValidatorIds(final List<String> validatorIds) {
		this.validatorIds = validatorIds;
	}

	public List<String> getWorkflowIds() {
		return workflowIds;
	}

	public void setWorkflowIds(final List<String> workflowIds) {
		this.workflowIds = workflowIds;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(final List<Rule> rules) {
		this.rules = rules;
	}

	public RuleConstraint getConstraint() {
		return constraint;
	}

	public void setConstraint(final RuleConstraint constraint) {
		this.constraint = constraint;
	}

	public RuleConstraint getValueConstraint() {
		return valueConstraint;
	}

	public void setValueConstraint(final RuleConstraint valueConstraint) {
		this.valueConstraint = valueConstraint;
	}

	@Override
	public String toString() {
		return "FieldModel{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", matcherMessage=" + matcherMessage +
			", advancedHelp=" + advancedHelp +
			", type='" + type + '\'' +
			", dataType='" + dataType + '\'' +
			", valueFormula='" + valueFormula + '\'' +
			", plugin=" + plugin +
			", searchable=" + searchable +
			", readOnly=" + readOnly +
			", exportable=" + exportable +
			", allowDateInFuture=" + allowDateInFuture +
			", exportOrder=" + exportOrder +
			", maxLength=" + maxLength +
			", maxIntegerDigits=" + maxIntegerDigits +
			", maxDecimalDigits=" + maxDecimalDigits +
			", minValue=" + minValue +
			", maxValue=" + maxValue +
			", minYear=" + minYear +
			", dictionary='" + dictionary + '\'' +
			", inlineHelp='" + inlineHelp + '\'' +
			", withYears=" + withYears +
			", withMonths=" + withMonths +
			", withDays=" + withDays +
			", withHours=" + withHours +
			", withMinutes=" + withMinutes +
			", withSeconds=" + withSeconds +
			", yearsMandatory=" + yearsMandatory +
			", monthsMandatory=" + monthsMandatory +
			", daysMandatory=" + daysMandatory +
			", hoursMandatory=" + hoursMandatory +
			", minutesMandatory=" + minutesMandatory +
			", secondsMandatory=" + secondsMandatory +
			", possibleValuesProvider='" + possibleValuesProvider + '\'' +
			", possibleValuesProviderDescription='" + possibleValuesProviderDescription + '\'' +
			", matcher='" + matcher + '\'' +
			", possibleValues=" + possibleValues +
			", validatorIds=" + validatorIds +
			", workflowIds=" + workflowIds +
			", rules=" + rules +
			", constraint=" + constraint +
			", valueConstraint=" + valueConstraint +
			'}';
	}
}
