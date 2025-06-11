import {Component, DestroyRef, OnInit} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {forkJoin} from 'rxjs';
import {StudyDTO} from '@core/model/study-dto';
import {AdministrationService} from '@core/services/administration.service';
import {ConfigurationService} from '@core/services/configuration.service';
import {AuthStateService} from 'src/app/services/auth-state.service';
import {NotificationService} from 'src/app/services/notification.service';
import {MatAnchor, MatButton} from '@angular/material/button';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatInput} from '@angular/material/input';
import {DatabaseService} from '@core/services/database.service';
import {DemoUserSchemeDTO} from '@core/model/demo-user-scheme-dto';

@Component({
	templateUrl: './management.component.html',
	styleUrls: ['./management.component.css'],
	imports: [
		ReactiveFormsModule,
		MatAnchor,
		MatButton,
		MatFormField,
		MatLabel,
		MatInput
	]
})
export class ManagementComponent implements OnInit {
	study: StudyDTO;
	editConfigurationLink: string;
	inMaintenance: boolean;
	inDebug: boolean;
	demoUserSchemeForm = new FormGroup({
		baseEmail: new FormControl('info@rodano.ch', {nonNullable: true, validators: [Validators.required, Validators.email]}),
		password: new FormControl('Password1!', {nonNullable: true, validators: [Validators.required]})
	});

	randomDataGenerationForm = new FormGroup({
		scale: new FormControl(10, {nonNullable: true, validators: [Validators.required, Validators.min(1), Validators.max(100)]})
	});

	constructor(
		private administrationService: AdministrationService,
		private databaseService: DatabaseService,
		private notificationService: NotificationService,
		private configurationService: ConfigurationService,
		private authStateService: AuthStateService,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		forkJoin({
			study: this.configurationService.getStudy(),
			inMaintenance: this.administrationService.isInMaintenance(),
			inDebug: this.administrationService.isInDebug()
		}).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(({study, inMaintenance, inDebug}) => {
			this.study = study;
			this.inMaintenance = inMaintenance;
			this.inDebug = inDebug;
		});

		this.editConfigurationLink = `/config/?api_url=/api&bearer_token=${this.authStateService.getToken()}`;
	}

	reloadConfiguration() {
		this.administrationService.reloadConfiguration().subscribe(() => this.notificationService.showSuccess('Configuration reloaded'));
	}

	toggleMaintenance(state: boolean) {
		this.administrationService.setMaintenance(state).subscribe(() => this.inMaintenance = state);
	}

	toggleDebug(state: boolean) {
		this.administrationService.setDebug(state).subscribe(() => this.inDebug = state);
	}

	createDemoUsers() {
		const demoUserScheme = this.demoUserSchemeForm.value as DemoUserSchemeDTO;
		this.databaseService.createDemoUsers(demoUserScheme).subscribe(() => this.notificationService.showSuccess('Demo users created'));
	}

	generateRandomDatabaseData() {
		const scale = this.randomDataGenerationForm.value.scale as number;
		this.databaseService.generateRandomData(scale).subscribe(() => this.notificationService.showSuccess('Database fill-in process started'));
	}
}
