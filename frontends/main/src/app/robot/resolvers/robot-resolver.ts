import {Service, inject} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, Router} from '@angular/router';
import {Observable, EMPTY} from 'rxjs';
import {Robot} from '@core/model/robot';
import {RobotService} from '@core/services/robot.service';
import {NotificationService} from '../../services/notification.service';

@Service()
export class RobotResolver implements Resolve<Robot> {
	private readonly robotService = inject(RobotService);
	private readonly router = inject(Router);
	private readonly notificationService = inject(NotificationService);

	resolve(route: ActivatedRouteSnapshot): Observable<Robot> {
		const robotPkParam = route.paramMap.get('robotPk');
		if(!robotPkParam) {
			this.router.navigate(['/robots']);
			this.notificationService.showError('Could not find robot');
			return EMPTY;
		}
		const robotPk = parseInt(robotPkParam, 10);
		return this.robotService.get(robotPk);
	}
}
