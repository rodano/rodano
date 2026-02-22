import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatTabsModule} from '@angular/material/tabs';
import {MatIconModule} from '@angular/material/icon';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {MatTooltip} from '@angular/material/tooltip';
import {ProjectSettingsDialogService} from '../../services/dialogs/project-settings-dialog.service';
import {LanguageService} from '../../services/language.service';
import {Subscription} from 'rxjs';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';

@Component({
	selector: 'app-project-settings-detail',
	standalone: true,
	templateUrl: './project-settings-detail.component.html',
	styleUrls: ['./project-settings-detail.component.css'],
	imports: [CommonModule, MatTabsModule, MatIconModule, MatTooltip]
})
export class ProjectSettingsDetailComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Input() workingProject: ConfiguratorProject | null = null;
	@Output() fieldsUpdated = new EventEmitter<Partial<ConfiguratorProject>>();

	selectedLanguage = '';
	private languageSubscription: Subscription;

	constructor(
		private dialogService: ProjectSettingsDialogService,
		private languageService: LanguageService,
		private profileManager: ProfileManagerService
	) {}

	ngOnInit(): void {
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['selectedNode']) {
			console.log('Selected node:', this.selectedNode);
		}
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	getTranslatedName(translations: Record<string, string> | undefined): string {
		return this.getTranslatedValue(translations);
	}

	getTranslatedValue(translations: Record<string, string> | undefined, languageCode?: string): string {
		if(!translations) {
			return '';
		}
		const lang = languageCode || this.selectedLanguage;
		return translations[lang] || '';
	}

	getLanguageName(code: string | undefined): string {
		if(!code) {
			return 'Unknown';
		}
		try {
			const displayNames = new Intl.DisplayNames(['en'], {type: 'language'});
			return displayNames.of(code) || code.toUpperCase();
		}
		catch (e) {
			console.error(e);
			return code.toUpperCase();
		}
	}

	isFieldModified(fieldName: string): boolean {
		if(!this.project || !this.workingProject) {
			return false;
		}
		const original = this.project[fieldName as keyof ConfiguratorProject];
		const current = this.workingProject[fieldName as keyof ConfiguratorProject];
		if(typeof original === 'object' && original !== null) {
			return JSON.stringify(original) !== JSON.stringify(current);
		}
		return original !== current;
	}

	onLanguageChange(event: Event): void {
		const select = event.target as HTMLSelectElement;
		this.selectedLanguage = select.value;
	}

	onEditBasicInfo(): void {
		if(!this.project) {
			return;
		}

		this.dialogService.openBasicInfoDialog(this.project).subscribe(result => {
			if(result) {
				this.fieldsUpdated.emit({
					code: result.code,
					shortname: result.shortname,
					longname: result.longname,
					description: result.description,
					url: result.url,
					color: result.color
				});
			}
		});
	}

	onEditIntroductionText(): void {
		if(!this.project) {
			return;
		}

		this.dialogService.openIntroductionTextDialog(this.project).subscribe(result => {
			if(result) {
				this.fieldsUpdated.emit({
					introductionText: result.introductionText
				});
			}
		});
	}

	onEditEmailSettings(): void {
		if(!this.project) {
			return;
		}

		this.dialogService.openEmailSettingsDialog(this.project).subscribe(result => {
			if(result) {
				this.fieldsUpdated.emit({
					email: result.email,
					smtpTls: result.smtpTls
				});
			}
		});
	}

	onEditPasswordPolicies(): void {
		if(!this.project) {
			return;
		}

		this.dialogService.openPasswordPoliciesDialog(this.project).subscribe(result => {
			if(result) {
				this.fieldsUpdated.emit({
					passwordStrong: result.passwordStrong,
					passwordLength: result.passwordLength,
					passwordValidityDuration: result.passwordValidityDuration,
					passwordUnique: result.passwordUnique
				});
			}
		});
	}

	onEditEproSettings(): void {
		if(!this.project) {
			return;
		}

		this.dialogService.openEproSettingsDialog(this.project).subscribe(result => {
			if(result) {
				this.fieldsUpdated.emit({
					eproEnabled: result.eproEnabled,
					eproProfileId: result.eproProfileId
				});
			}
		});
	}

	onEditClientInfo(): void {
		if(!this.project) {
			return;
		}

		this.dialogService.openClientInfoDialog(this.project).subscribe(result => {
			if(result) {
				this.fieldsUpdated.emit({
					clientName: result.clientName,
					clientEmail: result.clientEmail,
					protocolNo: result.protocolNo,
					versionNumber: result.versionNumber,
					versionDate: result.versionDate
				});
			}
		});
	}

	onEditLanguages(): void {
		if(!this.project) {
			return;
		}

		this.dialogService.openLanguagesDialog(this.project).subscribe(result => {
			if(result) {
				this.fieldsUpdated.emit({
					languages: result
				});
			}
		});
	}

	onEditRuleTags(): void {
		if(!this.project) {
			return;
		}

		this.dialogService.openRuleTagsDialog(this.project).subscribe(result => {
			if(result) {
				this.fieldsUpdated.emit({
					ruleTags: result
				});
			}
		});
	}

	getProfileLabel(profileId: string): string {
		const profile = this.profileManager.getById(profileId);
		if(!profile) {
			return profileId;
		}
		const name = this.languageService.getDefaultTranslation(profile.shortname) || profile.id;
		return `${name} (${profile.id})`;
	}
}
