export const Operator = Object.freeze({
	EQUALS: {
		symbol: '=',
		label: 'Equals to',
		has_value: true,
		test: function(a, b) {
			return a === b;
		}
	},
	NOT_EQUALS: {
		symbol: '!=',
		label: 'Not equals to',
		has_value: true,
		test: function(a, b) {
			return a !== b;
		}
	},
	CONTAINS: {
		symbol: '*=',
		label: 'Contains',
		has_value: true,
		test: function(a, b) {
			return a.includes(b);
		}
	},
	NOT_CONTAINS: {
		symbol: '!*=',
		label: 'Contains not',
		has_value: true,
		test: function(a, b) {
			return !a.includes(b);
		}
	},
	GREATER: {
		symbol: '>',
		label: 'Greater than',
		has_value: true,
		test: function(a, b) {
			return a > b;
		}
	},
	GREATER_EQUALS: {
		symbol: '>=',
		label: 'Greater or equals to',
		has_value: true,
		test: function(a, b) {
			return a >= b;
		}
	},
	LOWER: {
		symbol: '<',
		label: 'Lower than',
		has_value: true,
		test: function(a, b) {
			return a < b;
		}
	},
	LOWER_EQUALS: {
		symbol: '<=',
		label: 'Lower or equals to',
		has_value: true,
		test: function(a, b) {
			return a <= b;
		}
	},
	NULL: {
		label: 'Is null',
		has_value: false,
		test: function(a) {
			return a === undefined;
		}
	},
	NOT_NULL: {
		label: 'Is not null',
		has_value: false,
		test: function(a) {
			return a !== undefined;
		}
	},
	BLANK: {
		label: 'Is blank',
		has_value: false,
		test: function(a) {
			return a === undefined || a === '';
		}
	},
	NOT_BLANK: {
		label: 'Is not blank',
		has_value: false,
		test: function(a) {
			return a !== undefined && a !== '';
		}
	}
});
