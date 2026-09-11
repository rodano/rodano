import {Component, ViewChild, DestroyRef, OnInit, effect, input, signal} from '@angular/core';
import {ScopeService} from '@core/services/scope.service';
import {MatPaginator} from '@angular/material/paginator';
import {RouterLink} from '@angular/router';
import {ScopeModel} from '@core/model/scope-model';
import {forkJoin, merge, of, startWith, Subject, switchMap} from 'rxjs';
import {ScopeSearch} from '@core/utilities/search/scope-search';
import {PagedResultScope} from '@core/model/paged-result-scope';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {LowerCasePipe} from '@angular/common';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {MatDivider} from '@angular/material/divider';
import {MatHeaderCell, MatTable, MatTableModule} from '@angular/material/table';
import {MatIcon} from '@angular/material/icon';
import {MatButton} from '@angular/material/button';
import {MatInput, MatLabel} from '@angular/material/input';
import {MatFormField} from '@angular/material/form-field';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MatProgressBar} from '@angular/material/progress-bar';
import {ConfigurationService} from '@core/services/configuration.service';
import {MatSort, MatSortHeader} from '@angular/material/sort';
import {DownloadDirective} from '../../directives/download.component';
import {Profile} from '@core/model/profile';
import {ScopeMini} from '@core/model/scope-mini';
import {ScopeCodeShortnamePipe} from '../../pipes/scope-code-shortname.pipe';
import {MatOption, MatSelect} from '@angular/material/select';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {EMPTY_PAGED_RESULT} from '@core/utilities/empty-paged-result';
import {Rights} from '@core/model/rights';
import {RightEntity} from '@core/enums/right-entity';
import {PaginatedSearch} from '@core/utilities/search/paginated-search';
import {MeService} from '@core/services/me.service';

@Component({
	selector: 'app-scope-list',
	templateUrl: './scope-list.component.html',
	styleUrls: ['./scope-list.component.css'],
	imports: [
		ReactiveFormsModule,
		MatLabel,
		MatFormField,
		MatInput,
		MatButton,
		MatProgressBar,
		MatSort,
		MatSortHeader,
		MatTableModule,
		RouterLink,
		MatIcon,
		MatTable,
		MatSelect,
		MatOption,
		MatHeaderCell,
		MatDivider,
		MatToolbar,
		MatToolbarRow,
		MatPaginator,
		DownloadDirective,
		LowerCasePipe,
		LocalizeMapPipe,
		ScopeCodeShortnamePipe
	]
})
export class ScopeListComponent implements OnInit {
	readonly scopeModel = input.required<ScopeModel>();

	readonly leafScopeModel = signal<ScopeModel | undefined>(undefined);
	readonly defaultProfile = signal<Profile | undefined>(undefined);
	readonly exportUrl = signal('');

	readonly writeAccessOnParent = signal(false);

	readonly parentScopes = signal<ScopeMini[]>([]);

	searchForm = new FormGroup({
		fullText: new FormControl('', {nonNullable: true}),
		parentPk: new FormControl(0)
	});

	refreshSearch$ = new Subject<void>();

	readonly scopes = signal<PagedResultScope>(EMPTY_PAGED_RESULT);
	readonly loading = signal(false);
	readonly columnsToDisplay = signal<string[]>([]);

	@ViewChild(MatSort, {static: true}) sort: MatSort;
	@ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

	constructor(
		private configurationService: ConfigurationService,
		private meService: MeService,
		private scopeService: ScopeService,
		private destroyRef: DestroyRef
	) {
		effect(() => {
			const scopeModel = this.scopeModel();
			forkJoin({
				leafScopeModel: this.configurationService.getLeafScopeModel(),
				defaultProfile: scopeModel.defaultProfileId ? this.configurationService.getProfile(scopeModel.defaultProfileId) : of(undefined),
				parentScopes: this.meService.getScopesForRequiredRight(RightEntity.SCOPE_MODEL, scopeModel.id, Rights.READ, [scopeModel.defaultParentId]),
				parentsWithWriteAccess: this.meService.getScopesForRequiredRight(RightEntity.SCOPE_MODEL, scopeModel.id, Rights.WRITE, [scopeModel.defaultParentId])
			}).pipe(
				takeUntilDestroyed(this.destroyRef)
			).subscribe(({leafScopeModel, defaultProfile, parentScopes, parentsWithWriteAccess}) => {
				this.leafScopeModel.set(leafScopeModel);
				this.defaultProfile.set(defaultProfile);
				this.parentScopes.set(parentScopes);
				this.writeAccessOnParent.set(parentsWithWriteAccess.length > 0);
			});

			this.columnsToDisplay.set(['code', 'shortname']);
			if(scopeModel.defaultProfileId) {
				this.columnsToDisplay.update(cols => [...cols, 'userOfInterest']);
			}
			if(!scopeModel.leaf) {
				this.columnsToDisplay.update(cols => [...cols, 'leavesCount']);
			}

			this.reset();
		});
	}

	ngOnInit() {
		this.sort.active = ScopeSearch.DEFAULT_SORT_BY;
		this.sort.direction = PaginatedSearch.getSortDirection(ScopeSearch.DEFAULT_SORT_ASCENDING);

		merge(
			this.refreshSearch$.asObservable(),
			this.paginator.page,
			this.sort.sortChange
		).pipe(
			takeUntilDestroyed(this.destroyRef),
			startWith({}),
			switchMap(() => {
				this.loading.set(true);
				const search = new ScopeSearch();
				search.scopeModelId = this.scopeModel().id;
				search.fullText = this.searchForm.get('fullText')?.value;
				const parentPk = this.searchForm.get('parentPk')?.value;
				if(parentPk) {
					search.parentPks = [parentPk];
				}
				search.sortBy = this.sort.active;
				search.orderAscending = PaginatedSearch.getOrderAscending(this.sort.direction);
				search.pageIndex = this.paginator.pageIndex;
				this.exportUrl.set(this.scopeService.getExportUrl(search));
				return this.scopeService.search(search);
			})
		).subscribe(scopes => {
			this.scopes.set(scopes);
			this.loading.set(false);
		});
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
}
