import {Component, DestroyRef, OnInit, ViewChild} from '@angular/core';
import {EproService} from '@core/services/epro.service';
import {ScopeService} from '@core/services/scope.service';
import {MatPaginator} from '@angular/material/paginator';
import {startWith, switchMap} from 'rxjs/operators';
import {MatDialog} from '@angular/material/dialog';
import {ScopeSearch} from '@core/utilities/search/scope-search';
import {EproRobotDTO} from '@core/model/epro-robot-dto';
import {ScopeDTO} from '@core/model/scope-dto';
import {PagedResultScopeDTO} from '@core/model/paged-result-scope-dto';
import {Subject, forkJoin, merge, of} from 'rxjs';
import {EPROInvitationDTO} from '@core/model/epro-invitation-dto';
import {EproInvitationComponent} from '../epro-invitation/epro-invitation.component';
import {NotificationService} from 'src/app/services/notification.service';
import {ConfigurationService} from '@core/services/configuration.service';
import {ScopeRelationsService} from '@core/services/scope-relations.service';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {MatDivider} from '@angular/material/divider';
import {MatTableModule} from '@angular/material/table';
import {MatButton} from '@angular/material/button';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {EMPTY_PAGED_RESULT} from '@core/utilities/empty-paged-result';
import {Rights} from '@core/model/rights';
import {ScopeCodeShortnamePipe} from 'src/app/pipes/scope-code-shortname.pipe';
import {ProfileDTO} from '@core/model/profile-dto';

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
		MatTableModule,
		MatDivider,
		MatToolbar,
		MatToolbarRow,
		MatPaginator,
		ScopeCodeShortnamePipe
	]
})
export class EproListComponent implements OnInit {
	parentScopes: ScopeDTO[] = [];
	searchForm = new FormGroup({
		fullText: new FormControl('', {nonNullable: true}),
		parentPk: new FormControl(0)
	});

	refreshSearch$ = new Subject<void>();
	scopes: PagedResultScopeDTO = EMPTY_PAGED_RESULT;

	robots: EproRobotDTO[];
	columnsToDisplay: string[] = [
		'code',
		'name',
		'status',
		'actions'
	];

	eproProfile?: ProfileDTO;

	scopeRobotMap: Record<number, EproRobotDTO>;

	@ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

	constructor(
		private configurationService: ConfigurationService,
		private scopeService: ScopeService,
		private scopeRelationsService: ScopeRelationsService,
		private eproService: EproService,
		private notificationService: NotificationService,
		private dialog: MatDialog,
		private destroyRef: DestroyRef
	) {}

	ngOnInit(): void {
		this.configurationService.getStudy().pipe(
			switchMap(study => {
				return forkJoin({
					parentScopes: this.scopeRelationsService.getParents(study.leafScopeModel.id, Rights.READ),
					eproProfile: of(study.eproProfile),
					eproEnabled: of(study.eproEnabled)
				});
			}),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(result => {
			this.parentScopes = result.parentScopes;
			this.eproProfile = result.eproProfile;
		});

		merge(
			this.refreshSearch$.asObservable(),
			this.paginator.page
		).pipe(
			takeUntilDestroyed(this.destroyRef),
			startWith({}),
			switchMap(() => {
				const search = new ScopeSearch();
				search.leaf = true;
				search.fullText = this.searchForm.get('fullText')?.value;
				const parentPk = this.searchForm.get('parentPk')?.value;
				if(parentPk) {
					search.parentPks = [parentPk];
				}
				search.pageIndex = this.paginator.pageIndex;
				return forkJoin({
					robots: this.eproService.getInvitedRobots(),
					scopes: this.scopeService.search(search)
				});
			})
		).subscribe(({robots, scopes}) => {
			this.robots = robots;
			this.scopes = scopes;
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

	hasBeenInvited(scope: ScopeDTO): boolean {
		return this.robots.some(robot => robot.scopePk === scope.pk);
	}

	invite(scope: ScopeDTO) {
		this.eproService.invite(scope.pk).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(invitation => {
			this.openInvitationDialog(invitation);
			this.search();
		});
	}

	revoke(scope: ScopeDTO) {
		this.eproService.revoke(scope.pk).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(() => {
			this.search();
			this.notificationService.showSuccess('Access revoked');
		});
	}

	private openInvitationDialog(invitation: EPROInvitationDTO) {
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
