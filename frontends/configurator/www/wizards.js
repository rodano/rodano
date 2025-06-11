import {Effects} from './tools/effects.js';
import {Loader} from './basic-tools/loader.js';
import {bus_ui} from './bus_ui.js';

let selected_wizard;
let selected_wizard_parameters;
let selected_wizard_step_width;
let selected_step = 1;
let progressing = false;

function get_selected_wizard_step(step) {
	return document.querySelector(`#wizard_${Object.key(wizards, selected_wizard)} [data-step="${step}"]`);
}

function update_wizard_progress() {
	//progress
	document.querySelectorAll('#wizard_progress li').forEach((element, i) => {
		const index = i + 1;
		element.className = '';
		if(index < selected_step) {
			element.classList.add('done');
		}
		else if(index > selected_step) {
			element.classList.add('todo');
		}
		if(selected_step >= selected_wizard.no_return) {
			element.classList.add('blocked');
		}
	});
	//start button
	document.getElementById('wizard_start').classList.add('hidden');
	//cancel button
	const wizard_cancel = document.getElementById('wizard_cancel');
	selected_step !== selected_wizard.steps ? wizard_cancel.classList.remove('hidden') : wizard_cancel.classList.add('hidden');
	//previous button
	const wizard_previous = document.getElementById('wizard_previous');
	selected_step > 1 && !selected_wizard.no_return || selected_step < selected_wizard.no_return ? wizard_previous.classList.remove('hidden') : wizard_previous.classList.add('hidden');
	//next button
	const wizard_next = document.getElementById('wizard_next');
	selected_step <= selected_wizard.steps ? wizard_next.classList.remove('hidden') : wizard_next.classList.add('hidden');
	wizard_next.textContent = selected_wizard.labels[selected_step.toString()] || 'Next';

	//find scroll position
	let offset_left = 0;
	for(let i = 1; i < selected_step; i++) {
		offset_left += get_selected_wizard_step(i).offsetWidth;
	}
	document.getElementById('wizard_content').scrollLeft = offset_left;
	get_selected_wizard_step(selected_step).style.display = '';
	progressing = false;
}

function open_wizard() {
	const wizard_dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('wizard'));
	wizard_dialog.classList.remove('aside', 'fullscreen');
	wizard_dialog.style.display = '';
	if(selected_wizard.mode === Wizards.Mode.FULLSCREEN) {
		wizard_dialog.classList.add('fullscreen');
		wizard_dialog.show();
	}
	else {
		document.getElementById('content').classList.add('reduced');
		wizard_dialog.classList.add('aside');
		wizard_dialog.style.display = 'flex';
	}
}

function close_wizard() {
	//hide wizard
	const wizard_dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('wizard'));
	if(selected_wizard.mode === Wizards.Mode.FULLSCREEN) {
		wizard_dialog.close();
	}
	else {
		wizard_dialog.style.display = 'none';
		document.getElementById('content').classList.remove('reduced');
	}
	selected_wizard = undefined;
}

function wizard_progress_listener() {
	if(this.classList.contains('done') && !progressing) {
		selected_step = Array.prototype.indexOf.call(this.parentNode.children, this) + 1;
		//scroll to selected step
		progressing = true;
		Effects.Scroll(document.getElementById('wizard_content'), (selected_step - 1) * selected_wizard_step_width, 300).then(update_wizard_progress);
	}
}

function start_wizard() {
	//hide error
	document.getElementById('wizard_error').style.display = 'none';
	//hide description
	document.getElementById('wizard_description').style.display = 'none';
	//show wizard content
	document.getElementById('wizard_content').style.display = 'block';
	//manage actions
	document.getElementById('wizard_start').classList.add('hidden');
	document.getElementById('wizard_cancel').classList.remove('hidden');
	document.getElementById('wizard_previous').classList.remove('hidden');
	document.getElementById('wizard_next').classList.remove('hidden');
	//update progress bar
	const wizard_progress = document.getElementById('wizard_progress');
	wizard_progress.empty();
	for(let i = 1; i <= selected_wizard.steps; i++) {
		const wizard_progress_step = document.createFullElement('li', {}, i.toString(), {click: wizard_progress_listener});
		if(i > 1) {
			wizard_progress_step.classList.add('todo');
		}
		wizard_progress.appendChild(wizard_progress_step);
	}
	wizard_progress.style.display = 'block';
	//manage wizard initialization
	selected_wizard.onStart?.(selected_wizard_parameters);
	selected_step = 1;
	update_wizard_progress();
	//show wizard
	open_wizard();
	//retrieve step width
	selected_wizard_step_width = get_selected_wizard_step(selected_step).offsetWidth;
	document.getElementById('wizard_content').scrollLeft = 0;
}

