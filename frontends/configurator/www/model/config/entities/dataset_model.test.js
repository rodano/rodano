import {DatasetModel} from './dataset_model.js';
import {FieldModel} from './field_model.js';

export default async function test(bundle, assert) {
	bundle.begin();

	await bundle.describe('DatasetModel#getFieldModel and DatasetModel#getExportableFieldModels', async feature => {
		const dataset_model = new DatasetModel({
			id: 'PATIENT_DOCUMENT'
		});
		const field_model_1 = new FieldModel({
			id: 'GENDER',
			exportable: true
		});
		field_model_1.datasetModel = dataset_model;
		dataset_model.fieldModels.push(field_model_1);

		await feature.it('retrieves attributes properly', () => {
			assert.equal(dataset_model.getFieldModel('GENDER'), field_model_1, 'Retrieve field model with id "GENDER" in dataset model gives the good field model');
			assert.equal(dataset_model.getExportableFieldModels()[0], field_model_1, 'First field model of exportables field models for dataset model with id "PATIENT_DOCUMENT" is field model with id "GENDER"');
		});
	});

	bundle.end();
}
