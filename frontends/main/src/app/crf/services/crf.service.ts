import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Dataset} from '@core/model/dataset';
import {map} from 'rxjs/operators';
import {DatasetService} from '@core/services/dataset.service';
import {CRFDataset} from '../models/crf-dataset';
import {Form} from '@core/model/form';
import {Layout} from '@core/model/layout';
import {Cell} from '@core/model/cell';
import {DatasetUpdate} from '@core/model/dataset-update';
import {FormService} from '@core/services/form.service';
import {DatasetSubmission} from '@core/model/dataset-submission';
import {DatasetRestoration} from '@core/model/dataset-restoration';
import {DatasetCreation} from '@core/model/dataset-creation';
import {FieldUpdate} from '@core/model/field-update';
import {FieldModel} from '@core/model/field-model';
import {Field} from '@core/model/field';
import {CRFField} from '../models/crf-field';
import {OperandType} from '@core/model/operand-type';
import {FieldModelType} from '@core/model/field-model-type';
import {LocalizeMapPipe} from 'src/app/pipes/localize-map.pipe';

@Injectable({
	providedIn: 'root'
})
export class CRFService {
	constructor(
		private datasetService: DatasetService,
		private formService: FormService
	) { }

	getLayoutsCells(layouts: Layout[]): Cell[] {
		return layouts.flatMap(l => this.getLayoutCells(l));
	}

	getLayoutCells(layout: Layout): Cell[] {
		return layout.lines
			.flatMap(c => c.cells);
	}

	createCRFDataset(dataset: Dataset): CRFDataset {
		return {
			...dataset,
			fields: dataset.fields.map(f => ({...f, shown: true, error: undefined})),
			show: true,
			expanded: false,
			rationale: undefined
		} satisfies CRFDataset as CRFDataset;
	}

	createCRFDatasets(datasets: Dataset[]): CRFDataset[] {
		return datasets.map(d => this.createCRFDataset(d));
	}

	getCandidateCRFDataset(scopePk: number, eventPk: number | undefined, datasetModelId: string): Observable<CRFDataset> {
		return this.datasetService.getCandidate(scopePk, eventPk, datasetModelId).pipe(
			map(d => this.createCRFDataset(d))
		);
	}

	/**
	 * Pull datasets for a form, keeping only displayed values
	 * @param form A form
	 * @param layouts The layout of the forms
	 */
	getCRFDatasets(form: Form): Observable<CRFDataset[]> {
		return this.datasetService.searchOnForm(form.scopePk, form.eventPk, form.pk).pipe(
			map(d => this.createCRFDatasets(d))
		);
	}

	/**
	 * Transform datasets into a submission object and push it to the server
	 * @param form A form
	 * @param layouts The layout of the form, required to determine which fields are displayed and which are not
	 * @param datasets A list of CRFDatasets
	 */
	saveCRFDatasets(form: Form, layouts: Layout[], crfDatasets: CRFDataset[]): Observable<Dataset[]> {
		const updatedDatasets = [] as DatasetUpdate[];
		const newDatasets = [] as DatasetCreation[];
		const removedDatasets = {} as Record<number, string>;
		const restoredDatasets = [] as DatasetRestoration[];
		const formCells = this.getLayoutsCells(layouts).filter(c => this.getCellHasField(c));
		crfDatasets
			//remove read only data
			.filter(d => d.canWrite)
			.forEach(dataset => {
				//remove fields that are not part of the form
				dataset.fields = dataset.fields.filter(f =>
					formCells.some(c => c.datasetModelId === dataset.modelId && c.fieldModelId === f.modelId)
				);

				//mark hidden datasets as removed
				//do not try to do this in the multiple layout component
				//dataset may be shown/hidden multiple times before being submitted and only the final state count
				if(!dataset.show && dataset.pk) {
					dataset.removed = true;
					dataset.rationale = 'Reset by a visibility criterion';
				}
				//handled manually removed datasets
				if(dataset.removed) {
					removedDatasets[dataset.pk] = dataset.rationale ?? '';
				}
				else {
					//at this point, the content of the dataset is important, and fields must be transformed
					const updatedFields = dataset.fields.map(field => {
						const updatedField = {
							pk: field.pk,
							modelId: field.modelId,
							value: field.value,
							filePk: field.filePk
						} as FieldUpdate;
						//mark hidden fields as reset
						if(!field.shown) {
							updatedField.reset = true;
							updatedField.rationale = 'Reset by a visibility criterion';
						}
						return updatedField;
					});
					//handle new datasets
					if(!dataset.pk) {
						const newDataset = {
							id: dataset.id,
							modelId: dataset.modelId,
							fields: updatedFields
						} as DatasetCreation;
						newDatasets.push(newDataset);
					}
					else {
						//at this point, we deal with existing datasets
						const updateDataset = {
							pk: dataset.pk,
							fields: updatedFields
						} as DatasetUpdate;
						//handled restored datasets
						if(dataset.rationale) {
							restoredDatasets.push({rationale: dataset.rationale, dataset: updateDataset});
						}
						//handle updated datasets
						else {
							updatedDatasets.push(updateDataset);
						}
					}
				}
			});

		const submission = {
			updatedDatasets,
			newDatasets,
			removedDatasets,
			restoredDatasets
		} as DatasetSubmission;

		return this.formService.submit(form.scopePk, form.eventPk, form.pk, submission);
	}

