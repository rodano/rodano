import { PaginatedSearch } from './paginated-search';

export class ScopeSearch extends PaginatedSearch {
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

	reset() {
		super.reset();
		this.scopeModelId = undefined;
		this.fullText = undefined;
		this.code = undefined;
		this.ids = [];
		this.pks = [];
		this.parentPks = [];
		this.ancestorPks = [];
		this.workflowStates = {};
		this.fieldModelCriteria = undefined;
		this.leaf = undefined;
	}
}
