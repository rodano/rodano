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
import {RuleDefinitionProperty} from '@core/model/rule-definition-property';
import {RuleDefinitionPropertyManagerService} from '../../services/manager/rule-definition-property-manager.service';
import {RuleDefinitionPropertyDialogService} from '../../services/dialogs/rule-definition-property-dialog.service';
import {UsedByComponent} from '../../shared/used-by/used-by.component';

@Component({
	selector: 'app-rule-definition-property-detail',
	standalone: true,
	templateUrl: './rule-definition-property-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent,
		UsedByComponent]
})
export class RuleDefinitionPropertyDetailComponent extends BaseManagerDetailComponent<RuleDefinitionProperty, RuleDefinitionPropertyManagerService> {
	@Input() override entity!: RuleDefinitionProperty;
	@Input() override allEntities: RuleDefinitionProperty[] = [];
	@Output() ruleDefinitionPropertyUpdated = this.entityUpdated;
	@Output() ruleDefinitionPropertyDeleted = this.entityDeleted;

	@Input() set ruleDefinitionProperty(v: RuleDefinitionProperty) {this.entity = v;}
	get ruleDefinitionProperty(): RuleDefinitionProperty {return this.entity;}

	@Input() set allRuleDefinitionProperties(v: RuleDefinitionProperty[]) {this.allEntities = v;}

	constructor(
		ruleDefinitionPropertyManager: RuleDefinitionPropertyManagerService,
		languageService: LanguageService,
		private ruleDefinitionPropertyDialogService: RuleDefinitionPropertyDialogService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(ruleDefinitionPropertyManager, languageService, snackBar);
	}

	protected getEntityId(): string {return this.entity.ruleDefinitionPropertyId;}

	onEditBasicInfo(): void {
		this.ruleDefinitionPropertyDialogService.openDialog(
			this.projectId,
			this.entity
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Rule Definition Property',
				message: `Are you sure you want to delete "${this.entity.label}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.entityDeleted.emit(this.entity.ruleDefinitionPropertyId);
			}
		});
	}
}
