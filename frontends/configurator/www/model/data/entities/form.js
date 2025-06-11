export function add_form(study, Data, entity_constructor) {

	Data.Entities.Form = class Form {
		static getProperties() {
			return {
				scope: {type: 'Scope', back_reference: true},
				event: {type: 'Event', back_reference: true},
				formModelId: {type: 'string'},
				deleted: {type: 'boolean'}
			};
		}

		constructor(values) {
			this.scope = undefined;
			this.event = undefined;
			this.formModelId = undefined;
			this.deleted = false;
			this.workflowStatus = [];
			entity_constructor.call(this, values);
		}

		getFormModel() {
			return study.getFormModel(this.formModelId);
		}

		getFields() {
			const container = this.scope || this.event;
			const fields = [];
			this.getFormModel().layouts.forEach(layout => {
				const cells = layout.getCells().filter(c => c.hasFieldModel());
				if(layout.type === 'SINGLE') {
					fields.pushAll(cells.map(c => container.getDatasets(c.datasetModelId)[0].getValue(c.fieldModelId)));
				}
				else {
					const datasets = container.getDatasets(layout.datasetModelId).filter(d => !d.deleted);
					//retrieve field model ids
					const field_model_ids = cells.map(c => c.fieldModelId);
					fields.pushAll(datasets.flatMap(d => d.fields).filter(f => field_model_ids.includes(f.fieldModelId)));
				}
			});
			return fields;
		}
	};
}
