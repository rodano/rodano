import {ChangeDetectionStrategy, Component, OnInit, ViewChild, DestroyRef, computed, input, signal, effect} from '@angular/core';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {Profile} from '@core/model/profile';
import {ConfigurationService} from '@core/services/configuration.service';
import {UserService} from '@core/services/user.service';
import {UserSearch} from '@core/utilities/search/user-search';
import {PagedResultUser} from '@core/model/paged-result-user';
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
import {MatProgressBar} from '@angular/material/progress-bar';
import {EMPTY_PAGED_RESULT} from '@core/utilities/empty-paged-result';
import {merge, Subject, switchMap} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {RoleStatus, getRoleStatusDisplay} from '../role-status-display';
import {MatTooltip} from '@angular/material/tooltip';
import {MatSort, MatSortHeader} from '@angular/material/sort';
import {PaginatedSearch} from '@core/utilities/search/paginated-search';
import {YesNoPipe} from '../../pipes/yes-no.pipe';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	templateUrl: './user-list.component.html',
	styleUrls: ['./user-list.component.css'],
	selector: 'app-user-list',
	imports: [
		MatButton,
		RouterLink,
		MatIcon,
		MatSort,
		MatSortHeader,
		MatProgressBar,
		MatTableModule,
		MatTooltip,
		MatDivider,
		MatToolbar,
		MatToolbarRow,
		DownloadDirective,
		MatPaginatorModule,
		LocalizeMapPipe,
		LookupByIdPipe,
		GetFieldPipe,
		YesNoPipe
	]
})
export class UserListComponent implements OnInit {
	readonly predicate = input<UserSearch>(new UserSearch());
	readonly showExternallyManaged = input<boolean>();

	refreshSearch$ = new Subject<void>();

	readonly profiles = signal<Profile[]>([]);
	roleStatus = RoleStatus;

	getRoleStatusDisplay = getRoleStatusDisplay;

	readonly users = signal<PagedResultUser>(EMPTY_PAGED_RESULT);
	readonly loading = signal(false);
	readonly columnsToDisplay = computed<string[]>(() => {
		const columns = ['name', 'email', 'phone', 'roles'];
		if(this.showExternallyManaged()) {
			columns.push('externallyManaged');
		}
		return columns;
	});

	readonly exportUrl = signal('');

	@ViewChild(MatSort, {static: true}) sort: MatSort;
	@ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

	constructor(
		private configurationService: ConfigurationService,
		private userService: UserService,
		private destroyRef: DestroyRef
	) {
		effect(() => {
			//do not use Paginator:firstPage() as it will trigger a search
			this.paginator.pageIndex = 0;
			this.refreshSearch$.next();
		});
	}

	ngOnInit() {
		this.sort.active = UserSearch.DEFAULT_SORT_BY;
		this.sort.direction = PaginatedSearch.getSortDirection(UserSearch.DEFAULT_SORT_ASCENDING);
		this.configurationService.getProfiles().pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(p => this.profiles.set(p));

		merge(
			this.refreshSearch$.asObservable(),
			this.paginator.page,
			this.sort.sortChange
		).pipe(
			takeUntilDestroyed(this.destroyRef),
			switchMap(() => {
				this.loading.set(true);
				this.predicate().sortBy = this.sort.active;
				this.predicate().orderAscending = PaginatedSearch.getOrderAscending(this.sort.direction);
				this.predicate().pageIndex = this.paginator.pageIndex;
				this.exportUrl.set(this.userService.getExportUrl(this.predicate()));
				return this.userService.search(this.predicate());
			})
		).subscribe(users => {
			this.users.set(users);
			this.loading.set(false);
		});
	}
}
