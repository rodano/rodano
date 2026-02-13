import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {MatDialog} from '@angular/material/dialog';
import {Subscription} from 'rxjs';
import {LanguageService} from '../../../services/language.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../../confirmation-dialog/confirmation-dialog.component';
import {FieldModel} from '@core/model/field-model';
import {DatasetModel} from '@core/model/dataset-model';
import {FieldModelDialogService} from '../../../services/dialogs/field-model-dialog.service';
import {PossibleValue} from '@core/model/possible-value';

@Component({
	selector: 'app-field-model-detail',
	standalone: true,
	templateUrl: './field-model-detail.component.html',
	styleUrls: ['./field-model-detail.component.css'],
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatTooltipModule
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
	private languageSubscription: Subscription;

	allValidators: {id: string; name: string}[] = [];
	allWorkflows: {id: string; name: string}[] = [];

	constructor(
		private languageService: LanguageService,
		private fieldModelDialogService: FieldModelDialogService,
		private dialog: MatDialog,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
		this.loadFieldModel();
		this.loadValidators();
		this.loadWorkflows();
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
		const fieldModel = this.fieldModels.find(fm => fm.fieldModelId === this.fieldModelId);
		this.draftFieldModel = fieldModel ? JSON.parse(JSON.stringify(fieldModel)) : null;

		const original = this.originalFieldModels.find(fm => fm.fieldModelId === this.fieldModelId);
		this.originalFieldModel = original ? JSON.parse(JSON.stringify(original)) : null;
	}

	private loadValidators(): void {
		this.allValidators = [];
	}

	private loadWorkflows(): void {
		this.allWorkflows = [];
	}

	isFieldModified(field: keyof FieldModel): boolean {
		if(!this.originalFieldModel || !this.draftFieldModel) {
			return false;
		}
		return JSON.stringify(this.originalFieldModel[field]) !== JSON.stringify(this.draftFieldModel[field]);
	}

	onClose(): void {
		this.closed.emit();
	}

	getTranslatedValue(translations: Record<string, string> | undefined): string {
		if(!translations) {
			return '';
		}
		return translations[this.selectedLanguage] || '';
	}

	getPossibleValueLabel(pv: PossibleValue): string {
		const name = this.languageService.getDefaultTranslation(pv.shortname) || pv.id;
		return `${name} (${pv.id})`;
	}

	getSortedPossibleValues(): PossibleValue[] {
		if(!this.draftFieldModel?.possibleValues) {
			return [];
		}
		return [...this.draftFieldModel.possibleValues].sort((a, b) =>
			(a.sortOrder ?? 0) - (b.sortOrder ?? 0)
		);
	}

	getLanguageName(code: string | undefined): string {
		if(!code) {
			return 'Unknown';
		}
		try {
			const displayNames = new Intl.DisplayNames(['en'], {type: 'language'});
			return displayNames.of(code) || code.toUpperCase();
		}
		catch (error) {
			console.error(error);
			return code.toUpperCase();
		}
	}

	getValidatorName(validatorId: string): string {
		const validator = this.allValidators.find(v => v.id === validatorId);
		return validator?.name || validatorId;
	}

	getWorkflowName(workflowId: string): string {
		const workflow = this.allWorkflows.find(wf => wf.id === workflowId);
		return workflow?.name || workflowId;
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

		this.fieldModelDialogService.openResourcesDialog(
			this.draftFieldModel,
			this.allValidators,
			this.allWorkflows
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

	onDelete(): void {
		if(!this.draftFieldModel) {
			return;
		}

		const fieldModelName = this.getTranslatedValue(this.draftFieldModel.shortname) || this.draftFieldModel.id;

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
