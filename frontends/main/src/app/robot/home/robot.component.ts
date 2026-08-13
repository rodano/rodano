import {Component, InjectionToken, OnInit, WritableSignal, inject} from '@angular/core';
import {ActivatedRoute, RouterOutlet, RouterLink} from '@angular/router';
import {Robot} from '@core/model/robot';
import {MatIcon} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';
import {AuditTrailButtonComponent} from '../../audit-trail-button/audit-trail-button.component';
import {MatTooltip} from '@angular/material/tooltip';

export const ROBOT_TOKEN = new InjectionToken<WritableSignal<Robot>>('robot');

@Component({
	templateUrl: './robot.component.html',
	styleUrls: ['./robot.component.css'],
	imports: [
		MatIconButton,
		RouterLink,
		MatIcon,
		MatTooltip,
		RouterOutlet,
		AuditTrailButtonComponent
	]
})
export class RobotComponent implements OnInit {
	//robot is a shared signal so child components can update it and the parent template reacts
	readonly robot = inject(ROBOT_TOKEN);

	constructor(
		private route: ActivatedRoute
	) {}

	ngOnInit() {
		const robot: Robot | undefined = this.route.snapshot.data['robot'];
		if(robot) {
			this.robot.set(robot);
		}
	}
}
