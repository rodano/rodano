import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {ProjectService} from '@core/services/project.service';
import {Router} from '@angular/router';
import {catchError, Observable, throwError} from 'rxjs';

@Injectable()
export class ProjectInterceptor implements HttpInterceptor {
	constructor(
		private projectService: ProjectService,
		private router: Router
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
				if(error.status === 403 && error.error?.message?.includes('project')) {
					console.error('Project access denied or mismatch:', error.error.message);
					this.projectService.clearCurrentProject();
					this.router.navigate(['/projects']);
				}

				return throwError(() => error);
			})
		);
	}
}
