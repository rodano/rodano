import {Component, ViewChild, DestroyRef, OnInit, Input} from '@angular/core';
import {ReactiveFormsModule, FormControl, FormGroup} from '@angular/forms';
import {Observable, forkJoin, merge, of, iif, defer, fromEvent, EMPTY} from 'rxjs';
import {debounceTime, filter, map, switchMap, tap} from 'rxjs/operators';
import {ConfigurationService} from '@core/services/configuration.service';
import {Scope} from '@core/model/scope';
import {ScopeSearch} from '@core/utilities/search/scope-search';
import {ScopeService} from '@core/services/scope.service';
import {ScopeModel} from '@core/model/scope-model';
import {Workflow} from '@core/model/workflow';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {HttpParamsService} from '@core/services/http-params.service';
import {Router, ActivatedRoute, Routes} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {SelectScopeComponent} from './dialogs/create-scope/select-scope.component';
import {NotificationService} from 'src/app/services/notification.service';
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatDivider} from '@angular/material/divider';
import {MatChipsModule} from '@angular/material/chips';
import {MatTableModule} from '@angular/material/table';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatInput, MatSuffix} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatExpansionModule} from '@angular/material/expansion';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {FieldModel} from '@core/model/field-model';
import {FieldModelType} from '@core/model/field-model-type';
import {MatSelect, MatOption} from '@angular/material/select';
import {FieldModelCriterion} from '@core/model/field-model-criterion';
import {format, parse, isValid} from 'date-fns';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {Operator} from '@core/model/operator';
import {DateUTCPipe} from '../pipes/date-utc.pipe';
import {MeService} from '@core/services/me.service';
import {ScopeMini} from '@core/model/scope-mini';
import {LowerCasePipe} from '@angular/common';
import {FormService} from '@core/services/form.service';
import {ScopeRelationsService} from '@core/services/scope-relations.service';
import {Rights} from '@core/model/rights';
import {ExtendedScopeSearchResult} from '@core/model/extended-scope-search-result';
import {MatCheckbox} from '@angular/material/checkbox';

@Component({
	selector: 'app-search',
	templateUrl: './search.component.html',
	styleUrls: ['./search.component.css'],
	imports: [
		ReactiveFormsModule,
		MatExpansionModule,
		MatFormField,
		MatSuffix,
		MatInput,
		MatIcon,
		MatLabel,
		MatButton,
		MatProgressBarModule,
		MatTableModule,
		MatChipsModule,
		MatDivider,
		MatToolbarModule,
		MatPaginatorModule,
		LocalizeMapPipe,
		MatSortModule,
		MatSelect,
		MatOption,
		MatDatepickerModule,
		DateUTCPipe,
		LowerCasePipe,
		MatCheckbox
	]
})
export class SearchComponent implements OnInit {
	static readonly ROUTES: Routes = [
		{
			path: '',
			component: SearchComponent
		}
	];

	columnsToDisplay: string[] = [
		'parentScopeCode',
		'scopeCode'
	];

	Object = Object;
	FieldModelType = FieldModelType;

	@Input() scopeModel: ScopeModel;

	scopeModelId: string;
	selectedScopeModel: ScopeModel = {} as ScopeModel;
	selectedScopeModelParentModel: ScopeModel = {} as ScopeModel;
	writeAccessOnParent = false;
	hasManageDeletedDataFeature = false;

	searchableWorkflows: Workflow[];
	parentScopes: ScopeMini[];
	searchableFields: FieldModel[];

	fieldModelCriteria: FieldModelCriterion[] = [];

	extendedScopeSearchResult: ExtendedScopeSearchResult[] = [];

	searchForm = new FormGroup<Record<string, FormControl<any>>>({
		scopeCode: new FormControl()
	});

	parentScopeControl = new FormControl();
	showRemovedScopesControl = new FormControl(false);

	resultsLength: number;

	//Map to hold the workflowStatus model shortname
	workflowStatusModelMap = new Map<string, Record<string, string>>();

	statusModelMap = new Map<string, Record<string, string>>();

	//Map to hold the fieldModel
	fieldModelMap = new Map<string, Record<string, string>>();

	//Map to hold the aggregated workflow ID for a workflow aggregator
	aggregatedWorkflowMap = new Map<string, string>();

	loading = false;

	//Caches
	private readonly fieldModelCache = new Map<string, FieldModel>();
	private readonly workflowCache = new Map<string, Workflow>();

