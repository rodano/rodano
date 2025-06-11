import {Operator} from '../model/operator';

type OperatorByType = Record<string, Operator[]>;

export const operatorByType: OperatorByType = {
	STRING: [
		Operator.EQUALS,
		Operator.NOT_EQUALS,
		Operator.CONTAINS,
		Operator.NOT_CONTAINS,
		Operator.NULL,
		Operator.NOT_NULL
	],
	AUTO_COMPLETION: [
		Operator.EQUALS,
		Operator.NOT_EQUALS,
		Operator.CONTAINS,
		Operator.NOT_CONTAINS,
		Operator.NULL,
		Operator.NOT_NULL
	],
	DATE: [
		Operator.EQUALS,
		Operator.NOT_EQUALS,
		Operator.GREATER,
		Operator.GREATER_EQUALS,
		Operator.LOWER,
		Operator.LOWER_EQUALS,
		Operator.NULL,
		Operator.NOT_NULL
	],
	DATE_SELECT: [
		Operator.EQUALS,
		Operator.NOT_EQUALS,
		Operator.GREATER,
		Operator.GREATER_EQUALS,
		Operator.LOWER,
		Operator.LOWER_EQUALS,
		Operator.NULL,
		Operator.NOT_NULL
	],
	NUMBER: [
		Operator.EQUALS,
		Operator.NOT_EQUALS,
		Operator.GREATER,
		Operator.GREATER_EQUALS,
		Operator.LOWER,
		Operator.LOWER_EQUALS,
		Operator.NULL,
		Operator.NOT_NULL
	],
	SELECT: [
		Operator.EQUALS,
		Operator.NOT_EQUALS,
		Operator.NULL,
		Operator.NOT_NULL
	],
	RADIO: [
		Operator.EQUALS,
		Operator.NOT_EQUALS,
		Operator.NULL,
		Operator.NOT_NULL
	],
	CHECKBOX: [Operator.EQUALS],
	CHECKBOX_GROUP: [
		Operator.EQUALS,
		Operator.NOT_EQUALS,
		Operator.CONTAINS,
		Operator.NOT_CONTAINS,
		Operator.NULL,
		Operator.NOT_NULL
	],
	PALETTE: [
		Operator.EQUALS,
		Operator.NOT_EQUALS,
		Operator.NULL,
		Operator.NOT_NULL
	],
	TEXTAREA: [
		Operator.EQUALS,
		Operator.NOT_EQUALS,
		Operator.CONTAINS,
		Operator.NOT_CONTAINS,
		Operator.NULL,
		Operator.NOT_NULL
	]
};
