import {UI} from './tools/ui.js';
import {Router} from './router.js';
import {StudyTree} from './study_tree.js';

//handle on current language
let language;

export const Languages = {
	GetLanguage: function() {
		return language;
	},
	//this method must only be used when a study is loaded
	SetLanguage: function(selected_language) {
		language = selected_language;
	},
	ChangeLanguage: function(selected_language) {
		UI.StartLoading();
		language = selected_language;
		//refresh tree to update each leaf label
		StudyTree.GetTree().refresh();
		//reselect node if necessary
		if(Router.selectedNode) {
			Router.SelectNode(Router.selectedNode);
		}
		UI.StopLoading();
		UI.Notify(`Language changed to ${language}`, {tag: 'info', icon: 'images/notifications_icons/done.svg'});
	}
};
