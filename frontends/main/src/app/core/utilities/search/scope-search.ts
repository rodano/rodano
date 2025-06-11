import {PaginatedSearch} from './paginated-search';

export class ScopeSearch extends PaginatedSearch {
	static readonly DEFAULT_SORT_BY = 'code';
	static readonly DEFAULT_SORT_ASCENDING = true;

	scopeModelId?: string;
	fullText?: string;
	code?: string;
	ids: string[] = [];
	pks: number[] = [];
	parentPks: number[] = [];
	ancestorPks: number[] = [];
	workflowStates: Record<string, string[]> = {};
	fieldModelCriteria?: string;
	leaf?: boolean;

	constructor() {
		super();
		this.sortBy = ScopeSearch.DEFAULT_SORT_BY;
		this.orderAscending = ScopeSearch.DEFAULT_SORT_ASCENDING;
		this.pageSize = 20;
	}
}
