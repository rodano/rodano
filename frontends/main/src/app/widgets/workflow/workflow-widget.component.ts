import {ChangeDetectionStrategy, Component, ViewChild, computed, input, output, OnInit, DestroyRef, signal} from '@angular/core';
import {WidgetService} from '@core/services/widget.service';
import {MatPaginator} from '@angular/material/paginator';
import {merge} from 'rxjs';
import {MatTable, MatTableModule} from '@angular/material/table';
import {MatSort} from '@angular/material/sort';
import {switchMap, startWith, debounceTime} from 'rxjs/operators';
import {PagedResultWorkflowStatusInfo} from '@core/model/paged-result-workflow-status-info';
import {WorkflowStatusInfo} from '@core/model/workflow-status-info';
import {Scope} from '@core/model/scope';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {DownloadDirective} from '../../directives/download.component';
import {MatButton} from '@angular/material/button';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {MatFormField, MatInput, MatLabel} from '@angular/material/input';
import {MatProgressBar} from '@angular/material/progress-bar';
import {RouterLink} from '@angular/router';
import {EMPTY_PAGED_RESULT} from '@core/utilities/empty-paged-result';
import {takeUntilDestroyed, toSignal, toObservable} from '@angular/core/rxjs-interop';
import {WorkflowWidgetSearch} from '@core/utilities/search/workflow-widget-search';
import {DateUTCPipe} from 'src/app/pipes/date-utc.pipe';
import {PaginatedSearch} from '@core/utilities/search/paginated-search';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	selector: 'app-workflow-widget',
	templateUrl: './workflow-widget.component.html',
	styleUrls: ['./workflow-widget.component.css'],
	imports: [
		MatLabel,
		MatFormField,
		MatInput,
		ReactiveFormsModule,
		MatProgressBar,
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
	readonly id = input.required<string>();
	readonly scopes = input<Scope[]>();
	readonly workflowsLoaded = output<number>();
	private workflowsEmitted = false;

	readonly widget = toSignal(
		toObservable(this.id).pipe(
			switchMap(id => this.widgetService.getWorkflowWidget(id))
		)
	);

	readonly columnsToDisplay = computed(() => this.widget()?.columns.map(c => c.type) ?? []);
	readonly workflowStatuses = signal<PagedResultWorkflowStatusInfo>(EMPTY_PAGED_RESULT);
	readonly loading = signal(false);

	filter = new FormControl('', {nonNullable: true});

	private readonly scopePks = computed(() => this.scopes()?.map(s => s.pk));
	readonly exportUrl = computed(() => this.widgetService.getWorkflowWidgetExportUrl(this.id(), this.scopePks()));

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

		merge(
			this.sort.sortChange,
			this.paginator.page,
			this.filter.valueChanges.pipe(debounceTime(200))
		).pipe(
			takeUntilDestroyed(this.destroyRef),
			startWith({}),
			switchMap(() => {
				this.loading.set(true);
				const search = new WorkflowWidgetSearch();
				search.fullText = this.filter.value;
				search.scopePks = this.scopePks();
				search.sortBy = this.sort.active;
				search.orderAscending = PaginatedSearch.getOrderAscending(this.sort.direction);
				search.pageIndex = this.paginator.pageIndex;
				return this.widgetService.getWorkflowWidgetData(this.id(), search);
			})
		).subscribe(result => {
			this.workflowStatuses.set(result);
			//emit result only the first time, not when state changes
			if(!this.workflowsEmitted) {
				this.workflowsLoaded.emit(result.paging.total);
				this.workflowsEmitted = true;
			}
			this.loading.set(false);
		});
	}

	getColumnHeader(columnType: string) {
		return this.widget()?.columns.find(c => c.type === columnType)?.label || {};
	}
}
