import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {HttpErrorResponse} from '@angular/common/http';
import {forkJoin, of, Subscription} from 'rxjs';

import {ConfiguratorProject} from '@core/model/configurator-project';
import {ProjectLanguage} from '@core/model/project-language';
import {TimelineGraph} from '@core/model/timeline-graph';
import {TimelineGraphSection} from '@core/model/timeline-graph-section';

import {TimelineGraphManagerService} from '../../services/manager/timeline-graph-manager.service';
import {TimelineGraphSectionManagerService} from '../../services/manager/timeline-graph-section-manager.service';
import {LanguageService} from '../../services/language.service';
import {MatSnackBar} from '@angular/material/snack-bar';

import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {ListHeaderComponent} from '../../shared/list-header/list-header.component';
import {ModifiedDirective} from '../../shared/modified.directive';
import {ScopeModelManagerService} from '../../services/manager/scope-model-manager.service';
import {EventModelManagerService} from '../../services/manager/event-model-manager.service';
import {TimelineGraphDialogService} from '../../services/dialogs/timeline-graph-dialog.service';
import {TimelineGraphDetailComponent} from '../timeline-graph-detail/timeline-graph-detail.component';
import {TimelineGraphSectionDialogService} from '../../services/dialogs/timeline-graph-section-dialog.service';
import {
	TimelineGraphSectionDetailComponent
} from '../timeline-graph-section-detail/timeline-graph-section-detail.component';
import {FieldModelManagerService} from '../../services/manager/field-model-manager.service';
import {
	TimelineGraphGrantsMatrixComponent
} from '../timeline-graph-grants-matrix/timeline-graph-grants-matrix.component';
import {Profile} from '@core/model/profile';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';

type ViewMode = 'graph-detail' | 'section-list' | 'section-detail';

