import {Component, OnInit, ViewChild, signal} from '@angular/core';
import {Session} from '@core/model/session';
import {SessionService} from '@core/services/session.service';
import {MatProgressBar} from '@angular/material/progress-bar';
import {AuthStateService} from '../../services/auth-state.service';
import {DateTimeUTCPipe} from '../../pipes/date-time-utc.pipe';
import {MatButton} from '@angular/material/button';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {RouterLink} from '@angular/router';

@Component({
	templateUrl: './connected-users.component.html',
	styleUrls: ['./connected-users.component.css'],
	imports: [
		MatTableModule,
		MatSortModule,
		MatButton,
		MatProgressBar,
		DateTimeUTCPipe,
		RouterLink
	]
})
export class ConnectedUsersComponent implements OnInit {
	columnsToDisplay: string[] = [
		'name',
		'connected-since',
		'last-access',
		'user-agent',
		'actions'
	];

	@ViewChild(MatSort, {static: true}) sort: MatSort;

	readonly sessions = new MatTableDataSource<Session>([]);
	readonly loading = signal(false);

	constructor(
		private sessionService: SessionService,
		private authStateService: AuthStateService
	) {
		const sortingDataAccessor = this.sessions.sortingDataAccessor;
		this.sessions.sortingDataAccessor = (session, column) => {
			switch(column) {
				case 'connected-since':
					return session.connectedSince.getTime();
				case 'last-access':
					return session.lastAccessTime.getTime();
				default:
					return sortingDataAccessor(session, column);
			}
		};
	}

	ngOnInit() {
		this.sessions.sort = this.sort;
		this.loading.set(true);
		this.sessionService.get().subscribe(sessions => {
			this.sessions.data = sessions;
			this.loading.set(false);
		});
	}

	isCurrentSession(session: Session) {
		return this.authStateService.getToken() === session.token;
	}

	logout(sessionPk: number) {
		this.sessionService.delete(sessionPk).subscribe(() => {
			this.sessions.data = this.sessions.data.filter(s => s.pk !== sessionPk);
		});
	}
}
