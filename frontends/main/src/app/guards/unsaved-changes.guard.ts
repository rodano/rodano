import {Observable} from 'rxjs';
import {Injectable} from '@angular/core';
import {CanDeactivate} from '@angular/router';

export interface ComponentCanDeactivate {
	canDeactivate: () => boolean | Observable<boolean>;
}

@Injectable({
	providedIn: 'root'
})
export class UnsavedChangesGuard implements CanDeactivate<ComponentCanDeactivate> {
	canDeactivate(component: ComponentCanDeactivate): boolean | Observable<boolean> {
		return component.canDeactivate ? component.canDeactivate() : true;
	}
}
