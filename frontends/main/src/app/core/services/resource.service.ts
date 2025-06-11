import {HttpClient, HttpEvent, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {PagedResultResourceDTO} from '../model/paged-result-resource-dto';
import {ResourceSearch} from '../utilities/search/resource-search';
import {APIService} from './api.service';
import {HttpParamsService} from './http-params.service';
import {ResourceSubmissionDTO} from '../model/resource-submission-dto';
import {ResourceDTO} from '../model/resource-dto';
import {reviveDates} from '../decorators/revive-dates.decorator';

@Injectable({
	providedIn: 'root'
})
export class ResourceService {
	serviceUrl: string;

	constructor(
		private http: HttpClient,
		private apiService: APIService,
		private httpParamsService: HttpParamsService
	) {
		this.serviceUrl = `${this.apiService.getApiUrl()}/resources`;
	}

	@reviveDates
	search(search: ResourceSearch): Observable<PagedResultResourceDTO> {
		const params = this.httpParamsService.toHttpParams(search);
		return this.http.get<PagedResultResourceDTO>(this.serviceUrl, {params});
	}

	create(resourceSubmission: ResourceSubmissionDTO): Observable<ResourceDTO> {
		return this.http.post<ResourceDTO>(this.serviceUrl, resourceSubmission);
	}

	save(resource: ResourceDTO): Observable<ResourceDTO> {
		return this.http.put<ResourceDTO>(`${this.serviceUrl}/${resource.pk}`, resource);
	}

	remove(resource: ResourceDTO): Observable<ResourceDTO> {
		return this.http.put<ResourceDTO>(`${this.serviceUrl}/${resource.pk}/remove`, {});
	}

	restore(resource: ResourceDTO): Observable<ResourceDTO> {
		return this.http.put<ResourceDTO>(`${this.serviceUrl}/${resource.pk}/restore`, {});
	}

	downloadFile(resource: ResourceDTO): Observable<Blob> {
		return this.http.get(this.getFileUrl(resource), {responseType: 'blob'});
	}

	getFileUrl(resource: ResourceDTO): string {
		return `${this.serviceUrl}/${resource.pk}/file`;
	}

	uploadFile(resourcePk: number, file: File): Observable<HttpEvent<ResourceDTO>> {
		const formData = new FormData();
		formData.append('file', file, file.name);

		const url = `${this.serviceUrl}/${resourcePk}/file`;
		const request = new HttpRequest('POST', url, formData, {reportProgress: true});
		return this.http.request(request);
	}
}
