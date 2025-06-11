import {Component, Input, OnChanges} from '@angular/core';
import {UserSearch} from '@core/utilities/search/user-search';
import {ScopeDTO} from '@core/model/scope-dto';
import {UserListComponent} from 'src/app/user/user-list/user-list.component';

@Component({
	templateUrl: './scope-users.component.html',
	imports: [UserListComponent]
})
export class ScopeUsersComponent implements OnChanges {
	@Input() scope: ScopeDTO;

	predicate = new UserSearch();

	ngOnChanges() {
		this.predicate.scopePks = [this.scope.pk];
	}
}