const wizards = {};
const wizard_loader = new Loader(document, {url: 'wizards/', nocache: false});

export const Wizards = {
	Init: function() {
		document.getElementById('wizard_start').addEventListener('click', start_wizard);
		document.getElementById('wizard_cancel').addEventListener(
			'click',
			function() {
				selected_wizard.onCancel?.();
				close_wizard();
			}
		);
		document.getElementById('wizard_next').addEventListener(
			'click',
			function() {
				if(!progressing) {
					document.getElementById('wizard_error').style.display = 'none';
					if(selected_wizard.onValidate) {
						if(!selected_wizard.onValidate(selected_step)) {
							return;
						}
					}
					if(selected_step === selected_wizard.steps) {
						selected_wizard.onEnd?.();
						close_wizard();
					}
					else {
						selected_wizard.onNext?.(selected_step);
						selected_step++;
						//scroll to next step
						const wizard_content = document.getElementById('wizard_content');
						progressing = true;
						Effects.Scroll(wizard_content, wizard_content.scrollLeft + selected_wizard_step_width, 300).then(update_wizard_progress);
					}
				}
			}
		);
		document.getElementById('wizard_previous').addEventListener(
			'click',
			function() {
				if(!progressing) {
					selected_step--;
					//scroll to previous step
					const wizard_content = document.getElementById('wizard_content');
					progressing = true;
					Effects.Scroll(wizard_content, wizard_content.scrollLeft - selected_wizard_step_width, 300).then(update_wizard_progress);
				}
			}
		);
		bus_ui.register({
			onSave: function(event) {
				selected_wizard?.onSave?.call(undefined, event.node);
			}
		});
	},
	Register: function(wizard_id, wizard) {
		//keep a hook on wizard
		wizards[wizard_id] = wizard;
		//initialize wizard if needed
		wizard.init?.();
		//re-open it
		Wizards.Open(wizard_id, selected_wizard_parameters);
	},
	Open: function(wizard_id, wizard_parameters) {
		//select wizard
		selected_wizard = wizards[wizard_id];
		selected_wizard_parameters = wizard_parameters;
		const wizard_content = document.getElementById('wizard_content');
		//load wizard if required
		if(!wizards.hasOwnProperty(wizard_id)) {
			wizard_loader
				.loadHTML(`${wizard_id}.html`, wizard_content)
				.then(() => wizard_loader.loadCSS(`${wizard_id}.css`))
				.then(() => wizard_loader.loadModule(`${wizard_id}.js`));
			//when the wizard code will be loaded, it will register it-self and will re-open
			return;
		}
		//show wizard content and hide other wizards
		wizard_content.children.forEach(function(child) {
			child.style.display = child.id === (`wizard_${wizard_id}`) ? 'block' : 'none';
		});
		//update title
		document.getElementById('wizard_title').textContent = selected_wizard.title;
		//hide all actions
		const wizard_start = document.getElementById('wizard_start');
		wizard_start.classList.add('hidden');
		document.getElementById('wizard_previous').classList.add('hidden');
		document.getElementById('wizard_next').classList.add('hidden');
		//hide error
		document.getElementById('wizard_error').style.display = 'none';
		//hide wizard progress
		document.getElementById('wizard_progress').style.display = 'none';
		//hide wizard content
		document.getElementById('wizard_content').style.display = 'none';
		//update and show wizard description
		const wizard_description = document.getElementById('wizard_description');
		wizard_description.textContent = selected_wizard.description;
		wizard_description.style.display = 'block';
		//show start action
		wizard_start.classList.remove('hidden');
		//show wizard
		open_wizard();
	}
};

Wizards.Mode = {
	ASIDE: 'Aside',
	FULLSCREEN: 'Fullscreen'
};
