import {Component} from '@angular/core';
import {FormControl, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import {MatError, MatFormField, MatLabel} from '@angular/material/form-field';
import {BootstrapDTO} from '@core/model/bootstrap-dto';
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
	loading = false;
	error?: string;

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
		this.loading = true;
		this.error = undefined;
		const bootstrap = this.bootstrapForm.value as BootstrapDTO;

		this.databaseService.bootstrap(bootstrap).subscribe({
			next: () => this.router.navigate(['/login']),
			error: response => this.error = response.error.message,
			complete: () => this.loading = false
		}).add(() => this.loading = false);
	}
}
