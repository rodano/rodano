import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {MatIcon} from '@angular/material/icon';
import {ProjectSettingsDetailComponent} from './project-settings-detail/project-settings-detail.component';

@Component({
	selector: 'app-configurator-detail',
	standalone: true,
	templateUrl: './configurator-detail.component.html',
	styleUrls: ['./configurator-detail.component.css'],
	imports: [
		CommonModule,
		MatIcon,
		ProjectSettingsDetailComponent
	]
})
export class ConfiguratorDetailComponent implements OnChanges {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Input() modifiedFields = new Set<string>();
	@Output() fieldsUpdated = new EventEmitter<Partial<ConfiguratorProject>>();

	selectedNodeType: 'project-settings' | 'scope-model' | 'menu' | 'section' | null = null;

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['selectedNode']) {
			this.determineNodeType();
		}
	}

	private determineNodeType(): void {
		if(!this.selectedNode) {
			this.selectedNodeType = null;
			return;
		}

		if(this.selectedNode === 'project-settings') {
			this.selectedNodeType = 'project-settings';
		}
		else if(this.selectedNode.startsWith('scope-model-')) {
			this.selectedNodeType = 'scope-model';
		}
		else if(this.selectedNode.startsWith('menu-')) {
			this.selectedNodeType = 'menu';
		}
		else if(this.selectedNode.startsWith('section-')) {
			this.selectedNodeType = 'section';
		}
	}
}
