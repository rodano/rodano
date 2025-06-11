import {Component, DestroyRef, OnInit, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Subject, merge} from 'rxjs';
import {startWith, switchMap} from 'rxjs/operators';
import {PagedResultResourceDTO} from '@core/model/paged-result-resource-dto';
import {ResourceCategoryDTO} from '@core/model/resource-category-dto';
import {ResourceDTO} from '@core/model/resource-dto';
import {ConfigurationService} from '@core/services/configuration.service';
import {ResourceService} from '@core/services/resource.service';
import {ResourceSearch} from '@core/utilities/search/resource-search';
import {ResourceSubmissionDTO} from '@core/model/resource-submission-dto';
import {NotificationService} from 'src/app/services/notification.service';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatDivider} from '@angular/material/divider';
import {MatTableModule} from '@angular/material/table';
import {MatIcon} from '@angular/material/icon';
import {MatMenuModule} from '@angular/material/menu';
import {MatButton} from '@angular/material/button';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {DownloadDirective} from '../directives/download.component';
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';
import {ModifyResourceDialogComponent} from './modify-resource-dialog/modify-resource-dialog.component';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {EMPTY_PAGED_RESULT} from '@core/utilities/empty-paged-result';
import {MatSort, MatSortHeader} from '@angular/material/sort';
import {PaginatedSearch} from '@core/utilities/search/paginated-search';

@Component({
	selector: 'app-resource-list',
	templateUrl: './resource-list.component.html',
	styleUrls: ['./resource-list.component.css'],
	imports: [
		ReactiveFormsModule,
		MatFormField,
		MatLabel,
		MatInput,
		MatSelect,
		MatOption,
		MatButton,
		MatMenuModule,
		MatIcon,
		MatTableModule,
		DownloadDirective,
		MatDivider,
		MatToolbarModule,
		MatPaginatorModule,
		LocalizeMapPipe,
		MatSort,
		MatSortHeader
	]
})
export class ResourceListComponent implements OnInit {
	categories: ResourceCategoryDTO[];

	searchForm = new FormGroup({
		fullText: new FormControl('', {nonNullable: true}),
		categoryId: new FormControl('')
	});

	refreshSearch$ = new Subject<void>();

	resources: PagedResultResourceDTO = EMPTY_PAGED_RESULT;
	columnsToDisplay = [
		'title',
		'category',
		'scopeShortname',
		'isPublic',
		'actions'
	];

	@ViewChild(MatSort, {static: true}) sort: MatSort;
	@ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

	constructor(
		private configurationService: ConfigurationService,
		private resourceService: ResourceService,
		private notificationService: NotificationService,
		private dialog: MatDialog,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		this.sort.active = ResourceSearch.DEFAULT_SORT_BY;
		this.sort.direction = PaginatedSearch.getSortDirection(ResourceSearch.DEFAULT_SORT_ASCENDING);

		this.configurationService.getResourceCategories().pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(c => this.categories = c);

		merge(
			this.refreshSearch$.asObservable(),
			this.paginator.page,
			this.sort.sortChange
		).pipe(
			takeUntilDestroyed(this.destroyRef),
			startWith({}),
			switchMap(() => {
				const search = new ResourceSearch();
				Object.assign(search, this.searchForm.value);
				search.removed = true;
				search.sortBy = this.sort.active;
				search.orderAscending = PaginatedSearch.getOrderAscending(this.sort.direction);
				search.pageIndex = this.paginator.pageIndex;
				return this.resourceService.search(search);
			})
		).subscribe(r => this.resources = r);
	}

	search() {
		//do not user Paginator:firstPage() as it will trigger a search
		this.paginator.pageIndex = 0;
		this.refreshSearch$.next();
	}

	reset() {
		this.searchForm.reset();
		//do not user Paginator:firstPage() as it will trigger a search
		this.paginator.pageIndex = 0;
		this.refreshSearch$.next();
	}

	createResource(categoryId: string): void {
		const resource = {
			categoryId
		} as ResourceSubmissionDTO;
		this.saveResource(resource);
	}

	updateResource(resource: ResourceDTO): void {
		this.saveResource(resource);
	}

	removeResource(_: Event, resource: ResourceDTO) {
		this.resourceService.remove(resource).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(() => {
			this.notificationService.showSuccess('Resource removed');
			this.reset();
		});
	}

	restoreResource(_: Event, resource: ResourceDTO) {
		this.resourceService.restore(resource).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(() => {
			this.notificationService.showSuccess('Resource restored');
			this.reset();
		});
	}

	getFileUrl(resource: ResourceDTO): string {
		return this.resourceService.getFileUrl(resource);
	}

	private saveResource(resource: ResourceSubmissionDTO | ResourceDTO) {
		return this.dialog
			.open(ModifyResourceDialogComponent, {data: resource})
			.afterClosed()
			.subscribe(resource => {
				if(resource) {
					this.notificationService.showSuccess(`'${resource.title}' saved`);
					this.search();
				}
			});
	}
}
