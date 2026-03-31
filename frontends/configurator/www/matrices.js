import './basic-tools/extension.js';

import {CSV} from './basic-tools/csv.js';
import {Config} from './model_config.js';
import {Languages} from './languages.js';
import {NodeTools} from './node_tools.js';
import {StudyHandler} from './study_handler.js';
import {Entities} from './model/config/entities.js';
import {Profile} from './model/config/entities/profile.js';
import {Assignables, FamilyAssignableParents, RightAssignables} from './model/config/entities_categories.js';

const AssignableProfileToggle = {
	selected: {
		image: 'images/bullet_green.png',
		label: 'Remove right on all assignables for profile ${profile}'
	},
	unselected: {
		image: 'images/bullet_red.png',
		label: 'Give right on all assignables for profile ${profile}'
	}
};

const AssignableAssignableToggle = {
	selected: {
		image: 'images/bullet_green.png',
		label: 'Remove right on all profiles for assignable ${assignable}'
	},
	unselected: {
		image: 'images/bullet_red.png',
		label: 'Give right on all profiles for assignable ${assignable}'
	}
};

const AssignableToggle = {
	selected: {
		image: 'images/tick.png',
		label: 'Remove right on ${assignable} for profile ${profile}'
	},
	unselected: {
		image: 'images/untick.png',
		label: 'Give right on ${assignable} for profile ${profile}'
	}
};

const RightAssignableToggle = {
	selected: {
		image: 'images/bullet_green.png',
		label: 'Remove right ${right} on ${assignable} for profile ${profile}'
	},
	unselected: {
		image: 'images/bullet_red.png',
		label: 'Give right ${right} on ${assignable} for profile ${profile}'
	}
};

const FamilyAssignableToggle = {
	parent_selected: {
		image: 'images/tick.png',
		label: 'Remove right on ${parentAssignableId} for profile ${profileId}'
	},
	parent_unselected: {
		image: 'images/untick.png',
		label: 'Give right on ${parentAssignableId} for profile ${profileId}'
	},
	child_selected: {
		image: 'images/tick.png',
		label: 'Remove right on ${childAssignableId} of ${parentAssignableId} for profile ${profileId}'
	},
	child_unselected: {
		image: 'images/untick.png',
		label: 'Give right on ${childAssignableId} of ${parentAssignableId} for profile ${profileId}'
	}
};

function get_sorted_profiles(study) {
	const profiles = study.profiles.slice();
	profiles.sort(Profile.getComparator());
	return profiles;
}

function highlight_cell() {
	const index = this.parentNode.childNodes.indexOf(this);
	document.querySelectorAll('#matrix tbody tr').forEach(function(line) {
		line.childNodes[index].classList.add('highlight');
	});
	document.querySelector('#matrix thead tr').childNodes[index].classList.add('highlight');
}

function unhighlight_cell() {
	const index = this.parentNode.childNodes.indexOf(this);
	document.querySelectorAll('#matrix tbody tr').forEach(function(line) {
		line.childNodes[index].classList.remove('highlight');
	});
	document.querySelector('#matrix thead tr').childNodes[index].classList.remove('highlight');
}

//export
function generate_export_matrix_header(study, entity) {
	const headers = [];
	headers.push(entity.plural_label);
	headers.pushAll(get_sorted_profiles(study).map(p => p.getLocalizedShortname(Languages.GetLanguage())));
	return headers;
}

function generate_export_assignable_matrix(study, assignable_entity) {
	const data = [];
	//add header
	data.push(generate_export_matrix_header(study, assignable_entity));
	//add data
	const profiles = get_sorted_profiles(study);
	const assignables = study.getAssignables(assignable_entity).slice();
	assignables.sort(Config.Entities[assignable_entity.name].getComparator(Languages.GetLanguage()));
	assignables.forEach(function(assignable) {
		const line = [];
		line.push(assignable.getLocalizedShortname(Languages.GetLanguage()));
		line.pushAll(profiles.map(p => p.isAssigned(assignable_entity, assignable.id) ? 'X' : ''));
		data.push(line);
	});
	return data;
}

