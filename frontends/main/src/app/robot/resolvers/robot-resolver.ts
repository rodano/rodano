import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, Router} from '@angular/router';
import {Observable, EMPTY} from 'rxjs';
import {RobotDTO} from '@core/model/robot-dto';
import {RobotService} from '@core/services/robot.service';
import {NotificationService} from 'src/app/services/notification.service';

@Injectable({
	providedIn: 'root'
})
export class RobotResolver implements Resolve<RobotDTO> {
	constructor(
		private robotService: RobotService,
		private router: Router,
		private notificationService: NotificationService
	) {}

	resolve(route: ActivatedRouteSnapshot): Observable<RobotDTO> {
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
