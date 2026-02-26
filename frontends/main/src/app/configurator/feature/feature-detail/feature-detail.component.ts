import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {Subscription} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {ProjectLanguage} from '@core/model/project-language';
import {Feature} from '@core/model/feature';
import {FeatureManagerService} from '../../services/manager/feature-manager.service';
import {FeatureDialogService} from '../../services/dialogs/feature-dialog.service';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';

@Component({
	selector: 'app-feature-detail',
	standalone: true,
	templateUrl: './feature-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent]
})
export class FeatureDetailComponent implements OnInit, OnDestroy {
	@Input() feature!: Feature;
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() allFeatures: Feature[] = [];
	@Output() featureUpdated = new EventEmitter<Feature>();
	@Output() featureDeleted = new EventEmitter<string>();
	@Output() closed = new EventEmitter<void>();

	selectedLanguage = '';
	projectLanguages: ProjectLanguage[];
	private languageSubscription: Subscription;

	constructor(
		public featureManager: FeatureManagerService,
		public languageService: LanguageService,
		private featureDialogService: FeatureDialogService,
		private dialog: MatDialog,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	onEditBasicInfo(): void {
		this.featureDialogService.openBasicInfoDialog(
			this.projectId,
			this.feature,
			this.projectLanguages
		).subscribe((result: any) => {
			if(result) {
				const updatedFeature: Feature = {...this.feature, ...result};
				this.featureManager.update(updatedFeature);
				this.featureUpdated.emit(updatedFeature);
				this.showStagedMessage();
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Feature',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.feature.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.featureDeleted.emit(this.feature.featureId);
			}
		});
	}

	onClose(): void {
		this.closed.emit();
	}

	private showStagedMessage(): void {
		this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
	}

	isFieldModified(fieldName: string): boolean {
		return this.featureManager.isFieldModified(this.feature.featureId, fieldName);
	}
}