	@ViewChild(MatSort, {static: true}) sort: MatSort;
	@ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

	constructor(
		private readonly configurationService: ConfigurationService,
		private readonly scopeService: ScopeService,
		private readonly meService: MeService,
		private readonly httpParamsService: HttpParamsService,
		private readonly notificationService: NotificationService,
		private readonly router: Router,
		private readonly route: ActivatedRoute,
		private readonly dialog: MatDialog,
		private readonly destroyRef: DestroyRef,
		private readonly formService: FormService,
		private readonly scopeRelationService: ScopeRelationsService
	) {}

	ngOnInit(): void {
		//Get scopeModelId from URL query params
		this.scopeModelId = this.route.snapshot.queryParams['scopeModelId'];

		if(this.scopeModel) {
			//If scopeModel is provided as input, use it and update URL
			this.selectedScopeModel = this.scopeModel;
			this.scopeModelId = this.scopeModel.id;
		}

		//Get showRemovedScopes state from URL query params
		const showRemovedScopes = this.route.snapshot.queryParams['includeDeleted'];
		if(showRemovedScopes !== undefined) {
			this.showRemovedScopesControl.setValue(showRemovedScopes === 'true', {emitEvent: false});
		}

		this.initializeData();
		this.sort.active = 'scopeCode';
		this.sort.direction = 'asc';
	}

	private initializeData(): void {
		forkJoin({
			scopeModels: this.configurationService.getScopeModels(),
			me: this.meService.get()
		}).pipe(
			takeUntilDestroyed(this.destroyRef),
			switchMap(results => {
				const scopeModels = results.scopeModels;

				//Find the leaf scope model by ID from URL
				if(!this.selectedScopeModel.id && this.scopeModelId) {
					this.selectedScopeModel = scopeModels.find(sm => sm.id === this.scopeModelId) ?? ({} as ScopeModel);
				}

				this.selectedScopeModelParentModel = scopeModels.find(scopeModel => scopeModel.id === this.selectedScopeModel.defaultParentId) ?? ({} as ScopeModel);
				this.hasManageDeletedDataFeature = results.me.roles?.some(r => r.profile.features.some(f => f === 'MANAGE_DELETED_DATA')) ?? false;

				//Load dependent data after we have the scope model
				return forkJoin({
					searchableWorkflows: this.configurationService.getSearchableWorkflowsOnScope(this.selectedScopeModel),
					parentScopes: this.meService.getScopes(undefined, true, false),
					searchableFields: this.configurationService.getSearchableFieldModelsOnScope(this.selectedScopeModel)
				});
			}),
			switchMap(results => {
				this.searchableWorkflows = results.searchableWorkflows;
				this.parentScopes = results.parentScopes.filter(scope => scope.modelId === this.selectedScopeModel.defaultParentId);

				this.searchableFields = results.searchableFields;

				this.buildMapsAndColumns();

				//Cache workflows and fields
				this.searchableWorkflows.forEach(workflow => this.workflowCache.set(workflow.id, workflow));
				this.searchableFields.forEach(field => {
					const key = `${field.datasetModelId.toUpperCase()}.${field.id.toUpperCase()}`;
					this.fieldModelCache.set(key, field);
				});
				this.setupFormListeners();
				this.loadData();

				//Return the parentsWithWriteAccess observable
				return this.scopeRelationService.getParents(this.selectedScopeModel.id, Rights.WRITE).pipe(
					map(parentsWithWriteAccess => ({
						parentsWithWriteAccess
					}))
				);
			})
		).subscribe(({parentsWithWriteAccess}) => {
			this.writeAccessOnParent = parentsWithWriteAccess.length > 0;
		});
	}

	private buildMapsAndColumns(): void {
		//Build all maps in one pass
		this.searchableFields.forEach(fieldModel => {
			const controlName = this.getSFFormControlName(fieldModel);
			this.columnsToDisplay.push(controlName);
			this.fieldModelMap.set(controlName, fieldModel.shortname);
		});

		this.searchableWorkflows.forEach(workflow => {
			this.columnsToDisplay.push(workflow.id);
			this.workflowStatusModelMap.set(workflow.id, workflow.shortname);

			workflow.states.forEach(state => {
				this.statusModelMap.set(`${workflow.id}_${state.id}`, state.shortname);
			});

			if(workflow.aggregator && workflow.aggregatedWorkflowId) {
				this.aggregatedWorkflowMap.set(workflow.id, workflow.aggregatedWorkflowId);
			}
		});
	}

