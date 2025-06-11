import {CRFService} from './crf.service';
import {DatasetService} from '@core/services/dataset.service';
import {TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import {DATASET_VISIT_DOCUMENTATION, CRF_DATASET_VISIT_DOCUMENTATION, FORM} from 'src/test/stubs';
import {FormService} from '@core/services/form.service';

describe('CRFService', () => {
	let service: CRFService;
	let datasetServiceSpy: jasmine.SpyObj<DatasetService>;

	beforeEach(() => {
		const spy = jasmine.createSpyObj('DatasetService', ['searchOnForm']);
		const spy2 = jasmine.createSpyObj('DatasetService', ['searchOnEvent']);

		TestBed.configureTestingModule({
			providers: [
				{provide: DatasetService, useValue: spy},
				{provide: FormService, useValue: spy2}
			]
		});

		service = TestBed.inject(CRFService);
		datasetServiceSpy = TestBed.inject(DatasetService) as jasmine.SpyObj<DatasetService>;
	});

	it('#getCRFDatasets works', () => {
		datasetServiceSpy.searchOnForm.and.returnValue(of([DATASET_VISIT_DOCUMENTATION]));

		service.getCRFDatasets(FORM).subscribe({
			next: datasets => expect(datasets).toContain(CRF_DATASET_VISIT_DOCUMENTATION),
			error: fail
		});
	});
});
