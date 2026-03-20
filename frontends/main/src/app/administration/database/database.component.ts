import {AfterViewInit, ChangeDetectionStrategy, Component, ViewChild, signal} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {MatCheckbox} from '@angular/material/checkbox';
import {MatDivider} from '@angular/material/divider';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatPaginator} from '@angular/material/paginator';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {DatabaseService} from '@core/services/database.service';
import {DatabaseIssue} from '@core/model/database-issue';
import {DemoUserScheme} from '@core/model/demo-user-scheme';
import {NotificationService} from '../../services/notification.service';
import {getDatabaseIssueStatusDisplay} from './database-issue-status-display';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	templateUrl: './database.component.html',
	imports: [
		ReactiveFormsModule,
		MatButton,
		MatCheckbox,
		MatDivider,
		MatFormField,
		MatLabel,
		MatInput,
		MatPaginator,
		MatTableModule,
		MatToolbar,
		MatToolbarRow
	]
})
export class DatabaseComponent implements AfterViewInit {
	getDatabaseIssueStatusDisplay = getDatabaseIssueStatusDisplay;

	loading = signal(false);

	demoUserSchemeForm = new FormGroup({
		baseEmail: new FormControl('info@rodano.ch', {nonNullable: true, validators: [Validators.required, Validators.email]}),
		password: new FormControl('Password1!', {nonNullable: true, validators: [Validators.required]})
	});

	randomDataGenerationForm = new FormGroup({
		scale: new FormControl(10, {nonNullable: true, validators: [Validators.required, Validators.min(1), Validators.max(100)]})
	});

	@ViewChild(MatPaginator) paginator!: MatPaginator;
	issues = new MatTableDataSource<DatabaseIssue>([]);
	issuesStatus = signal('Run the database update to check the consistency of the database and see potential issues');
	dryRun = signal(true);

	protected readonly displayedColumns = ['entity', 'pk', 'error', 'status'];

	constructor(
		private databaseService: DatabaseService,
		private notificationService: NotificationService
	) {}

	ngAfterViewInit() {
		this.issues.paginator = this.paginator;
	}

	createDemoUsers() {
		this.loading.set(true);
		const demoUserScheme = this.demoUserSchemeForm.value as DemoUserScheme;
		this.databaseService.createDemoUsers(demoUserScheme).subscribe({
			next: () => {
				this.notificationService.showSuccess('Demo users created');
				this.loading.set(false);
			},
			error: () => this.loading.set(false)
		});
	}

	generateRandomDatabaseData() {
		this.loading.set(true);
		const scale = this.randomDataGenerationForm.value.scale as number;
		this.databaseService.generateRandomData(scale).subscribe({
			next: () => {
				this.notificationService.showSuccess('Database fill-in process started');
				this.loading.set(false);
			},
			error: () => this.loading.set(false)
		});
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
