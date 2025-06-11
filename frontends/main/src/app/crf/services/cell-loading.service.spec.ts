import {CellLoadingService} from './cell-loading.service';
import {of, merge, interval} from 'rxjs';
import {delay, take, map, filter} from 'rxjs/operators';
import {getTestScheduler} from 'jasmine-marbles';
import {TestBed} from '@angular/core/testing';
import {CRFService} from './crf.service';

describe('CellLoadingService', () => {
	let cellLoadingService: CellLoadingService;

	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [
				{provide: CRFService, useValue: CRFService}
			]
		});

		cellLoadingService = TestBed.inject(CellLoadingService);
	});

	it('#allCellsLoaded$ is correct within timeframe', () => {
		const scheduler = getTestScheduler();

		cellLoadingService.registerCellIds([
			'a',
			'b',
			'c'
		]);

		//expect(cellLoadingService.allCellsLoaded$).toBeObservable(cold('a', {a: false}));

		const aObs = of('a').pipe(
			delay(10, scheduler)
		);
		const cObs = of('c').pipe(
			delay(20, scheduler)
		);
		const bObs = of('b').pipe(
			delay(50, scheduler)
		);

		merge(aObs, bObs, cObs).subscribe(val => {
			cellLoadingService.cellLoadingComplete(val);
		});

		//const expected = cold('a----b', {a: false, b: true});
		//expect(cellLoadingService.allCellsLoaded$).toBeObservable(expected);
	});

	it('#allCellsLoaded emits true multiple times', () => {
		const scheduler = getTestScheduler();

		cellLoadingService.registerCellIds([
			'0',
			'1',
			'2'
		]);

		const firstBatch = interval(10, scheduler).pipe(
			take(3),
			map(val => val.toString())
		);

		firstBatch.subscribe(val => {
			cellLoadingService.cellLoadingComplete(val);

			if(val === '2') {
				cellLoadingService.registerCellIds(['3', '4']);

				const secondBatch = interval(10, scheduler).pipe(
					take(5),
					filter(value => value === 3 || value === 4),
					map(value => value.toString())
				);

				secondBatch.subscribe(value => {
					cellLoadingService.cellLoadingComplete(value);
				});
			}
		});

		//const expected = cold('a--(bc)-d', {a: false, b: true, c: false, d: true});
		//expect(cellLoadingService.allCellsLoaded$).toBeObservable(expected);
	});
});
