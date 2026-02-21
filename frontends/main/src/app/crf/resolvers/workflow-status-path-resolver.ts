import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Router, Resolve} from '@angular/router';
import {WorkflowStatusService} from '@core/services/workflow-status.service';
import {Observable, EMPTY} from 'rxjs';
import {tap} from 'rxjs/operators';
import {NotificationService} from 'src/app/services/notification.service';
import {FormInfo} from '@core/model/form-info';
import {SideMenuComponent} from '../side-menu/side-menu.component';

//TODO rework this resolver or find another way to navigate to issues

@Injectable({
	providedIn: 'root'
})
export class WorkflowStatusPathResolver implements Resolve<FormInfo> {
	constructor(
		private workflowStatusService: WorkflowStatusService,
		private router: Router,
		private notificationService: NotificationService
	) {}

	resolve(route: ActivatedRouteSnapshot): Observable<FormInfo> {
		const wsPkParam = route.paramMap.get('statusPk');
		if(!wsPkParam) {
			this.router.navigate(['/search']);
			this.notificationService.showError('Could not find workflow status');
			return EMPTY;
		}
		const wsPk = parseInt(wsPkParam, 10);
		return this.workflowStatusService.getFormForWorkflowStatus(wsPk).pipe(
			tap(form => {
				const command = ['/crf', form.scopePk];
				const queryParams: Record<string, string> = {};
				if(form.eventPk) {
					command.push('events', form.eventPk);
					queryParams[SideMenuComponent.EXPANDED_EVENT_PKS_PARAMETER] = form.eventPk.toString();
				}
				if(form.formPk) {
					command.push('forms', form.formPk);
				}
				this.router.navigate(command, {queryParams});
			})
		);
	}
}
