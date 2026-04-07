import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {DatasetModel} from '@core/model/dataset-model';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {DatasetModelDialogService} from '../../services/dialogs/dataset-model-dialog.service';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {BaseManagerDetailComponent} from '../../shared/base-manager-detail.component';
import {SettingItemComponent} from '../../shared/setting-item/setting-item.component';
import {Rule} from '@core/model/rule';
import {RuleListComponent} from '../../rules/rule-list/rule-list.component';
import {UsedByComponent} from '../../shared/used-by/used-by.component';

@Component({
	selector: 'app-dataset-model-detail',
	standalone: true,
	templateUrl: './dataset-model-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent,
		RuleListComponent, UsedByComponent]
})
export class DatasetModelDetailComponent extends BaseManagerDetailComponent<DatasetModel, DatasetModelManagerService> {
	@Input() override entity!: DatasetModel;
	@Input() override allEntities: DatasetModel[] = [];

	@Input() set datasetModel(v: DatasetModel) {this.entity = v;}
	get datasetModel(): DatasetModel {return this.entity;}

	@Input() set allDatasetModels(v: DatasetModel[]) {this.allEntities = v;}

	@Input() initialTab: 'general' | 'rules' = 'general';

	@Output() datasetModelUpdated = this.entityUpdated;
	@Output() datasetModelDeleted = this.entityDeleted;
	@Output() switchToRuleEditor = new EventEmitter<Rule>();

	@Output() switchToFieldModels = new EventEmitter<void>();

	readonly ruleTypes = [
		{type: 'DELETE', label: 'Removal Rules'},
		{type: 'RESTORE', label: 'Restoration Rules'}
	];

	readonly ruleDomains = ['SCOPE', 'EVENT', 'DATASET'];

	constructor(
		datasetModelManager: DatasetModelManagerService,
		languageService: LanguageService,
		private datasetModelDialogService: DatasetModelDialogService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(datasetModelManager, languageService, snackBar);
	}

	protected getEntityId(): string {return this.entity.datasetModelId;}

	onEditBasicInfo(): void {
		this.datasetModelDialogService.openBasicInfoDialog(
			this.projectId,
			this.entity,
			this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditFamily(): void {
		this.datasetModelDialogService.openFamilyDialog(
			this.entity
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditExport(): void {
		this.datasetModelDialogService.openExportDialog(
			this.entity
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditLabelPatterns(): void {
		this.datasetModelDialogService.openLabelPatternsDialog(
			this.entity
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
				title: 'Delete Dataset Model',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.entity.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.entityDeleted.emit(this.entity.datasetModelId);
			}
		});
	}

	onSwitchToFieldModels(): void {this.switchToFieldModels.emit();}
}
