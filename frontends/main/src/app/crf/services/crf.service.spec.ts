import {beforeEach, describe, it, expect, vi, type MockedObject} from 'vitest';
import {CRFService} from './crf.service';
import {DatasetService} from '@core/services/dataset.service';
import {TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import {DATASET_VISIT_DOCUMENTATION, CRF_DATASET_VISIT_DOCUMENTATION, FORM} from 'src/test/stubs';

describe('CRFService', () => {
	let service: CRFService;
	let datasetServiceSpy: MockedObject<DatasetService>;

	beforeEach(() => {
		const datasetServiceMock = {
			searchOnForm: vi.fn().mockName('DatasetService.searchOnForm'),
			searchOnEvent: vi.fn().mockName('DatasetService.searchOnEvent')
		};

		TestBed.configureTestingModule({
			providers: [
				{provide: DatasetService, useValue: datasetServiceMock}
			]
		});

		service = TestBed.inject(CRFService);
		datasetServiceSpy = TestBed.inject(DatasetService) as MockedObject<DatasetService>;
	});

	it('#getCRFDatasets works', () => {
		datasetServiceSpy.searchOnForm.mockReturnValue(of([DATASET_VISIT_DOCUMENTATION]));

		service.getCRFDatasets(FORM).subscribe({
			next: datasets => {
				expect(datasets).toHaveLength(1);
				const {fields, ...properties} = CRF_DATASET_VISIT_DOCUMENTATION;
				expect(datasets[0]).toMatchObject(properties);
				expect(datasets[0].fields).toHaveLength(fields.length);
			}
		});
	});
});
