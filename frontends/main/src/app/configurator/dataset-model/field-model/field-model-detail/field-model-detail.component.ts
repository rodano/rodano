import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {MatDialog} from '@angular/material/dialog';
import {Subscription} from 'rxjs';
import {LanguageService} from '../../../services/language.service';
import {ConfirmationDialogComponent} from '../../../../confirmation-dialog/confirmation-dialog.component';
import {FieldModel} from '@core/model/field-model';
import {DatasetModel} from '@core/model/dataset-model';
import {FieldModelDialogService} from '../../../services/dialogs/field-model-dialog.service';
import {PossibleValue} from '@core/model/possible-value';
import {FieldModelManagerService} from '../../../services/manager/field-model-manager.service';
import {ValidatorManagerService} from '../../../services/manager/validator-manager.service';
import {ProjectLanguage} from '@core/model/project-language';
import {WorkflowManagerService} from '../../../services/manager/workflow-manager.service';
import {DangerZoneComponent} from '../../../shared/danger-zone/danger-zone.component';

@Component({
	selector: 'app-field-model-detail',
	standalone: true,
	templateUrl: './field-model-detail.component.html',
	styleUrls: ['./field-model-detail.component.css'],
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatTooltipModule,
		DangerZoneComponent
	]
})
export class FieldModelDetailComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() fieldModelId = '';
	@Input() fieldModels: FieldModel[] = [];
	@Input() originalFieldModels: FieldModel[] = [];
	@Input() datasetModel: DatasetModel | null = null;
	@Input() project: ConfiguratorProject | null = null;
	@Output() closed = new EventEmitter<void>();
	@Output() fieldModelUpdated = new EventEmitter<any>();
	@Output() fieldModelDeleted = new EventEmitter<string>();

	originalFieldModel: FieldModel | null = null;
	draftFieldModel: FieldModel | null = null;
	selectedLanguage = '';
	projectLanguages: ProjectLanguage[] = [];
	private languageSubscription: Subscription;

	constructor(
		public languageService: LanguageService,
		private fieldModelDialogService: FieldModelDialogService,
		private fieldModelManager: FieldModelManagerService,
		private validatorManager: ValidatorManagerService,
		private workflowManager: WorkflowManagerService,
		private dialog: MatDialog
	) {}

	ngOnInit(): void {
		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['fieldModelId'] || changes['fieldModels']) {
			this.loadFieldModel();
		}
	}

	ngOnDestroy(): void {
		if(this.languageSubscription) {
			this.languageSubscription.unsubscribe();
		}
	}

	private loadFieldModel(): void {
		this.draftFieldModel = this.fieldModelManager.getById(this.fieldModelId)
			? JSON.parse(JSON.stringify(this.fieldModelManager.getById(this.fieldModelId)))
			: null;
		const original = this.fieldModelManager.getOriginals().find(fm => fm.fieldModelId === this.fieldModelId);
		this.originalFieldModel = original ? JSON.parse(JSON.stringify(original)) : null;
	}

	isFieldModified(field: keyof FieldModel): boolean {
		return this.fieldModelManager.isFieldModified(this.fieldModelId, field as string);
	}

	onClose(): void {
		this.closed.emit();
	}

	getSortedPossibleValues(): PossibleValue[] {
		if(!this.draftFieldModel?.possibleValues) {
			return [];
		}
		return [...this.draftFieldModel.possibleValues].sort((a, b) =>
			(a.sortOrder ?? 0) - (b.sortOrder ?? 0)
		);
	}

	getValidatorLabel(validatorId: string): string {
		return this.languageService.getLabelById(validatorId, id => this.validatorManager.getById(id));
	}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}

	getTypeLabel(type: string): string {
		const typeMap: Record<string, string> = {
			STRING: 'String',
			AUTO_COMPLETION: 'Autocompleted String',
			DATE: 'Date',
			DATE_SELECT: 'Date with Selection',
			NUMBER: 'Number',
			SELECT: 'Combobox',
			RADIO: 'Radio',
			CHECKBOX: 'Checkbox',
			CHECKBOX_GROUP: 'Checkbox Group',
			TEXTAREA: 'Text Area',
			FILE: 'File'
		};
		return typeMap[type] || type;
	}

	getDataTypeLabel(dataType: string): string {
		const dataTypeMap: Record<string, string> = {
			STRING: 'String',
			DATE: 'Date',
			NUMBER: 'Number',
			BOOLEAN: 'Boolean',
			BLOB: 'Blob'
		};
		return dataTypeMap[dataType] || dataType;
	}

	onEditBasicInfo(): void {
		if(!this.draftFieldModel || !this.datasetModel) {
			return;
		}

		this.fieldModelDialogService.openBasicInfoDialog(
			this.draftFieldModel,
			this.projectId,
			this.datasetModel.datasetModelId,
			this.projectLanguages
		).subscribe(result => {
			if(result && this.draftFieldModel) {
				this.draftFieldModel = {
					...this.draftFieldModel,
					...result
				};
				this.fieldModelUpdated.emit(this.draftFieldModel);
			}
		});
	}

	onEditValidation(): void {
		if(!this.draftFieldModel) {
			return;
		}

		this.fieldModelDialogService.openValidationDialog(
			this.draftFieldModel,
			this.projectId,
			this.project?.languages || []
		).subscribe(result => {
			if(result && this.draftFieldModel) {
				this.draftFieldModel = {
					...this.draftFieldModel,
					...result
				};
				this.fieldModelUpdated.emit(this.draftFieldModel);
			}
		});
	}

	onEditCalculatedValue(): void {
		if(!this.draftFieldModel) {
			return;
		}

		this.fieldModelDialogService.openCalculatedValueDialog(
			this.draftFieldModel,
			this.projectId
		).subscribe(result => {
			if(result && this.draftFieldModel) {
				this.draftFieldModel = {
					...this.draftFieldModel,
					...result
				};
				this.fieldModelUpdated.emit(this.draftFieldModel);
			}
		});
	}

	onEditExport(): void {
		if(!this.draftFieldModel) {
			return;
		}

		this.fieldModelDialogService.openExportDialog(this.draftFieldModel).subscribe(result => {
			if(result && this.draftFieldModel) {
				this.draftFieldModel = {
					...this.draftFieldModel,
					...result
				};
				this.fieldModelUpdated.emit(this.draftFieldModel);
			}
		});
	}

	onEditHelp(): void {
		if(!this.draftFieldModel) {
			return;
		}

		this.fieldModelDialogService.openHelpDialog(
			this.draftFieldModel,
			this.project?.languages || []
		).subscribe(result => {
			if(result && this.draftFieldModel) {
				this.draftFieldModel = {
					...this.draftFieldModel,
					...result
				};
				this.fieldModelUpdated.emit(this.draftFieldModel);
			}
		});
	}

	onEditPossibleValues(): void {
		if(!this.draftFieldModel) {
			return;
		}

		this.fieldModelDialogService.openPossibleValuesDialog(
			this.draftFieldModel,
			this.projectId,
			this.project?.languages || []
		).subscribe(result => {
			if(result && this.draftFieldModel) {
				this.draftFieldModel = {
					...this.draftFieldModel,
					...result
				};
				this.fieldModelUpdated.emit(this.draftFieldModel);
			}
		});
	}

	onEditResources(): void {
		if(!this.draftFieldModel) {
			return;
		}

		this.fieldModelDialogService.openResourcesDialog(this.draftFieldModel).subscribe(result => {
			if(result && this.draftFieldModel) {
				this.draftFieldModel = {...this.draftFieldModel, ...result};
				this.fieldModelUpdated.emit(this.draftFieldModel);
			}
		});
	}

	onDelete(): void {
		if(!this.draftFieldModel) {
			return;
		}

		const fieldModelName = this.languageService.getTranslatedValue(this.draftFieldModel.shortname) || this.draftFieldModel.id;

		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Field Model',
				message: `Are you sure you want to delete "${fieldModelName}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});

		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed && this.draftFieldModel) {
				this.fieldModelDeleted.emit(this.draftFieldModel.fieldModelId);
			}
		});
	}

	hasValidationSettings(): boolean {
		if(!this.draftFieldModel?.type) {
			return false;
		}
		const type = this.draftFieldModel.type;
		return type !== 'AUTO_COMPLETION'
		  && type !== 'SELECT'
		  && type !== 'RADIO'
		  && type !== 'CHECKBOX'
		  && type !== 'CHECKBOX_GROUP';
	}

	getAdvancedHelpText(): string {
		return this.languageService.getDefaultTranslation(this.draftFieldModel?.advancedHelp) || '';
	}

	hasAdvancedHelp(): boolean {
		return !!(this.draftFieldModel?.advancedHelp && Object.keys(this.draftFieldModel.advancedHelp).length > 0);
	}

	showStringValidation(): boolean {
		return this.draftFieldModel?.type === 'STRING';
	}

	showTextAreaValidation(): boolean {
		const type = this.draftFieldModel?.type;
		return type === 'TEXTAREA' || type === 'FILE';
	}

	showDateValidation(): boolean {
		return this.draftFieldModel?.type === 'DATE';
	}

	showDateSelectValidation(): boolean {
		return this.draftFieldModel?.type === 'DATE_SELECT';
	}

	showNumberValidation(): boolean {
		return this.draftFieldModel?.type === 'NUMBER';
	}
}
