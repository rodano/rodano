import {Injectable} from '@angular/core';
import {CanDeactivate} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {DialogComponent, DialogData} from '../configuration/ui-components/dialog/dialog.component';
import {ConfigurationChartsComponent} from '../configuration/configuration-charts/configuration-charts.component';

@Injectable({
	providedIn: 'root'
})
export class UnsavedChangesGuard implements CanDeactivate<ConfigurationChartsComponent> {
	constructor(private dialog: MatDialog) {}

	canDeactivate(
		component: ConfigurationChartsComponent
	): Observable<boolean> | Promise<boolean> | boolean {
		//If no changes, simply allow navigation
		if(!component.changesMade && !component.isNewChart) {
			return true;
		}

		//Show a dialog
		const data: DialogData = {
			title: 'Unsaved Changes',
			message: 'You have unsaved changes. Do you really want to leave?',
			buttons: [
				{label: 'Continue Editing', icon: '', value: 'continue'},
				{label: 'Discard Changes', icon: '', value: 'discard'},
				{label: 'Save Changes', icon: '', value: 'save'}
			]
		};

		//Return an observable that completes with true or false
		return new Observable<boolean>(observer => {
			this.dialog.open(DialogComponent, {
				data,
				width: '400px',
				disableClose: true
			}).afterClosed().subscribe(result => {
				switch(result) {
					case 'discard':
						component.handleCancel();
						observer.next(true);
						observer.complete();
						break;
					case 'save':
						component.handleSave();
						observer.next(true);
						observer.complete();
						break;
					case 'continue':
					default:
						observer.next(false);
						observer.complete();
						break;
				}
			});
		});
	}
}
