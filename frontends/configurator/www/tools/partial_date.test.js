import {PartialDate} from './partial_date.js';

export default async function test(bundle, assert) {
	bundle.begin();

	await bundle.describe('PartialDate#constructor', async feature => {
		await feature.it('can be created with a string well formatted', () => {
			const date = new PartialDate('12.02.2002');
			assert.equal(date.day, 12, 'Partial date has good day');
			assert.equal(date.month, 2, 'Partial date has good month');
			assert.equal(date.month, 2, 'Partial date has good month');
		});

		await feature.it('can be created with an array of strings', () => {
			const date = new PartialDate(['12', '02', '2002']);
			assert.equal(date.day, 12, 'Partial date has good day');
			assert.equal(date.month, 2, 'Partial date has good month');
			assert.equal(date.year, 2002, 'Partial date has good year');
		});

		await feature.it('can be created with an array of integers', () => {
			const date = new PartialDate([12, 2, 2002]);
			assert.equal(date.day, 12, 'Partial date has good day');
			assert.equal(date.month, 2, 'Partial date has good month');
			assert.equal(date.year, 2002, 'Partial date has good year');
		});

		await feature.it('must have 3 parts', () => {
			assert.doesThrow(
				() => new PartialDate('12.02'),
				e => e.message === 'Partial date must have 3 parts',
				'Date must have 3 parts'
			);

			assert.doesThrow(
				() => new PartialDate('12.02.'),
				e => e.message === 'Partial date must have 3 not empty parts',
				'Date must have 3 parts'
			);

			assert.doesThrow(
				() => new PartialDate([]),
				e => e.message === 'Partial date must have 3 parts',
				'Date must have 3 parts'
			);
		});

		await feature.it('must be consistent', () => {
			assert.doesThrow(
				() => new PartialDate('unknown.02.unknown'),
				e => e.message === 'Year cannot be unknown if month is known',
				'Date cannot have a month if year is unknown'
			);

			assert.doesThrow(
				() => new PartialDate('12.02.unknown'),
				e => e.message === 'Year cannot be unknown if month is known',
				'Date cannot have a day and month if year is unknown'
			);

			assert.doesThrow(
				() => new PartialDate('12.unknown.unknown'),
				e => e.message === 'Month cannot be unknown if day is known',
				'Date cannot have a day if month and year are unknown'
			);

			assert.doesNotThrow(
				() => new PartialDate('unknown.02.2002'),
				'Date can have an unknown day if month and year are'
			);
		});

		await feature.it('handles fully unknown dates properly', () => {
			assert.undefined(new PartialDate('unknown.unknown.unknown').day, 'Fully undefined date has undefined day');
			assert.undefined(new PartialDate('unknown.unknown.unknown').month, 'Fully undefined date has undefined month');
			assert.undefined(new PartialDate('unknown.unknown.unknown').year, 'Fully undefined date has undefined year');

			assert.ok(new PartialDate('unknown.unknown.unknown').isDayUnknown(), 'Fully undefined date has unknown day');
			assert.ok(new PartialDate('unknown.unknown.unknown').isMonthUnknown(), 'Fully undefined date has unknown month');
			assert.ok(new PartialDate('unknown.unknown.unknown').isYearUnknown(), 'Fully undefined date has unknown year');

			assert.undefined(new PartialDate('toto.titi.tutu').day, 'Fully undefined date has undefined day');
			assert.undefined(new PartialDate('toto.titi.tutu').month, 'Fully undefined date has undefined month');
			assert.undefined(new PartialDate('toto.titi.tutu').year, 'Fully undefined date has undefined year');

			assert.ok(new PartialDate('toto.titi.tutu').isDayUnknown(), 'Fully undefined date has unknown day');
			assert.ok(new PartialDate('toto.titi.tutu').isMonthUnknown(), 'Fully undefined date has unknown month');
			assert.ok(new PartialDate('toto.titi.tutu').isYearUnknown(), 'Fully undefined date has unknown year');

			assert.undefined(new PartialDate([undefined, undefined, undefined]).day, 'Fully undefined date has undefined day');
			assert.undefined(new PartialDate([undefined, undefined, undefined]).month, 'Fully undefined date has undefined month');
			assert.undefined(new PartialDate([undefined, undefined, undefined]).year, 'Fully undefined date has undefined year');

			assert.ok(new PartialDate([undefined, undefined, undefined]).isDayUnknown(), 'Fully undefined date has unknown day');
			assert.ok(new PartialDate([undefined, undefined, undefined]).isMonthUnknown(), 'Fully undefined date has unknown month');
			assert.ok(new PartialDate([undefined, undefined, undefined]).isYearUnknown(), 'Fully undefined date has unknown year');
		});
	});

	await bundle.describe('PartialDate#isBefore and PartialDate#isAfter', async feature => {
		await feature.it('compares with another partial date', () => {
			//two dates with unknown day, same year but different month can be compared'
			const date_1 = new PartialDate(['unknown', 10, 2005]);
			const date_2 = new PartialDate(['unknown', 12, 2005]);
			assert.ok(date_1.isBefore(date_2));
			assert.ok(date_2.isAfter(date_1));
		});

		await feature.it('can be equal to an other partial date', () => {
			//two dates with unknown day, same year and same month are equals'
			const date_1 = new PartialDate(['unknown', 10, 2005]);
			const date_2 = new PartialDate(['unknown', 10, 2005]);
			assert.notOk(date_1.isBefore(date_2));
			assert.notOk(date_2.isAfter(date_1));
		});
	});

	bundle.end();
}
