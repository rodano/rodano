export const YEAR_FORMAT = {
	parse: {
		dateInput: {month: 'short', year: 'numeric', day: 'numeric'}
	},
	display: {
		dateInput: 'year',
		monthYearLabel: {year: 'numeric'},
		dateA11yLabel: {year: 'numeric'},
		monthYearA11yLabel: {year: 'numeric'}
	}
};

export const MONTH_YEAR_FORMAT = {
	parse: {
		dateInput: {month: 'short', year: 'numeric', day: 'numeric'}
	},
	display: {
		dateInput: 'month-year',
		monthYearLabel: {year: 'numeric', month: 'numeric'},
		dateA11yLabel: {year: 'numeric', month: 'long'},
		monthYearA11yLabel: {year: 'numeric', month: 'long'}
	}
};
