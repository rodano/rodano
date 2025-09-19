import {Pipe, PipeTransform} from '@angular/core';
import {ScopeMini} from '@core/model/scope-mini';
import {Scope} from '@core/model/scope';
import {ScopeTiny} from '@core/model/scope-tiny';

@Pipe({
	name: 'scopeCodeShortname'
})
export class ScopeCodeShortnamePipe implements PipeTransform {
	transform(scope: Scope | ScopeMini | ScopeTiny): string {
		if(scope.code === scope.shortname) {
			return scope.code;
		}
		return `${scope.code} (${scope.shortname})`;
	}
}
