import {Component, Inject, signal} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {MailsService} from '@core/services/mails.service';
import {Mail} from '@core/model/mail';
import {MatButton} from '@angular/material/button';
import {DownloadDirective} from '../../directives/download.component';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
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
	readonly htmlBody = signal<SafeHtml>('');

	constructor(
		private mailsService: MailsService,
		@Inject(MAT_DIALOG_DATA) public mail: Mail,
		private domSanitizer: DomSanitizer
	) {
		this.htmlBody.set(this.domSanitizer.bypassSecurityTrustHtml(mail.htmlBody || ''));
	}

	getAttachmentUrl(mailPk: number, attPk: number): string {
		return this.mailsService.getAttachmentByPk(mailPk, attPk);
	}

	formatRecipients(recipients: Set<string>): string {
		return [...recipients].join(', ');
	}
}
