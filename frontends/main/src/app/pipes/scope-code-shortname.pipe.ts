import {Pipe, PipeTransform} from '@angular/core';
import {ScopeMiniDTO} from '@core/model/scope-mini-dto';
import {ScopeDTO} from '@core/model/scope-dto';
import {ScopeTinyDTO} from '@core/model/scope-tiny-dto';

@Pipe({
	name: 'scopeCodeShortname'
})
export class ScopeCodeShortnamePipe implements PipeTransform {
	transform(scope: ScopeDTO | ScopeMiniDTO | ScopeTinyDTO): string {
		if(scope.code === scope.shortname) {
			return scope.code;
		}
		return `${scope.code} (${scope.shortname})`;
	}
}
