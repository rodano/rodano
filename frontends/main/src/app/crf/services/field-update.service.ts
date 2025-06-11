import {Injectable} from '@angular/core';
import {filter, Observable, Subject} from 'rxjs';
import {CRFField} from '../models/crf-field';
import {LoggingService} from '@core/services/logging.service';

@Injectable({
	providedIn: 'root'
})
export class FieldUpdateService {
	public constructor(private loggingService: LoggingService) { }

	private readonly fieldUpdatedStream$ = new Subject<CRFField>();

	public readonly fieldUpdated$ = this.fieldUpdatedStream$.asObservable();

	public updateField(field: CRFField, value: string, valueLabel: string): void {
		if(field.value !== value) {
			this.loggingService.info(`Updating ${field.modelId} from ${value} to ${field.value}`);
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
