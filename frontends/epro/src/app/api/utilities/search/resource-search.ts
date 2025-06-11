import { PaginatedSearch } from './paginated-search';

export class ResourceSearch extends PaginatedSearch {
	categoryId?: string;
	fullText?: string;
	removed?: boolean;

	reset(): void{
		this.categoryId = '';
		this.fullText = '';
		super.reset();
	}
}
