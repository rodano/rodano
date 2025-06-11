import './basic-tools/extension.js';

import {CSV} from './basic-tools/csv.js';
import {Config} from './model_config.js';
import {Languages} from './languages.js';
import {NodeTools} from './node_tools.js';
import {StudyHandler} from './study_handler.js';
import {Entities} from './model/config/entities.js';
import {Profile} from './model/config/entities/profile.js';
import {Right} from './model/config/entities/right.js';
import {ProfileRight} from './model/config/entities/profile_right.js';
import {Assignables, Attributables, RightAssignables} from './model/config/entities_categories.js';

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

const AttributableToggle = {
	attributable_selected: {
		image: 'images/tick.png',
		label: 'Remove right on ${attributableId} for profile ${profileId}'
	},
	attributable_unselected: {
		image: 'images/untick.png',
		label: 'Give right on ${attributableId} for profile ${profileId}'
	},
	assignable_selected: {
		image: 'images/tick.png',
		label: 'Remove right on ${assignableId} of ${attributableId} for profile ${profileId}'
	},
	assignable_unselected: {
		image: 'images/untick.png',
		label: 'Give right on ${assignableId} of ${attributableId} for profile ${profileId}'
	},
	assignable_partially_selected: {
		image: 'images/check_error.png',
		label: 'Remove right on ${assignableId} of ${attributableId} for profile ${profileId}'
	},
	assignable_details: {
		label: 'See details of ${assignableId} of ${attributableId} for profile ${profileId}'
	},
	profile_selected: {
		image: 'images/tick.png',
		label: 'Remove right on ${assignableId} for profile ${profileId} if ${attributableId} has been created by profile ${creatorProfileId}'
	},
	profile_unselected: {
		image: 'images/untick.png',
		label: 'Give right on ${assignableId} for profile ${profileId} if ${attributableId} has been created by profile ${creatorProfileId}'
	},
	system_selected: {
		image: 'images/tick.png',
		label: 'Remove right on ${assignableId} for profile ${profileId} if ${attributableId} has been created by the system'
	},
	system_unselected: {
		image: 'images/untick.png',
		label: 'Give right on ${assignableId} for profile ${profileId} if ${attributableId} has been created by the system'
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

function generate_export_attributable_matrix(study, assignable_entity) {
	const data = [];
	//add header
	data.push(generate_export_matrix_header(study, assignable_entity));
	//add data
	const profiles = get_sorted_profiles(study);
	//TODO find real descendants
	//const attributables = study.getDescendants(assignable_entity);
	const attributables = study.workflows.slice();
	attributables.sort(Config.Entities[assignable_entity.name].getComparator(Languages.GetLanguage()));
	attributables.forEach(function(attributable) {
		//add line for attributable
		const line = [];
		line.push(attributable.getLocalizedShortname(Languages.GetLanguage()));
		line.pushAll(profiles.map(p => p.grantedWorkflowIds[attributable.id]?.right ? 'X' : ''));
		data.push(line);
		//add lines for actions
		attributable.actions.forEach(function(assignable) {
			const line = [];
			line.push(assignable.getLocalizedShortname(Languages.GetLanguage()));
			profiles.forEach(function(profile) {
				const rights = [];
				if(profile.grantedWorkflowIds[attributable.id]) {
					const profile_right = profile.grantedWorkflowIds[attributable.id].childRights[assignable.id];
					if(profile_right) {
						if(profile_right.system) {
							rights.push('SYSTEM');
						}
						rights.pushAll(profile_right.profileIds);
					}
				}
				line.push(rights.join(', '));
			});
			data.push(line);
		});
	});
	return data;
}

export const Matrices = {
	DrawAttributableMatrix: function(study, assignable_entity) {
		function toggle_attributable() {
			const profile = study.getProfile(this.dataset.profileId);
			//create profile right if needed
			if(!profile.grantedWorkflowIds[this.dataset.attributableId]) {
				profile.grantedWorkflowIds[this.dataset.attributableId] = new Right();
			}
			//adjust right
			if(profile.grantedWorkflowIds[this.dataset.attributableId].right) {
				profile.grantedWorkflowIds[this.dataset.attributableId].right = false;
				const label = AttributableToggle.attributable_unselected.label.replaceObject(this.dataset);
				this.setAttributes({src: AttributableToggle.attributable_unselected.image, alt: label, title: label});
				//remove right on all attributable assignable and disable attributable assignable toggles
				profile.grantedWorkflowIds[this.dataset.attributableId].childRights = {};
				const attributable = study.getWorkflow(this.dataset.attributableId);
				attributable.actions.forEach(function(assignable) {
					const assignable_toggles = document.querySelectorAll(`[data-attributable-id="${attributable.id}"][data-assignable-id="${assignable.id}"][data-profile-id="${profile.id}"]`);
					const label = AttributableToggle.assignable_unselected.label.replaceObject(assignable_toggles[0].dataset);
					assignable_toggles[0].setAttributes({src: AttributableToggle.assignable_unselected.image, alt: label, title: label});
					assignable_toggles.forEach(a => a.setAttribute('disabled', 'disabled'));
				});
			}
			else {
				profile.grantedWorkflowIds[this.dataset.attributableId].right = true;
				const label = AttributableToggle.attributable_selected.label.replaceObject(this.dataset);
				this.setAttributes({src: AttributableToggle.attributable_selected.image, alt: label, title: label});
				//enable attributable assignable toggles
				const attributable = study.getWorkflow(this.dataset.attributableId);
				attributable.actions.forEach(function(assignable) {
					const assignable_toggles = document.querySelectorAll(`[data-attributable-id="${attributable.id}"][data-assignable-id="${assignable.id}"][data-profile-id="${profile.id}"]`);
					assignable_toggles.forEach(a => a.removeAttribute('disabled'));
				});
			}
		}

		function toggle_assignable() {
			//toggle may be disable if no right has been given on attributable
			if(this.hasAttribute('disabled')) {
				return;
			}

			const profile = study.getProfile(this.dataset.profileId);
			//create profile right if needed
			if(!profile.grantedWorkflowIds[this.dataset.attributableId]) {
				profile.grantedWorkflowIds[this.dataset.attributableId] = new Right();
			}
			if(!profile.grantedWorkflowIds[this.dataset.attributableId].childRights[this.dataset.assignableId]) {
				profile.grantedWorkflowIds[this.dataset.attributableId].childRights[this.dataset.assignableId] = new ProfileRight();
			}
			const assignable_right = profile.grantedWorkflowIds[this.dataset.attributableId].childRights[this.dataset.assignableId];
			//create label values
			if(this.src.includes(AttributableToggle.assignable_unselected.image)) {
				assignable_right.profileIds = profile_ids.slice();
				assignable_right.system = true;
				const label = AttributableToggle.assignable_selected.label.replaceObject(this.dataset);
				this.setAttributes({src: AttributableToggle.assignable_selected.image, alt: label, title: label});
			}
			else {
				assignable_right.profileIds.length = 0;
				assignable_right.system = false;
				const label = AttributableToggle.assignable_unselected.label.replaceObject(this.dataset);
				this.setAttributes({src: AttributableToggle.assignable_unselected.image, alt: label, title: label});
			}
		}

		function details_assignable(event) {
			event.stop();
			//toggle may be disable if no right has been given on attributable
			if(this.hasAttribute('disabled')) {
				return;
			}

			const profile = study.getProfile(this.dataset.profileId);
			const that = this;

			function draw_creator_profile_item(creator_profile) {
				const profile_item = document.createFullElement('li');
				//build profile icon
				const selected = profile.grantedWorkflowIds[that.dataset.attributableId].childRights[that.dataset.assignableId]?.profileIds.includes(creator_profile.id);
				const properties = selected ? AttributableToggle.profile_selected : AttributableToggle.profile_unselected;
				const values = {
					attributableId: that.dataset.attributableId,
					assignableId: that.dataset.assignableId,
					profileId: profile.id,
					creatorProfileId: creator_profile.id
				};
				const label = properties.label.replaceObject(values);
				const profile_item_icon = document.createFullElement('img', {src: properties.image, alt: label, title: label});
				Object.assign(profile_item_icon.dataset, values);
				profile_item_icon.addEventListener('click', toggle_click_listener);
				profile_item.appendChild(profile_item_icon);
				profile_item.appendChild(document.createTextNode(creator_profile.getLocalizedShortname(Languages.GetLanguage())));
				return profile_item;
			}

			function toggle_click_listener() {
				const profile = study.getProfile(this.dataset.profileId);
				//create profile right if needed
				if(!profile.grantedWorkflowIds[this.dataset.attributableId]) {
					profile.grantedWorkflowIds[this.dataset.attributableId] = new Right();
				}
				if(!profile.grantedWorkflowIds[this.dataset.attributableId].childRights[this.dataset.assignableId]) {
					profile.grantedWorkflowIds[this.dataset.attributableId].childRights[this.dataset.assignableId] = new ProfileRight();
				}
				const profile_right = profile.grantedWorkflowIds[this.dataset.attributableId].childRights[this.dataset.assignableId];
				//toggle right
				if(this.dataset.system) {
					if(profile_right.system) {
						profile_right.system = false;
						this.setAttributes({src: AttributableToggle.system_unselected.image, alt: AttributableToggle.system_unselected.label.replaceObject(this.dataset), title: AttributableToggle.system_unselected.label.replaceObject(this.dataset)});
					}
					else {
						profile_right.system = true;
						this.setAttributes({src: AttributableToggle.system_selected.image, alt: AttributableToggle.system_selected.label.replaceObject(this.dataset), title: AttributableToggle.system_selected.label.replaceObject(this.dataset)});
					}
				}
				else {
					if(profile_right.profileIds.includes(this.dataset.creatorProfileId)) {
						profile_right.profileIds.removeElement(this.dataset.creatorProfileId);
						this.setAttributes({src: AttributableToggle.profile_unselected.image, alt: AttributableToggle.profile_unselected.label.replaceObject(this.dataset), title: AttributableToggle.profile_unselected.label.replaceObject(this.dataset)});
					}
					else {
						profile_right.profileIds.push(this.dataset.creatorProfileId);
						this.setAttributes({src: AttributableToggle.profile_selected.image, alt: AttributableToggle.profile_selected.label.replaceObject(this.dataset), title: AttributableToggle.profile_selected.label.replaceObject(this.dataset)});
					}
				}

				//update labels
				let properties;
				if(profile_right.profileIds.isEmpty() && !profile_right.system) {
					properties = AttributableToggle.assignable_unselected;
				}
				else if(Object.equals(profile_right.profileIds.sort(), profile_ids.sort()) && profile_right.system) {
					properties = AttributableToggle.assignable_selected;
				}
				else {
					properties = AttributableToggle.assignable_partially_selected;
				}
				const assignable_toggle = document.querySelector(`[data-attributable-id="${this.dataset.attributableId}"][data-assignable-id="${this.dataset.assignableId}"][data-profile-id="${this.dataset.profileId}"]`);
				assignable_toggle.setAttributes({src: properties.image, alt: properties.label.replaceObject(this.dataset), title: properties.label.replaceObject(this.dataset)});
			}

			//remove previous details window
			let details = document.getElementById('profile_right_matrix_matrix');
			if(details) {
				details.parentNode.removeChild(details);
			}
			//calculate window position
			const position = this.getPosition();
			//create window
			details = document.createFullElement('div', {id: 'profile_right_matrix_matrix', style: `left: ${position.left}px; top: ${position.top}px;`});
			//title
			const title = document.createFullElement('h2');
			const details_close = document.createFullElement('img', {src: 'images/cross.png', alt: 'Close', title: 'Close', style: 'position: absolute; right: 0.5rem; top: 0.5rem; cursor: pointer;'});
			details_close.addEventListener('click', function() {
				document.body.removeChild(details);
			});
			title.appendChild(document.createTextNode('Rights'));//for ' + profile.id + ' on ' + that.dataset.assignableId));
			title.appendChild(details_close);
			details.appendChild(title);
			//creator rights list
			const profile_list = document.createFullElement('ul');
			details.appendChild(profile_list);
			//creator profiles items
			profiles.map(draw_creator_profile_item).forEach(Node.prototype.appendChild, profile_list);
			//system item
			const system_item = document.createFullElement('li');
			const selected = profile.grantedWorkflowIds[that.dataset.attributableId].childRights[that.dataset.assignableId]?.system;
			const properties = selected ? AttributableToggle.system_selected : AttributableToggle.system_unselected;
			const values = {
				attributableId: that.dataset.attributableId,
				assignableId: that.dataset.assignableId,
				profileId: profile.id,
				system: true
			};
			const system_item_icon = document.createFullElement('img', {src: properties.image, alt: properties.label.replaceObject(values), title: properties.label.replaceObject(values)});
			Object.assign(system_item_icon.dataset, values);
			system_item_icon.addEventListener('click', toggle_click_listener);
			system_item.appendChild(system_item_icon);
			system_item.appendChild(document.createTextNode('SYSTEM'));
			profile_list.appendChild(system_item);

			//TODO this is standard but does not work on chrome
			/*profile_list.addEventListener('mouseleave', function(event) {
				document.body.removeChild(this);
			});*/
			//other strategy
			/*function hide_profile_list(event) {
				if(!profile_list.contains(event.target)) {
					document.removeEventListener('mousemove', hide_profile_list);
					document.body.removeChild(profile_list);
				}
			}
			document.addEventListener('mousemove', hide_profile_list);*/
			/*profile_list.addEventListener('mouseout', function(event) {
				if(event.target === profile_list) {
					document.body.removeChild(profile_list);
				}
			});*/

			document.body.appendChild(details);
			//adjust position according to actual height
			details.style.marginTop = `${-Math.round(details.offsetHeight / 2)}px`;
			//adjust position if window is outside browser window
			//overflow on top
			if(details.offsetTop < 70) {
				details.style.top = `${parseInt(details.style.top) + 70}px`;
			}
			//overflow on bottom
			const offset_bottom = details.offsetTop + details.offsetHeight;
			if(offset_bottom > (document.body.offsetHeight - 5)) {
				details.style.top = `${parseInt(details.style.top) + (document.body.offsetHeight - offset_bottom - 5)}px`;
			}
			//overflow on right
			const offset_right = details.offsetLeft + details.offsetWidth;
			if(offset_right > (document.body.offsetWidth - 5)) {
				details.style.left = `${parseInt(details.style.left) + (document.body.offsetWidth - offset_right - 5)}px`;
			}
		}

		const profiles = get_sorted_profiles(study);
		const profile_ids = profiles.map(p => p.id);
		//TODO find real descendants
		//const attributables = study.getDescendants(assignable_entity);
		const attributables = study.workflows.slice();
		attributables.sort(Config.Entities[assignable_entity.name].getComparator(Languages.GetLanguage()));

		//table
		const table = document.createFullElement('table', {'class': 'attributable_matrix'});

		//caption
		const caption = document.createFullElement('caption');
		caption.appendChild(document.createTextNode(assignable_entity.plural_label));
		const download = document.createFullElement('img', {src: 'images/disk.png', title: 'Download matrix', alt: 'Download'});
		download.addEventListener(
			'click',
			function() {
				const data = generate_export_attributable_matrix(study, assignable_entity);
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

		attributables.forEach(function(attributable) {
			const workflow_line = document.createFullElement('tr', {'class': 'assignable'});
			const workflow_cell = document.createFullElement('td');
			workflow_cell.appendChild(NodeTools.Draw(attributable));
			workflow_line.appendChild(workflow_cell);
			tbody.appendChild(workflow_line);

			profiles.forEach(function(profile) {
				const cell = document.createElement('td');
				cell.addEventListener('mouseover', highlight_cell);
				cell.addEventListener('mouseout', unhighlight_cell);

				//create label values
				const values = {attributableId: attributable.id, profileId: profile.id};

				//toggle
				const toggle = document.createFullElement('img');
				let properties;
				if(profile.grantedWorkflowIds[attributable.id]?.right) {
					properties = AttributableToggle.attributable_selected;
				}
				else {
					properties = AttributableToggle.attributable_unselected;
				}
				const label = properties.label.replaceObject(values);
				toggle.setAttributes({src: properties.image, alt: label, title: label});
				Object.assign(toggle.dataset, values);
				toggle.addEventListener('click', toggle_attributable);
				cell.appendChild(toggle);

				workflow_line.appendChild(cell);
			});

			attributable.actions.forEach(function(assignable) {
				const line = document.createElement('tr');
				tbody.appendChild(line);

				const assignable_cell = document.createFullElement('td', {style: 'padding-left: 2rem;'});
				assignable_cell.appendChild(NodeTools.Draw(assignable));
				line.appendChild(assignable_cell);

				profiles.forEach(function(profile) {
					const cell = document.createFullElement('td');
					cell.addEventListener('mouseover', highlight_cell);
					cell.addEventListener('mouseout', unhighlight_cell);

					if(!profile.grantedWorkflowIds[attributable.id]) {
						profile.grantedWorkflowIds[attributable.id] = new Right();
					}

					//create label values
					const values = {attributableId: attributable.id, assignableId: assignable.id, profileId: profile.id};

					//toggle
					const toggle = document.createFullElement('img');
					const profile_right = profile.grantedWorkflowIds[attributable.id].childRights[assignable.id];
					if(!profile_right || profile_right.profileIds.isEmpty() && !profile_right.system) {
						const label = AttributableToggle.assignable_unselected.label.replaceObject(values);
						toggle.setAttributes({src: AttributableToggle.assignable_unselected.image, alt: label, title: label});
					}
					else if(Object.equals(profile_right.profileIds.sort(), profile_ids.sort()) && profile_right.system) {
						const label = AttributableToggle.assignable_selected.label.replaceObject(values);
						toggle.setAttributes({src: AttributableToggle.assignable_selected.image, alt: label, title: label});
					}
					else {
						const label = AttributableToggle.assignable_partially_selected.label.replaceObject(values);
						toggle.setAttributes({src: AttributableToggle.assignable_partially_selected.image, alt: label, title: label});
					}
					Object.assign(toggle.dataset, values);
					toggle.addEventListener('click', toggle_assignable);
					cell.appendChild(toggle);

					//details
					const label = AttributableToggle.assignable_details.label.replaceObject(values);
					const details = document.createFullElement('img', {src: 'images/bullet_toggle_plus.png', alt: label, title: label});
					Object.assign(details.dataset, values);
					details.addEventListener('click', details_assignable);
					cell.appendChild(details);

					//disable toggle if no right has been given on attributable
					if(!profile.grantedWorkflowIds[attributable.id].right) {
						toggle.setAttribute('disabled', 'disabled');
						details.setAttribute('disabled', 'disabled');
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
				profile.unassignRight(assignable_entity, assignable_id, right);
				this.setAttribute('title', RightAssignableToggle.unselected.label.replaceObject(values));
				this.classList.remove('selected');
			}
			else {
				//having an important right gives all less important rights
				const inferior_rights = rights.slice(rights.indexOf(right), rights.length);
				inferior_rights.forEach(r => profile.assignRight(assignable_entity, assignable_id, r));
				//manage rights
				this.setAttribute('title', RightAssignableToggle.selected.label.replaceObject(values));
				this.classList.add('selected');
				let element = this;
				while(element.nextElementSibling) {
					element = element.nextElementSibling;
					element.classList.add('selected');
					values.right = element.dataset.right;
					element.setAttribute('title', RightAssignableToggle.selected.label.replaceObject(values));
				}
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
		Attributables.forEach(function(entity) {
			if(Entities.Study.children.hasOwnProperty(entity.name)) {
				data.pushAll(generate_export_attributable_matrix(study, entity));
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
