import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {Subscription} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {ProjectLanguage} from '@core/model/project-language';
import {PrivacyPolicy} from '@core/model/privacy-policy';
import {PrivacyPolicyManagerService} from '../../services/manager/privacy-policy-manager.service';
import {PrivacyPolicyDialogService} from '../../services/dialogs/privacy-policy-dialog.service';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';

@Component({
	selector: 'app-privacy-policy-detail',
	standalone: true,
	templateUrl: './privacy-policy-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent]
})
export class PrivacyPolicyDetailComponent implements OnInit, OnDestroy {
	@Input() privacyPolicy!: PrivacyPolicy;
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() allPrivacyPolicies: PrivacyPolicy[] = [];
	@Output() privacyPolicyUpdated = new EventEmitter<PrivacyPolicy>();
	@Output() privacyPolicyDeleted = new EventEmitter<string>();
	@Output() closed = new EventEmitter<void>();

	selectedLanguage = '';
	projectLanguages: ProjectLanguage[];
	private languageSubscription: Subscription;

	constructor(
		public privacyPolicyManager: PrivacyPolicyManagerService,
		public languageService: LanguageService,
		private privacyPolicyDialogService: PrivacyPolicyDialogService,
		private dialog: MatDialog,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	onEditBasicInfo(): void {
		this.privacyPolicyDialogService.openBasicInfoDialog(
			this.projectId,
			this.privacyPolicy,
			this.projectLanguages
		).subscribe((result: any) => {
			if(result) {
				const updatedPrivacyPolicy: PrivacyPolicy = {...this.privacyPolicy, ...result};
				this.privacyPolicyManager.update(updatedPrivacyPolicy);
				this.privacyPolicyUpdated.emit(updatedPrivacyPolicy);
				this.showStagedMessage();
			}
		});
	}

	onEditResources(): void {
		const currentDraft = this.privacyPolicyManager.getById(this.privacyPolicy.policyId);
		const draft = currentDraft || this.privacyPolicy;

		this.privacyPolicyDialogService.openResourcesDialog(
			this.projectId,
			draft
		).subscribe(result => {
			if(result) {
				const draft = this.privacyPolicyManager.getById(this.privacyPolicy.policyId);
				if(draft) {
					Object.assign(draft, result);
					this.privacyPolicyManager.update(draft);
					this.privacyPolicyUpdated.emit(draft);
					this.showStagedMessage();
				}
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Privacy Policy',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.privacyPolicy.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.privacyPolicyDeleted.emit(this.privacyPolicy.policyId);
			}
		});
	}

	onClose(): void {
		this.closed.emit();
	}

	private showStagedMessage(): void {
		this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
	}

	isFieldModified(fieldName: string): boolean {
		return this.privacyPolicyManager.isFieldModified(this.privacyPolicy.policyId, fieldName);
	}
}
