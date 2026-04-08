import {ChangeDetectionStrategy, Component, DestroyRef, OnInit, signal} from '@angular/core';
import {FormControl, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {UserService} from '@core/services/user.service';
import {ConfigurationService} from '@core/services/configuration.service';
import {Profile} from '@core/model/profile';
import {forkJoin} from 'rxjs';
import {NotificationService} from '../../services/notification.service';
import {UserCreation} from '@core/model/user-creation';
import {Language} from '@core/model/language';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatButton} from '@angular/material/button';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {User} from '@core/model/user';
import {ArraySortPipe} from '../../pipes/sort-array.pipe';
import {MeService} from '@core/services/me.service';
import {ScopeMini} from '@core/model/scope-mini';
import {ScopePickerComponent} from '../../scope-picker/scope-picker.component';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	templateUrl: './user-create.component.html',
	styleUrls: ['./user-create.component.css'],
	imports: [
		ReactiveFormsModule,
		MatFormField,
		MatLabel,
		MatInput,
		MatSelect,
		MatOption,
		MatButton,
		LocalizeMapPipe,
		ArraySortPipe,
		ScopePickerComponent
	]
})
export class UserCreateComponent implements OnInit {
	userCreationForm = new FormGroup({
		email: new FormControl('', [Validators.required, Validators.email]),
		name: new FormControl('', [Validators.required]),
		phone: new FormControl(''),
		languageId: new FormControl(''),
		role: new FormGroup({
			profileId: new FormControl('', [Validators.required]),
			scopePk: new FormControl(1, [Validators.required])
		})
	});

	readonly languages = signal<Language[]>([]);
	readonly profiles = signal<Profile[]>([]);
	readonly scopes = signal<ScopeMini[]>([]);
	readonly error = signal<string | undefined>(undefined);

	constructor(
		private router: Router,
		private configurationService: ConfigurationService,
		private userService: UserService,
		private meService: MeService,
		private notificationService: NotificationService,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		return forkJoin({
			languages: this.configurationService.getLanguages(),
			profiles: this.configurationService.getProfiles(),
			scopes: this.meService.getScopes(undefined, true, false)
		}).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(({languages, profiles, scopes}) => {
			this.languages.set(languages);
			this.profiles.set(profiles);
			this.scopes.set(scopes);
			this.userCreationForm.reset();
		});
	}

	save() {
		const userCreation = this.userCreationForm.value as UserCreation;

		this.userService.create(userCreation).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: (user: User) => {
				this.notificationService.showSuccess('User created and invited');
				this.router.navigate(['/users', user.pk]);
			},
			error: (response: any) => {
				this.error.set(response.error.message);
			}
		});
	}
}
