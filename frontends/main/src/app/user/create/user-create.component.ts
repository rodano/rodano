import {Component, DestroyRef, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {UserService} from '@core/services/user.service';
import {ConfigurationService} from '@core/services/configuration.service';
import {ProfileDTO} from '@core/model/profile-dto';
import {forkJoin} from 'rxjs';
import {NotificationService} from 'src/app/services/notification.service';
import {UserCreationDTO} from '@core/model/user-creation-dto';
import {LanguageDTO} from '@core/model/language-dto';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatButton} from '@angular/material/button';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {UserDTO} from '@core/model/user-dto';
import {ArraySortPipe} from 'src/app/pipes/sort-array.pipe';
import {MeService} from '@core/services/me.service';
import {ScopeMiniDTO} from '@core/model/scope-mini-dto';
import {ScopePickerComponent} from 'src/app/scope-picker/scope-picker.component';

@Component({
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

	languages: LanguageDTO[];
	profiles: ProfileDTO[];
	scopes: ScopeMiniDTO[];
	errorText: string;

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
			this.languages = languages;
			this.profiles = profiles;
			this.scopes = scopes;
			this.userCreationForm.reset();
		});
	}

	save() {
		const userCreation = this.userCreationForm.value as UserCreationDTO;

		this.userService.create(userCreation).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: (user: UserDTO) => {
				this.notificationService.showSuccess('User created and invited');
				this.router.navigate(['/users', user.pk]);
			},
			error: (response: any) => {
				this.errorText = response.error.message;
			}
		});
	}
}
