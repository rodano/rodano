import {inject, Service} from '@angular/core';
import {filter, Observable, Subject} from 'rxjs';
import {CRFField} from '../models/crf-field';
import {LoggingService} from '@core/services/logging.service';

@Service()
export class FieldUpdateService {
	private readonly loggingService = inject(LoggingService);

	private readonly fieldUpdatedStream$ = new Subject<CRFField>();

	public readonly fieldUpdated$ = this.fieldUpdatedStream$.asObservable();

	public updateField(field: CRFField, value: string, valueLabel: string): void {
		if(field.value !== value) {
			this.loggingService.info(`Updating ${field.modelId} from ${field.value} to ${value}`);
			field.value = value;
			field.valueLabel = valueLabel;
			this.fieldUpdatedStream$.next(field);
		}
	}

	public cellFieldUpdated$(field: CRFField): Observable<CRFField> {
		return this.fieldUpdatedStream$.pipe(
			filter(f => f.pk === field.pk)
		);
	}

	public datasetFieldUpdated$(datasetModelId: string): Observable<CRFField> {
		return this.fieldUpdatedStream$.pipe(
			filter(f => f.model.datasetModelId === datasetModelId)
		);
	}
}
