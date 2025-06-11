import {MailOrigin} from '../../model/mail-origin';
import {MailStatus} from '../../model/mail-status';
import {PaginatedSearch} from './paginated-search';

export class MailSearch extends PaginatedSearch {
	static readonly DEFAULT_SORT_BY = 'creationTime';
	static readonly DEFAULT_SORT_ASCENDING = false;

	origin?: MailOrigin;
	status?: MailStatus;
	intent?: string;
	sender?: string;
	recipient?: string;
	fullText?: string;
	beforeDate?: string;
	afterDate?: string;

	constructor() {
		super();
		this.sortBy = MailSearch.DEFAULT_SORT_BY;
		this.orderAscending = MailSearch.DEFAULT_SORT_ASCENDING;
		this.pageSize = 20;
	}
}
