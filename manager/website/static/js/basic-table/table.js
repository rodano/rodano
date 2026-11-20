import {Datasource} from './datasource.js';
import {styles} from './styles.js';

/**
 * Checks whether a value is a string
 * @param {object} object - The value to test
 * @returns {boolean} True if the value is a string
 */
function is_string(object) {
	return typeof (object) === 'string';
}

/**
 * Creates and returns an HTML element with optional attributes, text content, and event listeners
 * @param {string} tag - The HTML tag name
 * @param {{[key: string]: string}} [attributes] - Dictionary of attributes to set
 * @param {string} [text] - Text content to append inside the element
 * @param {{[key: string]: (event: Event) => void}} [listeners] - Dictionary of event type to listener function
 * @returns {HTMLElement} The newly created element
 */
function create_element(tag, attributes, text, listeners) {
	const element = document.createElement(tag);
	if(attributes) {
		for(const attribute in attributes) {
			if(attributes.hasOwnProperty(attribute)) {
				element.setAttribute(attribute, attributes[attribute]);
			}
		}
	}
	if(text !== undefined) {
		element.appendChild(document.createTextNode(text));
	}
	if(listeners) {
		for(const listener in listeners) {
			if(listeners.hasOwnProperty(listener)) {
				element.addEventListener(listener, listeners[listener], false);
			}
		}
	}
	return element;
}

/**
 * Creates and returns an SVG element in the SVG namespace with optional attributes.
 * @param {string} tag - The SVG tag name
 * @param {{[key: string]: string}} [attributes] - Dictionary of SVG attributes to set
 * @returns {SVGElement} The newly created SVG element
 */
function create_svg_element(tag, attributes) {
	const element = document.createElementNS('http://www.w3.org/2000/svg', tag);
	if(attributes) {
		for(const attribute in attributes) {
			if(attributes.hasOwnProperty(attribute)) {
				element.setAttribute(attribute, attributes[attribute]);
			}
		}
	}
	return element;
}

/**
 * Removes all child nodes from an element
 * @param {HTMLElement} element - The element to clear
 */
function clear_element(element) {
	while(element.firstChild) {
		element.removeChild(element.firstChild);
	}
}

/**
 * Resort the table and updates the sort-indicator icons
 * @param {Table} table - The table instance to resort
 */
function resort(table) {
	table.start = 0;
	table.draw();
	const sorting_order = table.datasource.sortingOrders[0];
	//update sort image
	for(let i = 0; i < table.columns.length; i++) {
		const column = table.columns[i];
		//only columns with data are sortable
		if(column.data && !column.unsortable) {
			const header_column = table.head.firstElementChild.children[i];
			const header_status = /**@type {HTMLElement}*/(header_column.lastElementChild);
			header_status.style.visibility = 'hidden';
			if(column.data === sorting_order.field) {
				header_status.style.visibility = 'visible';
				header_status.style.transform = sorting_order.descendant ? 'rotate(0.5turn)' : '';
			}
		}
	}
}

/**
 * Applies a record-level predicate filter to the table's datasource and redraws the table
 * @param {Table} table - The table instance to filter
 * @param {(object: object) => boolean} filter - Predicate called for each data record
 */
function data_filter(table, filter) {
	table.datasource.filter(filter);
	table.start = 0;
	table.draw();
}

/**
 * Enum of icon types used for sort and pagination SVG icons
 * @enum {number}
 */
const IconType = {
	ARROW: 0,
	FIRST: 1,
	PREVIOUS: 2,
	NEXT: 3,
	LAST: 4
};

/**
 * Creates an SVG icon of the given type
 * @param {number} type - One of the {@link IconType} constants
 * @param {string} [width] - CSS width value, defaults to 100%
 * @param {string} [height] - CSS height value, defaults to 100%
 * @returns {SVGElement} The SVG icon element
 */
function create_icon(type, width, height) {
	const svg = create_svg_element('svg', {viewBox: '0 0 10 10', width: width ?? '100%', height: height ?? '100%'});
	switch(type) {
		case IconType.ARROW: {
			svg.appendChild(create_svg_element('polygon', {points: '5 2,10 8,0 8'}));
			break;
		}
		case IconType.FIRST: {
			svg.appendChild(create_svg_element('rect', {x: '0', y: '0', width: '2', height: '10'}));
			svg.appendChild(create_svg_element('polygon', {points: '2 5,10 0,10 10'}));
			break;
		}
		case IconType.PREVIOUS: {
			svg.appendChild(create_svg_element('polygon', {points: '1 5,9 0,9 10'}));
			break;
		}
		case IconType.NEXT: {
			svg.appendChild(create_svg_element('polygon', {points: '9 5,1 10,1 0'}));
			break;
		}
		case IconType.LAST: {
			svg.appendChild(create_svg_element('rect', {x: '8', y: '0', width: '2', height: '10'}));
			svg.appendChild(create_svg_element('polygon', {points: '8 5,0 10,0 0'}));
			break;
		}
	}
	return svg;
}

