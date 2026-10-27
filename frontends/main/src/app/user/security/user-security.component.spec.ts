import {signal, WritableSignal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';
import {of, throwError} from 'rxjs';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {User} from '@core/model/user';
import {UserService} from '@core/services/user.service';
import {USER} from '../../../test/stubs';
import {NotificationService} from '../../services/notification.service';
import {USER_TOKEN} from '../home/user.component';
import {UserSecurityComponent} from './user-security.component';

describe('UserSecurityComponent email permissions', () => {
	let fixture: ComponentFixture<UserSecurityComponent>;
	let user: WritableSignal<User>;
	const userService = {changeEmail: vi.fn()};
	const dialog = {open: vi.fn()};
	const notifications = {showSuccess: vi.fn(), showError: vi.fn()};

	beforeEach(async () => {
		vi.resetAllMocks();
		user = signal({...USER, pk: 2, email: 'target@rodano.ch'});
		await TestBed.configureTestingModule({
			imports: [UserSecurityComponent],
			providers: [
				{provide: USER_TOKEN, useValue: user},
				{provide: UserService, useValue: userService},
				{provide: MatDialog, useValue: dialog},
				{provide: NotificationService, useValue: notifications}
			]
		}).compileComponents();
		fixture = TestBed.createComponent(UserSecurityComponent);
	});

	function setCaller(pk: number, admin = false) {
		fixture.componentRef.setInput('me', {...USER, pk, admin});
		fixture.detectChanges();
	}

	function changeButton(): HTMLButtonElement | undefined {
		return Array.from(fixture.nativeElement.querySelectorAll('button') as HTMLButtonElement[]).find(
			button => button.textContent?.trim() === 'Change email'
		);
	}

	it('defensively rejects unauthorized submissions even outside the guarded route', () => {
		user.update(value => ({...value, canWrite: true}));
		setCaller(1);
		fixture.componentInstance.emailForm.controls.email.setValue('new@rodano.ch');
		fixture.componentInstance.changeEmail();
		expect(dialog.open).not.toHaveBeenCalled();
		expect(userService.changeEmail).not.toHaveBeenCalled();
	});

	it('requires administrators to confirm their password before changing another user email directly', () => {
		setCaller(1, true);
		expect(changeButton()).toBeDefined();
		const updatedUser = {...user(), email: 'new@rodano.ch'};
		dialog.open.mockReturnValue({afterClosed: () => of({password: 'admin-secret'})});
		userService.changeEmail.mockReturnValue(of(updatedUser));
		fixture.componentInstance.emailForm.controls.email.setValue(updatedUser.email);
		fixture.componentInstance.changeEmail();
		expect(dialog.open).toHaveBeenCalled();
		expect(dialog.open.mock.calls[0][1].data.user.pk).toBe(1);
		expect(userService.changeEmail).toHaveBeenCalledWith(2, updatedUser.email, 'admin-secret');
		expect(user()).toEqual(updatedUser);
		expect(notifications.showSuccess).toHaveBeenCalledWith('Email changed');
	});

	it('requires a password dialog for a non-administrator changing their own email', () => {
		setCaller(2);
		expect(changeButton()).toBeDefined();
		dialog.open.mockReturnValue({afterClosed: () => of({password: 'secret'})});
		userService.changeEmail.mockReturnValue(of({...user(), pendingEmail: 'new@rodano.ch'}));
		fixture.componentInstance.emailForm.controls.email.setValue('new@rodano.ch');
		fixture.componentInstance.changeEmail();
		expect(dialog.open).toHaveBeenCalled();
		expect(userService.changeEmail).toHaveBeenCalledWith(2, 'new@rodano.ch', 'secret');
		expect(notifications.showSuccess).toHaveBeenCalledWith('Email change requested');
	});

	it('uses the direct flow when an administrator edits their own email', () => {
		setCaller(2, true);
		dialog.open.mockReturnValue({afterClosed: () => of({password: 'admin-secret'})});
		userService.changeEmail.mockReturnValue(of({...user(), email: 'new@rodano.ch'}));
		fixture.componentInstance.emailForm.controls.email.setValue('new@rodano.ch');
		fixture.componentInstance.changeEmail();
		expect(userService.changeEmail).toHaveBeenCalledWith(2, 'new@rodano.ch', 'admin-secret');
		expect(notifications.showSuccess).toHaveBeenCalledWith('Email changed');
	});

	it('does not change the email when an administrator cancels the password dialog', () => {
		setCaller(1, true);
		dialog.open.mockReturnValue({afterClosed: () => of(undefined)});
		fixture.componentInstance.emailForm.controls.email.setValue('new@rodano.ch');
		fixture.componentInstance.changeEmail();
		expect(userService.changeEmail).not.toHaveBeenCalled();
		expect(fixture.componentInstance.emailForm.controls.email.value).toBe(user().email);
	});

	it('does not submit when the password dialog is cancelled', () => {
		setCaller(2);
		dialog.open.mockReturnValue({afterClosed: () => of(undefined)});
		fixture.componentInstance.emailForm.controls.email.setValue('new@rodano.ch');
		fixture.componentInstance.changeEmail();
		expect(userService.changeEmail).not.toHaveBeenCalled();
		expect(fixture.componentInstance.emailForm.controls.email.value).toBe(user().email);
	});

	it('shows the current email and direct-flow explanation to admins with a pending change', () => {
		user.update(value => ({...value, pendingEmail: 'pending@rodano.ch'}));
		setCaller(1, true);
		expect(fixture.componentInstance.emailForm.controls.email.value).toBe(user().email);
		expect(fixture.nativeElement.textContent).toContain('The user is notified at their previous email.');
		expect(fixture.nativeElement.textContent).not.toContain('Resend email verification email');
	});

	it('restores the current email and displays errors when a direct update fails', () => {
		setCaller(1, true);
		dialog.open.mockReturnValue({afterClosed: () => of({password: 'admin-secret'})});
		userService.changeEmail.mockReturnValue(throwError(() => ({error: {message: 'Email already used'}})));
		fixture.componentInstance.emailForm.controls.email.setValue('new@rodano.ch');
		fixture.componentInstance.changeEmail();
		expect(fixture.componentInstance.emailForm.controls.email.value).toBe(user().email);
		expect(notifications.showError).toHaveBeenCalledWith('Email already used');
	});

	it('does not submit invalid email addresses', () => {
		setCaller(1, true);
		fixture.componentInstance.emailForm.controls.email.setValue('invalid');
		fixture.componentInstance.changeEmail();
		expect(userService.changeEmail).not.toHaveBeenCalled();
		expect(dialog.open).not.toHaveBeenCalled();
	});
});
