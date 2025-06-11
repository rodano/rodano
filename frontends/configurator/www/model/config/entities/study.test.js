import {Language} from './language.js';
import {ScopeModel} from './scope_model.js';
import {Study} from './study.js';
import {EventModel} from './event_model.js';

export default async function test(bundle, assert) {
	bundle.begin();

	await bundle.describe('Study#getSelectedLanguages', async feature => {
		const study = new Study();
		study.id = 'TEST';

		const language_1 = new Language({
			id: 'en',
			shortname: {
				en: 'English',
				fr: 'Anglais',
				de: 'Englisch'
			}
		});
		language_1.study = study;
		study.languages.push(language_1);

		const language_2 = new Language({
			id: 'fr',
			shortname: {
				en: 'French',
				fr: 'Français',
				de: 'Französisch'
			}
		});
		language_2.study = study;
		study.languages.push(language_2);

		const language_3 = new Language({
			id: 'de',
			shortname: {
				en: 'German',
				fr: 'Allemand',
				de: 'Deutsch'
			}
		});
		language_3.study = study;
		study.languages.push(language_3);

		assert.equal(study.languages.length, 3, 'There is 3 languages in study after adding 3 languages');
		assert.equal(study.languages[2].id, 'de', 'Third language of study is language with id "de"');

		study.languageIds = ['en', 'fr'];

		await feature.it('retrieves the selected languages', () => {
			assert.equal(study.getSelectedLanguages().length, 2, 'There is 2 selected languages in study');
			assert.equal(study.getSelectedLanguages()[0], language_1, 'First selected language of study is language "en"');
			assert.equal(study.getSelectedLanguages()[1], language_2, 'Second selected language of study is language "fr"');
		});
	});

	await bundle.describe('Study#getScopeModel', async feature => {
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

		await feature.it('retrieves a scope model from its id', () => {
			assert.equal(study.getScopeModel('PATIENT').id, 'PATIENT', 'Retrieve scope model with id "PATIENT" in study gives a scope model with id "PATIENT"');
			assert.equal(study.getScopeModel('PATIENT'), scope_model, 'Scope model with id "PATIENT" is equals to the one retrieved from study with id "PATIENT"');
			assert.equal(study.getScopeModel('PATIENT'), study.scopeModels[0], 'Scope model with id "PATIENT" is the first one in the list of scope models from study');
		});
	});

	await bundle.describe('Study#getRootScopeModel, Study#getLeafScopeModels and Study#getLeafScopeModel', async feature => {
		await feature.it('retrieves root and leaf scope models properly', () => {
			const study = new Study();
			const scope_model = new ScopeModel({
				id: 'PATIENT',
				shortname: {en: 'Patient'}
			});

			//add scope model in study
			scope_model.study = study;
			study.scopeModels.push(scope_model);

			assert.equal(study.getRootScopeModel(), scope_model, 'Scope model with id "PATIENT" is the root scope model');
			assert.equal(study.getLeafScopeModels().length, 1, 'There is only 1 leaf scope model');
			assert.equal(study.getLeafScopeModels()[0], scope_model, 'Scope model with id "PATIENT" is the only one leaf scope model');
			assert.equal(study.getLeafScopeModel(), scope_model, 'Scope model with id "PATIENT" is the leaf scope model');

			const scope_model_2 = new ScopeModel({
				id: 'CENTER',
				shortname: {en: 'Center'}
			});
			scope_model_2.study = study;
			study.scopeModels.push(scope_model_2);

			//scope model relations
			scope_model.parentIds = ['CENTER'];
			scope_model.defaultParentId = 'CENTER';

			assert.equal(study.getRootScopeModel(), scope_model_2, 'Scope model with id "CENTER" is the root scope model');
			assert.equal(study.getLeafScopeModels().length, 1, 'There is only 1 leaf scope model');
			assert.equal(study.getLeafScopeModels()[0], scope_model, 'Scope model with id "PATIENT" is the only one leaf scope model');
			assert.equal(study.getLeafScopeModel(), scope_model, 'Scope model with id "PATIENT" is the leaf scope model');

			//multiple leaf scope models
			const scope_model_3 = new ScopeModel({
				id: 'COUNTRY',
				shortname: {en: 'Country'}
			});
			scope_model_3.study = study;
			study.scopeModels.push(scope_model_3);

			scope_model_2.parentIds = ['COUNTRY'];
			scope_model_2.defaultParentId = 'COUNTRY';

			const scope_model_4 = new ScopeModel({
				id: 'DEVICE',
				shortname: {en: 'Device'}
			});
			scope_model_4.study = study;
			study.scopeModels.push(scope_model_4);

			scope_model_4.parentIds = ['COUNTRY'];
			scope_model_4.defaultParentId = 'COUNTRY';

			assert.equal(study.getRootScopeModel(), scope_model_3, 'Scope model with id "COUNTRY" is the root scope model');
			assert.equal(study.getLeafScopeModels().length, 2, 'There are 2 leaf scope models');
			assert.similar(study.getLeafScopeModels(), [scope_model, scope_model_4], 'Scope models with id "PATIENT" and "DEVICE" are both leaf scope models');
			assert.equal(study.getLeafScopeModel(), scope_model, 'Scope model with id "PATIENT" is the leaf scope model because it has the deepest path');
		});
	});

	await bundle.describe('Study#getEventModels', async feature => {
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
		event_model.scopeModel = scope_model;
		scope_model.eventModels.push(event_model);

		await feature.it('retrieves all event models', () => {
			assert.equal(study.getEventModels().length, 1, 'There is 1 event model in study');
			assert.equal(study.getEventModels()[0], event_model);
		});
	});

	bundle.end();
}
