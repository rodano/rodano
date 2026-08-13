import {Component, DestroyRef, OnInit, effect, input, model, signal, untracked} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {forkJoin, of} from 'rxjs';
import {Form} from '@core/model/form';
import {Layout} from '@core/model/layout';
import {FormService} from '@core/services/form.service';
import {filter, finalize, switchMap} from 'rxjs/operators';
import {CRFService} from '../services/crf.service';
import {CellLoadingService} from '../services/cell-loading.service';
import {NotificationService} from '../../services/notification.service';
import {BlockingErrors} from '@core/model/blocking-errors';
import {CRFDataset} from '../models/crf-dataset';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatButton} from '@angular/material/button';
import {LayoutComponent} from '../layout/layout.component';
import {Scope} from '@core/model/scope';
import {Event} from '@core/model/event';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {MultipleLayoutComponent} from '../multiple-layout/multiple-layout.component';
import {WorkflowStatusComponent} from '../workflow-status/workflow-status.component';
import {WorkflowStatus} from '@core/model/workflow-status';
import {CRFField} from '../models/crf-field';
import {LayoutType} from '@core/model/layout-type';
import {FieldUpdateService} from '../services/field-update.service';
import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {AuditTrailButtonComponent} from '../../audit-trail-button/audit-trail-button.component';
import {WorkflowableUpdateService} from '../services/workflowable-update.service';
import {MatProgressBar} from '@angular/material/progress-bar';
import {Workflowable} from '@core/utilities/workflowable';

@Component({
	selector: 'app-form',
	templateUrl: './form.component.html',
	styleUrls: ['./form.component.css'],
	imports: [
		FormsModule,
		MatButton,
		MatProgressBar,
		LocalizeMapPipe,
		MultipleLayoutComponent,
		LayoutComponent,
		WorkflowStatusComponent,
		AuditTrailButtonComponent
	]
})
export class FormComponent implements OnInit {
	readonly scope = input.required<Scope>();
	//event may be null for form attached directly to the scope
	readonly event = input<Event>();
	//using a writable signal so it can be updated when the form is updated
	//however, this gives the illusion that this will be propagated to the route resolver, which is not the case
	//the route resolver will still have the old form reference until a new resolve process is triggered
	//this means other components in the same router-outlet will not see the updated form until a new resolve is triggered, which can be confusing
	//maybe at some point we will have "withComponentModelBinding" instead of "withComponentInputBinding"
	readonly form = model.required<Form>();

	layoutType = LayoutType;
	workflowableEntity = WorkflowableEntity;

	readonly layouts = signal<Layout[]>([]);
	readonly datasets = signal<CRFDataset[]>([]);

	//dedicated signal so it can be refreshed without triggering a full form reload
	readonly workflowStatuses = signal<WorkflowStatus[]>([]);

	//distinguish between loading the form (all the cells are being initialized) vs saving the form
	readonly formLoading = signal(false);
	readonly saveLoading = signal(false);
	readonly dirty = signal(false);

	constructor(
		private crfService: CRFService,
		private cellLoadingService: CellLoadingService,
		private formService: FormService,
		private notificationService: NotificationService,
		private fieldUpdateService: FieldUpdateService,
		private workflowableUpdateService: WorkflowableUpdateService,
		private destroyRef: DestroyRef
	) {
		effect(() => {
			this.scope();
			this.event();
			this.form();
			untracked(() => {
				this.workflowStatuses.set(this.form().workflowStatuses);
				this.reloadContent();
			});
		});
	}

	ngOnInit() {
		//when any workflowable is updated, refresh only the workflow statuses to preserve user input in the form
		this.workflowableUpdateService.updatedWorkflowable$.pipe(
			switchMap(typedWorkflowable => {
				if(this.workflowableUpdateService.match(typedWorkflowable, WorkflowableEntity.FORM, this.form().pk)) {
					return of(typedWorkflowable.workflowable as Form);
				}
				//if not, refetch it to get the latest status
				return this.formService.get(this.scope().pk, this.event()?.pk, this.form().pk);
			}),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(form => {
			this.workflowStatuses.set(form.workflowStatuses);
		});

		this.fieldUpdateService.fieldUpdated$
			.pipe(
				takeUntilDestroyed(this.destroyRef),
				filter(() => !this.formLoading() && !this.saveLoading())
			).subscribe(() => {
				this.dirty.set(true);
			});
		this.cellLoadingService.allCellsLoaded$
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe(() => {
				this.formLoading.set(false);
			});
	}

	reloadContent() {
		this.formLoading.set(true);
		//reset the layouts and datasets parameters to force Angular to re-create the child components
		//otherwise, child components may be re-used (for example if the same form is displayed but for different events)
		//in that case, the child components (layouts and cells) will "stay the same" and Angular will not re-create them
		//however, the new cells will be registered in the cell loading service, but the cells will never emit the "finished loading" event
		//so the form will never finish loading
		this.layouts.set([]);
		this.datasets.set([]);
		forkJoin({
			layouts: this.formService.getLayouts(this.scope().pk, this.event()?.pk, this.form().pk),
			datasets: this.crfService.getCRFDatasets(this.form())
		})
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe(({layouts, datasets}) => {
				this.layouts.set(layouts);
				this.datasets.set(datasets);
				this.cellLoadingService.registerFormCells(this.layouts(), this.datasets());
				this.dirty.set(false);
			});
	}

	//when saving datasets, the form is not reloaded from the perspective of Angular
	//the child components will not be destroyed and re-created
	saveDatasets() {
		this.saveLoading.set(true);
		this.crfService.saveCRFDatasets(this.form(), this.layouts(), this.datasets())
			.pipe(
				takeUntilDestroyed(this.destroyRef),
				finalize(() => {
					this.saveLoading.set(false);
				}),
				switchMap(() => {
					//refresh form, especially to update the workflow statuses
					return this.formService.get(this.scope().pk, this.event()?.pk, this.form().pk);
				})
			).subscribe({
				next: newForm => {
					this.form.set(newForm);
					//resetting the form (hence reloading the form content) is required because its layouts may have been updated
					//also remember that only write-access datasets are submitted and returned by the "save" API
					//the other read-only need to be refreshed in case they have been updated
					//used by the side menu to refresh the entities
					this.workflowableUpdateService.emitUpdatedWorkflowable(WorkflowableEntity.FORM, this.form());
					this.notificationService.showSuccess('Form saved');
				},
				error: (response: any) => {
					const result = response.error as BlockingErrors;
					result.errors.forEach(error => {
						const dataset = this.datasets().find(d => d.id === error.datasetId) as CRFDataset;
						const field = dataset.fields.find(f => f.modelId === error.fieldModelId) as CRFField;
						field.error.set(error.message);
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

	onWorkflowExecution(newForm: Workflowable) {
		this.form.set(newForm as Form);
	}
}
