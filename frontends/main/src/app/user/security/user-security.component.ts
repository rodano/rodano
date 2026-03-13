import {ChangeDetectionStrategy, Component, DestroyRef, effect, inject, input} from '@angular/core';
import {FormControl, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';
import {User} from '@core/model/user';
import {UserService} from '@core/services/user.service';
import {MatDialog} from '@angular/material/dialog';
import {UserPasswordDialogComponent} from '../dialogs/user-password.dialog';
import {NotificationService} from 'src/app/services/notification.service';
import {switchMap, takeWhile} from 'rxjs/operators';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatCardModule} from '@angular/material/card';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ChangePasswordComponent} from 'src/app/change-password/change-password.component';
import {ChangePasswordContext} from 'src/app/change-password/change-password-context';
import {UserPasswordDialogResult} from '../dialogs/user-password-dialog-result';
import {DeleteRestoreComponent} from 'src/app/crf/dialogs/delete-restore/delete-restore.component';
import {of} from 'rxjs';
import {USER_TOKEN} from '../home/user.component';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	templateUrl: './user-security.component.html',
	styleUrls: ['./user-security.component.css'],
	imports: [
		ReactiveFormsModule,
		MatCardModule,
		MatLabel,
		MatFormField,
		MatInput,
		MatButton,
		ChangePasswordComponent
	]
})
export class UserSecurityComponent {
	readonly user = inject(USER_TOKEN);
	readonly me = input.required<User>();

	emailForm = new FormGroup({
		email: new FormControl('', {
			nonNullable: true,
			validators: [Validators.required, Validators.email]
		})
	});

	ChangePasswordContext = ChangePasswordContext;

	constructor(
		private userService: UserService,
		private notificationService: NotificationService,
		private dialog: MatDialog,
		private destroyRef: DestroyRef
	) {
		effect(() => {
			this.emailForm.controls.email.setValue(this.user().pendingEmail || this.user().email);
		});
	}

	convertToLocal() {
		this.userService.convertToLocal(this.user().pk).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(user => {
			this.user.set(user);
			this.notificationService.showSuccess('User converted to a local user');
		});
	}

	convertToExternal() {
		this.userService.convertToExternal(this.user().pk).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(user => {
			this.user.set(user);
			this.notificationService.showSuccess('User converted to an external user');
		});
	}

	changeEmail() {
		return this.dialog
			.open<UserPasswordDialogComponent, any, UserPasswordDialogResult>(UserPasswordDialogComponent, {data: {user: this.me(), actionLabel: 'Change email'}})
			.afterClosed()
			.pipe(
				//if the user canceled the dialog, we don't want to continue
				takeWhile(userPassword => !!userPassword),
				switchMap(userPassword => {
					const email = this.emailForm.controls.email.value;
					return this.userService.changeEmail(this.user().pk, userPassword.password, email);
				}),
				takeUntilDestroyed(this.destroyRef)
			).subscribe({
				next: user => {
					this.user.set(user);
					this.notificationService.showSuccess('Email change requested');
				},
				error: response => this.notificationService.showError(response.error.message)
			});
	}

	getDifferenceInDays(date: Date | undefined) {
		if(!date) {
			throw new Error('Unable to calculate difference');
		}
		const now = new Date();
		const time = date.getTime() - now.getTime();
		return Math.ceil(time / (1000 * 3600 * 24));
	}

	resendEmailVerificationEmail() {
		this.userService.resendEmailVerificationEmail(this.user().pk).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(() => {
			this.notificationService.showSuccess('Email verification email resent');
		});
	}

	remove() {
		return this.dialog
			.open(DeleteRestoreComponent, {data: true})
			.afterClosed()
			.pipe(
				switchMap((rationale?: string) => {
					if(rationale) {
						return this.userService.remove(this.user().pk, rationale);
					}
					return of(undefined);
				})
			)
			.subscribe({
				next: user => {
					if(user) {
						this.user.set(user);
						this.notificationService.showSuccess('User removed');
					}
				},
				error: response => {
					this.notificationService.showError(response.error.message);
				}
			});
	}

	restore() {
		return this.dialog
			.open(DeleteRestoreComponent, {data: false})
			.afterClosed()
			.pipe(
				switchMap((rationale?: string) => {
					if(rationale) {
						return this.userService.restore(this.user().pk, rationale);
					}
					return of(undefined);
				})
			)
			.subscribe({
				next: user => {
					if(user) {
						this.user.set(user);
						this.notificationService.showSuccess('User restored');
					}
				},
				error: response => {
					this.notificationService.showError(response.error.message);
				}
			});
	}

	resendAccountActivationEmail() {
		this.userService.resendAccountActivationEmail(this.user().pk).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(() => {
			this.notificationService.showSuccess('Activation email resent');
		});
	}

	unblock() {
		this.userService.unblock(this.user().pk).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(user => {
			this.user.set(user);
			this.notificationService.showSuccess('User unblocked');
		});
	}
}
