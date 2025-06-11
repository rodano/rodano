export function add_scope(study, Data, entity_constructor) {

	Data.Entities.Scope = class Scope {
		static getProperties() {
			return {
				id: {type: 'string'},
				code: {type: 'string'},
				shortname: {type: 'object'},
				longname: {type: 'object'},
				description: {type: 'object'},
				scopeModelId: {type: 'string'},
				path: {type: 'array'},
				status: {type: 'string'},
				virtual: {type: 'boolean'},
				deleted: {type: 'boolean'},
				expectedNumber: {type: 'number'},
				maxNumber: {type: 'number'},
				workflowStatus: {type: 'array'},
				events: {type: 'array'},
				datasets: {type: 'array'},
				forms: {type: 'array'}
			};
		}

		constructor(values) {
			this.id = undefined;
			this.code = undefined;
			this.shortname = {};
			this.longname = {};
			this.description = {};
			this.scopeModelId = undefined;
			this.paths = [];
			this.status = undefined;
			this.virtual = false;
			this.deleted = false;
			this.expectedNumber = undefined;
			this.maxNumber = undefined;
			this.workflowStatus = [];
			this.events = [];
			this.datasets = [];
			this.forms = [];
			entity_constructor.call(this, values);
			//create datasets and forms
			if(this.scopeModelId) {
				const scope_model = this.getScopeModel();
				scope_model.getDatasetModels().forEach(Data.Entities.Scope.prototype.createDataset, this);
				scope_model.getFormModels().forEach(Data.Entities.Scope.prototype.createForm, this);
				scope_model.getEventModels().forEach(Data.Entities.Scope.prototype.createEvent, this);
			}
		}

		getScopeModel() {
			return study.getScopeModel(this.scopeModelId);
		}

		getEvents(event_group_id, event_group_number) {
			return this.events.filter(function(event) {
				let valid = event.eventGroupId === event_group_id;
				if(event_group_number) {
					valid = valid && event.eventGroupNumber === event_group_number;
				}
				return valid;
			});
		}

		createDataset(dataset_model) {
			const dataset = new Data.Entities.Dataset({
				scope: this,
				datasetModel: dataset_model.id,
			});
			this.datasets.push(dataset);
			return dataset;
		}

		createForm(form_model) {
			const form = new Data.Entities.Form({
				scope: this,
				formModelId: form_model.id
			});
			this.forms.push(form);
			return form;
		}

		createEvent(event_model) {
			//TODO check event can be added to this scope
			//checkAddition(study, event);

			//retrieve last event number
			const event_group_number = this.getEvents(event_model.id).reduce(function(previous, event) {
				return previous > event.eventGroupNumber ? previous : event.eventGroupNumber;
			}, -1) + 1;

			const event = this.createInternalEvent(event_model);

			event_model.getImpliedEventModels().forEach(implied_event_model => {
				const implied_event = this.createInternalEvent(implied_event_model);
				implied_event.eventGroupNumber = event_group_number;
			});

			this.resetEventDates();

			return event;
		}

		createInternalEvent(event_model) {
			const event = new Data.Entities.Event({
				scope: this,
				eventModelId: event_model.id,
				eventGroupId: event_model.eventGroup.id,
				expected: event_model.eventGroup.isInterventional(),
				date: new Date()
			});
			this.events.push(event);
			return event;
		}

		resetEventDates() {
			//retrieve events and sort them according to configuration
			const events = this.getEvents();
			events.sort(function(event_1, event_2) {
				const event_model_1 = event_1.getEventModel();
				const event_model_2 = event_2.getEventModel();
				return event_model_1.constructor.getSchedulingComparator(event_model_1, event_model_2);
			});

			//build reference events map
			const events_by_id = {};
			events.forEach(function(event) {
				events_by_id[event.eventModelId] = event;
			});

			//set events date
			events.forEach(function(event) {
				const event_model = event.getEventModel();
				if(event.expected && event_model.isPlanned()) {
					event.date = new Date(events_by_id[event_model.deadlineReferenceEventModelId].date).addSeconds(event_model.getDeadlineInSeconds());
				}
			});
		}

		getDatasets(dataset_model_id, number) {
			return this.datasets.filter(d => d.datasetModelId === dataset_model_id && d.number === number);
		}

		getForm(form_model_id) {
			return this.forms.find(p => p.formModelId === form_model_id);
		}
	};
}
