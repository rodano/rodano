import { PaginatedSearch } from './paginated-search';

export class RobotSearch extends PaginatedSearch {
	profileId?: string;
	name?: string;

	reset() {
		super.reset();

		this.profileId = undefined;
		this.name = undefined;
	}

}
