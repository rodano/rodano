import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {MatIcon} from '@angular/material/icon';
import {ProjectSettingsDetailComponent} from './project-settings-detail/project-settings-detail.component';
import {ScopeModelsListComponent} from '../scope-model/scope-model-list/scope-models-list.component';

@Component({
	selector: 'app-configurator-detail',
	standalone: true,
	templateUrl: './configurator-detail.component.html',
	styleUrls: ['./configurator-detail.component.css'],
	imports: [
		CommonModule,
		MatIcon,
		ProjectSettingsDetailComponent,
		ScopeModelsListComponent
	]
})
export class ConfiguratorDetailComponent implements OnChanges {
	@ViewChild(ScopeModelsListComponent) scopeModelsListComponent?: ScopeModelsListComponent;

	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Input() modifiedFields = new Set<string>();
	@Output() fieldsUpdated = new EventEmitter<Partial<ConfiguratorProject>>();
	@Output() nodeSelected = new EventEmitter<string | null>();
	@Output() scopeModelsChanged = new EventEmitter<{modificationCount: number}>();
	@Output() modificationCountChanged = new EventEmitter<number>();
	@Output() scopeModelContextChanged = new EventEmitter<{
		eventModels: any[];
		eventGroups: any[];
		selectedScopeModelId: string | null;
		selectedEventModelId: string | null;
		selectedEventGroupId: string | null;
	}>();

	selectedNodeType: 'project-settings' | 'scope-models' | 'overview' | null = null;

	scopeModelModificationCount = 0;

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['selectedNode']) {
			this.determineNodeType();
		}
	}

	onScopeModelSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onScopeModelsChanged(event: {modificationCount: number}): void {
		this.scopeModelModificationCount = event.modificationCount;
		this.scopeModelsChanged.emit(event);
		this.modificationCountChanged.emit(event.modificationCount);
	}

	private determineNodeType(): void {
		if(!this.selectedNode) {
			this.selectedNodeType = 'overview';
			return;
		}

		if(this.selectedNode.startsWith('scope-model-') || this.selectedNode.startsWith('event-model-') || this.selectedNode.startsWith('event-group-')) {
			this.selectedNodeType = 'scope-models';
			return;
		}

		if(this.selectedNode === 'project-settings') {
			this.selectedNodeType = 'project-settings';
			return;
		}

		if(this.selectedNode === 'scope-models') {
			this.selectedNodeType = 'scope-models';
			return;
		}

		this.selectedNodeType = 'overview';
	}

	onScopeModelContextChanged(context: {
		eventModels: any[];
		eventGroups: any[];
		selectedScopeModelId: string | null;
		selectedEventModelId: string | null;
		selectedEventGroupId: string | null;
	}): void {
		this.scopeModelContextChanged.emit(context);
	}
}
