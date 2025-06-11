import {Effects} from '../tools/effects.js';
import {Languages} from '../languages.js';
import {DatasetModel} from '../model/config/entities/dataset_model.js';
import {UI} from '../tools/ui.js';

const sort_margin = 5;

let selected_node;

Effects.Sortable(
	document.getElementById('nodes_dataset_models_sort'),
	function() {
		let offset = 0;
		this.children.forEach(child => {
			selected_node.getDatasetModel(child.dataset.datasetModelId).exportOrder = offset;
			child.querySelector('span').textContent = offset;
			offset += sort_margin;
		});
	}
);

function draw_dataset_model(doc) {
	const doc_li = document.createFullElement('li', {'data-dataset-model-id': doc.id});
	doc_li.appendChild(document.createFullElement('img', {src: 'images/arrows_up_down.png', alt: 'Sort dataset model', title: 'Sort dataset model'}));
	doc_li.appendChild(document.createFullElement('span', {}, doc.exportOrder === undefined ? 'x' : doc.exportOrder));
	doc_li.appendChild(document.createTextNode(doc.getLocalizedLabel(Languages.GetLanguage())));
	return doc_li;
}

function sort_listener(comparator, event) {
	event.preventDefault();

	const dataset_models = selected_node.datasetModels.slice().sort(comparator);
	dataset_models.forEach((d, i) => d.exportOrder = i * sort_margin);
	dataset_models.map(draw_dataset_model).forEach(Node.prototype.appendChild, document.getElementById('nodes_dataset_models_sort').empty());

	UI.Notify(
		'Dataset models sorted',
		{
			tag: 'info',
			icon: 'images/notifications_icons/done.svg',
			body: 'Dataset models are now sorted'
		}
	);
}

export default {
	init: function() {
		const id_comparator = (d1, d2) => d1.id.compareTo(d2.id);
		document.getElementById('nodes_dataset_models_sort_alphabetically_id').addEventListener('click', sort_listener.bind(undefined, id_comparator));
		const language = Languages.GetLanguage();
		const shortname_comparator = (d1, d2) => d1.getLocalizedShortname(language).compareTo(d2.getLocalizedShortname(language));
		document.getElementById('nodes_dataset_models_sort_alphabetically_shortname').addEventListener('click', sort_listener.bind(undefined, shortname_comparator));
	},
	open: function(node) {
		selected_node = node;

		const dataset_models = node.datasetModels.slice();
		dataset_models.sort(DatasetModel.getExportComparator());
		dataset_models.map(draw_dataset_model).forEach(Node.prototype.appendChild, document.getElementById('nodes_dataset_models_sort').empty());
	}
};
