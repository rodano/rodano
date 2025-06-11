import {ScheduledTask} from './scheduled-task';

export interface ScheduledTasks {
	cron: ScheduledTask[];
	fixedDelay: ScheduledTask[];
	fixedRate: ScheduledTask[];
	custom: ScheduledTask[];
}
