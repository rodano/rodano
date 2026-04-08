import {ChangeDetectionStrategy, Component, DestroyRef, effect, inject, input, signal} from '@angular/core';
import {Validators, FormControl, FormGroup, ReactiveFormsModule, FormArray} from '@angular/forms';
import {forkJoin, of} from 'rxjs';
import {debounceTime, distinctUntilChanged, startWith, switchMap, catchError} from 'rxjs/operators';
import {ScopeModel} from '@core/model/scope-model';
import {Scope} from '@core/model/scope';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatInput} from '@angular/material/input';
import {MatOption} from '@angular/material/core';
import {MatOptgroup, MatSelect} from '@angular/material/select';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatCheckbox} from '@angular/material/checkbox';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatTableModule} from '@angular/material/table';
import {MatTooltip} from '@angular/material/tooltip';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FieldModelCriterion} from '@core/model/field-model-criterion';
import {ScopeMini} from '@core/model/scope-mini';
import {FieldModel} from '@core/model/field-model';
import {ConfigurationService} from '@core/services/configuration.service';
import {MeService} from '@core/services/me.service';
import {Operator} from '@core/model/operator';
import {operatorByType} from '@core/enums/operator-by-type';
import {PossibleValue} from '@core/model/possible-value';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {CapitalizeFirstPipe} from '../../pipes/capitalize-first.pipe';
import {EnrollmentModel} from '@core/model/enrollment-model';
import {EnrollmentType} from '@core/model/enrollment-type';
import {ScopeService} from '@core/services/scope.service';
import {NotificationService} from '../../services/notification.service';
import {LowerCasePipe} from '@angular/common';
import {ScopeSearch} from '@core/utilities/search/scope-search';
import {DateTimeUTCPipe} from '../../pipes/date-time-utc.pipe';
import {SCOPE_TOKEN} from '../home/scope.component';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	selector: 'app-scope-enrollment',
	templateUrl: './scope-enrollment.component.html',
	styleUrls: ['./scope-enrollment.component.css'],
	imports: [
		MatTableModule,
		MatLabel,
		MatIcon,
		MatButton,
		ReactiveFormsModule,
		MatFormField,
		MatSelect,
		MatOptgroup,
		MatOption,
		MatInput,
		MatDatepickerModule,
		MatCheckbox,
		MatTooltip,
		LocalizeMapPipe,
		LowerCasePipe,
		DateTimeUTCPipe,
		CapitalizeFirstPipe
	]
})
export class ScopeEnrollmentComponent {
	readonly scope = inject(SCOPE_TOKEN);
	readonly scopeModel = input.required<ScopeModel>();

	readonly childScopeModel = signal<ScopeModel | undefined>(undefined);

	//customization form
	criteria = new FormArray([] as FormArray[]);
	enrollmentForm = new FormGroup({
		type: new FormControl<EnrollmentType | null>(null),
		system: new FormControl(false, {nonNullable: true}),
		rootScopeIds: new FormControl([] as number[], {nonNullable: true}),
		criteria: this.criteria
	}) as FormGroup;

	readonly scopeModels = signal<ScopeModel[]>([]);
	readonly rootScopes = signal<ScopeMini[]>([]);
	readonly fieldModels = signal<FieldModel[]>([]);
	enrollmentTypes: EnrollmentType[] = [EnrollmentType.AUTOMATIC, EnrollmentType.MANUAL];
	readonly enrollableCount = signal<number | undefined>(undefined);

	readonly childScopes = signal<Scope[]>([]);
	columnsToDisplay: string[] = ['code', 'shortname', 'startDate', 'stopDate'];

	constructor(
		private configurationService: ConfigurationService,
		private scopeService: ScopeService,
		private meService: MeService,
		private notificationService: NotificationService,
		private destroyRef: DestroyRef
	) {
		effect(() => {
			this.loadChildScopes();
			const childScopeModelId = this.scopeModel().childScopeModelIds[0];
			forkJoin({
				scopeModels: this.configurationService.getScopeModelsSorted(),
				fieldModels: this.configurationService.getScopeModelFieldModels(childScopeModelId),
				childScopeModel: this.configurationService.getScopeModel(childScopeModelId),
				rootScopes: this.meService.getScopes(undefined, true, false)
			}).pipe(
				takeUntilDestroyed(this.destroyRef)
			).subscribe(({scopeModels, fieldModels, childScopeModel, rootScopes}) => {
				this.scopeModels.set(scopeModels);
				this.fieldModels.set(fieldModels);
				this.childScopeModel.set(childScopeModel);
				this.rootScopes.set(rootScopes);
				this.reset();
			});
		});

		this.criteria.valueChanges.pipe(
			debounceTime(500),
			distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
			takeUntilDestroyed(this.destroyRef),
			startWith(this.criteria.value),
			switchMap(() => {
				if(!this.fieldModels().length) {
					return of(undefined);
				}
				if(!this.enrollmentForm.valid) {
					return of(undefined);
				}

				const criteria = this.generateCriteria();
				return this.scopeService.countEnrollable(this.scope().pk, criteria).pipe(
					catchError(() => of(undefined))
				);
			})
		).subscribe(count => {
			this.enrollableCount.set(count);
		});
	}

