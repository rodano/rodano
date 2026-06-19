import {Component, signal} from '@angular/core';
import {FormControl, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import {MatError, MatFormField, MatLabel} from '@angular/material/form-field';
import {Bootstrap} from '@core/model/bootstrap';
import {DatabaseService} from '@core/services/database.service';

@Component({
	selector: 'app-bootstrap',
	templateUrl: './bootstrap.component.html',
	styleUrls: ['./bootstrap.component.css'],
	imports: [
		ReactiveFormsModule,
		MatFormField,
		MatError,
		MatLabel,
		MatInput,
		MatButton
	]
})
export class BootstrapComponent {
	readonly loading = signal(false);
	readonly error = signal<string | undefined>(undefined);

	bootstrapForm = new FormGroup({
		rootScopeName: new FormControl('', {
			nonNullable: true,
			validators: [Validators.required]
		}),
		userEmail: new FormControl('', {
			nonNullable: true,
			validators: [Validators.required, Validators.email]
		}),
		userPassword: new FormControl('', {
			nonNullable: true,
			validators: [Validators.required]
		}),
		userName: new FormControl('', {
			nonNullable: true,
			validators: [Validators.required]
		})
	});

	constructor(
		private databaseService: DatabaseService,
		private router: Router
	) { }

	bootstrap() {
		this.loading.set(true);
		this.error.set(undefined);
		const bootstrap = this.bootstrapForm.value as Bootstrap;

		this.databaseService.bootstrap(bootstrap).subscribe({
			next: () => this.router.navigate(['/login']),
			error: response => this.error.set(response.error.message),
			complete: () => this.loading.set(false)
		}).add(() => this.loading.set(false));
	}
}
