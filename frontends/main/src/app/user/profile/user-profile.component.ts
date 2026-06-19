import {Component, DestroyRef, effect, inject, input, OnInit, signal} from '@angular/core';
import {FormControl, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';
import {User} from '@core/model/user';
import {UserService} from '@core/services/user.service';
import {NotificationService} from '../../services/notification.service';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AuditTrailButtonComponent} from '../../audit-trail-button/audit-trail-button.component';
import {ConfigurationService} from '@core/services/configuration.service';
import {Language} from '@core/model/language';
import {MatOption} from '@angular/material/core';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatSelect} from '@angular/material/select';
import {USER_TOKEN} from '../home/user.component';

@Component({
	templateUrl: './user-profile.component.html',
	styleUrls: ['./user-profile.component.css'],
	imports: [
		ReactiveFormsModule,
		MatFormField,
		MatInput,
		MatSelect,
		MatLabel,
		MatButton,
		MatOption,
		AuditTrailButtonComponent,
		LocalizeMapPipe
	]
})
export class UserProfileComponent implements OnInit {
	readonly user = inject(USER_TOKEN);
	readonly me = input.required<User>();

	readonly languages = signal<Language[]>([]);

	userUpdateForm = new FormGroup({
		name: new FormControl('', [Validators.required]),
		phone: new FormControl(''),
		languageId: new FormControl('')
	});

	constructor(
		private configurationService: ConfigurationService,
		private userService: UserService,
		private notificationService: NotificationService,
		private destroyRef: DestroyRef
	) {
		effect(() => this.updateForm());
	}

	ngOnInit() {
		this.configurationService.getLanguages().subscribe(languages => {
			this.languages.set(languages);
		});
	}

	get canSave() {
		return !this.user().externallyManaged && !this.user().removed && this.user().canWrite;
	}

	updateForm() {
		this.userUpdateForm.reset(this.user());
		if(!this.canSave) {
			this.userUpdateForm.disable();
		}
		else {
			this.userUpdateForm.enable();
		}
	}

	save() {
		const userUpdate = Object.assign({}, this.userUpdateForm.value) as User;

		this.userService.save(this.user().pk, userUpdate).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(user => {
			this.user.set(user);
			this.notificationService.showSuccess('Modifications saved');
		});
	}
}
