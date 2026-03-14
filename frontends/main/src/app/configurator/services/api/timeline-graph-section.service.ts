import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {TimelineGraphSection} from '@core/model/timeline-graph-section';

@Injectable({providedIn: 'root'})
export class TimelineGraphSectionService {
	constructor(private http: HttpClient) {}

	private base(projectId: string, timelineGraphId: string): string {
		return `/api/superuser/configurator/projects/${projectId}/config/timeline-graphs/${timelineGraphId}/sections`;
	}

	getSections(projectId: string, timelineGraphId: string): Observable<TimelineGraphSection[]> {
		return this.http.get<TimelineGraphSection[]>(this.base(projectId, timelineGraphId));
	}

	getSection(projectId: string, timelineGraphId: string, sectionId: string): Observable<TimelineGraphSection> {
		return this.http.get<TimelineGraphSection>(`${this.base(projectId, timelineGraphId)}/${sectionId}`);
	}

	createSection(projectId: string, timelineGraphId: string, section: TimelineGraphSection): Observable<TimelineGraphSection> {
		return this.http.post<TimelineGraphSection>(this.base(projectId, timelineGraphId), section);
	}

	updateSection(projectId: string, timelineGraphId: string, sectionId: string, section: TimelineGraphSection): Observable<TimelineGraphSection> {
		return this.http.put<TimelineGraphSection>(`${this.base(projectId, timelineGraphId)}/${sectionId}`, section);
	}

	deleteSection(projectId: string, timelineGraphId: string, sectionId: string): Observable<void> {
		return this.http.delete<void>(`${this.base(projectId, timelineGraphId)}/${sectionId}`);
	}
}
