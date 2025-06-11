import {Component, OnInit, ViewChild, DestroyRef} from '@angular/core';
import {RobotDTO} from '@core/model/robot-dto';
import {RobotService} from '@core/services/robot.service';
import {startWith, switchMap} from 'rxjs/operators';
import {ConfigurationService} from '@core/services/configuration.service';
import {ProfileDTO} from '@core/model/profile-dto';
import {MatPaginator} from '@angular/material/paginator';
import {PagedResultRobotDTO} from '@core/model/paged-result-robot-dto';
import {RobotSearch} from '@core/utilities/search/robot-search';
import {Subject, merge} from 'rxjs';
import {GetFieldPipe} from '../../pipes/get-field.pipe';
import {LookupByIdPipe} from '../../pipes/lookup-by-id.pipe';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {MatDivider} from '@angular/material/divider';
import {MatTableModule} from '@angular/material/table';
import {MatIcon} from '@angular/material/icon';
import {RouterLink} from '@angular/router';
import {MatButton} from '@angular/material/button';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {NotificationService} from 'src/app/services/notification.service';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {EMPTY_PAGED_RESULT} from '@core/utilities/empty-paged-result';
import {MatSort, MatSortHeader} from '@angular/material/sort';
import {PaginatedSearch} from '@core/utilities/search/paginated-search';

@Component({
	templateUrl: './robot-list.component.html',
	styleUrls: ['./robot-list.component.css'],
	imports: [
		ReactiveFormsModule,
		MatFormField,
		MatSort,
		MatSortHeader,
		MatInput,
		MatLabel,
		MatSelect,
		MatOption,
		MatButton,
		RouterLink,
		MatIcon,
		MatTableModule,
		MatDivider,
		MatToolbar,
		MatToolbarRow,
		MatPaginator,
		LocalizeMapPipe,
		LookupByIdPipe,
		GetFieldPipe
	]
})
export class RobotListComponent implements OnInit {
	profiles: ProfileDTO[];
	searchForm = new FormGroup({
		name: new FormControl('', {nonNullable: true}),
		profileId: new FormControl('')
	});

	refreshSearch$ = new Subject<void>();

	robots: PagedResultRobotDTO = EMPTY_PAGED_RESULT;
	columnsToDisplay: string[] = [
		'name',
		'profileId',
		'scopeShortname',
		'actions'
	];

	@ViewChild(MatSort, {static: true}) sort: MatSort;
	@ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

	constructor(
		private configurationService: ConfigurationService,
		private robotService: RobotService,
		private notificationService: NotificationService,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		this.sort.active = RobotSearch.DEFAULT_SORT_BY;
		this.sort.direction = PaginatedSearch.getSortDirection(RobotSearch.DEFAULT_SORT_ASCENDING);

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
				const search = new RobotSearch();
				Object.assign(search, this.searchForm.value);
				search.sortBy = this.sort.active;
				search.orderAscending = PaginatedSearch.getOrderAscending(this.sort.direction);
				search.pageIndex = this.paginator.pageIndex;
				return this.robotService.search(search);
			})
		).subscribe(r => this.robots = r);
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

	remove(robot: RobotDTO) {
		this.robotService.remove(robot.pk).subscribe(() => {
			robot.removed = true;
			this.notificationService.showSuccess('Robot removed');
		});
	}

	restore(robot: RobotDTO) {
		this.robotService.restore(robot.pk).subscribe(() => {
			robot.removed = false;
			this.notificationService.showSuccess('Robot restored');
		});
	}
}
