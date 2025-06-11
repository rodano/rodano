import './basic-tools/extension.js';

import {UI} from './tools/ui.js';
import {Config, ConfigHelpers} from './model_config.js';
import {bus} from './model/config/entities_hooks.js';
import {StudyHandler} from './study_handler.js';
import {Backups} from './backups.js';
import {Changelogs} from './changelogs.js';
import {Router} from './router.js';

let left_side;
let right_side;
let selected_side;

function select_bundle(bundle) {
	if(selected_side === 'left') {
		left_side = bundle;
	}
	else {
		right_side = bundle;
	}
}

function toggle_difference_results() {
	if(this.classList.contains('open')) {
		this.classList.remove('open');
		this.classList.add('closed');
	}
	else {
		this.classList.remove('closed');
		this.classList.add('open');
	}
}

function get_difference_message(difference) {
	const entity = difference.node.getEntity();
	const entity_label = entity.label ?? entity.name;
	const node_message = `${entity_label} ${difference.node.id}`;
	switch(difference.constructor.name) {
		case 'DifferenceProperty' :
			return `${node_message} - Property ${difference.property} (from ${difference.otherValue} to ${difference.value})`;
		case 'DifferenceArrayLength' :
			if(difference.way) {
				return `${node_message} - Property ${difference.property} - New element ${difference.element}`;
			}
			return `${node_message} - Property ${difference.property} - Missing element ${difference.element}`;
		case 'DifferenceArrayOrdering' :
			return `${node_message} - Property ${difference.property} has been mixed up`;
		case 'DifferenceArrayElement' :
			return `${node_message} - Property ${difference.property}, index ${difference.index} (from ${difference.otherElement} to ${difference.element})`;
		case 'DifferenceChild' :
			if(difference.way) {
				return `${node_message} - New child ${difference.childEntity.label} ${difference.childId}`;
			}
			return `${node_message} - Missing child ${difference.childEntity.label} ${difference.childId}`;
		default:
			return 'Unknown difference type';
	}
}

function enhance_configuration(config) {
	//insert static nodes if required
	if(!config.processed) {
		ConfigHelpers.InsertStaticNodes(config.config);
	}
	//revive config if required
	if(!config.revived) {
		//TODO find why it is required to disable bus
		bus.disable();
		bus.lock();
		const reviver = ConfigHelpers.GetReviver();
		//customize reviver to disable registration of nodes from the compared config in the bus
		delete reviver.entitiesConstructors;
		reviver.factory = function(entity) {
			const entity_builder = ConfigHelpers.GetConfigEntitiesConstructors(entity);
			return new entity_builder(function() {
				const properties = Config.Entities[this.constructor.name].getProperties();

				//declare all properties
				for(const property in properties) {
					//define real property
					Object.defineProperty(this, property, {
						configurable: true,
						writable: true,
						enumerable: properties[property].back_reference,
					});
				}
			});
		};
		config.study = reviver.revive(config.config);
		bus.unlock();
		bus.enable();
	}
	else {
		config.study = config.config;
	}
}

function compare_configurations(source, target) {
	if(source.config.configVersion !== target.config.configVersion) {
		throw new Error('You can only compare two configurations with same version number');
	}
	enhance_configuration(source);
	enhance_configuration(target);
	return target.study.compare(source.study);
}