	private setupFormListeners(): void {
		const formChanges$ = [
			this.searchForm.controls.scopeCode.valueChanges.pipe(debounceTime(300)),
			...this.createFormControlsAndObservables(),
			this.parentScopeControl.valueChanges,
			this.showRemovedScopesControl.valueChanges,
			this.paginator.page,
			this.sort.sortChange
		];

		merge(...formChanges$)
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe(() => this.search());
	}

	private createFormControlsAndObservables(): Observable<any>[] {
		const observables: Observable<any>[] = [];

		//Create workflow controls
		this.searchableWorkflows.forEach(workflow => {
			const control = new FormControl();
			this.searchForm.addControl(this.getWSFormControlName(workflow), control);
			observables.push(control.valueChanges);
		});

		//Create searchable field controls
		this.searchableFields.forEach(fieldModel => {
			const control = new FormControl();
			this.searchForm.addControl(this.getSFFormControlName(fieldModel), control);

			if(this.getIsSearchableDate(fieldModel)) {
				const enterKeyPress$ = fromEvent<KeyboardEvent>(document, 'keydown').pipe(
					filter(event => event.key === 'Enter'),
					map(() => control.value),
					takeUntilDestroyed(this.destroyRef)
				);
				observables.push(enterKeyPress$);
			}
			else {
				observables.push(control.valueChanges);
			}
		});

		return observables;
	}

	search(): void {
		const path = this.httpParamsService.toHttpParams(this.generateScopeSearch());
		this.router.navigateByUrl(`/search?${path.toString()}`);
	}

	private loadData(): void {
		this.route.queryParams.pipe(
			tap(() => {
				this.loading = true;
				this.extendedScopeSearchResult = [];
				this.resultsLength = 0;
			}),
			switchMap(params => {
				let scopeSearchObj: ScopeSearch;

				//if no parameters are provided, use default search object
				if(Object.entries(params).length === 0) {
					scopeSearchObj = new ScopeSearch();
				}
				else {
					scopeSearchObj = this.httpParamsService.toScopeSearch(params);
				}

				scopeSearchObj.scopeModelId = this.selectedScopeModel.id;

				this.syncFormAndUrl(scopeSearchObj);
				return this.scopeService.extendedSearch(scopeSearchObj);
			}),
			tap(() => this.loading = false)
		).subscribe(scopeResults => {
			this.extendedScopeSearchResult = scopeResults.objects;
			this.resultsLength = scopeResults.paging.total;
		});
	}

	getWSFormControlName(workflow: Workflow): string {
		return `ws_${workflow.id}`;
	}

	getSFFormControlName(fieldModel: FieldModel): string {
		const fieldName = `${fieldModel.datasetModelId}.${fieldModel.id}`.toLowerCase();
		return fieldName;
	}

	createPatient() {
		const scopeSearch = new ScopeSearch();
		scopeSearch.scopeModelId = this.selectedScopeModel.parentIds[0];

		//1. search for all the scopes with the parent scope model
		//2. if there is more than one scope available, open the scope selection dialog, otherwise pick the only one that is available
		//3. get the candidate scope
		//4. send the scope creation request
		//5. redirect to the newly created patient on success
		this.scopeService.search(scopeSearch).pipe(
			switchMap(searchResult => {
				const parentScopes = searchResult.objects;

				//Because the iif function executes all the branching paths even though they are not selected, we add a defer to the
				//dialog observable

				return iif(
					() => parentScopes.length > 1,
					defer(() => this.openScopeSelectDialog(parentScopes, this.selectedScopeModel)),
					of(parentScopes[0])
				);
			}),
			switchMap(parentScope => {
				if(!parentScope) {
					return EMPTY;
				}
				return this.scopeService.getCandidate(parentScope.pk, this.selectedScopeModel.id);
			}),
			switchMap(candidateScope => this.scopeService.create(candidateScope)),
			switchMap(newScope =>
				//get the forms for the new scope
				this.formService.searchOnScope(newScope.pk).pipe(
					map(forms => ({newScope, forms}))
				)
			)
		).subscribe(({newScope, forms}) => {
			if(forms.length > 0) {
				this.router.navigate(['crf', newScope.pk, 'form', forms[0].pk]);
			}
			else {
				this.router.navigate(['crf', newScope.pk]);
			}
			this.notificationService.showSuccess(`${newScope.shortname} created`);
		});
	}

