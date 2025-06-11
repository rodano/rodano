//for the Node dependency to be downloaded first to make tests run
import {Node} from './node.js'; //eslint-disable-line no-unused-vars
import {DifferenceArrayElement, DifferenceArrayLength, DifferenceChild, DifferenceProperty} from './compare.js'; //eslint-disable-line no-unused-vars
import {Entities} from './entities.js';
import {EventModel} from './entities/event_model.js';
import {ScopeModel} from './entities/scope_model.js';
import {Study} from './entities/study.js';

export default async function test(bundle, assert) {
	bundle.begin();

	//study
	const study_1 = new Study();
	study_1.id = 'TEST';

	//scope models
	const scope_model_11 = new ScopeModel();
	scope_model_11.id = 'PATIENT';
	scope_model_11.study = study_1;
	scope_model_11.datasetModelIds = ['ADDRESS', 'DEMOGRAPHICS'];
	study_1.scopeModels.push(scope_model_11);

	const scope_model_12 = new ScopeModel();
	scope_model_12.id = 'CENTER';
	scope_model_12.study = study_1;
	study_1.scopeModels.push(scope_model_12);

	//event models
	const event_model_11 = new EventModel();
	event_model_11.id = 'BASELINE_VISIT';
	event_model_11.scopeModel = scope_model_11;
	scope_model_11.eventModels.push(event_model_11);

	const event_model_12 = new EventModel();
	event_model_12.id = 'FOLLOW_UP_VISIT';
	event_model_12.scopeModel = scope_model_11;
	scope_model_11.eventModels.push(event_model_12);

	const event_model_13 = new EventModel();
	event_model_13.id = 'TELEPHONE_VISIT';
	event_model_13.scopeModel = scope_model_11;
	scope_model_11.eventModels.push(event_model_13);

	const event_model_14 = new EventModel();
	event_model_14.id = 'TERMINATION_VISIT';
	event_model_14.scopeModel = scope_model_11;
	event_model_14.blockedEventModelIds = ['BASELINE_VISIT', 'FOLLOW_UP_VISIT', 'TELEPHONE_VISIT'];
	scope_model_11.eventModels.push(event_model_14);

	//study
	const study_2 = new Study();
	study_2.id = 'TEST';

	//scope models
	const scope_model_21 = new ScopeModel();
	scope_model_21.id = 'PATIENT';
	scope_model_21.study = study_2;
	scope_model_21.datasetModelIds = ['ADDRESS'];
	study_2.scopeModels.push(scope_model_21);

	//event models
	const event_model_21 = new EventModel();
	event_model_21.id = 'BASELINE_VISIT';
	event_model_21.scopeModel = scope_model_21;
	scope_model_21.eventModels.push(event_model_21);

	const event_models_22 = new EventModel();
	event_models_22.id = 'FOLLOW_UP_VISIT';
	event_models_22.scopeModel = scope_model_21;
	scope_model_21.eventModels.push(event_models_22);

	const event_models_23 = new EventModel();
	event_models_23.id = 'PHONE_VISIT';
	event_models_23.scopeModel = scope_model_21;
	scope_model_21.eventModels.push(event_models_23);

	const event_models_24 = new EventModel();
	event_models_24.id = 'TERMINATION_VISIT';
	event_models_24.scopeModel = scope_model_21;
	event_models_24.blockedEventModelIds = ['BASELINE_VISIT', 'FOLLOW_UP_VISIT', 'PHONE_VISIT'];
	scope_model_21.eventModels.push(event_models_24);

	/*var scope_model_22 = new ScopeModel();
	scope_model_22.id = 'CENTER';
	scope_model_22.study = study_2;
	study_2.scopeModels.push(scope_model_22);*/

	await bundle.describe('Node#compare', async feature => {
		await feature.it('compares 2 studies properly', () => {
			const differences = study_2.compare(study_1);
			assert.equal(differences.length, 4, 'There are 4 differences');
			let difference;

			difference = /**@type {DifferenceArrayLength}*/ (differences[0]);
			assert.equal(difference.constructor.name, 'DifferenceArrayLength', 'Array length difference is spotted');
			assert.equal(difference.node, scope_model_21, 'Array length difference is related to good node');
			assert.equal(difference.property, 'datasetModelIds', 'Array length difference is related to good property');

			difference = /**@type {DifferenceProperty}*/ (differences[1]);
			assert.equal(difference.constructor.name, 'DifferenceProperty', 'Property difference is spotted');
			assert.equal(difference.node, event_models_23, 'Property difference is related to good node');
			assert.equal(difference.property, 'id', 'Property difference is related to good property');
			assert.equal(difference.value, 'PHONE_VISIT', 'Property difference detects good source value');
			assert.equal(difference.otherValue, 'TELEPHONE_VISIT', 'Property difference detects good target value');

			difference = /**@type {DifferenceArrayElement}*/ (differences[2]);
			assert.equal(difference.constructor.name, 'DifferenceArrayElement', 'Array element difference is spotted');
			assert.equal(difference.node, event_models_24, 'Array element difference is related to good node');
			assert.equal(difference.property, 'blockedEventModelIds', 'Array element difference is related to good property');
			assert.equal(difference.element, 'PHONE_VISIT', 'Array element difference detects good source value');
			assert.equal(difference.otherElement, 'TELEPHONE_VISIT', 'Array element difference detects good target value');

			difference = /**@type {DifferenceChild}*/ (differences[3]);
			assert.equal(difference.constructor.name, 'DifferenceChild', 'Child difference is spotted');
			assert.equal(difference.node, study_2, 'Child difference is related to good node');
			assert.equal(difference.childEntity, Entities.ScopeModel, 'Child difference is related to good entity');
			assert.equal(difference.childId, 'CENTER', 'Child difference is related to good child id');
		});
	});

	bundle.end();
}
