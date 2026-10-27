import {signal, WritableSignal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ActivatedRoute, Data, provideRouter} from '@angular/router';
import {BehaviorSubject, EMPTY} from 'rxjs';
import {beforeEach, describe, expect, it} from 'vitest';
import {User} from '@core/model/user';
import {USER} from '../../../test/stubs';
import {AuthStateService} from '../../services/auth-state.service';
import {UserComponent, USER_TOKEN} from './user.component';

describe('UserComponent', () => {
	let fixture: ComponentFixture<UserComponent>;
	let user: WritableSignal<User>;
	let data: BehaviorSubject<Data>;
	const me = {...USER, pk: 1, name: 'Me'};
	const other = {...USER, pk: 2, name: 'Other user'};

	beforeEach(async () => {
		user = signal(null as unknown as User);
		data = new BehaviorSubject<Data>({user: other, me});
		await TestBed.configureTestingModule({
			imports: [UserComponent],
			providers: [
				provideRouter([]),
				{provide: ActivatedRoute, useValue: {data, get snapshot() {return {data: data.value};}}},
				{provide: USER_TOKEN, useValue: user},
				{provide: AuthStateService, useValue: {listenConnectedUser: () => EMPTY}}
			]
		}).compileComponents();
		fixture = TestBed.createComponent(UserComponent);
		fixture.componentRef.setInput('me', me);
		fixture.detectChanges();
	});

	it('displays the user resolved by the route', () => {
		expect(user()).toEqual(other);
		expect(fixture.nativeElement.querySelector('h1').textContent).toContain('Other user');
	});

	it('displays the newly resolved user when the component is reused for another user', () => {
		data.next({user: me, me});
		fixture.detectChanges();
		expect(user()).toEqual(me);
		expect(fixture.nativeElement.querySelector('h1').textContent).toContain('Me');
		expect(fixture.nativeElement.querySelector('h1').textContent).not.toContain('Other user');
	});
});
