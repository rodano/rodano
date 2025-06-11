export const DateAggregationFunction = Object.freeze({
	MIN: {
		label: 'Min',
		accumulator: (d1, d2) => Math.min(d1, d2)
	},
	MAX: {
		label: 'Max',
		accumulator: (d1, d2) => Math.max(d1, d2)
	}
});
