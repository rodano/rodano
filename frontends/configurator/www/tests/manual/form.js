import '../../basic-tools/extension.js';
import '../../basic-tools/dom_extension.js';
import '../../tools/custom_dom_extension.js';

document.addEventListener('DOMContentLoaded', function() {
	const properties = document.getElementById('properties');
	const source = /**@type HTMLFormElement*/ (document.getElementById('source'));

	let person = {firstname: 'Luke', lastname: 'Skywalker', gender: 'male', age: 28, offline: true};
	update_source();

	function update_source() {
		source['data'].value = JSON.stringify(person, undefined, '\t');
	}

	properties.addEventListener(
		'submit',
		function(event) {
			event.preventDefault();
			this.writeToObject(person);
			update_source();
		}
	);

	source.addEventListener(
		'submit',
		function(event) {
			event.preventDefault();
			person = JSON.parse(this['data'].value);
			properties.readFromObject(person);
		}
	);

	document.getElementById('check').addEventListener(
		'click',
		function() {
			const dirty = properties.isDirty(person);
			document.getElementById('dirty').textContent = dirty;
		}
	);
});
