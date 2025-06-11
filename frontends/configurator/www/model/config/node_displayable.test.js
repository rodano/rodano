import {ScopeModel} from './entities/scope_model.js';
import {Study} from './entities/study.js';

export default async function test(bundle, assert) {
	bundle.begin();

	await bundle.describe('DisplayableNode#getLocalizedShortname, DisplayableNode#getLocalizedLongname and DisplayableNode#getLocalizedLabel', async feature => {
		await feature.it('works properly', () => {
			//study
			const study = new Study();
			study.id = 'TEST';

			assert.equal(study.getLocalizedShortname('fr'), '', 'Study localized shortname in "fr" is "" because no shortname has been set yet');
			assert.equal(study.getLocalizedLongname('fr'), '', 'Study localized longname in "fr" is "" because no shortname has been set yet');
			assert.equal(study.getLocalizedLabel('fr'), 'TEST', 'Study localized label in "fr" is "TEST" because only id has been set yet');

			study.shortname = {
				en: 'Test study',
				fr: 'Etude de test'
			};

			assert.equal(study.shortname.fr, 'Etude de test', 'Study shortname in "fr" is "Etude de test"');
			assert.equal(study.getLocalizedShortname('fr'), 'Etude de test', 'Study localized shortname in "fr" is "Etude de test"');
			assert.equal(study.getLocalizedLongname('fr'), '', 'Study localized label in "fr" is "TEST" because only id has been set yet');
			assert.equal(study.getLocalizedLabel('fr'), 'Etude de test', 'Study localized label in "fr" is "Etude de test"');

			assert.equal(study.getLocalizedShortname('de'), '', 'Study localized shortname in "de" is "" because shortname has not been defined for this language yet');
			assert.equal(study.getLocalizedLabel('de'), 'TEST', 'Study localized label in "de" is "TEST" because only id has been set for this language yet');

			assert.equal(study.getLocalizedShortname(['de','fr']), 'Etude de test', 'Study localized shortname for languages ["de","fr"] is "Etude de test" as shortname has not been defined in "fr" but not in "de"');
			assert.equal(study.getLocalizedLabel(['de','fr']), 'Etude de test', 'Study localized label for languages ["de","fr"] is "Etude de test" as shortname has not been defined in "fr" but not in "de"');

			study.longname = {
				en: 'Test study for test purpose',
				fr: 'Etude de test à des fins de test'
			};

			assert.equal(study.getLocalizedLongname('fr'), 'Etude de test à des fins de test', 'Study localized longname in "fr" is "Etude de test à des fins de test"');

			//scope model
			const scope_model = new ScopeModel({
				id: 'PATIENT',
				shortname: {
					en: 'Patient',
					fr: 'Patient'
				}
			});
			assert.equal(scope_model.shortname.fr, 'Patient', 'Scope model shortname in "fr" is "Patient"');
			assert.equal(scope_model.getLocalizedShortname('en'), 'Patient', 'Scope model localized shortname in "en" is "Patient"');
		});
	});

	bundle.end();
}
