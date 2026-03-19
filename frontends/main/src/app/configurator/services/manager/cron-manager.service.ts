import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {BaseManagerService} from './base-manager.service';
import {Cron} from '@core/model/cron';
import {CronService} from '../api/cron.service';

@Injectable({providedIn: 'root'})
export class CronManagerService extends BaseManagerService<Cron> {
	constructor(private cronService: CronService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (f: Cron) => f.cronId;}
	protected getSimpleFields(): (keyof Cron)[] {
		return ['id', 'intervalValue', 'intervalUnit'];
	}

	protected getTranslationFields(): (keyof Cron)[] {
		return ['description'];
	}

	protected getArrayFields(): (keyof Cron)[] {
		return [];
	}

	protected fetchAll(projectId: string): Observable<Cron[]> {
		return this.cronService.getCrons(projectId);
	}

	protected createEntity(projectId: string, entity: Cron): Observable<Cron> {
		return this.cronService.createCron(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.cronService.deleteCron(projectId, id);
	}
}
