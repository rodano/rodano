import {Pipe, PipeTransform} from '@angular/core';
import {MailAttachmentDTO} from '@core/model/mail-attachment-dto';

@Pipe({
	name: 'yesNo'
})
export class YesNoPipe implements PipeTransform {
	transform(value: MailAttachmentDTO[]): string {
		return value.length ? 'Yes' : 'No';
	}
}
