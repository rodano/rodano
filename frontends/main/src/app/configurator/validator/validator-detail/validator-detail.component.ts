import {Component, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {Validator} from '@core/model/validator';
import {ValidatorManagerService} from '../../services/manager/validator-manager.service';
import {ValidatorDialogService} from '../../services/dialogs/validator-dialog.service';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {BaseManagerDetailComponent} from '../../shared/base-manager-detail.component';

@Component({
	selector: 'app-validator-detail',
	standalone: true,
	templateUrl: './validator-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent]
})
export class ValidatorDetailComponent extends BaseManagerDetailComponent<Validator, ValidatorManagerService> {
	@Input() override entity!: Validator;
	@Input() override allEntities: Validator[] = [];
	@Output() validatorUpdated = this.entityUpdated;
	@Output() validatorDeleted = this.entityDeleted;

	@Input() set validator(v: Validator) {this.entity = v;}
	get validator(): Validator {return this.entity;}

	@Input() set allValidators(v: Validator[]) {this.allEntities = v;}

	constructor(
		validatorManager: ValidatorManagerService,
		languageService: LanguageService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private validatorDialogService: ValidatorDialogService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(validatorManager, languageService, snackBar);
	}

	protected getEntityId(): string {return this.entity.validatorId;}

	onEditBasicInfo(): void {
		this.validatorDialogService.openBasicInfoDialog(
			this.projectId,
			this.entity,
			this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditWorkflow(): void {
		this.validatorDialogService.openWorkflowDialog(
			this.entity, this.projectId
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Validator',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.entity.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.entityDeleted.emit(this.entity.validatorId);
			}
		});
	}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}

	getWorkflowStateLabel(workflowStateId: string): string {
		return this.languageService.getLabelById(workflowStateId, id => this.workflowStateManager.getById(id));
	}
}
