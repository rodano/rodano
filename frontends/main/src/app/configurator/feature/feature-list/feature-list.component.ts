import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {ProjectLanguage} from '@core/model/project-language';
import {forkJoin, Subscription} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {HttpErrorResponse} from '@angular/common/http';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {Feature} from '@core/model/feature';
import {FeatureManagerService} from '../../services/manager/feature-manager.service';
import {FeatureDialogService} from '../../services/dialogs/feature-dialog.service';
import {MatTooltip} from '@angular/material/tooltip';
import {FeatureDetailComponent} from '../feature-detail/feature-detail.component';

@Component({
	selector: 'app-feature-list',
	standalone: true,
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatProgressSpinnerModule,
		MatSnackBarModule,
		FeatureDetailComponent,
		EmptyStateComponent,
		MatTooltip
	],
	templateUrl: './feature-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class FeatureListComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string | null>();
	@Output() featuresChanged = new EventEmitter<{modificationCount: number}>();
	@Output() featureContextChanged = new EventEmitter<{
		features: any[];
		selectedFeatureId: string | null;
	}>();

	selectedFeature: Feature | null = null;
	viewMode = 'detail';
	loading = false;

	projectLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';
	private languageSubscription: Subscription;

	constructor(
		public featureManager: FeatureManagerService,
		public languageService: LanguageService,
		private featureDialogService: FeatureDialogService,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadFeatures();

		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnChanges(changes: any): void {
		if(changes['selectedNode'] && this.features.length > 0) {
			const nodeId = this.selectedNode;
			if(nodeId?.startsWith('feature-')) {
				const featureId = nodeId.replace('feature-', '');
				const feature = this.features.find(f => f.featureId === featureId);
				if(feature) {
					this.selectedFeature = feature;
				}
			}
			else if(nodeId === 'features') {
				this.selectedFeature = null;
			}
		}
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	get viewLevel(): number {
		if(!this.selectedFeature) {
			return 0;
		}
		return 2;
	}

	get features(): Feature[] {
		return this.featureManager.getAll();
	}

	get modifiedFeatureIds(): Set<string> {
		return this.featureManager.getModifiedIds();
	}

	get originalFeatures(): Feature[] {
		return this.featureManager.getOriginals();
	}

	get totalModificationCount(): number {
		return this.featureManager.getModificationCount();
	}

	loadFeatures(): void {
		this.loading = true;
		forkJoin({
			features: this.featureManager.load(this.projectId)
		}).subscribe({
			next: ({features}) => {
				if(this.selectedFeature) {
					this.selectedFeature = features.find(
						f => f.featureId === this.selectedFeature!.featureId
					) || null;
				}
				this.loading = false;
				this.emitContext();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error loading features:', error);
				this.snackBar.open('Failed to load features', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	onSelectFeature(feature: Feature): void {
		if(this.selectedFeature?.featureId === feature.featureId) {
			this.clearSelection();
		}
		else {
			this.selectFeature(feature);
		}
		this.emitContext();
	}

	clearSelection(): void {
		this.selectedFeature = null;
		this.viewMode = 'detail';
		this.nodeSelected.emit('features');
	}

	private selectFeature(feature: Feature): void {
		const previousFeatureId = this.selectedFeature?.featureId;
		this.selectedFeature = feature;

		if(previousFeatureId !== feature.featureId) {
			this.viewMode = 'detail';
		}
		this.emitContext();
		this.nodeSelected.emit(`feature-${feature.featureId}`);
	}

	isSelected(feature: Feature): boolean {
		return this.selectedFeature?.featureId === feature.featureId;
	}

	onCreateFeature(): void {
		this.featureDialogService.openCreateDialog(
			this.projectId,
			this.projectLanguages
		).subscribe((result: Feature | null) => {
			if(result) {
				this.featureManager.create(this.projectId, result).subscribe({
					next: () => {
						this.snackBar.open('Feature created', 'Close', {duration: 2000});
						this.loadFeatures();
						this.emitModificationChange();
					},
					error: error => {
						console.error('Error creating feature', error);
						this.snackBar.open('Failed to create feature', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onFeatureUpdated(updatedFeature: Feature): void {
		this.selectedFeature = this.featureManager.getById(updatedFeature.featureId) || null;
		this.emitModificationChange();
	}

	onFeatureDeleted(featureId: string): void {
		const feature = this.features.find(f => f.featureId === featureId);
		if(!feature) {
			return;
		}
		this.performDelete(feature);
	}

	private performDelete(feature: Feature): void {
		this.featureManager.delete(this.projectId, feature.featureId).subscribe({
			next: () => {
				this.snackBar.open('Feature deleted', 'Close', {duration: 2000});

				if(this.selectedFeature?.featureId === feature.featureId) {
					this.clearSelection();
				}

				this.loadFeatures();
				this.emitModificationChange();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting feature', error);
				this.snackBar.open('Failed to delete feature', 'Close', {duration: 3000});
			}
		});
	}

	private emitModificationChange(): void {
		this.featuresChanged.emit({modificationCount: this.totalModificationCount});
	}

	private emitContext(): void {
		this.featureContextChanged.emit({
			features: [...this.features],
			selectedFeatureId: this.selectedFeature?.featureId || null
		});
	}

	isModified(featureId: string): boolean {
		return this.featureManager.isModified(featureId);
	}
}
