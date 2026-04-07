import {Component, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {BaseManagerDetailComponent} from '../../shared/base-manager-detail.component';
import {SettingItemComponent} from '../../shared/setting-item/setting-item.component';
import {RuleDefinitionAction} from '@core/model/rule-definition-action';
import {RuleDefinitionActionManagerService} from '../../services/manager/rule-definition-action-manager.service';
import {RuleDefinitionActionDialogService} from '../../services/dialogs/rule-definition-action-dialog.service';
import {UsedByComponent} from '../../shared/used-by/used-by.component';

@Component({
	selector: 'app-rule-definition-action-detail',
	standalone: true,
	templateUrl: './rule-definition-action-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent,
		UsedByComponent]
})
export class RuleDefinitionActionDetailComponent extends BaseManagerDetailComponent<RuleDefinitionAction, RuleDefinitionActionManagerService> {
	@Input() override entity!: RuleDefinitionAction;
	@Input() override allEntities: RuleDefinitionAction[] = [];
	@Output() ruleDefinitionActionUpdated = this.entityUpdated;
	@Output() ruleDefinitionActionDeleted = this.entityDeleted;

	@Input() set ruleDefinitionAction(v: RuleDefinitionAction) {this.entity = v;}
	get ruleDefinitionAction(): RuleDefinitionAction {return this.entity;}

	@Input() set allRuleDefinitionActions(v: RuleDefinitionAction[]) {this.allEntities = v;}

	constructor(
		ruleDefinitionActionManager: RuleDefinitionActionManagerService,
		languageService: LanguageService,
		private ruleDefinitionActionDialogService: RuleDefinitionActionDialogService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(ruleDefinitionActionManager, languageService, snackBar);
	}

	protected getEntityId(): string {return this.entity.ruleDefinitionActionId;}

	onEditBasicInfo(): void {
		this.ruleDefinitionActionDialogService.openEditBasicInfoDialog(
			this.projectId,
			this.entity
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditParameters(): void {
		this.ruleDefinitionActionDialogService.openParametersDialog(
			this.entity
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, parameters: result});
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Rule Definition Action',
				message: `Are you sure you want to delete "${this.entity.label}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.entityDeleted.emit(this.entity.ruleDefinitionActionId);
			}
		});
	}
}
