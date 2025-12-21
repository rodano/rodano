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

	private currentProjectIdSubject = new BehaviorSubject<string | null>(
		this.loadProjectIdFromStorage()
	);

	public currentProjectId$ = this.currentProjectIdSubject.asObservable();

	constructor(private http: HttpClient) {}

	private loadProjectIdFromStorage(): string | null {
		return localStorage.getItem('currentProjectId');
	}

	getProjects(): Observable<Project[]> {
		return this.http.get<Project[]>('/api/projects');
	}

	selectProject(projectId: string): Observable<PublicStudy> {
		return this.http.post<PublicStudy>(`/api/projects/select/${projectId}`, {}).pipe(
			tap(study => {
				localStorage.setItem('currentProjectId', projectId);
				this.currentProjectIdSubject.next(projectId);

				const project: Project = {
					projectId: study.projectId,
					code: study.id,
					shortname: study.shortname,
					longname: {},
					description: {},
					url: study.url,
					color: study.color,
					introductionText: study.introductionText || ''
				};
				this.currentProjectSubject.next(project);
			})
		);
	}

	getCurrentProjectId(): string | null {
		return this.currentProjectIdSubject.value;
	}

	getCurrentProject(): Project | null {
		return this.currentProjectSubject.value;
	}

	clearProjectSelection(): Observable<void> {
		return this.http.post<void>('/api/projects/clear', {}).pipe(
			tap(() => this.currentProjectSubject.next(null))
		);
	}

	clearCurrentProject(): void {
		localStorage.removeItem('currentProjectId');
		this.currentProjectSubject.next(null);
		this.currentProjectSubject.next(null);
	}

	hasProjectSelected(): boolean {
		return this.currentProjectIdSubject.value !== null;
	}
}
