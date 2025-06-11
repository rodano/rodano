import {StudyLoader} from './study_loader.js';

//handle on current template and on current repository
let template;
let repository;

export const TemplateLoader = {
	HasTemplate: function() {
		return !!template;
	},
	GetTemplate: function() {
		return template;
	},
	GetRepository: function() {
		return repository;
	},
	Load: function(selected_template, selected_repository) {
		template = selected_template;
		repository = selected_repository;
	},
	Unload: function() {
		if(TemplateLoader.HasTemplate()) {
			StudyLoader.Unload();
		}
	}
};
