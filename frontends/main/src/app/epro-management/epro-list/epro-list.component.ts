import {Component, DestroyRef, OnInit, ViewChild, signal} from '@angular/core';
import {EproService} from '@core/services/epro.service';
import {MatPaginator} from '@angular/material/paginator';
import {startWith, switchMap} from 'rxjs/operators';
import {MatDialog} from '@angular/material/dialog';
import {ScopeSearch} from '@core/utilities/search/scope-search';
import {EproScope} from '@core/model/epro-scope';
import {PagedResultEproScope} from '@core/model/paged-result-epro-scope';
import {Subject, forkJoin, merge, of} from 'rxjs';
import {EPROInvitation} from '@core/model/epro-invitation';
import {EproInvitationComponent} from '../epro-invitation/epro-invitation.component';
import {NotificationService} from '../../services/notification.service';
import {ConfigurationService} from '@core/services/configuration.service';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {MatDivider} from '@angular/material/divider';
import {MatTableModule} from '@angular/material/table';
import {MatButton} from '@angular/material/button';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MatProgressBar} from '@angular/material/progress-bar';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {EMPTY_PAGED_RESULT} from '@core/utilities/empty-paged-result';
import {Rights} from '@core/model/rights';
import {RightEntity} from '@core/enums/right-entity';
import {ScopeCodeShortnamePipe} from '../../pipes/scope-code-shortname.pipe';
import {Profile} from '@core/model/profile';
import {MeService} from '@core/services/me.service';
import {ScopeMini} from '@core/model/scope-mini';

@Component({
	templateUrl: './epro-list.component.html',
	styleUrls: ['./epro-list.component.css'],
	imports: [
		ReactiveFormsModule,
		MatFormField,
		MatLabel,
		MatInput,
		MatSelect,
		MatOption,
		MatButton,
		MatProgressBar,
		MatTableModule,
		MatDivider,
		MatToolbar,
		MatToolbarRow,
		MatPaginator,
		ScopeCodeShortnamePipe
	]
})
export class EproListComponent implements OnInit {
	readonly parentScopes = signal<ScopeMini[]>([]);
	searchForm = new FormGroup({
		fullText: new FormControl('', {nonNullable: true}),
		parentPk: new FormControl(0)
	});

	refreshSearch$ = new Subject<void>();
	readonly scopes = signal<PagedResultEproScope>(EMPTY_PAGED_RESULT);
	readonly loading = signal(false);

	columnsToDisplay: string[] = [
		'code',
		'name',
		'status',
		'actions'
	];

	eproProfile?: Profile;

	@ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

	constructor(
		private configurationService: ConfigurationService,
		private meService: MeService,
		private eproService: EproService,
		private notificationService: NotificationService,
		private dialog: MatDialog,
		private destroyRef: DestroyRef
	) {}

	ngOnInit(): void {
		this.configurationService.getStudy().pipe(
			switchMap(study => {
				const leafScopeModel = study.leafScopeModel;
				return forkJoin({
					parentScopes: this.meService.getScopesForRequiredRight(RightEntity.SCOPE_MODEL, leafScopeModel.id, Rights.READ, [leafScopeModel.defaultParentId]),
					eproProfile: of(study.eproProfile)
				});
			}),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(({parentScopes, eproProfile}) => {
			this.parentScopes.set(parentScopes);
			this.eproProfile = eproProfile;
		});

		merge(
			this.refreshSearch$.asObservable(),
			this.paginator.page
		).pipe(
			takeUntilDestroyed(this.destroyRef),
			startWith({}),
			switchMap(() => {
				this.loading.set(true);
				const search = new ScopeSearch();
				search.fullText = this.searchForm.get('fullText')?.value;
				const parentPk = this.searchForm.get('parentPk')?.value;
				if(parentPk) {
					search.parentPks = [parentPk];
				}
				search.pageIndex = this.paginator.pageIndex;
				return this.eproService.searchScopes(search);
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

	invite(scope: EproScope) {
		this.eproService.invite(scope.scope.pk).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(invitation => {
			this.openInvitationDialog(invitation);
			this.search();
		});
	}

	revoke(scope: EproScope) {
		this.eproService.revoke(scope.scope.pk).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(() => {
			this.search();
			this.notificationService.showSuccess('Access revoked');
		});
	}

	private openInvitationDialog(invitation: EPROInvitation) {
		return this.dialog
			.open(EproInvitationComponent, {
				data: {
					invitation,
					eproProfile: this.eproProfile
				}
			})
			.afterClosed();
	}
}
