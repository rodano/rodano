import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder, Validators, ReactiveFormsModule} from '@angular/forms';
import {ScopeModelDTO} from '@core/model/scope-model-dto';
import {ScopeDTO} from '@core/model/scope-dto';
import {ScopeService} from '@core/services/scope.service';
import {NotificationService} from 'src/app/services/notification.service';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatButton} from '@angular/material/button';
import {MatFormField, MatInput, MatLabel} from '@angular/material/input';
import {AuditTrailButtonComponent} from 'src/app/audit-trail-button/audit-trail-button.component';
import {MatDialog} from '@angular/material/dialog';
import {DeleteRestoreComponent} from 'src/app/crf/dialogs/delete-restore/delete-restore.component';
import {of, switchMap} from 'rxjs';
import {WorkflowStatusComponent} from 'src/app/crf/workflow-status/workflow-status.component';
import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {WorkflowableDTO} from '@core/utilities/workflowable-dto';

@Component({
	templateUrl: './scope-settings.component.html',
	styleUrls: ['./scope-settings.component.css'],
	imports: [
		ReactiveFormsModule,
		MatFormField,
		MatLabel,
		MatInput,
		MatButton,
		MatDatepickerModule,
		AuditTrailButtonComponent,
		WorkflowStatusComponent
	]
})
export class ScopeSettingsComponent implements OnInit {
	workflowableEntity = WorkflowableEntity;

	@Input() scopeModel: ScopeModelDTO;
	@Input() scope: ScopeDTO;

	scopeUpdateForm = this.formBuilder.group({
		code: ['', [Validators.required]],
		shortname: ['', [Validators.required]],
		longname: ['', []],
		maxNumber: [0, []],
		expectedNumber: [0, []],
		startDate: [new Date(), [Validators.required]],
		stopDate: [new Date(), []]
	});

	constructor(
		private formBuilder: FormBuilder,
		private scopeService: ScopeService,
		private notificationService: NotificationService,
		private dialog: MatDialog
	) { }

	ngOnInit() {
		this.updateForm();
	}

	updateForm() {
		this.scopeUpdateForm.reset(this.scope);
		if(this.scope.removed) {
			this.scopeUpdateForm.disable();
		}
		else {
			this.scopeUpdateForm.enable();
		}
	}

	save() {
		const updatedScope = {...this.scope, ...this.scopeUpdateForm.value} as ScopeDTO;
		this.scopeService.save(this.scope.pk, updatedScope).subscribe(scope => {
			Object.assign(this.scope, scope);
			this.scopeUpdateForm.reset(this.scope);
			this.notificationService.showSuccess('Modifications saved');
		});
	}

	remove() {
		return this.dialog
			.open(DeleteRestoreComponent, {data: true})
			.afterClosed()
			.pipe(
				switchMap((rationale?: string) => {
					if(rationale) {
						return this.scopeService.remove(this.scope.pk, rationale);
					}
					return of(undefined);
				})
			)
			.subscribe({
				next: scope => {
					if(scope) {
						this.scope.removed = scope.removed;
						this.updateForm();
						this.notificationService.showSuccess('Scope removed');
					}
				},
				error: response => {
					this.notificationService.showError(response.error.message);
				}
			});
	}

	restore() {
		return this.dialog
			.open(DeleteRestoreComponent, {data: false})
			.afterClosed()
			.pipe(
				switchMap((rationale?: string) => {
					if(rationale) {
						return this.scopeService.restore(this.scope.pk, rationale);
					}
					return of(undefined);
				})
			)
			.subscribe({
				next: scope => {
					if(scope) {
						this.scope.removed = scope.removed;
						this.updateForm();
						this.notificationService.showSuccess('Scope restored');
					}
				},
				error: response => {
					this.notificationService.showError(response.error.message);
				}
			});
	}

	lock() {
		this.scopeService.lock(this.scope.pk).subscribe(scope => {
			this.scope.locked = scope.locked;
			this.notificationService.showSuccess('Scope locked');
		});
	}

	unlock() {
		this.scopeService.unlock(this.scope.pk).subscribe(scope => {
			this.scope.locked = scope.locked;
			this.notificationService.showSuccess('Scope unlocked');
		});
	}

	onWorkflowExecution(newScope: WorkflowableDTO) {
		this.scope = newScope as ScopeDTO;
	}
}
