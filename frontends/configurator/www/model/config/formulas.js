export const Formulas = Object.freeze({
	CREATE_DATE: {
		label: 'Create date from 6 components',
		parameters: [
			{
				name: 'Year',
				type: 'NUMBER'
			},
			{
				name: 'Month',
				type: 'NUMBER'
			},
			{
				name: 'Date',
				type: 'NUMBER'
			},
			{
				name: 'Hours',
				type: 'NUMBER'
			},
			{
				name: 'Minutes',
				type: 'NUMBER'
			},
			{
				name: 'Seconds',
				type: 'NUMBER'
			},
		],
		returns: 'DATE'
	},
	TODAY: {
		label: 'Today',
		parameters: [],
		returns: 'DATE'
	},
	ADD_DURATION: {
		label: 'Add duration to date',
		parameters: [
			{
				name: 'Date',
				type: 'DATE'
			},
			{
				name: 'Years',
				type: 'NUMBER'
			},
			{
				name: 'Months',
				type: 'NUMBER'
			},
			{
				name: 'Days',
				type: 'NUMBER'
			},
			{
				name: 'Hours',
				type: 'NUMBER'
			},
			{
				name: 'Minutes',
				type: 'NUMBER'
			},
			{
				name: 'Seconds',
				type: 'NUMBER'
			},
		],
		returns: 'DATE'
	},
	ADD_YEARS: {
		label: 'Add years to date',
		parameters: [
			{
				name: 'Date',
				type: 'DATE'
			},
			{
				name: 'Years',
				type: 'NUMBER'
			}
		],
		returns: 'DATE'
	},
	ADD_MONTHS: {
		label: 'Add months to date',
		parameters: [
			{
				name: 'Date',
				type: 'DATE'
			},
			{
				name: 'Months',
				type: 'NUMBER'
			}
		],
		returns: 'DATE'
	},
	ADD_DAYS: {
		label: 'Add days to date',
		parameters: [
			{
				name: 'Date',
				type: 'DATE'
			},
			{
				name: 'Days',
				type: 'NUMBER'
			}
		],
		returns: 'DATE'
	},
	ADD_HOURS: {
		label: 'Add hours to date',
		parameters: [
			{
				name: 'Date',
				type: 'DATE'
			},
			{
				name: 'Hours',
				type: 'NUMBER'
			}
		],
		returns: 'DATE'
	},
	ADD_MINUTES: {
		label: 'Add minutes to date',
		parameters: [
			{
				name: 'Date',
				type: 'DATE'
			},
			{
				name: 'Minutes',
				type: 'NUMBER'
			}
		],
		returns: 'DATE'
	},
	ADD_SECONDS: {
		label: 'Add seconds to date',
		parameters: [
			{
				name: 'Date',
				type: 'DATE'
			},
			{
				name: 'Seconds',
				type: 'NUMBER'
			}
		],
		returns: 'DATE'
	},
	DIFFERENCE_IN_YEARS: {
		label: 'Calculate the number of years between two dates',
		parameters: [
			{
				name: 'Date 1',
				type: 'DATE'
			},
			{
				name: 'Date 2',
				type: 'DATE'
			}
		],
		returns: 'NUMBER'
	},
	DIFFERENCE_IN_MONTHS: {
		label: 'Calculate the number of months between two dates',
		parameters: [
			{
				name: 'Date 1',
				type: 'DATE'
			},
			{
				name: 'Date 2',
				type: 'DATE'
			}
		],
		returns: 'NUMBER'
	},
	DIFFERENCE_IN_DAYS: {
		label: 'Calculate the number of days between two dates',
		parameters: [
			{
				name: 'Date 1',
				type: 'DATE'
			},
			{
				name: 'Date 2',
				type: 'DATE'
			}
		],
		returns: 'NUMBER'
	},
	DIFFERENCE_IN_SECONDS: {
		label: 'Calculate the number of seconds between two dates',
		parameters: [
			{
				name: 'Date 1',
				type: 'DATE'
			},
			{
				name: 'Date 2',
				type: 'DATE'
			}
		],
		returns: 'NUMBER'
	},
	MONTH_OF_DATE: {
		label: 'Returns the parameter date with its day of month set to 1',
		parameters: [
			{
				name: 'Date',
				type: 'DATE'
			}
		],
		returns: 'DATE'
	},
	IF: {
		label: 'Condition that returns second or third parameter depending on first parameter',
		parameters: [
			{
				name: 'Condition',
				type: 'BOOLEAN'
			},
			{
				name: 'Result if true',
				type: 'ANY'
			},
			{
				name: 'Result if false',
				type: 'ANY'
			}
		],
		returns: 'ANY'
	},
	IS_EQUAL_TO: {
		label: 'Test if both objects are equal',
		parameters: [
			{
				name: 'Object 1',
				type: 'ANY'
			},
			{
				name: 'Object 2',
				type: 'ANY'
			}
		],
		returns: 'BOOLEAN'
	},
	NUMBER_TO_STRING: {
		label: 'Convert a number to a string',
		parameters: [
			{
				name: 'Number',
				type: 'NUMBER'
			}
		],
		returns: 'STRING'
	},
	STRING_TO_NUMBER: {
		label: 'Convert a string to a number',
		parameters: [
			{
				name: 'String',
				type: 'STRING'
			}
		],
		returns: 'NUMBER'
	},
	IS_BLANK: {
		label: 'Test if string is blank',
		parameters: [
			{
				name: 'String',
				type: 'STRING'
			}
		],
		returns: 'BOOLEAN'
	},
	CONCAT: {
		label: 'Concat strings',
		parameters: [
			{
				name: 'String',
				type: 'STRING',
				repeatable: true
			}
		],
		returns: 'STRING'
	},
	UPPERCASE: {
		label: 'Returns the string in uppercase',
		parameters: [
			{
				name: 'String',
				type: 'STRING'
			}
		],
		returns: 'STRING'
	},
	LOWERCASE: {
		label: 'Returns the string in lowercase',
		parameters: [
			{
				name: 'String',
				type: 'STRING'
			}
		],
		returns: 'STRING'
	},
	IS_GREATER_THAN: {
		label: 'Test if the first number is greater than the second',
		parameters: [
			{
				name: 'Number 1',
				type: 'NUMBER'
			},
			{
				name: 'Number 2',
				type: 'NUMBER'
			}
		],
		returns: 'BOOLEAN'
	},
	IS_GREATER_OR_EQUAL: {
		label: 'Test if the first number is greater than or equal to the second',
		parameters: [
			{
				name: 'Number 1',
				type: 'NUMBER'
			},
			{
				name: 'Number 2',
				type: 'NUMBER'
			}
		],
		returns: 'BOOLEAN'
	},
	IS_LESS_THAN: {
		label: 'Test if the first number is less than the second',
		parameters: [
			{
				name: 'Number 1',
				type: 'NUMBER'
			},
			{
				name: 'Number 2',
				type: 'NUMBER'
			}
		],
		returns: 'BOOLEAN'
	},
	IS_LESS_OR_EQUAL: {
		label: 'Test if the first number is less than or equal to the second',
		parameters: [
			{
				name: 'Number 1',
				type: 'NUMBER'
			},
			{
				name: 'Number 2',
				type: 'NUMBER'
			}
		],
		returns: 'BOOLEAN'
	},
	SUM: {
		label: 'Sum numbers (tolerate null values and non-numbers)',
		parameters: [
			{
				name: 'Number',
				type: 'NUMBER',
				repeatable: true
			}
		],
		returns: 'NUMBER'
	},
	MULTIPLY: {
		label: 'Multiply numbers (tolerate null values and non-numbers)',
		parameters: [
			{
				name: 'Number',
				type: 'NUMBER',
				repeatable: true
			}
		],
		returns: 'NUMBER'
	},
	SUBTRACT: {
		label: 'Subtract numbers (tolerate null values and non-numbers)',
		parameters: [
			{
				name: 'Number',
				type: 'NUMBER'
			},
			{
				name: 'Subtrahend',
				type: 'NUMBER',
				repeatable: true
			}
		],
		returns: 'NUMBER'
	},
	DIVIDE: {
		label: 'Divide numbers (tolerate null values and non-numbers)',
		parameters: [
			{
				name: 'Number',
				type: 'NUMBER'
			},
			{
				name: 'Divider',
				type: 'NUMBER',
				repeatable: true
			}
		],
		returns: 'NUMBER'
	},
	POWER: {
		label: 'Raise a base to an exponent',
		parameters: [
			{
				name: 'Base',
				type: 'NUMBER'
			},
			{
				name: 'Exponent',
				type: 'NUMBER'
			}
		],
		returns: 'NUMBER'
	},
	SQRT: {
		label: 'Square root of a number',
		parameters: [
			{
				name: 'Number',
				type: 'NUMBER'
			}
		],
		returns: 'NUMBER'
	},
	INVERSE: {
		label: 'Inverse a number',
		parameters: [
			{
				name: 'Number',
				type: 'NUMBER'
			}
		],
		returns: 'NUMBER'
	},
	AVERAGE: {
		label: 'Calculate the average for a collection of numbers (tolerate null values)',
		parameters: [
			{
				name: 'Number',
				type: 'NUMBER',
				repeatable: true
			}
		],
		returns: 'NUMBER'
	},
	MEDIAN: {
		label: 'Calculate the median for a collection of numbers (tolerate null values)',
		parameters: [
			{
				name: 'Number',
				type: 'NUMBER',
				repeatable: true
			}
		],
		returns: 'NUMBER'
	},
	MIN: {
		label: 'Find the minimum among a collection of numbers (tolerate null values)',
		parameters: [
			{
				name: 'Number',
				type: 'NUMBER',
				repeatable: true
			}
		],
		returns: 'NUMBER'
	},
	MAX: {
		label: 'Find the maximum among a collection of numbers (tolerate null values)',
		parameters: [
			{
				name: 'Number',
				type: 'NUMBER',
				repeatable: true
			}
		],
		returns: 'NUMBER'
	},
	MODULO: {
		label: 'Calculate modulo of a number',
		parameters: [
			{
				name: 'Dividend',
				type: 'NUMBER'
			},
			{
				name: 'Divisor',
				type: 'NUMBER'
			}
		],
		returns: 'NUMBER'
	},
	ABS: {
		label: 'Absolute value of a number',
		parameters: [
			{
				name: 'Number',
				type: 'NUMBER'
			}
		],
		returns: 'NUMBER'
	},
	ROUND: {
		label: 'Round a number',
		parameters: [
			{
				name: 'Number',
				type: 'NUMBER'
			},
			{
				name: 'Precision',
				type: 'NUMBER'
			}
		],
		returns: 'NUMBER'
	},
	CEIL: {
		label: 'Take ceil value of a number',
		parameters: [
			{
				name: 'Number',
				type: 'NUMBER'
			}
		],
		returns: 'NUMBER'
	},
	FLOOR: {
		label: 'Take floor value of a number',
		parameters: [
			{
				name: 'Number',
				type: 'NUMBER'
			}
		],
		returns: 'NUMBER'
	},
	BMI: {
		label: 'Calculate BMI out of height and weight',
		parameters: [
			{
				name: 'Weight',
				type: 'NUMBER'
			},
			{
				name: 'Height',
				type: 'NUMBER'
			}
		],
		returns: 'NUMBER'
	}
});
