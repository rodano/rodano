import {ScopeModel} from './scope_model.js';
import {EventGroup} from './event_group.js';
import {EventModel} from './event_model.js';

export default async function test(bundle, assert) {
	bundle.begin();

	await bundle.describe('EventGroup#getEventModels', async feature => {
		await feature.it('works properly', () => {
			const scope_model = new ScopeModel();
			const event_group = new EventGroup({
				id: 'TREATMENTS',
				shortname: {
					en: 'Treatments',
					fr: 'Traitements'
				}
			});
			event_group.scopeModel = scope_model;
			scope_model.eventGroups.push(event_group);
			const event = new EventModel({
				id: 'BASELINE',
				deadline: 0,
				deadlineUnit: 'DAYS'
			});
			event.scopeModel = scope_model;
			scope_model.eventModels.push(event);

			assert.equal(event_group.getEventModels().length, 0, 'There is no event for event group with id "TREATMENTS"');
			event.eventGroupId = 'TREATMENTS';
			assert.equal(event_group.getEventModels().length, 1, 'There is now 1 event for event group with id "TREATMENTS"');
			assert.equal(event_group.getEventModels()[0], event, 'First event for event group with id "TREATMENTS" is the current event');
		});
	});

	bundle.end();
}