function generate_export_right_assignable_matrix(study, assignable_entity) {
	const data = [];
	//add header
	data.push(generate_export_matrix_header(study, assignable_entity));
	//add data
	const profiles = get_sorted_profiles(study);
	const rights = Object.keys(Config.Enums.ProfileRightType);
	const assignables = study.getRightAssignables(assignable_entity).slice();
	assignables.sort(Config.Entities[assignable_entity.name].getComparator(Languages.GetLanguage()));
	assignables.forEach(function(assignable) {
		const line = [];
		line.push(assignable.getLocalizedShortname(Languages.GetLanguage()));
		for(let j = 0; j < profiles.length; j++) {
			const profile = profiles[j];
			let cell = '';
			for(let k = 0; k < rights.length; k++) {
				if(profile.isAssignedRightNode(assignable, rights[k])) {
					cell += rights[k].substring(0, 1);
				}
			}
			line.push(cell);
		}
		data.push(line);
	});
	return data;
}

function generate_export_family_assignable_matrix(study, assignable_entity) {
	const data = [];
	//add header
	data.push(generate_export_matrix_header(study, assignable_entity));
	//add data
	const profiles = get_sorted_profiles(study);
	//TODO find real descendants
	//const assignables = study.getDescendants(assignable_entity);
	const assignables = study.workflows.slice();
	assignables.sort(Config.Entities[assignable_entity.name].getComparator(Languages.GetLanguage()));
	assignables.forEach(parent_assignable => {
		//add line for parent assignable
		const line = [];
		line.push(parent_assignable.getLocalizedShortname(Languages.GetLanguage()));
		line.pushAll(profiles.map(p => p.grantedWorkflowIds.hasOwnProperty(parent_assignable.id) ? 'X' : ''));
		data.push(line);
		//add lines for child assignable
		parent_assignable.actions.forEach(function(child_assignable) {
			const line = [];
			line.push(child_assignable.getLocalizedShortname(Languages.GetLanguage()));
			line.pushAll(profiles.map(p => p.grantedWorkflowIds[parent_assignable.id]?.includes(child_assignable.id) ? 'X' : ''));
			data.push(line);
		});
	});
	return data;
}

