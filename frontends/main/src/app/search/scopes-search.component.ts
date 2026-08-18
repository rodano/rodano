import {Component, ViewChild, DestroyRef, OnInit, signal, HostListener} from '@angular/core';
import {ReactiveFormsModule, FormControl, FormGroup} from '@angular/forms';
import {Observable, Subject, forkJoin, merge, of, iif, defer, EMPTY} from 'rxjs';
import {debounceTime, filter, map, switchMap, tap, distinctUntilChanged} from 'rxjs/operators';
import {ConfigurationService} from '@core/services/configuration.service';
import {Scope} from '@core/model/scope';
import {ScopeSearch} from '@core/utilities/search/scope-search';
import {ScopeService} from '@core/services/scope.service';
import {ScopeModel} from '@core/model/scope-model';
import {Workflow} from '@core/model/workflow';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {HttpParamsService} from '@core/services/http-params.service';
import {Router, ActivatedRoute} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {SelectScopeComponent} from './dialogs/create-scope/select-scope.component';
import {NotificationService} from '../services/notification.service';
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
import {Location, LowerCasePipe} from '@angular/common';
import {FormService} from '@core/services/form.service';
import {ScopeRelationsService} from '@core/services/scope-relations.service';
import {Rights} from '@core/model/rights';
import {ExtendedScopeSearchResult} from '@core/model/extended-scope-search-result';
import {MatCheckbox} from '@angular/material/checkbox';
import {AutofocusDirective} from '../directives/autofocus.directive';
import {FeatureStatic} from '@core/model/feature-static';

@Component({
	selector: 'app-search',
	templateUrl: './scopes-search.component.html',
	styleUrls: ['./scopes-search.component.css'],
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
		LowerCasePipe,
		MatCheckbox,
		AutofocusDirective
	]
})
export class SearchComponent implements OnInit {
	readonly columnsToDisplay = signal<string[]>([]);
	SearchComponent = SearchComponent;

	FieldModelType = FieldModelType;

	scopeModelId: string;
	readonly selectedScopeModel = signal<ScopeModel>({} as ScopeModel);
	readonly selectedScopeModelParentModel = signal<ScopeModel>({} as ScopeModel);
	readonly writeAccessOnParent = signal(false);
	readonly hasManageDeletedDataFeature = signal(false);

	readonly parentScopes = signal<ScopeMini[]>([]);

	searchableWorkflows: Workflow[];
	searchableFields: FieldModel[];

	fieldModelCriteria: FieldModelCriterion[] = [];

	extendedScopeSearchResult = signal<ExtendedScopeSearchResult[]>([]);

	searchForm = new FormGroup<Record<string, FormControl<any>>>({
		scopeCode: new FormControl()
	});

	parentScopeControl = new FormControl();
	showRemovedScopesControl = new FormControl(false);

	resultsLength = signal(0);

	loading = signal(false);

	private readonly searchTrigger$ = new Subject<ScopeSearch>();

	@ViewChild(MatSort, {static: true}) sort: MatSort;
	@ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

	static readonly DEFAULT_START_DATE: Date = (() => {
		const date = new Date();
		date.setFullYear(date.getFullYear() - 30);
		date.setMonth(0);
		date.setDate(1);
		return date;
	})();

	constructor(
		private readonly configurationService: ConfigurationService,
		private readonly scopeService: ScopeService,
		private readonly meService: MeService,
		private readonly httpParamsService: HttpParamsService,
		private readonly notificationService: NotificationService,
		private readonly router: Router,
		private readonly route: ActivatedRoute,
		private readonly location: Location,
		private readonly dialog: MatDialog,
		private readonly destroyRef: DestroyRef,
		private readonly formService: FormService,
		private readonly scopeRelationService: ScopeRelationsService
	) { }

