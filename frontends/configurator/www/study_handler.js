//this is like a singleton service that keeps a reference to the current loaded study
//the study may have been loaded from a file or from REST API
let study;

export const StudyHandler = {
	HasStudy: function() {
		return !!study;
	},
	GetStudy: function() {
		return study;
	},
	SetStudy: function(selected_study) {
		study = selected_study;
	},
	RemoveStudy: function() {
		study = undefined;
	}
};
