import {Migrator} from '../model/config/migrator.js';
import {StudyLoader} from '../study_loader.js';
import {ConfigHelpers} from '../model_config.js';
import {bus} from '../model/config/entities_hooks.js';
import {Wizards} from '../wizards.js';
import {TemplateRepositories} from '../template_repositories.js';

(function() {

	let template;

	Wizards.Register('study', {
		title: 'New study',
		description: 'This wizard will help you to create a study.',
		steps: 4,
		mode: Wizards.Mode.FULLSCREEN,
		last_action_label: 'Let\'s go',
		onStart: function(parameters) {
			template = parameters.template;
		},
		onEnd: function() {
			//load study template
			TemplateRepositories.GetRepository(template.repository).open()
				.then(repository => repository.get('study', template.id))
				.then(data => {
					const config = data.nodes.Study;
					ConfigHelpers.InsertStaticNodes(config);
					Migrator.Migrate(config);
					bus.reset();
					const study = ConfigHelpers.Revive(config);
					//customize study name
					study.id = document.getElementById('wizard_study_id').value;
					study.shortname[study.defaultLanguageId] = document.getElementById('wizard_study_name').value;
					//customize scope models
					const country = study.getScopeModel('COUNTRY');
					const center = study.getScopeModel('CENTER');
					const patient = study.getScopeModel('PATIENT');
					if(!document.getElementById('scope_model_country').checked) {
						country.delete();
						center.parentIds.push('STUDY');
						center.defaultParentId = 'STUDY';
					}
					if(!document.getElementById('scope_model_center').checked) {
						center.delete();
						patient.parentIds.push('STUDY');
						patient.defaultParentId = 'STUDY';
					}
					//customize profiles
					document.querySelectorAll('#wizard_study_profiles input').forEach(function(input) {
						if(!input.checked) {
							study.getProfile(input.dataset.profileId).delete();
						}
					});
					//update ui
					StudyLoader.Load(study);
				});
		},
		onCancel: function() {
			/**@type {HTMLDialogElement}*/ (document.getElementById('welcome')).showModal();
		},
		onValidate: function(step) {
			if(step === 1) {
				if(!document.getElementById('wizard_study_id').value || !document.getElementById('wizard_study_name').value) {
					document.getElementById('wizard_error').textContent = 'Fill all fields';
					document.getElementById('wizard_error').style.display = 'block';
					return false;
				}
			}
			return true;
		}
	});
})();
