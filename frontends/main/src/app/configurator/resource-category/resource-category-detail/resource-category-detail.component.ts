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
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {ResourceCategoryManagerService} from '../../services/manager/resource-category-manager.service';
import {ResourceCategory} from '@core/model/resource-category';
import {ResourceCategoryDialogService} from '../../services/dialogs/resource-category-dialog.service';

@Component({
	selector: 'app-resource-category-detail',
	standalone: true,
	templateUrl: './resource-category-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent]
})
export class ResourceCategoryDetailComponent implements OnInit, OnDestroy {
	@Input() resourceCategory!: ResourceCategory;
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() allResourceCategories: ResourceCategory[] = [];
	@Output() resourceCategoryUpdated = new EventEmitter<ResourceCategory>();
	@Output() resourceCategoryDeleted = new EventEmitter<string>();
	@Output() closed = new EventEmitter<void>();

	selectedLanguage = '';
	projectLanguages: ProjectLanguage[];
	private languageSubscription: Subscription;

	constructor(
		public resourceCategoryManager: ResourceCategoryManagerService,
		public languageService: LanguageService,
		private resourceCategoryDialogService: ResourceCategoryDialogService,
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
		this.resourceCategoryDialogService.openBasicInfoDialog(
			this.projectId,
			this.resourceCategory,
			this.projectLanguages
		).subscribe((result: any) => {
			if(result) {
				const updatedResourceCategory: ResourceCategory = {...this.resourceCategory, ...result};
				this.resourceCategoryManager.update(updatedResourceCategory);
				this.resourceCategoryUpdated.emit(updatedResourceCategory);
				this.showStagedMessage();
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Resource Category',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.resourceCategory.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.resourceCategoryDeleted.emit(this.resourceCategory.categoryId);
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
		return this.resourceCategoryManager.isFieldModified(this.resourceCategory.categoryId, fieldName);
	}
}
