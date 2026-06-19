import {Component, effect, inject, signal} from '@angular/core';
import {UserSearch} from '@core/utilities/search/user-search';
import {UserListComponent} from '../../user/user-list/user-list.component';
import {SCOPE_TOKEN} from '../home/scope.component';

@Component({
	templateUrl: './scope-users.component.html',
	imports: [UserListComponent]
})
export class ScopeUsersComponent {
	readonly scope = inject(SCOPE_TOKEN);

	readonly predicate = signal<UserSearch>(new UserSearch());

	constructor() {
		effect(() => {
			const predicate = new UserSearch();
			predicate.scopePks = [this.scope().pk];
			this.predicate.set(predicate);
		});
	}
}
