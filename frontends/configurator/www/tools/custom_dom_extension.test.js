import '../basic-tools/dom_extension.js';
import './custom_dom_extension.js';

export default async function test(bundle, assert) {
	bundle.begin();

	//form
	const form = document.createElement('form');
	const firstname = document.createFullElement('input', {name: 'firstname'});
	const lastname = document.createFullElement('input', {name: 'lastname'});
	const gender = document.createFullElement('select', {name: 'gender'});
	gender.appendChild(document.createFullElement('option', {value: 'F'}, 'Female'));
	gender.appendChild(document.createFullElement('option', {value: 'M'}, 'Male'));
	const birthdate = document.createFullElement('input', {name: 'birthdate', type: 'date'});
	const vaccinated = document.createFullElement('input', {name: 'vaccinated', type: 'checkbox'});
	const vaccinations = document.createFullElement('input', {name: 'vaccinations', type: 'number'});
	const street = document.createFullElement('input', {name: 'address.street'});
	const zipcode = document.createFullElement('input', {name: 'address.zipcode', type: 'number'});
	const children = document.createFullElement('input', {name: 'children', 'data-type': 'json'});
	const car = document.createFullElement('input', {name: 'car', 'data-type': 'json'});
	form.appendChildren([firstname, lastname, gender, birthdate, vaccinated, vaccinations, street, zipcode, children, car]);

	const people_1 = {
		firstname: 'John',
		lastname: 'Doe',
		gender: 'M',
		birthdate: new Date(Date.UTC(1970, 5, 10)),
		vaccinated: true,
		vaccinations: 5,
		address: {street: '2, market street', zipcode: 1206, city: 'Geneva'},
		children: ['Tom', 'Donald'],
		car: {brand: 'Toyota', model: 'Prius', horsepower: 150},
		eyes_color: 'blue'
	};

	const people_2 = {
		firstname: 'Maria',
		lastname: 'Moe',
		gender: 'F',
		birthdate: new Date(Date.UTC(1981, 4, 8, 10, 42, 30)),
		vaccinated: false,
		address: {street: '60, ocean avenue', zipcode: 1253, city: 'Geneva'},
		children: ['Robert', 'Josh'],
		car: {brand: 'Nissan', model: 'Leaf', horsepower: 110},
		eyes_color: 'brown'
	};

	await bundle.describe('Form#readFromObject', async feature => {
		await feature.it('updates a form with properties of an object', () => {
			form.readFromObject(people_1);

			assert.equal(firstname.value, 'John', 'Standard input has been updated');
			assert.equal(lastname.value, 'Doe', 'Standard input has been updated');
			assert.equal(gender.value, 'M', 'Select has been updated');
			assert.equal(birthdate.value, '1970-06-10', 'Date input has been updated');
			assert.ok(vaccinated.checked, 'Checkbox input has been updated');
			assert.equal(vaccinations.value, '5', 'Number input has been updated');
			assert.equal(street.value, '2, market street', 'Standard input for sub property has been updated');
			assert.equal(zipcode.value, '1206', 'Number input for sub property has been updated');
			assert.equal(children.value, '["Tom","Donald"]', 'Standard input for array has been updated');
			assert.equal(car.value, '{"brand":"Toyota","model":"Prius","horsepower":150}', 'Standard input for object has been updated');

			form.readFromObject(people_2);

			assert.equal(firstname.value, 'Maria', 'Standard input has been updated');
			assert.equal(lastname.value, 'Moe', 'Standard input has been updated');
			assert.equal(gender.value, 'F', 'Select has been updated');
			assert.equal(birthdate.value, '1981-05-08', 'Date input has been updated');
			assert.notOk(vaccinated.checked, 'Checkbox input has been updated');
			assert.equal(vaccinations.value, '', 'Number input has been updated');
			assert.equal(street.value, '60, ocean avenue', 'Standard input for sub property has been updated');
			assert.equal(zipcode.value, '1253', 'Number input for sub property has been updated');
			assert.equal(children.value, '["Robert","Josh"]', 'Standard input for array has been updated');
			assert.equal(car.value, '{"brand":"Nissan","model":"Leaf","horsepower":110}', 'Standard input for object has been updated');
		});
	});

	await bundle.describe('Form#writeToObject', async feature => {
		await feature.it('updates an object with values of a form', () => {
			firstname.value = 'Marie';
			birthdate.value = '1982-06-08';
			vaccinated.checked = true;
			vaccinations.value = '3';
			street.value = '65, ocean avenue';
			children.value = '["Adam", "Ryan"]';
			form.writeToObject(people_2);

			assert.equal(people_2.firstname, 'Marie', 'String has been updated');
			assert.equal(people_2.lastname, 'Moe', 'Unmodified string has not been updated');
			assert.equal(people_2.gender, 'F', 'Unmodified string has not been updated');
			assert.equal(people_2.birthdate.getTime(), new Date('1982-06-08').getTime(), 'Date has been updated');
			assert.ok(people_2.vaccinated, 'Boolean has been updated');
			assert.equal(people_2.vaccinations, 3, 'Number has been updated');
			assert.equal(people_2.address.street, '65, ocean avenue', 'String from sub property has been updated');
			assert.equal(people_2.address.zipcode, 1253, 'Unmodified number from sub property has not been updated');
			assert.similar(people_2.children, ['Adam', 'Ryan'], 'Array has been updated');
			assert.similar(people_2.car, {brand: 'Nissan', model: 'Leaf', horsepower: 110}, 'Unmodified object has not been updated');
		});
	});

	bundle.end();
}
