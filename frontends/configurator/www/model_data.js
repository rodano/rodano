import {bus_ui} from './bus_ui.js';
import {StudyHandler} from './study_handler.js';
import {create_data} from './model/data/entities_data.js';

let model;

//initialize data if possible
if(StudyHandler.HasStudy()) {
	model = create_data(StudyHandler.GetStudy());
}

bus_ui.register({
	onLoadStudy: function(event) {
		model = create_data(event.study);
	}
});

export const ModelData = {
	GetModel: function() {
		return model;
	}
};