/**
 * A web-component-based data table supporting sorting, filtering, pagination, and custom rendering
 */
export class Table {
	/**
	 * Creates and mounts the table into the given container element
	 * @param {object} parameters - Configuration object
	 * @param {HTMLElement!} parameters.container - The host element that will contain the table
	 * @param {object[]!} parameters.columns - Column definitions
	 * @param {string} [parameters.id] - Unique identifier used to persist state, defaults to the id of the container
	 * @param {{label: string | HTMLElement, url?: string}[]} [parameters.actions] - Footer action link/button definitions
	 * @param {string} [parameters.statusText] - Pagination status template
	 * @param {number} [parameters.rowPerPage] - Number of rows per page, set 0 to disable pagination
	 * @param {(object: object, index: number) => object} [parameters.rowCSS] - Callback that returns an inline style object for each row
	 * @param {boolean} [parameters.enableSearch] - Whether to show the text-filter search input
	 * @param {boolean} [parameters.allowMissingData] - When set to true, missing column data fields do not throw an error
	 * @param {(table: Table) => void} [parameters.afterRender] - Callback invoked once data is loaded and the table is first drawn
	 * @throws {Error} If configuration parameters are missing
	 */
	constructor(parameters) {
		//required parameters
		/**@type {HTMLElement}*/
		this.container = undefined;
		this.columns = [];
		this.datasource;
		//optional parameters with default value
		this.id;
		this.actions = [];
		/*eslint-disable-next-line no-template-curly-in-string*/
		this.statusText = 'Display items ${start} - ${stop} of ${total}';
		this.rowPerPage = 10;
		this.rowCSS = undefined;
		this.enableSearch = true;
		this.allowMissingData = false;
		//events
		this.afterRender = undefined;

		//bind parameters
		for(const parameter in parameters) {
			this[parameter] = parameters[parameter];
		}

		//internal variables
		this.start = 0;

		//try to identify table
		if(!this.id) {
			this.id = this.container.id;
		}

		//check required parameters
		if(!this.container || !this.columns) {
			throw new Error('Following parameters are required: container and columns');
		}

		//check columns
		for(let i = 0; i < this.columns.length; i++) {
			const column = this.columns[i];
			if((!column.data && !column.render) || !column.label) {
				throw new Error(`Column ${i} is incomplete (must have data or render, and label)`);
			}
			if(!column.unsortable && (!column.data || !column.type)) {
				throw new Error(`Column ${column.label} must have data or type to be sortable or be set as unsortable`);
			}
		}

		const that = this;

		//shadow
		this.shadow = this.container.attachShadow({mode: 'open'});

		//add css
		const style = create_element('style', {}, styles);
		this.shadow.appendChild(style);

		//table
		this.table = create_element('table');

		//table header
		this.head = document.createElement('thead');
		const header_line = create_element('tr');
		this.head.appendChild(header_line);

		this.columns.forEach(column => {
			const header_column = create_element('th');
			if(column.width) {
				header_column.setAttribute('style', `width: ${column.width}px;`);
			}
			//create or use label
			const header_label = is_string(column.label) ? document.createTextNode(column.label) : column.label;
			header_column.appendChild(header_label);
			const header_status = create_icon(IconType.ARROW, '10px', '10px');
			header_status.style.visibility = 'hidden';
			header_column.appendChild(header_status);
			if(!column.unsortable) {
				header_column.style.cursor = 'pointer';
				header_column.addEventListener(
					'click',
					(function(field) {
						return function() {
							const previous_sort_order = that.datasource.sortingOrders[0];
							that.addOrdering(field, !previous_sort_order.descendant);
						};
					})(column.data)
				);
			}
			header_line.appendChild(header_column);
		});
		this.table.appendChild(this.head);

		//table body
		this.body = create_element('tbody');
		this.table.appendChild(this.body);

		//footer
		this.footer = create_element('footer');

		const search_bar = create_element('div', {class: 'search'});
		this.footer.appendChild(search_bar);

		if(this.enableSearch) {
			const search_form = create_element('form');
			const search_label = create_element('label', {}, 'Filter');
			this.search_input = /**@type {HTMLInputElement}*/(create_element('input', {type: 'search'}));
			//scan search input
			let last_filter = '';
			setInterval(() => {
				if(last_filter !== this.search_input.value) {
					last_filter = this.search_input.value;
					this.filter(last_filter);
				}
			}, 100);
			search_label.appendChild(this.search_input);
			search_form.appendChild(search_label);
			search_bar.appendChild(search_form);
		}

		this.buttons = create_element('div', {class: 'buttons'});
		this.footer.appendChild(this.buttons);

		this.setActions(this.actions);

		if(this.rowPerPage) {
			//controls
			this.controls = create_element('div', {class: 'controls'});
			this.footer.appendChild(this.controls);

			//first
			this.firstButton = create_element('button', {title: 'First', alt: 'First'}, undefined,
				{
					click: function() {
						if(that.start !== 0) {
							that.start = 0;
							that.draw();
						}
					}
				}
			);
			this.firstButton.appendChild(create_icon(IconType.FIRST));
			this.controls.appendChild(this.firstButton);
			//previous
			this.previousButton = create_element('button', {title: 'Previous', alt: 'Previous'}, undefined,
				{
					click: function() {
						if(that.start > 1) {
							that.start -= that.rowPerPage;
							that.draw();
						}
					}
				}
			);
			this.previousButton.appendChild(create_icon(IconType.PREVIOUS));
			this.controls.appendChild(this.previousButton);
			//next
			this.nextButton = create_element('button', {title: 'Next', alt: 'Next'}, undefined,
				{
					click: function() {
						if(that.start + that.rowPerPage < that.datasource.getLength()) {
							that.start += that.rowPerPage;
							that.draw();
						}
					}
				}
			);
			this.nextButton.appendChild(create_icon(IconType.NEXT));
			this.controls.appendChild(this.nextButton);
			//last
			this.lastButton = create_element('button', {title: 'Last', alt: 'Last'}, undefined,
				{
					click: function() {
						const last_start = (Math.ceil(that.datasource.length / that.rowPerPage) - 1) * that.rowPerPage;
						if(that.start !== last_start) {
							that.start = last_start;
							that.draw();
						}
					}
				}
			);
			this.lastButton.appendChild(create_icon(IconType.LAST));
			this.controls.appendChild(this.lastButton);

			//status
			this.status = create_element('div', {class: 'status'});
			this.footer.appendChild(this.status);
		}
		//insertion
		clear_element(this.container);
		this.shadow.appendChild(this.table);
		this.shadow.appendChild(this.footer);
		//display footer only if there is something in it
		this.footer.style.display = this.enableSearch || this.rowPerPage || this.actions.length > 0 ? 'flex' : 'none';
	}

