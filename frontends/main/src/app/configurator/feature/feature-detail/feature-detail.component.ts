import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {Feature} from '@core/model/feature';
import {FeatureManagerService} from '../../services/manager/feature-manager.service';
import {FeatureDialogService} from '../../services/dialogs/feature-dialog.service';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {BaseManagerDetailComponent} from '../../shared/base-manager-detail.component';

@Component({
	selector: 'app-feature-detail',
	standalone: true,
	templateUrl: './feature-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent]
})
export class FeatureDetailComponent extends BaseManagerDetailComponent<Feature, FeatureManagerService> {
	@Input() override entity!: Feature;
	@Input() override allEntities: Feature[] = [];
	@Output() featureUpdated = this.entityUpdated;
	@Output() featureDeleted = this.entityDeleted;

	@Input() set feature(v: Feature) {this.entity = v;}
	get feature(): Feature {return this.entity;}

	@Input() set allFeatures(v: Feature[]) {this.allEntities = v;}

	constructor(
		featureManager: FeatureManagerService,
		languageService: LanguageService,
		private featureDialogService: FeatureDialogService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(featureManager, languageService, snackBar);
	}

	protected getEntityId(): string {return this.entity.featureId;}

	onEditBasicInfo(): void {
		this.featureDialogService.openBasicInfoDialog(
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
				title: 'Delete Feature',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.entity.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.entityDeleted.emit(this.entity.featureId);
			}
		});
	}
}
