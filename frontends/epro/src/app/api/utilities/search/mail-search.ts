import { MailOrigin } from '../../enums/mail-origin';
import { PaginatedSearch } from './paginated-search';

export class MailSearch extends PaginatedSearch {
	origin?: MailOrigin;
	status?: string;
	intent?: string;
	sender?: string;
	recipient?: string;
	fullText?: string;
	beforeDate?: string;
	afterDate?: string;

	reset(): void{
		super.reset();

		this.origin = undefined;
		this.status = undefined;
		this.intent = undefined;
		this.sender = undefined;
		this.recipient = undefined;
		this.fullText = undefined;
		this.beforeDate = undefined;
		this.afterDate = undefined;
	}
}
