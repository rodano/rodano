import {Component, Inject, input} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {EPROInvitation} from '@core/model/epro-invitation';
import {MatButton} from '@angular/material/button';
import {QRCodeComponent} from 'angularx-qrcode';
import {Profile} from '@core/model/profile';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';

@Component({
	selector: 'app-epro-invitation',
	templateUrl: './epro-invitation.component.html',
	imports: [MatDialogModule, MatButton, QRCodeComponent, LocalizeMapPipe]
})
export class EproInvitationComponent {
	readonly eproProfile = input.required<Profile>();

	public qrCodeUrl: string;

	constructor(
		@Inject(MAT_DIALOG_DATA) public data: {invitation: EPROInvitation; eproProfile: Profile}
	) {
		this.qrCodeUrl = `${data.invitation.url}/eproapp/login?code=${data.invitation.key}`;
	}
}
