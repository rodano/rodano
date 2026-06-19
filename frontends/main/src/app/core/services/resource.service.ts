import {HttpClient, HttpEvent, HttpRequest} from '@angular/common/http';
import {Service, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {PagedResultResource} from '../model/paged-result-resource';
import {ResourceSearch} from '../utilities/search/resource-search';
import {APIService} from './api.service';
import {HttpParamsService} from './http-params.service';
import {ResourceSubmission} from '../model/resource-submission';
import {Resource} from '../model/resource';
import {reviveDates} from '../decorators/revive-dates.decorator';

@Service()
export class ResourceService {
	serviceUrl: string;

	private readonly http = inject(HttpClient);
	private readonly apiService = inject(APIService);
	private readonly httpParamsService = inject(HttpParamsService);

	constructor() {
		this.serviceUrl = `${this.apiService.getApiUrl()}/resources`;
	}

	@reviveDates
	search(search: ResourceSearch): Observable<PagedResultResource> {
		const params = this.httpParamsService.toHttpParams(search);
		return this.http.get<PagedResultResource>(this.serviceUrl, {params});
	}

	create(resourceSubmission: ResourceSubmission): Observable<Resource> {
		return this.http.post<Resource>(this.serviceUrl, resourceSubmission);
	}

	save(resource: Resource): Observable<Resource> {
		return this.http.put<Resource>(`${this.serviceUrl}/${resource.pk}`, resource);
	}

	remove(resource: Resource): Observable<Resource> {
		return this.http.put<Resource>(`${this.serviceUrl}/${resource.pk}/remove`, {});
	}

	restore(resource: Resource): Observable<Resource> {
		return this.http.put<Resource>(`${this.serviceUrl}/${resource.pk}/restore`, {});
	}

	downloadFile(resource: Resource): Observable<Blob> {
		return this.http.get(this.getFileUrl(resource), {responseType: 'blob'});
	}

	getFileUrl(resource: Resource): string {
		return `${this.serviceUrl}/${resource.pk}/file`;
	}

	uploadFile(resourcePk: number, file: File): Observable<HttpEvent<Resource>> {
		const formData = new FormData();
		formData.append('file', file, file.name);

		const url = `${this.serviceUrl}/${resourcePk}/file`;
		const request = new HttpRequest('POST', url, formData, {reportProgress: true});
		return this.http.request(request);
	}
}
