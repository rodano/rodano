export const LayoutType = Object.freeze({
	SINGLE: {
		label: 'Single',
		repeatable: false,
		has_dataset_model: false
	},
	MULTIPLE: {
		label: 'Multiple',
		repeatable: true,
		has_dataset_model: true
	}
});
