import {Pipe, PipeTransform} from '@angular/core';
import {MailAttachment} from '@core/model/mail-attachment';

@Pipe({
	name: 'yesNo'
})
export class YesNoPipe implements PipeTransform {
	transform(value: MailAttachment[]): string {
		return value.length ? 'Yes' : 'No';
	}
}
