import {Component, DestroyRef, Input, OnInit} from '@angular/core';
import {Validators, FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {forkJoin} from 'rxjs';
import {ScopeModelDTO} from '@core/model/scope-model-dto';
import {ScopeDTO} from '@core/model/scope-dto';
import {ScopeRelationDTO} from '@core/model/scope-relation-dto';
import {NotificationService} from 'src/app/services/notification.service';
import {ScopeRelationsService} from '@core/services/scope-relations.service';
import {ScopeRelationCreationDTO} from '@core/model/scope-relation-creation-dto';
import {DateUTCPipe} from '../../pipes/date-utc.pipe';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatInput} from '@angular/material/input';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatCard, MatCardActions, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatTableModule} from '@angular/material/table';
import {ScopeCodeShortnamePipe} from 'src/app/pipes/scope-code-shortname.pipe';
import {Rights} from '@core/model/rights';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ArraySortPipe} from 'src/app/pipes/sort-array.pipe';
import {ScopePickerComponent} from '../../scope-picker/scope-picker.component';

@Component({
	selector: 'app-scope-parents',
	templateUrl: './scope-parents.component.html',
	styleUrls: ['./scope-parents.component.css'],
	imports: [
		MatTableModule,
		MatLabel,
		MatIcon,
		MatButton,
		ReactiveFormsModule,
		MatCard,
		MatCardHeader,
		MatCardTitle,
		MatCardActions,
		MatCardContent,
		MatFormField,
		MatSelect,
		MatOption,
		MatInput,
		MatDatepickerModule,
		DateUTCPipe,
		ScopeCodeShortnamePipe,
		ArraySortPipe,
		ScopePickerComponent
	]
})
export class ScopeParentsComponent implements OnInit {
	@Input() scopeModel: ScopeModelDTO;
	@Input() scope: ScopeDTO;

	scopeRelations: ScopeRelationDTO[] = [];

	displayedColumns = [
		'scope',
		'startDate',
		'stopDate',
		'default',
		'actions'
	];

	addParentForm = new FormGroup({
		parentPk: new FormControl<number | undefined>(undefined, {
			validators: [Validators.required]
		}),
		startDate: new FormControl(new Date(), {
			validators: [Validators.required]
		})
	});

	transferForm = new FormGroup({
		parentPk: new FormControl<number | undefined>(undefined, {
			validators: [Validators.required]
		})
	});

	allParentScopes: ScopeDTO[] = [];
	parentScopes: ScopeDTO[] = [];
	transferParentScopes: ScopeDTO[] = [];

	constructor(
		private scopeRelationsService: ScopeRelationsService,
		private notificationService: NotificationService,
		private destroyRef: DestroyRef
	) { }

	ngOnInit(): void {
		forkJoin({
			allParentScopes: this.scopeRelationsService.getParents(this.scopeModel.id, Rights.WRITE, false),
			scopeRelations: this.scopeRelationsService.getParentRelations(this.scope.pk)
		}).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(({allParentScopes, scopeRelations}) => {
			this.scopeRelations = scopeRelations;
			this.allParentScopes = allParentScopes;
			this.updateParentScopes();
		});
	}

	addParent() {
		const scopeRelationCreation = this.addParentForm.value as ScopeRelationCreationDTO;

		this.scopeRelationsService.createScopeRelation(this.scope.pk, scopeRelationCreation).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: parentRelations => {
				//Update the parent scopes
				this.scopeRelations = parentRelations;

				//Remove the actual parents from the potential parent list
				this.updateParentScopes();

				this.notificationService.showSuccess('Parent added');

				//Reset the form
				this.addParentForm.reset();
			},
			error: response => {
				this.notificationService.showError(response.error.message);
			}
		});
	}

	transfer() {
		const parentPk = this.transferForm.value.parentPk;
		const scopeRelationCreation = {parentPk, startDate: new Date()} as ScopeRelationCreationDTO;

		this.scopeRelationsService.transfer(this.scope.pk, scopeRelationCreation).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: parentRelations => {
				//Update the parent scopes
				this.scopeRelations = parentRelations;

				//Remove the actual parents from the potential parent list
				this.updateParentScopes();

				//Notify the user
				const newParentRelation = parentRelations.find(s => s.parent.pk === parentPk);
				this.notificationService.showSuccess(`Transferred to ${newParentRelation?.parent.shortname}`);

				//Reset the form
				this.transferForm.reset();
			},
			error: response => {
				this.notificationService.showError(response.error.message);
			}
		});
	}

	endRelation(relationPk: number) {
		this.scopeRelationsService.endRelation(this.scope.pk, relationPk, new Date()).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: parentRelations => {
				//Update the parent scopes
				this.scopeRelations = parentRelations;

				//Remove the actual parents from the potential parent list
				this.updateParentScopes();

				//Notify the user
				this.notificationService.showSuccess('Relation ended');

				//Reset the form
				this.addParentForm.reset();
			},
			error: response => {
				this.notificationService.showError(response.error.message);
			}
		});
	}

	updateParentScopes() {
		const currentParentScopePks = this.scopeRelations
			.filter(rel => this.isCurrent(rel))
			.map(rel => rel.parent.pk);
		this.parentScopes = this.allParentScopes.filter(s => !currentParentScopePks.includes(s.pk));
		const defaultParentScopeModelId = this.scopeModel.defaultParentId;
		this.transferParentScopes = this.parentScopes.filter(s => s.modelId === defaultParentScopeModelId);
	}

	isCurrent(scopeRelation: ScopeRelationDTO): boolean {
		return !scopeRelation.stopDate || scopeRelation.stopDate.getTime() > new Date().getTime();
	}
}
