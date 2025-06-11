import './basic-tools/dom_extension.js';
import './tools/custom_dom_extension.js';

import {Languages} from './languages.js';
import {StudyHandler} from './study_handler.js';

function check_property(source_object, source_path) {
	let object = source_object;
	let path = source_path;
	while(path.includes('.')) {
		const current = path.substring(0, path.indexOf('.'));
		const properties = object.constructor.getProperties();
		if(properties && !properties.hasOwnProperty(current)) {
			console.log(`Property ${current} has no been declared in entity ${object.getEntity().name}`);
		}
		object = Function.isFunction(object[current]) ? object[current]() : object[current];
		path = path.substring(path.indexOf('.') + 1);
	}
}

function object_value(object, value_property) {
	if(Function.isFunction(object[value_property])) {
		return object[value_property]();
	}
	return object[value_property];
}

function object_label(object, label_property) {
	if(Object.isObject(object[label_property])) {
		return object[label_property][Languages.GetLanguage()];
	}
	else if(Function.isFunction(object[label_property])) {
		return object[label_property](Languages.GetLanguage());
	}
	return object[label_property];
}

function build_entries(objects, value_prop, label_prop, disable_auto_sort) {
	const value_property = value_prop || 'id';
	const label_property = label_prop || 'getLocalizedShortname';

	//TODO remove this and use an array of entries to keep order
	//sort elements
	const sorted_objects = [...objects];
	if(!disable_auto_sort && objects.length > 0 && objects[0].constructor.getComparator) {
		sorted_objects.sort(objects[0].constructor.getComparator(Languages.GetLanguage()));
	}

	return sorted_objects.map(o => [object_value(o, value_property), object_label(o, label_property)]);
}

