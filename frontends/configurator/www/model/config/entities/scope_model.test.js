import {Study} from './study.js';
import {ScopeModel} from './scope_model.js';
import {EventModel} from './event_model.js';

export default async function test(bundle, assert) {
	bundle.begin();

	await bundle.describe('ScopeModel#isLeaf', async feature => {
		await feature.it('works properly', () => {
			const study = new Study();
			const scope_model = new ScopeModel({
				id: 'CENTER',
				shortname: {
					en: 'Center',
					fr: 'Centre'
				}
			});
			scope_model.study = study;
			study.scopeModels.push(scope_model);

			assert.ok(scope_model.isLeaf(), 'Scope model with id "CENTER" is the leaf scope');
			assert.equal(scope_model.study.scopeModels[0], scope_model);

			const scope_model_2 = new ScopeModel({
				id: 'PATIENT',
				shortname: {
					en: 'Patient',
					fr: 'Patient'
				}
			});
			scope_model_2.parentIds = ['CENTER'];
			scope_model_2.defaultParentId = 'CENTER';
			study.scopeModels.push(scope_model_2);
			scope_model_2.study = study;

			assert.notOk(scope_model.isLeaf(), 'Scope model with id "CENTER" is no longer the leaf scope');
			assert.ok(scope_model_2.isLeaf(), 'Scope model with id "PATIENT" is the leaf scope');
			assert.equal(scope_model_2.study.scopeModels[1], scope_model_2);
		});
	});

	await bundle.describe('ScopeModel#getScopeModelParents, ScopeModel#getScopeModelDefaultParent, ScopeModel#getScopeModelAncestors, ScopeModel#getScopeModelChildren and ScopeModel#getScopeModelDescendants', async feature => {
		const study = new Study();
		const scope_model = new ScopeModel({
			id: 'PATIENT',
			shortname: {
				en: 'Patient',
				fr: 'Patient'
			}
		});

		//add scope model in study
		scope_model.study = study;
		study.scopeModels.push(scope_model);

		const scope_model_2 = new ScopeModel({
			id: 'CENTER',
			shortname: {
				en: 'Center',
				fr: 'Centre'
			}
		});
		scope_model_2.study = study;
		study.scopeModels.push(scope_model_2);

		//scope model relations
		scope_model.parentIds = ['CENTER'];
		scope_model.defaultParentId = 'CENTER';

		await feature.it('retrieves parents properly', () => {
			assert.equal(scope_model.getScopeModelParents().length, 1, 'Scope model with id "PATIENT" has only one parent');
			assert.ok(scope_model_2.getScopeModelParents().isEmpty(), 'Scope model with id "CENTER" does not have parent');
			assert.equal(scope_model.getScopeModelDefaultParent(), scope_model_2, 'Scope model with id "CENTER" is the default parent of scope model with id "PATIENT"');
			assert.equal(scope_model.getScopeModelParents()[0], scope_model_2, 'Scope model with id "CENTER" is the parent of scope model with id "PATIENT"');
		});

		const scope_model_3 = new ScopeModel({
			id: 'COUNTRY',
			shortname: {
				en: 'Country',
				fr: 'Pays'
			}
		});
		scope_model_3.study = study;
		study.scopeModels.push(scope_model_3);

		//scope model relations
		scope_model_2.parentIds = ['COUNTRY'];

		await feature.it('retrieves parents and ancestors properly', () => {
			assert.equal(scope_model.getScopeModelParents().length, 1, 'Scope model with id "PATIENT" has only one parent');
			assert.equal(scope_model_2.getScopeModelParents().length, 1, 'Scope model with id "CENTER" has only one parent');
			assert.equal(scope_model_2.getScopeModelParents()[0], scope_model_3, 'Scope model with id "COUNTRY" is a parent of scope model with id "CENTER"');
			assert.equal(scope_model.getScopeModelAncestors().length, 2, 'Scope model with id "PATIENT" has three ancestors');
			assert.equal(scope_model.getScopeModelAncestors()[0], scope_model_2, 'Scope model with id "CENTER" is an ancestor of scope model with id "PATIENT"');
			assert.equal(scope_model.getScopeModelAncestors()[1], scope_model_3, 'Scope model with id "COUNTRY" is an ancestor of scope model with id "PATIENT"');
		});

		const scope_model_4 = new ScopeModel({
			id: 'STUDY',
			shortname: {
				en: 'Study',
				fr: 'Etude'
			}
		});
		scope_model_4.study = study;
		study.scopeModels.push(scope_model_4);

		scope_model_3.parentIds = ['STUDY'];

		const scope_model_5 = new ScopeModel({
			id: 'SUBSTUDY',
			shortname: {
				en: 'Substudy',
				fr: 'Sous-étude'
			}
		});
		scope_model_5.study = study;
		study.scopeModels.push(scope_model_5);

		//scope model relations
		scope_model_5.parentIds = ['STUDY'];
		scope_model.parentIds.push('SUBSTUDY');

		await feature.it('retrieves parents, ancestors, children and descendants properly', () => {
			assert.equal(scope_model.getScopeModelParents().length, 2, 'Scope model with id "PATIENT" has 2 parents');
			assert.equal(scope_model.getScopeModelAncestors().length, 4, 'Scope model with id "PATIENT" has 4 ancestors');
			assert.similar(scope_model.getScopeModelAncestors(), [scope_model_2, scope_model_5, scope_model_3, scope_model_4], 'Scope model with id "PATIENT" has scope models "CENTER", "SUBSTUDY", "COUNTRY" and "STUDY" as ancestors');

			assert.equal(scope_model_4.getScopeModelChildren().length, 2, 'Scope model with id "STUDY" has 2 children');
			assert.similar(scope_model_4.getScopeModelChildren(), [scope_model_3, scope_model_5], 'Scope model with id "STUDY" has scope models "COUNTRY" and "SUBSTUDY" as children');
			assert.equal(scope_model_4.getScopeModelDescendants().length, 4, 'Scope model with id "STUDY" has 4 descendants');
			assert.similar(scope_model_4.getScopeModelDescendants(), [scope_model, scope_model_2, scope_model_3, scope_model_5], 'Scope model with id "STUDY" has scope models "PATIENT", "CENTER", "COUNTRY", "SUBSTUDY" as descendants');
		});
	});

	await bundle.describe('ScopeModel#getEventModel', async feature => {
		const study = new Study();
		const scope_model = new ScopeModel({
			id: 'PATIENT',
			shortname: {
				en: 'Patient',
				fr: 'Patient'
			}
		});
		scope_model.study = study;
		study.scopeModels.push(scope_model);
		const event_model = new EventModel({
			id: 'BASELINE',
			deadline: 0,
			deadlineUnit: 'DAYS'
		});

		//add event in scope model
		event_model.scopeModel = scope_model;
		scope_model.eventModels.push(event_model);

		await feature.it('retrieves an event model from its id', () => {
			assert.equal(scope_model.getEventModel('BASELINE'), event_model, 'Retrieve event with id "BASELINE" in scope model gives the good event');
		});
	});

	bundle.end();
}
