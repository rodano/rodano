import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {DatasetModel} from '@core/model/dataset-model';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {Subscription} from 'rxjs';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {DatasetModelDialogService} from '../../services/dialogs/dataset-model-dialog.service';
import {ProjectLanguage} from '@core/model/project-language';

@Component({
	selector: 'app-dataset-model-detail',
	standalone: true,
	templateUrl: './dataset-model-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatTooltipModule
	]
})
export class DatasetModelDetailComponent implements OnInit, OnDestroy {
	@Input() datasetModel!: DatasetModel;
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() allDatasetModels: DatasetModel[] = [];
	@Output() datasetModelUpdated = new EventEmitter<DatasetModel>();
	@Output() datasetModelDeleted = new EventEmitter<string>();
	@Output() closed = new EventEmitter<void>();
	@Output() switchToFieldModels = new EventEmitter<void>();

	selectedLanguage = '';
	projectLanguages: ProjectLanguage[] = [];
	private languageSubscription: Subscription;

	constructor(
		public datasetModelManager: DatasetModelManagerService,
		public languageService: LanguageService,
		private datasetModelDialogService: DatasetModelDialogService,
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
		this.datasetModelDialogService.openBasicInfoDialog(
			this.projectId,
			this.datasetModel,
			this.projectLanguages
		).subscribe((result: any) => {
			if(result) {
				const updatedDatasetModel: DatasetModel = {...this.datasetModel, ...result};
				this.datasetModelManager.update(updatedDatasetModel);
				this.datasetModelUpdated.emit(updatedDatasetModel);
				this.showStagedMessage();
			}
		});
	}

	onEditFamily(): void {
		this.datasetModelDialogService.openFamilyDialog(
			this.datasetModel
		).subscribe((result: any) => {
			if(result) {
				const updatedDatasetModel: DatasetModel = {...this.datasetModel, ...result};
				this.datasetModelManager.update(updatedDatasetModel);
				this.datasetModelUpdated.emit(updatedDatasetModel);
				this.showStagedMessage();
			}
		});
	}

	onEditExport(): void {
		this.datasetModelDialogService.openExportDialog(
			this.datasetModel
		).subscribe((result: any) => {
			if(result) {
				const updatedDatasetModel: DatasetModel = {...this.datasetModel, ...result};
				this.datasetModelManager.update(updatedDatasetModel);
				this.datasetModelUpdated.emit(updatedDatasetModel);
				this.showStagedMessage();
			}
		});
	}

	onEditLabelPatterns(): void {
		this.datasetModelDialogService.openLabelPatternsDialog(
			this.datasetModel
		).subscribe((result: any) => {
			if(result) {
				const updatedDatasetModel: DatasetModel = {...this.datasetModel, ...result};
				this.datasetModelManager.update(updatedDatasetModel);
				this.datasetModelUpdated.emit(updatedDatasetModel);
				this.showStagedMessage();
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Dataset Model',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.datasetModel.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});

		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.datasetModelDeleted.emit(this.datasetModel.datasetModelId);
			}
		});
	}

	onClose(): void {
		this.closed.emit();
	}

	onSwitchToFieldModels(): void {
		this.switchToFieldModels.emit();
	}

	private showStagedMessage(): void {
		this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
	}

	isFieldModified(fieldName: string): boolean {
		return this.datasetModelManager.isFieldModified(this.datasetModel.datasetModelId, fieldName);
	}
}
