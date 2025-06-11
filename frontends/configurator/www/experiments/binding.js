import '../basic-tools/extension.js';
import './binding_core.js';

document.addEventListener('DOMContentLoaded', function() {
	//welcome
	const context = {
		name: 'John Doe',
		time: new Date(),
	};
	setInterval(function() {
		context.time = new Date();
	}, 1000);

	document.getElementById('welcome').autobind(context);

	//users
	const users = [];
	users.push({greetings: 'mr', firstname: 'John', lastname: 'Doe', email: 'john@doe.name', offline: true, profiles: ['user', 'admin']});
	users.push({greetings: 'mr', firstname: 'Chuck', lastname: 'Norris', email: 'chuck@norris.name', offline: false, profiles: ['user']});
	const greetings = {mr: 'Mr', ms: 'Ms', m: 'M'};

	function user_computations(user) {
		return {
			admin: {
				toModel: function(value) {
					if(value) {
						if(!this.profiles) {
							this.profiles = [];
						}
						if(!this.profiles.includes('admin')) {
							this.profiles.push('admin');
						}
					}
					else {
						this.profiles.removeElement('admin');
					}
				},
				toUi: function() {
					return this.profiles?.includes('admin');
				},
				context: user,
				dependencies: [{object: user, properties: ['profiles']}]
			}
		};
	}

	document.getElementById('users').lastElementChild.bindArray(users, function(user) {
		const user_representation = document.createFullElement('tr');
		//greetings
		const user_greetings_representation = document.createFullElement('td', {}, greetings[user.greetings]);
		user_greetings_representation.bindText(user, 'greetings', function(value) {return value ? greetings[value] : '';});
		user_representation.appendChild(user_greetings_representation);
		//first name
		const user_firstname_representation = document.createFullElement('td', {}, user.firstname);
		user_firstname_representation.bindText(user, 'firstname');
		user_representation.appendChild(user_firstname_representation);
		//last name
		const user_lastname_representation = document.createFullElement('td', {}, user.lastname);
		user_lastname_representation.bindText(user, 'lastname');
		user_representation.appendChild(user_lastname_representation);
		//e-mail
		const user_email_representation = document.createFullElement('td', {}, user.email);
		user_email_representation.bindText(user, 'email');
		user_representation.appendChild(user_email_representation);
		//offline
		const user_offline_representation = document.createFullElement('td');
		user_offline_representation.bind(user, 'offline', function(value) {
			this.empty();
			if(value) {
				this.appendChild(document.createFullElement('img', {src: 'tick.png', alt: 'Admin'}));
			}
		});
		user_representation.appendChild(user_offline_representation);
		//profiles
		const user_profiles_representation = document.createFullElement('td');
		user_profiles_representation.bind(user, 'profiles', function(value) {
			if(value) {
				this.textContent = value.join(', ');
			}
		});
		user_representation.appendChild(user_profiles_representation);
		//action
		const user_action_representation = document.createFullElement('td');
		//edit
		const edit_user_button = document.createFullElement('img', {src: 'page_white_edit.png', title: 'Edit'});
		edit_user_button.addEventListener(
			'click',
			function() {
				document.getElementById('user').bind(user, {}, user_computations(user));
				document.getElementById('user_save').style.display = 'none';
				document.getElementById('user_cancel').textContent = 'Ok';
				document.getElementById('user').style.display = 'block';
			}
		);
		user_action_representation.appendChild(edit_user_button);
		//delete
		const delete_user_button = document.createFullElement('img', {src: 'cross.png', title: 'Delete'});
		delete_user_button.addEventListener(
			'click',
			function() {
				users.removeElement(user);
				const form = document.getElementById('user');
				if(user === form.boundObject) {
					form.style.display = 'none';
				}
			}
		);
		user_action_representation.appendChild(delete_user_button);
		user_representation.appendChild(user_action_representation);
		return user_representation;
	});
	document.getElementById('user_add').addEventListener(
		'click',
		function() {
			const user = {};
			document.getElementById('user').bind(user, {}, user_computations(user));
			document.getElementById('user_save').style.display = 'inline';
			document.getElementById('user_cancel').textContent = 'Cancel';
			document.getElementById('user').style.display = 'block';
		}
	);
	document.getElementById('user_cancel').addEventListener(
		'click',
		function() {
			document.getElementById('user').style.display = 'none';
		}
	);
	document.getElementById('user').addEventListener(
		'submit',
		function(event) {
			event.stop();
			const user = this.boundObject;
			if(!users.includes(user)) {
				users.push(user);
			}
			this.style.display = 'none';
		}
	);
});
