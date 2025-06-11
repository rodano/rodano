import {Effects} from './effects.js';

window.addEventListener('load', function() {
	//sortables
	(function() {
		const sortables = document.getElementById('sortables');
		new Effects.Sortable(sortables, show_position);

		function show_position() {
			this.children.forEach(function(element) {
				const position = Math.round(element.getBoundingClientRect().top);
				if(element.firstElementChild) {
					element.firstElementChild.textContent = position;
				}
				else {
					element.appendChild(document.createFullElement('span', {style: 'float: right; font-size: 9px;'}, position.toString()));
				}
			});
		}
		show_position.call(sortables);
	})();

	//handle sortables
	(function() {
		const sortables = document.getElementById('handle_sortables');

		const elements = [
			{label: 'First', color: '#000'},
			{label: 'Second', color: '#333'},
			{label: 'Third', color: '#666'},
			{label: 'Fourth', color: '#999'}
		];

		function add_element(element) {
			const li = document.createFullElement('li', {'data-index': elements.indexOf(element), style: `background-color: ${element.color};`});
			li.appendChild(document.createFullElement('img', {src: 'arrows_up_down.png'}));
			li.appendChild(document.createFullElement('span', {}, element.label));
			sortables.appendChild(li);
		}

		elements.slice(0, 3).forEach(add_element);

		new Effects.Sortable(sortables, undefined, 'img');

		document.getElementById('handle_sortables_add').addEventListener(
			'click',
			function() {
				if(sortables.children.length < 4) {
					add_element(elements[sortables.children.length]);
				}
			}
		);

		document.getElementById('handle_sortables_remove').addEventListener(
			'click',
			function() {
				if(sortables.children.length > 2) {
					let max;
					for(let i = 0; i < sortables.children.length; i++) {
						max = (!max || max < parseInt(sortables.children[i].dataset.index)) ? i : max;
					}
					sortables.removeChild(sortables.children[max]);
				}
			}
		);
	})();

	//checked sortable
	(function() {
		const sortables = document.getElementById('checked_sortables');
		new Effects.Sortable(sortables, undefined, undefined, function(moving_element, target_element, new_position) {
			//only upper element can go in the upper position
			if(new_position === 0 && moving_element.dataset.type !== 'upper') {
				return false;
			}
			//only lower element can go in the lower position
			if(new_position === 7 && moving_element.dataset.type !== 'lower') {
				return false;
			}
			if(moving_element.dataset.type === 'up' && new_position > 3) {
				return false;
			}
			if(moving_element.dataset.type === 'low' && new_position < 4) {
				return false;
			}
			return true;
		});
	})();

	//float sortable
	(function() {
		const sortables = document.getElementById('float_sortables');
		new Effects.Sortable(sortables, undefined, 'span:first-child');
	})();

	//table sortables
	(function() {
		new Effects.Sortable(document.querySelector('#table_sortables > tbody'), undefined, 'td > img');
	})();
});
