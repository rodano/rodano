import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {forkJoin} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {HttpErrorResponse} from '@angular/common/http';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {Feature} from '@core/model/feature';
import {FeatureManagerService} from '../../services/manager/feature-manager.service';
import {FeatureDialogService} from '../../services/dialogs/feature-dialog.service';
import {MatTooltip} from '@angular/material/tooltip';
import {FeatureDetailComponent} from '../feature-detail/feature-detail.component';
import {BaseListComponent} from '../../shared/base-list.component';
import {ListHeaderComponent} from '../../shared/list-header/list-header.component';
import {ModifiedDirective} from '../../shared/modified.directive';

@Component({
	selector: 'app-feature-list',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule,
		MatSnackBarModule, FeatureDetailComponent, EmptyStateComponent, MatTooltip, ListHeaderComponent, ModifiedDirective],
	templateUrl: './feature-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class FeatureListComponent
	extends BaseListComponent<Feature>
	implements OnInit, OnChanges, OnDestroy {
	@Input() override projectId = '';
	@Input() override project: ConfiguratorProject | null = null;
	@Input() override selectedNode: string | null = null;
	@Output() featuresChanged = new EventEmitter<{modificationCount: number}>();
	@Output() featureContextChanged = new EventEmitter<{
		features: any[];
		selectedFeatureId: string | null;
	}>();

	constructor(
		public featureManager: FeatureManagerService,
		public override languageService: LanguageService,
		private featureDialogService: FeatureDialogService,
		snackBar: MatSnackBar
	) {
		super(featureManager, languageService, snackBar);
	}

	getEntityId(f: Feature): string {return f.featureId;}
	getNodePrefix(): string {return 'feature';}
	getListNodeName(): string {return 'features';}

	get features(): Feature[] {return this.featureManager.getAll();}
	get selectedFeature(): Feature | null {return this.selected as Feature | null;}
	get modifiedFeatureIds(): Set<string> {return this.featureManager.getModifiedIds();}
	get originalFeatures(): Feature[] {return this.featureManager.getOriginals();}

	loadFeatures(): void {this.load();}
	load(): void {
		this.loading = true;
		forkJoin({
			features: this.featureManager.load(this.projectId)
		}).subscribe({
			next: ({features}) => this.afterLoad(features),
			error: (e: HttpErrorResponse) => this.handleLoadError(e, 'features')
		});
	}

	emitChangedEvent(count: number): void {
		this.featuresChanged.emit({modificationCount: count});
	}

	emitContextEvent(): void {
		this.featureContextChanged.emit({
			features: [...this.features],
			selectedFeatureId: this.selected?.featureId || null
		});
	}

	onCreate(): void {
		this.featureDialogService.openCreateDialog(this.projectId, this.projectLanguages)
			.subscribe((result: Feature | null) => {
				if(result) {
					this.featureManager.create(this.projectId, result).subscribe({
						next: () => this.afterCreate('Feature'),
						error: e => {
							console.error(e);
							this.snackBar.open('Failed to create feature', 'Close', {duration: 3000});
						}
					});
				}
			});
	}

	onUpdated(updated: Feature): void {
		this.selected = this.featureManager.getById(updated.featureId) || null;
		this.emitModificationChange();
	}

	onDeleted(featureId: string): void {
		const feature = this.features.find(f => f.featureId === featureId);
		if(!feature) {
			return;
		}
		this.featureManager.delete(this.projectId, featureId).subscribe({
			next: () => this.afterDelete(feature, 'Feature'),
			error: (e: HttpErrorResponse) => {
				console.error(e);
				this.snackBar.open('Failed to delete feature', 'Close', {duration: 3000});
			}
		});
	}

	onSelectFeature(f: Feature): void {this.onSelect(f);}
	onCreateFeature(): void {this.onCreate();}
	onFeatureUpdated(f: Feature): void {this.onUpdated(f);}
	onFeatureDeleted(id: string): void {this.onDeleted(id);}
}
