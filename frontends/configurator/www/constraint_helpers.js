import './basic-tools/dom_extension.js';

import {RuleConstraint} from './model/config/entities/rule_constraint.js';

export const ConstraintHelpers = {
	/**
	 * @param {RuleConstraint} constraint - The constraint on which the autocomplete is based on
	 * @param {HTMLInputElement} input - The input element that will be enhanced
	 */
	EnhanceValueInput(constraint, input) {
		const autocomplete = document.createFullElement('ul', {'class': 'rule_value_autocomplete'});
		//TODO remove this
		//this is a mega hack to make the autocomplete appear at the right place
		//this code could be called before the input has been placed in the page
		//in this case, the input has no offset position
		setTimeout(() => {
			input.parentNode.appendChild(autocomplete);
			autocomplete.style.left = `${input.offsetLeft}px`;
			autocomplete.style.width = `${input.offsetWidth}px`;
		});

		//close autocomplete on a click outside
		document.addEventListener('click', function(event) {
			if(!autocomplete.contains(event.target) && !input.contains(event.target)) {
				autocomplete.style.display = 'none';
			}
		});

		let proposals = [];
		let proposal;

		function unselect_all() {
			proposal = undefined;
			autocomplete.querySelectorAll('li').forEach(i => i.classList.remove('selected'));
		}

		function manage_mouse_over() {
			unselect_all();
			proposal = proposals.find(r => r.id === this.dataset.id);
			this.classList.add('selected');
		}

		function manage_mouse_out() {
			unselect_all();
			this.classList.remove('selected');
		}

		function manage_mouse_click() {
			autocomplete.style.display = 'none';
			input.value = this.dataset.value;
		}

		function manage_keys(event) {
			//enter
			if(event.key === 'Enter' && proposal) {
				event.stop();
				input.value = proposal.value;
				input_change.call(input);
			}
			//escape
			if(event.key === 'Escape') {
				proposal = undefined;
				autocomplete.style.display = 'none';
			}
			//down or up
			if(event.key === 'ArrowUp' || event.key === 'ArrowDown') {
				//going down
				if(event.key === 'ArrowDown') {
					//initialize selection on the top node
					if(!proposal || proposal === proposals.last()) {
						proposal = proposals.first();
					}
					//normal case, select the next node
					else {
						proposal = proposals[proposals.indexOf(proposal) + 1];
					}
				}
				//going up
				else {
					//initialize selection on bottom node
					if(!proposal || proposal === proposals.first()) {
						proposal = proposals.last();
					}
					//normal case, select the previous node
					else {
						proposal = proposals[proposals.indexOf(proposal) - 1];
					}
				}
				//update autocomplete
				autocomplete.querySelectorAll('li').forEach(item => {
					if(item.dataset.id === proposal.id) {
						item.classList.add('selected');
					}
					else {
						item.classList.remove('selected');
					}
				});
			}
		}

		function draw_proposal(proposal, regexp) {
			const proposal_li = document.createFullElement('li', {'data-id': proposal.id, 'data-value': proposal.value});
			//proposal label
			const proposal_label = document.createFullElement('span', {}, proposal.label);
			proposal_li.appendChild(proposal_label);
			//separator
			proposal_li.appendChild(document.createElement('br'));
			//proposal id
			const proposal_id = document.createFullElement('span', {class: 'id'});
			proposal_id.innerHTML = proposal.id.replace(regexp, '<span class="highlight">$1</span>');
			proposal_li.appendChild(proposal_id);
			//add listeners
			proposal_li.addEventListener('mouseout', manage_mouse_out);
			proposal_li.addEventListener('mouseover', manage_mouse_over);
			proposal_li.addEventListener('click', manage_mouse_click);
			//return item
			return proposal_li;
		}

		function input_change() {
			//reset results as input content has changed
			autocomplete.empty();
			autocomplete.style.display = 'none';
			//reset selection
			proposal = undefined;
			//stop listening keyboard
			document.removeEventListener('keydown', manage_keys);

			if(this.value?.startsWith('=')) {
				const result = constraint.autocomplete(this.value);
				//prepare regexp to highlight part of node matching the search
				const regexp_value = result.last_part.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
				const regexp = new RegExp(`(${regexp_value})`, 'gi');
				proposals = result.proposals.slice(0, 10);
				if(proposals.length > 0) {
					//add header if any
					if(result.last_function) {
						const header = document.createFullElement('li', {class: 'header'});
						header.appendChild(document.createFullElement('span', {}, `Formula ${result.last_function.label}`));
						header.appendChild(document.createElement('br'));
						const help = `${result.last_function.name}(${result.last_function.parameters.map(p => p.name.toUpperCase()).join(', ')})`;
						header.appendChild(document.createFullElement('span', {class: 'help'}, help));
						autocomplete.appendChild(header);
					}
					if(result.last_condition) {
						const header = document.createFullElement('li', {class: 'header'});
						header.appendChild(document.createFullElement('span', {}, `Properties for condition ${result.last_condition.id}`));
						header.appendChild(document.createElement('br'));
						header.appendChild(document.createFullElement('span', {class: 'help'}, `Entity ${result.last_condition.entity.name}`));
						autocomplete.appendChild(header);
					}
					proposals.map(r => draw_proposal(r, regexp)).forEach(Node.prototype.appendChild, autocomplete);
					autocomplete.style.display = 'block';
					//listen keyboard in order to let user navigate through results
					document.addEventListener('keydown', manage_keys);
				}
			}
		}

		//show search results as user type
		input.addEventListener('input', input_change);
		//manage search when input content is cut or paste
		input.addEventListener('change', input_change);
		//display results when field takes focus
		input.addEventListener('focus', input_change);
	}
};
