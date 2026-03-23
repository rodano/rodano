import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatSnackBar} from '@angular/material/snack-bar';
import {BaseDialogComponent} from '../../base-dialog.component';
import {ChartModel} from '@core/model/chart-model';
import {WorkflowWidgetConfig} from '@core/model/workflow-widget-config';
import {WorkflowSummary} from '@core/model/workflow-summary';
import {ResourceCategory} from '@core/model/resource-category';
import {LanguageService} from '../../../services/language.service';
import {Widget} from '@core/model/widget';
import {WidgetTypeDef} from '../../../shared/layout-widget-types';
import {Feature} from '@core/model/feature';
import {ScopeModel} from '@core/model/scope-model';
import {EventModel} from '@core/model/event-model';
import {DatasetModel} from '@core/model/dataset-model';
import {FormModel} from '@core/model/form-model';
import {Profile} from '@core/model/profile';
import {MatCheckboxModule} from '@angular/material/checkbox';

export interface WidgetConfigDialogData {
	widget: Widget;
	typeDef: WidgetTypeDef;
	charts: ChartModel[];
	workflowWidgets: WorkflowWidgetConfig[];
	workflowSummaries: WorkflowSummary[];
	resourceCategories: ResourceCategory[];
	features: Feature[];
	scopeModels: ScopeModel[];
	eventModels: EventModel[];
	datasetModels: DatasetModel[];
	formModels: FormModel[];
	profiles: Profile[];
}

@Component({
	selector: 'app-widget-config-dialog',
	standalone: true,
	templateUrl: './widget-config-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatIconModule, MatInputModule,
		MatSelectModule, MatCheckboxModule]
})
export class WidgetConfigDialogComponent extends BaseDialogComponent<WidgetConfigDialogData> implements OnInit {
	form: FormGroup;
	paramForms = new Map<string, FormGroup>();
	targetOptions: {id: string; label: string}[] = [];

	readonly WIDTH_OPTIONS = [
		{value: 'FULL', label: 'Whole Page'},
		{value: 'HALF', label: 'Half the Page'}
	];

	constructor(
		public languageService: LanguageService,
		private fb: FormBuilder,
		dialogRef: MatDialogRef<WidgetConfigDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: WidgetConfigDialogData,
		snackBar: MatSnackBar
	) {
		super(dialogRef, data, snackBar);
	}

	ngOnInit(): void {
		this.form = this.fb.group({
			width: [this.data.widget.width ?? 'FULL'],
			textBefore: [this.data.widget.textBefore ?? ''],
			textAfter: [this.data.widget.textAfter ?? ''],
			requiredFeatureId: [this.data.widget.requiredFeatureId ?? ''],
			rightEntity: [this.data.widget.rightEntity ?? ''],
			rightValue: [this.data.widget.rightValue ?? ''],
			rightTargetId: [this.data.widget.rightTargetId ?? '']
		});

		if(this.data.widget.rightEntity) {
			this.updateTargetOptions(this.data.widget.rightEntity);
		}

		this.form.get('rightEntity')!.valueChanges.subscribe(entity => {
			this.form.get('rightTargetId')!.setValue('');
			this.updateTargetOptions(entity);
		});

		this.data.typeDef.parameters.forEach(param => {
			const raw = this.data.widget.parameters?.[param.id];
			const value = param.kind === 'boolean'
				? (raw === 'true' || raw === true as any)
				: (raw ?? (param.kind === 'number' ? null : ''));
			this.paramForms.set(param.id, this.fb.group({value: [value]}));
		});
	}

	getParamForm(paramId: string): FormGroup {
		return this.paramForms.get(paramId)!;
	}

	get availableFeatures(): Feature[] {
		return this.data.features ?? [];
	}

	updateTargetOptions(entity: string): void {
		switch(entity) {
			case 'SCOPE_MODEL':
				this.targetOptions = (this.data.scopeModels ?? []).map((s: ScopeModel) => ({
					id: s.scopeModelId,
					label: this.languageService.getLabel(s)
				}));
				break;
			case 'EVENT_MODEL':
				this.targetOptions = (this.data.eventModels ?? []).map((e: EventModel) => ({
					id: e.eventModelId,
					label: this.languageService.getLabel(e)
				}));
				break;
			case 'DATASET_MODEL':
				this.targetOptions = (this.data.datasetModels ?? []).map((d: DatasetModel) => ({
					id: d.datasetModelId,
					label: this.languageService.getLabel(d)
				}));
				break;
			case 'FORM_MODEL':
				this.targetOptions = (this.data.formModels ?? []).map((f: FormModel) => ({
					id: f.formModelId,
					label: this.languageService.getLabel(f)
				}));
				break;
			case 'PROFILE':
				this.targetOptions = (this.data.profiles ?? []).map((p: Profile) => ({
					id: p.profileId,
					label: this.languageService.getLabel(p)
				}));
				break;
			default:
				this.targetOptions = [];
		}
	}

	onSave(): void {
		const parameters: Record<string, string> = {};
		this.data.typeDef.parameters.forEach(param => {
			const form = this.paramForms.get(param.id)!;
			const value = form.getRawValue().value;
			if(value !== null && value !== '' && value !== undefined) {
				parameters[param.id] = String(value);
			}
		});
		const v = this.form.getRawValue();
		const result: any = {parameters, width: v.width};

		if(v.textBefore) {
			result.textBefore = v.textBefore;
		}
		if(v.textAfter) {
			result.textAfter = v.textAfter;
		}
		if(v.requiredFeatureId) {
			result.requiredFeatureId = v.requiredFeatureId;
		}
		if(v.rightEntity) {
			result.rightEntity = v.rightEntity;
		}
		if(v.rightValue) {
			result.rightValue = v.rightValue;
		}
		if(v.rightTargetId) {
			result.rightTargetId = v.rightTargetId;
		}

		this.dialogRef.close(result);
	}
}
