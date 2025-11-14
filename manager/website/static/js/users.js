import {api_base_url, Fetch} from './fetch.js';

function draw_user(user) {
	const date = new Date(user.login_date).toFullDisplay();
	return document.createFullElement('li', {}, `${user.name} (${user.email}) ${date}`);
}

export const Users = {
	GetUsers: function() {
		return Fetch(`${api_base_url}/users`, {'managed-error': [500]});
	},
	RefreshUsers() {
		Users.GetUsers()
			.then(users => {
				document.getElementById('connected_users_message').textContent = `Connected users: ${users.length}`;
				const connected_users = document.getElementById('connected_users');
				connected_users.empty();
				users.map(draw_user).forEach(Node.prototype.appendChild, connected_users);
			})
			.catch(() => {
				document.getElementById('connected_users_message').textContent = 'Connected users: NA';
			});
	},
	Init: function() {
		setInterval(() => Users.RefreshUsers(), 10000);
	}
};
