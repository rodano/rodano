import {SVG} from '../basic-tools/svg.js';
import {Languages} from '../languages.js';
import {Config} from '../model_config.js';
import {Effects} from '../tools/effects.js';
import {EventModel} from '../model/config/entities/event_model.js';
import {UI} from '../tools/ui.js';

const sort_margin = 5;
const scheduling_height = 600;

let selected_node;

Effects.Sortable(
	document.getElementById('nodes_event_models_sort'),
	function() {
		let offset = 0;
		this.children.forEach(child => {
			selected_node.getEventModel(child.dataset.eventModelId).number = offset;
			child.querySelector('span').textContent = offset;
			offset += sort_margin;
		});
	}
);

function draw_event_model(event_model) {
	const doc_li = document.createFullElement('li', {'data-event-model-id': event_model.id});
	doc_li.appendChild(document.createFullElement('img', {src: 'images/arrows_up_down.png', alt: 'Sort event model', title: 'Sort event model'}));
	doc_li.appendChild(document.createFullElement('span', {}, event_model.number === undefined ? 'x' : event_model.number));
	doc_li.appendChild(document.createTextNode(event_model.getLocalizedLabel(Languages.GetLanguage())));
	return doc_li;
}

function sort_listener(comparator, event) {
	event.preventDefault();

	const event_models = selected_node.eventModels.slice().sort(comparator);
	event_models.forEach((e, i) => e.number = i * sort_margin);
	event_models.map(draw_event_model).forEach(Node.prototype.appendChild, document.getElementById('nodes_event_models_sort').empty());

	UI.Notify(
		'Event models sorted',
		{
			tag: 'info',
			icon: 'images/notifications_icons/done.svg',
			body: 'Event models are now sorted'
		}
	);
}

