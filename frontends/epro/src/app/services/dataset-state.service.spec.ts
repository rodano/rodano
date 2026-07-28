import {beforeEach, describe, it, expect, vi, type MockedObject} from 'vitest';
import {TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {Dataset} from '@core/model/dataset';
import {DatasetService} from '@core/services/dataset.service';
import {DatasetStateService} from './dataset-state.service';

describe('DatasetStateService', () => {
	let datasetStateService: DatasetStateService;
	let datasetServiceSpy: MockedObject<DatasetService>;

	beforeEach(() => {
		const datasetServiceMock = {
			searchOnEvent: vi.fn().mockName('DatasetService.searchOnEvent'),
			searchOnScope: vi.fn().mockName('DatasetService.searchOnScope'),
			saveForEvent: vi.fn().mockName('DatasetService.saveForEvent'),
			saveForScope: vi.fn().mockName('DatasetService.saveForScope')
		};

		TestBed.configureTestingModule({
			providers: [DatasetStateService, {provide: DatasetService, useValue: datasetServiceMock}]
		});

		datasetStateService = TestBed.inject(DatasetStateService);
		datasetServiceSpy = TestBed.inject(DatasetService) as MockedObject<DatasetService>;
	});

	it('#pullDatasets works', () => {
		const d1 = {
			pk: 1,
			fields: []
		} as unknown as Dataset;

		datasetServiceSpy.searchOnEvent.mockReturnValue(of([d1]));

		datasetStateService.pullDatasets(1, [1]).subscribe(newDatasets => {
			expect(newDatasets).toHaveLength(1);
			expect(newDatasets[0]).toEqual(d1);
		});
	});

	it('#pullDatasets repeatedly works', () => {
		const d1 = {
			pk: 1,
			eventPk: 1,
			fields: []
		} as unknown as Dataset;

		const d2 = {
			pk: 2,
			eventPk: 1,
			fields: []
		} as unknown as Dataset;

		datasetServiceSpy.searchOnEvent.mockReturnValue(of([d1]));

		datasetStateService.pullDatasets(1, [1]).subscribe(firstResult => {
			expect(firstResult).toHaveLength(1);
			expect(firstResult[0]).toEqual(d1);

			const firstCurrentDatasets = datasetStateService.getDatasetsForEvent(1);
			expect(firstCurrentDatasets).toHaveLength(1);
			expect(firstCurrentDatasets[0]).toEqual(d1);

			datasetServiceSpy.searchOnEvent.mockReturnValue(of([d1, d2]));

			datasetStateService.pullDatasets(1, [1]).subscribe(secondResult => {
				expect(secondResult).toHaveLength(2);
				expect(secondResult[0]).toEqual(d1);
				expect(secondResult[1]).toEqual(d2);

				const secondCurrentDatasets = datasetStateService.getDatasetsForEvent(1);
				expect(secondCurrentDatasets).toHaveLength(2);
				expect(secondCurrentDatasets[0]).toEqual(d1);
				expect(secondCurrentDatasets[1]).toEqual(d2);
			});
		});
	});

	it('#saveDataset works', () => {
		const d1 = {
			pk: 1,
			eventPk: 1,
			fields: []
		} as unknown as Dataset;

		const d2Modified = {
			pk: 2,
			eventPk: 2,
			fields: []
		} as unknown as Dataset;

		datasetServiceSpy.searchOnEvent.mockReturnValue(of([d1]));
		datasetServiceSpy.saveForEvent.mockReturnValue(of(d2Modified));

		datasetStateService.pullDatasets(1, [1]).pipe(
			switchMap(() => datasetStateService.saveDataset(d2Modified))
		).subscribe(() => {
			const event1Datasets = datasetStateService.getDatasetsForEvent(1);
			expect(event1Datasets).toHaveLength(1);
			expect(event1Datasets[0]).toEqual(d1);

			const event2Datasets = datasetStateService.getDatasetsForEvent(2);
			expect(event2Datasets).toHaveLength(1);
			expect(event2Datasets[0]).toEqual(d2Modified);
		});
	});
});
