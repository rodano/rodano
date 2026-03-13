import {ChangeDetectionStrategy, Component, computed, DestroyRef, input, OnInit, output, ViewChild, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {MatFormField, MatInput, MatLabel} from '@angular/material/input';
import {MatPaginator} from '@angular/material/paginator';
import {MatProgressBar} from '@angular/material/progress-bar';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {MatTable, MatTableModule} from '@angular/material/table';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {RouterLink} from '@angular/router';
import {debounceTime, merge, startWith, switchMap} from 'rxjs';
import {Overdue} from '@core/model/overdue';
import {PagedResultOverdue} from '@core/model/paged-result-overdue';
import {Scope} from '@core/model/scope';
import {ConfigurationService} from '@core/services/configuration.service';
import {WidgetService} from '@core/services/widget.service';
import {EMPTY_PAGED_RESULT} from '@core/utilities/empty-paged-result';
import {OverdueWidgetSearch} from '@core/utilities/search/overdue-widget-search';
import {PaginatedSearch} from '@core/utilities/search/paginated-search';
import {DownloadDirective} from 'src/app/directives/download.component';
import {DateTimeUTCPipe} from 'src/app/pipes/date-time-utc.pipe';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	selector: 'app-overdue-widget',
	imports: [
		ReactiveFormsModule,
		MatLabel,
		MatFormField,
		DownloadDirective,
		MatInput,
		MatButton,
		MatProgressBar,
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
	readonly scopes = input<Scope[]>();
	readonly id = input.required<string>();
	readonly specificColumnName = input.required<string>();

	readonly scopesLoaded = output<number>();
	private scopesEmitted = false;

	readonly scopeOverdue = signal<PagedResultOverdue>(EMPTY_PAGED_RESULT);
	readonly loading = signal(false);

	readonly scopeName = signal('');
	readonly parentScopeName = signal('');

	private readonly scopePks = computed(() => this.scopes()?.map(s => s.pk));
	readonly exportUrl = computed(() => this.widgetService.getScopeOverdueExportUrl(this.id(), this.scopePks()));

	@ViewChild(MatTable, {static: true}) table: MatTable<Overdue>;
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
			this.scopeName.set(config.scopeModels.filter(s => s.leaf)[0].shortname['en']);
			this.parentScopeName.set(config.scopeModels.filter(s => s.id === config.scopeModels.filter(s => s.leaf)[0].defaultParentId)[0].shortname['en']);
		});

		merge(
			this.sort.sortChange,
			this.paginator.page,
			this.filter.valueChanges.pipe(debounceTime(200))
		).pipe(
			takeUntilDestroyed(this.destroyRef),
			startWith({}),
			switchMap(() => {
				this.loading.set(true);
				const search = new OverdueWidgetSearch();
				search.fullText = this.filter.value;
				search.scopePks = this.scopePks();
				search.sortBy = this.sort.active;
				search.orderAscending = PaginatedSearch.getOrderAscending(this.sort.direction);
				search.pageIndex = this.paginator.pageIndex;
				return this.widgetService.getScopeOverdue(this.id(), search);
			})).subscribe(result => {
			this.scopeOverdue.set(result);
			//emit result only the first time, not when state changes
			if(!this.scopesEmitted) {
				this.scopesLoaded.emit(result.paging.total);
				this.scopesEmitted = true;
			}
			this.loading.set(false);
		});
	}
}
