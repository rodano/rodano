import {PaginatedSearch} from './paginated-search';

export class OverdueWidgetSearch extends PaginatedSearch {
	static readonly DEFAULT_SORT_BY = 'scopeCode';
	static readonly DEFAULT_SORT_ASCENDING = true;

	widgetId: string;
	scopePks?: number[];
	fullText?: string;

	constructor() {
		super();
		this.sortBy = OverdueWidgetSearch.DEFAULT_SORT_BY;
		this.orderAscending = OverdueWidgetSearch.DEFAULT_SORT_ASCENDING;
	}
}
