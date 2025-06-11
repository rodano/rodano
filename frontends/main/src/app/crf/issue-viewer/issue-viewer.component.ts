import {Component, DestroyRef, Input, OnInit, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {MatTable, MatTableModule} from '@angular/material/table';
import {merge} from 'rxjs';
import {startWith, switchMap} from 'rxjs/operators';
import {PagedResultWorkflowStatusDTO} from '@core/model/paged-result-workflow-status-dto';
import {WorkflowStatusDTO} from '@core/model/workflow-status-dto';
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
		RouterLink,
		MatPaginator,
		LocalizeMapPipe,
		CapitalizeFirstPipe,
		DateUTCPipe
	]
})
export class IssueViewerComponent implements OnInit {
	@Input() scopePks: number[];
	@Input() eventPks?: number[];

	workflowIds: string[] = [];
	stateIds: string[] = [];
	columnsToDisplay = [
		'eventShortname',
		'eventDate',
		'fieldShortname',
		'workflowId',
		'stateId',
		'triggerMessage'
	];

	workflowStatuses: PagedResultWorkflowStatusDTO = EMPTY_PAGED_RESULT;

	@ViewChild(MatTable, {static: true}) table: MatTable<WorkflowStatusDTO>;
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

		this.configurationService.getWorkflows().subscribe(
			workflows => {
				//display only workflows with at least one important states
				workflows.forEach(workflow => {
					workflow.states.forEach(state => {
						if(state.important) {
							this.workflowIds.push(workflow.id);
							this.stateIds.push(state.id);
						}
					});
				});

				merge(
					this.sort.sortChange,
					this.paginator.page
				).pipe(
					takeUntilDestroyed(this.destroyRef),
					startWith({}),
					switchMap(() => {
						const search = new WorkflowStatusSearch();
						search.workflowIds = this.workflowIds;
						search.stateIds = this.stateIds;
						search.scopePks = this.scopePks;
						search.eventPks = this.eventPks;
						search.pageIndex = this.paginator.pageIndex;
						search.sortBy = this.sort.active;
						search.orderAscending = PaginatedSearch.getOrderAscending(this.sort.direction);
						return this.workflowStatusService.search(search);
					})
				).subscribe(result => {
					this.workflowStatuses = result;
				});
			});
	}
}
