import {Component, OnInit, signal} from '@angular/core';
import {ScheduledTask} from '@core/model/scheduled-task';
import {ActuatorService} from '@core/services/actuator.service';
import {MatProgressBar} from '@angular/material/progress-bar';
import {MatTableModule} from '@angular/material/table';
import {MatButton} from '@angular/material/button';
import {NotificationService} from '../../services/notification.service';
import {AdministrationService} from '@core/services/administration.service';

@Component({
	templateUrl: './scheduled-tasks.component.html',
	styleUrls: ['./scheduled-tasks.component.css'],
	imports: [
		MatButton,
		MatProgressBar,
		MatTableModule
	]
})
export class ScheduledTasksComponent implements OnInit {
	columnsToDisplay: string[] = ['target', 'schedule', 'actions'];
	readonly scheduledTasks = signal<ScheduledTask[]>([]);
	readonly loading = signal(false);

	constructor(
		private actuatorService: ActuatorService,
		private administrationService: AdministrationService,
		private notificationService: NotificationService
	) {}

	ngOnInit() {
		this.loading.set(true);
		this.actuatorService.getScheduledTasks().subscribe(scheduledTasks => {
			this.scheduledTasks.set(scheduledTasks.cron);
			this.loading.set(false);
		});
	}

	executeScheduledTask(task: ScheduledTask) {
		this.administrationService.executeScheduledTask(task.runnable.target).subscribe(() => this.notificationService.showSuccess('Task executed'));
	}
}
