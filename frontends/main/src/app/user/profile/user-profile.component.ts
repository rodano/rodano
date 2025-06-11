import {Component, DestroyRef, Input, OnChanges, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';
import {UserDTO} from '@core/model/user-dto';
import {UserService} from '@core/services/user.service';
import {NotificationService} from 'src/app/services/notification.service';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AuditTrailButtonComponent} from 'src/app/audit-trail-button/audit-trail-button.component';
import {ConfigurationService} from '@core/services/configuration.service';
import {LanguageDTO} from '@core/model/language-dto';
import {MatOption} from '@angular/material/core';
import {LocalizeMapPipe} from 'src/app/pipes/localize-map.pipe';
import {MatSelect} from '@angular/material/select';

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
export class UserProfileComponent implements OnInit, OnChanges {
	@Input() me: UserDTO;
	@Input() user: UserDTO;

	languages: LanguageDTO[];

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
	) {}

	ngOnInit() {
		this.configurationService.getLanguages().subscribe(languages => {
			this.languages = languages;
		});
	}

	ngOnChanges() {
		this.updateForm();
	}

	get canSave() {
		return !this.user.externallyManaged && !this.user.removed && this.user.canWrite;
	}

	updateForm() {
		this.userUpdateForm.reset(this.user);
		if(!this.canSave) {
			this.userUpdateForm.disable();
		}
		else {
			this.userUpdateForm.enable();
		}
	}

	save() {
		const userUpdate = Object.assign({}, this.userUpdateForm.value) as UserDTO;

		this.userService.save(this.user.pk, userUpdate).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(user => {
			Object.assign(this.user, user);
			this.notificationService.showSuccess('Modifications saved');
		});
	}
}
