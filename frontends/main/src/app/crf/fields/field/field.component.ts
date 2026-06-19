import {Component, DestroyRef, OnInit, computed, effect, input, model, signal} from '@angular/core';
import {Cell} from '@core/model/cell';
import {LocalizeMapPipe} from '../../../pipes/localize-map.pipe';
import {FileUploadComponent} from '../file-upload/file-upload.component';
import {AutoCompleteComponent} from '../auto-complete/auto-complete.component';
import {CheckboxGroupComponent} from '../checkbox-group/checkbox-group.component';
import {DateSelectComponent} from '../date-select/date-select.component';
import {DateComponent} from '../date/date.component';
import {MatRadioModule} from '@angular/material/radio';
import {MatCheckbox} from '@angular/material/checkbox';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {FormControl, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatInput} from '@angular/material/input';
import {MatFormField} from '@angular/material/form-field';
import {CRFService} from '../../services/crf.service';
import {FieldUpdateService} from '../../services/field-update.service';
import {CRFField} from '../../models/crf-field';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FieldModelType} from '@core/model/field-model-type';
import {MatIcon} from '@angular/material/icon';
import {MatDialog} from '@angular/material/dialog';
import {FieldModel} from '@core/model/field-model';
import {FieldModelHelpComponent} from '../../dialogs/field-model-help/field-model-help.component';
import {EmptyObjectCheck} from '../../../utils/empty-object-check';
import {AuditTrailFieldComponent} from '../../../audit-trail-field/audit-trail-field.component';
import {AuthStateService} from '../../../services/auth-state.service';
import {MatMenuModule} from '@angular/material/menu';
import {WorkflowStatusComponent} from '../../workflow-status/workflow-status.component';
import {LocalizeFieldModelPipe} from '../../../pipes/localize-field-model.pipe';
import {WorkflowAction} from '@core/model/workflow-action';
import {WorkflowActionService} from '../../services/workflow-action.service';
import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {Workflowable} from '@core/utilities/workflowable';
import {AdministrationService} from '@core/services/administration.service';
import {MatIconButton} from '@angular/material/button';
import {WorkflowStatusImportantPipe} from '../../../pipes/workflow-status-important.pipe';
import {WorkflowStatus} from '@core/model/workflow-status';
import {WorkflowStatusNotImportantPipe} from '../../../pipes/workflow-status-not-important';
import {FeatureStatic} from '@core/model/feature-static';
import {Field} from '@core/model/field';
import {WorkflowableUpdateService} from '../../services/workflowable-update.service';

@Component({
	selector: 'app-field',
	templateUrl: './field.component.html',
	styleUrls: ['./field.component.css'],
	imports: [
		MatFormField,
		MatInput,
		ReactiveFormsModule,
		MatIcon,
		MatIconButton,
		MatMenuModule,
		MatSelect,
		MatOption,
		MatCheckbox,
		MatRadioModule,
		DateComponent,
		DateSelectComponent,
		CheckboxGroupComponent,
		AutoCompleteComponent,
		FileUploadComponent,
		LocalizeMapPipe,
		WorkflowStatusComponent,
		WorkflowStatusImportantPipe,
		WorkflowStatusNotImportantPipe,
		LocalizeFieldModelPipe
	]
})
export class FieldComponent implements OnInit {
	readonly field = model.required<CRFField>();
	readonly cell = input.required<Cell>();
	readonly disabled = input(false);

	fieldModelType = FieldModelType;
	workflowableEntity = WorkflowableEntity;

	control = new FormControl('');

	readonly debug = signal<string | undefined>(undefined);
	readonly displayAuditTrail = signal(false);

	constructor(
		private crfService: CRFService,
		private authStateService: AuthStateService,
		private fieldUpdateService: FieldUpdateService,
		private workflowableUpdateService: WorkflowableUpdateService,
		private workflowActionService: WorkflowActionService,
		private administrationService: AdministrationService,
		private dialog: MatDialog,
		private destroyRef: DestroyRef
	) {
		effect(() => {
			const field = this.field();
			const value = this.crfService.typeFieldValue(field.model, field.value);
			this.control.reset(value);
			//add the max length validator on top of the max length attribute in HTML
			//the HTML attribute will enforce the max length in the UI
			//the validator will enforce the max length in the form control
			if(field.model.maxLength) {
				this.control.addValidators(Validators.maxLength(field.model.maxLength));
			}
			if(this.getDisabled()) {
				this.control.disable();
			}
			else {
				this.control.enable();
			}
		});
	}

