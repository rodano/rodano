import {Component, ViewChild, Input, OnChanges, DestroyRef, OnInit} from '@angular/core';
import {ScopeService} from '@core/services/scope.service';
import {MatPaginator} from '@angular/material/paginator';
import {RouterLink} from '@angular/router';
import {ScopeModelDTO} from '@core/model/scope-model-dto';
import {forkJoin, merge, of, startWith, Subject, switchMap} from 'rxjs';
import {ScopeSearch} from '@core/utilities/search/scope-search';
import {PagedResultScopeDTO} from '@core/model/paged-result-scope-dto';
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
import {ConfigurationService} from '@core/services/configuration.service';
import {MatSort, MatSortHeader} from '@angular/material/sort';
import {DownloadDirective} from 'src/app/directives/download.component';
import {ProfileDTO} from '@core/model/profile-dto';
import {ScopeRelationsService} from '@core/services/scope-relations.service';
import {ScopeDTO} from '@core/model/scope-dto';
import {ScopeCodeShortnamePipe} from 'src/app/pipes/scope-code-shortname.pipe';
import {MatOption, MatSelect} from '@angular/material/select';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {EMPTY_PAGED_RESULT} from '@core/utilities/empty-paged-result';
import {Rights} from '@core/model/rights';
import {PaginatedSearch} from '@core/utilities/search/paginated-search';

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
export class ScopeListComponent implements OnInit, OnChanges {
	@Input({required: true}) scopeModel: ScopeModelDTO;

	leafScopeModel?: ScopeModelDTO;
	defaultProfile: ProfileDTO | undefined;
	exportUrl: string;

	writeAccessOnParent = false;

	parentScopes: ScopeDTO[] = [];

	searchForm = new FormGroup({
		fullText: new FormControl('', {nonNullable: true}),
		parentPk: new FormControl(0)
	});

	refreshSearch$ = new Subject<void>();

	scopes: PagedResultScopeDTO = EMPTY_PAGED_RESULT;
	columnsToDisplay: string[] = [];

	@ViewChild(MatSort, {static: true}) sort: MatSort;
	@ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

	constructor(
		private configurationService: ConfigurationService,
		private scopeService: ScopeService,
		private scopeRelationService: ScopeRelationsService,
		private destroyRef: DestroyRef
	) { }

	ngOnChanges() {
		forkJoin({
			leafScopeModel: this.configurationService.getLeafScopeModel(),
			defaultProfile: this.scopeModel.defaultProfileId ? this.configurationService.getProfile(this.scopeModel.defaultProfileId) : of(undefined),
			parentScopes: this.scopeRelationService.getParents(this.scopeModel.id, Rights.READ),
			parentsWithWriteAccess: this.scopeRelationService.getParents(this.scopeModel.id, Rights.WRITE)

		}).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(({leafScopeModel, defaultProfile, parentScopes, parentsWithWriteAccess}) => {
			this.leafScopeModel = leafScopeModel;
			this.defaultProfile = defaultProfile;
			this.parentScopes = parentScopes;
			this.writeAccessOnParent = parentsWithWriteAccess.length > 0;
		});

		this.columnsToDisplay = ['code', 'shortname'];
		if(this.scopeModel.defaultProfileId) {
			this.columnsToDisplay.push('userOfInterest');
		}
		if(!this.scopeModel.leaf) {
			this.columnsToDisplay.push('leavesCount');
		}

		this.reset();
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
				const search = new ScopeSearch();
				search.scopeModelId = this.scopeModel.id;
				search.fullText = this.searchForm.get('fullText')?.value;
				const parentPk = this.searchForm.get('parentPk')?.value;
				if(parentPk) {
					search.parentPks = [parentPk];
				}
				search.sortBy = this.sort.active;
				search.orderAscending = PaginatedSearch.getOrderAscending(this.sort.direction);
				search.pageIndex = this.paginator.pageIndex;
				this.exportUrl = this.scopeService.getExportUrl(search);
				return this.scopeService.search(search);
			})
		).subscribe(s => this.scopes = s);
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
