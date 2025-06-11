import {Component, OnInit} from '@angular/core';
import {ScheduledTask} from '@core/model/scheduled-task';
import {ActuatorService} from '@core/services/actuator.service';
import {MatTableModule} from '@angular/material/table';
import {MatButton} from '@angular/material/button';
import {NotificationService} from 'src/app/services/notification.service';
import {AdministrationService} from '@core/services/administration.service';

@Component({
	templateUrl: './scheduled-tasks.component.html',
	styleUrls: ['./scheduled-tasks.component.css'],
	imports: [
		MatButton,
		MatTableModule
	]
})
export class ScheduledTasksComponent implements OnInit {
	columnsToDisplay: string[] = ['target', 'schedule', 'actions'];
	scheduledTasks: ScheduledTask[];

	constructor(
		private actuatorService: ActuatorService,
		private administrationService: AdministrationService,
		private notificationService: NotificationService
	) {}

	ngOnInit() {
		this.actuatorService.getScheduledTasks().subscribe(scheduledTasks => this.scheduledTasks = scheduledTasks.cron);
	}

	executeScheduledTask(task: ScheduledTask) {
		this.administrationService.executeScheduledTask(task.runnable.target).subscribe(() => this.notificationService.showSuccess('Task executed'));
	}
}
