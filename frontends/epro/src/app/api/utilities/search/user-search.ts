import { RoleStatus } from '../../model/role-status-dto';
import { PaginatedSearch } from './paginated-search';

export class UserSearch extends PaginatedSearch {
	scopePks: number[] = [];
	profileIds: string[] = [];
	states: RoleStatus[] = [];
	email?: string;
	fullText?: string;
	enabled?: boolean;
	externallyManaged?: boolean;

	reset() {
		super.reset();

		this.scopePks = [];
		this.profileIds = [];
		this.states = [];
		this.email = undefined;
		this.fullText = undefined;
		this.enabled = undefined;
		this.externallyManaged = undefined;
	}

	resetPagination() {
		super.reset();
	}
}
