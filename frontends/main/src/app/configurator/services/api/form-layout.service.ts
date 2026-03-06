import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Layout} from '@core/model/layout';

@Injectable({
	providedIn: 'root'
})
export class FormLayoutService {
	constructor(private http: HttpClient) {}

	getLayouts(projectId: string, formModelId: string): Observable<Layout[]> {
		return this.http.get<Layout[]>(
			`/api/superuser/configurator/projects/${projectId}/config/form-models/${formModelId}/form-layouts`
		);
	}

	getLayout(projectId: string, formModelId: string, formLayoutId: string): Observable<Layout> {
		return this.http.get<Layout>(
			`/api/superuser/configurator/projects/${projectId}/config/form-models/${formModelId}/form-layouts/${formLayoutId}`
		);
	}

	createLayout(projectId: string, formModelId: string, formLayout: Layout): Observable<Layout> {
		return this.http.post<Layout>(
			`/api/superuser/configurator/projects/${projectId}/config/form-models/${formModelId}/form-layouts`, formLayout
		);
	}

	updateLayout(projectId: string, formModelId: string, formLayoutId: string, formLayout: Layout): Observable<Layout> {
		return this.http.put<Layout>(
			`/api/superuser/configurator/projects/${projectId}/config/form-models/${formModelId}/form-layouts/${formLayoutId}`,
			formLayout
		);
	}

	deleteLayout(projectId: string, formModelId: string, formLayoutId: string): Observable<void> {
		return this.http.delete<void>(
			`/api/superuser/configurator/projects/${projectId}/config/form-models/${formModelId}/form-layouts/${formLayoutId}`
		);
	}
}
