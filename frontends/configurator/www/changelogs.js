import {Changelog} from './model/config/entities/changelog.js';
import {APITools} from './api_tools.js';
import {StudyHandler} from './study_handler.js';
import {Router} from './router.js';

let current_callback;

function create_changelog(changelog) {
	const changelog_li = document.createFullElement('li');
	changelog_li.appendChild(document.createFullElement('p', {}, `Edited by ${changelog.user} ${new Date(changelog.date).toFullDisplay()}`));
	changelog_li.appendChild(document.createFullElement('p', {}, changelog.message));
	return changelog_li;
}

export const Changelogs = {
	Init: function() {
		const changelog_dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('changelog'));

		//changelog dialog is managed by the application URL
		changelog_dialog.addEventListener('close', () => Router.CloseTool());

		changelog_dialog.querySelector('form').addEventListener(
			'submit',
			function(event) {
				event.stop();
				const changelog = new Changelog({
					user: APITools.GetUser() ? APITools.GetUser().login : 'NA',
					date: new Date().getTime(),
					message: this['text'].value
				});
				StudyHandler.GetStudy().configChangelogs.push(changelog);
				/**@type {HTMLDialogElement}*/ (document.getElementById('changelog')).close();
				//call callback if set
				if(current_callback) {
					current_callback.call();
					current_callback = undefined;
				}
			}
		);
	},
	Open: function(text, callback) {
		//show previous documentation
		StudyHandler.GetStudy().configChangelogs.map(create_changelog).forEach(Node.prototype.appendChild, document.getElementById('changelog_previous').empty());

		//store callback
		current_callback = callback;

		const dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('changelog'));

		//prepare form
		const changelog = dialog.querySelector('form');
		changelog['text'].value = text || '';

		dialog.showModal();
	}
};