	/**
	 * Replaces the footer action buttons with the provided action definitions
	 * @param {{label: string | HTMLElement, url?: string}[]} actions - Action definitions
	 */
	setActions(actions) {
		this.actions = actions;
		clear_element(this.buttons);
		this.actions.forEach(action => {
			let action_item;
			if(is_string(action.label)) {
				action_item = create_element('a', {href: action.url, class: 'button'}, action.label);
			}
			else {
				action_item = action.label;
			}
			this.buttons.appendChild(action_item);
		});
		//display footer only if there is something in it
		this.footer.style.display = this.enableSearch || this.rowPerPage || this.actions.length > 0 ? 'flex' : 'none';
	}

	/**
	 * Sets a single sort order, replacing any existing ordering, then redraws the table
	 * @param {string} field - The data field name to sort by
	 * @param {boolean} descendant - True for descending order, false for ascending
	 */
	setOrdering(field, descendant) {
		this.datasource.sortingOrders = [{field: field, descendant: descendant}];
		resort(this);
	}

	/**
	 * Prepends a sort order for the given field (up to 3 levels are retained) and redraws the table
	 * If the field already has an ordering, it is removed before being re-added with the new direction
	 * @param {string} field - The data field name to sort by
	 * @param {boolean} descendant - True for descending order, false for ascending
	 */
	addOrdering(field, descendant) {
		//remove any ordering if it already exists
		this.datasource.sortingOrders = this.datasource.sortingOrders.filter(o => o.field !== field);
		//add new sort order
		this.datasource.sortingOrders.unshift({field: field, descendant: descendant});
		//no need to sort on more than three columns
		if(this.datasource.sortingOrders.length > 3) {
			this.datasource.sortingOrders.pop();
		}
		resort(this);
	}

