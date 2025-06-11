import {booleanAttribute, Component, EventEmitter, Input, Output} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {ScopeDTO} from '@core/model/scope-dto';
import {ScopeMiniDTO} from '@core/model/scope-mini-dto';
import {ScopeTinyDTO} from '@core/model/scope-tiny-dto';
import {ScopeCodeShortnamePipe} from 'src/app/pipes/scope-code-shortname.pipe';

@Component({
	selector: 'app-scope-breadcrumb',
	templateUrl: './scope-breadcrumb.component.html',
	styleUrls: ['./scope-breadcrumb.component.css'],
	imports: [MatButton, ScopeCodeShortnamePipe]
})
export class ScopeBreadcrumbComponent<T extends (ScopeDTO | ScopeMiniDTO | ScopeTinyDTO)> {
	@Input() scopes: T[];
	@Input({transform: booleanAttribute}) disableDeepest = false;
	@Output() scopeChange = new EventEmitter<T>();

	selectScope(scope: T) {
		this.scopeChange.emit(scope);
	}
}
