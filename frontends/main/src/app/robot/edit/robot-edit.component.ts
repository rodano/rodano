import {Component, effect, inject, signal} from '@angular/core';
import {Validators, ReactiveFormsModule, FormControl, FormGroup} from '@angular/forms';
import {RobotService} from '@core/services/robot.service';
import {RobotUpdate} from '@core/model/robot-update';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {AuditTrailButtonComponent} from '../../audit-trail-button/audit-trail-button.component';
import {NotificationService} from '../../services/notification.service';
import {ROBOT_TOKEN} from '../home/robot.component';

@Component({
	templateUrl: './robot-edit.component.html',
	styleUrls: ['./robot-edit.component.css'],
	imports: [
		ReactiveFormsModule,
		MatFormField,
		MatLabel,
		MatInput,
		MatButton,
		AuditTrailButtonComponent
	]
})
export class RobotEditComponent {
	readonly robot = inject(ROBOT_TOKEN);

	robotUpdateForm = new FormGroup({
		name: new FormControl('', [Validators.required]),
		key: new FormControl('')
	});

	readonly error = signal<string | undefined>(undefined);

	constructor(
		private robotService: RobotService,
		private notificationService: NotificationService
	) {
		effect(() => {
			this.robotUpdateForm.reset(this.robot());
			if(this.robot().removed) {
				this.robotUpdateForm.disable();
			}
			else {
				this.robotUpdateForm.enable();
			}
		});
	}

	save() {
		const robotUpdate = this.robotUpdateForm.value as RobotUpdate;
		this.robotService.save(this.robot().pk, robotUpdate).subscribe({
			next: robot => {
				this.robot.set(robot);
				this.notificationService.showSuccess('Robot saved');
			},
			error: e => this.error.set(e.error.message)
		});
	}
}