export const Matrices = {
	DrawFamilyAssignableMatrix: function(study, assignable_entity) {
		function toggle_parent_assignable() {
			const profile = study.getProfile(this.dataset.profileId);
			if(profile.grantedWorkflowIds.hasOwnProperty(this.dataset.parentAssignableId)) {
				delete profile.grantedWorkflowIds[this.dataset.parentAssignableId];
				const label = FamilyAssignableToggle.parent_unselected.label.replaceObject(this.dataset);
				this.setAttributes({src: FamilyAssignableToggle.parent_unselected.image, alt: label, title: label});
				//disable child assignable toggles
				const parent_assignable = study.getWorkflow(this.dataset.parentAssignableId);
				parent_assignable.actions.forEach(child_assignable => {
					const assignable_toggles = document.querySelectorAll(`[data-parent-assignable-id="${parent_assignable.id}"][data-child-assignable-id="${child_assignable.id}"][data-profile-id="${profile.id}"]`);
					const label = FamilyAssignableToggle.child_unselected.label.replaceObject(assignable_toggles[0].dataset);
					assignable_toggles[0].setAttributes({src: FamilyAssignableToggle.child_unselected.image, alt: label, title: label});
					assignable_toggles.forEach(a => a.setAttribute('disabled', 'disabled'));
				});
			}
			else {
				profile.grantedWorkflowIds[this.dataset.parentAssignableId] = [];
				const label = FamilyAssignableToggle.parent_selected.label.replaceObject(this.dataset);
				this.setAttributes({src: FamilyAssignableToggle.parent_selected.image, alt: label, title: label});
				//enable child assignable toggles
				const parent_assignable = study.getWorkflow(this.dataset.parentAssignableId);
				parent_assignable.actions.forEach(child_assignable => {
					const assignable_toggles = document.querySelectorAll(`[data-parent-assignable-id="${parent_assignable.id}"][data-child-assignable-id="${child_assignable.id}"][data-profile-id="${profile.id}"]`);
					assignable_toggles.forEach(a => a.removeAttribute('disabled'));
				});
			}
		}

		function toggle_child_assignable() {
			//toggle may be disable if no right has been given on parent assignable
			if(this.hasAttribute('disabled')) {
				return;
			}

			const profile = study.getProfile(this.dataset.profileId);
			const child_rights = profile.grantedWorkflowIds[this.dataset.parentAssignableId];
			if(child_rights.includes(this.dataset.childAssignableId)) {
				child_rights.removeElement(this.dataset.childAssignableId);
				const label = FamilyAssignableToggle.child_unselected.label.replaceObject(this.dataset);
				this.setAttributes({src: FamilyAssignableToggle.child_unselected.image, alt: label, title: label});
			}
			else {
				child_rights.push(this.dataset.childAssignableId);
				const label = FamilyAssignableToggle.child_selected.label.replaceObject(this.dataset);
				this.setAttributes({src: FamilyAssignableToggle.child_selected.image, alt: label, title: label});
			}
		}

		const profiles = get_sorted_profiles(study);
		//TODO find real descendants
		//const parent_assignables = study.getDescendants(assignable_entity);
		const parent_assignables = study.workflows.slice();
		parent_assignables.sort(Config.Entities[assignable_entity.name].getComparator(Languages.GetLanguage()));

		//table
		const table = document.createFullElement('table', {'class': 'family_assignable_matrix'});

		//caption
		const caption = document.createFullElement('caption');
		caption.appendChild(document.createTextNode(assignable_entity.plural_label));
		const download = document.createFullElement('img', {src: 'images/disk.png', title: 'Download matrix', alt: 'Download'});
		download.addEventListener(
			'click',
			function() {
				const data = generate_export_family_assignable_matrix(study, assignable_entity);
				new CSV(data).download(`${assignable_entity.id}_matrix.csv`);
			}
		);
		caption.appendChild(download);
		table.appendChild(caption);

		//header
		const thead = document.createElement('thead');
		table.appendChild(thead);
		const header = document.createElement('tr');
		thead.appendChild(header);

		//first cell
		header.appendChild(document.createFullElement('th'));

		//other headers
		profiles.forEach(function(profile) {
			const profile_cell = document.createFullElement('th');
			const profile_span = document.createFullElement('span');
			profile_span.appendChild(NodeTools.Draw(profile));
			profile_cell.appendChild(profile_span);
			profile_cell.addEventListener('mouseover', highlight_cell);
			profile_cell.addEventListener('mouseout', unhighlight_cell);
			header.appendChild(profile_cell);
		});

		//content
		const tbody = document.createElement('tbody');
		table.appendChild(tbody);

		parent_assignables.forEach(function(parent_assignable) {
			const workflow_line = document.createFullElement('tr', {'class': 'assignable'});
			const workflow_cell = document.createFullElement('td');
			workflow_cell.appendChild(NodeTools.Draw(parent_assignable));
			workflow_line.appendChild(workflow_cell);
			tbody.appendChild(workflow_line);

			profiles.forEach(function(profile) {
				const cell = document.createElement('td');
				cell.addEventListener('mouseover', highlight_cell);
				cell.addEventListener('mouseout', unhighlight_cell);

				//create label values
				const values = {parentAssignableId: parent_assignable.id, profileId: profile.id};

				//toggle
				const toggle = document.createFullElement('img');
				let properties;
				if(profile.grantedWorkflowIds.hasOwnProperty(parent_assignable.id)) {
					properties = FamilyAssignableToggle.parent_selected;
				}
				else {
					properties = FamilyAssignableToggle.parent_unselected;
				}
				const label = properties.label.replaceObject(values);
				toggle.setAttributes({src: properties.image, alt: label, title: label});
				Object.assign(toggle.dataset, values);
				toggle.addEventListener('click', toggle_parent_assignable);
				cell.appendChild(toggle);

				workflow_line.appendChild(cell);
			});

			parent_assignable.actions.forEach(function(child_assignable) {
				const line = document.createElement('tr');
				tbody.appendChild(line);

				const assignable_cell = document.createFullElement('td', {style: 'padding-left: 2rem;'});
				assignable_cell.appendChild(NodeTools.Draw(child_assignable));
				line.appendChild(assignable_cell);

				profiles.forEach(function(profile) {
					const cell = document.createFullElement('td');
					cell.addEventListener('mouseover', highlight_cell);
					cell.addEventListener('mouseout', unhighlight_cell);

					//create label values
					const values = {parentAssignableId: parent_assignable.id, childAssignableId: child_assignable.id, profileId: profile.id};

					//toggle
					const toggle = document.createFullElement('img');
					let properties;
					const child_rights = profile.grantedWorkflowIds[parent_assignable.id] ?? [];
					if(!child_rights.includes(child_assignable.id)) {
						properties = FamilyAssignableToggle.child_unselected;
					}
					else {
						properties = FamilyAssignableToggle.child_selected;
					}
					const label = properties.label.replaceObject(values);
					toggle.setAttributes({src: properties.image, alt: label, title: label});
					Object.assign(toggle.dataset, values);
					toggle.addEventListener('click', toggle_child_assignable);
					cell.appendChild(toggle);

					//disable toggle if no right has been given on parent assignable
					if(!profile.grantedWorkflowIds.hasOwnProperty(parent_assignable.id)) {
						toggle.setAttribute('disabled', 'disabled');
					}

					line.appendChild(cell);
				});
			});
		});

		return table;
	},

	DrawRightAssignableMatrix: function(study, assignable_entity) {
		const rights = Object.keys(Config.Enums.ProfileRightType);
		const profiles = get_sorted_profiles(study);
		const assignables = study.getRightAssignables(assignable_entity).slice();
		assignables.sort(Config.Entities[assignable_entity.name].getComparator(Languages.GetLanguage()));

		//table
		const table = document.createFullElement('table', {'class': 'right_assignable_matrix'});

		//caption
		const caption = document.createFullElement('caption');
		caption.appendChild(document.createTextNode(assignable_entity.plural_label));
		const download = document.createFullElement('img', {src: 'images/disk.png'});
		download.addEventListener(
			'click',
			function() {
				const data = generate_export_right_assignable_matrix(study, assignable_entity);
				new CSV(data).download(`${assignable_entity.id}_matrix.csv`);
			}
		);
		caption.appendChild(download);
		table.appendChild(caption);

		//header
		const thead = document.createElement('thead');
		table.appendChild(thead);
		const header = document.createElement('tr');
		thead.appendChild(header);

		//first cell
		header.appendChild(document.createFullElement('th'));

		//other headers
		profiles.forEach(function(profile) {
			const profile_cell = document.createFullElement('th');
			const profile_span = document.createFullElement('span');
			profile_span.appendChild(NodeTools.Draw(profile));
			profile_cell.appendChild(profile_span);
			profile_cell.addEventListener('mouseover', highlight_cell);
			profile_cell.addEventListener('mouseout', unhighlight_cell);
			header.appendChild(profile_cell);
		});

		function toggle_assignable() {
			const profile = study.getProfile(this.dataset.profileId);
			const assignable_id = this.dataset.assignableId;
			const right = this.dataset.right;
			//create label values
			const values = {assignable: assignable_id, profile: this.dataset.profileId, right: right};
			if(profile.isAssignedRight(assignable_entity, assignable_id, right)) {
				//removing a right also removes more important rights
				const superior_rights = rights.slice(0, rights.indexOf(right) + 1);
				superior_rights.forEach(r => profile.unassignRight(assignable_entity, assignable_id, r));
				//manage interface
				let element = this;
				do {
					values.right = element.dataset.right;
					element.classList.remove('selected');
					element.setAttribute('title', RightAssignableToggle.unselected.label.replaceObject(values));
					element = element.previousElementSibling;
				} while(element);
			}
			else {
				//adding a right also gives less important rights
				const inferior_rights = rights.slice(rights.indexOf(right), rights.length);
				inferior_rights.forEach(r => profile.assignRight(assignable_entity, assignable_id, r));
				//manage interface
				let element = this;
				do {
					values.right = element.dataset.right;
					element.classList.add('selected');
					element.setAttribute('title', RightAssignableToggle.selected.label.replaceObject(values));
					element = element.nextElementSibling;
				} while(element);
			}
		}

		//content
		const tbody = document.createElement('tbody');
		table.appendChild(tbody);

		assignables.forEach(function(assignable) {
			const line = document.createElement('tr');
			tbody.appendChild(line);

			const assignable_cell = document.createFullElement('td');
			assignable_cell.appendChild(NodeTools.Draw(assignable));
			line.appendChild(assignable_cell);

			profiles.forEach(function(profile) {
				const cell = document.createElement('td');
				cell.addEventListener('mouseover', highlight_cell);
				cell.addEventListener('mouseout', unhighlight_cell);

				rights.forEach(function(right) {
					const values = {assignable: assignable.id, profile: profile.id, right: right};
					const toggle = document.createFullElement('span', {}, right.substring(0, 1));
					if(profile.isAssignedRight(assignable_entity, assignable.id, right)) {
						toggle.classList.add('selected');
						toggle.setAttribute('title', RightAssignableToggle.selected.label.replaceObject(values));
					}
					else {
						toggle.setAttribute('title', RightAssignableToggle.unselected.label.replaceObject(values));
					}
					toggle.dataset.profileId = profile.id;
					toggle.dataset.assignableId = assignable.id;
					toggle.dataset.right = right;
					toggle.addEventListener('click', toggle_assignable);
					cell.appendChild(toggle);
				});

				line.appendChild(cell);
			});
		});

		return table;
	},

	DrawAssignableMatrix: function(study, assignable_entity) {
		const profiles = get_sorted_profiles(study);
		const assignables = study.getAssignables(assignable_entity).slice();
		assignables.sort(Config.Entities[assignable_entity.name].getComparator(Languages.GetLanguage()));

		//cache toggles
		const toggles = {};

		//table
		const table = document.createFullElement('table');

		//caption
		const caption = document.createFullElement('caption');
		caption.appendChild(document.createTextNode(assignable_entity.plural_label));
		const download = document.createFullElement('img', {src: 'images/disk.png'});
		download.addEventListener(
			'click',
			function() {
				const data = generate_export_assignable_matrix(study, assignable_entity);
				new CSV(data).download(`${assignable_entity.id}_matrix.csv`);
			}
		);
		caption.appendChild(download);
		table.appendChild(caption);

		//header
		const thead = document.createElement('thead');
		table.appendChild(thead);
		const header = document.createElement('tr');
		thead.appendChild(header);

		//first cell
		header.appendChild(document.createFullElement('th'));

		//other headers
		profiles.forEach(function(profile) {
			const profile_cell = document.createFullElement('th');
			const profile_span = document.createFullElement('span');
			const toggle = document.createFullElement('img', {src: AssignableProfileToggle.unselected.image});
			toggle.dataset.profileId = profile.id;
			toggles[`P_${profile.id}`] = toggle;
			toggle.addEventListener('click', toggle_profile);
			profile_span.appendChild(toggle);
			profile_span.appendChild(NodeTools.Draw(profile));
			profile_cell.appendChild(profile_span);
			profile_cell.addEventListener('mouseover', highlight_cell);
			profile_cell.addEventListener('mouseout', unhighlight_cell);
			header.appendChild(profile_cell);
		});

		function update_toggles() {
			//build assignations cache
			const assignations = {};
			profiles.forEach(function(profile) {
				assignables.forEach(function(assignable) {
					assignations[`${profile.id}_${assignable.id}`] = profile.isAssigned(assignable_entity, assignable.id);
				});
			});

			function is_assigned(profile, assignable) {
				return assignations[`${profile.id}_${assignable.id}`];
			}

			profiles.forEach(function(profile) {
				let all_assigned = true;
				assignables.forEach(function(assignable) {
					//let toggle = document.querySelector('img[data-profile-id="' + profile.id + '"][data-assignable-id="' + assignable.id + '"]');
					const toggle = toggles[`P_${profile.id}_A_${assignable.id}`];
					const values = {assignable: assignable.id, profile: profile.id};
					if(is_assigned(profile, assignable)) {
						const label = AssignableToggle.selected.label.replaceObject(values);
						toggle.setAttributes({src: AssignableToggle.selected.image, alt: label, title: label});
					}
					else {
						all_assigned = false;
						const label = AssignableToggle.unselected.label.replaceObject(values);
						toggle.setAttributes({src: AssignableToggle.unselected.image, alt: label, title: label});
					}
				});
				//let profile_toggle = document.querySelector('th img[data-profile-id="' + profile.id + '"]');
				const profile_toggle = toggles[`P_${profile.id}`];
				if(all_assigned) {
					const label = AssignableProfileToggle.selected.label.replaceObject({profile: profile.id});
					profile_toggle.setAttributes({src: AssignableProfileToggle.selected.image, alt: label, title: label});
				}
				else {
					const label = AssignableProfileToggle.unselected.label.replaceObject({profile: profile.id});
					profile_toggle.setAttributes({src: AssignableProfileToggle.unselected.image, alt: label, title: label});
				}
			});
			assignables.forEach(function(assignable) {
				//let assignable_toggle = document.querySelector('td:first-child img[data-assignable-id="' + assignable.id + '"]');
				const assignable_toggle = toggles[`A_${assignable.id}`];
				if(study.profiles.every(p => is_assigned(p, assignable))) {
					const label = AssignableAssignableToggle.selected.label.replaceObject({assignable: assignable.id});
					assignable_toggle.setAttributes({src: AssignableAssignableToggle.selected.image, alt: label, title: label});
				}
				else {
					const label = AssignableAssignableToggle.unselected.label.replaceObject({assignable: assignable.id});
					assignable_toggle.setAttributes({src: AssignableAssignableToggle.unselected.image, alt: label, title: label});
				}
			});
		}

		function toggle_profile() {
			const profile = study.getProfile(this.dataset.profileId);
			//check what to do
			let action;
			if(assignables.every(a => profile.isAssigned(assignable_entity, a.id))) {
				action = a => profile.unassign(assignable_entity, a.id);
			}
			else {
				action = a => profile.assign(assignable_entity, a.id);
			}
			assignables.forEach(action);
			update_toggles();
		}

		function toggle_assignable() {
			const assignable_id = this.dataset.assignableId;
			//check what to do
			let action;
			if(profiles.every(p => p.isAssigned(assignable_entity, assignable_id))) {
				action = p => p.unassign(assignable_entity, assignable_id);
			}
			else {
				action = p => p.assign(assignable_entity, assignable_id);
			}
			profiles.forEach(action);
			update_toggles();
		}

		function toggle_profile_assignable() {
			const profile = study.getProfile(this.dataset.profileId);
			if(profile.isAssigned(assignable_entity, this.dataset.assignableId)) {
				profile.unassign(assignable_entity, this.dataset.assignableId);
			}
			else {
				profile.assign(assignable_entity, this.dataset.assignableId);
			}
			update_toggles();
		}

		//content
		const tbody = document.createElement('tbody');
		table.appendChild(tbody);

		assignables.forEach(function(assignable) {
			const line = document.createElement('tr');
			tbody.appendChild(line);

			const assignable_cell = document.createFullElement('td');
			const assignable_span = document.createFullElement('span');
			const toggle = document.createFullElement('img', {src: AssignableAssignableToggle.unselected.image});
			toggle.dataset.assignableId = assignable.id;
			toggles[`A_${assignable.id}`] = toggle;
			toggle.addEventListener('click', toggle_assignable);
			assignable_span.appendChild(toggle);
			assignable_span.appendChild(NodeTools.Draw(assignable));
			assignable_cell.appendChild(assignable_span);
			line.appendChild(assignable_cell);

			profiles.forEach(function(profile) {
				const cell = document.createElement('td');
				cell.addEventListener('mouseover', highlight_cell);
				cell.addEventListener('mouseout', unhighlight_cell);
				const toggle = document.createFullElement('img', {src: AssignableToggle.unselected.image});
				toggle.dataset.profileId = profile.id;
				toggle.dataset.assignableId = assignable.id;
				toggles[`P_${profile.id}_A_${assignable.id}`] = toggle;
				toggle.addEventListener('click', toggle_profile_assignable);

				cell.appendChild(toggle);
				line.appendChild(cell);
			});
		});

		update_toggles();

		return table;
	},

	ExportMatrices: function() {
		const study = StudyHandler.GetStudy();
		const data = [];
		Assignables.forEach(function(entity) {
			data.pushAll(generate_export_assignable_matrix(study, entity));
			data.push([]);
		});
		RightAssignables.forEach(function(entity) {
			data.pushAll(generate_export_right_assignable_matrix(study, entity));
			data.push([]);
		});
		FamilyAssignableParents.forEach(function(entity) {
			if(Entities.Study.children.hasOwnProperty(entity.name)) {
				data.pushAll(generate_export_family_assignable_matrix(study, entity));
				data.push([]);
			}
		});
		new CSV(data).download('matrices.csv');
	}
};

