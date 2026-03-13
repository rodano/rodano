import {ChangeDetectionStrategy, booleanAttribute, Component, input, output} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {Scope} from '@core/model/scope';
import {ScopeMini} from '@core/model/scope-mini';
import {ScopeTiny} from '@core/model/scope-tiny';
import {ScopeCodeShortnamePipe} from 'src/app/pipes/scope-code-shortname.pipe';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	selector: 'app-scope-breadcrumb',
	templateUrl: './scope-breadcrumb.component.html',
	styleUrls: ['./scope-breadcrumb.component.css'],
	imports: [MatButton, ScopeCodeShortnamePipe]
})
export class ScopeBreadcrumbComponent<T extends (Scope | ScopeMini | ScopeTiny)> {
	readonly scopes = input.required<T[]>();
	readonly disableDeepest = input(false, {transform: booleanAttribute});
	readonly scopeChange = output<T>();

	selectScope(scope: T) {
		this.scopeChange.emit(scope);
	}
}
