import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {MailsService} from '@core/services/mails.service';
import {MailDTO} from '@core/model/mail-dto';
import {MatButton} from '@angular/material/button';
import {DownloadDirective} from '../../directives/download.component';
import {DomSanitizer} from '@angular/platform-browser';
import {MatTableModule} from '@angular/material/table';
import {DateTimeUTCPipe} from '../../pipes/date-time-utc.pipe';

@Component({
	selector: 'app-mail-detail',
	templateUrl: './mail-detail.component.html',
	styleUrls: ['./mail-detail.component.css'],
	imports: [
		MatDialogModule,
		DownloadDirective,
		MatButton,
		MatTableModule,
		DateTimeUTCPipe
	]
})
export class MailDetailComponent {
	trustedHtml: any;

	constructor(
		private mailsService: MailsService,
		@Inject(MAT_DIALOG_DATA) public mail: MailDTO,
		private domSanitizer: DomSanitizer
	) {
		this.trustedHtml = this.domSanitizer.bypassSecurityTrustHtml(mail.htmlBody || '');
	}

	getAttachmentUrl(mailPk: number, attPk: number): string {
		return this.mailsService.getAttachmentByPk(mailPk, attPk);
	}

	formatRecipients(recipients: Set<string>): string {
		return [...recipients].join(', ');
	}
}
