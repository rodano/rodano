import {Injectable} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {
	RuleDefinitionPropertyDialogComponent, RuleDefinitionPropertyDialogData
} from '../../dialogs/rule-definition/rule-definition-property-dialog/rule-definition-property-dialog.component';
import {RuleDefinitionProperty} from '@core/model/rule-definition-property';

@Injectable({
	providedIn: 'root'
})
export class RuleDefinitionPropertyDialogService {
	constructor(
		private dialog: MatDialog
	) {}

	openCreateDialog(projectId: string): Observable<any> {
		const dialogRef: MatDialogRef<RuleDefinitionPropertyDialogComponent> = this.dialog.open(
			RuleDefinitionPropertyDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {
					projectId,
					ruleDefinitionProperty: null
				} as RuleDefinitionPropertyDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openDialog(projectId: string, ruleDefinitionProperty: RuleDefinitionProperty): Observable<any> {
		const dialogRef: MatDialogRef<RuleDefinitionPropertyDialogComponent> = this.dialog.open(
			RuleDefinitionPropertyDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {
					projectId,
					ruleDefinitionProperty: JSON.parse(JSON.stringify(ruleDefinitionProperty))
				} as RuleDefinitionPropertyDialogData
			}
		);
		return dialogRef.afterClosed();
	}
}
