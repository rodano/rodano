import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {
	BasicInfoDialogData,
	ProjectSettingsBasicInfoDialogComponent
} from '../../dialogs/project-settings/project-settings-basic-info-dialog/project-settings-basic-info-dialog.component';
import {
	ProjectSettingsIntroTextDialogComponent, IntroductionTextDialogData
} from '../../dialogs/project-settings/project-settings-intro-text-dialog/project-settings-intro-text-dialog.component';
import {
	ProjectSettingsEmailDialogComponent, EmailSettingsDialogData
} from '../../dialogs/project-settings/project-settings-email-dialog/project-settings-email-dialog.component';
import {
	ProjectSettingsPasswordPoliciesDialogComponent, PasswordPoliciesDialogData
} from '../../dialogs/project-settings/project-settings-password-policies-dialog/project-settings-password-policies-dialog.component';
import {
	ProjectSettingsEproDialogComponent, EproSettingsDialogData
} from '../../dialogs/project-settings/project-settings-epro-dialog/project-settings-epro-dialog.component';
import {
	ClientInfoDialogData,
	ProjectSettingsClientInfoDialogComponent
} from '../../dialogs/project-settings/project-settings-client-info-dialog/project-settings-client-info-dialog.component';
import {
	ProjectSettingsLanguagesDialogComponent, LanguagesDialogData
} from '../../dialogs/project-settings/project-settings-languages-dialog/project-settings-languages-dialog.component';
import {
	ProjectSettingsRuleTagsDialogComponent, RuleTagsDialogData
} from '../../dialogs/project-settings/project-settings-rule-tags-dialog/project-settings-rule-tags-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class ProjectSettingsDialogService {
	constructor(
		private dialog: MatDialog
	) {}

	openBasicInfoDialog(project: ConfiguratorProject): Observable<any> {
		const dialogRef = this.dialog.open(ProjectSettingsBasicInfoDialogComponent, {
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
		const dialogRef = this.dialog.open(ProjectSettingsIntroTextDialogComponent, {
			width: '500px',
			data: {
				introductionText: project?.introductionText
			} as IntroductionTextDialogData
		});

		return dialogRef.afterClosed();
	}

	openEmailSettingsDialog(project: ConfiguratorProject): Observable<any> {
		const dialogRef = this.dialog.open(ProjectSettingsEmailDialogComponent, {
			width: '500px',
			data: {
				email: project?.email,
				smtpTls: project?.smtpTls
			} as EmailSettingsDialogData
		});

		return dialogRef.afterClosed();
	}

	openPasswordPoliciesDialog(project: ConfiguratorProject): Observable<any> {
		const dialogRef = this.dialog.open(ProjectSettingsPasswordPoliciesDialogComponent, {
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
		const dialogRef = this.dialog.open(ProjectSettingsEproDialogComponent, {
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
		const dialogRef = this.dialog.open(ProjectSettingsClientInfoDialogComponent, {
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
		const dialogRef = this.dialog.open(ProjectSettingsLanguagesDialogComponent, {
			width: '500px',
			data: {
				languages: project?.languages || []
			} as LanguagesDialogData
		});

		return dialogRef.afterClosed();
	}

	openRuleTagsDialog(project: ConfiguratorProject): Observable<any> {
		const dialogRef = this.dialog.open(ProjectSettingsRuleTagsDialogComponent, {
			width: '500px',
			data: {
				ruleTags: project?.ruleTags || []
			} as RuleTagsDialogData
		});

		return dialogRef.afterClosed();
	}
}
