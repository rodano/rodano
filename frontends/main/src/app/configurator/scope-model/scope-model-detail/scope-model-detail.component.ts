import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ScopeModel} from '@core/model/scope-model';
import {
	WorkflowStateSelection
} from '../../dialogs/scope-model/scope-model-resources-dialog/scope-model-resources-dialog.component';
import {ScopeModelManagerService} from '../../services/manager/scope-model-manager.service';
import {ScopeModelDialogService} from '../../services/dialogs/scope-model-dialog.service';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';
import {LanguageService} from '../../services/language.service';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {BaseManagerDetailComponent} from '../../shared/base-manager-detail.component';
import {SettingItemComponent} from '../../shared/setting-item/setting-item.component';
import {FormModelManagerService} from '../../services/manager/form-model-manager.service';
import {LayoutEditorComponent} from '../../shared/layout-editor/layout-editor.component';

interface WorkflowStateGroup {
	workflowId: string;
	workflowName: string;
	states: {id: string; name: string}[];
}

@Component({
	selector: 'app-scope-model-detail',
	standalone: true,
	templateUrl: './scope-model-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent,
		LayoutEditorComponent]
})
export class ScopeModelDetailComponent extends BaseManagerDetailComponent<ScopeModel, ScopeModelManagerService> {
	@Input() override entity!: ScopeModel;
	@Input() override allEntities: ScopeModel[] = [];

	@Input() set scopeModel(v: ScopeModel) {this.entity = v;}
	get scopeModel(): ScopeModel {return this.entity;}

	@Input() set allScopeModels(v: ScopeModel[]) {this.allEntities = v;}

	@Output() scopeModelUpdated = this.entityUpdated;
	@Output() scopeModelDeleted = this.entityDeleted;
	@Output() switchToEventModels = new EventEmitter<void>();
	@Output() switchToEventGroups = new EventEmitter<void>();

	activeTab: 'general' | 'layout' = 'general';

	constructor(
		scopeModelManager: ScopeModelManagerService,
		languageService: LanguageService,
		private scopeModelDialogService: ScopeModelDialogService,
		private datasetModelManager: DatasetModelManagerService,
		private formModelManager: FormModelManagerService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private profileManager: ProfileManagerService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(scopeModelManager, languageService, snackBar);
	}

	protected getEntityId(): string {return this.entity.scopeModelId;}

	protected override onInit(): void {
		this.workflowStateManager.load(this.projectId).subscribe();
	}

	onEditBasicInfo(): void {
		this.scopeModelDialogService.openBasicInfoDialog(
			this.projectId, this.entity, this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditRelationships(): void {
		this.scopeModelDialogService.openRelationshipsDialog(
			this.projectId, this.entity
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditDefaultSettings(): void {
		this.scopeModelDialogService.openDefaultSettingsDialog(
			this.projectId, this.entity
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditPattern(): void {
		this.scopeModelDialogService.openPatternDialog(this.entity).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditResources(): void {
		const workflowStateSelections: WorkflowStateSelection[] = (this.entity.workflowStateIds || []).map(stateId => {
			const state = this.workflowStateManager.getById(stateId);
			return {workflowId: state?.workflowId || '', workflowStateId: stateId};
		});

		this.scopeModelDialogService.openResourcesDialog(
			this.projectId, this.entity, workflowStateSelections
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
				title: 'Delete Scope Model',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedName(this.entity.shortname)}"?`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed) {
				this.entityDeleted.emit(this.entity.scopeModelId);
			}
		});
	}

	onSwitchToEventModels(): void {this.switchToEventModels.emit();}
	onSwitchToEventGroups(): void {this.switchToEventGroups.emit();}

	get workflowStateIdsModified(): boolean {
		return this.isFieldModified('workflowStateIds');
	}

	getWorkflowStateSelections(): WorkflowStateGroup[] {
		const grouped = new Map<string, {id: string; name: string}[]>();
		(this.entity.workflowStateIds || []).forEach(stateId => {
			const state = this.workflowStateManager.getById(stateId);
			if(!state) {
				return;
			}
			if(!grouped.has(state.workflowId)) {
				grouped.set(state.workflowId, []);
			}
			const name = `${this.languageService.getDefaultTranslation(state.shortname) || state.id} (${state.id})`;
			grouped.get(state.workflowId)!.push({id: stateId, name});
		});
		const result: WorkflowStateGroup[] = [];
		grouped.forEach((states, workflowId) => result.push({workflowId, workflowName: this.getWorkflowLabel(workflowId), states}));
		return result;
	}

	getScopeModelLabel(scopeModelId: string): string {
		return this.languageService.getLabelById(scopeModelId, id => this.manager.getById(id));
	}

	getDatasetModelLabel(datasetModelId: string): string {
		return this.languageService.getLabelById(datasetModelId, id => this.datasetModelManager.getById(id));
	}

	getFormModelLabel(formModelId: string): string {
		return this.languageService.getLabelById(formModelId, id => this.formModelManager.getById(id));
	}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}

	getProfileLabel(profileId: string): string {
		return this.languageService.getLabelById(profileId, id => this.profileManager.getById(id));
	}
}