	/**
	 * Filters table rows by matching a search string against column data
	 * @param {string} filter - The text to search for
	 * @param {object} [filter_column] - A column definition object that, if provided, limits the search to that column's data field
	 * @param {boolean} [exact_matching] - When true, requires an exact match instead of a substring match
	 */
	filter(filter, filter_column, exact_matching) {
		const lower_filter = filter.toLowerCase();
		data_filter.call(undefined, this, record => {
			for(let i = 0; i < this.columns.length; i++) {
				const column = this.columns[i];
				//filter only on one column if asked
				if(!filter_column || (filter_column && column.data === filter_column.data)) {
					const value = record[column.data];
					if(typeof value === 'string') {
						if((exact_matching && value === filter) || value.toLowerCase().includes(lower_filter)) {
							return true;
						}
					}
					else if(typeof value === 'object') {
						//this does not work anymore
						if((exact_matching && value.innerHTML === filter) || value.innerHTML.toLowerCase().includes(lower_filter)) {
							return true;
						}
					}
				}
			}
			return false;
		});
	}

	/**
	 * Filters table rows using a custom predicate function
	 * @param {(record: object) => boolean} filter - Predicate called for each record
	 */
	filterFunction(filter) {
		data_filter.call(undefined, this, function(record) {
			return filter(record);
		});
	}

	/**
	 * Removes the active filter and redraws the table showing all records
	 */
	unfilter() {
		this.datasource.unfilter();
		this.start = 0;
		this.draw();
	}

	/**
	 * Binds a datasource to the table
	 * @param {Datasource} datasource - The datasource to render
	 * @throws {Error} If datasource is missing or is not a datasource instance
	 */
	render(datasource) {
		//keep a handle on datasource
		this.datasource = datasource;
		//datasource is required and must be a table datasource
		if(!this.datasource || this.datasource.constructor !== Datasource) {
			throw new Error('A datasource is required to render the table');
		}
		//restore state
		const serialized_state = sessionStorage.getItem(this.id);
		if(serialized_state) {
			try {
				const state = JSON.parse(serialized_state);

				this.start = state.start;
				this.datasource.sortingOrders = state.datasource.sortingOrders;
				//remove now invalid columns from restored sorting columns
				this.datasource.sortingOrders = this.datasource.sortingOrders.filter(sorting_order => {
					const column = this.columns.find(c => c.data === sorting_order.field);
					return column && !column.unsortable;
				});

				/*if(this.enableSearch && state.search) {
					this.search_input.value = state.search;
				}*/
			}
			catch(exception) {
				//unable to restore state
				console.error(`Unable to restore state for table ${this.id}: ${exception.message}`);
			}
		}

		//set arbitrary sorting order if needed
		if(this.datasource.sortingOrders.length === 0) {
			//find first sortable column
			const column = this.columns.find(c => !c.unsortable);
			//if there is a sortable column, add it in sorting orders
			if(column) {
				this.datasource.sortingOrders.push({field: column.data, descendant: false});
			}
		}

		//initialize datasource and render retrieved data if any
		datasource.init().then(() => {
			//check start offset
			if(this.start > this.datasource.getLength()) {
				this.start = 0;
			}

			//update column ui
			if(this.datasource.sortingOrders.length > 0) {
				const column_index = this.columns.findIndex(c => c.data === this.datasource.sortingOrders[0].field);
				const header_column = this.head.children[0].children[column_index];
				const header_status = header_column.lastElementChild;
				//@ts-ignore
				header_status.style.visibility = 'visible';
				//@ts-ignore
				header_status.style.transform = this.datasource.sortingOrders[0].descendant ? 'rotate(0.5turn)' : '';
			}

			//data may already be available
			if(datasource.data) {
				//check data
				if(!this.allowMissingData) {
					for(let i = 0; i < this.columns.length; i++) {
						const column = this.columns[i];
						if(column.data) {
							for(let j = 0; j < datasource.data.length; j++) {
								if(!datasource.data[j].hasOwnProperty(column.data)) {
									throw new Error(`Column ${i} uses data ${column.data} but this data does not exist in record ${j}`);
								}
							}
						}
					}
				}

				//do search if needed
				if(this.enableSearch && this.search_input.value) {
					this.filter(this.search_input.value);
				}
			}

			//call callback
			if(this.afterRender) {
				this.afterRender.call(this);
			}

			try {
				this.draw();
			}
			catch(exception) {
				throw new Error(`Unable to draw table: ${exception}`, {cause: exception});
			}
		});
	}

