import {API} from './api.js';

const user_email = 'investigator@rodano.ch';
const user_password = 'Password1!';

const api = new API();

export default async function test(assert) {

	async function auth() {
		//login with unknown user
		let response = await api.auth.login('john.doe@rodano.ch', 'toto');
		assert.equal(response.status, 401, 'Login with a wrong login returns a 401 http response');
		assert.equal(response.message, 'Wrong login or password', 'Login with invalid login returns error "Wrong login or password"');
		//login
		response = await api.auth.login(user_email, user_password);
		assert.equal(response.status, 200, 'Successful login returns a 200 http response');
		assert.equal(response.data.email, user_email, 'Successful login returns the good user');
		//logout
		await api.auth.logout();
		//login again
		response = await api.auth.login(user_email, user_password);
		assert.equal(response.data.email, user_email, 'Successful login returns the good user');
		await api.auth.logout();
	}

	async function config() {
		await api.auth.login(user_email, user_password);
		const response = await api.config.pull();
		assert.equal(response.data.id, 'TEST', 'Getting config give config with id "TEST"');
		assert.notOk(response.data.profiles.isEmpty(), 'Getting config give config containing profiles');
	}

	async function me() {
		await api.auth.login(user_email, user_password);
		const response = await api.users.me();
		assert.equal(response.data.email, user_email, 'Successful login returns the good user');
		assert.equal(response.data.name, 'Investigator', 'Successful login returns the good user');
	}

	assert.begin();
	await auth();
	await config();
	await me();
	assert.end();
}
