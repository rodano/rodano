import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {WidgetLayout} from '@core/model/widget-layout';

@Injectable({providedIn: 'root'})
export class LayoutService {
	constructor(private http: HttpClient) {}

	getLayout(projectId: string, entityPath: string): Observable<WidgetLayout> {
		return this.http.get<WidgetLayout>(`/api/superuser/configurator/projects/${projectId}/config/${entityPath}/layout`);
	}

	saveLayout(projectId: string, entityPath: string, layout: WidgetLayout): Observable<WidgetLayout> {
		return this.http.put<WidgetLayout>(`/api/superuser/configurator/projects/${projectId}/config/${entityPath}/layout`, layout);
	}
}
