import '../../basic-tools/extension.js';

import {add_scope} from './entities/scope.js';
import {add_event} from './entities/event.js';
import {add_dataset} from './entities/dataset.js';
import {add_field} from './entities/field.js';
import {add_form} from './entities/form.js';

export function create_data(study, custom_constructor) {

	//use custom constructor or default constructor
	const entity_constructor = custom_constructor || function(values) {
		//set custom values
		Object.assign(this, values);
	};

	const Data = {};

	//classes
	Data.Entities = {};

	add_scope(study, Data, entity_constructor);
	add_event(study, Data, entity_constructor);
	add_dataset(study, Data, entity_constructor);
	add_field(study, Data, entity_constructor);
	add_form(study, Data, entity_constructor);

	Data.Errors = {};
	Data.Errors.ValidationError = function(validator, field, value, message) {
		this.validator = validator;
		this.field = field;
		this.value = value;
		this.message = message;
	};
	Data.Errors.ValidationError.prototype = new Error();
	Data.Errors.ValidationError.prototype.constructor = Data.Errors.ValidationError;

	return Data;
}
