import {RoleStatus} from '../../model/role-status';
import {PaginatedSearch} from './paginated-search';

export class UserSearch extends PaginatedSearch {
	static readonly DEFAULT_SORT_BY = 'name';
	static readonly DEFAULT_SORT_ASCENDING = true;
	static readonly DEFAULT_PAGE_SIZE = 20;

	scopePks: number[] = [];
	profileIds: string[] = [];
	states: RoleStatus[] = [];
	email?: string;
	fullText?: string;
	enabled?: boolean;
	externallyManaged?: boolean;

	constructor() {
		super();
		this.sortBy = UserSearch.DEFAULT_SORT_BY;
		this.orderAscending = UserSearch.DEFAULT_SORT_ASCENDING;
		this.pageSize = UserSearch.DEFAULT_PAGE_SIZE;
	}
}
