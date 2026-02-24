import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Feature} from '@core/model/feature';

@Injectable({
	providedIn: 'root'
})
export class FeatureService {
	constructor(private http: HttpClient) {}

	getFeatures(projectId: string): Observable<Feature[]> {
		return this.http.get<Feature[]>(`/api/superuser/configurator/projects/${projectId}/config/features`);
	}

	getFeature(projectId: string, featureId: string): Observable<Feature> {
		return this.http.get<Feature>(`/api/superuser/configurator/projects/${projectId}/config/features/${featureId}`);
	}

	createFeature(projectId: string, feature: Feature): Observable<Feature> {
		return this.http.post<Feature>(`/api/superuser/configurator/projects/${projectId}/config/features`, feature);
	}

	updateFeature(projectId: string, featureId: string, feature: Feature): Observable<Feature> {
		return this.http.put<Feature>(`/api/superuser/configurator/projects/${projectId}/config/features/${featureId}`, feature);
	}

	deleteFeature(projectId: string, featureId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/features/${featureId}`);
	}
}