	getScopes(modelId: string): ScopeMini[] {
		return this.rootScopes().filter(s => s.modelId === modelId) ?? [];
	}

	getControl(i: number, j: number): FormControl {
		const criterion = this.criteria.controls[i] as FormArray;
		return criterion.controls[j] as FormControl;
	}

	getFieldModel(index: number): FieldModel {
		const criterion = this.criteria.controls[index] as FormArray;
		const fieldModelId = criterion.controls[0].value;
		return this.fieldModels().find(f => f.id === fieldModelId) as FieldModel;
	}

	getOperators(index: number): Operator[] {
		const fieldModel = this.getFieldModel(index);
		if(!fieldModel) {
			return [];
		}
		return operatorByType[fieldModel.type];
	}

	getPossibleValues(index: number): PossibleValue[] {
		const fieldModel = this.getFieldModel(index);
		if(!fieldModel) {
			return [];
		}
		return fieldModel.possibleValues;
	}

	addCriterion(fieldId = '', operator = '', value = '') {
		const criterion = new FormArray([
			new FormControl(fieldId, Validators.required),
			new FormControl(operator, Validators.required),
			new FormControl(value, Validators.required)
		]);
		const fieldControl = criterion.controls[0] as FormControl;
		const operatorControl = criterion.controls[1] as FormControl;
		const valueControl = criterion.controls[2] as FormControl;

		fieldControl.valueChanges.pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(() => {
			operatorControl.reset();
			valueControl.reset();
		});
		this.criteria.push(criterion);
	}

	deleteCriterion(index: number) {
		this.criteria.removeAt(index);
	}

	generateCriteria(): FieldModelCriterion[] {
		return this.criteria.controls.map((criterion: FormArray, index: number) => {
			const fieldModel = this.getFieldModel(index);
			return {
				datasetModelId: fieldModel.datasetModelId,
				fieldModelId: fieldModel.id,
				operator: criterion.controls[1].value,
				value: criterion.controls[2].value
			} as FieldModelCriterion;
		});
	}

	reset() {
		this.enrollmentForm.reset();
		const type = this.scope().enrollmentModel?.type ?? null;
		const system = this.scope().enrollmentModel?.system ?? false;
		const rootScopeIds = this.scope().enrollmentModel?.scopesContainerIds ?? [];
		const criteria = this.scope().enrollmentModel?.criteria ?? [];
		this.enrollmentForm.get('type')?.setValue(type);
		this.enrollmentForm.get('system')?.setValue(system);
		this.enrollmentForm.get('rootScopeIds')?.setValue(rootScopeIds);
		this.criteria.clear();
		criteria.forEach(c => this.addCriterion(c.fieldModelId, c.operator, c.value));
	}

	save() {
		const type = this.enrollmentForm.get('type')?.value;
		const system = this.enrollmentForm.get('system')?.value ?? false;
		const rootScopeIds = !system ? (this.enrollmentForm.get('rootScopeIds')?.value ?? []) as string[] : [];
		const criteria = this.generateCriteria();

		const enrollmentModel = {
			type: type,
			system: system,
			scopesContainerIds: rootScopeIds,
			criteria: criteria
		} as EnrollmentModel;

		const updatedScope = {
			...this.scope(),
			enrollmentModel
		} as Scope;

		this.scopeService.save(this.scope().pk, updatedScope).subscribe(scope => {
			this.scope.set(scope);
			this.notificationService.showSuccess('Enrollment model saved');
		});
	}

	canEnroll(): boolean {
		return this.scope().enrollmentModel?.type === EnrollmentType.AUTOMATIC;
	}

	enroll() {
		this.scopeService.enroll(this.scope().pk).subscribe(
			{
				next: () => {
					this.notificationService.showSuccess('All scopes enrolled successfully');
					this.loadChildScopes();
				},
				error: response => {
					console.error(response);
					this.notificationService.showError(`Failed to enroll scopes: ${response.error.message}`);
				}
			}
		);
	}

	unenroll() {
		this.scopeService.unenroll(this.scope().pk).subscribe({
			next: () => {
				this.notificationService.showSuccess('All scopes unenrolled successfully');
				this.loadChildScopes();
			},
			error: response => {
				console.error(response);
				this.notificationService.showError(`Failed to unenroll scopes: ${response.error.message}`);
			}
		});
	}

	loadChildScopes() {
		if(!this.scope()?.pk) {
			return;
		}
		const search = new ScopeSearch();
		search.parentPks = [this.scope().pk];
		this.scopeService.search(search).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(result => {
			this.childScopes.set(result.objects);
		});
	}
}
