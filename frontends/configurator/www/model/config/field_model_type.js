import {Operator} from './operator.js';

export const FieldModelType = Object.freeze({
	STRING: {
		label: 'String',
		is_text: true,
		is_time: false,
		is_sizable: true,
		is_multiple_choice: false,
		has_multiple_values: false,
		has_visibility_criteria: false,
		operators: [
			Operator.EQUALS,
			Operator.NOT_EQUALS,
			Operator.CONTAINS,
			Operator.NOT_CONTAINS,
			Operator.NULL,
			Operator.NOT_NULL
		],
		sql: function(length) {
			return `varchar(${length})`;
		},
		default_max_length: 400
	},
	AUTO_COMPLETION: {
		label: 'Autocompleted string',
		is_text: true,
		is_time: false,
		is_sizable: true,
		//TODO create a new field model "has_multiple_choice" to make a difference between this type and other field model types
		//in Java, "is_multiple_choice" is set to false
		is_multiple_choice: true,
		has_multiple_values: false,
		has_visibility_criteria: true,
		operators: [
			Operator.EQUALS,
			Operator.NOT_EQUALS,
			Operator.NULL,
			Operator.NOT_NULL
		],
		sql: function(length) {
			return `varchar(${length})`;
		},
		default_max_length: 400
	},
	DATE: {
		label: 'Date',
		is_text: false,
		is_time: true,
		is_sizable: false,
		is_multiple_choice: false,
		has_multiple_values: false,
		has_visibility_criteria: false,
		operators: [
			Operator.EQUALS,
			Operator.NOT_EQUALS,
			Operator.GREATER,
			Operator.GREATER_EQUALS,
			Operator.LOWER,
			Operator.LOWER_EQUALS,
			Operator.NULL,
			Operator.NOT_NULL
		],
		sql: function() {
			return 'datetime';
		},
		default_max_length: undefined
	},
	DATE_SELECT: {
		label: 'Date with selection',
		is_text: false,
		is_time: true,
		is_sizable: false,
		is_multiple_choice: false,
		has_multiple_values: false,
		has_visibility_criteria: false,
		operators: [
			Operator.EQUALS,
			Operator.NOT_EQUALS,
			Operator.GREATER,
			Operator.GREATER_EQUALS,
			Operator.LOWER,
			Operator.LOWER_EQUALS,
			Operator.NULL,
			Operator.NOT_NULL
		],
		sql: function(length) {
			return `varchar(${length})`;
		},
		default_max_length: 23
	},
	NUMBER: {
		label: 'Number',
		is_text: false,
		is_time: false,
		is_sizable: false,
		is_multiple_choice: false,
		has_multiple_values: false,
		has_visibility_criteria: true,
		operators: [
			Operator.EQUALS,
			Operator.NOT_EQUALS,
			Operator.GREATER,
			Operator.GREATER_EQUALS,
			Operator.LOWER,
			Operator.LOWER_EQUALS,
			Operator.NULL,
			Operator.NOT_NULL
		],
		sql: function() {
			return 'double';
		},
		default_max_length: undefined
	},
	SELECT: {
		label: 'Combobox',
		is_text: true,
		is_time: false,
		is_sizable: true,
		is_multiple_choice: true,
		has_multiple_values: false,
		has_visibility_criteria: true,
		operators: [
			Operator.EQUALS,
			Operator.NOT_EQUALS,
			Operator.NULL,
			Operator.NOT_NULL
		],
		sql: function(length) {
			return `varchar(${length})`;
		},
		default_max_length: 80
	},
	RADIO: {
		label: 'Radio',
		is_text: true,
		is_time: false,
		is_sizable: false,
		is_multiple_choice: true,
		has_multiple_values: false,
		has_visibility_criteria: true,
		operators: [
			Operator.EQUALS,
			Operator.NOT_EQUALS,
			Operator.NULL,
			Operator.NOT_NULL
		],
		sql: function(length) {
			return `varchar(${length})`;
		},
		default_max_length: 100
	},
	CHECKBOX: {
		label: 'Checkbox',
		is_text: false,
		is_time: false,
		is_sizable: false,
		is_multiple_choice: false,
		has_multiple_values: false,
		has_visibility_criteria: true,
		operators: [
			Operator.EQUALS,
			Operator.NOT_EQUALS,
			Operator.NULL,
			Operator.NOT_NULL
		],
		sql: function() {
			return 'varchar(5)';
		},
		default_max_length: undefined
	},
	CHECKBOX_GROUP: {
		label: 'Checkbox group',
		is_text: false,
		is_time: false,
		is_sizable: false,
		is_multiple_choice: true,
		has_multiple_values: true,
		has_visibility_criteria: true,
		operators: [
			Operator.EQUALS,
			Operator.NOT_EQUALS,
			Operator.CONTAINS,
			Operator.NOT_CONTAINS,
			Operator.NULL,
			Operator.NOT_NULL
		],
		sql: function(length) {
			return `varchar(${length})`;
		},
		default_max_length: 400
	},
	TEXTAREA: {
		label: 'Text area',
		is_text: true,
		is_time: false,
		is_sizable: true,
		is_multiple_choice: false,
		has_multiple_values: false,
		has_visibility_criteria: false,
		operators: [
			Operator.EQUALS,
			Operator.NOT_EQUALS,
			Operator.NULL,
			Operator.NOT_NULL
		],
		sql: function() {
			return 'text';
		},
		default_max_length: undefined
	},
	FILE: {
		label: 'File',
		is_text: false,
		is_time: false,
		is_sizable: false,
		is_multiple_choice: false,
		has_multiple_values: false,
		has_visibility_criteria: false,
		operators: [],
		sql: function(length) {
			return `varchar(${length})`;
		},
		default_max_length: 400
	}
});
