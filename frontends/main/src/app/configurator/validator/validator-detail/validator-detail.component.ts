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
import {Validator} from '@core/model/validator';
import {ValidatorManagerService} from '../../services/manager/validator-manager.service';
import {ValidatorDialogService} from '../../services/dialogs/validator-dialog.service';
import {
	ValidatorWorkflowConfiguration
} from '../../dialogs/validator/validator-workflow-dialog/validator-workflow-dialog.component';

@Component({
	selector: 'app-validator-detail',
	standalone: true,
	templateUrl: './validator-detail.component.html',
	styleUrls: ['./validator-detail.component.css'],
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatTooltipModule
	]
})
export class ValidatorDetailComponent implements OnInit, OnDestroy {
	@Input() validator!: Validator;
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() allValidators: Validator[] = [];
	@Output() validatorUpdated = new EventEmitter<Validator>();
	@Output() validatorDeleted = new EventEmitter<string>();
	@Output() closed = new EventEmitter<void>();

	selectedLanguage = '';
	private languageSubscription: Subscription;

	private workflowConfigurationsMap = new Map<string, ValidatorWorkflowConfiguration | null>();

	constructor(
		public validatorManager: ValidatorManagerService,
		private languageService: LanguageService,
		private validatorDialogService: ValidatorDialogService,
		private dialog: MatDialog,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	onEditBasicInfo(): void {
		this.validatorDialogService.openBasicInfoDialog(
			this.projectId,
			this.validator,
			this.project?.languages || []
		).subscribe((result: any) => {
			if(result) {
				const updatedValidator: Validator = {...this.validator, ...result};
				this.validatorManager.update(updatedValidator);
				this.validatorUpdated.emit(updatedValidator);
				this.showStagedMessage();
			}
		});
	}

	onEditWorkflow(): void {
		const currentConfiguration = this.workflowConfigurationsMap.get(this.validator.validatorId) || null;

		this.validatorDialogService.openWorkflowDialog(
			this.validator,
			currentConfiguration
		).subscribe(result => {
			if(result) {
				if(result.workflowConfiguration !== undefined) {
					this.workflowConfigurationsMap.set(this.validator.validatorId, result.workflowConfiguration);
				}

				const updatedValidator: Validator = {...this.validator, ...result};
				this.validatorManager.update(updatedValidator);
				this.validatorUpdated.emit(updatedValidator);
				this.showStagedMessage();
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Validator',
				message: `Are you sure you want to delete "${this.getTranslatedValue(this.validator.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});

		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.validatorDeleted.emit(this.validator.validatorId);
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
		return this.validatorManager.isFieldModified(this.validator.validatorId, fieldName);
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

	getWorkflowConfiguration(): ValidatorWorkflowConfiguration | null {
		return this.workflowConfigurationsMap.get(this.validator.validatorId) || null;
	}

	getInvalidStatesDisplay(): string {
		const config = this.getWorkflowConfiguration();
		if(!config?.invalidWorkflowStateIds || config.invalidWorkflowStateIds.length === 0) {
			return '';
		}
		return config.invalidWorkflowStateIds.join(', ');
	}

	getValidStatesDisplay(): string {
		const config = this.getWorkflowConfiguration();
		if(!config?.validWorkflowStateIds || config.validWorkflowStateIds.length === 0) {
			return '';
		}
		return config.validWorkflowStateIds.join(', ');
	}
}
