import {Observable, of} from 'rxjs';
import {reviveObjectDates} from './revive-dates-helper';
import {reviveDates} from './revive-dates.decorator';

const SAMPLE = {
	pk: 12,
	id: 'TEST',
	code: '2024-01-03T',
	creationTime: '2024-02-02T16:42:00.606Z',
	lastUpdateTime: '2024-03-09T18:20:55.606Z',
	model: {
		id: 'MODEL',
		creationTime: '2020-01-01T10:00:05.000Z'
	},
	events: [
		{
			pk: 46,
			id: 'TEST_EVENT',
			date: '2024-08-04T06:00:00Z'
		}
	]
};

class SampleClass {
	@reviveDates
	public getSample(): Observable<any> {
		return of(SAMPLE);
	}
}

describe('ReviveObject', () => {
	it('should revive dates', () => {
		const object = Object.assign({}, SAMPLE);
		reviveObjectDates(object);
		expect(object.pk).toBe(12);
		expect(object.code).toBe('2024-01-03T');
		expect(object.creationTime).toBeInstanceOf(Date);
		expect((object.creationTime as unknown as Date).getUTCHours()).toBe(16);
		expect(object.lastUpdateTime).toBeInstanceOf(Date);
		expect((object.lastUpdateTime as unknown as Date).getUTCMinutes()).toBe(20);

		const model = object.model;
		expect(model.id).toBe('MODEL');
		expect(model.creationTime).toBeInstanceOf(Date);
		expect((model.creationTime as unknown as Date).getUTCSeconds()).toBe(5);

		const event = object.events[0];
		expect(event.pk).toBe(46);
		expect(event.date).toBeInstanceOf(Date);
		expect((event.date as unknown as Date).getUTCDate()).toBe(4);
	});
});

describe('ReviveObjectDecorator', () => {
	it('should revive dates', () => {
		new SampleClass().getSample().subscribe((object: any) => {
			expect(object.pk).toBe(12);
			expect(object.code).toBe('2024-01-03T');
			expect(object.creationTime).toBeInstanceOf(Date);
			expect((object.creationTime as unknown as Date).getUTCHours()).toBe(16);
		});
	});
});
