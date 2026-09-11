import {Component, DestroyRef, computed, effect, inject, input, signal} from '@angular/core';
import {Validators, FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {forkJoin} from 'rxjs';
import {ScopeModel} from '@core/model/scope-model';
import {ScopeRelation} from '@core/model/scope-relation';
import {NotificationService} from '../../services/notification.service';
import {ScopeRelationsService} from '@core/services/scope-relations.service';
import {ScopeRelationCreation} from '@core/model/scope-relation-creation';
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
import {ScopeCodeShortnamePipe} from '../../pipes/scope-code-shortname.pipe';
import {Rights} from '@core/model/rights';
import {RightEntity} from '@core/enums/right-entity';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ArraySortPipe} from '../../pipes/sort-array.pipe';
import {ScopePickerComponent} from '../../scope-picker/scope-picker.component';
import {SCOPE_TOKEN} from '../home/scope.component';
import {MeService} from '@core/services/me.service';
import {ScopeMini} from '@core/model/scope-mini';

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
export class ScopeParentsComponent {
	readonly scope = inject(SCOPE_TOKEN);
	readonly scopeModel = input.required<ScopeModel>();

	readonly scopeRelations = signal<ScopeRelation[]>([]);

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

	private readonly allParentScopes = signal<ScopeMini[]>([]);
	readonly parentScopes = computed(() => {
		const currentParentScopePks = this.scopeRelations()
			.filter(rel => this.isCurrent(rel))
			.map(rel => rel.parent.pk);
		return this.allParentScopes().filter(s => !currentParentScopePks.includes(s.pk));
	});

	readonly transferParentScopes = computed(() =>
		this.parentScopes().filter(s => s.modelId === this.scopeModel().defaultParentId)
	);

	constructor(
		private meService: MeService,
		private scopeRelationsService: ScopeRelationsService,
		private notificationService: NotificationService,
		private destroyRef: DestroyRef
	) {
		effect(() => {
			forkJoin({
				allParentScopes: this.meService.getScopesForRequiredRight(RightEntity.SCOPE_MODEL, this.scopeModel().id, Rights.WRITE, this.scopeModel().parentIds),
				scopeRelations: this.scopeRelationsService.getParentRelations(this.scope().pk)
			}).pipe(
				takeUntilDestroyed(this.destroyRef)
			).subscribe(({allParentScopes, scopeRelations}) => {
				this.scopeRelations.set(scopeRelations);
				this.allParentScopes.set(allParentScopes);
			});
		});
	}

	addParent() {
		const scopeRelationCreation = this.addParentForm.value as ScopeRelationCreation;

		this.scopeRelationsService.createScopeRelation(this.scope().pk, scopeRelationCreation).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: parentRelations => {
				this.scopeRelations.set(parentRelations);
				this.notificationService.showSuccess('Parent added');
				this.addParentForm.reset();
			},
			error: response => {
				this.notificationService.showError(response.error.message);
			}
		});
	}

	transfer() {
		const parentPk = this.transferForm.value.parentPk;
		const scopeRelationCreation = {parentPk, startDate: new Date()} as ScopeRelationCreation;

		this.scopeRelationsService.transfer(this.scope().pk, scopeRelationCreation).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: parentRelations => {
				this.scopeRelations.set(parentRelations);
				const newParentRelation = parentRelations.find(s => s.parent.pk === parentPk);
				this.notificationService.showSuccess(`Transferred to ${newParentRelation?.parent.shortname}`);
				this.transferForm.reset();
			},
			error: response => {
				this.notificationService.showError(response.error.message);
			}
		});
	}

	endRelation(relationPk: number) {
		this.scopeRelationsService.endRelation(this.scope().pk, relationPk, new Date()).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: parentRelations => {
				this.scopeRelations.set(parentRelations);
				this.notificationService.showSuccess('Relation ended');
				this.addParentForm.reset();
			},
			error: response => {
				this.notificationService.showError(response.error.message);
			}
		});
	}

	isCurrent(scopeRelation: ScopeRelation): boolean {
		return !scopeRelation.stopDate || scopeRelation.stopDate.getTime() > new Date().getTime();
	}
}
