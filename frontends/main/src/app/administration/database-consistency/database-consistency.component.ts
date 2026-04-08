import {AfterViewInit, ChangeDetectionStrategy, Component, ViewChild, signal} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {MatCheckbox} from '@angular/material/checkbox';
import {MatDivider} from '@angular/material/divider';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatPaginator} from '@angular/material/paginator';
import {MatOption, MatSelect} from '@angular/material/select';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatTooltip} from '@angular/material/tooltip';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {DatabaseService} from '@core/services/database.service';
import {getDatabaseIssueStatusDisplay} from './database-issue-status-display';
import {getDatabaseIssueTypeDisplay} from './database-issue-type-display';
import {DatabaseIssueGroup} from '@core/model/database-issue-group';
import {DatabaseIssueEntity} from '@core/model/database-issue-entity';
import {DatabaseIssueStatus} from '@core/model/database-issue-status';
import {DatabaseIssueType} from '@core/model/database-issue-type';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	templateUrl: './database-consistency.component.html',
	styleUrl: './database-consistency.component.css',
	imports: [
		ReactiveFormsModule,
		MatButton,
		MatCheckbox,
		MatDivider,
		MatFormField,
		MatLabel,
		MatOption,
		MatPaginator,
		MatSelect,
		MatSortModule,
		MatTableModule,
		MatTooltip,
		MatToolbar,
		MatToolbarRow
	]
})
export class DatabaseConsistencyComponent implements AfterViewInit {
	getDatabaseIssueStatusDisplay = getDatabaseIssueStatusDisplay;
	getDatabaseIssueTypeDisplay = getDatabaseIssueTypeDisplay;

	loading = signal(false);

	readonly entityOptions = Object.values(DatabaseIssueEntity) as DatabaseIssueEntity[];
	readonly typeOptions = Object.values(DatabaseIssueType) as DatabaseIssueType[];
	readonly statusOptions = Object.values(DatabaseIssueStatus) as DatabaseIssueStatus[];

	filterForm = new FormGroup({
		entity: new FormControl<DatabaseIssueEntity[]>([], {nonNullable: true}),
		type: new FormControl<DatabaseIssueType[]>([], {nonNullable: true}),
		status: new FormControl<DatabaseIssueStatus[]>([], {nonNullable: true})
	});

	@ViewChild(MatPaginator) paginator!: MatPaginator;
	@ViewChild(MatSort) sort!: MatSort;
	issueGroups = new MatTableDataSource<DatabaseIssueGroup>([]);
	issuesStatus = signal('Run the database update to check the consistency of the database and see potential issues');
	dryRun = signal(true);

	protected readonly displayedColumns = ['entity', 'modelId', 'type', 'missingEntityId', 'count', 'status'];

	constructor(
		private databaseService: DatabaseService
	) {
		this.issueGroups.filterPredicate = (data: DatabaseIssueGroup, filter: string) => {
			const {entity, type, status} = JSON.parse(filter) as {
				entity: DatabaseIssueEntity[];
				type: DatabaseIssueType[];
				status: DatabaseIssueStatus[];
			};
			if(entity.length > 0 && !entity.includes(data.entity)) {
				return false;
			}
			if(type.length > 0 && !type.includes(data.type)) {
				return false;
			}
			if(status.length > 0 && !status.includes(data.status)) {
				return false;
			}
			return true;
		};
	}

	search() {
		const value = this.filterForm.value;
		const {entity, type, status} = value;
		const hasFilter = (entity?.length ?? 0) > 0 || (type?.length ?? 0) > 0 || (status?.length ?? 0) > 0;
		this.issueGroups.filter = hasFilter ? JSON.stringify(value) : '';
	}

	resetFilters() {
		this.filterForm.reset();
		this.issueGroups.filter = '';
	}

	ngAfterViewInit() {
		this.issueGroups.paginator = this.paginator;
		this.issueGroups.sort = this.sort;
	}

	runDatabaseUpdate() {
		this.loading.set(true);
		this.databaseService.runDatabaseUpdate(this.dryRun()).subscribe({
			next: result => {
				this.issueGroups.data = result;
				this.issuesStatus.set('No issues detected.');
				this.loading.set(false);
			},
			error: () => this.loading.set(false)
		});
	}
}
