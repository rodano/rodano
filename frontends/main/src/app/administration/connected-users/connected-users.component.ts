import {Component, OnInit} from '@angular/core';
import {SessionDTO} from '@core/model/session-dto';
import {SessionService} from '@core/services/session.service';
import {AuthStateService} from 'src/app/services/auth-state.service';
import {DateTimeUTCPipe} from '../../pipes/date-time-utc.pipe';
import {MatButton} from '@angular/material/button';
import {MatTableModule} from '@angular/material/table';

@Component({
	templateUrl: './connected-users.component.html',
	styleUrls: ['./connected-users.component.css'],
	imports: [
		MatTableModule,
		MatButton,
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

	sessions: SessionDTO[];

	constructor(
		private sessionService: SessionService,
		private authStateService: AuthStateService
	) { }

	ngOnInit() {
		this.sessionService.get().subscribe(sessions => this.sessions = sessions);
	}

	isCurrentSession(session: SessionDTO) {
		return this.authStateService.getToken() === session.token;
	}

	logout(sessionPk: number) {
		this.sessionService.delete(sessionPk).subscribe(() => {
			this.sessions = this.sessions.filter(session => session.pk !== sessionPk);
		});
	}
}
