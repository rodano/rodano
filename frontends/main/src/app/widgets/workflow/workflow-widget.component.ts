import {Component, ViewChild, Input, Output, EventEmitter, OnInit, DestroyRef} from '@angular/core';
import {WidgetService} from '@core/services/widget.service';
import {MatPaginator} from '@angular/material/paginator';
import {merge} from 'rxjs';
import {MatTable, MatTableModule} from '@angular/material/table';
import {MatSort} from '@angular/material/sort';
import {switchMap, startWith, debounceTime} from 'rxjs/operators';
import {PagedResultWorkflowStatusInfo} from '@core/model/paged-result-workflow-status-info';
import {WorkflowWidgetDTO} from '@core/model/workflow-widget-dto';
import {WorkflowStatusInfo} from '@core/model/workflow-status-info';
import {ScopeDTO} from '@core/model/scope-dto';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {DownloadDirective} from '../../directives/download.component';
import {MatButton} from '@angular/material/button';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {MatFormField, MatInput, MatLabel} from '@angular/material/input';
import {RouterLink} from '@angular/router';
import {EMPTY_PAGED_RESULT} from '@core/utilities/empty-paged-result';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {WorkflowWidgetSearch} from '@core/utilities/search/workflow-widget-search';
import {DateUTCPipe} from 'src/app/pipes/date-utc.pipe';
import {PaginatedSearch} from '@core/utilities/search/paginated-search';

@Component({
	selector: 'app-workflow-widget',
	templateUrl: './workflow-widget.component.html',
	styleUrls: ['./workflow-widget.component.css'],
	imports: [
		MatLabel,
		MatFormField,
		MatInput,
		ReactiveFormsModule,
		MatTableModule,
		MatSort,
		MatToolbar,
		MatToolbarRow,
		MatButton,
		DownloadDirective,
		MatPaginator,
		LocalizeMapPipe,
		DateUTCPipe,
		RouterLink
	]
})
export class WorkflowWidgetComponent implements OnInit {
	@Input() id: string;
	@Input() scopes?: ScopeDTO[];
	@Output() workflowsLoaded = new EventEmitter<number>();

	widget: WorkflowWidgetDTO;
	columnsToDisplay: string[] = [];
	workflowStatuses: PagedResultWorkflowStatusInfo = EMPTY_PAGED_RESULT;

	filter = new FormControl('', {nonNullable: true});

	exportUrl: string;

	columnToApiPropertyMap: Record<string, string> = {
		WORKFLOW_LABEL: 'workflow',
		WORKFLOW_TRIGGER_MESSAGE: 'triggerMessage',
		STATUS_LABEL: 'status',
		STATUS_DATE: 'statusDate',
		PARENT_SCOPE_CODE: 'parentScopeCode',
		SCOPE_CODE: 'scopeCode',
		EVENT_LABEL: 'eventLabel',
		EVENT_DATE: 'eventDate',
		FORM_LABEL: 'formLabel',
		FORM_DATE: 'formDate',
		FIELD_LABEL: 'fieldLabel',
		FIELD_DATE: 'fieldDate'
	};

	@ViewChild(MatTable, {static: true}) table: MatTable<WorkflowStatusInfo>;
	@ViewChild(MatSort, {static: true}) sort: MatSort;
	@ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

	constructor(
		private widgetService: WidgetService,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		this.sort.active = WorkflowWidgetSearch.DEFAULT_SORT_BY;
		this.sort.direction = PaginatedSearch.getSortDirection(WorkflowWidgetSearch.DEFAULT_SORT_ASCENDING);

		this.widgetService.getWorkflowWidget(this.id).subscribe(widget => {
			this.widget = widget;
			this.columnsToDisplay = this.widget.columns.map(c => c.type);
		});

		const scopePks = this.scopes?.map(s => s.pk);
		this.exportUrl = this.widgetService.getWorkflowWidgetExportUrl(this.id, scopePks);

		merge(
			this.sort.sortChange,
			this.paginator.page,
			this.filter.valueChanges.pipe(debounceTime(200))
		).pipe(
			takeUntilDestroyed(this.destroyRef),
			startWith({}),
			switchMap(() => {
				const search = new WorkflowWidgetSearch();
				search.fullText = this.filter.value;
				search.scopePks = scopePks;
				search.sortBy = this.sort.active;
				search.orderAscending = PaginatedSearch.getOrderAscending(this.sort.direction);
				search.pageIndex = this.paginator.pageIndex;
				return this.widgetService.getWorkflowWidgetData(this.id, search);
			})
		).subscribe(result => {
			this.workflowStatuses = result;
			//We emit the number of workflows found and complete the stream as soon
			//as at least one workflow is found
			this.workflowsLoaded.emit(this.workflowStatuses.paging.total);
			this.workflowsLoaded.complete();
		});
	}

	getColumnHeader(columnType: string) {
		return this.widget.columns.find(c => c.type === columnType)?.shortname || {};
	}
}
