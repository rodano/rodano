import {Component, DestroyRef, Input, OnChanges, OnInit} from '@angular/core';
import {Validators, FormControl, FormGroup, ReactiveFormsModule, FormArray} from '@angular/forms';
import {forkJoin} from 'rxjs';
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
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FieldModelCriterion} from '@core/model/field-model-criterion';
import {ScopeMini} from '@core/model/scope-mini';
import {FieldModel} from '@core/model/field-model';
import {ConfigurationService} from '@core/services/configuration.service';
import {MeService} from '@core/services/me.service';
import {Operator} from '@core/model/operator';
import {operatorByType} from '@core/enums/operator-by-type';
import {PossibleValue} from '@core/model/possible-value';
import {LocalizeMapPipe} from 'src/app/pipes/localize-map.pipe';
import {CapitalizeFirstPipe} from 'src/app/pipes/capitalize-first.pipe';
import {EnrollmentModel} from '@core/model/enrollment-model';
import {EnrollmentType} from '@core/model/enrollment-type';
import {ScopeService} from '@core/services/scope.service';
import {NotificationService} from 'src/app/services/notification.service';

@Component({
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
		LocalizeMapPipe,
		CapitalizeFirstPipe
	]
})
export class ScopeEnrollmentComponent implements OnInit, OnChanges {
	@Input() scopeModel: ScopeModel;
	@Input() scope: Scope;

	//customization form
	criteria = new FormArray([] as FormArray[]);
	enrollmentForm = new FormGroup({
		type: new FormControl<EnrollmentType | null>(null),
		system: new FormControl(false, {nonNullable: true}),
		rootScopeIds: new FormControl([] as number[], {nonNullable: true}),
		criteria: this.criteria
	}) as FormGroup;

	scopeModels: ScopeModel[] = [];
	rootScopes: ScopeMini[] = [];
	fieldModels: FieldModel[];
	enrollmentTypes: EnrollmentType[] = [EnrollmentType.AUTOMATIC, EnrollmentType.MANUAL];

	constructor(
		private configurationService: ConfigurationService,
		private scopeService: ScopeService,
		private meService: MeService,
		private notificationService: NotificationService,
		private destroyRef: DestroyRef
	) { }

	ngOnInit() {
		//virtual scope model must have exactly one child scope model, which is the one to use for enrollment configuration
		const childScopeModelId = this.scopeModel.childScopeModelIds[0];
		forkJoin({
			scopeModels: this.configurationService.getScopeModelsSorted(),
			fieldModels: this.configurationService.getScopeModelFieldModels(childScopeModelId),
			rootScopes: this.meService.getScopes(undefined, true, false)
		}).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(({scopeModels, fieldModels, rootScopes}) => {
			this.scopeModels = scopeModels;
			this.fieldModels = fieldModels;
			this.rootScopes = rootScopes;
			this.reset();
		});
	}

	ngOnChanges() {
		this.reset();
	}

	getScopes(modelId: string): ScopeMini[] {
		return this.rootScopes?.filter(s => s.modelId === modelId) ?? [];
	}

	getControl(i: number, j: number): FormControl {
		const criterion = this.criteria.controls[i] as FormArray;
		return criterion.controls[j] as FormControl;
	}

	getFieldModel(index: number): FieldModel {
		const criterion = this.criteria.controls[index] as FormArray;
		const fieldModelId = criterion.controls[0].value;
		return this.fieldModels?.find(f => f.id === fieldModelId) as FieldModel;
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

	addCriterion() {
		const criterion = new FormArray([
			new FormControl('', Validators.required),
			new FormControl('', Validators.required),
			new FormControl('', Validators.required)
		]);
		this.criteria.push(criterion);
	}

	deleteCriterion(index: number) {
		this.criteria.removeAt(index);
	}

	reset() {
		this.enrollmentForm.reset();
		const type = this.scope.enrollmentModel?.type ?? null;
		const system = this.scope.enrollmentModel?.system ?? false;
		const rootScopeIds = this.scope.enrollmentModel?.scopesContainerIds ?? [];
		const criteria = this.scope.enrollmentModel?.criteria ?? [];
		this.enrollmentForm.get('system')?.setValue(system);
		this.enrollmentForm.get('type')?.setValue(type);
		this.enrollmentForm.get('rootScopeIds')?.setValue(rootScopeIds);
		this.criteria.clear();
		criteria.forEach(c => {
			const criterion = new FormArray([
				new FormControl(c.fieldModelId, Validators.required),
				new FormControl(c.operator, Validators.required),
				new FormControl(c.value, Validators.required)
			]);
			this.criteria.push(criterion);
		});
	}

	save() {
		const type = this.enrollmentForm.get('type')?.value;
		const system = this.enrollmentForm.get('system')?.value ?? false;
		const scopes = !system ? (this.enrollmentForm.get('rootScopeIds')?.value ?? []) as string[] : [];
		const criteria = this.criteria.controls.map((criterion: FormArray, index: number) => {
			const fieldModel = this.getFieldModel(index);
			return {
				datasetModelId: fieldModel.datasetModelId,
				fieldModelId: fieldModel.id,
				operator: criterion.controls[1].value,
				value: criterion.controls[2].value
			} as FieldModelCriterion;
		});

		const enrollmentModel = {
			type: type,
			system: system,
			scopesContainerIds: scopes,
			criteria: criteria
		} as EnrollmentModel;

		const updatedScope = {
			...this.scope,
			enrollmentModel
		} as Scope;

		this.scopeService.save(this.scope.pk, updatedScope).subscribe(scope => {
			Object.assign(this.scope, scope);
			this.notificationService.showSuccess('Enrollment model saved');
		});
	}
}
