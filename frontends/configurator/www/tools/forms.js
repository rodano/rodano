export const Forms = {
	Autocomplete: function(input, list, items_provider, item_drawer, selection_callback, highlight_callback, update_callback) {
		let selection; //currently selected item
		let candidates; //current candidate items

		function destroy_list() {
			list.empty();
			list.style.display = 'none';
			//stop listening keyboard
			document.removeEventListener('keydown', manage_keys);
		}

		//close results on a click outside
		document.addEventListener('click', function(event) {
			if(!list.contains(event.target) && !input.contains(event.target)) {
				destroy_list();
			}
		});

		function unselect_all() {
			selection = undefined;
			list.querySelectorAll('li').forEach(function(item) {
				item.classList.remove('selected');
			});
		}

		function manage_selection() {
			destroy_list();
			//replace selection with selected item in the list
			if(selection) {
				input.value = item_drawer(selection).dataset.value;
				if(selection_callback) {
					selection_callback.call(undefined, selection, input.value);
				}
				selection = undefined;
				return true;
			}
			return false;
		}

		function manage_mouse_over() {
			unselect_all();
			selection = this.item;
			this.classList.add('selected');
		}

		function manage_mouse_out() {
			unselect_all();
			this.classList.remove('selected');
		}

		function manage_keys(event) {
			//enter
			if(event.key === 'Enter') {
				if(manage_selection()) {
					//disable form submission only if a selection has been made through the autocomplete
					event.preventDefault();
				}
				return;
			}
			//escape
			if(event.key === 'Escape') {
				selection = undefined;
				list.style.display = 'none';
				return;
			}
			//down or up
			if(event.key === 'ArrowDown' || event.key === 'ArrowUp') {
				//going down
				if(event.key === 'ArrowDown') {
					//initialize selection on the top candidate
					if(!selection || selection === candidates.last()) {
						selection = candidates[0];
					}
					//normal case, select the next result_nodes
					else {
						selection = candidates[candidates.indexOf(selection) + 1];
					}
				}
				//going up
				else {
					//initialize selection on bottom result_nodes
					if(!selection || selection === candidates.first()) {
						selection = candidates.last();
					}
					//normal case, select the previous result_nodes
					else {
						selection = candidates[candidates.indexOf(selection) - 1];
					}
				}
				//update results list
				list.querySelectorAll('li').forEach(function(item) {
					if(item.item === selection) {
						item.classList.add('selected');
					}
					else {
						item.classList.remove('selected');
					}
				});
				if(highlight_callback) {
					highlight_callback.call(undefined, selection);
				}
			}
		}

		function manage_update() {
			const value = this.value;

			if(update_callback) {
				update_callback.call(undefined, value);
			}

			//reset candidates and selection
			candidates = [];
			selection = undefined;

			//destroy existing list
			destroy_list();

			if(value) {
				//ask for matching items and draw them
				candidates = items_provider(value);
				if(candidates.length > 0) {
					candidates.map(function(candidate) {
						const item_ui = item_drawer(candidate, value);
						//enhance element
						item_ui.item = candidate;
						item_ui.addEventListener('mouseout', manage_mouse_out);
						item_ui.addEventListener('mouseover', manage_mouse_over);
						item_ui.addEventListener('click', manage_selection);
						//add tab index to make the item ui focusable
						//this is important when used in a "tagger" because "tagger" must be able to detect if the input focus is lost to the autocomplete list
						//this is possible only if autocomplete elements are focusable
						item_ui.setAttribute('tabindex', 0);
						return item_ui;
					}).forEach(Node.prototype.appendChild, list);

					list.style.display = 'block';
					//listen keyboard in order to let user navigate trough results
					document.addEventListener('keydown', manage_keys);
				}
			}
		}

		//add listeners to show search results as user type
		input.addEventListener('input', manage_update);
		//add listener on change to manage search when input content is cut or paste
		//input.addEventListener('change', manage_update);
	},
	Tagger: function(input, list, selected_list, selected_input, items_provider, item_drawer, selection_callback, highlight_callback, update_callback) {

		function remove_item_listener() {
			remove_item(this.parentNode.dataset.value);
			input.focus();
		}

		function remove_item(item) {
			const items = JSON.parse(selected_input.value);
			items.removeElement(item);
			selected_input.value = JSON.stringify(items);
			refresh_selection();
		}

		function add_item(item) {
			const items = JSON.parse(selected_input.value);
			if(!items.includes(item)) {
				items.push(item);
				selected_input.value = JSON.stringify(items);
				refresh_selection();
			}
		}

		function retrieve_items() {
			return selected_input.value ? JSON.parse(selected_input.value) : [];
		}

		function create_item(item) {
			//create item in selected list
			const item_button = document.createFullElement('li', {'data-value': item, style: 'margin-right: 4px;'});
			item_button.appendChild(document.createFullElement('span', {}, item));
			const item_delete_button = document.createFullElement('span', {'style': 'margin-left: 0.5rem; cursor: pointer;'}, '×');
			item_delete_button.addEventListener('click', remove_item_listener);
			item_button.appendChild(item_delete_button);
			selected_list.appendChild(item_button);
			//manage input and hidden field
			input.value = '';
			return item_button;
		}

		function refresh_selection() {
			const items = retrieve_items();
			//synchronize items in model and display
			items.forEach(function(item, index) {
				//create only missing items
				if(selected_list.childNodes.length <= index || selected_list.childNodes[index].dataset.value !== item) {
					selected_list.insertBefore(create_item(item), selected_list.childNodes[index]);
				}
			});
			//remove items that no longer exist
			selected_list.childNodes.slice(items.length).forEach(e => selected_list.removeChild(e));
			//add padding on input used to enter data
			input.style.paddingLeft = `${selected_list.offsetWidth + 2}px`;
		}

		function enhanced_selection_callback(selection, value) {
			if(selection) {
				add_item(item_drawer(selection).dataset.value);
			}
			if(selection_callback) {
				selection_callback.call(undefined, selection, value);
			}
		}

		function add_value(value) {
			//add item only if it is not empty and it does not already exist
			if(value && !retrieve_items().includes(value)) {
				add_item(value);
			}
			//manage input and hidden field
			input.value = '';
		}

		function blur_listener(event) {
			//do not react to blur if the user put the focus in the autocomplete list
			//in this case, let the user use the autocomplete list to add a new tag
			//the value must be added as a tag only if the user leaves the input field for elsewhere
			if(!list.contains(event.relatedTarget)) {
				add_value(this.value);
			}
		}

		function enhanced_update_callback(value) {
			if(value.endsWith(' ')) {
				add_value(value.trim());
			}
			if(update_callback) {
				update_callback.call(undefined, value);
			}
		}

		function enhanced_items_provider(text) {
			const items = retrieve_items();
			const result_items = items_provider(text);
			//remove items that already exist
			return result_items.filter(i => !items.includes(i));
		}

		//update field according to current selection
		refresh_selection();

		//listen hidden field to update fake selected items list
		selected_input.addEventListener('change', refresh_selection);

		//add current value when input loose focus
		input.addEventListener('blur', blur_listener);

		Forms.Autocomplete(input, list, enhanced_items_provider, item_drawer, enhanced_selection_callback, highlight_callback, enhanced_update_callback);
	}
};
