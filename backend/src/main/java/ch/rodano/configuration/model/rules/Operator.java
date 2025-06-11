package ch.rodano.configuration.model.rules;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.PartialDate;
import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.utils.DisplayableUtils;

public enum Operator {
	EQUALS {
		@Override
		public String toSql(final FieldModel fieldModel, final String column) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toSql(final FieldModel fieldModel, final String column, final String value) {
			return String.format("%s = %s", column, parseValue(fieldModel, value));
		}

		@Override
		public Map<String, String> getLabels() {
			final Map<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Equals to");
			shortname.put(LanguageStatic.fr.getId(), "Egal à");
			return shortname;
		}

		@Override
		public boolean hasValue() {
			return true;
		}

		//one value
		@Override
		public boolean test(final String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final PartialDate value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Number value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Boolean value) {
			throw new UnsupportedOperationException();
		}

		//two values
		@Override
		public boolean test(final String value1, final String value2) {
			return value1 != null && value1.equals(value2);
		}

		@Override
		public boolean test(final PartialDate value1, final PartialDate value2) {
			return value1 != null && value1.equals(value2);
		}

		@Override
		public boolean test(final Number value1, final Number value2) {
			return value1 == null && value2 == null || value1 != null && value2 != null && value1.doubleValue() == value2.doubleValue();
		}

		@Override
		public boolean test(final Boolean value1, final Boolean value2) {
			return value1 != null && value1.equals(value2);
		}

	},
	NOT_EQUALS {

		@Override
		public String toSql(final FieldModel fieldModel, final String column) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toSql(final FieldModel fieldModel, final String column, final String value) {
			return String.format("%s != %s", column, parseValue(fieldModel, value));
		}

		@Override
		public Map<String, String> getLabels() {
			final Map<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Not equals to");
			shortname.put(LanguageStatic.fr.getId(), "Différent de");
			return shortname;
		}

		@Override
		public boolean hasValue() {
			return true;
		}

		@Override
		public boolean isNegate() {
			return true;
		}

		//one value
		@Override
		public boolean test(final String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final PartialDate value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Number value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Boolean value) {
			throw new UnsupportedOperationException();
		}

		//two values
		@Override
		public boolean test(final String value1, final String value2) {
			return value1 == null && value2 == null || value1 != null && !value1.equals(value2);
		}

		@Override
		public boolean test(final PartialDate value1, final PartialDate value2) {
			return value1 == null && value2 == null || value1 != null && !value1.equals(value2);
		}

		@Override
		public boolean test(final Number value1, final Number value2) {
			return value1 == null && value2 != null || value1 != null && value2 == null || value1 != null && value2 != null && value1.doubleValue() != value2.doubleValue();
		}

		@Override
		public boolean test(final Boolean value1, final Boolean value2) {
			return value1 == null && value2 == null || value1 != null && !value1.equals(value2);
		}
	},
	CONTAINS {

		@Override
		public String toSql(final FieldModel fieldModel, final String column) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toSql(final FieldModel fieldModel, final String column, final String value) {
			return String.format("%s LIKE '%%%s%%'", column, value);
		}

		@Override
		public Map<String, String> getLabels() {
			final Map<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Contains");
			shortname.put(LanguageStatic.fr.getId(), "Contient");
			return shortname;
		}

		@Override
		public boolean hasValue() {
			return true;
		}

		//one value
		@Override
		public boolean test(final String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final PartialDate value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Number value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Boolean value) {
			throw new UnsupportedOperationException();
		}

		//two values
		@Override
		public boolean test(final String value1, final String value2) {
			return value1.contains(value2);
		}

		@Override
		public boolean test(final PartialDate value1, final PartialDate value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Number value1, final Number value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Boolean value1, final Boolean value2) {
			throw new UnsupportedOperationException();
		}
	},
	NOT_CONTAINS {
		@Override
		public String toSql(final FieldModel fieldModel, final String column) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toSql(final FieldModel fieldModel, final String column, final String value) {
			return String.format("%s NOT LIKE '%%%s%%'", column, value);
		}

		@Override
		public Map<String, String> getLabels() {
			final Map<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Contains not");
			shortname.put(LanguageStatic.fr.getId(), "Ne contient pas");
			return shortname;
		}

		@Override
		public boolean hasValue() {
			return true;
		}

		@Override
		public boolean isNegate() {
			return true;
		}

		//one value
		@Override
		public boolean test(final String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final PartialDate value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Number value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Boolean value) {
			throw new UnsupportedOperationException();
		}

		//two values
		@Override
		public boolean test(final String value1, final String value2) {
			return !value1.contains(value2);
		}

		@Override
		public boolean test(final PartialDate value1, final PartialDate value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Number value1, final Number value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Boolean value1, final Boolean value2) {
			throw new UnsupportedOperationException();
		}
	},
	GREATER {
		@Override
		public String toSql(final FieldModel fieldModel, final String column) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toSql(final FieldModel fieldModel, final String column, final String value) {
			return String.format("%s > %s", column, parseValue(fieldModel, value));
		}

		@Override
		public Map<String, String> getLabels() {
			final Map<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Greater than");
			shortname.put(LanguageStatic.fr.getId(), "Plus grand que");
			return shortname;
		}

		@Override
		public boolean hasValue() {
			return true;
		}

		//one value
		@Override
		public boolean test(final String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final PartialDate value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Number value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Boolean value) {
			throw new UnsupportedOperationException();
		}

		//two values
		@Override
		public boolean test(final String value1, final String value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final PartialDate value1, final PartialDate value2) {
			if(value1 == null || value2 == null) {
				return false;
			}
			return value1.after(value2);
		}

		@Override
		public boolean test(final Number value1, final Number value2) {
			if(value1 == null || value2 == null) {
				return false;
			}
			return value1.doubleValue() > value2.doubleValue();
		}

		@Override
		public boolean test(final Boolean value1, final Boolean value2) {
			throw new UnsupportedOperationException();
		}
	},
	GREATER_EQUALS {
		@Override
		public String toSql(final FieldModel fieldModel, final String column) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toSql(final FieldModel fieldModel, final String column, final String value) {
			return String.format("%s >= %s", column, parseValue(fieldModel, value));
		}

		@Override
		public Map<String, String> getLabels() {
			final Map<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Greater or equals to");
			shortname.put(LanguageStatic.fr.getId(), "Plus grand ou égal à");
			return shortname;
		}

		@Override
		public boolean hasValue() {
			return true;
		}

		//one value
		@Override
		public boolean test(final String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final PartialDate value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Number value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Boolean value) {
			throw new UnsupportedOperationException();
		}

		//two values
		@Override
		public boolean test(final String value1, final String value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final PartialDate value1, final PartialDate value2) {
			if(value1 == null || value2 == null) {
				return false;
			}
			return value1.equals(value2) || value1.after(value2);
		}

		@Override
		public boolean test(final Number value1, final Number value2) {
			if(value1 == null || value2 == null) {
				return false;
			}
			return value1.doubleValue() >= value2.doubleValue();
		}

		@Override
		public boolean test(final Boolean value1, final Boolean value2) {
			throw new UnsupportedOperationException();
		}
	},
	LOWER {
		@Override
		public String toSql(final FieldModel fieldModel, final String column) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toSql(final FieldModel fieldModel, final String column, final String value) {
			return String.format("%s < %s", column, parseValue(fieldModel, value));
		}

		@Override
		public Map<String, String> getLabels() {
			final Map<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Lower than");
			shortname.put(LanguageStatic.fr.getId(), "Plus petit que");
			return shortname;
		}

		@Override
		public boolean hasValue() {
			return true;
		}

		//one value
		@Override
		public boolean test(final String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final PartialDate value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Number value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Boolean value) {
			throw new UnsupportedOperationException();
		}

		//two values
		@Override
		public boolean test(final String value1, final String value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final PartialDate value1, final PartialDate value2) {
			if(value1 == null || value2 == null) {
				return false;
			}
			return value1.before(value2);
		}

		@Override
		public boolean test(final Number value1, final Number value2) {
			if(value1 == null || value2 == null) {
				return false;
			}
			return value1.doubleValue() < value2.doubleValue();
		}

		@Override
		public boolean test(final Boolean value1, final Boolean value2) {
			throw new UnsupportedOperationException();
		}
	},
	LOWER_EQUALS {
		@Override
		public String toSql(final FieldModel fieldModel, final String column) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toSql(final FieldModel fieldModel, final String column, final String value) {
			return String.format("%s <= %s", column, parseValue(fieldModel, value));
		}

		@Override
		public Map<String, String> getLabels() {
			final Map<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Lower or equals to");
			shortname.put(LanguageStatic.fr.getId(), "Plus petit ou égal à");
			return shortname;
		}

		@Override
		public boolean hasValue() {
			return true;
		}

		//one value
		@Override
		public boolean test(final String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final PartialDate value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Number value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Boolean value) {
			throw new UnsupportedOperationException();
		}

		//two values
		@Override
		public boolean test(final String value1, final String value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final PartialDate value1, final PartialDate value2) {
			if(value1 == null || value2 == null) {
				return false;
			}
			return value1.equals(value2) || value1.before(value2);
		}

		@Override
		public boolean test(final Number value1, final Number value2) {
			if(value1 == null || value2 == null) {
				return false;
			}
			return value1.doubleValue() <= value2.doubleValue();
		}

		@Override
		public boolean test(final Boolean value1, final Boolean value2) {
			throw new UnsupportedOperationException();
		}
	},
	NULL {
		@Override
		public String toSql(final FieldModel fieldModel, final String column) {
			return String.format("%s IS NULL", column);
		}

		@Override
		public String toSql(final FieldModel fieldModel, final String column, final String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<String, String> getLabels() {
			final Map<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Is null");
			shortname.put(LanguageStatic.fr.getId(), "Est nul");
			return shortname;
		}

		@Override
		public boolean hasValue() {
			return false;
		}

		//one value
		@Override
		public boolean test(final String value) {
			return value == null;
		}

		@Override
		public boolean test(final PartialDate value) {
			return value == null;
		}

		@Override
		public boolean test(final Number value) {
			return value == null;
		}

		@Override
		public boolean test(final Boolean value) {
			return value == null;
		}

		//two values
		@Override
		public boolean test(final String value1, final String value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final PartialDate value1, final PartialDate value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Number value1, final Number value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Boolean value1, final Boolean value2) {
			throw new UnsupportedOperationException();
		}

	},
	NOT_NULL {
		@Override
		public String toSql(final FieldModel fieldModel, final String column) {
			return String.format("%s IS NOT NULL", column);
		}

		@Override
		public String toSql(final FieldModel fieldModel, final String column, final String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<String, String> getLabels() {
			final Map<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Is not null");
			shortname.put(LanguageStatic.fr.getId(), "N'est pas nul");
			return shortname;
		}

		@Override
		public boolean hasValue() {
			return false;
		}

		//one value
		@Override
		public boolean test(final String value) {
			return value != null;
		}

		@Override
		public boolean test(final PartialDate value) {
			return value != null;
		}

		@Override
		public boolean test(final Number value) {
			return value != null;
		}

		@Override
		public boolean test(final Boolean value) {
			return value != null;
		}

		//two values
		@Override
		public boolean test(final String value1, final String value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final PartialDate value1, final PartialDate value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Number value1, final Number value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Boolean value1, final Boolean value2) {
			throw new UnsupportedOperationException();
		}
	},
	BLANK {
		@Override
		public String toSql(final FieldModel fieldModel, final String column) {
			return String.format("(%s IS NULL OR %s LIKE '')", fieldModel.getId());
		}

		@Override
		public String toSql(final FieldModel fieldModel, final String column, final String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<String, String> getLabels() {
			final Map<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Is blank");
			shortname.put(LanguageStatic.fr.getId(), "Est nul ou vide");
			return shortname;
		}

		@Override
		public boolean hasValue() {
			return false;
		}

		//one value
		@Override
		public boolean test(final String value) {
			return StringUtils.isBlank(value);
		}

		@Override
		public boolean test(final PartialDate value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Number value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Boolean value) {
			throw new UnsupportedOperationException();
		}

		//two values
		@Override
		public boolean test(final String value1, final String value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final PartialDate value1, final PartialDate value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Number value1, final Number value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Boolean value1, final Boolean value2) {
			throw new UnsupportedOperationException();
		}

	},
	NOT_BLANK {
		@Override
		public String toSql(final FieldModel fieldModel, final String column) {
			return String.format("(%s IS NOT NULL AND %s NOT LIKE '')", fieldModel.getId());
		}

		@Override
		public String toSql(final FieldModel fieldModel, final String column, final String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<String, String> getLabels() {
			final Map<String, String> shortname = new TreeMap<>();
			shortname.put(LanguageStatic.en.getId(), "Is not blank");
			shortname.put(LanguageStatic.fr.getId(), "N'est pas null ou vide");
			return shortname;
		}

		@Override
		public boolean hasValue() {
			return false;
		}

		//one value
		@Override
		public boolean test(final String value) {
			return StringUtils.isNotBlank(value);
		}

		@Override
		public boolean test(final PartialDate value) {
			return value != null;
		}

		@Override
		public boolean test(final Number value) {
			return value != null;
		}

		@Override
		public boolean test(final Boolean value) {
			return value != null;
		}

		//two values
		@Override
		public boolean test(final String value1, final String value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final PartialDate value1, final PartialDate value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Number value1, final Number value2) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean test(final Boolean value1, final Boolean value2) {
			throw new UnsupportedOperationException();
		}
	};

	public static String parseValue(final FieldModel fieldModel, final String value) {
		switch(fieldModel.getDataType()) {
			case DATE: {
				final var formatter = new StringBuilder();
				final StringBuilder format = new StringBuilder();
				if(fieldModel.isWithDays()) {
					format.append("%d");
				}
				if(fieldModel.isWithMonths()) {
					if(!format.isEmpty()) {
						format.append(".");
					}
					format.append("%m");
				}
				if(fieldModel.isWithYears()) {
					if(!format.isEmpty()) {
						format.append(".");
					}
					format.append("%Y");
				}
				if(fieldModel.isWithHours()) {
					if(!format.isEmpty()) {
						format.append(" ");
					}
					format.append("%H");
				}
				if(fieldModel.isWithMinutes()) {
					if(!format.isEmpty()) {
						format.append(":");
					}
					format.append("%i");
				}
				if(fieldModel.isWithSeconds()) {
					if(!format.isEmpty()) {
						format.append(":");
					}
					format.append("%s");
				}
				return String.format("STR_TO_DATE('%s', '%s')", value, formatter);
			}
			case STRING:
				return String.format("'%s'", value);
			default:
				return value;
		}
	}

	public abstract String toSql(FieldModel fieldModel, String column);

	public abstract String toSql(FieldModel fieldModel, String column, String value);

	//label
	public abstract Map<String, String> getLabels();

	public String getLocalizedLabel(final String... languages) {
		return DisplayableUtils.getLocalizedMap(getLabels(), languages);
	}

	public boolean isNegate() {
		return false;
	}

	//test
	public abstract boolean hasValue();

	public abstract boolean test(String value);

	public abstract boolean test(PartialDate value);

	public abstract boolean test(Number value);

	public abstract boolean test(Boolean value);

	public abstract boolean test(String value1, String value2);

	public abstract boolean test(PartialDate value1, PartialDate value2);

	public abstract boolean test(Number value1, Number value2);

	public abstract boolean test(Boolean value1, Boolean value2);

	public boolean test(final OperandType operandType, final Object value1, final Object value2) {
		switch(operandType) {
			case DATE:
				return test(PartialDate.ofObject(value1), PartialDate.ofObject(value2));
			case NUMBER:
				return test((Number) value1, (Number) value2);
			case BOOLEAN:
				return test((Boolean) value1, (Boolean) value2);
			default:
				return test((String) value1, (String) value2);
		}
	}

	public boolean test(final OperandType operandType, final Object value) {
		switch(operandType) {
			case DATE:
				return test(PartialDate.ofObject(value));
			case NUMBER:
				return test((Number) value);
			case BOOLEAN:
				return test((Boolean) value);
			default:
				return test((String) value);
		}
	}
}
