import '../basic-tools/extension.js';

(function() {

	function get_element_attributes(element, object) {
		const value = Object.getObjectPathValue(object, element.name);
		//elements that deal with JSON
		if(element.dataset.type === 'json' || ['APP-LOCALIZED-INPUT', 'APP-PALETTE'].includes(element.tagName)) {
			return {value: value ? JSON.stringify(value) : ''};
		}
		//inputs
		if(element.tagName === 'INPUT') {
			const type = element.type || element.getAttribute('type');
			switch(type) {
				case 'number':
					return value !== undefined ? {valueAsNumber: value} : {value: ''};
				case 'date':
					return value !== undefined ? {valueAsDate: value} : {value: ''};
				case 'checkbox':
					return {checked: !!value};
			}
		}
		//text inputs and other form elements
		return {value: value || ''};
	}

	function get_element_value(element) {
		//elements that deal with JSON
		if(element.dataset.type === 'json' || ['APP-LOCALIZED-INPUT', 'APP-PALETTE'].includes(element.tagName)) {
			return element.value ? JSON.parse(element.value) : undefined;
		}
		//inputs
		if(element.tagName === 'INPUT') {
			const type = element.type || element.getAttribute('type');
			switch(type) {
				case 'number':
					return element.value ? element.valueAsNumber : undefined;
				case 'date':
					return element.value ? element.valueAsDate : undefined;
				case 'checkbox':
					return element.checked;
			}
		}
		//text input and other form elements
		return element.value || undefined;
	}

	//HTMLFormElement
	HTMLFormElement.prototype.readFromObject = function(object) {
		this.elements
			//don't consider elements that are here just for design (those that don't have a name)
			.filter(e => e.name)
			//consider disabled elements (when reading the object)
			//.filter(e => !e.disabled)
			.forEach(element => {
				const attributes = get_element_attributes(element, object);
				for(const attribute in attributes) {
					if(attributes.hasOwnProperty(attribute)) {
						element[attribute] = attributes[attribute];
					}
				}
			});
	};

	HTMLFormElement.prototype.writeToObject = function(object) {
		this.elements
			//don't consider elements that are here just for design (those that don't have a name)
			.filter(e => e.name)
			//don't consider disabled elements (only when writing the object)
			.filter(e => !e.disabled)
			.forEach(element => {
				const last_relation = Object.getLastObjectInPath(object, element.name);
				last_relation.object[last_relation.property] = get_element_value(element);
			});
	};

	HTMLFormElement.prototype.getUnsavedData = function(object) {
		const unsaved_data = [];
		this.elements
			//don't consider elements that are here just for design (those don't have a name)
			.filter(e => e.name)
			//don't consider disabled elements
			.filter(e => !e.disabled)
			//consider elements that are hidden (those don't have any offset parent)
			//some elements such as type="hidden" are hidden but required
			//.filter(e => e.offsetParent !== null)
			.forEach(element => {
				const last_relation = Object.getLastObjectInPath(object, element.name);
				const object_value = last_relation.object[last_relation.property];
				const element_value = get_element_value(element);
				if(!Object.equals(object_value, element_value)) {
					unsaved_data.push({element: element, formValue: element_value, objectValue: object_value});
				}
			});
		return unsaved_data;
	};

	HTMLFormElement.prototype.isDirty = function(object) {
		return !this.getUnsavedData(object).isEmpty();
	};
})();
