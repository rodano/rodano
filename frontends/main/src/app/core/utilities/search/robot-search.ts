import {PaginatedSearch} from './paginated-search';

export class RobotSearch extends PaginatedSearch {
	static readonly DEFAULT_SORT_BY = 'name';
	static readonly DEFAULT_SORT_ASCENDING = true;

	name?: string;
	profileId?: string;

	constructor() {
		super();
		this.sortBy = RobotSearch.DEFAULT_SORT_BY;
		this.orderAscending = RobotSearch.DEFAULT_SORT_ASCENDING;
	}
}
