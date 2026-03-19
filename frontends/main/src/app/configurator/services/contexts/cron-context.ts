import {Cron} from '@core/model/cron';
import {CronManagerService} from '../manager/cron-manager.service';

export interface CronContext {
	cronManager: CronManagerService;
	crons: Cron[];
	originalCrons: Cron[];
	modifiedCronIds: Set<string>;
}
