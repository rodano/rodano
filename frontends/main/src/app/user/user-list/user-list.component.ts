import {Component, OnInit, ViewChild, Input, DestroyRef, OnChanges} from '@angular/core';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {ProfileDTO} from '@core/model/profile-dto';
import {ConfigurationService} from '@core/services/configuration.service';
import {UserService} from '@core/services/user.service';
import {UserSearch} from '@core/utilities/search/user-search';
import {PagedResultUserDTO} from '@core/model/paged-result-user-dto';
import {GetFieldPipe} from '../../pipes/get-field.pipe';
import {LookupByIdPipe} from '../../pipes/lookup-by-id.pipe';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {DownloadDirective} from '../../directives/download.component';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {MatDivider} from '@angular/material/divider';
import {MatTableModule} from '@angular/material/table';
import {MatIcon} from '@angular/material/icon';
import {RouterLink} from '@angular/router';
import {MatButton} from '@angular/material/button';
import {EMPTY_PAGED_RESULT} from '@core/utilities/empty-paged-result';
import {merge, startWith, Subject, switchMap} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {RoleStatus, getRoleStatusDisplay} from '../role-status-display';
import {MatTooltip} from '@angular/material/tooltip';
import {MatSort, MatSortHeader} from '@angular/material/sort';
import {PaginatedSearch} from '@core/utilities/search/paginated-search';

@Component({
	templateUrl: './user-list.component.html',
	styleUrls: ['./user-list.component.css'],
	selector: 'app-user-list',
	imports: [
		MatButton,
		RouterLink,
		MatIcon,
		MatSort,
		MatSortHeader,
		MatTableModule,
		MatTooltip,
		MatDivider,
		MatToolbar,
		MatToolbarRow,
		DownloadDirective,
		MatPaginatorModule,
		LocalizeMapPipe,
		LookupByIdPipe,
		GetFieldPipe
	]
})
export class UserListComponent implements OnInit, OnChanges {
	@Input() predicate: UserSearch = new UserSearch();
	@Input() showExternallyManaged: boolean;

	refreshSearch$ = new Subject<void>();

	profiles: ProfileDTO[];
	roleStatus = RoleStatus;

	getRoleStatusDisplay = getRoleStatusDisplay;

	users: PagedResultUserDTO = EMPTY_PAGED_RESULT;
	columnsToDisplay: string[] = [
		'name',
		'email',
		'phone',
		'roles'
	];

	exportUrl: string;

	@ViewChild(MatSort, {static: true}) sort: MatSort;
	@ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

	constructor(
		private configurationService: ConfigurationService,
		private userService: UserService,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		this.sort.active = UserSearch.DEFAULT_SORT_BY;
		this.sort.direction = PaginatedSearch.getSortDirection(UserSearch.DEFAULT_SORT_ASCENDING);
		this.configurationService.getProfiles().pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(p => this.profiles = p);

		merge(
			this.refreshSearch$.asObservable(),
			this.paginator.page,
			this.sort.sortChange
		).pipe(
			takeUntilDestroyed(this.destroyRef),
			startWith({}),
			switchMap(() => {
				this.predicate.sortBy = this.sort.active;
				this.predicate.orderAscending = PaginatedSearch.getOrderAscending(this.sort.direction);
				this.predicate.pageIndex = this.paginator.pageIndex;
				this.exportUrl = this.userService.getExportUrl(this.predicate);
				return this.userService.search(this.predicate);
			})
		).subscribe(u => this.users = u);
	}

	ngOnChanges() {
		if(this.showExternallyManaged && !this.columnsToDisplay.includes('externallyManaged')) {
			this.columnsToDisplay.push('externallyManaged');
		}
		//do not use Paginator:firstPage() as it will trigger a search
		this.paginator.pageIndex = 0;
		this.refreshSearch$.next();
	}
}
