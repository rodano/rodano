import {PartialDate} from '../../../tools/partial_date.js';

export function add_field(study, Data, entity_constructor) {

	Data.Entities.Field = class Field {
		static getProperties() {
			return {
				dataset: {type: 'Dataset', back_reference: true},
				datasetModelId: {type: 'string'},
				fieldModelId: {type: 'string'},
				value: {type: 'string'}
			};
		}

		constructor(values) {
			this.dataset = undefined;
			this.datasetModelId = undefined;
			this.fieldModelId = undefined;
			this.value = undefined;
			entity_constructor.call(this, values);
		}

		getFieldModel() {
			return this.dataset.getDatasetModel().getFieldModel(this.fieldModelId);
		}

		checkValidity(value) {
			/*var dataset = this.dataset;
			var event = dataset.event;
			var scope = event.scope;*/

			const field_model = this.getFieldModel();

			//basic validation
			/*if(value === undefined || value === null) {
				throw new Error('Null value is not allowed');
			}*/

			//do some specific checks if value is not empty
			if(value) {

				//number must be parsable as a double
				if('NUMBER' === field_model.type) {
					if(`${parseFloat(value)}` !== value) {
						throw new Error(`${value} is not a valid number`);
					}
				}

				//date select must be well formed
				else if('DATE_SELECT' === field_model.type) {
					//check consistency
					const parts = value.split('.');
					parts.forEach(function(part) {
						if(!part) {
							throw new Error('Incomplete partial date');
						}
					});
					//check consistency regarding precision
					let has_unknown_part = false;
					parts.reverse().forEach(function(part) {
						if(part === PartialDate.UNKNOWN) {
							has_unknown_part = true;
						}
						else {
							if(has_unknown_part) {
								//TODO throw good exception
								throw new Error('Partial date not consistent');
							}
						}
					});
					if(!has_unknown_part) {
						//check valid date
						const date = new Date(parts[2], parts[1], parts[0]);
						if(date.getDate() !== parts[0] || date.getMonth() + 1 !== parts[1] || date.getFullYear() !== parts[2]) {
							throw new Error('Invalid date');
						}
					}

					const date = new PartialDate(value);
					//check date is not before 1900
					if(date.isBefore(new PartialDate(['unknown', 'unknown', 1900]))) {
						throw new Error('Date cannot be before 1900');
					}

					//check date is not in future
					const tomorrow = new Date().addDays(1);
					if(!field_model.allowDateInFuture && date.isAfter(new PartialDate(tomorrow))) {
						throw new Error('Date cannot be in the future');
					}
				}

				//select and radio must have their value among possible values
				if('SELECT' === field_model.type || 'RADIO' === field_model.type) {
					try {
						field_model.possibleValues.find(function(possible_value) {
							return possible_value.id === value;
						});
					}
					catch {
						throw new Error(`Attribute ${field_model.id} does not allow value ${value} because it's not an id of one of its possible values`);
					}
				}

				//checkbox group and palette must have their values among possible values and eventually an other value
				/*if(FieldModelType.CHECKBOX_GROUP.equals(field_model.getType()) || FieldModelType.PALETTE.equals(field_model.getType())) {
					final TreeSet<String> values = new TreeSet<String>(field_model.getPossibleValueComparator());
					values.addAll(Arrays.asList(value.split(",")));
					//check possible values and other option
					final List<String> possibleValueIds = field_model.getPossibleValueIds();
					boolean otherValue = false;
					for(final String possibleValueId : values) {
						if(!possibleValueIds.includes(possibleValueId)) {
							if(!field_model.isOtherOption()) {
								throw new NoRespectForConfiguration(String.format("Field model %s does not allow value %s because it's not an id of one of its possible values", field_model.getId(), value));
							}
							if(otherValue) {
								throw new NoRespectForConfiguration(String.format("Field model %s does not allow more than one other option", field_model.getId(), value));
							}
							otherValue = true;
						}
					}
					toSetValue = Joiner.on(",").join(values);
				}*/
			}

			field_model.getValidators().forEach(validator => {
				if(validator.required && !value) {
					throw new Data.Errors.ValidationError(validator, this, value, 'is required');
				}
			});
		}
	};
}
