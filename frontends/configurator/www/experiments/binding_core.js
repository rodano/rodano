import '../basic-tools/extension.js';
import '../basic-tools/dom_extension.js';
import '../tools/custom_dom_extension.js';
import {Bus} from '../basic-tools/bus.js';

(function() {
	//object change property
	function BusEventObjectChangeProperty(object, property, oldValue, newValue) {
		this.object = object;
		this.property = property;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	BusEventObjectChangeProperty.prototype.hit = function(listener) {
		if(listener.redispatch) {
			listener.redispatch(this);
		}
	};

	//array add element
	function BusEventArrayAddElement(array, element) {
		this.array = array;
		this.element = element;
	}

	BusEventArrayAddElement.prototype.hit = function(listener) {
		if(listener.onArrayAddElement) {
			listener.onArrayAddElement(this);
		}
	};

	//array remove element
	function BusEventArrayRemoveElement(array, element, index) {
		this.array = array;
		this.element = element;
		this.index = index;
	}

	BusEventArrayRemoveElement.prototype.hit = function(listener) {
		if(listener.onArrayRemoveElement) {
			listener.onArrayRemoveElement(this);
		}
	};

	const triggers = {
		objects: [],
		properties: [],
		isRegistered: function(object, property) {
			const index = triggers.objects.indexOf(object);
			return index !== -1 && triggers.properties[index].includes(property);
		},
		register: function(object, property) {
			const index = triggers.objects.indexOf(object);
			if(index === -1) {
				triggers.objects.push(object);
				triggers.properties.push([property]);
			}
			else {
				triggers.properties[index].push(property);
			}
		}
	};

	const listener = {
		objects: [],
		properties: [],
		callbacks: [],
		listen: function(object, property, callback, context) {
			//fill objects and properties matrix
			let object_index = listener.objects.indexOf(object);
			let property_index;
			if(object_index === -1) {
				listener.objects.push(object);
				listener.properties.push([property]);
				object_index = listener.objects.length - 1;
				property_index = 0;
			}
			else {
				property_index = listener.properties[object_index].indexOf(property);
				if(property_index === -1) {
					listener.properties[object_index].push(property);
					property_index = listener.properties[object_index].length - 1;
				}
			}
			//fill callbacks matrix
			if(!listener.callbacks[object_index]) {
				listener.callbacks[object_index] = [];
			}
			if(!listener.callbacks[object_index][property_index]) {
				listener.callbacks[object_index][property_index] = [];
			}
			listener.callbacks[object_index][property_index].push({callback: callback, context: context});
		},
		unlisten: function(object, property, callback) {
			const object_index = listener.objects.indexOf(object);
			const property_index = listener.properties[object_index].indexOf(property);
			const callbacks = listener.callbacks[object_index][property_index];
			const index = callbacks.findIndex(c => c.callback === callback);
			callbacks.remove(index);
		},
		redispatch: function(event) {
			const object_index = listener.objects.indexOf(event.object);
			const property_index = listener.properties[object_index].indexOf(event.property);
			const callbacks = listener.callbacks[object_index][property_index];
			for(let i = callbacks.length - 1; i >= 0; i--) {
				callbacks[i].callback.call(callbacks[i].context, event.newValue);
			}
		}
	};

	const bindbus = new Bus();
	bindbus.register(listener);

	//HTMLFormElement
	HTMLFormElement.prototype.bind = function(object, modifiers, computations) {
		this.boundObject = object;
		this.elements.forEach(function(element) {
			if(element.name) {
				//virtual property
				if(computations?.hasOwnProperty(element.name)) {
					const computation = computations[element.name];
					element.bindCallback(computation.toModel, computation.toUi, computation.context, computation.dependencies);
				}
				//object property
				else {
					//apply modifier if provided
					if(modifiers.hasOwnProperty(element.name)) {
						element.bind(object, element.name, modifiers[element.name].toModel, modifiers[element.name].toUi);
					}
					else {
						element.bind(object, element.name);
					}
				}
			}
		});
	};

	function register_array(array, object, property) {
		//do this for all methods
		array.push = function(element) {
			//call original method
			const result = Array.prototype.push.call(this, element);
			//trigger event
			bindbus.dispatch(new BusEventArrayAddElement(this, element));
			//dispatch change property event if array is a property of an object
			if(object && property) {
				bindbus.dispatch(new BusEventObjectChangeProperty(object, property, this, this));
			}
			//return original result
			return result;
		};
		//wrong
		array.remove = function(index) {
			const item = this[index];
			//call original method
			const result = Array.prototype.remove.call(this, index);
			//trigger event
			bindbus.dispatch(new BusEventArrayRemoveElement(this, item, index));
			//dispatch change property event if array is a property of an object
			if(object && property) {
				bindbus.dispatch(new BusEventObjectChangeProperty(object, property, this, this));
			}
			//return original result
			return result;
		};
		array.splice = function(index, length) {
			const items = [];
			for(let i = index; i < index + length; i++) {
				items.push(this[i]);
			}
			//call original method
			const result = Array.prototype.splice.call(this, index, length);
			//trigger event
			for(let i = index; i < index + length; i++) {
				bindbus.dispatch(new BusEventArrayRemoveElement(this, items[i - index], i));
			}
			//dispatch change property event if array is a property of an object
			if(object && property) {
				bindbus.dispatch(new BusEventObjectChangeProperty(object, property, this, this));
			}
			//return original result
			return result;
		};
	}

	function register(object, property) {
		//check object and property have not already been registered
		if(!triggers.isRegistered(object, property)) {
			//property is an array
			if(Array.isArray(object[property])) {
				register_array(object[property], object, property);
			}
			else {
				//retrieve property descriptor
				let descriptor = Object.getOwnPropertyDescriptor(object, property);
				//check if a descriptor has already been linked to the property
				if(descriptor && !descriptor.hasOwnProperty('value')) {
					//keep a handle on original setter
					const old_setter = descriptor.set;
					const new_setter = function(value) {
						//trigger event
						bindbus.dispatch(new BusEventObjectChangeProperty(this, property, this[property], value));
						//set value in object using original setter
						old_setter.call(object, value);
						//register value if it is an array
						if(Array.isArray(value)) {
							register_array(value, object, property);
						}
					};
					//override current descriptor with new setter
					descriptor.set = new_setter;
				}
				else {
					//store value in this closure
					let value_storage = object[property];
					//create descriptor
					descriptor = {
						get: function() {
							return value_storage;
						},
						set: function(value) {
							//trigger event
							bindbus.dispatch(new BusEventObjectChangeProperty(this, property, value_storage, value));
							//store value
							value_storage = value;
							//register value if it is an array
							if(Array.isArray(value)) {
								register_array(value, object, property);
							}
						},
						enumerable: true,
						configurable: true
					};
					//remove old property
					delete object[property];
				}
				Object.defineProperty(object, property, descriptor);
				//register object property couple
				triggers.register(object, property);
			}
		}
	}

	function update_relation(last_relation, modifier) {
		return function() {
			//retrieve property to modify
			//modify input value
			let value;
			if(modifier) {
				if(this.type === 'checkbox') {
					value = modifier.call(last_relation.object, this.checked);
				}
				else {
					value = modifier.call(last_relation.object, this.value);
				}
			}
			else {
				//object
				if(Object.isObject(last_relation.object[last_relation.property])) {
					value = this.value ? JSON.parse(this.value) : {};
				}
				//array
				else if(Array.isArray(last_relation.object[last_relation.property])) {
					value = this.value ? this.value.split('|') : [];
				}
				else {
					if(this.type === 'checkbox') {
						value = this.checked;
					}
					else {
						//update property with undefined if value is evaluated as false
						value = this.value || undefined;
					}
				}
			}
			//set value
			last_relation.object[last_relation.property] = value;
		};
	}

	function update_ui(last_relation, modifier) {
		return function(value) {
			if(this.type === 'checkbox') {
				this.checked = modifier ? modifier.call(last_relation.object, value) : (value || false);
			}
			else {
				this.value = modifier ? modifier.call(last_relation.object, value) : (value || '');
			}
		};
	}

	HTMLInputElement.prototype.bindCallback = function(to_model, to_ui, context, dependencies) {
		this.unbind();
		const that = this;
		//update binding settings
		this.binding = {
			context: context,
			dependencies: dependencies,
			mbinder: function() {to_model.call(context, that.type === 'checkbox' ? that.checked : that.value);},
			ubinder: function() {
				if(that.type === 'checkbox') {
					that.checked = to_ui.call(context);
				}
				else {
					that.value = to_ui.call(context);
				}
			}
		};
		//register dependencies
		dependencies.forEach(function(dependency) {
			dependency.properties.forEach(function(property) {
				register(dependency.object, property);
				listener.listen(dependency.object, property, that.binding.ubinder, context);
			});
		});
		//update object when input is modified
		if(this.type === 'checkbox') {
			this.addEventListener('change', this.binding.mbinder, false);
		}
		else {
			this.addEventListener('input', this.binding.mbinder, false);
		}
		//update input now
		this.binding.ubinder.call(context);
	};

	//HTMLInputElement
	HTMLInputElement.prototype.bind = function(object, prop, to_model, to_ui) {
		this.unbind();
		//retrieve last relation
		const property = prop || this.name;
		const last_relation = Object.getLastObjectInPath(object, property);
		//update binding settings
		this.binding = {
			context: last_relation.object,
			dependencies: [{object: last_relation.object, properties: [last_relation.property]}],
			mbinder: update_relation(last_relation, to_model),
			ubinder: update_ui(last_relation, to_ui)
		};
		//update object when input is modified
		if(this.type === 'checkbox') {
			this.addEventListener('change', this.binding.mbinder, false);
		}
		else {
			this.addEventListener('input', this.binding.mbinder, false);
		}
		//update input when object is modified
		register(object, property);
		listener.listen(object, property, this.binding.ubinder, this);
		//update input now
		this.binding.ubinder.call(this, last_relation.object[last_relation.property] || '');
	};

	HTMLInputElement.prototype.unbind = function() {
		if(this.binding) {
			//remove ui listener
			if(this.type === 'checkbox') {
				this.removeEventListener('change', this.binding.mbinder, false);
			}
			else {
				this.removeEventListener('input', this.binding.mbinder, false);
			}
			//remove dependencies listeners
			this.binding.dependencies.forEach(function(dependency) {
				dependency.properties.forEach(function(property) {
					listener.unlisten(dependency.object, property, this.binding.ubinder);
				}, this);
			}, this);
			//delete binding
			delete this['binding'];
		}
	};

	//HTMLSelectElement
	HTMLSelectElement.prototype.bind = function(object, prop, to_model, to_ui) {
		this.unbind();
		//retrieve last relation
		const property = prop || this.name;
		const last_relation = Object.getLastObjectInPath(object, property);
		//update binding settings
		this.binding = {
			context: last_relation.object,
			dependencies: [{object: last_relation.object, properties: [last_relation.property]}],
			mbinder: update_relation(last_relation, to_model),
			ubinder: update_ui(to_ui)
		};
		//update object when input is modified
		this.addEventListener('change', this.binding.mbinder, false);
		//update input when object is modified
		register(last_relation.object, last_relation.property);
		listener.listen(last_relation.object, last_relation.property, this.binding.ubinder, this);
		//update select now
		this.binding.ubinder.call(this, last_relation.object[last_relation.property] || '');
	};

	HTMLSelectElement.prototype.unbind = function() {
		if(this.binding) {
			//remove ui listener
			this.removeEventListener('change', this.binding.mbinder, false);
			//remove dependencies listeners
			this.binding.dependencies.forEach(function(dependency) {
				dependency.properties.forEach(function(property) {
					listener.unlisten(dependency.object, property, this.binding.ubinder);
				}, this);
			}, this);
			//delete binding
			delete this['binding'];
		}
	};

	//HTMLElement
	HTMLElement.prototype.bindText = function(object, property, modifier) {
		this.bind(object, property, function(value) {this.textContent = modifier ? modifier.call(undefined, value) : (value || '');});
	};

	HTMLElement.prototype.bind = function(object, property, to_ui) {
		register(object, property);
		listener.listen(object, property, to_ui, this);
		//update element now
		to_ui.call(this, object[property]);
	};

	HTMLElement.prototype.bindArray = function(array, view) {
		register_array(array);
		this.onArrayAddElement = function(event) {
			if(event.array && event.array === array) {
				this.appendChild(view.call(this, event.element, event.array.length - 1));
			}
		};
		this.onArrayRemoveElement = function(event) {
			if(event.array && event.array === array) {
				this.removeChild(this.childNodes[event.index]);
			}
		};
		bindbus.register(this);
		//update element now
		this.empty();
		for(let i = 0; i < array.length; i++) {
			this.appendChild(view.call(this, array[i], i));
		}
	};

	//Text
	Text.prototype.bind = function(object, property, modifier) {
		register(object, property);
		listener.listen(object, property, function(value) {this.data = modifier ? modifier.call(undefined, value) : (value || '');}, this);
	};
})();

Node.prototype.autobind = function(obj) {
	const root_object = obj || window;
	this.querySelectorAll('[data-bind]').forEach(function(node) {
		const last_object = Object.getLastObjectInPath(root_object, node.dataset.bind);
		const object = last_object.object;
		const property = last_object.property;
		let modifier;
		if(node.dataset.bindModifier) {
			//modifier is a method of current object
			if(property && node.dataset.bindModifier in object[property]) {
				modifier = function(value) {return object[property][node.dataset.bindModifier].call(value);};
			}
			//modifier is a function in container
			else if(node.dataset.bindModifier in object) {
				modifier = function(value) {return object[node.dataset.bindModifier].bind(value);};
			}
		}
		node.bindText(object, property, modifier);
	});
};
