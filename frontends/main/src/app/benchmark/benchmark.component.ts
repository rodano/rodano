import {Component, DestroyRef, Input, OnChanges, OnInit, QueryList, ViewChildren} from '@angular/core';
import {forkJoin} from 'rxjs';
import {PossibleValueDTO} from '@core/model/possible-value-dto';
import {operatorByType} from '@core/enums/operator-by-type';
import {ConfigurationService} from '@core/services/configuration.service';
import {FieldModelDTO} from '@core/model/field-model-dto';
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
import {ScopeMiniDTO} from '@core/model/scope-mini-dto';
import {ChartWidgetComponent} from 'src/app/widgets/chart/chart-widget.component';
import {CMSLayoutDTO} from '@core/model/cms-layout-dto';
import {CMSWidgetDTO} from '@core/model/cms-widget-dto';
import {ScopeModelDTO} from '@core/model/scope-model-dto';
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';

@Component({
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
export class BenchmarkComponent implements OnInit, OnChanges {
	@Input() layout: CMSLayoutDTO;
	widgets: CMSWidgetDTO[] = [];

	@ViewChildren(ChartWidgetComponent) chartWidgets: QueryList<ChartWidgetComponent>;

	//customization form
	criteria = new FormArray([] as FormArray[]);
	customizeForm = new FormGroup({
		rootScopePks: new FormControl([] as number[], {nonNullable: true, validators: [Validators.required]}),
		criteria: this.criteria
	}) as FormGroup;

	scopeModels: ScopeModelDTO[] = [];
	rootScopes: ScopeMiniDTO[] = [];
	fieldModels: FieldModelDTO[];

	//parameters sent to widgets
	chartScopes: ScopeMiniDTO[] = [];
	chartCriteria: FieldModelCriterion[] = [];

	constructor(
		private configurationService: ConfigurationService,
		private meService: MeService,
		private destroyRef: DestroyRef
	) { }

	ngOnInit() {
		forkJoin({
			scopeModels: this.configurationService.getScopeModelsSorted(),
			fieldModels: this.configurationService.getSearchableFieldModels(),
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
		this.widgets = this.layout.sections[0].widgets;
	}

	getScopes(modelId: string): ScopeMiniDTO[] {
		return this.rootScopes?.filter(s => s.modelId === modelId) ?? [];
	}

	getControl(i: number, j: number): FormControl {
		const criterion = this.criteria.controls[i] as FormArray;
		return criterion.controls[j] as FormControl;
	}

	getFieldModel(index: number): FieldModelDTO {
		const criterion = this.criteria.controls[index] as FormArray;
		const fieldModelId = criterion.controls[0].value;
		return this.fieldModels.find(f => f.id === fieldModelId) as FieldModelDTO;
	}

	getOperators(index: number): Operator[] {
		const fieldModel = this.getFieldModel(index);
		if(!fieldModel) {
			return [];
		}
		return operatorByType[fieldModel.type];
	}

	getPossibleValues(index: number): PossibleValueDTO[] {
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
		this.chartScopes = scopes.map(p => this.rootScopes.find(s => s.pk === p) as ScopeMiniDTO);

		this.chartCriteria = this.criteria.controls.map((criterion: FormArray, index: number) => {
			const fieldModel = this.getFieldModel(index);
			return {
				datasetModelId: fieldModel.datasetModelId,
				fieldModelId: fieldModel.id,
				operator: criterion.controls[1].value,
				value: criterion.controls[2].value
			} as FieldModelCriterion;
		});
		console.log(this.chartScopes);
		console.log(this.chartCriteria);

		const selectedRootScopeIds: string[] = [];
		for(const scope of this.chartScopes) {
			selectedRootScopeIds.push(scope.id);
		}

		this.updateAllChartsBasedOnScopes(selectedRootScopeIds, this.chartCriteria);
	}

	reset() {
		this.customizeForm.reset();
		this.customizeForm.get('rootScopePks')?.setValue([this.rootScopes[0].pk]);
		this.criteria.clear();
	}

	updateAllChartsBasedOnScopes(selectedRootScopes: string[], criteria: FieldModelCriterion[]): void {
		this.chartWidgets.forEach(widget => {
			widget.updateChartBasedOnSelectedRootScopesAndCriteria(selectedRootScopes, criteria);
		});
	}
}