	private syncFormAndUrl(scopeSearch: ScopeSearch) {
		//Sync the scope code
		this.searchForm.controls.scopeCode.setValue(scopeSearch.fullText, {emitEvent: false});

		//Sync the workflow controls
		if(scopeSearch.workflowStates) {
			Object.keys(scopeSearch.workflowStates).forEach(workflowId => {
				const workflow = this.getWorkflow(workflowId);
				if(workflow) {
					const control = this.searchForm.controls[this.getWSFormControlName(workflow)];
					control.setValue(scopeSearch.workflowStates[workflowId], {emitEvent: false});
				}
			});
		}
		//Sync the fieldModelCriteria fields
		if(scopeSearch.fieldModelCriteria) {
			this.fieldModelCriteria = JSON.parse(scopeSearch.fieldModelCriteria);
			this.fieldModelCriteria.forEach(criteria => {
				const datasetModelId = criteria.datasetModelId;
				const fieldModelId = criteria.fieldModelId;
				if(!datasetModelId || !fieldModelId) {
					return;
				}

				const fieldModel = this.getFieldModel(datasetModelId, fieldModelId);
				if(!fieldModel) {
					return;
				}

				const control = this.searchForm.controls[this.getSFFormControlName(fieldModel)];
				if(control) {
					if(criteria.value && this.getIsSearchableDate(fieldModel)) {
						const date = parse(criteria.value, 'dd.MM.yyyy', 0);
						control.setValue(date, {emitEvent: false});
					}
					else {
						control.setValue(criteria.value, {emitEvent: false});
					}
				}
			});
		}

		//Sync the ancestor scopes
		if(scopeSearch.parentPks) {
			const control = this.parentScopeControl;
			control.setValue(scopeSearch.parentPks, {emitEvent: false});
		}

		//Sync the show removed scopes checkbox - always sync, default to false
		this.showRemovedScopesControl.setValue(scopeSearch.includeDeleted ?? false, {emitEvent: false});

		//Sync the table paginator
		this.paginator.pageIndex = scopeSearch.pageIndex;

		//Sync the sort
		//transform 'scopeCode' to 'code', use other sort fields as-is
		scopeSearch.sortBy = this.sort.active === 'scopeCode' ? 'code' : this.sort.active;
		scopeSearch.orderAscending = this.sort.direction === 'asc';
	}

	private generateScopeSearch(): ScopeSearch {
		const search = new ScopeSearch();
		search.fullText = this.searchForm.controls.scopeCode.value;

		search.scopeModelId = this.scopeModelId;
		search.workflowStates = {};
		search.fieldModelCriteria = '';
		search.includeDeleted = this.showRemovedScopesControl.value ?? false;

		//Process workflow states
		this.searchableWorkflows.forEach(workflow => {
			const control = this.searchForm.controls[this.getWSFormControlName(workflow)];
			const value = control?.value;

			if(!value?.length) {
				return;
			}

			const workflowId = this.aggregatedWorkflowMap.get(workflow.id) ?? workflow.id;
			search.workflowStates[workflowId] = value;
		});

		//Process field model criteria
		this.searchableFields.forEach(fieldModel => {
			const control = this.searchForm.controls[this.getSFFormControlName(fieldModel)];
			if(!control?.value) {
				return;
			}

			const isString = fieldModel.type === FieldModelType.STRING;
			const value = this.getFieldValueForCriteria(fieldModel, control.value);

			//Skip strings with less than 3 characters
			if(isString && value.length < 3) {
				return;
			}

			const operator = isString ? Operator.CONTAINS : Operator.EQUALS;
			const existingIndex = this.fieldModelCriteria.findIndex(
				c => c.datasetModelId === fieldModel.datasetModelId && c.fieldModelId === fieldModel.id
			);

			if(existingIndex >= 0) {
				this.fieldModelCriteria[existingIndex] = {...this.fieldModelCriteria[existingIndex], value, operator};
			}
			else {
				this.fieldModelCriteria.push({
					value,
					operator,
					datasetModelId: fieldModel.datasetModelId,
					fieldModelId: fieldModel.id
				});
			}
		});

		if(this.fieldModelCriteria.length > 0) {
			search.fieldModelCriteria = JSON.stringify(this.fieldModelCriteria);
		}

		const parentScopeControl = this.parentScopeControl;
		if(parentScopeControl.value) {
			search.parentPks = parentScopeControl.value;
		}

		search.pageIndex = this.paginator.pageIndex;

		//Sync the sort
		//transform 'scopeCode' to 'code', use other sort fields as-is
		search.sortBy = this.sort.active === 'scopeCode' ? 'code' : this.sort.active;
		search.orderAscending = this.sort.direction === 'asc';
		return search;
	}

