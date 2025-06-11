import {Component, ViewChild, DestroyRef, OnInit} from '@angular/core';
import {ReactiveFormsModule, FormControl, FormGroup} from '@angular/forms';
import {Observable, forkJoin, merge, of, iif, defer, fromEvent, EMPTY} from 'rxjs';
import {debounceTime, filter, map, switchMap, tap} from 'rxjs/operators';
import {ConfigurationService} from '@core/services/configuration.service';
import {ScopeDTO} from '@core/model/scope-dto';
import {ScopeSearch} from '@core/utilities/search/scope-search';
import {ScopeService} from '@core/services/scope.service';
import {ScopeModelDTO} from '@core/model/scope-model-dto';
import {WorkflowDTO} from '@core/model/workflow-dto';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {HttpParamsService} from '@core/services/http-params.service';
import {Router, ActivatedRoute, RouterLink, Routes} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {SelectScopeComponent} from './dialogs/create-scope/select-scope.component';
import {NotificationService} from 'src/app/services/notification.service';
import {WorkflowStatusService} from '@core/services/workflow-status.service';
import {WorkflowStatusSearch} from '@core/utilities/search/workflow-status-search';
import {WorkflowStatusDTO} from '@core/model/workflow-status-dto';
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
import {FieldModelDTO} from '@core/model/field-model-dto';
import {FieldModelType} from '@core/model/field-model-type';
import {MatSelect, MatOption} from '@angular/material/select';
import {FieldModelCriterion} from '@core/model/field-model-criterion';
import {format, parse, isValid} from 'date-fns';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {Operator} from '@core/model/operator';
import {DateUTCPipe} from '../pipes/date-utc.pipe';
import {MeService} from '@core/services/me.service';
import {ScopeMiniDTO} from '@core/model/scope-mini-dto';
import {LowerCasePipe} from '@angular/common';
import {FormModelDTO} from '@core/model/form-model-dto';
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

	leafScopeModel: ScopeModelDTO = {} as ScopeModelDTO;
	leafScopeModelParent: ScopeModelDTO = {} as ScopeModelDTO;
	writeAccessOnParent = false;

	leafScopeForms: FormModelDTO[] = [];

	workflows: WorkflowDTO[];
	parentScopes: ScopeMiniDTO[];
	searchableFields: FieldModelDTO[];

	fieldModelCriteria: FieldModelCriterion[] = [];

	scopes: ScopeDTO[] = [];

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
	importantWorkflowsPerScope: Record<number, WorkflowStatusDTO[]> = {};

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
				this.leafScopeModel = scopeModels.find(scopeModel => scopeModel.leaf) ?? ({} as ScopeModelDTO);
				this.leafScopeForms = results.formModels;
				this.leafScopeModelParent = scopeModels.find(scopeModel => scopeModel.id === this.leafScopeModel.defaultParentId) ?? ({} as ScopeModelDTO);

				this.workflows = results.workflows.filter(ws => this.leafScopeModel.workflowIds.includes(ws.id));
				this.parentScopes = results.parentScopes.filter(scope => scope.modelId === this.leafScopeModelParent.id);
				this.searchableFields = results.searchableFields;

				//fill in the fieldModelMap and the columnsToDisplay
				this.searchableFields.forEach(fieldModel => {
					this.columnsToDisplay.push(this.getSFFormControlName(fieldModel));
					this.fieldModelMap.set(this.getSFFormControlName(fieldModel), fieldModel.shortname);
				});

				this.workflows.forEach(workflow => {
					this.columnsToDisplay.push(workflow.id);
					this.workflowStatusModelMap.set(workflow.id, workflow.shortname);
					workflow.states.forEach(state => {
						this.statusModelMap.set(`${workflow.id}_${state.id}`, state.shortname);
					});
					//Handle the case where the workflow is an aggregator
					if(workflow.aggregator && workflow.aggregatedWorkflowId) {
						this.aggregatedWorkflowMap.set(workflow.id, workflow.aggregatedWorkflowId);
					}
				});
				this.setupFormListeners();
				this.loadData();
				//Now return the parentsWithWriteAccess observable
				return this.scopeRelationService.getParents(this.leafScopeModel.id, Rights.WRITE).pipe(
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
				scopeSearchObj.scopeModelId = this.leafScopeModel.id;
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

	getWSFormControlName(workflowDTO: WorkflowDTO): string {
		return `ws_${workflowDTO.id}`;
	}

	getSFFormControlName(fieldModelDTO: FieldModelDTO): string {
		return `${fieldModelDTO.datasetModelId}/${fieldModelDTO.id}`;
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
				return this.scopeService.getCandidate(parentScope.pk, this.leafScopeModel.id);
			}),
			switchMap(candidateScope => this.scopeService.create(candidateScope)),
			switchMap(newScopeDTO =>
				//get the forms for the new scope
				this.formService.searchOnScope(newScopeDTO.pk).pipe(
					map(forms => ({newScopeDTO, forms}))
				)
			)
		).subscribe(({newScopeDTO, forms}) => {
			if(forms.length > 0) {
				this.router.navigate(['crf', newScopeDTO.pk, 'form', forms[0].pk]);
			}
			else {
				this.router.navigate(['crf', newScopeDTO.pk]);
			}
			this.notificationService.showSuccess(`${newScopeDTO.shortname} created`);
		});
	}

	private createParentScopeFormControls(parentScopes: ScopeMiniDTO[]): Observable<any>[] {
		return parentScopes.map(parentScopeDTO => {
			const control = new FormControl();
			this.searchForm.addControl(parentScopeDTO.id, control);
			return control.valueChanges;
		});
	}

	private createWSStateChanges(workflows: WorkflowDTO[]): Observable<any>[] {
		const observables: Observable<any>[] = [];

		workflows.forEach(workflow => {
			const control = new FormControl();
			this.searchForm.addControl(this.getWSFormControlName(workflow), control);
			observables.push(control.valueChanges);
		});
		return observables;
	}

	private createSFStateChanges(searchableFields: FieldModelDTO[]): Observable<any>[] {
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
			Object.keys(scopeSearch.workflowStates).forEach(workflow => {
				const workflowDTO = this.getWorkflowDTO(workflow);
				if(workflowDTO) {
					const control = this.searchForm.controls[this.getWSFormControlName(workflowDTO)];
					control.setValue(scopeSearch.workflowStates[workflow], {emitEvent: false});
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

				const fieldModel = this.getFieldModelDTO(datasetModelId, fieldModelId);
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
		search.scopeModelId = this.leafScopeModel.id;
		search.workflowStates = {};
		search.fieldModelCriteria = '';

		this.workflows.forEach(workflow => {
			const control = this.searchForm.controls[this.getWSFormControlName(workflow)];
			const value = control.value;
			const aggregatedWorkflowId = this.aggregatedWorkflowMap.get(workflow.id);

			if(value) {
				//If the workflow is an aggregated workflow, use the aggregatedWorkflowId
				if(aggregatedWorkflowId) {
					search.workflowStates[aggregatedWorkflowId] = value;
				}
				else {
					search.workflowStates[workflow.id] = value;
				}
			}

			//Clean up empty workflow states
			if(!search.workflowStates[workflow.id]?.length) {
				delete search.workflowStates[workflow.id];
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
						&& criteria.fieldModelId === fieldModel.id
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
						fieldModelId: fieldModel.id
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
		search.workflowIds = this.workflows.map(w => w.id);
		search.ancestorScopePks = this.scopes.map(s => s.pk);
		search.filterExpectedEvents = true;
		search.pageSize = 20;

		this.workflowStatusService.search(search).pipe(
			map(results => {
				const worklowStatuses = results.objects;

				const res: Record<number, WorkflowStatusDTO[]> = {};

				this.scopes.forEach(scope => {
					res[scope.pk] = worklowStatuses.filter(ws => ws.scopeFk === scope.pk);
				});

				return res;
			}),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(result => this.importantWorkflowsPerScope = result);
	}

	private openScopeSelectDialog(parentScopes: ScopeDTO[], leafScopeModel: ScopeModelDTO): Observable<ScopeDTO | undefined> {
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
			if(fieldModel.id === fieldId && fieldModel.datasetModelId === datasetModelId && fieldModel.possibleValues) {
				const value = fieldModel.possibleValues?.find(value => value.id === field)?.shortname;
				if(value) {
					return value;
				}
			}
		}
		return field;
	}

	getDateObject(datasetModelId: string, fieldId: string, field: any): Date | undefined {
		for(const fieldModel of this.searchableFields) {
			if(fieldModel.id === fieldId && fieldModel.datasetModelId === datasetModelId && fieldModel.type === FieldModelType.DATE) {
				const parsedDate = parse(field, 'dd.MM.yyyy', new Date());
				return isValid(parsedDate) ? parsedDate : undefined;
			}
		}
		return undefined;
	}

	getIsPossibleValue(datasetModelId: string, fieldId: string): boolean {
		for(const fieldModel of this.searchableFields) {
			if(fieldModel.id === fieldId && fieldModel.datasetModelId === datasetModelId) {
				return fieldModel.possibleValues.length > 0;
			}
		}
		return false;
	}

	getIsCompleteDate(datasetModelId: string, fieldId: string): boolean {
		for(const fieldModel of this.searchableFields) {
			if(fieldModel.id === fieldId && fieldModel.datasetModelId === datasetModelId) {
				return (fieldModel.type === FieldModelType.DATE || fieldModel.type === FieldModelType.DATE_SELECT) && fieldModel.daysMandatory && fieldModel.monthsMandatory && fieldModel.yearsMandatory;
			}
		}
		return false;
	}

	getFieldModelDTO(datasetModelId: string, fieldId: string): FieldModelDTO | undefined {
		for(const fieldModel of this.searchableFields) {
			if(fieldModel.id === fieldId && fieldModel.datasetModelId === datasetModelId) {
				return fieldModel;
			}
		}
		return undefined;
	}

	getWorkflowDTO(workflowId: string): WorkflowDTO | undefined {
		for(const workflow of this.workflows) {
			if(workflow.id === workflowId) {
				return workflow;
			}
		}
		return undefined;
	}

	getFieldModelType(datasetModelId: string, fieldId: string): FieldModelType | undefined {
		const fieldModelDTO = this.getFieldModelDTO(datasetModelId, fieldId);
		if(fieldModelDTO) {
			return fieldModelDTO.type;
		}
		return undefined;
	}

	getFieldValueForCriteria(fieldModel: FieldModelDTO, value: string): string {
		if(this.getIsSearchableDate(fieldModel)) {
			const dateValue = new Date(value);
			return format(dateValue, 'dd.MM.yyyy');
		}
		return value;
	}

	getIsSearchableDate(fieldModel: FieldModelDTO): boolean {
		if(fieldModel) {
			return (fieldModel.type === FieldModelType.DATE || fieldModel.type === FieldModelType.DATE_SELECT);
			//&& (fieldModel.daysMandatory && fieldModel.monthsMandatory && fieldModel.yearsMandatory)
		}
		return false;
	}

	resetFieldCriteria(fieldModel: FieldModelDTO): void {
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
	getSearchableFieldFromFormControlId(formControlId: string): FieldModelDTO | undefined {
		const [datasetModelId, fieldModelId] = formControlId.split('/');
		return this.getFieldModelDTO(datasetModelId, fieldModelId);
	}
}