/*
function draw_profile_matrices() {
	Router.Reset();

	const matrix = document.getElementById('matrix');
	matrix.style.display = 'block';

	const description_max_size = 0;
	let profile_matrix;
	for(let i = 0; i < Assignables.length; i++) {
		profile_matrix = draw_profile_matrix(Assignables[i]);
		matrix.appendChild(profile_matrix);
		//retain max size
		if(profile_matrix.firstChild.firstChild.offsetWidth > description_max_size) {
			description_max_size = profile_matrix.firstChild.firstChild.offsetWidth;
		}
	}
	//calculate matrix size based on last drawn matrix
	const matrix_size = description_max_size;
	for(let i = profile_matrix.firstChild.childNodes.length - 1; i >= 0; i--) {
		matrix_size += profile_matrix.firstChild.childNodes[i].offsetWidth;
	}

	for(let i = 0; i < RightAssignables.length; i++) {
		const right_assignable_matrix = draw_advanced_profile_matrix(RightAssignables[i]);
		matrix.appendChild(right_assignable_matrix);
		matrix_size += right_assignable_matrix.firstChild.childNodes[i].offsetWidth;
	}

	//adjust matrices size
	const matrix_tables = matrix.querySelectorAll('table');
	for(let i = matrix_tables.length - 1; i >= 0; i--) {
		const matrix_table = matrix_tables[i];
		matrix_table.style.width = matrix_size + 'px';
		matrix_table.style.tableLayout = 'fixed';
		matrix_table.firstChild.firstChild.style.width = description_max_size + 'px';
	}
};
*/