@Component({
	selector: 'app-timeline-graph-list',
	standalone: true,
	templateUrl: './timeline-graph-list.component.html',
	styleUrls: ['../../shared/list-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, MatProgressSpinnerModule,
		TimelineGraphDetailComponent, TimelineGraphSectionDetailComponent, EmptyStateComponent, ListHeaderComponent,
		ModifiedDirective, TimelineGraphGrantsMatrixComponent]
})
export class TimelineGraphListComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string>();
	@Output() timelineGraphChanged = new EventEmitter<boolean>();
	@Output() timelineGraphContextChanged = new EventEmitter<{
		timelineGraphs: TimelineGraph[];
		sections: TimelineGraphSection[];
		selectedTimelineGraphId: string | null;
		selectedGraphSectionId: string | null;
	}>();

	showMatrix = false;

	selectedTimelineGraph: TimelineGraph | null = null;
	selectedGraphSectionId: string | null = null;
	viewMode: ViewMode = 'graph-detail';
	loading = false;

	projectLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';
	private languageSubscription: Subscription;

	private currentSections: TimelineGraphSection[] = [];

	constructor(
		public timelineGraphManager: TimelineGraphManagerService,
		public timelineGraphSectionManager: TimelineGraphSectionManagerService,
		public languageService: LanguageService,
		private scopeModelManager: ScopeModelManagerService,
		private eventModelManager: EventModelManagerService,
		private fieldModelManager: FieldModelManagerService,
		private profileManager: ProfileManagerService,
		private timelineGraphDialogService: TimelineGraphDialogService,
		private timelineGraphSectionDialogService: TimelineGraphSectionDialogService,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.projectLanguages = this.project?.languages?.length
			? this.project.languages
			: this.languageService.projectLanguages;

		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});

		this.loadTimelineGraphs();
	}

	ngOnChanges(changes: any): void {
		if(changes['selectedNode'] && (this.timelineGraphs?.length ?? 0) > 0) {
			const nodeId = this.selectedNode;

			if(nodeId?.startsWith('timeline-graph-')) {
				const graph = this.timelineGraphs.find(
					tg => tg.timelineGraphId === nodeId?.replace('timeline-graph-', '')
				);
				if(graph) {
					this.selectedTimelineGraph = graph;
					this.updateFilteredSections();
				}
			}
			else if(nodeId === 'timeline-graphs') {
				this.selectedTimelineGraph = null;
				this.currentSections = [];
			}
		}
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	selectById(id: string): void {
		const entity = this.timelineGraphs.find(tg => tg.timelineGraphId === id);
		if(entity) {
			this.selectTimelineGraph(entity);
		}
	}

	selectSectionById(id: string): void {
		this.viewMode = 'section-list';
		this.updateFilteredSections();
		if(this.currentSections.some(tgs => tgs.graphSectionId === id)) {
			this.selectedGraphSectionId = id;
			this.viewMode = 'section-detail';
			this.nodeSelected.emit(`timeline-graph-model-${id}`);
			this.emitContext();
		}
	}

	get viewLevel(): number {
		if(!this.selectedTimelineGraph) {
			return 0;
		}
		if(this.viewMode === 'graph-detail' || this.viewMode === 'section-list') {
			return 2;
		}
		if(this.viewMode === 'section-detail') {
			return 3;
		}
		return 0;
	}

	get timelineGraphs(): TimelineGraph[] {return this.timelineGraphManager.getAll();}
	get sections(): TimelineGraphSection[] {return this.currentSections;}

	get modifiedTimelineGraphIds(): Set<string> {return this.timelineGraphManager.getModifiedIds();}
	get modifiedSectionIds(): Set<string> {return this.timelineGraphSectionManager.getModifiedIds();}

	get originalTimelineGraphs(): TimelineGraph[] {return this.timelineGraphManager.getOriginals();}
	get originalSections(): TimelineGraphSection[] {return this.timelineGraphSectionManager.getOriginals();}

	get totalModificationCount(): number {
		return this.timelineGraphManager.getModificationCount() + this.timelineGraphSectionManager.getModificationCount();
	}

	get profiles(): Profile[] {return this.profileManager.getAll();}

	loadTimelineGraphs(): void {
		this.loading = true;

		const eventModels$ = this.eventModelManager.isLoaded()
			? of(this.eventModelManager.getAll())
			: this.eventModelManager.load(this.projectId);

		const fieldModels$ = this.fieldModelManager.isLoaded()
			? of(this.fieldModelManager.getAll())
			: this.fieldModelManager.load(this.projectId);

		forkJoin({
			timelineGraphs: this.timelineGraphManager.load(this.projectId),
			eventModels: eventModels$,
			fieldModels: fieldModels$
		}).subscribe({
			next: ({timelineGraphs}) => {
				if(this.selectedTimelineGraph) {
					this.selectedTimelineGraph = timelineGraphs.find(
						tg => tg.timelineGraphId === this.selectedTimelineGraph!.timelineGraphId
					) || null;
					this.loadSectionsForCurrentGraph();
				}
				else {
					this.loading = false;
				}
				this.emitContext();
			},
			error: error => {
				console.error('Error loading timeline graphs:', error);
				this.snackBar.open('Failed to load timeline graphs', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	private loadSectionsForCurrentGraph(): void {
		if(!this.selectedTimelineGraph) {
			this.currentSections = [];
			this.loading = false;
			return;
		}

		this.timelineGraphSectionManager.loadForGraph(this.projectId, this.selectedTimelineGraph.timelineGraphId).subscribe({
			next: sections => {
				this.currentSections = sections;
				this.loading = false;
				this.emitContext();
			},
			error: error => {
				console.error('Error loading sections:', error);
				this.snackBar.open('Failed to load sections', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	private updateFilteredSections(): void {
		if(!this.selectedTimelineGraph) {
			this.currentSections = [];
			return;
		}
		this.currentSections = this.timelineGraphSectionManager.getAll();
	}

	onSelectTimelineGraph(graph: TimelineGraph): void {
		if(this.selectedTimelineGraph?.timelineGraphId === graph.timelineGraphId) {
			this.clearSelection();
		}
		else {
			this.selectTimelineGraph(graph);
		}
		this.emitContext();
	}

	private selectTimelineGraph(graph: TimelineGraph): void {
		const previousId = this.selectedTimelineGraph?.timelineGraphId;
		this.selectedTimelineGraph = graph;
		this.selectedGraphSectionId = null;

		if(previousId !== graph.timelineGraphId) {
			this.viewMode = 'graph-detail';
			this.loadSectionsForCurrentGraph();
		}

		this.nodeSelected.emit(`timeline-graph-${graph.timelineGraphId}`);
		this.emitContext();
	}

	clearSelection(): void {
		this.selectedTimelineGraph = null;
		this.selectedGraphSectionId = null;
		this.currentSections = [];
		this.viewMode = 'graph-detail';
		this.nodeSelected.emit('timeline-graphs');
	}

	isSelected(graph: TimelineGraph): boolean {
		return this.selectedTimelineGraph?.timelineGraphId === graph.timelineGraphId;
	}

	onCreateTimelineGraph(): void {
		this.timelineGraphDialogService.openCreateDialog(
			this.projectId,
			this.projectLanguages
		).subscribe((result: TimelineGraph | null) => {
			if(result) {
				this.timelineGraphManager.create(this.projectId, result).subscribe({
					next: () => {
						this.snackBar.open('Timeline graph created', 'Close', {duration: 2000});
						this.loadTimelineGraphs();
						this.emitModificationChange();
					},
					error: error => {
						console.error('Error creating timeline graph:', error);
						this.snackBar.open('Failed to create timeline graph', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onTimelineGraphUpdated(updated: TimelineGraph): void {
		this.selectedTimelineGraph = this.timelineGraphManager.getById(updated.timelineGraphId) || null;
		this.emitModificationChange();
	}

	onTimelineGraphDeleted(timelineGraphId: string): void {
		const graph = this.timelineGraphs.find(tg => tg.timelineGraphId === timelineGraphId);
		if(!graph) {
			return;
		}
		this.timelineGraphManager.delete(this.projectId, timelineGraphId).subscribe({
			next: () => {
				this.snackBar.open('Timeline graph deleted', 'Close', {duration: 2000});
				if(this.selectedTimelineGraph?.timelineGraphId === timelineGraphId) {
					this.clearSelection();
				}
				this.loadTimelineGraphs();
				this.emitModificationChange();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting timeline graph:', error);
				this.snackBar.open('Failed to delete timeline graph', 'Close', {duration: 3000});
			}
		});
	}

	switchToSectionView(): void {
		if(!this.selectedTimelineGraph) {
			return;
		}
		this.viewMode = 'section-list';
		this.emitContext();
	}

	backToGraphDetail(): void {
		this.viewMode = 'graph-detail';
		this.selectedGraphSectionId = null;
		this.emitContext();
	}

	onSelectSection(sectionId: string): void {
		if(this.selectedGraphSectionId === sectionId) {
			this.selectedGraphSectionId = null;
			this.viewMode = 'section-list';
		}
		else {
			this.selectedGraphSectionId = sectionId;
			this.viewMode = 'section-detail';
			this.nodeSelected.emit(`timeline-graph-section-${sectionId}`);
		}
		this.emitContext();
	}

	onCreateSection(): void {
		if(!this.selectedTimelineGraph) {
			return;
		}

		this.timelineGraphSectionDialogService.openCreateDialog(
			this.projectId,
			this.selectedTimelineGraph.timelineGraphId,
			this.projectLanguages
		).subscribe((result: TimelineGraphSection | null) => {
			if(result) {
				this.timelineGraphSectionManager.create(this.projectId, result).subscribe({
					next: () => {
						this.snackBar.open('Section created', 'Close', {duration: 2000});
						this.loadSectionsForCurrentGraph();
						this.emitModificationChange();
					},
					error: error => {
						console.error('Error creating section:', error);
						this.snackBar.open('Failed to create section', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onSectionUpdated(updated: TimelineGraphSection): void {
		this.timelineGraphSectionManager.update(updated);
		this.updateFilteredSections();
		this.emitModificationChange();
		this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
	}

	onSectionDeleted(sectionId: string): void {
		this.timelineGraphSectionManager.delete(this.projectId, sectionId).subscribe({
			next: () => {
				this.snackBar.open('Section deleted', 'Close', {duration: 2000});

				if(this.selectedGraphSectionId === sectionId) {
					this.selectedGraphSectionId = null;
					this.viewMode = 'section-list';
				}

				this.updateFilteredSections();
				this.emitModificationChange();
				this.emitContext();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting section:', error);
				this.snackBar.open('Failed to delete section', 'Close', {duration: 3000});
			}
		});
	}

	isSectionModified(sectionId: string): boolean {
		return this.timelineGraphSectionManager.isModified(sectionId);
	}

	getSectionCount(): number {
		return this.currentSections.length;
	}

	private emitModificationChange(): void {
		this.timelineGraphChanged.emit(this.totalModificationCount > 0);
	}

	private emitContext(): void {
		this.timelineGraphContextChanged.emit({
			timelineGraphs: [...this.timelineGraphs],
			sections: this.currentSections,
			selectedTimelineGraphId: this.selectedTimelineGraph?.timelineGraphId || null,
			selectedGraphSectionId: this.selectedGraphSectionId
		});
	}

	getScopeModelLabel(scopeModelId: string): string {
		return this.languageService.getLabelById(scopeModelId, id => this.scopeModelManager.getById(id));
	}

	getEventModelLabel(eventModelId: string): string {
		return this.languageService.getLabelById(eventModelId, id => this.eventModelManager.getById(id));
	}

	getTypeLabel(type: string): string {
		const typeMap: Record<string, string> = {
			ACTION: 'Action',
			PERIOD: 'Period',
			DATE: 'Date',
			LINE: 'Line',
			DOT: 'Dot',
			BAR: 'Bar'
		};
		return typeMap[type] || type;
	}

	onToggleMatrix(): void {
		this.showMatrix = !this.showMatrix;
		if(this.showMatrix) {
			this.selectedTimelineGraph = null;
		}
	}
}
