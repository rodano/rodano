import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {ProjectService} from '@core/services/project.service';
import {Router} from '@angular/router';
import {catchError, EMPTY, Observable, throwError} from 'rxjs';
import {NotificationService} from '../../services/notification.service';

@Injectable()
export class ProjectInterceptor implements HttpInterceptor {
	constructor(
		private projectService: ProjectService,
		private router: Router,
		private notificationService: NotificationService
	) {}

	intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		const excludedUrls = [
			'/api/auth/login',
			'/api/auth/logout',
			'/api/auth/recover',
			'/api/projects/select',
			'/api/projects',
			'/api/config/public-file',
			'/api/config/public-study',
			'/api/sessions',
			'/api/administration'
		];

		const shouldExclude = excludedUrls.some(url => req.url.includes(url));
		const isProjectListing = req.url.endsWith('/projects') || req.url.includes('/projects?');

		let modifiedReq = req.clone({
			withCredentials: true
		});

		if(!shouldExclude && !isProjectListing) {
			const projectId = this.projectService.getCurrentProjectId();

			if(projectId) {
				modifiedReq = modifiedReq.clone({
					setHeaders: {
						'X-Project-Id': projectId
					}
				});
			}
		}

		return next.handle(modifiedReq).pipe(
			catchError((error: HttpErrorResponse) => {
				if(error.status === 403) {
					const errorMessage = error.error?.message || '';

					if(errorMessage.includes('No write access to this project')) {
						console.log('✅ Project write access denied - showing notification');
						this.notificationService.showError(
							'This project is read-only. You do not have permission to make changes.'
						);
						return EMPTY;
					}

					else if(errorMessage.includes('project')) {
						console.error('Project access denied or mismatch:', errorMessage);
						this.projectService.clearCurrentProject();
						this.router.navigate(['/projects']);
					}
				}

				return throwError(() => error);
			})
		);
	}
}
