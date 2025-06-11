import { APISettings } from 'src/app/api/settings/api-settings';

export class PaginatedSearch {

	pageSize = APISettings.DEFAULT_PAGE_SIZE;
	pageIndex = 0;
	sortBy?: string;
	orderAscending?: boolean;

	reset() {
		this.pageSize = APISettings.DEFAULT_PAGE_SIZE;
		this.pageIndex = 0;
	}

	resetPagination() {
		this.pageIndex = 0;
	}
}