export default {
	init: function() {
		const id_comparator = (e1, e2) => e1.id.compareTo(e2.id);
		document.getElementById('nodes_event_models_sort_alphabetically_id').addEventListener('click', sort_listener.bind(undefined, id_comparator));
		const language = Languages.GetLanguage();
		const shortname_comparator = (e1, e2) => e1.getLocalizedShortname(language).compareTo(e2.getLocalizedShortname(language));
		document.getElementById('nodes_event_models_sort_alphabetically_shortname').addEventListener('click', sort_listener.bind(undefined, shortname_comparator));
	},
	open: function(node) {
		selected_node = node;

		const event_models = node.eventModels.slice();
		event_models.sort(EventModel.getExportComparator());
		event_models.map(draw_event_model).forEach(Node.prototype.appendChild, document.getElementById('nodes_event_models_sort').empty());

		//draw event scheduling
		//draw scheduling
		const event_scheduling = document.getElementById('nodes_event_models_scheduling');
		event_scheduling.empty();

		//create event highlighting listeners
		function event_mouseover() {
			this.classList.add('highlight');
			//svg.querySelectorAll(`[data-state-source-id="${this.dataset.stateId}"]`).forEach(g => g.classList.add('highlight'));
		}

		function event_mouseout() {
			this.classList.remove('highlight');
			//svg.querySelectorAll(`[data-state-source-id="${this.dataset.stateId}"]`).forEach(g => g.classList.remove('highlight'));
		}

		//show event model scheduling for the scope model
		event_models.sort((e1, e2) => e1.getDeadlineAbsolute() - e2.getDeadlineAbsolute());
		if(event_models.length > 1 && event_models.some(e => e.deadline > 0)) {
			const svg = SVG.Create({width: 'calc(100% - 2rem)', height: `${scheduling_height + 300}px`, style: 'margin: 1rem'});
			event_scheduling.appendChild(svg);

			const graph = SVG.Element('g', {transform: 'translate(55.5,15.5)'});
			svg.appendChild(graph);

			//retrieve time units from longer to short
			const units = Object.values(Config.Enums.EventTimeUnit);
			units.reverse();

			//retrieve maximum point in the scheduling
			//it is not necessarily the latest event
			//it may be a previous event with a bigger interval
			const max_time = event_models
				.map(e => e.getDeadlineAbsolute() + (e.interval ? e.getIntervalInSeconds() : 0))
				.reduce((d1, d2) => Math.max(d1, d2));
			//find scale
			const scale = scheduling_height / max_time;
			//find biggest unit
			let biggest_unit = units.find(u => max_time / u.seconds > 4);
			if(!biggest_unit) {
				biggest_unit = Config.Enums.EventTimeUnit.HOURS;
			}
			//find biggest value
			const biggest_value = Math.round(max_time / biggest_unit.seconds);

			//draw axis
			graph.appendChild(SVG.Line(0, 0, 0, scheduling_height + 30, {style: 'stroke: var(--background-highlight-color); stroke-width: 1.5px;'}));
			const path = `M-5 ${scheduling_height + 30} L5 ${scheduling_height + 30} L0 ${scheduling_height + 40} Z`;
			graph.appendChild(SVG.Element('path', {d: path, style: 'fill: var(--background-highlight-color); stroke: var(--background-highlight-color); stroke-width: 1.5px;'}));
			graph.appendChild(SVG.Text(15, scheduling_height + 40, `Time (${biggest_unit.label})`, {style: 'fill: var(--background-highlight-color);'}));

			//draw axis units
			//find stepping (with a protection provided by Math.min)
			const step = Math.max(1, Math.round(biggest_value / 20));
			for(let i = 0; i <= biggest_value; i += step) {
				const position = Math.round(i * biggest_unit.seconds * scale);
				graph.appendChild(SVG.Line(-5, position, 0, position, {style: 'stroke: var(--background-highlight-color); stroke-width: 1px;'}));
				graph.appendChild(SVG.Text(-35, position + 3, i.toString(), {style: 'font-size: 1rem; fill: var(--background-highlight-color);'}));
			}

			//draw event models
			//remember already occupied space
			//coordinates of labels indexed by deadline (for each deadline, store x and y coordinates)
			const label_coordinates = {};
			const interval_zones = [];
			event_models.forEach(event => {
				const deadline = event.getDeadlineAbsolute();
				let grading_x = 10;
				let interval_grading_min, interval_grading_max;
				//calculate x offset
				if(event.interval) {
					const interval = event.getIntervalInSeconds();
					const deadline_min = deadline - interval;
					//for some event models, min can be less than 0
					//deadline_min = Math.max(0, deadline_min);
					const deadline_max = deadline + interval;
					let zone = interval_zones.find(z => deadline_min > z.min && deadline_min < z.max || deadline_max > z.min && deadline_max < z.max);
					if(zone) {
						zone.item++;
					}
					else {
						zone = {
							min: deadline_min,
							max: deadline_max,
							item: 0
						};
						interval_zones.push(zone);
					}
					interval_grading_min = Math.round(deadline_min * scale);
					interval_grading_max = Math.round(deadline_max * scale);
					grading_x = (zone.item + 1) * 15;
				}
				//draw
				const group = SVG.Element('g');
				group.addEventListener('mouseover', event_mouseover);
				group.addEventListener('mouseout', event_mouseout);
				const position = Math.round(deadline * scale);
				group.appendChild(SVG.Line(0, position, grading_x, position, {class: 'event_grading'}));
				graph.appendChild(group);
				//initialize label offset if necessary
				if(!label_coordinates[deadline]) {
					//space may be already taken by the text that overflows from previous deadline
					//find maximum among the positions of text from previous deadline and the calculated position
					const y_coordinate = Object.values(label_coordinates).map(o => o.y + 15).reduce((y1, y2) => Math.max(y1, y2), position);
					label_coordinates[deadline] = {x: 20, y: y_coordinate};
				}
				const label_coordinate = label_coordinates[deadline];
				//draw label
				const event_link = SVG.Link(`#node=${event.getGlobalId()}`);
				const event_label = event.getLocalizedLongname(Languages.GetLanguage()) || event.getLocalizedShortname(Languages.GetLanguage());
				const event_text = SVG.Text(grading_x + label_coordinate.x, label_coordinate.y + 5, event_label, {class: 'event_label'});
				event_link.appendChild(event_text);
				group.appendChild(event_link);
				//if text overflows, put it on the next line
				label_coordinate.x += event_text.getBBox().width;
				if(label_coordinate.x >= svg.parentElement.offsetWidth - 20) {
					label_coordinate.x = 20;
					label_coordinate.y += 15;
					event_text.setAttribute('x', label_coordinate.x);
					event_text.setAttribute('y', label_coordinate.y);
					label_coordinate.x += event_text.getBBox().width + 15;
				}
				else {
					label_coordinate.x += 15;
				}
				//draw interval
				if(event.interval) {
					group.appendChild(SVG.Line(grading_x, interval_grading_min, grading_x, interval_grading_max, {class: 'event_grading'}));
					group.appendChild(SVG.Line(grading_x - 3, interval_grading_min, grading_x + 3, interval_grading_min, {class: 'event_grading'}));
					group.appendChild(SVG.Line(grading_x - 3, interval_grading_max, grading_x + 3, interval_grading_max, {class: 'event_grading'}));
				}
			});
		}
	}
};
