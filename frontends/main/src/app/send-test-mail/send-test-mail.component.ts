import {Component, OnInit} from '@angular/core';
import {Validators, ReactiveFormsModule, FormGroup, FormControl} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MailsService} from '@core/services/mails.service';
import {NotificationService} from '../services/notification.service';
import {MailCreationDTO} from '@core/model/mail-creation-dto';
import {MeService} from '@core/services/me.service';
import {MailStatus} from '@core/model/mail-status';

@Component({
	templateUrl: './send-test-mail.component.html',
	styleUrls: ['./send-test-mail.component.css'],
	imports: [
		ReactiveFormsModule,
		MatLabel,
		MatFormField,
		MatInput,
		MatButton
	]
})
export class SendTestMailComponent implements OnInit {
	error?: string = undefined;
	mailForm = new FormGroup({
		recipient: new FormControl('', [Validators.required]),
		subject: new FormControl('Test email', [Validators.required]),
		body: new FormControl('This is a test email. If you received this, it\'s working!', [Validators.required])
	});

	constructor(
		private meService: MeService,
		private mailService: MailsService,
		private notificationService: NotificationService
	) { }

	ngOnInit() {
		this.meService.get().subscribe(user => this.mailForm.controls.recipient.setValue(user.email));
	}

	sendMail() {
		this.error = undefined;
		const mail = this.mailForm.value as MailCreationDTO;
		this.mailService.send(mail).subscribe({
			next: mail => {
				if(mail.status === MailStatus.FAILED) {
					this.error = `Unable to send email: ${mail.error}`;
				}
				else {
					this.notificationService.showSuccess('Email sent');
				}
			},
			error: response => this.notificationService.showError(response.error.message)
		});
	}
}
