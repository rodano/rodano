import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Profile} from '@core/model/profile';

@Injectable({
	providedIn: 'root'
})
export class ProfileService {
	constructor(private http: HttpClient) {}

	getProfiles(projectId: string): Observable<Profile[]> {
		return this.http.get<Profile[]>(`/api/superuser/configurator/projects/${projectId}/config/profiles`);
	}

	getProfile(projectId: string, profileId: string): Observable<Profile> {
		return this.http.get<Profile>(`/api/superuser/configurator/projects/${projectId}/config/profiles/${profileId}`);
	}

	createProfile(projectId: string, profile: Profile): Observable<Profile> {
		return this.http.post<Profile>(`/api/superuser/configurator/projects/${projectId}/config/profiles`, profile);
	}

	updateProfile(projectId: string, profileId: string, profile: Profile): Observable<Profile> {
		return this.http.put<Profile>(`/api/superuser/configurator/projects/${projectId}/config/profiles/${profileId}`, profile);
	}

	deleteProfile(projectId: string, profileId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/profiles/${profileId}`);
	}
}
