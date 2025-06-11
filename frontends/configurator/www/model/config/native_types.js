import {Operator} from './operator.js';

export const NativeType = Object.freeze({
	string: {
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
	number: {
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
	boolean: {
		label: 'Boolean',
		operators: [
			Operator.EQUALS,
			Operator.NOT_EQUALS,
			Operator.NULL,
			Operator.NOT_NULL
		]
	}
});