	/**
	 * Resolves the display value for a single cell, applying type coercion and any custom render function
	 * @param {object} record - The data record for the current row
	 * @param {object} column - The column definition
	 * @param {number} column_index - Zero-based column index
	 * @returns {string | number | boolean | HTMLElement | undefined} The resolved cell value
	 * @throws {Error} If the column's render function throws or returns undefined
	 */
	getCellValue(record, column, column_index) {
		let value = undefined;
		if(column.data) {
			value = record[column.data];
		}
		//revive date
		if(column.type === Table.DataType.DATE && value) {
			value = new Date(value);
		}
		//render
		if(column.render) {
			try {
				value = column.render(value, record, this);
			}
			catch(exception) {
				throw new Error(`Unable to use render function for column ${column_index} with data ${record}: ${exception}`, {cause: exception});
			}
			if(value === undefined) {
				throw new Error(`Render function for column ${column_index} does not produce a valid result with data ${record}`);
			}
		}
		return value;
	}

	/**
	 * Clears the table body, and redraws it with the current page of (sorted/filtered) data
	 */
	draw() {
		//save state
		try {
			const state = {
				datasource: {
					sortingOrders: this.datasource.sortingOrders
				},
				start: this.start
			};
			if(this.enableSearch) {
				state.search = this.search_input.value;
			}
			sessionStorage.setItem(this.id, JSON.stringify(state));
		}
		catch(exception) {
			//unable to save state
			console.error(`Unable to save state: ${exception.message}`);
		}
		//empty table
		while(this.body.firstChild) {
			this.body.removeChild(this.body.firstChild);
		}
		//manage pagination
		if(this.rowPerPage) {
			//check range
			if(this.start < 0) {
				this.start = 0;
			}
			//manage first and previous button
			if(this.start > 1) {
				this.previousButton.removeAttribute('disabled');
				this.firstButton.removeAttribute('disabled');
			}
			else {
				this.previousButton.setAttribute('disabled', 'disabled');
				this.firstButton.setAttribute('disabled', 'disabled');
			}
			//manage next and last button
			if(this.start + this.rowPerPage < this.datasource.getLength()) {
				this.nextButton.removeAttribute('disabled');
				this.lastButton.removeAttribute('disabled');
			}
			else {
				this.nextButton.setAttribute('disabled', 'disabled');
				this.lastButton.setAttribute('disabled', 'disabled');
			}
		}
		//retrieve data to display
		const data = this.datasource.getData(this.start, this.rowPerPage);
		//no data
		if(data.length === 0) {
			const no_data = create_element('tr', {class: 'even'});
			no_data.appendChild(create_element('td', {colspan: this.columns.length.toString()}, 'No data to display'));
			this.body.appendChild(no_data);
		}
		else {
			//insert in table
			for(let i = 0; i < data.length; i++) {
				const record = data[i];
				const row = create_element('tr');
				if(this.rowCSS) {
					const rules = this.rowCSS.call(undefined, record, i);
					if(rules) {
						for(const [property, value] of Object.entries(rules)) {
							row.style[property] = value;
						}
					}
				}
				else {
					row.classList.add(i % 2 === 0 ? 'even' : 'odd');
				}
				for(let j = 0; j < this.columns.length; j++) {
					const column = this.columns[j];
					const value = this.getCellValue(record, column, j);
					const element = create_element('td');
					//string are just appended
					if(typeof value === 'string') {
						//value must not be falsy
						if(value) {
							element.appendChild(document.createTextNode(value));
						}
					}
					//number are aligned to the right
					else if(typeof value === 'number') {
						element.setAttribute('style', 'text-align: right;');
						element.appendChild(document.createTextNode(value.toString()));
					}
					//boolean are converted to string
					else if(typeof value === 'boolean') {
						element.appendChild(document.createTextNode(value.toString()));
					}
					//render function may have returned a HTML element
					else {
						//value must not be falsy
						if(value) {
							element.appendChild(value);
						}
					}
					row.appendChild(element);
				}
				this.body.appendChild(row);
			}
		}
		//display status
		if(this.rowPerPage) {
			//calculate stop index
			const stop = Math.min(this.start + this.rowPerPage, this.datasource.getLength());
			//adjust start index so it's human readable, except when there is not entry
			const start = data.length === 0 ? 0 : this.start + 1;
			/*eslint-disable-next-line no-template-curly-in-string*/
			const status = this.statusText.replace('${start}', start.toString()).replace('${stop}', stop.toString()).replace('${total}', this.datasource.getLength().toString());
			this.status.textContent = status;
		}
	}
}

Table.DataType = {
	STRING: 'String',
	DATE: 'Date',
	NUMBER: 'Number'
};
