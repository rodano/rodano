import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Params } from '@angular/router';
import { ScopeSearch } from '../utilities/search/scope-search';

@Injectable({
	providedIn: 'root'
})
export class HttpParamsService {

	toHttpParams(object: any, filter?: string[]) {
		let httpParams = new HttpParams();
		Object.keys(object)
			.filter(key => !filter || !filter.includes(key))
			.forEach(key => {
				const value = object[key];
				if(this.isValueNotNull(value)) {
					if(Array.isArray(value)) {
						if(value.length > 0) {
							const strArray: string[] = [];
							value.filter(this.isValueNotNull).forEach(v => {
								strArray.push(v.toString());
							});
							httpParams = httpParams.append(key, strArray.join(','));
						}
					}
					else if(value instanceof Object && value.constructor === Object) {
						const encodedValue = encodeURIComponent(JSON.stringify(value));
						httpParams = httpParams.append(key, encodedValue);
					}
					else {
						httpParams = httpParams.append(key, value.toString());
					}
				}
			});
		return httpParams;
	}

	toScopeSearch(params: Params): ScopeSearch {
		const search = new ScopeSearch();
		Object.keys(params).forEach(key => {
			const value = params[key] as string;
			switch(key) {
				case 'fullText':
					search.fullText = value;
					break;
				case 'scopeModelId':
					search.scopeModelId = value;
					break;
				case 'pks':
					search.pks = JSON.parse(decodeURIComponent(value));
					break;
				case 'workflowStates':
					search.workflowStates = JSON.parse(decodeURIComponent(value));
					break;
				case 'pageSize':
					search.pageSize = Number(value);
					break;
				case 'pageIndex':
					search.pageIndex = Number(value);
					break;
				case 'orderAscending':
					search.orderAscending = JSON.parse(value);
					break;
			}
		});

		return search;
	}


	private isValueNotNull(value: any) {
		return value !== undefined && value !== null && value !== '';
	}
}
