import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {of} from 'rxjs';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {Session} from '@core/model/session';
import {SessionService} from '@core/services/session.service';
import {AuthStateService} from '../../services/auth-state.service';
import {ConnectedUsersComponent} from './connected-users.component';

describe('ConnectedUsersComponent', () => {
	let fixture: ComponentFixture<ConnectedUsersComponent>;
	const sessions: Session[] = [
		{
			pk: 1,
			token: 'current-token',
			userPk: 101,
			name: 'Zoe',
			connectedSince: new Date('2025-01-01T00:00:00Z'),
			lastAccessTime: new Date('2025-03-01T00:00:00Z'),
			userAgent: 'Browser'
		},
		{
			pk: 2,
			token: 'other-token',
			userPk: 102,
			name: 'Alice',
			connectedSince: new Date('2024-12-31T23:59:59Z'),
			lastAccessTime: new Date('2025-04-01T00:00:00Z'),
			userAgent: 'Browser'
		},
		{
			pk: 3,
			token: 'third-token',
			userPk: 103,
			name: 'Bob',
			connectedSince: new Date('2025-02-01T00:00:00Z'),
			lastAccessTime: new Date('2025-02-01T00:00:00Z'),
			userAgent: 'Browser'
		}
	];
	const deleteSession = vi.fn(() => of(undefined));

	beforeEach(async () => {
		deleteSession.mockClear();
		await TestBed.configureTestingModule({
			imports: [ConnectedUsersComponent],
			providers: [
				provideRouter([]),
				{provide: SessionService, useValue: {get: () => of(sessions), delete: deleteSession}},
				{provide: AuthStateService, useValue: {getToken: () => 'current-token'}}
			]
		}).compileComponents();
		fixture = TestBed.createComponent(ConnectedUsersComponent);
		fixture.detectChanges();
	});

	function names(): string[] {
		const element: HTMLElement = fixture.nativeElement;
		return Array.from(element.querySelectorAll('td.mat-column-name'), cell => cell.textContent?.trim() ?? '');
	}

	function sort(column: string) {
		const element: HTMLElement = fixture.nativeElement;
		element.querySelector<HTMLElement>(`th.mat-column-${column} .mat-sort-header-container`)!.click();
		fixture.detectChanges();
	}

	it.each([
		['name', ['Alice', 'Bob', 'Zoe']],
		['connected-since', ['Alice', 'Zoe', 'Bob']],
		['last-access', ['Bob', 'Zoe', 'Alice']]
	])('sorts %s ascending and descending and restores the original order when cleared', (column, expected) => {
		sort(column);
		expect(names()).toEqual(expected);
		sort(column);
		expect(names()).toEqual([...expected].reverse());
		sort(column);
		expect(names()).toEqual(['Zoe', 'Alice', 'Bob']);
	});

	it('links each name to the user rather than the session', () => {
		const element: HTMLElement = fixture.nativeElement;
		const links = Array.from(element.querySelectorAll<HTMLAnchorElement>('td.mat-column-name a'));
		expect(links.map(link => link.getAttribute('href'))).toEqual(['/users/101', '/users/102', '/users/103']);
	});

	it('keeps the selected sort after logout and does not offer logout for the current session', () => {
		sort('name');
		const element: HTMLElement = fixture.nativeElement;
		const rows = Array.from(element.querySelectorAll<HTMLTableRowElement>('tr.mat-mdc-row'));
		expect(rows[2].querySelector('button')).toBeNull();
		rows[0].querySelector<HTMLButtonElement>('button')!.click();
		fixture.detectChanges();
		expect(deleteSession).toHaveBeenCalledWith(2);
		expect(names()).toEqual(['Bob', 'Zoe']);
		expect(fixture.componentInstance.sort.active).toBe('name');
	});
});
