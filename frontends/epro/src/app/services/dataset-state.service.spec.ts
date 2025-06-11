import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { DatasetDTO } from '../api/model/dataset-dto';
import { DatasetService } from '../api/services/dataset.service';
import { DatasetStateService } from './dataset-state.service';

describe('DatasetStateService', () => {
	let datasetStateService: DatasetStateService;
	let datasetServiceSpy: jasmine.SpyObj<DatasetService>;

	beforeEach(() => {
		const spy = jasmine.createSpyObj('DatasetService', ['getDatasetsForEvent', 'save']);

		TestBed.configureTestingModule({
			providers: [DatasetStateService, { provide: DatasetService, useValue: spy }]
		});

		datasetStateService = TestBed.inject(DatasetStateService);
		datasetServiceSpy = TestBed.inject(DatasetService) as jasmine.SpyObj<DatasetService>;
	});

	it('#pullDatasets works', () => {
		const d1 = {
			pk: 1,
			fields: []
		} as unknown as DatasetDTO;

		datasetServiceSpy.getDatasetsForEvent.and.returnValue(of([d1]));

		datasetStateService.pullDatasets(1, [1]).subscribe(
			newDatasets => {
				expect(newDatasets).toHaveSize(1);
				expect(newDatasets[0]).toEqual(d1);
			},
			fail
		);
	});

	it('#pullDatasets repeatedly works', () => {
		const d1 = {
			pk: 1,
			eventPk: 1,
			fields: []
		} as unknown as DatasetDTO;

		const d2 = {
			pk: 2,
			eventPk: 1,
			fields: []
		} as unknown as DatasetDTO;

		datasetServiceSpy.getDatasetsForEvent.and.returnValue(of([d1]));

		datasetStateService.pullDatasets(1, [1]).subscribe(
			firstResult => {
				expect(firstResult).toHaveSize(1);
				expect(firstResult[0]).toEqual(d1);

				const firstCurrentDatasets = datasetStateService.getDatasetsForEvent(1);
				expect(firstCurrentDatasets).toHaveSize(1);
				expect(firstCurrentDatasets[0]).toEqual(d1);

				datasetServiceSpy.getDatasetsForEvent.and.returnValue(of([d1, d2]));

				datasetStateService.pullDatasets(1, [1]).subscribe(
					secondResult => {
						expect(secondResult).toHaveSize(2);
						expect(secondResult[0]).toEqual(d1);
						expect(secondResult[1]).toEqual(d2);

						const secondCurrentDatasets = datasetStateService.getDatasetsForEvent(1);
						expect(secondCurrentDatasets).toHaveSize(2);
						expect(secondCurrentDatasets[0]).toEqual(d1);
						expect(secondCurrentDatasets[1]).toEqual(d2);
					},
					fail
				);
			},
			fail
		);
	});

	it('#saveDataset works', () => {
		const d1 = {
			pk: 1,
			eventPk: 1,
			fields: []
		} as unknown as DatasetDTO;

		/*
		const d2 = {
			pk: 2,
			eventPk: 1
		} as Dataset;
		*/

		const d2Modified = {
			pk: 2,
			eventPk: 2,
			fields: []
		} as unknown as DatasetDTO;

		datasetServiceSpy.getDatasetsForEvent.and.returnValue(of([d1]));
		datasetServiceSpy.save.and.returnValue(of(d2Modified));

		datasetStateService.pullDatasets(1, [1]).pipe(
			switchMap(() => datasetStateService.saveDataset(d2Modified))
		).subscribe(
			() => {
				const event1Datasets = datasetStateService.getDatasetsForEvent(1);
				expect(event1Datasets).toHaveSize(1);
				expect(event1Datasets[0]).toEqual(d1);

				const event2Datasets = datasetStateService.getDatasetsForEvent(2);
				expect(event2Datasets).toHaveSize(1);
				expect(event2Datasets[0]).toEqual(d2Modified);
			},
			fail
		);

	});
});
