import {Languages} from './languages.js';
import {bus_ui, BusEventLoadStudy, BusEventUnloadStudy} from './bus_ui.js';
import {Router} from './router.js';
import {StudyHandler} from './study_handler.js';

export const StudyLoader = {
	Load: function(study) {
		StudyHandler.SetStudy(study);

		//manage selected language
		if(study.defaultLanguageId) {
			Languages.SetLanguage(study.defaultLanguageId);
		}

		//update ui
		document.title = `Rodano - ${study.id}`;

		//close modal
		/**@type {HTMLDialogElement}*/ (document.getElementById('welcome')).close();
		/**@type {HTMLDialogElement}*/ (document.getElementById('authentication')).close();

		//restore config settings from local storage
		const settings_key = `rodano.configurator.configs.${study.id}`;
		const settings = localStorage.getItem(settings_key) ? localStorage.getObject(settings_key) : undefined;

		//send event on bus
		bus_ui.dispatch(new BusEventLoadStudy(study, settings));

		//try to restore selected node
		const event = new UIEvent('hashchange', {bubbles: true, cancelable: true, detail: 1});
		window.dispatchEvent(event);
	},
	Unload: function() {
		if(StudyHandler.HasStudy()) {
			const study = StudyHandler.GetStudy();

			//reset ui
			Router.Reset();

			//manage study specific settings
			const settings = {};
			//send event on bus
			bus_ui.dispatch(new BusEventUnloadStudy(study, settings));
			//store config settings in local storage
			localStorage.setObject(`rodano.configurator.configs.${study.id}`, settings);

			StudyHandler.RemoveStudy();
		}
	}
};
