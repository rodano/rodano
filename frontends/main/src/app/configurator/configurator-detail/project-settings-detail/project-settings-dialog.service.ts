import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {
	BasicInfoDialogData,
	EditBasicInfoDialogComponent
} from '../../settings-dialog/edit-basic-info-dialog/edit-basic-info-dialog.component';
import {
	EditIntroductionTextDialogComponent, IntroductionTextDialogData
} from '../../settings-dialog/edit-introduction-text-dialog/edit-introduction-text-dialog.component';
import {
	EditEmailSettingsDialogComponent, EmailSettingsDialogData
} from '../../settings-dialog/edit-email-settings-dialog/edit-email-settings-dialog.component';
import {
	EditPasswordPoliciesDialogComponent, PasswordPoliciesDialogData
} from '../../settings-dialog/edit-password-policies-dialog/edit-password-policies-dialog.component';
import {
	EditEproSettingsDialogComponent, EproSettingsDialogData
} from '../../settings-dialog/edit-epro-settings-dialog/edit-epro-settings-dialog.component';
import {
	ClientInfoDialogData,
	EditClientInfoDialogComponent
} from '../../settings-dialog/edit-client-info-dialog/edit-client-info-dialog.component';
import {
	EditLanguagesDialogComponent, LanguagesDialogData
} from '../../settings-dialog/edit-languages-dialog/edit-languages-dialog.component';
import {
	EditRuleTagsDialogComponent, RuleTagsDialogData
} from '../../settings-dialog/edit-rule-tags-dialog/edit-rule-tags-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class ProjectSettingsDialogService {
	constructor(
		private dialog: MatDialog
	) {}

	openBasicInfoDialog(project: ConfiguratorProject): Observable<any> {
		const dialogRef = this.dialog.open(EditBasicInfoDialogComponent, {
			width: '500px',
			data: {
				code: project?.code,
				shortname: project?.shortname,
				longname: project?.longname,
				description: project?.description,
				url: project?.url,
				color: project?.color,
				languages: project?.languages || []
			} as BasicInfoDialogData
		});

		return dialogRef.afterClosed();
	}

	openIntroductionTextDialog(project: ConfiguratorProject): Observable<any> {
		const dialogRef = this.dialog.open(EditIntroductionTextDialogComponent, {
			width: '500px',
			data: {
				introductionText: project?.introductionText
			} as IntroductionTextDialogData
		});

		return dialogRef.afterClosed();
	}

	openEmailSettingsDialog(project: ConfiguratorProject): Observable<any> {
		const dialogRef = this.dialog.open(EditEmailSettingsDialogComponent, {
			width: '500px',
			data: {
				email: project?.email,
				smtpTls: project?.smtpTls
			} as EmailSettingsDialogData
		});

		return dialogRef.afterClosed();
	}

	openPasswordPoliciesDialog(project: ConfiguratorProject): Observable<any> {
		const dialogRef = this.dialog.open(EditPasswordPoliciesDialogComponent, {
			width: '500px',
			data: {
				passwordStrong: project?.passwordStrong,
				passwordLength: project?.passwordLength,
				passwordValidityDuration: project?.passwordValidityDuration,
				passwordUnique: project?.passwordUnique
			} as PasswordPoliciesDialogData
		});

		return dialogRef.afterClosed();
	}

	openEproSettingsDialog(project: ConfiguratorProject): Observable<any> {
		const dialogRef = this.dialog.open(EditEproSettingsDialogComponent, {
			width: '500px',
			data: {
				eproEnabled: project?.eproEnabled,
				eproProfileId: project?.eproProfileId,
				availableProfiles: [] //TODO: Fetch from service
			} as EproSettingsDialogData
		});

		return dialogRef.afterClosed();
	}

	openClientInfoDialog(project: ConfiguratorProject): Observable<any> {
		const dialogRef = this.dialog.open(EditClientInfoDialogComponent, {
			width: '500px',
			data: {
				clientName: project?.clientName,
				clientEmail: project?.clientEmail,
				protocolNo: project?.protocolNo,
				versionNumber: project?.versionNumber,
				versionDate: project?.versionDate
			} as ClientInfoDialogData
		});

		return dialogRef.afterClosed();
	}

	openLanguagesDialog(project: ConfiguratorProject): Observable<any> {
		const dialogRef = this.dialog.open(EditLanguagesDialogComponent, {
			width: '500px',
			data: {
				languages: project?.languages || []
			} as LanguagesDialogData
		});

		return dialogRef.afterClosed();
	}

	openRuleTagsDialog(project: ConfiguratorProject): Observable<any> {
		const dialogRef = this.dialog.open(EditRuleTagsDialogComponent, {
			width: '500px',
			data: {
				ruleTags: project?.ruleTags || []
			} as RuleTagsDialogData
		});

		return dialogRef.afterClosed();
	}
}
