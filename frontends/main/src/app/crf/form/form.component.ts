import {Component, Input, OnChanges, DestroyRef, OnInit} from '@angular/core';
import {forkJoin} from 'rxjs';
import {FormDTO} from '@core/model/form-dto';
import {LayoutDTO} from '@core/model/layout-dto';
import {FormService} from '@core/services/form.service';
import {filter, finalize, switchMap} from 'rxjs/operators';
import {CRFService} from '../services/crf.service';
import {CellLoadingService} from '../services/cell-loading.service';
import {NotificationService} from 'src/app/services/notification.service';
import {BlockingErrorsDTO} from '@core/model/blocking-errors-dto';
import {CRFDataset} from '../models/crf-dataset';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatButton} from '@angular/material/button';
import {LayoutComponent} from '../layout/layout.component';
import {ScopeDTO} from '@core/model/scope-dto';
import {EventDTO} from '@core/model/event-dto';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {MultipleLayoutComponent} from '../multiple-layout/multiple-layout.component';
import {WorkflowStatusComponent} from '../workflow-status/workflow-status.component';
import {CRFField} from '../models/crf-field';
import {LayoutType} from '@core/model/layout-type';
import {FieldUpdateService} from '../services/field-update.service';
import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {AuditTrailButtonComponent} from 'src/app/audit-trail-button/audit-trail-button.component';
import {CRFChangeService} from '../services/crf-change.service';
import {MatProgressBar} from '@angular/material/progress-bar';

@Component({
	selector: 'app-form',
	templateUrl: './form.component.html',
	styleUrls: ['./form.component.scss'],
	imports: [
		MatButton,
		MatProgressBar,
		LocalizeMapPipe,
		MultipleLayoutComponent,
		LayoutComponent,
		WorkflowStatusComponent,
		AuditTrailButtonComponent
	]
})
export class FormComponent implements OnInit, OnChanges {
	@Input() scope: ScopeDTO;
	//event may be null form scope attached directly to the scope
	@Input() event?: EventDTO;
	@Input() form: FormDTO;

	layoutType = LayoutType;
	workflowableEntity = WorkflowableEntity;

	layouts: LayoutDTO[];
	datasets: CRFDataset[];

	//distinguish between loading the form (all the cells are being initialized) vs saving the form
	formLoading = false;
	saveLoading = false;
	dirty = false;

	constructor(
		private crfService: CRFService,
		private cellLoadingService: CellLoadingService,
		private formService: FormService,
		private notificationService: NotificationService,
		private fieldUpdateService: FieldUpdateService,
		private crfChangeService: CRFChangeService,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		this.fieldUpdateService.fieldUpdated$
			.pipe(
				takeUntilDestroyed(this.destroyRef),
				filter(() => !this.formLoading && !this.saveLoading)
			).subscribe(() => {
				this.dirty = true;
			});
		this.cellLoadingService.allCellsLoaded$
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe(() => {
				this.formLoading = false;
			});
	}

	ngOnChanges() {
		this.reloadContent();
	}

	reloadContent() {
		this.formLoading = true;
		//reset the layouts and datasets parameters to force Angular to re-create the child components
		//otherwise, child components may be re-used (for example if the same form is displayed but for different events)
		//in that case, the child components (layouts and cells) will "stay the same" and Angular will not re-create them
		//however, the new cells will be registered in the cell loading service, but the cells will never emit the "finished loading" event
		//so the form will never be finish loading
		this.layouts = [];
		this.datasets = [];
		forkJoin({
			layouts: this.formService.getLayouts(this.scope.pk, this.event?.pk, this.form.pk),
			datasets: this.crfService.getCRFDatasets(this.form)
		})
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe(({layouts, datasets}) => {
				this.layouts = layouts;
				this.datasets = datasets;
				this.cellLoadingService.registerFormCells(this.layouts, this.datasets);
				this.dirty = false;
			});
	}

	//when saving datasets, the form is not reloaded from the perspective of Angular
	//the child components will not be destroyed and re-created
	saveDatasets() {
		this.saveLoading = true;
		this.crfService.saveCRFDatasets(this.form, this.datasets)
			.pipe(
				takeUntilDestroyed(this.destroyRef),
				finalize(() => {
					//refreshing the datasets or adding errors (in the next or error functions below) will trigger the refresh of the child components
					//this will be done in another tick by Angular
					//we need to take into account these operations before considering the save as finished
					//remember that the "saveLoading" boolean is used to prevent the dirty state to be updated
					//these refreshing operations may update fields (for example a plugin, or any empty field, for which the value will be set from an empty string to null)
					//this will trigger a "field update" event and set the dirty state to true
					//this is not the expected behavior as the dirty state must only be set to true due to operations made by the user
					//so we need to shift setting the saveLoading state to false by a tick
					setTimeout(() => this.saveLoading = false, 0);
				}),
				switchMap(() => {
					//refresh form, especially to update the workflow statuses
					return this.formService.get(this.scope.pk, this.event?.pk, this.form.pk);
				})
			).subscribe({
				next: newForm => {
					this.form = newForm;
					//reload the form content because its layout may have been updated
					//also remember that only write-access datasets are submitted and returned by the "save" API
					//the other read-only need to be refreshed in case they have been updated
					this.reloadContent();
					//used by the side menu to refresh the entities
					this.crfChangeService.emitUpdatedWorkflowable(WorkflowableEntity.FORM, this.form);
					this.notificationService.showSuccess('Form saved');
				},
				error: (response: any) => {
					const result = response.error as BlockingErrorsDTO;
					result.errors.forEach(error => {
						const dataset = this.datasets.find(d => d.id === error.datasetId) as CRFDataset;
						const field = dataset.fields.find(f => f.modelId === error.fieldModelId) as CRFField;
						field.error = error.message;
					});

					if(result.message) {
						this.notificationService.showError(result.message);
					}
					else {
						this.notificationService.showError('Something went wrong, please try again later');
					}
				}
			});
	}

	onActionResponse(newForm: FormDTO) {
		(this.form as FormDTO).workflowStatuses = (newForm as FormDTO).workflowStatuses;
	}
}