	//transform a field value into a typed value
	public typeFieldValue(fieldModel: FieldModel, value?: string): any {
		switch(fieldModel?.dataType) {
			case OperandType.BOOLEAN:
				return value ? value === 'true' : undefined;
			case OperandType.NUMBER.toLowerCase():
				return value ? parseFloat(value) : undefined;
			default:
				return value;
		}
	}

	//transform field values into a list of typed values
	//remember that the value of a field may be an array (for example, checkbox groups)
	public typeFieldValues(fieldModel: FieldModel, values: string[]): any[] {
		return values.map(v => this.typeFieldValue(fieldModel, v));
	}

	public parseFieldValue(field: Field): any[] {
		const value = field.value as string;
		let values;
		switch(field.model.type) {
			case FieldModelType.CHECKBOX_GROUP:
				values = value?.split(',') ?? [];
				break;
			default:
				values = [value];
		}
		return this.typeFieldValues(field.model, values);
	}

	//transform a typed field value into a string value
	//for example, this is used to transform a date value into a string value
	public buildFieldValue(fieldModel: FieldModel, value?: any): string {
		switch(fieldModel?.dataType) {
			case OperandType.BOOLEAN:
			case OperandType.NUMBER:
				return value?.toString() ?? '';
			default:
				return value ?? '';
		}
	}

	//transform a typed field value into a value label
	public buildFieldValueLabel(fieldModel: FieldModel, value?: any): string {
		const stringValue = this.buildFieldValue(fieldModel, value);
		switch(fieldModel.type) {
			case FieldModelType.SELECT:
			case FieldModelType.RADIO: {
				const possibleValue = fieldModel.possibleValues.find(v => v.id === stringValue);
				return possibleValue ? new LocalizeMapPipe().transform(possibleValue.shortname) : stringValue;
			}
			default:
				return stringValue;
		}
	}

	getLayoutFields(layout: Layout, datasets: CRFDataset[]): CRFField[] {
		const cells = this.getLayoutCells(layout);
		return cells
			.filter(c => this.getCellHasField(c))
			.map(c => this.getCellField(c, datasets));
	}

	getCellHasField(cell: Cell) {
		return cell.datasetModelId && cell.fieldModelId;
	}

	getCellDataset(cell: Cell, datasets: CRFDataset[]): CRFDataset {
		if(!this.getCellHasField(cell)) {
			throw new Error(`Cell ${cell.id} does not contain a field`);
		}
		return datasets.find(d => d.modelId === cell.datasetModelId) as CRFDataset;
	}

	getCellField(cell: Cell, datasets: CRFDataset[]): CRFField {
		if(!this.getCellHasField(cell)) {
			throw new Error(`Cell ${cell.id} does not contain a field`);
		}
		return this.getCellDataset(cell, datasets)?.fields.find(f => f.modelId === cell.fieldModelId) as CRFField;
	}
}