	ngOnInit(): void {
		//Get scopeModelId from URL path params
		this.scopeModelId = this.route.snapshot.params['scopeModelId'];

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

				//Use the resolved scope model from the resolver
				const resolvedScopeModel = this.route.snapshot.data['scopeModel'];
				this.selectedScopeModel.set(resolvedScopeModel);
				this.selectedScopeModelParentModel.set(scopeModels.find(scopeModel => scopeModel.id === this.selectedScopeModel().defaultParentId) ?? ({} as ScopeModel));
				this.hasManageDeletedDataFeature.set(results.me.roles?.some(r => r.profile.features.includes(FeatureStatic.MANAGE_REMOVED_DATA)) ?? false);

				//Load dependent data after we have the scope model
				return forkJoin({
					searchableWorkflows: this.configurationService.getScopeModelSearchableWorkflows(this.selectedScopeModel()),
					parentScopes: this.meService.getScopes(undefined, true, false),
					searchableFields: this.configurationService.getScopeModelFieldModels(this.selectedScopeModel().id, true),
					parentsWithWriteAccess: this.scopeRelationService.getParents(this.selectedScopeModel().id, Rights.WRITE)
				});
			})
		).subscribe(results => {
			this.searchableWorkflows = results.searchableWorkflows;
			this.parentScopes.set(results.parentScopes.filter(scope => scope.modelId === this.selectedScopeModel().defaultParentId));

			//Filter out date fields that are not fully collected and mandatory, as they cannot be searched
			//TODO implement date range search to allow searching on partially collected dates
			this.searchableFields = results.searchableFields.filter(field => !(field.type === FieldModelType.DATE || field.type === FieldModelType.DATE_SELECT) || SearchComponent.getIsCompleteDate(field));

			//Warn if any searchable fields were filtered out
			if(results.searchableFields.length > this.searchableFields.length) {
				const filteredOutFields: FieldModel[] = results.searchableFields.filter(field => !this.searchableFields.some(f => f.type === field.type));
				console.warn(`Searchable fields filtered out: ${filteredOutFields.map(f => f.id).join(', ')}`);
			}

			this.buildColumnsArray();
			this.setupFormListeners();
			this.setupSearch();

			this.writeAccessOnParent.set(results.parentsWithWriteAccess.length > 0);
		});
	}

	private buildColumnsArray(): void {
		const newColumns = ['parentScopeCode', 'scopeCode'];

		this.searchableFields.forEach(fieldModel => {
			newColumns.push(SearchComponent.getSFFormControlName(fieldModel));
		});

		this.searchableWorkflows.forEach(workflow => {
			newColumns.push(workflow.id);
		});

		this.columnsToDisplay.set(newColumns);
	}

	private setupFormListeners(): void {
		const criteriaChanges$ = merge(
			this.searchForm.controls.scopeCode.valueChanges.pipe(debounceTime(300)),
			...this.createFormControlsAndObservables(),
			this.parentScopeControl.valueChanges,
			this.showRemovedScopesControl.valueChanges
		);

		//When filters/sort change, always go back to the first page.
		merge(criteriaChanges$, this.sort.sortChange)
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe(() => {
				this.paginator.pageIndex = 0;
				this.search();
			});

		//When only paging changes (page index / page size), keep the current page index.
		this.paginator.page
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe(() => this.search());

		this.route.queryParams
			.pipe(
				takeUntilDestroyed(this.destroyRef),
				filter(params => Object.keys(params).length > 0),
				map(params => this.httpParamsService.toScopeSearch(params))
			)
			.subscribe(scopeSearch => {
				this.syncFormAndUrl(scopeSearch);
			});
	}

	private createFormControlsAndObservables(): Observable<any>[] {
		const observables: Observable<any>[] = [];

		//Create workflow controls
		this.searchableWorkflows.forEach(workflow => {
			const control = new FormControl();
			this.searchForm.addControl(SearchComponent.getWSFormControlName(workflow), control);
			observables.push(control.valueChanges);
		});

		//Create searchable field controls
		this.searchableFields.forEach(fieldModel => {
			const control = new FormControl();
			this.searchForm.addControl(SearchComponent.getSFFormControlName(fieldModel), control);

			observables.push(control.valueChanges);
		});
		return observables;
	}

	search(): void {
		const scopeSearch = this.generateScopeSearch();
		//Remove scopeModelId from query params since it's in the path
		delete scopeSearch.scopeModelId;

		if(scopeSearch.workflowStates && Object.keys(scopeSearch.workflowStates).length > 0) {
			(scopeSearch as any).workflowStates = JSON.stringify(scopeSearch.workflowStates);
		}
		if(scopeSearch.fieldModelCriteria && Array.isArray(scopeSearch.fieldModelCriteria) && scopeSearch.fieldModelCriteria.length > 0) {
			(scopeSearch as any).fieldModelCriteria = JSON.stringify(scopeSearch.fieldModelCriteria);
		}

		const urlTree = this.router.createUrlTree(
			['scopes-search', this.scopeModelId],
			{queryParams: scopeSearch}
		);
		this.location.replaceState(this.router.serializeUrl(urlTree));
		this.searchTrigger$.next(scopeSearch);
	}

	private setupSearch(): void {
		this.searchTrigger$.pipe(takeUntilDestroyed(this.destroyRef),
			tap(() => {
				this.loading.set(true);
				this.extendedScopeSearchResult.set([]);
				this.resultsLength.set(0);
			}),
			switchMap(scopeSearchObj => {
				scopeSearchObj.scopeModelId = this.selectedScopeModel().id;
				return this.scopeService.extendedSearch(scopeSearchObj);
			}),
			tap(() => this.loading.set(false))
		).subscribe(scopeResults => {
			//If the current pageIndex is out of range (e.g. filters reduced total), jump to the last valid page.
			if(scopeResults.objects.length === 0 && scopeResults.paging.total > 0) {
				const lastPageIndex = Math.max(0, Math.ceil(scopeResults.paging.total / scopeResults.paging.pageSize) - 1);
				if(this.paginator.pageIndex > lastPageIndex) {
					this.paginator.pageIndex = lastPageIndex;
					this.search();
					return;
				}
			}

			this.extendedScopeSearchResult.set(scopeResults.objects);
			this.resultsLength.set(scopeResults.paging.total);
		});

		//Trigger initial load from URL params and sync form only on initial load
		const params = this.route.snapshot.queryParams;
		const initialScopeSearch = Object.entries(params).length === 0
			? new ScopeSearch()
			: this.httpParamsService.toScopeSearch(params);

		//Sync form state from URL only on initial page load
		this.syncFormAndUrl(initialScopeSearch);

		this.searchTrigger$.next(initialScopeSearch);
	}

	static getWSFormControlName(workflow: Workflow): string {
		return `ws_${workflow.id}`;
	}

	static getSFFormControlName(fieldModel: FieldModel): string {
		const fieldName = `${fieldModel.datasetModelId}.${fieldModel.id}`.toLowerCase();
		return fieldName;
	}

	createScope() {
		const scopeSearch = new ScopeSearch();
		scopeSearch.scopeModelId = this.selectedScopeModel().parentIds[0];

		//1. search for all the scopes with the parent scope model
		//2. if there is more than one scope available, open the scope selection dialog, otherwise pick the only one that is available
		//3. get the candidate scope
		//4. send the scope creation request
		//5. redirect to the newly created scope on success
		this.scopeService.search(scopeSearch).pipe(
			switchMap(searchResult => {
				const parentScopes = searchResult.objects;

				//Because the iif function executes all the branching paths even though they are not selected, we add a defer to the
				//dialog observable

				return iif(
					() => parentScopes.length > 1,
					defer(() => this.openScopeSelectDialog(parentScopes, this.selectedScopeModel())),
					of(parentScopes[0])
				);
			}),
			switchMap(parentScope => {
				if(!parentScope) {
					return EMPTY;
				}
				return this.scopeService.getCandidate(parentScope.pk, this.selectedScopeModel().id);
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
				this.router.navigate(['crf', newScope.pk, 'forms', forms[0].pk]);
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
					const control = this.searchForm.controls[SearchComponent.getWSFormControlName(workflow)];
					control.setValue(scopeSearch.workflowStates![workflowId], {emitEvent: false});
				}
			});
		}
		//Sync the fieldModelCriteria fields
		if(scopeSearch.fieldModelCriteria) {
			this.fieldModelCriteria = JSON.parse(scopeSearch.fieldModelCriteria as string);
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

				const control = this.searchForm.controls[SearchComponent.getSFFormControlName(fieldModel)];
				if(control) {
					//We ensure that when the field model is date, it is a complete date (searchableDate) to parse it directly to a Date object
					if(criteria.value && SearchComponent.getIsCompleteDate(fieldModel)) {
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
			this.parentScopeControl.setValue(scopeSearch.parentPks, {emitEvent: false});
		}

		//Sync the show removed scopes checkbox - always sync, default to false
		this.showRemovedScopesControl.setValue(scopeSearch.includeDeleted ?? false, {emitEvent: false});

		//Sync the table paginator
		this.paginator.pageIndex = scopeSearch.pageIndex;
		this.paginator.pageSize = scopeSearch.pageSize;

		//Sync the sort
		//transform 'scopeCode' to 'code', use other sort fields as-is
		scopeSearch.sortBy = this.sort.active === 'scopeCode' ? 'code' : this.sort.active;
		scopeSearch.orderAscending = this.sort.direction === 'asc';
	}

	private generateScopeSearch(): ScopeSearch {
		const search = new ScopeSearch();
		search.fullText = this.searchForm.controls.scopeCode.value;

		search.scopeModelId = this.selectedScopeModel().id;
		search.workflowStates = {};
		search.fieldModelCriteria = '';
		search.includeDeleted = this.showRemovedScopesControl.value === true ? true : undefined;

		//Process workflow states
		this.searchableWorkflows.forEach(workflow => {
			const control = this.searchForm.controls[SearchComponent.getWSFormControlName(workflow)];
			const value = control?.value;

			if(!value?.length) {
				return;
			}

			const workflowId = workflow.aggregatedWorkflowId ?? workflow.id;
			search.workflowStates![workflowId] = value;
		});

		//Rebuild fieldModelCriteria from current control values
		const newFieldModelCriteria: FieldModelCriterion[] = [];
		this.searchableFields.forEach(fieldModel => {
			const control = this.searchForm.controls[SearchComponent.getSFFormControlName(fieldModel)];
			if(!control?.value) {
				return;
			}

			const isString = fieldModel.type === FieldModelType.STRING;
			const value = SearchComponent.getFieldValueForCriteria(fieldModel, control.value);

			//Skip strings with less than 3 characters
			if(isString && value.length < 3) {
				return;
			}

			const operator = isString ? Operator.CONTAINS : Operator.EQUALS;
			newFieldModelCriteria.push({
				value,
				operator,
				datasetModelId: fieldModel.datasetModelId,
				fieldModelId: fieldModel.id
			});
		});

		this.fieldModelCriteria = newFieldModelCriteria;
		if(this.fieldModelCriteria.length === 0) {
			delete (search).fieldModelCriteria;
		}
		else {
			(search as any).fieldModelCriteria = this.fieldModelCriteria;
		}

		if(Object.keys(search.workflowStates).length === 0) {
			delete (search as any).workflowStates;
		}

		const parentScopeControl = this.parentScopeControl;
		if(parentScopeControl.value) {
			search.parentPks = parentScopeControl.value;
		}

		search.pageIndex = this.paginator.pageIndex;
		search.pageSize = this.paginator.pageSize;

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

	getStatusModel(workflowId: string, stateId: string): Record<string, string> | undefined {
		const workflow = this.getWorkflow(workflowId);
		const state = workflow?.states.find(s => s.id === stateId);
		return state?.shortname;
	}

	getValueShortname(datasetModelId: string, fieldId: string, field: any): Record<string, string> | undefined {
		const fieldModel = this.getFieldModel(datasetModelId, fieldId);
		return fieldModel?.possibleValues?.find(v => v.id === field)?.shortname ?? field;
	}

	getDateObject(datasetModelId: string, fieldId: string, field: any): Date | undefined {
		const fieldModel = this.getFieldModel(datasetModelId, fieldId);
		if(fieldModel?.type === FieldModelType.DATE) {
			return SearchComponent.parseDateString(field);
		}
		return undefined;
	}

	static getDateObjectFromString(field: any): Date | undefined {
		return field ? SearchComponent.parseDateString(field) : undefined;
	}

	static parseDateString(dateStr: string): Date | undefined {
		try {
			const parsedDate = parse(dateStr, 'dd.MM.yyyy', new Date());
			return isValid(parsedDate) ? parsedDate : undefined;
		}
		catch {
			return undefined;
		}
	}

	static getIsPossibleValue(fieldModel: FieldModel): boolean {
		return (fieldModel?.possibleValues?.length ?? 0) > 0;
	}

	getFieldModel(datasetModelId: string, fieldId: string): FieldModel | undefined {
		return this.searchableFields.find(field =>
			field.datasetModelId === datasetModelId && field.id === fieldId
		);
	}

	getWorkflow(workflowId: string): Workflow | undefined {
		return this.searchableWorkflows.find(workflow => workflow.id === workflowId);
	}

	getFieldModelType(datasetModelId: string, fieldId: string): FieldModelType | undefined {
		return this.getFieldModel(datasetModelId, fieldId)?.type;
	}

	static getFieldValueForCriteria(fieldModel: FieldModel, value: string): string {
		//We ensure that when the field model is date, it is a complete date (searchableDate) to parse it directly to a Date object
		if(SearchComponent.getIsCompleteDate(fieldModel)) {
			const dateValue = new Date(value);
			return format(dateValue, 'dd.MM.yyyy');
		}
		return value;
	}

	static getIsCompleteDate(fieldModel: FieldModel): boolean {
		const dMYCollected = (fieldModel.type === FieldModelType.DATE || fieldModel.type === FieldModelType.DATE_SELECT) && fieldModel.withDays && fieldModel.withMonths && fieldModel.withYears;
		const dMYMandatory = fieldModel.type === FieldModelType.DATE || (fieldModel.type === FieldModelType.DATE_SELECT && fieldModel.daysMandatory && fieldModel.monthsMandatory && fieldModel.yearsMandatory);
		return dMYCollected && dMYMandatory;
	}


	getFieldValueForDisplay(result: ExtendedScopeSearchResult, fieldModel: FieldModel | undefined): string | undefined {
		if(!result.fieldValues || !fieldModel) {
			return undefined;
		}
		const rawValue = result.fieldValues[fieldModel.datasetModelId]?.[fieldModel.id];
		if(!rawValue) {
			return undefined;
		}

		if(SearchComponent.getIsPossibleValue(fieldModel)) {
			const localizationMap = this.getValueShortname(fieldModel.datasetModelId, fieldModel.id, rawValue);
			return localizationMap ? new LocalizeMapPipe().transform(localizationMap) : rawValue;
		}
		if(SearchComponent.getIsCompleteDate(fieldModel)) {
			const dateObj = SearchComponent.getDateObjectFromString(rawValue);
			return dateObj ? new DateUTCPipe().transform(dateObj) : rawValue;
		}

		return rawValue;
	}

	resetSearchCriteria(): void {
		//Reset all form controls without emitting to avoid multiple searches
		this.searchForm.reset({}, {emitEvent: false});
		this.parentScopeControl.setValue([], {emitEvent: false});
		this.fieldModelCriteria = [];
		this.paginator.pageIndex = 0;

		//Trigger a single search after all resets
		this.search();
	}

	navigateToScope(scopePk: number): void {
		this.router.navigate(['crf', scopePk]);
	}

	@HostListener('keydown.escape')
	onKeyDown(): void {
		this.resetSearchCriteria();
	}
}
