import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {TimelineGraph} from '@core/model/timeline-graph';

@Injectable({
	providedIn: 'root'
})
export class TimelineGraphService {
	constructor(private http: HttpClient) {}

	getTimelineGraphs(projectId: string): Observable<TimelineGraph[]> {
		return this.http.get<TimelineGraph[]>(`/api/superuser/configurator/projects/${projectId}/config/timeline-graphs`);
	}

	getTimelineGraph(projectId: string, timelineGraphId: string): Observable<TimelineGraph> {
		return this.http.get<TimelineGraph>(`/api/superuser/configurator/projects/${projectId}/config/timeline-graphs/${timelineGraphId}`);
	}

	createTimelineGraph(projectId: string, timelineGraph: TimelineGraph): Observable<TimelineGraph> {
		return this.http.post<TimelineGraph>(`/api/superuser/configurator/projects/${projectId}/config/timeline-graphs`, timelineGraph);
	}

	updateTimelineGraph(projectId: string, timelineGraphId: string, timelineGraph: TimelineGraph): Observable<TimelineGraph> {
		return this.http.put<TimelineGraph>(`/api/superuser/configurator/projects/${projectId}/config/timeline-graphs/${timelineGraphId}`, timelineGraph);
	}

	deleteTimelineGraph(projectId: string, timelineGraphId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/timeline-graphs/${timelineGraphId}`);
	}
}
