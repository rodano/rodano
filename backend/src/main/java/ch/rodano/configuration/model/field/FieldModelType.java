package ch.rodano.configuration.model.field;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.rodano.configuration.model.common.SuperDisplayable;
import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.configuration.model.rules.Operator;


public enum FieldModelType implements SuperDisplayable {
	STRING {
		@Override
		public boolean isSizable() {
			return true;
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "String");
			shortname.put(LanguageStatic.fr.getId(), "Texte");
			return shortname;
		}

		@Override
		public Set<OperandType> getOperandTypes() {
			return Collections.singleton(OperandType.STRING);
		}

		@Override
		public List<Operator> getOperators() {
			return Arrays.asList(Operator.EQUALS, Operator.NOT_EQUALS, Operator.CONTAINS, Operator.NOT_CONTAINS, Operator.NULL, Operator.NOT_NULL);
		}
	},
	AUTO_COMPLETION {
		@Override
		public boolean isMultipleChoice() {
			return false;
		}

		@Override
		public boolean isSizable() {
			return true;
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Autocompleted string");
			shortname.put(LanguageStatic.fr.getId(), "Texte autocompleté");
			return shortname;
		}

		@Override
		public Set<OperandType> getOperandTypes() {
			return Collections.singleton(OperandType.STRING);
		}

		@Override
		public List<Operator> getOperators() {
			return Arrays.asList(Operator.EQUALS, Operator.NOT_EQUALS, Operator.CONTAINS, Operator.NOT_CONTAINS, Operator.NULL, Operator.NOT_NULL);
		}
	},
	DATE {
		@Override
		public boolean hasDefaultMaxLength() {
			return false;
		}

		@Override
		public int getDefaultMaxLength() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getSQL() {
			return "datetime";
		}

		@Override
		public boolean hasFormat() {
			return true;
		}

		@Override
		public boolean isSizable() {
			return true;
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Date");
			shortname.put(LanguageStatic.fr.getId(), "Date");
			return shortname;
		}

		@Override
		public Set<OperandType> getOperandTypes() {
			return Collections.singleton(OperandType.DATE);
		}

		@Override
		public List<Operator> getOperators() {
			return Arrays.asList(Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER, Operator.GREATER_EQUALS, Operator.LOWER, Operator.LOWER_EQUALS, Operator.NULL, Operator.NOT_NULL);
		}
	},
	DATE_SELECT {
		@Override
		public boolean hasDefaultMaxLength() {
			return true;
		}

		@Override
		public int getDefaultMaxLength() {
			//max length is unknown.unknown.unknown
			return 23;
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Date selection");
			shortname.put(LanguageStatic.fr.getId(), "Date par selection");
			return shortname;
		}

		@Override
		public Set<OperandType> getOperandTypes() {
			return Collections.singleton(OperandType.DATE);
		}

		@Override
		public List<Operator> getOperators() {
			return Arrays.asList(Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER, Operator.GREATER_EQUALS, Operator.LOWER, Operator.LOWER_EQUALS, Operator.NULL, Operator.NOT_NULL);
		}
	},
	NUMBER {
		@Override
		public boolean hasDefaultMaxLength() {
			return false;
		}

		@Override
		public int getDefaultMaxLength() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getSQL() {
			return "double";
		}

		@Override
		public boolean hasFormat() {
			return true;
		}

		@Override
		public boolean isSizable() {
			return true;
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Number");
			shortname.put(LanguageStatic.fr.getId(), "Nombre");
			return shortname;
		}

		@Override
		public Set<OperandType> getOperandTypes() {
			return Collections.singleton(OperandType.NUMBER);
		}

		@Override
		public List<Operator> getOperators() {
			return Arrays.asList(Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER, Operator.GREATER_EQUALS, Operator.LOWER, Operator.LOWER_EQUALS, Operator.NULL, Operator.NOT_NULL);
		}
	},
	SELECT {
		@Override
		public boolean isMultipleChoice() {
			return true;
		}

		@Override
		public int getDefaultMaxLength() {
			return 80;
		}

		@Override
		public String getSQL() {
			return "varchar(%d)";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Combobox");
			shortname.put(LanguageStatic.fr.getId(), "Zone de liste");
			return shortname;
		}

		@Override
		public Set<OperandType> getOperandTypes() {
			return Collections.singleton(OperandType.STRING);
		}

		@Override
		public List<Operator> getOperators() {
			return Arrays.asList(Operator.EQUALS, Operator.NOT_EQUALS, Operator.NULL, Operator.NOT_NULL);
		}
	},
	RADIO {
		@Override
		public boolean isMultipleChoice() {
			return true;
		}

		@Override
		public int getDefaultMaxLength() {
			return 100;
		}

		@Override
		public String getSQL() {
			return "varchar(%d)";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Radio button");
			shortname.put(LanguageStatic.fr.getId(), "Bouton radio");
			return shortname;
		}

		@Override
		public Set<OperandType> getOperandTypes() {
			return Collections.singleton(OperandType.STRING);
		}

		@Override
		public List<Operator> getOperators() {
			return Arrays.asList(Operator.EQUALS, Operator.NOT_EQUALS, Operator.NULL, Operator.NOT_NULL);
		}
	},
	CHECKBOX {
		@Override
		public boolean hasVisibilityCriteria() {
			return true;
		}

		@Override
		public boolean hasDefaultMaxLength() {
			return true;
		}

		@Override
		public int getDefaultMaxLength() {
			return 5;
		}

		@Override
		public String getSQL() {
			return "varchar(%d)";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Checkbox");
			shortname.put(LanguageStatic.fr.getId(), "Case à cocher");
			return shortname;
		}

		@Override
		public Set<OperandType> getOperandTypes() {
			return Collections.singleton(OperandType.BOOLEAN);
		}

		@Override
		public List<Operator> getOperators() {
			return Collections.singletonList(Operator.EQUALS);
		}
	},
	CHECKBOX_GROUP {
		@Override
		public boolean isMultipleChoice() {
			return true;
		}

		@Override
		public boolean hasMultipleValues() {
			return true;
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Checkbox group");
			shortname.put(LanguageStatic.fr.getId(), "Groupe de cases à cocher");
			return shortname;
		}

		@Override
		public Set<OperandType> getOperandTypes() {
			return Collections.singleton(OperandType.STRING);
		}

		@Override
		public List<Operator> getOperators() {
			return Arrays.asList(Operator.EQUALS, Operator.NOT_EQUALS, Operator.CONTAINS, Operator.NOT_CONTAINS, Operator.NULL, Operator.NOT_NULL);
		}
	},
	TEXTAREA {
		@Override
		public boolean isSizable() {
			return true;
		}

		@Override
		public boolean hasDefaultMaxLength() {
			return false;
		}

		@Override
		public int getDefaultMaxLength() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getSQL() {
			return "text";
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "Text area");
			shortname.put(LanguageStatic.fr.getId(), "Zone de texte");
			return shortname;
		}

		@Override
		public Set<OperandType> getOperandTypes() {
			return Collections.singleton(OperandType.STRING);
		}

		@Override
		public List<Operator> getOperators() {
			return Arrays.asList(Operator.EQUALS, Operator.NOT_EQUALS, Operator.CONTAINS, Operator.NOT_CONTAINS, Operator.NULL, Operator.NOT_NULL);
		}
	},
	FILE {
		@Override
		public boolean isSizable() {
			return true;
		}

		@Override
		public Map<String, String> getShortname() {
			final Map<String, String> shortname = new HashMap<>();
			shortname.put(LanguageStatic.en.getId(), "File");
			shortname.put(LanguageStatic.fr.getId(), "Fichier");
			return shortname;
		}

		@Override
		public Set<OperandType> getOperandTypes() {
			return Collections.emptySet();
		}

		@Override
		public List<Operator> getOperators() {
			return Collections.emptyList();
		}
	};

	public boolean hasVisibilityCriteria() {
		return isMultipleChoice();
	}

	public boolean hasFormat() {
		return false;
	}

	public boolean isMultipleChoice() {
		return false;
	}

	public boolean hasMultipleValues() {
		return false;
	}

	public boolean isSizable() {
		return false;
	}

	public boolean hasDefaultMaxLength() {
		return true;
	}

	public int getDefaultMaxLength() {
		return FieldModel.MAX_LENGTH;
	}

	public String getSQL() {
		return "varchar(%d)";
	}

	public final String toSQL(final Integer maxLength) {
		return String.format(getSQL(), maxLength);
	}

	public abstract Set<OperandType> getOperandTypes();

	public abstract List<Operator> getOperators();

	@Override
	public final String getId() {
		return name();
	}

	@Override
	public abstract Map<String, String> getShortname();

	@Override
	public Map<String, String> getLongname() {
		return getShortname();
	}

	@Override
	public Map<String, String> getDescription() {
		return getShortname();
	}
}
