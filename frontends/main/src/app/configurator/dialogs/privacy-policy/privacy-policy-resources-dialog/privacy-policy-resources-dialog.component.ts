import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../../services/language.service';
import {PrivacyPolicy} from '@core/model/privacy-policy';
import {Profile} from '@core/model/profile';

export interface PrivacyPolicyResourcesDialogData {
	privacyPolicy: PrivacyPolicy;
	availableProfiles: Profile[];
}

@Component({
	selector: 'app-privacy-policy-resources-dialog',
	standalone: true,
	imports: [
		CommonModule,
		MatDialogModule,
		MatButtonModule,
		MatIconModule,
		MatSelectModule
	],
	templateUrl: './privacy-policy-resources-dialog.component.html',
	styleUrls: ['../../dialog-shared.css']
})
export class PrivacyPolicyResourcesDialogComponent implements OnInit {
	availableProfiles: Profile[] = [];
	selectedProfiles: Profile[] = [];

	constructor(
		public languageService: LanguageService,
		private dialogRef: MatDialogRef<PrivacyPolicyResourcesDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: PrivacyPolicyResourcesDialogData
	) {}

	ngOnInit(): void {
		this.initializeProfiles();
	}

	private initializeProfiles(): void {
		const selectedIds = this.data.privacyPolicy.profileIds || [];
		this.selectedProfiles = this.data.availableProfiles.filter(p => selectedIds.includes(p.profileId));
		this.availableProfiles = this.data.availableProfiles.filter(p => !selectedIds.includes(p.profileId));
	}

	onAddProfile(profile: Profile): void {
		this.availableProfiles = this.availableProfiles.filter(p => p.profileId !== profile.profileId);
		this.selectedProfiles = [...this.selectedProfiles, profile];
	}

	onRemoveProfile(profile: Profile): void {
		this.selectedProfiles = this.selectedProfiles.filter(p => p.profileId !== profile.profileId);
		this.availableProfiles = [...this.availableProfiles, profile];
	}

	onSave(): void {
		const result: any = {};

		const originalFormIds = [...(this.data.privacyPolicy.profileIds || [])].sort();
		const currentFormIds = [...this.selectedProfiles.map(p => p.profileId)].sort();
		if(JSON.stringify(originalFormIds) !== JSON.stringify(currentFormIds)) {
			result.profileIds = this.selectedProfiles.map(p => p.profileId);
		}

		this.dialogRef.close(result);
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}
}