	private openScopeSelectDialog(parentScopes: Scope[], leafScopeModel: ScopeModel): Observable<Scope | undefined> {
		const data = {
			parentScopes: parentScopes,
			childScopeModel: leafScopeModel
		};
		return this.dialog
			.open(SelectScopeComponent, {data})
			.afterClosed();
	}

	getStatusModel(workflowId: string, stateId: string) {
		return this.statusModelMap.get(`${workflowId}_${stateId}`);
	}

	getValueShortname(datasetModelId: string, fieldId: string, field: any): Record<string, string> | undefined {
		const fieldModel = this.getFieldModel(datasetModelId, fieldId);
		return fieldModel?.possibleValues?.find(v => v.id === field)?.shortname ?? field;
	}

	getDateObject(datasetModelId: string, fieldId: string, field: any): Date | undefined {
		const fieldModel = this.getFieldModel(datasetModelId, fieldId);
		if(fieldModel?.type === FieldModelType.DATE) {
			return this.parseDateString(field);
		}
		return undefined;
	}

	getDateObjectFromString(field: any): Date | undefined {
		return field ? this.parseDateString(field) : undefined;
	}

	private parseDateString(dateStr: string): Date | undefined {
		try {
			const parsedDate = parse(dateStr, 'dd.MM.yyyy', new Date());
			return isValid(parsedDate) ? parsedDate : undefined;
		}
		catch {
			return undefined;
		}
	}

	getIsPossibleValue(datasetModelId: string, fieldId: string): boolean {
		const fieldModel = this.getFieldModel(datasetModelId, fieldId);
		return (fieldModel?.possibleValues?.length ?? 0) > 0;
	}

	getIsCompleteDate(datasetModelId: string, fieldId: string): boolean {
		const fieldModel = this.getFieldModel(datasetModelId, fieldId);
		if(!fieldModel) {
			return false;
		}

		const isDateType = fieldModel.type === FieldModelType.DATE || fieldModel.type === FieldModelType.DATE_SELECT;
		return isDateType && fieldModel.daysMandatory && fieldModel.monthsMandatory && fieldModel.yearsMandatory;
	}

	getFieldModel(datasetModelId: string, fieldId: string): FieldModel {
		const key = `${datasetModelId.toUpperCase()}.${fieldId.toUpperCase()}`;
		return this.fieldModelCache.get(key) ?? {} as FieldModel;
	}

	getWorkflow(workflowId: string): Workflow | undefined {
		return this.workflowCache.get(workflowId);
	}

	getFieldModelType(datasetModelId: string, fieldId: string): FieldModelType | undefined {
		return this.getFieldModel(datasetModelId, fieldId)?.type;
	}

	getFieldValueForCriteria(fieldModel: FieldModel, value: string): string {
		if(this.getIsSearchableDate(fieldModel)) {
			const dateValue = new Date(value);
			return format(dateValue, 'dd.MM.yyyy');
		}
		return value;
	}

	getIsSearchableDate(fieldModel: FieldModel): boolean {
		return fieldModel?.type === FieldModelType.DATE || (fieldModel?.type === FieldModelType.DATE_SELECT && fieldModel.daysMandatory && fieldModel.monthsMandatory && fieldModel.yearsMandatory);
	}

	getStartDate() {
		//Return a date that is 30 years in the past
		//This is used to set the default date for the date picker
		const date = new Date();
		date.setFullYear(date.getFullYear() - 30);
		date.setMonth(0);
		date.setDate(1);
		return date;
	}

	//return the searchable field from the form control ID
	getSearchableFieldFromFormControlId(formControlId: string): FieldModel {
		const [datasetModelId, fieldModelId] = formControlId.split('.');

		return this.getFieldModel(datasetModelId, fieldModelId);
	}

	resetSearchCriteria(): void {
		//Reset all form controls without emitting to avoid multiple searches
		this.searchForm.reset({emitEvent: false});
		this.parentScopeControl.setValue([], {emitEvent: false});
		this.fieldModelCriteria = [];
		this.paginator.pageIndex = 0;

		//Trigger a single search after all resets
		this.search();
	}

	navigateToScope(scopePk: number): void {
		this.router.navigate(['crf', scopePk]);
	}
}
