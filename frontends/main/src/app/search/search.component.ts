import {Component, DestroyRef, OnInit, ViewChild} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {defer, EMPTY, forkJoin, fromEvent, iif, merge, Observable, of} from 'rxjs';
import {debounceTime, filter, map, switchMap, tap} from 'rxjs/operators';
import {ConfigurationService} from '@core/services/configuration.service';
import {Scope} from '@core/model/scope';
import {ScopeSearch} from '@core/utilities/search/scope-search';
import {ScopeService} from '@core/services/scope.service';
import {ScopeModel} from '@core/model/scope-model';
import {Workflow} from '@core/model/workflow';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {HttpParamsService} from '@core/services/http-params.service';
import {ActivatedRoute, Router, RouterLink, Routes} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {SelectScopeComponent} from './dialogs/create-scope/select-scope.component';
import {NotificationService} from 'src/app/services/notification.service';
import {WorkflowStatusService} from '@core/services/workflow-status.service';
import {WorkflowStatusSearch} from '@core/utilities/search/workflow-status-search';
import {WorkflowStatus} from '@core/model/workflow-status';
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
import {MatOption, MatSelect} from '@angular/material/select';
import {FieldModelCriterion} from '@core/model/field-model-criterion';
import {format, isValid, parse} from 'date-fns';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {Operator} from '@core/model/operator';
import {DateUTCPipe} from '../pipes/date-utc.pipe';
import {MeService} from '@core/services/me.service';
import {ScopeMini} from '@core/model/scope-mini';
import {LowerCasePipe} from '@angular/common';
import {FormModel} from '@core/model/form-model';
import {FormService} from '@core/services/form.service';
import {ScopeRelationsService} from '@core/services/scope-relations.service';
import {Rights} from '@core/model/rights';

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
		RouterLink,
		MatDivider,
		MatToolbarModule,
		MatPaginatorModule,
		LocalizeMapPipe,
		MatSortModule,
		MatSelect,
		MatOption,
		MatDatepickerModule,
		DateUTCPipe,
		LowerCasePipe
	]
})
export class SearchComponent implements OnInit {
	static ROUTES: Routes = [
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

	leafScopeModel: ScopeModel = {} as ScopeModel;
	leafScopeModelParent: ScopeModel = {} as ScopeModel;
	writeAccessOnParent = false;

	leafScopeForms: FormModel[] = [];

	workflows: Workflow[];
	parentScopes: ScopeMini[];
	searchableFields: FieldModel[];

	fieldModelCriteria: FieldModelCriterion[] = [];

	scopes: Scope[] = [];

	searchForm = new FormGroup<Record<string, FormControl<any>>>({
		scopeCode: new FormControl()
	});

	parentScopeControl = new FormControl();
	workflowControl = new FormControl();

	resultsLength: number;
	pageSize: number;

	//Map to hold the workflowStatus model shortname
	workflowStatusModelMap = new Map<string, Record<string, string>>();

	statusModelMap = new Map<string, Record<string, string>>();

	fieldModelMap = new Map<string, Record<string, string>>();

	//Map to hold the aggregated workflow ID for a workflow aggregator
	aggregatedWorkflowMap = new Map<string, string>();

	//TODO filter out only the workflows that we want visible to the user
	importantWorkflowsPerScope: Record<number, WorkflowStatus[]> = {};

	loading = false;

	@ViewChild(MatSort, {static: true}) sort: MatSort;
	@ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

	constructor(
		private configurationService: ConfigurationService,
		private scopeService: ScopeService,
		private meService: MeService,
		private workflowStatusService: WorkflowStatusService,
		private httpParamsService: HttpParamsService,
		private notificationService: NotificationService,
		private router: Router,
		private route: ActivatedRoute,
		private dialog: MatDialog,
		private destroyRef: DestroyRef,
		private formService: FormService,
		private scopeRelationService: ScopeRelationsService
	) {}

	ngOnInit(): void {
		this.initializeData();
		this.sort.active = 'scopeCode';
		this.sort.direction = 'asc';
	}

	private initializeData(): void {
		forkJoin({
			scopeModel: this.configurationService.getScopeModels(),
			workflows: this.configurationService.getWorkflows(),
			parentScopes: this.meService.getScopes(undefined, true, false),
			searchableFields: this.configurationService.getSearchableFieldModels(),
			formModels: this.configurationService.getLeafScopeModelFormModels()
		}).pipe(
			takeUntilDestroyed(this.destroyRef),
			switchMap(results => {
				const scopeModels = results.scopeModel;
				this.leafScopeModel = scopeModels.find(scopeModel => scopeModel.leaf) ?? ({} as ScopeModel);
				this.leafScopeForms = results.formModels;
				this.leafScopeModelParent = scopeModels.find(scopeModel => scopeModel.scopeModelId === this.leafScopeModel.defaultParentId) ?? ({} as ScopeModel);

				this.workflows = results.workflows.filter(ws => this.leafScopeModel.workflowIds.includes(ws.workflowId));
				this.parentScopes = results.parentScopes.filter(scope => scope.modelId === this.leafScopeModelParent.scopeModelId);
				this.searchableFields = results.searchableFields;

				//fill in the fieldModelMap and the columnsToDisplay
				this.searchableFields.forEach(fieldModel => {
					this.columnsToDisplay.push(this.getSFFormControlName(fieldModel));
					this.fieldModelMap.set(this.getSFFormControlName(fieldModel), fieldModel.shortname);
				});

				this.workflows.forEach(workflow => {
					this.columnsToDisplay.push(workflow.workflowId);
					this.workflowStatusModelMap.set(workflow.workflowId, workflow.shortname);
					workflow.states.forEach(state => {
						this.statusModelMap.set(`${workflow.workflowId}_${state.workflowStateId}`, state.shortname);
					});
					//Handle the case where the workflow is an aggregator
					if(workflow.aggregator && workflow.aggregatedWorkflowId) {
						this.aggregatedWorkflowMap.set(workflow.workflowId, workflow.aggregatedWorkflowId);
					}
				});
				this.setupFormListeners();
				this.loadData();
				//Now return the parentsWithWriteAccess observable
				return this.scopeRelationService.getParents(this.leafScopeModel.scopeModelId, Rights.WRITE).pipe(
					map(parentsWithWriteAccess => ({
						parentsWithWriteAccess
					}))
				);
			})
		).subscribe(({parentsWithWriteAccess}) => {
			this.writeAccessOnParent = parentsWithWriteAccess.length > 0;
		});
	}

	private setupFormListeners(): void {
		const scopeCode$ = this.searchForm.controls.scopeCode.valueChanges.pipe(debounceTime(300));
		const workflows$ = this.createWSStateChanges(this.workflows);
		const parentScopes$ = this.createParentScopeFormControls(this.parentScopes);
		const fieldModels$ = this.createSFStateChanges(this.searchableFields);
		const scopeCodeControl$ = this.parentScopeControl.valueChanges;

		merge(
			...workflows$,
			...parentScopes$,
			...fieldModels$,
			scopeCode$,
			scopeCodeControl$,
			this.paginator.page,
			this.sort.sortChange
		).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(() => {
			this.search();
		});
	}

	search(): void {
		const path = this.httpParamsService.toHttpParams(this.generateScopeSearch());
		this.router.navigateByUrl(`/search?${path.toString()}`);
	}

	private loadData(): void {
		this.route.queryParams.pipe(
			tap(() => this.loading = true),
			switchMap(params => {
				let scopeSearchObj: ScopeSearch;

				//if no parameters are provided, use default search object
				if(Object.entries(params).length === 0) {
					scopeSearchObj = new ScopeSearch();
				}
				else {
					scopeSearchObj = this.httpParamsService.toScopeSearch(params);
				}

				//Always set the scope model to PATIENT
				scopeSearchObj.scopeModelId = this.leafScopeModel.scopeModelId;
				this.syncFormAndUrl(scopeSearchObj);

				return this.scopeService.search(scopeSearchObj);
			}),
			tap(() => this.loading = false)
		).subscribe(scopeResults => {
			this.scopes = scopeResults.objects;
			this.resultsLength = scopeResults.paging.total;
			this.updateImportantWorkflows();
		});
	}

	getWSFormControlName(workflow: Workflow): string {
		return `ws_${workflow.workflowId}`;
	}

	getSFFormControlName(fieldModel: FieldModel): string {
		return `${fieldModel.datasetModelId}/${fieldModel.fieldModelId}`;
	}

	createPatient() {
		const scopeSearch = new ScopeSearch();
		//TODO improve this as soon as the scopes search supports multiple scopeModelIds
		scopeSearch.scopeModelId = this.leafScopeModel.parentIds[0];

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
					defer(() => this.openScopeSelectDialog(parentScopes, this.leafScopeModel)),
					of(parentScopes[0])
				);
			}),
			switchMap(parentScope => {
				if(!parentScope) {
					return EMPTY;
				}
				return this.scopeService.getCandidate(parentScope.pk, this.leafScopeModel.scopeModelId);
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

	private createParentScopeFormControls(parentScopes: ScopeMini[]): Observable<any>[] {
		return parentScopes.map(parentScope => {
			const control = new FormControl();
			this.searchForm.addControl(parentScope.modelId, control);
			return control.valueChanges;
		});
	}

	private createWSStateChanges(workflows: Workflow[]): Observable<any>[] {
		const observables: Observable<any>[] = [];

		workflows.forEach(workflow => {
			const control = new FormControl();
			this.searchForm.addControl(this.getWSFormControlName(workflow), control);
			observables.push(control.valueChanges);
		});
		return observables;
	}

	private createSFStateChanges(searchableFields: FieldModel[]): Observable<any>[] {
		const observables: Observable<any>[] = [];

		searchableFields.forEach(fieldModel => {
			const control = new FormControl();
			this.searchForm.addControl(this.getSFFormControlName(fieldModel), control);
			if(this.getIsSearchableDate(fieldModel)) {
				//Add a listener for the "Enter" key press
				const enterKeyPress$ = fromEvent<KeyboardEvent>(document, 'keydown').pipe(
					filter((event: {key: string}) => event.key === 'Enter'),
					map(() => control.value) //Emit the current value of the control
				);
				observables.push(enterKeyPress$);
			}
			else {observables.push(control.valueChanges);}
		});
		return observables;
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

		//Sync the table paginator
		this.paginator.pageIndex = scopeSearch.pageIndex;
		this.paginator.pageSize = scopeSearch.pageSize;

		//Sync the sort
		//transform 'scopeCode' to 'code'
		scopeSearch.sortBy = this.sort.active === 'scopeCode' ? 'code' : '';
		scopeSearch.orderAscending = this.sort.direction === 'asc';
	}

	private generateScopeSearch(): ScopeSearch {
		const search = new ScopeSearch();
		search.fullText = this.searchForm.controls.scopeCode.value;
		search.scopeModelId = this.leafScopeModel.scopeModelId;
		search.workflowStates = {};
		search.fieldModelCriteria = '';

		this.workflows.forEach(workflow => {
			const control = this.searchForm.controls[this.getWSFormControlName(workflow)];
			const value = control.value;
			const aggregatedWorkflowId = this.aggregatedWorkflowMap.get(workflow.workflowId);

			if(value) {
				//If the workflow is an aggregated workflow, use the aggregatedWorkflowId
				if(aggregatedWorkflowId) {
					search.workflowStates[aggregatedWorkflowId] = value;
				}
				else {
					search.workflowStates[workflow.workflowId] = value;
				}
			}

			//Clean up empty workflow states
			if(!search.workflowStates[workflow.workflowId]?.length) {
				delete search.workflowStates[workflow.workflowId];
			}
			if(aggregatedWorkflowId && !search.workflowStates[aggregatedWorkflowId]?.length) {
				delete search.workflowStates[aggregatedWorkflowId];
			}
		});

		this.searchableFields.forEach(fieldModel => {
			const controlName = this.getSFFormControlName(fieldModel);
			const control = this.searchForm.controls[controlName];
			if(control?.value) {
				const isString = fieldModel.type === FieldModelType.STRING;
				const value = this.getFieldValueForCriteria(fieldModel, control.value);

				//For string fields, require at least 3 characters
				if(isString && (!value || value.length < 3)) {
					return;
				}

				const existingCriteria = this.fieldModelCriteria.find(
					criteria =>
						criteria.datasetModelId === fieldModel.datasetModelId
						&& criteria.fieldModelId === fieldModel.fieldModelId
				);

				if(existingCriteria) {
					//Update the existing criterion
					existingCriteria.value = value;
					existingCriteria.operator = isString
						? Operator.CONTAINS
						: Operator.EQUALS;
				}
				else {
					//Add a new criterion
					this.fieldModelCriteria.push({
						value: value,
						operator: isString
							? Operator.CONTAINS
							: Operator.EQUALS,
						datasetModelId: fieldModel.datasetModelId,
						fieldModelId: fieldModel.fieldModelId
					});
				}
				//Update the search object
				search.fieldModelCriteria = JSON.stringify(this.fieldModelCriteria);
			}
		});

		const parentScopeControl = this.parentScopeControl;
		if(parentScopeControl.value) {
			search.parentPks = parentScopeControl.value;
		}

		search.pageIndex = this.paginator.pageIndex;
		search.pageSize = this.paginator.pageSize;

		//Sync the sort
		//transform 'scopeCode' to 'code'
		search.sortBy = this.sort.active === 'scopeCode' ? 'code' : '';
		search.orderAscending = this.sort.direction === 'asc';

		return search;
	}

	private updateImportantWorkflows(): void {
		const search = new WorkflowStatusSearch();
		search.workflowIds = this.workflows.map(w => w.workflowId);
		search.ancestorScopePks = this.scopes.map(s => s.pk);
		search.filterExpectedEvents = true;
		search.pageSize = 20;

		this.workflowStatusService.search(search).pipe(
			map(results => {
				const worklowStatuses = results.objects;

				const res: Record<number, WorkflowStatus[]> = {};

				this.scopes.forEach(scope => {
					res[scope.pk] = worklowStatuses.filter(ws => ws.scopeFk === scope.pk);
				});

				return res;
			}),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(result => this.importantWorkflowsPerScope = result);
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

	getWorkflowScope() {
		return this.workflows;
	}

	getStatusModel(workflowId: string, stateId: string) {
		return this.statusModelMap.get(`${workflowId}_${stateId}`);
	}

	getValueShortname(datasetModelId: string, fieldId: string, field: any): Record<string, string> | undefined {
		for(const fieldModel of this.searchableFields) {
			if(fieldModel.id === fieldId && fieldModel.possibleValues) {
				const value = fieldModel.possibleValues?.find(value => value.possibleValueId === field)?.shortname;
				if(value) {
					return value;
				}
			}
		}
		return field;
	}

	getDateObject(datasetModelId: string, fieldId: string, field: any): Date | undefined {
		for(const fieldModel of this.searchableFields) {
			if(fieldModel.fieldModelId === fieldId && fieldModel.datasetModelId === datasetModelId && fieldModel.type === FieldModelType.DATE) {
				const parsedDate = parse(field, 'dd.MM.yyyy', new Date());
				return isValid(parsedDate) ? parsedDate : undefined;
			}
		}
		return undefined;
	}

	getIsPossibleValue(datasetModelId: string, fieldId: string): boolean {
		for(const fieldModel of this.searchableFields) {
			if(fieldModel.id === fieldId) {
				return fieldModel.possibleValues.length > 0;
			}
		}
		return false;
	}

	getIsCompleteDate(datasetModelId: string, fieldId: string): boolean {
		for(const fieldModel of this.searchableFields) {
			if(fieldModel.id === fieldId) {
				return (fieldModel.type === FieldModelType.DATE || fieldModel.type === FieldModelType.DATE_SELECT) && fieldModel.daysMandatory && fieldModel.monthsMandatory && fieldModel.yearsMandatory;
			}
		}
		return false;
	}

	getFieldModel(datasetModelId: string, fieldId: string): FieldModel | undefined {
		for(const fieldModel of this.searchableFields) {
			if(fieldModel.fieldModelId === fieldId && fieldModel.datasetModelId === datasetModelId) {
				return fieldModel;
			}
		}
		return undefined;
	}

	getWorkflow(workflowId: string): Workflow | undefined {
		for(const workflow of this.workflows) {
			if(workflow.workflowId === workflowId) {
				return workflow;
			}
		}
		return undefined;
	}

	getFieldModelType(datasetModelId: string, fieldId: string): FieldModelType | undefined {
		const fieldModel = this.getFieldModel(datasetModelId, fieldId);
		if(fieldModel) {
			return fieldModel.type;
		}
		return undefined;
	}

	getFieldValueForCriteria(fieldModel: FieldModel, value: string): string {
		if(this.getIsSearchableDate(fieldModel)) {
			const dateValue = new Date(value);
			return format(dateValue, 'dd.MM.yyyy');
		}
		return value;
	}

	getIsSearchableDate(fieldModel: FieldModel): boolean {
		if(fieldModel) {
			return (fieldModel.type === FieldModelType.DATE || fieldModel.type === FieldModelType.DATE_SELECT);
			//&& (fieldModel.daysMandatory && fieldModel.monthsMandatory && fieldModel.yearsMandatory)
		}
		return false;
	}

	resetFieldCriteria(fieldModel: FieldModel): void {
		const controlName = this.getSFFormControlName(fieldModel);
		const control = this.searchForm.controls[controlName];
		if(control) {
			control.setValue(null, {emitEvent: true});
		}
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
	getSearchableFieldFromFormControlId(formControlId: string): FieldModel | undefined {
		const [datasetModelId, fieldModelId] = formControlId.split('/');
		return this.getFieldModel(datasetModelId, fieldModelId);
	}
}
