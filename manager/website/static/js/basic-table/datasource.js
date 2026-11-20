/**
 * Checks whether a value is a string
 * @param {object} object - The value to test
 * @returns {boolean} True if the value is a string
 */
function is_string(object) {
	return typeof (object) === 'string';
}

/**
 * Checks whether a value is a finite number
 * @param {object} object - The value to test
 * @returns {boolean} True if the value is a finite number
 */
function is_number(object) {
	return !isNaN(parseFloat(object)) && isFinite(object);
}

/**
 * Checks whether a value is a date instance
 * @param {object} object - The value to test
 * @returns {boolean} True if the value is a date
 */
function is_date(object) {
	return Object.prototype.toString.call(object) === '[object Date]';
}

/**
 * Checks whether a value is a boolean.
 * @param {object} object - The value to test
 * @returns {boolean} True if the value is a boolean
 */
function is_boolean(object) {
	return typeof object === 'boolean';
}

/**
 * Compares two values for sorting purposes, supports strings, numbers, dates, and booleans
 * @param {object} object_1 - The first value
 * @param {object} object_2 - The second value
 * @returns {number} Negative if object_1 < object_2, positive if object_1 > object_2, or 0 if equal
 */
function compare(object_1, object_2) {
	if(is_string(object_1) && is_string(object_2)) {
		return object_1.localeCompare(object_2);
	}
	if(is_number(object_1) && is_number(object_2)) {
		return object_1 - object_2;
	}
	if(is_date(object_1) && is_date(object_2)) {
		return object_1.getTime() - object_2.getTime();
	}
	if(is_boolean(object_1) && is_boolean(object_2)) {
		if(object_1 === object_2) {
			return 0;
		}
		return object_1 ? -1 : 1;
	}
	return 0;
}

/**
 * Datasource for the table component
 * Supports both preloaded arrays and remote JSON data fetched from a URL
 */
export class Datasource {
	/**
	 * Creates a new datasource
	 * @param {object} parameters - The configuration object
	 * @param {string} [parameters.url] - URL to fetch JSON data from (async mode)
	 * @param {object[]} [parameters.data] - Preloaded array of records (sync mode)
	 */
	constructor(parameters) {
		//asynchronous mode
		this.url = undefined;
		//synchronous mode
		this.data = [];

		//events
		this.ready = false;

		//bind parameters
		for(const parameter in parameters) {
			this[parameter] = parameters[parameter];
		}

		//check required parameters
		if(!this.url && !this.data) {
			throw new Error('One of following parameters is required : url or data');
		}

		//internal variables
		this.length;
		this.filteredData;
		this.sortingOrders = [];
	}

	/**
	 * Initialises the datasource
	 * For URL-based sources, fetches and parses the remote JSON
	 * For preloaded sources, resolves immediately
	 * @returns {Promise<void>} Resolves when data is ready
	 */
	init() {
		return new Promise((resolve, reject) => {
			//URL datasources
			if(this.url) {
				fetch(this.url).then(response => {
					if(response.status === 200) {
						response.json().then(data => {
							this.data = data;
							this.length = this.data.length;
							resolve();
						});
					}
					else {
						reject(`Unable to retrieve data: ${response.status} ' ' ${response.statusText}`);
					}
				});
			}
			//preloaded datasource
			else {
				this.length = this.data.length;
				resolve();
			}
		});
	}

	/**
	 * Returns the total number of records, respecting any active filter
	 * @returns {number} Record count
	 */
	getLength() {
		return this.filteredData ? this.filteredData.length : this.length;
	}

	/**
	 * Sorts an array of records in place according to the current sorting orders
	 * @param {object[]} data - The array of records to sort
	 */
	sort(data) {
		if(this.sortingOrders.length > 0) {
			data.sort((a, b) => {
				let index = 0;
				let field;
				let result;
				while(!result && index < this.sortingOrders.length) {
					field = this.sortingOrders[index].field;
					const a_data = a[field];
					const b_data = b[field];
					if(!a_data && !b_data) {
						result = 0;
					}
					else {
						if(!a_data) {
							result = -1;
						}
						else if(!b_data) {
							result = 1;
						}
						else {
							result = compare(a_data, b_data);
						}
					}
					index++;
				}
				return this.sortingOrders[index - 1].descendant ? -result : result;
			});
		}
	}

	/**
	 * Returns a page of (optionally filtered and sorted) records
	 * @param {number} start - Zero-based start index
	 * @param {number} [limit] - Maximum number of records to return. If omitted, all remaining records are returned
	 * @returns {object[]} Array of records for the requested page
	 */
	getData(start, limit) {
		//filtered data
		if(this.filteredData) {
			this.sort(this.filteredData);
			return this.filteredData.slice(start, limit ? start + limit : undefined);
		}
		else {
			this.sort(this.data);
			return this.data.slice(start, limit ? start + limit : undefined);
		}
	}

	/**
	 * Applies a predicate to the datasource, storing matching records internally
	 * @param {(object) => boolean} filter - Predicate called for each record
	 */
	filter(filter) {
		this.filteredData = this.data.filter(filter);
	}

	/**
	 * Clears the active filter, restoring the full dataset
	 */
	unfilter() {
		this.filteredData = undefined;
	}
}
