class TypeDoesNotMatch extends Error {
	constructor(container, value, type) {
		super(`Value ${value} does not match the expected type "${type}" in ${container.constructor.name}`);
	}
}

/**
 * @typedef {Function} Factory
 * @param {string} entity - String containing the object entity
 * @param {string} [container] - The container of the object that is currently being revived
 * @returns {Function} - The constructor associated to the entity
 */

class Reviver {
	static ENTITY_PROPERTY = 'className';

	constructor(parameters) {
		//String: property used to know which property of plain objects must be used to determine their entity
		this.entityProperty = Reviver.ENTITY_PROPERTY;
		//factory used to create objects if you want to create objects manually
		//Function: return an instance for entity
		//entity, container => an instance of the class matching the parameter entity
		/**@type {Factory} */
		this.factory;
		//delegate creation of the objects to the reviver, only providing a constructor function
		//Function: return a constructor for an entity that will be used to create the object
		//entity, plain_object => a function that will be used as a constructor
		this.entitiesConstructors = undefined;
		//specify properties that can be imported can be left undefined
		//if specified, only properties returned by this function are imported
		//Function: must return a map of all properties for an entity
		//entity => map of entities properties
		//the keys of the map must be the name of the properties and the values a property object
		//a property object can have following fields:
		//type: the type of the property (string, number, boolean, array or object or the id of an entity)
		//back_reference: a boolean specifying that this property must be set to the container
		this.entitiesProperties = undefined;
		//Boolean: do not import a property that does not match its type definition
		//this works only if properties have a type
		this.enforceTypes = false;
		//Boolean: preserve unknown properties
		//keep properties that are not returned by the "entitiesProperties" function
		this.preserveUnknownProperties = false;
		//Boolean: preserve entity property
		//"preserveUnknownProperties" works even if the "entityProperty" is not returned by the "entitiesProperties" function
		this.preserveEntityProperty = false;
		//Boolean: write to console properties which are present in plain object but have not been defined by this.entitiesProperties function
		//show something only when "preserveUnknownProperties" is set to false
		this.debug = false;
		//Function: callback to apply for every objects after the revival
		//() => revived_object, entity, container, plain_object
		this.callback = undefined;

		//bind parameters
		for(const parameter in parameters) {
			this[parameter] = parameters[parameter];
		}

		//check required conditions
		if(!this.factory && !this.entitiesConstructors) {
			throw 'A factory or a function which returns entity constructor is required';
		}

		//build factory using constructor function
		if(!this.factory && this.entitiesConstructors) {
			this.factory = function(entity) {
				const builder = this.entitiesConstructors(entity);
				if(!builder) {
					throw new Error(`Missing constructor for entity ${entity}`);
				}
				return new builder();
			};
		}
	}
	/**
	 * @param {object} object - The object to revive
	 * @param {any} [container] - The container of the object that will be bound to object back references
	 * @param {string} [type] - Awaited type of object
	 * @returns {any} - The revived object
	 */
	revive(object, container, type) {
		//nothing to do with undefined or null objects
		if(object === undefined || object === null) {
			return object;
		}
		//array of objects
		if(Array.isArray(object)) {
			if(this.enforceTypes && type && type !== 'array') {
				throw new TypeDoesNotMatch(container, object, type);
			}
			return object.map(o => this.revive(o, container));
		}
		//object
		if(typeof object === 'object') {
			//typed object
			if(object[this.entityProperty]) {
				//create revived object
				//send object values directly to the factory to be able to construct the object directly with its values
				//this allows for immutables objects
				const revived_object = this.factory(object[this.entityProperty], object);
				//retrieve properties
				const declared_properties = this.entitiesProperties ? this.entitiesProperties(object[this.entityProperty]) : undefined;
				//import properties, looping only on own properties (property must no be inherited)
				for(const [property, value] of Object.entries(object)) {
					//check that current property has been declared or unknown properties are preserved or this is the entity property
					if(this.preserveUnknownProperties || this.preserveEntityProperty && property === this.entityProperty || declared_properties.hasOwnProperty(property)) {
						//revive may fail du to incompatible types
						revived_object[property] = this.revive(value, revived_object, declared_properties[property]?.type);
					}
					else if(this.debug && !declared_properties.hasOwnProperty(property)) {
						//warn user that a property from the object has not been declared in class
						console.log(`Property ${property} (value: ${value}) does not exist in ${object[this.entityProperty]} and has not been assigned for object`, object);
					}
				}
				//set back references only available when entities properties are provided
				if(declared_properties && container) {
					//there can be more than one back reference for entities linked to two different parent entities
					const back_references = Object.entries(declared_properties).filter(e => e[1].back_reference);
					if(back_references.length > 0) {
						let back_reference_set = false;
						for(const [name, definition] of back_references) {
							//check if back reference match property type
							//the goal is to associate the container to the right back reference
							//some entities may have more than one reference if they are used in different containers
							//this is only possible if "entitiesConstructors" is used
							if(!this.entitiesConstructors) {
								back_reference_set = true;
								revived_object[name] = container;
							}
							else {
								//if type is specified, set back reference only if type of container matches declared type
								if(definition.type) {
									if(definition.type === container.constructor.name) {
										back_reference_set = true;
										revived_object[name] = container;
									}
								}
								else {
									back_reference_set = true;
									revived_object[name] = container;
								}
							}
						}
						if(this.debug && !back_reference_set) {
							//warn user that an object has back references but none could be set
							console.info('No back reference have been set for object', object);
						}
					}
					//exclude this property for enumeration
					/*var descriptor = Object.getOwnPropertyDescriptor(proto_object, property);
					descriptor.enumerable = false;
					Object.defineProperty(proto_object, property, descriptor);*/
				}
				if(this.callback) {
					this.callback(revived_object, object[this.entityProperty], container, object);
				}
				return revived_object;
			}
			//map of objects
			else {
				const proto_object = {};
				for(const [key, value] of Object.entries(object)) {
					//revive each value keeping a reference to container
					proto_object[key] = this.revive(value, container);
				}
				return proto_object;
			}
		}
		//primitive data type
		if(this.enforceTypes && type && typeof(object) !== type) {
			throw new TypeDoesNotMatch(container, object, type);
		}
		//nothing to revive with primitive data type
		return object;
	}
}

export {Reviver};
