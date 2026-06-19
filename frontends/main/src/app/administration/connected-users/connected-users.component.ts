import {Component, OnInit, signal} from '@angular/core';
import {Session} from '@core/model/session';
import {SessionService} from '@core/services/session.service';
import {MatProgressBar} from '@angular/material/progress-bar';
import {AuthStateService} from '../../services/auth-state.service';
import {DateTimeUTCPipe} from '../../pipes/date-time-utc.pipe';
import {MatButton} from '@angular/material/button';
import {MatTableModule} from '@angular/material/table';

@Component({
	templateUrl: './connected-users.component.html',
	styleUrls: ['./connected-users.component.css'],
	imports: [
		MatTableModule,
		MatButton,
		MatProgressBar,
		DateTimeUTCPipe
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

	readonly sessions = signal<Session[]>([]);
	readonly loading = signal(false);

	constructor(
		private sessionService: SessionService,
		private authStateService: AuthStateService
	) { }

	ngOnInit() {
		this.loading.set(true);
		this.sessionService.get().subscribe(sessions => {
			this.sessions.set(sessions);
			this.loading.set(false);
		});
	}

	isCurrentSession(session: Session) {
		return this.authStateService.getToken() === session.token;
	}

	logout(sessionPk: number) {
		this.sessionService.delete(sessionPk).subscribe(() => {
			this.sessions.set(this.sessions().filter(s => s.pk !== sessionPk));
		});
	}
}
