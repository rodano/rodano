import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {tap} from 'rxjs/operators';
import {PublicStudy} from '@core/model/public-study';

export interface Project {
	projectId: string;
	code: string;
	shortname: Record<string, string>;
	longname: Record<string, string>;
	description: Record<string, string>;
	url: string;
	color: string;
	introductionText: string;
}

@Injectable({
	providedIn: 'root'
})
export class ProjectService {
	private currentProjectSubject = new BehaviorSubject<Project | null>(null);
	public currentProject$ = this.currentProjectSubject.asObservable();

	constructor(private http: HttpClient) {}

	getAccessibleProjects(): Observable<Project[]> {
		return this.http.get<Project[]>('/api/projects');
	}

	selectProject(projectId: string): Observable<PublicStudy> {
		return this.http.post<PublicStudy>(`/api/projects/select/${projectId}`, {}).pipe(
			tap(study => {
				this.currentProjectSubject.next({
					projectId: study.projectId,
					code: study.id,
					shortname: study.shortname,
					longname: {},
					description: {},
					url: study.url,
					color: study.color,
					introductionText: study.introductionText || ''
				});
			})
		);
	}

	clearProjectSelection(): Observable<void> {
		return this.http.post<void>('/api/projects/clear', {}).pipe(
			tap(() => this.currentProjectSubject.next(null))
		);
	}

	getCurrentProject(): Observable<Project | null> {
		return this.http.get<Project>('/api/projects/current');
	}

	clearCurrentProject(): void {
		this.currentProjectSubject.next(null);
	}
}
