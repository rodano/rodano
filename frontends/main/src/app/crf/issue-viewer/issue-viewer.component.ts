import {Component, DestroyRef, Input, OnChanges, OnInit, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatProgressBar} from '@angular/material/progress-bar';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {MatTable, MatTableModule} from '@angular/material/table';
import {merge, Subject} from 'rxjs';
import {map, startWith, switchMap} from 'rxjs/operators';
import {PagedResultWorkflowStatus} from '@core/model/paged-result-workflow-status';
import {WorkflowStatus} from '@core/model/workflow-status';
import {WorkflowStatusService} from '@core/services/workflow-status.service';
import {WorkflowStatusSearch} from '@core/utilities/search/workflow-status-search';
import {CapitalizeFirstPipe} from '../../pipes/capitalize-first.pipe';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {RouterLink} from '@angular/router';
import {EMPTY_PAGED_RESULT} from '@core/utilities/empty-paged-result';
import {ConfigurationService} from '@core/services/configuration.service';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {DateUTCPipe} from 'src/app/pipes/date-utc.pipe';
import {PaginatedSearch} from '@core/utilities/search/paginated-search';

@Component({
	selector: 'app-issue-viewer',
	templateUrl: './issue-viewer.component.html',
	styleUrls: ['./issue-viewer.component.css'],
	imports: [
		MatTableModule,
		MatSortModule,
		MatProgressBar,
		RouterLink,
		MatPaginator,
		LocalizeMapPipe,
		CapitalizeFirstPipe,
		DateUTCPipe
	]
})
export class IssueViewerComponent implements OnInit, OnChanges {
	@Input() scopePks: number[];
	@Input() eventPks?: number[];

	loading = false;
	columnsToDisplay = [
		'eventShortname',
		'eventDate',
		'fieldShortname',
		'workflowId',
		'stateId',
		'triggerMessage'
	];

	refreshSearch$ = new Subject<void>();

	workflowStatuses: PagedResultWorkflowStatus = EMPTY_PAGED_RESULT;

	@ViewChild(MatTable, {static: true}) table: MatTable<WorkflowStatus>;
	@ViewChild(MatSort, {static: true}) sort: MatSort;
	@ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

	constructor(
		private configurationService: ConfigurationService,
		private workflowStatusService: WorkflowStatusService,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		this.sort.active = WorkflowStatusSearch.DEFAULT_SORT_BY;
		this.sort.direction = PaginatedSearch.getSortDirection(WorkflowStatusSearch.DEFAULT_SORT_ASCENDING);

		this.configurationService.getWorkflows().pipe(
			takeUntilDestroyed(this.destroyRef),
			switchMap(workflows => {
				const workflowIds: string[] = [];
				const stateIds: string[] = [];

				//display only workflows with at least one important states
				workflows.forEach(workflow => {
					workflow.states.forEach(state => {
						if(state.important) {
							workflowIds.push(workflow.id);
							stateIds.push(state.id);
						}
					});
				});

				return merge(
					this.refreshSearch$.asObservable(),
					this.sort.sortChange,
					this.paginator.page
				).pipe(
					startWith({}),
					map(() => ({workflowIds, stateIds}))
				);
			}),
			switchMap(({workflowIds, stateIds}) => {
				this.loading = true;
				const search = new WorkflowStatusSearch();
				search.workflowIds = workflowIds;
				search.stateIds = stateIds;
				search.scopePks = this.scopePks;
				search.eventPks = this.eventPks;
				search.pageIndex = this.paginator.pageIndex;
				search.sortBy = this.sort.active;
				search.orderAscending = PaginatedSearch.getOrderAscending(this.sort.direction);
				return this.workflowStatusService.search(search);
			})
		).subscribe(statuses => {
			this.workflowStatuses = statuses;
			this.loading = false;
		});
	}

	ngOnChanges() {
		this.refreshSearch$.next();
	}
}
