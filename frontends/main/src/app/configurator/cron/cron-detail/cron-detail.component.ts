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
import {Cron} from '@core/model/cron';
import {CronManagerService} from '../../services/manager/cron-manager.service';
import {CronDialogService} from '../../services/dialogs/cron-dialog.service';

@Component({
	selector: 'app-cron-detail',
	standalone: true,
	templateUrl: './cron-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent]
})
export class CronDetailComponent extends BaseManagerDetailComponent<Cron, CronManagerService> {
	@Input() override entity!: Cron;
	@Input() override allEntities: Cron[] = [];
	@Output() cronUpdated = this.entityUpdated;
	@Output() cronDeleted = this.entityDeleted;

	@Input() set cron(v: Cron) {this.entity = v;}
	get cron(): Cron {return this.entity;}

	@Input() set allCrons(v: Cron[]) {this.allEntities = v;}

	constructor(
		cronManager: CronManagerService,
		languageService: LanguageService,
		private cronDialogService: CronDialogService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(cronManager, languageService, snackBar);
	}

	protected getEntityId(): string {return this.entity.cronId;}

	onEditBasicInfo(): void {
		this.cronDialogService.openBasicInfoDialog(
			this.projectId,
			this.entity,
			this.projectLanguages
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
				title: 'Delete cron',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.entity.description)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.entityDeleted.emit(this.entity.cronId);
			}
		});
	}
}
