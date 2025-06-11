import {HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {ScopeSearch} from '../utilities/search/scope-search';
import {Params} from '@angular/router';

@Injectable({
	providedIn: 'root'
})
export class HttpParamsService {
	toHttpParams(object: any, filter?: string[]): HttpParams {
		let httpParams = new HttpParams();
		Object.keys(object)
			.filter(key => !filter || !filter.includes(key))
			.forEach(key => {
				const value = object[key];
				const encodedValue = this.encodeValue(value);
				if(encodedValue) {
					httpParams = httpParams.append(key, encodedValue);
				}
			});
		return httpParams;
	}

	private isValueNotNull(value: any) {
		return value !== undefined && value !== null && value !== '';
	}

	private encodeValue(value: any): string | undefined {
		if(!this.isValueNotNull(value)) {
			return undefined;
		}
		if(Array.isArray(value)) {
			if(value.length > 0) {
				return value
					.filter(this.isValueNotNull)
					.map(v => v.toString())
					.join(',');
			}
			return undefined;
		}
		if(value instanceof Object && value.constructor === Object) {
			return encodeURIComponent(JSON.stringify(value));
		}
		if(value instanceof Date) {
			return value.toISOString();
		}
		return value.toString();
	}

	toScopeSearch(params: Params): ScopeSearch {
		const search = new ScopeSearch();
		Object.keys(params).forEach(key => {
			const value = params[key] as string;
			switch(key) {
				case 'scopeModelId':
					//TODO decomment this when we can pass the scope model id array to the scope search API
					//search.scopeModelIds = value.split(',');
					search.scopeModelId = value;
					break;
				case 'pks':
					search.pks = value.split(',').map(s => Number(s));
					break;
				case 'parentPks':
					search.parentPks = value.split(',').map(s => Number(s));
					break;
				case 'ancestorPks':
					search.ancestorPks = value.split(',').map(s => Number(s));
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
				case 'fieldModelCriteria':
					search.fieldModelCriteria = decodeURIComponent(value);
					break;
				default:
					(search as any)[key] = value;
			}
		});

		return search;
	}
}
