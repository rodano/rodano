export function add_dataset(study, Data, entity_constructor) {

	Data.Entities.Dataset = class Dataset {
		static getProperties() {
			return {
				scope: {type: 'Scope', back_reference: true},
				event: {type: 'Event', back_reference: true},
				datasetModelId: {type: 'string'},
				number: {type: 'number'},
				deleted: {type: 'boolean'},
				fields: {type: 'array'},
			};
		}

		constructor(values) {
			this.scope = undefined;
			this.event = undefined;
			this.datasetModelId = undefined;
			this.number = 0;
			this.deleted = false;
			this.fields = [];
			entity_constructor.call(this, values);
			//create fields
			if(this.datasetModelId) {
				this.getDatasetModel().fieldModels.forEach(Data.Entities.Dataset.prototype.createField, this);
			}
		}

		createField(field_model) {
			const field = new Data.Entities.Field({
				dataset: this,
				fieldModelId: field_model.id
			});
			this.fields.push(field);
			return field;
		}

		getDatasetModel() {
			return study.getDatasetModel(this.datasetModelId);
		}

		getField(field_model_id) {
			return this.fields.find(f => f.fieldModelId === field_model_id);
		}
	};
}
