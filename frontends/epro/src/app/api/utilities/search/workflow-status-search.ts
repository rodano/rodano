import { PaginatedSearch } from './paginated-search';

export class WorkflowStatusSearch extends PaginatedSearch {
	workflowIds?: string[];
	ancestorScopePks?: number[];
	scopePks?: number[];
	eventPks?: number[];
	fullText?: string;
	filterExpectedEvents?: boolean;
	
	reset(): void {
		super.reset();
		this.workflowIds = [];
		this.ancestorScopePks = [];
		this.scopePks = [];
		this.eventPks = [];
		this.fullText = undefined;
		this.filterExpectedEvents = undefined;
	}
}
