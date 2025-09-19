import {Pipe, PipeTransform} from '@angular/core';
import {FieldModel} from '@core/model/field-model';

@Pipe({
	name: 'localizeFieldModel'
})
export class LocalizeFieldModelPipe implements PipeTransform {
	transform(fieldModel: FieldModel): string {
		let label = fieldModel.shortname['en'];
		if(fieldModel.required) {
			label += ' *';
		}
		return label;
	}
}
