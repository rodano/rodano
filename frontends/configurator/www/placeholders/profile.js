import {Effects} from '../tools/effects.js';
import {Languages} from '../languages.js';
import {Profile} from '../model/config/entities/profile.js';

const sort_margin = 5;

let selected_node;

Effects.Sortable(
	document.getElementById('nodes_profiles_sort'),
	function() {
		let offset = 0;
		this.children.forEach(function(child) {
			selected_node.getProfile(child.dataset.profileId).orderBy = offset;
			child.querySelector('span').textContent = offset;
			offset += sort_margin;
		});
	}
);

function draw_profile(profile) {
	const profile_li = document.createFullElement('li', {'data-profile-id': profile.id});
	profile_li.appendChild(document.createFullElement('img', {src: 'images/arrows_up_down.png', alt: 'Sort profile', title: 'Sort profile'}));
	profile_li.appendChild(document.createFullElement('span', {}, profile.orderBy === undefined ? 'x' : profile.orderBy));
	profile_li.appendChild(document.createTextNode(profile.getLocalizedLabel(Languages.GetLanguage())));
	return profile_li;
}

export default {
	open: function(node) {
		selected_node = node;

		const profiles = node.profiles.slice();
		profiles.sort(Profile.getOrderComparator());
		profiles.map(draw_profile).forEach(Node.prototype.appendChild, document.getElementById('nodes_profiles_sort').empty());
	}
};
