import {Operator} from './operator.js';

export const DataType = Object.freeze({
	STRING: {
		label: 'String',
		operators: [
			Operator.EQUALS,
			Operator.NOT_EQUALS,
			Operator.CONTAINS,
			Operator.NOT_CONTAINS,
			Operator.NULL,
			Operator.NOT_NULL,
			Operator.BLANK,
			Operator.NOT_BLANK
		]
	},
	DATE: {
		label: 'Date',
		operators: [
			Operator.EQUALS,
			Operator.NOT_EQUALS,
			Operator.GREATER,
			Operator.GREATER_EQUALS,
			Operator.LOWER,
			Operator.LOWER_EQUALS,
			Operator.NULL,
			Operator.NOT_NULL
		]
	},
	NUMBER: {
		label: 'Number',
		operators: [
			Operator.EQUALS,
			Operator.NOT_EQUALS,
			Operator.GREATER,
			Operator.GREATER_EQUALS,
			Operator.LOWER,
			Operator.LOWER_EQUALS,
			Operator.NULL,
			Operator.NOT_NULL
		]
	},
	BOOLEAN: {
		label: 'Boolean',
		operators: [
			Operator.EQUALS,
			Operator.NOT_EQUALS,
			Operator.NULL,
			Operator.NOT_NULL
		]
	},
	BLOB: {
		label: 'Blob',
		operators: []
	}
});
