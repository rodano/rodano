import {Component, DestroyRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {MatFormField, MatInput, MatLabel} from '@angular/material/input';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {MatTable, MatTableModule} from '@angular/material/table';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {RouterLink} from '@angular/router';
import {debounceTime, merge, startWith, switchMap} from 'rxjs';
import {OverdueDTO} from '@core/model/overdue-dto';
import {PagedResultOverdueDTO} from '@core/model/paged-result-overdue-dto';
import {ScopeDTO} from '@core/model/scope-dto';
import {ConfigurationService} from '@core/services/configuration.service';
import {WidgetService} from '@core/services/widget.service';
import {EMPTY_PAGED_RESULT} from '@core/utilities/empty-paged-result';
import {OverdueWidgetSearch} from '@core/utilities/search/overdue-widget-search';
import {PaginatedSearch} from '@core/utilities/search/paginated-search';
import {DownloadDirective} from 'src/app/directives/download.component';
import {DateTimeUTCPipe} from 'src/app/pipes/date-time-utc.pipe';

@Component({
	selector: 'app-overdue-widget',
	imports: [
		ReactiveFormsModule,
		MatLabel,
		MatFormField,
		DownloadDirective,
		MatInput,
		MatButton,
		MatTableModule,
		MatSortModule,
		MatToolbar,
		MatToolbarRow,
		MatPaginator,
		DateTimeUTCPipe,
		RouterLink
	],
	templateUrl: './overdue.component.html',
	styleUrl: './overdue.component.css'
})
export class OverdueComponent implements OnInit {
	@Input() scopes?: ScopeDTO[];
	@Input() id: string;
	@Input() specificColumnName: string;

	@Output() scopesLoaded = new EventEmitter<number>();

	scopeOverdue: PagedResultOverdueDTO = EMPTY_PAGED_RESULT;
	overdueType: string;

	scopeName: string;
	parentScopeName: string;

	scopePks: number[];
	exportUrl: string;

	@ViewChild(MatTable, {static: true}) table: MatTable<OverdueDTO>;
	@ViewChild(MatSort, {static: true}) sort: MatSort;
	@ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

	filter = new FormControl('', {nonNullable: true});

	columnsToDisplay: string[] = [
		'parentScopeCode',
		'scopeCode',
		'lastDate',
		'daysOverdue'
	];

	constructor(private widgetService: WidgetService,
		private configService: ConfigurationService,
		private destroyRef: DestroyRef
	) {}

	ngOnInit(): void {
		this.sort.active = OverdueWidgetSearch.DEFAULT_SORT_BY;
		this.sort.direction = PaginatedSearch.getSortDirection(OverdueWidgetSearch.DEFAULT_SORT_ASCENDING);

		this.configService.getStudy().subscribe(config => {
			this.scopeName = config.scopeModels.filter(s => s.leaf)[0].shortname['en'];
			this.parentScopeName = config.scopeModels
				.filter(s => s.id === config.scopeModels
					.filter(s => s.leaf)[0].defaultParentId)[0].shortname['en'];
		});

		const scopePks = this.scopes?.map(s => s.pk);
		this.exportUrl = this.widgetService.getScopeOverdueExportUrl(this.id, scopePks);

		merge(
			this.sort.sortChange,
			this.paginator.page,
			this.filter.valueChanges.pipe(debounceTime(200))
		).pipe(
			takeUntilDestroyed(this.destroyRef),
			startWith({}),
			switchMap(() => {
				const search = new OverdueWidgetSearch();
				search.fullText = this.filter.value;
				search.scopePks = scopePks;
				search.sortBy = this.sort.active;
				search.orderAscending = PaginatedSearch.getOrderAscending(this.sort.direction);
				search.pageIndex = this.paginator.pageIndex;
				return this.widgetService.getScopeOverdue(this.id, search);
			})).subscribe(result => {
			this.scopeOverdue = result;
			this.scopesLoaded.emit(this.scopeOverdue.paging.total);
			this.scopesLoaded.complete();
		});
	}
}
