package ch.rodano.core.model.rules.formula;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ch.rodano.configuration.model.field.PartialDate;
import ch.rodano.core.model.rules.formula.exception.FormulaNullParameterException;
import ch.rodano.core.model.rules.formula.exception.FormulaWrongFunctionParametersException;
import ch.rodano.core.model.rules.formula.exception.FormulaWrongParameterTypeException;

//when writing a function, keep in mind that all numbers will have the type Double and all dates will have the type ZonedDateTime
public enum FormulaFunction {
	//date
	CREATE_DATE {
		@Override
		public PartialDate getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 6) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 6);
			}
			final int year = ((Double) parameters.get(0)).intValue();
			final int month = ((Double) parameters.get(1)).intValue();
			final int day = ((Double) parameters.get(2)).intValue();
			final int hour = ((Double) parameters.get(3)).intValue();
			final int minute = ((Double) parameters.get(4)).intValue();
			final int second = ((Double) parameters.get(5)).intValue();
			return PartialDate.of(year, month, day, hour, minute, second);
		}
	},
	TODAY {
		@Override
		public PartialDate getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(!parameters.isEmpty()) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 0);
			}
			return PartialDate.now();
		}
	},
	ADD_DURATION {
		@Override
		public PartialDate getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 7) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 7);
			}
			final var initialDate = (PartialDate) parameters.get(0);
			return initialDate
				.plusYears(((Double) parameters.get(1)).intValue())
				.plusMonths(((Double) parameters.get(2)).intValue())
				.plusDays(((Double) parameters.get(3)).intValue())
				.plusHours(((Double) parameters.get(4)).intValue())
				.plusMinutes(((Double) parameters.get(5)).intValue())
				.plusSeconds(((Double) parameters.get(6)).intValue());
		}
	},
	ADD_YEARS {
		@Override
		public PartialDate getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final var initialDate = (PartialDate) parameters.get(0);
			return initialDate.plusYears(((Double) parameters.get(1)).intValue());
		}
	},
	ADD_MONTHS {
		@Override
		public PartialDate getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final var initialDate = (PartialDate) parameters.get(0);
			return initialDate.plusMonths(((Double) parameters.get(1)).intValue());
		}
	},
	ADD_DAYS {
		@Override
		public PartialDate getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final var initialDate = (PartialDate) parameters.get(0);
			return initialDate.plusDays(((Double) parameters.get(1)).intValue());
		}
	},
	ADD_HOURS {
		@Override
		public PartialDate getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final var initialDate = (PartialDate) parameters.get(0);
			return initialDate.plusHours(((Double) parameters.get(1)).intValue());
		}
	},
	ADD_MINUTES {
		@Override
		public PartialDate getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final var initialDate = (PartialDate) parameters.get(0);
			return initialDate.plusMinutes(((Double) parameters.get(1)).intValue());
		}
	},
	ADD_SECONDS {
		@Override
		public PartialDate getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final var initialDate = (PartialDate) parameters.get(0);
			return initialDate.plusSeconds(((Double) parameters.get(1)).intValue());
		}
	},
	DIFFERENCE_IN_YEARS {
		@Override
		public Double getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final var date1 = (PartialDate) parameters.get(0);
			final var date2 = (PartialDate) parameters.get(1);
			final long difference = ChronoUnit.YEARS.between(date1.toZonedDateTime().get(), date2.toZonedDateTime().get());
			return Long.valueOf(Math.abs(difference)).doubleValue();
		}
	},
	DIFFERENCE_IN_MONTHS {
		@Override
		public Double getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final var date1 = (PartialDate) parameters.get(0);
			final var date2 = (PartialDate) parameters.get(1);
			final long difference = ChronoUnit.MONTHS.between(date1.toZonedDateTime().get(), date2.toZonedDateTime().get());
			return Long.valueOf(Math.abs(difference)).doubleValue();
		}
	},
	DIFFERENCE_IN_DAYS {
		@Override
		public Double getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final var date1 = (PartialDate) parameters.get(0);
			final var date2 = (PartialDate) parameters.get(1);
			final long difference = ChronoUnit.DAYS.between(date1.toZonedDateTime().get(), date2.toZonedDateTime().get());
			return Long.valueOf(Math.abs(difference)).doubleValue();
		}
	},
	DIFFERENCE_IN_SECONDS {
		@Override
		public Double getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final var date1 = (PartialDate) parameters.get(0);
			final var date2 = (PartialDate) parameters.get(1);
			final long difference = ChronoUnit.SECONDS.between(date1.toZonedDateTime().get(), date2.toZonedDateTime().get());
			return Long.valueOf(Math.abs(difference)).doubleValue();
		}
	},
	MONTH_OF_DATE {
		@Override
		public PartialDate getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 1) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 1);
			}
			final var initialDate = (PartialDate) parameters.get(0);
			return initialDate.withDayOfMonth(1);
		}
	},
	//condition
	IF {
		@Override
		public Object getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 3) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 3);
			}
			final var condition = (Boolean) parameters.get(0);
			return condition ? parameters.get(1) : parameters.get(2);
		}
	},
	IS_EQUAL_TO {
		@Override
		public Boolean getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			return parameters.get(0) != null && parameters.get(1) != null && parameters.get(0).equals(parameters.get(1));
		}
	},
	//conversion
	NUMBER_TO_STRING {
		@Override
		public String getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 1) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 1);
			}
			final Double value = (Double) parameters.get(0);
			final long longValue = value.longValue();
			if(Double.valueOf(longValue).equals(value)) {
				return Long.toString(longValue);
			}
			return value.toString();
		}
	},
	STRING_TO_NUMBER {
		@Override
		public Double getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 1) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 1);
			}
			final String value = (String) parameters.get(0);
			return Double.valueOf(value);
		}
	},
	//string
	IS_BLANK {
		@Override
		public Boolean getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 1) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 1);
			}
			return StringUtils.isBlank((String) parameters.get(0));
		}
	},
	CONCAT {
		@Override
		public String getValue(final List<Object> parameters) {
			return parameters.stream()
				.map(FormulaFunction::checkNotNull)
				.map(Object::toString)
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.joining());
		}
	},
	UPPERCASE {
		@Override
		public String getValue(final List<Object> parameters) {
			return StringUtils.upperCase((String) parameters.get(0));
		}
	},
	LOWERCASE {
		@Override
		public String getValue(final List<Object> parameters) {
			return StringUtils.lowerCase((String) parameters.get(0));
		}
	},
	//numbers
	IS_GREATER_THAN {
		@Override
		public Boolean getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() < 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final Double number1 = (Double) parameters.get(0);
			final Double number2 = (Double) parameters.get(1);
			return number1 > number2;
		}
	},
	IS_GREATER_OR_EQUAL_TO {
		@Override
		public Boolean getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() < 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final Double number1 = (Double) parameters.get(0);
			final Double number2 = (Double) parameters.get(1);
			return number1 >= number2;
		}
	},
	IS_LESS_THAN {
		@Override
		public Boolean getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() < 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final Double number1 = (Double) parameters.get(0);
			final Double number2 = (Double) parameters.get(1);
			return number1 < number2;
		}
	},
	IS_LESS_OR_EQUAL_TO {
		@Override
		public Boolean getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() < 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final Double number1 = (Double) parameters.get(0);
			final Double number2 = (Double) parameters.get(1);
			return number1 <= number2;
		}
	},
	SUM {
		@Override
		public Double getValue(final List<Object> parameters) {
			//do not use the sum method from DoubleStream
			//it will not return accurate results
			return parameters.stream()
				.map(FormulaFunction::checkNotNull)
				.map(FormulaFunction::toBigDecimal)
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.doubleValue();
		}
	},
	MULTIPLY {
		@Override
		public Double getValue(final List<Object> parameters) {
			//do not use the multiply method from DoubleStream
			//it will not return accurate results
			return parameters.stream()
				.map(FormulaFunction::checkNotNull)
				.map(FormulaFunction::toBigDecimal)
				.reduce(BigDecimal.ONE, BigDecimal::multiply)
				.doubleValue();
		}
	},
	SUBTRACT {
		@Override
		public Double getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() < 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			//do not use the subtract directly the doubles
			//it will not return accurate results
			return parameters.stream()
				.map(FormulaFunction::checkNotNull)
				.map(FormulaFunction::toBigDecimal)
				.reduce((a, b) -> a.subtract(b, MathContext.DECIMAL128))
				.orElseThrow()
				.doubleValue();
		}
	},
	DIVIDE {
		@Override
		public Double getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() < 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			//do not use the divide directly the doubles
			//it will not return accurate results
			return parameters.stream()
				.map(FormulaFunction::checkNotNull)
				.map(FormulaFunction::toBigDecimal)
				.reduce((a, b) -> a.divide(b, MathContext.DECIMAL128))
				.orElseThrow()
				.doubleValue();
		}
	},
	POWER {
		@Override
		public Double getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final Double base = (Double) parameters.get(0);
			final var exponent = (Double) parameters.get(1);
			return Math.pow(base, exponent);
		}
	},
	SQRT {
		@Override
		public Double getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 1) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 1);
			}
			final Double number = (Double) parameters.get(0);
			return Math.sqrt(number);
		}
	},
	INVERSE {
		@Override
		public Double getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 1) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 1);
			}
			final var number = BigDecimal.valueOf((Double) parameters.get(0));
			return BigDecimal.ONE.divide(number).doubleValue();
		}
	},
	AVERAGE {
		@Override
		public Double getValue(final List<Object> parameters) {
			return parameters.stream()
				.filter(Objects::nonNull)
				.mapToDouble(p -> (Double) p)
				.average()
				.orElseThrow();
		}
	},
	MEDIAN {
		@Override
		public Double getValue(final List<Object> parameters) {
			final var values = parameters.stream()
				.filter(Objects::nonNull)
				.mapToDouble(p -> (Double) p)
				.sorted()
				.boxed()
				.toList();
			final var length = values.size();
			if(length % 2 == 0) {
				return (values.get(length / 2) + values.get(length / 2 - 1)) / 2;
			}
			return values.get(length / 2);
		}
	},
	MIN {
		@Override
		public Double getValue(final List<Object> parameters) {
			return parameters.stream()
				.map(FormulaFunction::checkNotNull)
				.mapToDouble(p -> (Double) p)
				.min()
				.orElse(Double.NaN);
		}
	},
	MAX {
		@Override
		public Double getValue(final List<Object> parameters) {
			return parameters.stream()
				.map(FormulaFunction::checkNotNull)
				.mapToDouble(p -> (Double) p)
				.max()
				.orElse(Double.NaN);
		}
	},
	MODULO {
		@Override
		public Double getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final var dividend = (Double) parameters.get(0);
			final var divisor = (Double) parameters.get(1);
			return (double) Math.floorMod(dividend.longValue(), divisor.longValue());
		}
	},
	ABS {
		@Override
		public Double getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 1) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 1);
			}
			return Math.abs((Double) parameters.get(0));
		}
	},
	ROUND {
		@Override
		public Double getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final int precision = ((Double) parameters.get(1)).intValue();
			final var number = BigDecimal.valueOf((Double) parameters.get(0));
			return number.setScale(precision, RoundingMode.HALF_DOWN).doubleValue();
		}
	},
	CEIL {
		@Override
		public Double getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 1) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 1);
			}
			return Math.ceil((Double) parameters.get(0));
		}
	},
	FLOOR {
		@Override
		public Double getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 1) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 1);
			}
			return Math.floor((Double) parameters.get(0));
		}
	},
	BMI {
		@Override
		public Double getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException {
			if(parameters.size() != 2) {
				throw new FormulaWrongFunctionParametersException(this, parameters.size(), 2);
			}
			final var weight = (Double) parameters.get(0);
			final var heightCm = (Double) parameters.get(1);
			if(weight == null || heightCm == null) {
				return null;
			}
			final var heightM = heightCm / 100;
			return weight / Math.pow(heightM, 2);
		}
	};

	public static <T> T checkNotNull(final T object) {
		if(object == null) {
			throw new FormulaNullParameterException();
		}
		return object;
	}

	public static BigDecimal toBigDecimal(final Object object) {
		if(object == null) {
			throw new FormulaNullParameterException();
		}
		try {
			return BigDecimal.valueOf(((Number) object).doubleValue());
		}
		catch(final Exception e) {
			throw new FormulaWrongParameterTypeException(object, e);
		}
	}

	public abstract Object getValue(final List<Object> parameters) throws FormulaWrongFunctionParametersException;
}