export const FormHelpers = {
	UpdateForm: function(form, object) {
		form.elements.filter(e => e.name).forEach(function(element) {
			//check if properties have been declared for object
			check_property(object, element.name);
			//reset validation
			element.setCustomValidity('');
		});
		form.readFromObject(object);
	},

	UpdateObject: function(object, form) {
		//check if properties have been declared for object
		form.elements.filter(e => e.name).forEach(e => check_property(object, e.name));
		form.writeToObject(object);
	},

	FillSelect: function(select, objects, blank_option, selected_values, options) {
		const entries = build_entries(objects, options?.value_property, options?.label_property, options?.disable_auto_sort);
		select.fill(entries, blank_option, selected_values);
	},

	FillSelectEnum: function(select, enumerable, blank_option, selected_values) {
		const entries = Object.entries(enumerable).map(e => [e[0], e[1].label || e[1]]);
		select.fill(entries, blank_option, selected_values);
	},

	FillPalette: function(palette, objects, value_property, label_property, selected_values) {
		palette.fill(build_entries(objects, value_property, label_property));
		palette.setValues(selected_values);
	},

	FillConstrainedInput: function(input, objects, value_property, label_property, selected_value) {
		input.fill(build_entries(objects, value_property, label_property));
		input.value = selected_value;
	},

	FillLocalizedInput: function(input, custom_languages, values) {
		//TODO remove languages parameter because languages can be retrieved directly from study
		const study = StudyHandler.GetStudy();
		const languages = study.languageIds.map(l => study.getLanguage(l));
		input.fill(build_entries(languages));
		input.setValues(values);
	},

	EnhanceInputMapString: function(input) {
		//clean previous enhancement
		if(input.nextElementSibling) {
			input.parentNode.removeChild(input.nextElementSibling);
		}
		input.style.display = 'none';

		const container = document.createFullElement('div', {style: 'float: left;'});
		input.parentNode.appendChild(container);

		const entries = document.createFullElement('div');
		container.appendChild(entries);

		//entry
		function draw_entry(key, value) {
			const entry_paragraph = document.createFullElement('p', {'class': 'inline', style: 'padding-top: 0;'});
			const key_label = document.createFullElement('label', {}, 'Key');
			key_label.appendChild(document.createFullElement('input', {value: key || ''}, undefined, {change: update_input}));
			entry_paragraph.appendChild(key_label);
			const value_label = document.createFullElement('label', {style: 'margin-left: 1rem;'}, 'Value');
			value_label.appendChild(document.createFullElement('input', {value: value || ''}, undefined, {change: update_input}));
			entry_paragraph.appendChild(value_label);
			const delete_button = document.createFullElement('button', {type: 'button', title: 'Delete', class: 'image'});
			delete_button.appendChild(document.createFullElement('img', {src: 'images/cross.png', alt: 'Delete'}));
			delete_button.addEventListener(
				'click',
				function() {
					this.parentNode.parentNode.removeChild(this.parentNode);
					update_input();
				}
			);
			entry_paragraph.appendChild(delete_button);
			return entry_paragraph;
		}

		//add button
		const add_entry_button = document.createFullElement('button', {type: 'button', style: 'margin-bottom: 8px;'}, 'Add entry');
		add_entry_button.addEventListener(
			'click',
			function() {
				entries.appendChild(draw_entry());
				update_input();
			}
		);
		container.appendChild(add_entry_button);

		//update input value
		function update_input() {
			const map = {};
			for(let i = 0; i < entries.childNodes.length; i++) {
				const inputs = entries.childNodes[i].querySelectorAll('input');
				if(inputs[0].value && inputs[1].value) {
					map[inputs[0].value] = inputs[1].value;
				}
			}
			input.value = JSON.stringify(map);
		}

		//update current parameters
		const map = input.value ? JSON.parse(input.value) : {};
		for(const key in map) {
			if(map.hasOwnProperty(key)) {
				entries.appendChild(draw_entry(key, map[key]));
			}
		}
	},

	//TODO rename this to EnhanceInputListString
	EnhanceInputSimpleListString: function(input) {
		//clean previous enhancement
		if(input.nextElementSibling) {
			input.parentNode.removeChild(input.nextElementSibling);
		}
		input.style.display = 'none';

		const container = document.createFullElement('div', {style: 'float: left;'});
		input.parentNode.appendChild(container);

		const entries = document.createFullElement('div');
		container.appendChild(entries);

		//entry
		function draw_entry(value) {
			const entry_paragraph = document.createFullElement('p', {'class': 'inline', style: 'padding-top: 0;'});
			entry_paragraph.appendChild(document.createFullElement('label', {}, 'Value '));
			entry_paragraph.appendChild(document.createFullElement('input', {value: value || ''}, undefined, {change: update_input}));
			const delete_entry_button = document.createFullElement('img', {src: 'images/cross.png', alt: 'Delete', title: 'Delete', style: 'cursor: pointer; vertical-align: middle;'});
			delete_entry_button.addEventListener(
				'click',
				function() {
					this.parentNode.parentNode.removeChild(this.parentNode);
					update_input();
				}
			);
			entry_paragraph.appendChild(delete_entry_button);
			return entry_paragraph;
		}

		//add button
		const add_entry_button = document.createFullElement('button', {type: 'button', style: 'margin-bottom: 8px;'}, 'Add entry');
		add_entry_button.addEventListener(
			'click',
			function() {
				entries.appendChild(draw_entry());
				update_input();
			}
		);
		container.appendChild(add_entry_button);

		//update input value
		function update_input() {
			const list = entries.childNodes.map(n => n.querySelector('input')).map(i => i.value).filter(i => !!i);
			input.value = JSON.stringify(list);
		}

		//update current parameters
		const list = input.value ? JSON.parse(input.value) : [];
		list.map(draw_entry).forEach(Node.prototype.appendChild, entries);
	},

	EnhanceInputAutocomplete: function(input, autocomplete) {
		//clean previous enhancement
		if(input.nextElementSibling) {
			input.parentNode.removeChild(input.nextElementSibling);
			input.parentNode.removeChild(input.nextElementSibling);
		}
		input.style.display = 'none';

		//create input and datalist fields
		const input_autocomplete = document.createFullElement('input', {type: 'text', list: `${input.id}_datalist`, 'class': input.getAttribute('class'), style: 'width: 100px;'});
		const datalist_autocomplete = document.createFullElement('datalist', {id: `${input.id}_datalist`});

		function use_results(results) {
			datalist_autocomplete.empty();
			for(let i = 0; i < results.length; i++) {
				datalist_autocomplete.appendChild(document.createFullElement('option', {value: results[i].id}, results[i].label));
			}
		}

		function hop(event) {
			console.log(`hop ${event}`);
		}
		datalist_autocomplete.addEventListener('click', hop, true);
		datalist_autocomplete.addEventListener('change', hop, true);
		datalist_autocomplete.addEventListener('input', hop, true);
		datalist_autocomplete.addEventListener('select', hop, true);
		datalist_autocomplete.addEventListener('submit', hop, true);

		input_autocomplete.addEventListener(
			'input',
			function() {
				autocomplete(this.value, use_results);
			}
		);

		input_autocomplete.addEventListener(
			'keydown',
			function(event) {
				if(event.which === 13) { //|| event.which === 39) {
					//retrieve label
					let label;
					for(let i = 0; i < datalist_autocomplete.children.length; i++) {
						if(datalist_autocomplete.children[i].value === this.value) {
							label = datalist_autocomplete.children[i].textContent;
							break;
						}
					}
					//add to hidden input
					const selection = input.value ? JSON.parse(input.value) : [];
					selection.push(this.value);
					input.value = JSON.stringify(selection);
					//add label
					const element = document.createFullElement('span', {style: 'padding: 2px; float: left;'}, label);
					element.value = this.value;
					const element_remove = document.createFullElement('img', {src: 'images/cross.png', alt: 'Remove', style: 'cursor: pointer; padding: 0 2px; vertical-align: top;'});
					element_remove.addEventListener(
						'click',
						function() {
							//remove to hidden input
							const selection = input.value ? JSON.parse(input.value) : [];
							selection.removeElement(this.parentNode.value);
							input.value = JSON.stringify(selection);
							//remove label
							this.parentNode.parentNode.removeChild(this.parentNode);
						}
					);
					element.appendChild(element_remove);
					input.parentNode.appendChild(element);
					input_autocomplete.value = '';
				}
			}
		);

		input.parentNode.appendChild(input_autocomplete);
		input.parentNode.appendChild(datalist_autocomplete);
	}
};
