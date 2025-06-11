import {PaginatedSearch} from './paginated-search';

export class ResourceSearch extends PaginatedSearch {
	static readonly DEFAULT_SORT_BY = 'title';
	static readonly DEFAULT_SORT_ASCENDING = true;

	categoryId?: string;
	fullText?: string;
	removed?: boolean;

	constructor() {
		super();
		this.sortBy = ResourceSearch.DEFAULT_SORT_BY;
		this.orderAscending = ResourceSearch.DEFAULT_SORT_ASCENDING;
	}
}
