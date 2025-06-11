import {Component, DestroyRef, Input, OnChanges, OnInit} from '@angular/core';
import {CellDTO} from '@core/model/cell-dto';
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
import {FieldModelDTO} from '@core/model/field-model-dto';
import {FieldModelHelpComponent} from '../../dialogs/field-model-help/field-model-help.component';
import {EmptyObjectCheck} from 'src/app/utils/empty-object-check';
import {AuditTrailFieldComponent} from 'src/app/audit-trail-field/audit-trail-field.component';
import {AuthStateService} from 'src/app/services/auth-state.service';
import {MatMenuModule} from '@angular/material/menu';
import {WorkflowStatusComponent} from '../../workflow-status/workflow-status.component';
import {LocalizeFieldModelPipe} from 'src/app/pipes/localize-field-model.pipe';
import {WorkflowActionDTO} from '@core/model/workflow-action-dto';
import {WorkflowActionService} from '../../services/workflow-action.service';
import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {WorkflowableDTO} from '@core/utilities/workflowable-dto';
import {AdministrationService} from '@core/services/administration.service';
import {MatIconButton} from '@angular/material/button';
import {WorkflowStatusImportantPipe} from 'src/app/pipes/workflow-status-important.pipe';
import {WorkflowStatusDTO} from '@core/model/workflow-status-dto';
import {WorkflowStatusNotImportantPipe} from 'src/app/pipes/workflow-status-not-important';
import {FeatureStatic} from '@core/model/feature-static';

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
export class FieldComponent implements OnInit, OnChanges {
	@Input() field: CRFField;
	@Input() cell: CellDTO;
	@Input() disabled: boolean;

	fieldModelType = FieldModelType;
	workflowableEntity = WorkflowableEntity;

	control = new FormControl('');

	debug = undefined as string | undefined;
	displayAuditTrail = false;

	constructor(
		private crfService: CRFService,
		private authStateService: AuthStateService,
		private fieldUpdateService: FieldUpdateService,
		private workflowActionService: WorkflowActionService,
		private administrationService: AdministrationService,
		private dialog: MatDialog,
		private destroyRef: DestroyRef
	) {
	}

	ngOnInit() {
		//subscribe to value change only after the initial value is set
		this.control.valueChanges.pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(value => {
			const fieldValue = this.crfService.buildFieldValue(this.field.model, value);
			const fieldValueLabel = this.crfService.buildFieldValueLabel(this.field.model, value);
			this.fieldUpdateService.updateField(this.field, fieldValue, fieldValueLabel);
		});

		this.authStateService.listenConnectedUser().pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(user => {
			this.displayAuditTrail = user?.roles.some(r => r.profile.features.includes(FeatureStatic.VIEW_AUDIT_TRAIL)) ?? false;
		});

		this.administrationService.isInDebug().subscribe(debug => {
			if(debug) {
				const fieldId = `${this.field.datasetModelId}/${this.field.modelId}`;
				const fieldContainers = [];
				fieldContainers.push(`scope=${this.field.scopePk}`);
				if(this.field.eventPk) {
					fieldContainers.push(`event=${this.field.eventPk}`);
				}
				fieldContainers.push(`dataset=${this.field.datasetPk}`);
				fieldContainers.push(`field=${this.field.pk}`);
				this.debug = `${fieldId} (${fieldContainers.join(', ')})`;
			}
			else {
				this.debug = undefined;
			}
		});
	}

	ngOnChanges() {
		const value = this.crfService.typeFieldValue(this.field.model, this.field.value);
		this.control.reset(value);
		//add the max length validator on top of the max length attribute in HTML
		//the HTML attribute will enforce the max length in the UI
		//the validator will enforce the max length in the form control
		if(this.field.model.maxLength) {
			this.control.addValidators(Validators.maxLength(this.field.model.maxLength));
		}
		if(this.getDisabled()) {
			this.control.disable();
		}
		else {
			this.control.enable();
		}
	}

	get id(): string {
		//remember that pending multiple dataset instances don't have pk wile the form has not been saved
		return `dataset-${this.field.datasetId}-field-${this.field.modelId}`;
	}

	get style(): Record<string, string> {
		const importantStatuses = this.field.workflowStatuses.filter(s => s.state.important);
		if(importantStatuses.length === 0) {
			return {};
		}
		const color = importantStatuses[0].state.color;
		return {
			backgroundColor: `${color + 15}`,
			border: `1px solid ${color}`
		};
	}

	get hasActions(): boolean {
		return this.field.possibleWorkflows.length > 0 || this.field.workflowStatuses.filter(s => !s.state.important).some(s => s.state.possibleActions.length > 0);
	}

	get creationActions(): WorkflowActionDTO[] {
		return this.field.possibleWorkflows.map(workflow => {
			return workflow.actions.find(a => a.id === workflow.actionId) as WorkflowActionDTO;
		});
	}

	getDisabled(): boolean {
		return this.field.model.dynamic || this.field.model.readOnly || this.disabled;
	}

	isEmptyObject(object: any): boolean {
		return EmptyObjectCheck.isEmptyObject(object);
	}

	initializeWorkflow(action: WorkflowActionDTO) {
		this.workflowActionService.createOnField(this.field, action).subscribe(newField => {
			Object.assign(this.field, newField);
		});
	}

	updateWorkflow(status: WorkflowStatusDTO, action: WorkflowActionDTO) {
		this.workflowActionService.executeActionOnField(this.field, status, action).subscribe(newField => {
			Object.assign(this.field, newField);
		});
	}

	onActionResponse(newField: WorkflowableDTO) {
		Object.assign(this.field, newField);
	}

	openHelp(fieldModel: FieldModelDTO) {
		return this.dialog
			.open(FieldModelHelpComponent, {data: fieldModel})
			.afterClosed();
	}

	openAuditTrail() {
		return this.dialog
			.open(AuditTrailFieldComponent, {data: this.field})
			.afterClosed();
	}
}
