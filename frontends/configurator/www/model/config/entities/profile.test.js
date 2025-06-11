import {Profile} from './profile.js';
import {Feature} from './feature.js';
import {ScopeModel} from './scope_model.js';
import {Entities} from '../entities.js';

export default async function test(bundle, assert) {
	bundle.begin();

	await bundle.describe('Profile#isAssigned', async bundle_feature => {
		const profile = new Profile({
			id: 'ADMIN',
			shortname: {
				en: 'Admin',
				fr: 'Admin'
			}
		});
		const feature = new Feature({
			id: 'MANAGE_DELETED_DATA'
		});

		//assignation
		await bundle_feature.it('works properly', () => {
			assert.equal(profile.grantedFeatureIds.length, 0, 'No feature has been assigned to profile');
			profile.assign(Entities.Feature, feature.id);
			assert.equal(profile.grantedFeatureIds.length, 1, 'One feature has been assigned to profile');
			assert.equal(profile.grantedFeatureIds[0], 'MANAGE_DELETED_DATA', '"MANAGE_DELETED_DATA" is the id of the first feature assigned to profile"');
			assert.ok(profile.isAssigned(Entities.Feature, 'MANAGE_DELETED_DATA'), 'Feature "MANAGE_DELETED_DATA" has been assigned to profile');
			profile.unassign(Entities.Feature, feature.id);
			assert.equal(profile.grantedFeatureIds.length, 0, 'There is no more feature assigned to profile');
			assert.notOk(profile.isAssigned(Entities.Feature, 'MANAGE_DELETED_DATA'), 'Feature "MANAGE_DELETED_DATA" is no more assigned to profile');
		});
	});

	await bundle.describe('Profile#isAssignedRight', async bundle_feature => {
		const profile = new Profile({
			id: 'ADMIN',
			shortname: {
				en: 'Admin',
				fr: 'Admin'
			}
		});
		const scope_model = new ScopeModel({
			id: 'PATIENT',
			shortname: {
				en: 'Patient',
				fr: 'Patient'
			}
		});

		//right assignation
		await bundle_feature.it('works properly', () => {
			assert.ok(Object.isEmpty(profile.grantedScopeModelIdRights), 'No right has been assigned to any scope model for profile');
			profile.assignRight(Entities.ScopeModel, scope_model.id, 'READ');
			assert.notOk(Object.isEmpty(profile.grantedScopeModelIdRights), 'A right as been assigned to a scope model for profile');
			assert.equal(profile.grantedScopeModelIdRights[scope_model.id].length, 1, 'There is 1 right assigned for the scope model in profile');
			assert.equal(profile.grantedScopeModelIdRights[scope_model.id][0], 'READ', 'Right assigned to scope model is "READ"');
			assert.ok(profile.isAssignedRight(Entities.ScopeModel, scope_model.id, 'READ'), 'Right "READ" has been assigned to profile');
			profile.assignRight(Entities.ScopeModel, scope_model.id, 'WRITE');
			assert.equal(profile.grantedScopeModelIdRights[scope_model.id].length, 2, 'There is 2 rights assigned for the scope model in profile');
			assert.equal(profile.grantedScopeModelIdRights[scope_model.id][1], 'WRITE', 'Right assigned to scope model is "WRITE"');
			assert.ok(profile.isAssignedRight(Entities.ScopeModel, scope_model.id, 'WRITE'), 'Right "WRITE" has been assigned to profile');
			assert.ok(profile.isAssignedRight(Entities.ScopeModel, scope_model.id, 'READ'), 'Right "READ" is still assigned to profile');
			profile.unassignRight(Entities.ScopeModel, scope_model.id, 'WRITE');
			assert.equal(profile.grantedScopeModelIdRights[scope_model.id].length, 1, 'There is 1 right assigned for the scope model in profile');
			assert.equal(profile.grantedScopeModelIdRights[scope_model.id][0], 'READ', 'First right assigned to scope model is "READ"');
			assert.notOk(profile.isAssignedRight(Entities.ScopeModel, scope_model.id, 'WRITE'), 'Right "WRITE" is no more assigned to profile');
			assert.ok(profile.isAssignedRight(Entities.ScopeModel, scope_model.id, 'READ'), 'Right "READ" is still assigned to profile');
		});
	});

	//TODO test assignation with node
	//profile.assignRightNode(profile_2, 'WRITE');
	//assert.ok(profile.isAssignedRight(Entities.Profile, 'CRA', 'WRITE'), 'Profile with id "CRA" has been assigned to profile with id "ADMIN"');
	//assert.ok(profile.profilesRight.hasOwnProperty('CRA'), 'Profile with id "ADMIN" contains profile with id "CRA"');

	bundle.end();
}
