import {Component, Inject, Input} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {EPROInvitationDTO} from '@core/model/epro-invitation-dto';
import {MatButton} from '@angular/material/button';
import {QRCodeComponent} from 'angularx-qrcode';
import {ProfileDTO} from '@core/model/profile-dto';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';

@Component({
	selector: 'app-epro-invitation',
	templateUrl: './epro-invitation.component.html',
	imports: [MatDialogModule, MatButton, QRCodeComponent, LocalizeMapPipe]
})
export class EproInvitationComponent {
	@Input() eproProfile: ProfileDTO;

	public qrCodeUrl: string;

	constructor(
		@Inject(MAT_DIALOG_DATA) public data: {invitation: EPROInvitationDTO; eproProfile: ProfileDTO}
	) {
		this.qrCodeUrl = `${data.invitation.url}/eproapp/login?code=${data.invitation.key}`;
	}
}
