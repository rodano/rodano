import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {APIService} from './api.service';
import {map, Observable} from 'rxjs';
import {CRFDocumentationGenerationStatus} from '../model/crf-documentation-generation-status';

@Injectable({
	providedIn: 'root'
})
export class DocumentationService {
	private serviceUrl: string;

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {
		this.serviceUrl = `${this.apiService.getApiUrl()}/documentation`;
	}

	getDataStructureUrl(): string {
		return `${this.serviceUrl}/data-structure`;
	}

	getBlankCrfUrl(scopeModelId: string, annotated = false): string {
		const params = new HttpParams()
			.set('annotated', annotated);
		return `${this.serviceUrl}/crf-blank/${scopeModelId}?${params}`;
	}

	getArchiveOneCrfUrl(scopePk: number, auditTrails = false): string {
		const params = new HttpParams()
			.set('auditTrails', auditTrails);
		return `${this.serviceUrl}/crf-archive/${scopePk}?${params}`;
	}

	archiveCrfRequest(scopePks: number[], scopeModelId: string, auditTrails = false): Observable<string> {
		const payload = {
			scopeModelId,
			scopePks,
			auditTrails
		};
		return this.http.post<{id: string}>(`${this.serviceUrl}/crf-archive/request`, payload).pipe(
			map(r => r.id)
		);
	}

	getArchiveCrfStatus(): Observable<CRFDocumentationGenerationStatus> {
		return this.http.get<{status: CRFDocumentationGenerationStatus}>(`${this.serviceUrl}/crf-archive/status`).pipe(
			map(r => r.status)
		);
	}

	getArchiveMultipleCrfUrl(): string {
		return `${this.serviceUrl}/crf-archive/download`;
	}
}
