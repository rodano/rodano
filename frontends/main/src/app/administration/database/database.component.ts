import {ChangeDetectionStrategy, Component} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {DatabaseService} from '@core/services/database.service';
import {DemoUserScheme} from '@core/model/demo-user-scheme';
import {NotificationService} from '../../services/notification.service';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	templateUrl: './database.component.html',
	imports: [
		ReactiveFormsModule,
		MatButton,
		MatFormField,
		MatLabel,
		MatInput
	]
})
export class DatabaseComponent {
	demoUserSchemeForm = new FormGroup({
		baseEmail: new FormControl('info@rodano.ch', {nonNullable: true, validators: [Validators.required, Validators.email]}),
		password: new FormControl('Password1!', {nonNullable: true, validators: [Validators.required]})
	});

	randomDataGenerationForm = new FormGroup({
		scale: new FormControl(10, {nonNullable: true, validators: [Validators.required, Validators.min(1), Validators.max(100)]})
	});

	constructor(
		private databaseService: DatabaseService,
		private notificationService: NotificationService
	) {}

	createDemoUsers() {
		const demoUserScheme = this.demoUserSchemeForm.value as DemoUserScheme;
		this.databaseService.createDemoUsers(demoUserScheme).subscribe(() => this.notificationService.showSuccess('Demo users created'));
	}

	generateRandomDatabaseData() {
		const scale = this.randomDataGenerationForm.value.scale as number;
		this.databaseService.generateRandomData(scale).subscribe(() => this.notificationService.showSuccess('Database fill-in process started'));
	}
}
