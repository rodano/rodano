export function add_event(study, Data, entity_constructor) {

	Data.Entities.Event = class Event {
		static getProperties() {
			return {
				scope: {type: 'Scope', back_reference: true},
				scopeModelId: {type: 'string'},
				eventModelId: {type: 'string'},
				expectedDate: {type: 'date'},
				date: {type: 'date'},
				endDate: {type: 'date'},
				blocking: {type: 'boolean'},
				deleted: {type: 'boolean'},
				datasets: {type: 'array'},
				forms: {type: 'array'}
			};
		}

		constructor(values) {
			this.scope = undefined;
			this.scopeModelId = undefined;
			this.eventModelId = undefined;
			this.expectedDate = undefined;
			this.date = undefined;
			this.expectedDate = undefined;
			this.endDate = undefined;
			this.blocking = false;
			this.deleted = false;
			this.datasets = [];
			this.forms = [];
			entity_constructor.call(this, values);
			//create datasets and forms
			if(this.eventModelId) {
				const event_model = this.getEventModel();
				event_model.getDatasetModels().forEach(Data.Entities.Event.prototype.createDataset, this);
				event_model.getFormModels().forEach(Data.Entities.Event.prototype.createForm, this);
			}
		}

		getEventModel() {
			return this.scope.getScopeModel().getEventGroup(this.eventModelId);
		}

		getEventGroup() {
			return this.getEventModel().getEventGroup();
		}

		getLocalizedLabel(languages) {
			return `${this.getEventModel().getLocalizedLabel(languages)} - ${this.date.toDisplay()}`;
		}

		createDataset(dataset_model) {
			const dataset = new Data.Entities.Dataset({
				event: this,
				datasetModel: dataset_model.id,
			});
			this.datasets.push(dataset);
			return dataset;
		}

		createForm(form_model) {
			const form = new Data.Entities.Form({
				event: this,
				formModelId: form_model.id
			});
			this.forms.push(form);
			return form;
		}

		getDatasets(dataset_model_id) {
			return this.datasets.filter(d => dataset_model_id === d.datasetModelId);
		}

		getDataset(dataset_model_id, number) {
			return this.datasets.find(d => d.datasetModelId === dataset_model_id && d.number === number && !d.deleted);
		}
	};
}
