import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {CreateProjectRequest} from '@core/model/create-project-request';
import {ProjectConfigVersion} from '@core/model/project-config-version';
import {ConfigSnapshot} from '@core/model/config-snapshot';

@Injectable({
	providedIn: 'root'
})
export class ConfiguratorService {
	private readonly baseUrl = '/api/superuser/configurator';

	constructor(private http: HttpClient) {}

	getAllProjects(): Observable<ConfiguratorProject[]> {
		return this.http.get<ConfiguratorProject[]>(`${this.baseUrl}/projects`);
	}

	getProject(projectId: string): Observable<ConfiguratorProject> {
		return this.http.get<ConfiguratorProject>(`${this.baseUrl}/projects/${projectId}`);
	}

	createProject(request: CreateProjectRequest): Observable<ConfiguratorProject> {
		return this.http.post<ConfiguratorProject>(`${this.baseUrl}/projects`, request);
	}

	updateProject(projectId: string, project: Partial<ConfiguratorProject>): Observable<ConfiguratorProject> {
		const updateRequest = {
			code: project.code,
			shortname: project.shortname,
			longname: project.longname,
			description: project.description,
			url: project.url,
			color: project.color,
			introductionText: project.introductionText,
			versionDate: project.versionDate,
			email: project.email,
			smtpTls: project.smtpTls,
			passwordStrong: project.passwordStrong,
			passwordLength: project.passwordLength,
			passwordValidityDuration: project.passwordValidityDuration,
			passwordUnique: project.passwordUnique,
			eproEnabled: project.eproEnabled,
			eproProfileId: project.eproProfileId,
			clientName: project.clientName,
			clientEmail: project.clientEmail,
			protocolNo: project.protocolNo,
			versionNumber: project.versionNumber,
			languages: project.languages,
			ruleTags: project.ruleTags
		};

		return this.http.put<ConfiguratorProject>(`${this.baseUrl}/projects/${projectId}`, updateRequest);
	}

	getOrCreateDraft(projectId: string): Observable<ProjectConfigVersion> {
		return this.http.get<ProjectConfigVersion>(`${this.baseUrl}/projects/${projectId}/draft`);
	}

	publishDraft(projectId: string, versionId: number, changeSummary: string): Observable<void> {
		return this.http.put<void>(`${this.baseUrl}/projects/${projectId}/versions/${versionId}/publish`, {changeSummary});
	}

	archiveDraft(projectId: string, versionId: number): Observable<void> {
		return this.http.put<void>(`${this.baseUrl}/projects/${projectId}/versions/${versionId}/archive`, {});
	}

	restoreDraft(projectId: string, versionId: number): Observable<void> {
		return this.http.put<void>(`${this.baseUrl}/projects/${projectId}/versions/${versionId}/restore`, {});
	}

	getVersions(projectId: string): Observable<ProjectConfigVersion[]> {
		return this.http.get<ProjectConfigVersion[]>(`${this.baseUrl}/projects/${projectId}/versions`);
	}

	getVersion(projectId: string, versionId: number): Observable<ProjectConfigVersion> {
		return this.http.get<ProjectConfigVersion>(`${this.baseUrl}/projects/${projectId}/versions/${versionId}`);
	}

	createSnapshot(projectId: string, versionId: number, summary: string): Observable<void> {
		return this.http.post<void>(`${this.baseUrl}/projects/${projectId}/versions/${versionId}/snapshot`, {summary});
	}

	rollbackSnapshot(projectId: string, versionId: number): Observable<ConfiguratorProject> {
		return this.http.post<ConfiguratorProject>(`${this.baseUrl}/projects/${projectId}/versions/${versionId}/rollback`, {});
	}

	rollForwardSnapshot(projectId: string, versionId: number): Observable<ConfiguratorProject> {
		return this.http.post<ConfiguratorProject>(`${this.baseUrl}/projects/${projectId}/versions/${versionId}/rollforward`, {});
	}

	getSnapshots(projectId: string, versionId: number): Observable<ConfigSnapshot> {
		return this.http.get<ConfigSnapshot>(`${this.baseUrl}/projects/${projectId}/versions/${versionId}/snapshots`);
	}

	exportConfig(projectId: string): Observable<Blob> {
		return this.http.get(`/api/superuser/configurator/projects/${projectId}/config/export`, {responseType: 'blob'});
	}

	cloneProject(projectId: string, request: any): Observable<ConfiguratorProject> {
		return this.http.post<ConfiguratorProject>(`/api/superuser/configurator/projects/${projectId}/clone`, request);
	}
}
