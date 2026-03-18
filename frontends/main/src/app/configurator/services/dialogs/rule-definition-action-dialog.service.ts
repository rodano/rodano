import {Injectable} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {RuleDefinitionAction} from '@core/model/rule-definition-action';
import {
	RuleDefinitionActionBasicInfoDialogComponent, RuleDefinitionActionBasicInfoDialogData
} from '../../dialogs/rule-definition/rule-definition-action-basic-info-dialog/rule-definition-action-basic-info-dialog.component';
import {
	RuleDefinitionActionParameterDialogComponent, RuleDefinitionActionParameterDialogData
} from '../../dialogs/rule-definition/rule-definition-action-parameter-dialog/rule-definition-action-parameter-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class RuleDefinitionActionDialogService {
	constructor(
		private dialog: MatDialog
	) {}

	openCreateDialog(projectId: string): Observable<any> {
		const dialogRef: MatDialogRef<RuleDefinitionActionBasicInfoDialogComponent> = this.dialog.open(
			RuleDefinitionActionBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {
					projectId,
					ruleDefinitionAction: null
				} as RuleDefinitionActionBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openEditBasicInfoDialog(projectId: string, ruleDefinitionAction: RuleDefinitionAction): Observable<any> {
		const dialogRef: MatDialogRef<RuleDefinitionActionBasicInfoDialogComponent> = this.dialog.open(
			RuleDefinitionActionBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {
					projectId,
					ruleDefinitionAction: JSON.parse(JSON.stringify(ruleDefinitionAction))
				} as RuleDefinitionActionBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openParametersDialog(action: RuleDefinitionAction): Observable<any> {
		return this.dialog.open(RuleDefinitionActionParameterDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				parameters: JSON.parse(JSON.stringify(action.parameters ?? []))
			} as RuleDefinitionActionParameterDialogData
		}).afterClosed();
	}
}
