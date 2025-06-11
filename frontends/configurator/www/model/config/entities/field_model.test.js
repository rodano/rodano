import {FieldModel} from './field_model.js';
import {PossibleValue} from './possible_value.js';

export default async function test(bundle, assert) {
	bundle.begin();

	await bundle.describe('Attribute#getPossibleValue', async feature => {
		const attribute = new FieldModel({
			id: 'EMPLOYMENT'
		});

		const attribute_possible_value_1 = new PossibleValue({
			id: 'EMPLOYED',
			shortname: {
				en: 'Employed'
			},
			specify: false
		});
		attribute.possibleValues.push(attribute_possible_value_1);
		const attribute_possible_value_2 = new PossibleValue({
			id: 'RETIRED',
			shortname: {
				en: 'Retired'
			},
			specify: false
		});
		attribute.possibleValues.push(attribute_possible_value_2);

		await feature.it('retrieves possible values properly', () => {
			assert.equal(attribute.getPossibleValue('EMPLOYED'), attribute_possible_value_1, 'Retrieve possible value with id "EMPLOYED" in attribute gives the good possible value');
			assert.equal(attribute.getPossibleValue('RETIRED').id, 'RETIRED', 'Retrieve a possible value with id "RETIRED" in attribute gives a possible value which id is "RETIRED"');
		});
	});

	bundle.end();
}
