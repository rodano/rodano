import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {FormModel} from '@core/model/form-model';
import {FormModelManagerService} from '../../services/manager/form-model-manager.service';
import {FormModelDialogService} from '../../services/dialogs/form-model-dialog.service';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {LanguageService} from '../../services/language.service';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {BaseManagerDetailComponent} from '../../shared/base-manager-detail.component';
import {SettingItemComponent} from '../../shared/setting-item/setting-item.component';
import {Rule} from '@core/model/rule';
import {RuleListComponent} from '../../rules/rule-list/rule-list.component';

@Component({
	selector: 'app-form-model-detail',
	standalone: true,
	templateUrl: './form-model-detail.component.html',
	styleUrls: ['./form-model-detail.component.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent, RuleListComponent]
})
export class FormModelDetailComponent extends BaseManagerDetailComponent<FormModel, FormModelManagerService> {
	@Input() override entity!: FormModel;
	@Input() override allEntities: FormModel[] = [];

	@Input() set formModel(v: FormModel) {this.entity = v;}
	get formModel(): FormModel {return this.entity;}

	@Input() set allFormModels(v: FormModel[]) {this.allEntities = v;}

	@Input() initialTab: 'general' | 'rules' = 'general';

	@Output() formModelUpdated = this.entityUpdated;
	@Output() formModelDeleted = this.entityDeleted;
	@Output() switchToLayouts = new EventEmitter<void>();
	@Output() switchToRuleEditor = new EventEmitter<Rule>();

	readonly ruleTypes = [{type: null, label: 'Rules'}];
	readonly ruleDomains = ['SCOPE', 'EVENT', 'FORM'];

	constructor(
		formModelManager: FormModelManagerService,
		languageService: LanguageService,
		private formModelDialogService: FormModelDialogService,
		private workflowManager: WorkflowManagerService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(formModelManager, languageService, snackBar);
	}

	protected getEntityId(): string {return this.entity.formModelId;}

	onEditBasicInfo(): void {
		this.formModelDialogService.openBasicInfoDialog(
			this.projectId, this.entity, this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditResources(): void {
		this.formModelDialogService.openResourcesDialog(
			this.projectId, this.entity
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
				title: 'Delete Form Model',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedName(this.entity.shortname)}"?`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed) {
				this.entityDeleted.emit(this.entity.formModelId);
			}
		});
	}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}
}
