import {Component, OnInit} from '@angular/core';
import {forkJoin} from 'rxjs';
import {Health} from '@core/model/health';
import {Info} from '@core/model/info';
import {ActuatorService} from '@core/services/actuator.service';
import {HumanReadableFileSizePipe} from '../../pipes/human-readable-file-size';
import {DateTimeUTCPipe} from '../../pipes/date-time-utc.pipe';

@Component({
	templateUrl: './details.component.html',
	styleUrls: ['./details.component.css'],
	imports: [
		DateTimeUTCPipe,
		HumanReadableFileSizePipe
	]
})
export class DetailsComponent implements OnInit {
	info?: Info;
	health?: Health;

	constructor(
		private actuatorService: ActuatorService
	) {}

	ngOnInit() {
		forkJoin({
			info: this.actuatorService.getInfo(),
			health: this.actuatorService.getHealth()
		}).subscribe(result => {
			this.info = result.info;
			this.health = result.health;
		});
	}

	getStatusClass(status: string | undefined): string {
		return status === 'UP' ? 'success' : 'error';
	}
}
