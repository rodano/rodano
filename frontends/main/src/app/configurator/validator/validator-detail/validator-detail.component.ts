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
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';

@Component({
	selector: 'app-validator-detail',
	standalone: true,
	templateUrl: './validator-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule]
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

	constructor(
		public validatorManager: ValidatorManagerService,
		public languageService: LanguageService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
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
		this.validatorDialogService.openWorkflowDialog(this.validator, this.projectId).subscribe(result => {
			if(result) {
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
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.validator.shortname)}"? This action cannot be undone.`,
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

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}

	getWorkflowStateLabel(workflowStateId: string): string {
		return this.languageService.getLabelById(workflowStateId, id => this.workflowStateManager.getById(id));
	}
}
