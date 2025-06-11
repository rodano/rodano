import {BusEventAddChildNode, BusEventCreateNode, BusEventDeleteNode, BusEventNodeChangeProperty} from './entities_config_bus.js';
import {Bus} from '../../basic-tools/bus.js';
import {Reviver} from '../../basic-tools/reviver.js';

const bus = new Bus();

const proxy = {
	after_setter: function(entity, property, previous_value, new_value) {
		//do not trigger bus for entity property
		if(property !== Reviver.ENTITY_PROPERTY) {
			//detect if there is a modification
			if(!Object.equals(previous_value, new_value)) {
				//create change event
				bus.dispatch(new BusEventNodeChangeProperty(entity, this, property, previous_value, new_value));
			}
		}
	}
};

function define_property(property) {
	Object.defineProperty(this, property, {
		configurable: true, //this.properties[property].back_reference,
		enumerable: property === Reviver.ENTITY_PROPERTY || !this.constructor.getProperties()[property].back_reference,
		set: function(value) {
			//console.log(entity + ' set ' + property + ' ' + value);
			const previous_value = this.properties[property];
			//use setter proxy
			if(proxy?.hasOwnProperty('setter')) {
				this.properties[property] = proxy.setter.call(this, this.constructor.name, property, previous_value, value);
			}
			else {
				//change property
				this.properties[property] = value;
			}
			//call after setter in proxy
			if(proxy?.hasOwnProperty('after_setter')) {
				proxy['after_setter'].call(this, this.constructor.name, property, previous_value, value);
			}
		},
		get: function() {
			//console.log(entity + ' get ' + property);
			//use getter proxy
			if(proxy?.hasOwnProperty('getter')) {
				return proxy.getter.call(this, this.constructor.name, property, this.properties[property]);
			}
			//return property
			return this.properties[property];
		}
	});
}

const EntitiesHooks = {
	CreateNode: function(values) {
		//retrieve entities properties names
		const properties_names = Object.keys(this.constructor.getProperties());

		//build properties storage
		const properties = Object.fromEntries(properties_names.map(p => [p, undefined]));

		//add entity property
		properties[Reviver.ENTITY_PROPERTY] = this.constructor.name;

		//keep only values that are defined in the class
		const valid_values = values ? Object.fromEntries(Object.entries(values).filter(([key]) => properties_names.includes(key))) : undefined;

		//assign default values and incoming valid values to properties storage
		Object.assign(properties, this, valid_values);

		//define property which will retain all other properties
		//use constructor properties for default values first, and set parameters values
		//it is important to set values directly in the "properties" array to avoid triggering the bus
		Object.defineProperty(this, 'properties', {
			value: properties,
			writable: true,
			enumerable: false
		});

		//declare all properties and set up a proxy to intercept modifications
		Object.keys(properties).forEach(define_property, this);

		//register new node
		bus.register(this);
		bus.dispatch(new BusEventCreateNode(this.getEntity().name, this));
	},
	DeleteNode: function() {
		bus.dispatch(new BusEventDeleteNode(this.getEntity().name, this));
		bus.unregister(this);
	},
	AddChildNode: function(child) {
		bus.dispatch(new BusEventAddChildNode(this, child));
	}
};

export {bus, EntitiesHooks};
