import {PaginatedSearch} from './paginated-search';

export class WorkflowStatusSearch extends PaginatedSearch {
	static readonly DEFAULT_SORT_BY = 'workflowId';
	static readonly DEFAULT_SORT_ASCENDING = true;

	workflowIds?: string[];
	stateIds?: string[];
	ancestorScopePks?: number[];
	scopePks?: number[];
	eventPks?: number[];
	fullText?: string;
	filterExpectedEvents?: boolean;

	constructor() {
		super();
		this.sortBy = WorkflowStatusSearch.DEFAULT_SORT_BY;
		this.orderAscending = WorkflowStatusSearch.DEFAULT_SORT_ASCENDING;
	}
}
