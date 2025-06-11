import {Pipe, PipeTransform} from '@angular/core';
import {FieldModelDTO} from '@core/model/field-model-dto';

@Pipe({
	name: 'localizeFieldModel'
})
export class LocalizeFieldModelPipe implements PipeTransform {
	transform(fieldModel: FieldModelDTO): string {
		let label = fieldModel.shortname['en'];
		if(fieldModel.required) {
			label += ' *';
		}
		return label;
	}
}