	ngOnInit() {
		//subscribe to value change only after the initial value is set
		this.control.valueChanges.pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(value => {
			const fieldValue = this.crfService.buildFieldValue(this.field().model, value);
			const fieldValueLabel = this.crfService.buildFieldValueLabel(this.field().model, value);
			this.fieldUpdateService.updateField(this.field(), fieldValue, fieldValueLabel);
		});

		this.authStateService.listenConnectedUser().pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(user => {
			this.displayAuditTrail.set(user?.roles.some(r => r.profile.features.includes(FeatureStatic.VIEW_AUDIT_TRAIL)) ?? false);
		});

		this.administrationService.isInDebug().subscribe(debug => {
			if(debug) {
				const fieldId = `${this.field().datasetModelId}/${this.field().modelId}`;
				const fieldContainers = [];
				fieldContainers.push(`scope=${this.field().scopePk}`);
				if(this.field().eventPk) {
					fieldContainers.push(`event=${this.field().eventPk}`);
				}
				fieldContainers.push(`dataset=${this.field().datasetPk}`);
				fieldContainers.push(`field=${this.field().pk}`);
				this.debug.set(`${fieldId} (${fieldContainers.join(', ')})`);
			}
			else {
				this.debug.set(undefined);
			}
		});
	}

	//remember that pending multiple dataset instances don't have pk wile the form has not been saved
	readonly id = computed(() => `dataset-${this.field().datasetId}-field-${this.field().modelId}`);

	readonly style = computed(() => {
		const importantStatuses = this.field().workflowStatuses.filter(s => s.state.important);
		if(importantStatuses.length === 0) {
			return {} as Record<string, string>;
		}
		const color = importantStatuses[0].state.color;
		return {
			backgroundColor: `${color + 15}`,
			border: `1px solid ${color}`
		};
	});

	readonly hasActions = computed(() =>
		this.field().possibleWorkflows.length > 0 || this.field().workflowStatuses.filter(s => !s.state.important).some(s => s.state.possibleActions.length > 0)
	);

	readonly creationActions = computed<WorkflowAction[]>(() =>
		this.field().possibleWorkflows.map(workflow => {
			return workflow.actions.find(a => a.id === workflow.actionId) as WorkflowAction;
		})
	);

	readonly getDisabled = computed(() => this.field().model.dynamic || this.field().model.readOnly || this.disabled());

	get indeterminate(): boolean {
		return typeof this.control.value !== 'boolean';
	}

	isEmptyObject(object: any): boolean {
		return EmptyObjectCheck.isEmptyObject(object);
	}

	initializeWorkflow(action: WorkflowAction) {
		this.workflowActionService.createOnField(this.field(), action).subscribe(newField => {
			this.field.update(f => ({...newField, shown: f.shown, error: f.error}));
		});
	}

	updateWorkflow(status: WorkflowStatus, action: WorkflowAction) {
		this.workflowActionService.executeActionOnField(this.field(), status, action).subscribe(newField => {
			this.field.update(f => ({...newField, shown: f.shown, error: f.error}));
			this.workflowableUpdateService.emitUpdatedWorkflowable(WorkflowableEntity.FIELD, newField);
		});
	}

	onActionResponse(newField: Workflowable) {
		this.field.update(f => ({...newField as Field, shown: f.shown, error: f.error}));
	}

	openHelp(fieldModel: FieldModel) {
		return this.dialog
			.open(FieldModelHelpComponent, {data: fieldModel})
			.afterClosed();
	}

	openAuditTrail() {
		return this.dialog
			.open(AuditTrailFieldComponent, {data: this.field()})
			.afterClosed();
	}
}