export const Comparator = {
	Init: function() {
		const compare_source = document.getElementById('compare_source');
		const compare_target = document.getElementById('compare_target');
		const compare_error = document.getElementById('compare_error');
		const compare_choice = document.getElementById('compare_choice');

		function backup_compare_listener(event) {
			event.stop();
			const backup_date = new Date(parseInt(this.dataset.id));
			Backups.Get(backup_date).then(function(backup) {
				const bundle = {config: backup.config, name: `${backup.name} - ${backup.description} by ${backup.user}`, processed: true, revived: false};
				select_bundle(bundle);
				//update ui
				compare_choice.style.display = 'none';
				Comparator.Compare(left_side, right_side);
			});
		}

		function draw_backup(backup) {
			const backup_li = document.createFullElement('li', {}, `${backup.name} - ${backup.description} - ${backup.date.toFullDisplay()}`);
			backup_li.dataset.id = backup.date.getTime();
			backup_li.addEventListener('click', backup_compare_listener);
			return backup_li;
		}

		function dragover(event) {
			event.stop();
			this.classList.add('dragover');
		}

		function dragend(event) {
			event.stop();
			this.classList.remove('dragover');
		}

		function drop(event) {
			event.preventDefault();
			const that = this;
			selected_side = this.dataset.comparatorSide;
			compare_error.style.display = 'none';
			//only one file can be managed at a time
			if(event.dataTransfer.files.length > 1) {
				that.classList.remove('dragover');
				compare_error.textContent = 'Drop only one file';
				compare_error.style.display = 'block';
			}
			else {
				const file = event.dataTransfer.files[0];
				if(file.type !== '' && file.type !== 'application/json') {
					compare_error.textContent = 'Configuration must be a JSON file';
					compare_error.style.display = 'block';
				}
				else {
					const reader = new FileReader();
					reader.addEventListener(
						'loadstart',
						function() {
							UI.StartLoading();
						}
					);
					reader.addEventListener(
						'error',
						function() {
							compare_error.textContent = `Error while loading ${file.name}`;
							compare_error.style.display = 'block';
						}
					);
					reader.addEventListener(
						'load',
						function(reader_event) {
							try {
								const bundle = {
									name: file.name,
									config: JSON.parse(reader_event.target.result),
									processed: false,
									revived: false
								};
								that.classList.remove('dragover');
								select_bundle(bundle);
								Comparator.Compare(left_side, right_side);
							}
							catch(exception) {
								compare_error.textContent = exception.message;
								compare_error.style.display = 'block';
							}
						}
					);
					reader.addEventListener(
						'loadend',
						function() {
							UI.StopLoading();
						}
					);
					reader.readAsText(file);
				}
			}
		}

		document.addEventListener(
			'click',
			function(event) {
				if(!compare_choice.contains(event.target)) {
					compare_choice.style.display = 'none';
				}
			}
		);

		function show_comparator_choice() {
			UI.StartLoading();
			selected_side = this.dataset.comparatorSide;
			const rects = this.getClientRects();
			compare_choice.style.left = `${rects[0].left}px`;
			compare_choice.style.top = `${rects[0].bottom - 2}px`;
			Backups.GetAll().then(backups => {
				backups.map(draw_backup).forEach(Node.prototype.appendChild, document.getElementById('compare_choice_backups').empty());
				UI.StopLoading();
				compare_choice.style.display = 'block';
			});
		}

		compare_source.addEventListener('dragover', dragover);
		compare_source.addEventListener('dragend', dragend);
		compare_source.addEventListener('drop', drop);
		compare_target.addEventListener('dragover', dragover);
		compare_target.addEventListener('dragend', dragend);
		compare_target.addEventListener('drop', drop);

		compare_source.addEventListener('click', show_comparator_choice);
		compare_target.addEventListener('click', show_comparator_choice);

		const compare_dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('compare'));

		//compare dialog is managed by the application URL
		compare_dialog.addEventListener('close', () => Router.CloseTool());

		document.getElementById('compare_choice_current').addEventListener(
			'click',
			function() {
				const bundle = {config: StudyHandler.GetStudy(), name: 'Current configuration', processed: true, revived: true};
				select_bundle(bundle);
				//update ui
				compare_choice.style.display = 'none';
				Comparator.Compare(left_side, right_side);
			}
		);

		document.getElementById('compare_switch').addEventListener(
			'click',
			function() {
				if(left_side && right_side) {
					Comparator.Compare(right_side, left_side);
				}
			}
		);

		//documentation
		const compare_documentation = document.getElementById('compare_documentation');

		compare_documentation.addEventListener(
			'submit',
			function(event) {
				event.stop();
				compare_dialog.close();
				Changelogs.Open(this['text'].value);
			}
		);

		document.getElementById('compare_document').addEventListener(
			'click',
			function() {
				compare_documentation.style.display = 'block';
			}
		);

		document.getElementById('compare_documentation_cancel').addEventListener(
			'click',
			function() {
				compare_documentation.style.display = 'none';
			}
		);
	},
	Compare: function(source, target) {
		const compare_source = document.getElementById('compare_source');
		const compare_target = document.getElementById('compare_target');

		left_side = source;
		right_side = target;
		//reset ui
		const no_selection = 'Click to select or drop a file';
		compare_source.textContent = source ? source.name : no_selection;
		compare_target.textContent = target ? target.name : no_selection;

		const compare_has_difference = document.getElementById('compare_has_difference');
		compare_has_difference.style.display = 'none';

		const compare_no_difference = document.getElementById('compare_no_difference');
		compare_no_difference.style.display = 'none';

		const compare_differences = document.getElementById('compare_differences');
		compare_differences.empty();

		//compare only if there is two configurations
		if(source && target) {
			compare_source.textContent = source.name;
			compare_target.textContent = target.name;
			//find differences
			const differences = compare_configurations(source, target);
			if(differences.isEmpty()) {
				compare_no_difference.style.display = 'block';
			}
			else {
				compare_has_difference.style.display = 'block';
				//try to explain differences
				let i, length;
				//find id modifications
				const id_differences = differences.filter(function(difference) {
					return difference.constructor.name === 'DifferenceProperty' && difference.property === 'id';
				});
				//find resulting modifications
				const results_id_differences = differences.filter(function(difference) {
					if(difference.constructor.name === 'DifferenceProperty' && difference.property !== 'id') {
						for(let i = id_differences.length - 1; i >= 0; i--) {
							const id_difference = id_differences[i];
							if(difference.value === id_difference.value && difference.otherValue === id_difference.otherValue) {
								if(!id_difference.results) {
									id_difference.results = [];
								}
								id_difference.results.push(difference);
								return true;
							}
						}
					}
					if(difference.constructor.name === 'DifferenceArrayElement') {
						for(let i = id_differences.length - 1; i >= 0; i--) {
							const id_difference = id_differences[i];
							if(difference.element === id_difference.value && difference.otherElement === id_difference.otherValue) {
								if(!id_difference.results) {
									id_difference.results = [];
								}
								id_difference.results.push(difference);
								return true;
							}
						}
					}
					return false;
				});
				differences.removeElements(results_id_differences);
				//find deletion
				const deletion_differences = differences.filter(function(difference) {
					return difference.constructor.name === 'DifferenceChild' && !difference.way;
				});
				//TODO optimize this
				deletion_differences.sort(function(difference_1, difference_2) {
					const is_structural_1 = difference_1.isStructural();
					const is_structural_2 = difference_2.isStructural();
					if(is_structural_1 === is_structural_2) {
						return 0;
					}
					return is_structural_1 ? -1 : 1;
				});
				//find resulting modifications
				const results_deletion_differences = differences.filter(function(difference) {
					if(difference.constructor.name === 'DifferenceProperty' && difference.property !== 'id') {
						for(i = 0, length = deletion_differences.length; i < length; i++) {
							const deletion_difference = deletion_differences[i];
							if(difference.otherValue === deletion_difference.childId && !difference.value) {
								if(!deletion_difference.results) {
									deletion_difference.results = [];
								}
								deletion_difference.results.push(difference);
								return true;
							}
						}
					}
					if(difference.constructor.name === 'DifferenceArrayLength') {
						for(i = 0, length = deletion_differences.length; i < length; i++) {
							const deletion_difference = deletion_differences[i];
							if(difference.element === deletion_difference.childId) {
								if(!deletion_difference.results) {
									deletion_difference.results = [];
								}
								deletion_difference.results.push(difference);
								return true;
							}
						}
					}
					return false;
				});
				differences.removeElements(results_deletion_differences);

				const differences_types = {
					modification: 0,
					addition: 0,
					deletion: 0
				};
				//display differences
				differences.forEach(function(difference) {
					const difference_class = difference.getType().toLowerCase();
					differences_types[difference_class]++;
					const difference_li = document.createFullElement('li', {'class': difference_class}, get_difference_message(difference));
					if(difference.results) {
						difference_li.classList.add('closed');
						difference_li.appendChild(document.createFullElement('span', {}, `${difference.results.length} resulting modifications`));
						//insertBefore(document.createFullElement('img', {src : 'images/bullet_arrow_right.png', title : 'Open resulting differences', alt : 'Open'}), difference_li.firstChild);
						const difference_results = document.createFullElement('ul');
						difference_li.appendChild(difference_results);
						difference_li.addEventListener('click', toggle_difference_results);
						for(let i = 0, length = difference.results.length; i < length; i++) {
							difference_results.appendChild(document.createFullElement('li', {}, get_difference_message(difference.results[i])));
						}
					}
					compare_differences.appendChild(difference_li);
				});
				document.getElementById('compare_differences_modifications_number').textContent = differences_types.modification.toString();
				document.getElementById('compare_differences_additions_number').textContent = differences_types.addition.toString();
				document.getElementById('compare_differences_deletions_number').textContent = differences_types.deletion.toString();
				document.getElementById('compare_differences_number').textContent = `${(differences_types.modification + differences_types.addition + differences_types.deletion).toString()} differences`;
			}
		}
		//TODO improve this
		const dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('compare'));
		if(!dialog.open) {
			dialog.showModal();
		}
	}
};
