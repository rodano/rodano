import {Service, inject} from '@angular/core';
import {CanDeactivate} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {map, Observable, of} from 'rxjs';
import {UnsavedChangesComponent} from '../../dialogs/unsaved-changes/unsaved-changes.component';
import {FormComponent} from '../../form/form.component';

@Service()
export class UnsavedChangesGuard implements CanDeactivate<FormComponent> {
	private readonly dialog = inject(MatDialog);

	canDeactivate(
		component: FormComponent
	): Observable<boolean> {
		if(!component.dirty()) {
			return of(true);
		}
		return this.dialog
			.open<UnsavedChangesComponent, any, boolean>(UnsavedChangesComponent)
			.afterClosed()
			.pipe(
				map(result => result ?? false)
			);
	}
}
