import {SortDirection} from '@angular/material/sort';

export class PaginatedSearch {
	static getSortDirection(orderAscending: boolean): SortDirection {
		return orderAscending ? 'asc' : 'desc';
	}

	static getOrderAscending(sortDirection: SortDirection): boolean {
		return sortDirection !== 'desc';
	}

	pageSize = 10;
	pageIndex = 0;
	sortBy?: string;
	orderAscending = true;
}
