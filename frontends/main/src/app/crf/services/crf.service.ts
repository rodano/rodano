import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {DatasetDTO} from '@core/model/dataset-dto';
import {map} from 'rxjs/operators';
import {DatasetService} from '@core/services/dataset.service';
import {CRFDataset} from '../models/crf-dataset';
import {FormDTO} from '@core/model/form-dto';
import {LayoutDTO} from '@core/model/layout-dto';
import {CellDTO} from '@core/model/cell-dto';
import {DatasetUpdateDTO} from '@core/model/dataset-update-dto';
import {FormService} from '@core/services/form.service';
import {DatasetSubmissionDTO} from '@core/model/dataset-submission-dto';
import {DatasetRestorationDTO} from '@core/model/dataset-restoration-dto';
import {DatasetCreationDTO} from '@core/model/dataset-creation-dto';
import {FieldUpdateDTO} from '@core/model/field-update-dto';
import {FieldModelDTO} from '@core/model/field-model-dto';
import {FieldDTO} from '@core/model/field-dto';
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

	getLayoutsCells(layouts: LayoutDTO[]): CellDTO[] {
		return layouts.flatMap(l => this.getLayoutCells(l));
	}

	getLayoutCells(layout: LayoutDTO): CellDTO[] {
		return layout.lines
			.flatMap(c => c.cells);
	}

	createCRFDataset(dataset: DatasetDTO): CRFDataset {
		return {
			...dataset,
			fields: dataset.fields.map(f => ({...f, shown: true, error: undefined})),
			show: true,
			expanded: false,
			rationale: undefined
		} satisfies CRFDataset as CRFDataset;
	}

	createCRFDatasets(datasets: DatasetDTO[]): CRFDataset[] {
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
	getCRFDatasets(form: FormDTO): Observable<CRFDataset[]> {
		return this.datasetService.searchOnForm(form.scopePk, form.eventPk, form.pk).pipe(
			map(d => this.createCRFDatasets(d))
		);
	}

	/**
	 * Transform datasets into a submission DTO and push it to the server
	 * @param form A form
	 * @param datasets A list of CRFDatasets
	 */
	saveCRFDatasets(form: FormDTO, crfDatasets: CRFDataset[]): Observable<DatasetDTO[]> {
		const updatedDatasets = [] as DatasetUpdateDTO[];
		const newDatasets = [] as DatasetCreationDTO[];
		const removedDatasets = {} as Record<number, string>;
		const restoredDatasets = [] as DatasetRestorationDTO[];
		crfDatasets
			//remove read only data
			.filter(d => d.canWrite)
			.forEach(dataset => {
				//mark hidden datasets as removed
				//do no try to do this in the multiple layout component
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
						} as FieldUpdateDTO;
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
						} as DatasetCreationDTO;
						newDatasets.push(newDataset);
					}
					else {
						//at this point, we deal with existing datasets
						const updateDataset = {
							pk: dataset.pk,
							fields: updatedFields
						} as DatasetUpdateDTO;
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

		const submissionDTO = {
			updatedDatasets,
			newDatasets,
			removedDatasets,
			restoredDatasets
		} as DatasetSubmissionDTO;

		return this.formService.submit(form.scopePk, form.eventPk, form.pk, submissionDTO);
	}

	//transform a field value into a typed value
	public typeFieldValue(fieldModel: FieldModelDTO, value?: string): any {
		switch(fieldModel?.dataType) {
			case OperandType.BOOLEAN:
				return value === 'true';
			case OperandType.NUMBER.toLowerCase():
				return value ? parseFloat(value) : undefined;
			default:
				return value;
		}
	}

	//transform field values into a list of typed values
	//remember that the value of a field may be an array (for example, checkbox groups)
	public typeFieldValues(fieldModel: FieldModelDTO, values: string[]): any[] {
		return values.map(v => this.typeFieldValue(fieldModel, v));
	}

	public parseFieldValue(field: FieldDTO): any[] {
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
	public buildFieldValue(fieldModel: FieldModelDTO, value?: any): string {
		switch(fieldModel?.dataType) {
			case OperandType.BOOLEAN:
			case OperandType.NUMBER:
				return value?.toString() ?? '';
			default:
				return value ?? '';
		}
	}

	//transform a typed field value into a value label
	public buildFieldValueLabel(fieldModel: FieldModelDTO, value?: any): string {
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

	getLayoutFields(layout: LayoutDTO, datasets: CRFDataset[]): CRFField[] {
		const cells = this.getLayoutCells(layout);
		return cells
			.filter(c => this.getCellHasField(c))
			.map(c => this.getCellField(c, datasets));
	}

	getCellHasField(cell: CellDTO) {
		return cell.datasetModelId && cell.fieldModelId;
	}

	getCellDataset(cell: CellDTO, datasets: CRFDataset[]): CRFDataset {
		if(!this.getCellHasField(cell)) {
			throw new Error(`Cell ${cell.id} does not contain a field`);
		}
		return datasets.find(d => d.modelId === cell.datasetModelId) as CRFDataset;
	}

	getCellField(cell: CellDTO, datasets: CRFDataset[]): CRFField {
		if(!this.getCellHasField(cell)) {
			throw new Error(`Cell ${cell.id} does not contain a field`);
		}
		return this.getCellDataset(cell, datasets)?.fields.find(f => f.modelId === cell.fieldModelId) as CRFField;
	}
}
