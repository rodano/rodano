import {ChangeDetectionStrategy, Component, computed, DestroyRef, OnInit, input, signal} from '@angular/core';
import {forkJoin} from 'rxjs';
import {PossibleValue} from '@core/model/possible-value';
import {operatorByType} from '@core/enums/operator-by-type';
import {ConfigurationService} from '@core/services/configuration.service';
import {FieldModel} from '@core/model/field-model';
import {FieldModelCriterion} from '@core/model/field-model-criterion';
import {CapitalizeFirstPipe} from '../pipes/capitalize-first.pipe';
import {MatIcon} from '@angular/material/icon';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import {MatOption} from '@angular/material/core';
import {MatOptgroup, MatSelect} from '@angular/material/select';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {Operator} from '@core/model/operator';
import {MeService} from '@core/services/me.service';
import {ScopeMini} from '@core/model/scope-mini';
import {ChartWidgetComponent} from '../widgets/chart/chart-widget.component';
import {CMSLayout} from '@core/model/cms-layout';
import {ScopeModel} from '@core/model/scope-model';
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	selector: 'app-benchmark',
	templateUrl: './benchmark.component.html',
	styleUrls: ['./benchmark.component.css'],
	imports: [
		ReactiveFormsModule,
		MatLabel,
		MatFormField,
		MatSelect,
		MatOptgroup,
		MatOption,
		MatInput,
		MatButton,
		MatIcon,
		LocalizeMapPipe,
		CapitalizeFirstPipe,
		ChartWidgetComponent
	]
})
export class BenchmarkComponent implements OnInit {
	readonly layout = input.required<CMSLayout>();
	readonly widgets = computed(() => this.layout().sections[0].widgets);

	//customization form
	criteria = new FormArray([] as FormArray[]);
	customizeForm = new FormGroup({
		rootScopePks: new FormControl([] as number[], {nonNullable: true, validators: [Validators.required]}),
		criteria: this.criteria
	}) as FormGroup;

	readonly scopeModels = signal<ScopeModel[]>([]);
	readonly rootScopes = signal<ScopeMini[]>([]);
	readonly fieldModels = signal<FieldModel[]>([]);

	//parameters sent to widgets
	readonly chartScopes = signal<ScopeMini[]>([]);
	readonly chartCriteria = signal<FieldModelCriterion[]>([]);

	constructor(
		private configurationService: ConfigurationService,
		private meService: MeService,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		forkJoin({
			scopeModels: this.configurationService.getScopeModelsSorted(),
			fieldModels: this.configurationService.getSearchableFieldModels(),
			rootScopes: this.meService.getScopes(undefined, true, false)
		}).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(({scopeModels, fieldModels, rootScopes}) => {
			this.scopeModels.set(scopeModels);
			this.fieldModels.set(fieldModels);
			this.rootScopes.set(rootScopes);
			this.reset();
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

	update() {
		const scopes = (this.customizeForm.get('rootScopePks')?.value ?? []) as number[];
		this.chartScopes.set(scopes.map(p => this.rootScopes().find(s => s.pk === p) as ScopeMini));

		this.chartCriteria.set(this.criteria.controls.map((criterion: FormArray, index: number) => {
			const fieldModel = this.getFieldModel(index);
			return {
				datasetModelId: fieldModel.datasetModelId,
				fieldModelId: fieldModel.id,
				operator: criterion.controls[1].value,
				value: criterion.controls[2].value
			} as FieldModelCriterion;
		}));
	}

	reset() {
		this.customizeForm.reset();
		this.customizeForm.get('rootScopePks')?.setValue([this.rootScopes()[0].pk]);
		this.criteria.clear();
		this.update();
	}
}
