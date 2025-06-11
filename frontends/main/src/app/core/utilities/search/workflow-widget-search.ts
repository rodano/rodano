import {PaginatedSearch} from './paginated-search';

export class WorkflowWidgetSearch extends PaginatedSearch {
	static readonly DEFAULT_SORT_BY = 'workflow';
	static readonly DEFAULT_SORT_ASCENDING = true;

	widgetId: string;
	scopePks?: number[];
	fullText?: string;

	constructor() {
		super();
		this.sortBy = WorkflowWidgetSearch.DEFAULT_SORT_BY;
		this.orderAscending = WorkflowWidgetSearch.DEFAULT_SORT_ASCENDING;
	}
}
