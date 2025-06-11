import {Formulas} from './model/config/formulas.js';

const SEARCH_DELAY = 100;

/**
 * @typedef {object} FormulaParameter
 * @property {string} name - Name of the parameter
 * @property {string} type - Type of the parameter
 * @property {boolean} repeatable - True if the parameter can be repeated (only one parameter per formula can have this flag set to true)
 */

/**
 * @typedef {object} Formula
 * @property {string} id - Id of the formula
 * @property {string} label - Id of the formula
 * @property {FormulaParameter[]} parameters - The parameters for the formula
 * @property {string} returns - The return type of the formula
 */

/**
 *
 * @param {FormulaParameter} parameter - The parameter to draw
 * @returns {HTMLSpanElement} - The result
 */
function draw_parameter(parameter) {
	const parameter_span = document.createFullElement('span');
	parameter_span.appendChild(document.createTextNode(parameter.name.toUpperCase()));
	parameter_span.appendChild(document.createTextNode(':'));
	parameter_span.appendChild(document.createFullElement('span', {class: 'type'}, parameter.type));
	return parameter_span;
}

/**
 *
 * @param {Formula} formula - The formula to draw
 * @returns {HTMLLIElement} - The result
 */
function draw_formula(formula) {
	const formula_li = document.createFullElement('li', {'data-id': formula.id});
	formula_li.appendChild(document.createFullElement('span', {}, `${formula.id}: ${formula.label}`));
	formula_li.appendChild(document.createElement('br'));

	const formula_syntax = document.createFullElement('span', {class: 'syntax'});
	formula_syntax.appendChild(document.createTextNode(formula.id));
	formula_syntax.appendChild(document.createTextNode('('));
	formula.parameters.map(draw_parameter).forEach((parameter_span, index) => {
		if(index > 0) {
			formula_syntax.appendChild(document.createTextNode(','));
		}
		formula_syntax.appendChild(parameter_span);

	});
	formula_syntax.appendChild(document.createTextNode('):'));
	formula_syntax.appendChild(document.createFullElement('span', {class: 'type'}, formula.returns));
	formula_li.appendChild(formula_syntax);
	return formula_li;
}

export const FormulasHelp = {
	Init: function() {
		function filter_formulas(search) {
			document.getElementById('formulas_help_list').children.forEach(function(formula_li) {
				let match = false;
				if(search) {
					const formula_id = formula_li.dataset.id;
					const formula = Formulas[formula_id];
					if(formula_id.nocaseIncludes(search) || formula.label.nocaseIncludes(search)) {
						match = true;
					}
				}
				else {
					match = true;
				}
				if(match) {
					formula_li.style.display = 'block';
				}
				else {
					formula_li.style.display = 'none';
				}
			});
		}

		let search_throttle;
		document.getElementById('formulas_help_search').addEventListener(
			'input',
			function(event) {
				event.stop();
				//add throttle to real search to avoid filling browser history with useless searches
				//timeout is used to detect end of the search
				if(search_throttle) {
					clearTimeout(search_throttle);
				}
				search_throttle = setTimeout(() => {
					filter_formulas(this.value);
				}, SEARCH_DELAY);
			}
		);

		document.getElementById('formulas_help_close').addEventListener('click', () => /**@type {HTMLDialogElement}*/ (document.getElementById('formulas_help')).close());

		Object.entries(Formulas)
			.map(e => Object.assign({}, {id: e[0]}, e[1]))
			.map(draw_formula)
			.forEach(Node.prototype.appendChild, document.getElementById('formulas_help_list').empty());
	},
	Open: function() {
		/**@type {HTMLDialogElement}*/ (document.getElementById('formulas_help')).showModal();
	}
};
