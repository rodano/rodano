import {UI} from './tools/ui.js';
import {StudyHandler} from './study_handler.js';
import {Configuration} from './configuration.js';

export const ConfigurationSerialized = {
	Init: function() {
		document.getElementById('study_config_select').addEventListener('click', () => ConfigurationSerialized.Select());
		document.getElementById('study_config_close').addEventListener('click', () => ConfigurationSerialized.Close());
		document.getElementById('study_config_content').addEventListener('click', () => ConfigurationSerialized.Select());
	},
	//WARNING this kills Google Chrome
	//selecting a very long text (using this piece of code or manually) slows down Chrome to the point it may crash
	//it my be due to the contextual menu displaying a part of the selected text
	Select: function() {
		if(!/Chrome/.test(navigator.userAgent)) {
			//select text
			const range = document.createRange();
			range.selectNodeContents(document.getElementById('study_config_content'));
			//range.setStart(study_config_content.firstChild, 0);
			//range.setEnd(study_config_content.firstChild, study_config_content.firstChild.length - 1);
			const selection = window.getSelection();
			selection.removeAllRanges();
			selection.addRange(range);
		}
	},
	Close: function() {
		document.getElementById('study_config_content').textContent = '';
		document.getElementById('study_config').close();
	},
	Show: function() {
		UI.StartLoading();
		UI.Delay(function() {
			const content = document.createTextNode(Configuration.Serialize(StudyHandler.GetStudy()));
			document.getElementById('study_config_content').appendChild(content);
			UI.StopLoading();
			document.getElementById('study_config').showModal();
			ConfigurationSerialized.Select();
		});
	}
};
