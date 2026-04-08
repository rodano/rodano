import {AfterViewInit, ChangeDetectionStrategy, Component, ViewChild, signal} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {MatCheckbox} from '@angular/material/checkbox';
import {MatDivider} from '@angular/material/divider';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatTooltip} from '@angular/material/tooltip';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {DatabaseService} from '@core/services/database.service';
import {DatabaseIssue} from '@core/model/database-issue';
import {getDatabaseIssueStatusDisplay} from './database-issue-status-display';
import {getDatabaseIssueTypeDisplay} from './database-issue-type-display';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	templateUrl: './database-consistency.component.html',
	styleUrl: './database-consistency.component.css',
	imports: [
		MatButton,
		MatCheckbox,
		MatDivider,
		MatPaginator,
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

	@ViewChild(MatPaginator) paginator!: MatPaginator;
	@ViewChild(MatSort) sort!: MatSort;
	issues = new MatTableDataSource<DatabaseIssue>([]);
	issuesStatus = signal('Run the database update to check the consistency of the database and see potential issues');
	dryRun = signal(true);

	protected readonly displayedColumns = ['entity', 'modelId', 'pk', 'type', 'error', 'status'];

	constructor(
		private databaseService: DatabaseService
	) {}

	ngAfterViewInit() {
		this.issues.paginator = this.paginator;
		this.issues.sort = this.sort;
	}

	runDatabaseUpdate() {
		this.loading.set(true);
		this.databaseService.runDatabaseUpdate(this.dryRun()).subscribe({
			next: result => {
				this.issues.data = result;
				this.issuesStatus.set('No issues detected.');
				this.loading.set(false);
			},
			error: () => this.loading.set(false)
		});
	}
}
