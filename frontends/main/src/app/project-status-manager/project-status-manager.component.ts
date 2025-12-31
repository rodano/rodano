import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MatSelectModule} from '@angular/material/select';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatButtonModule} from '@angular/material/button';
import {ProjectStatus} from '@core/model/project-status';
import {ProjectService} from '@core/services/project.service';

@Component({
	selector: 'app-project-status-manager',
	standalone: true,
	templateUrl: './project-status-manager.component.html',
	styleUrl: './project-status-manager.component.css',
	imports: [
		CommonModule,
		FormsModule,
		MatSelectModule,
		MatFormFieldModule,
		MatButtonModule
	]
})
export class ProjectStatusManagerComponent implements OnInit {
	selectedStatus: ProjectStatus = 'ACTIVE';
	currentProjectId: string | null = null;

	constructor(private projectService: ProjectService) {}

	ngOnInit(): void {
		this.loadCurrentProjectStatus();

		const currentProject = this.projectService.getCurrentProject();
		if(currentProject) {
			this.selectedStatus = currentProject.status || 'ACTIVE';
			this.currentProjectId = currentProject.projectId;
		}
	}

	private loadCurrentProjectStatus(): void {
		this.currentProjectId = this.projectService.getCurrentProjectId();

		if(this.currentProjectId) {
			this.projectService.getProjects().subscribe(projects => {
				const currentProject = projects.find(p => p.projectId === this.currentProjectId);
				if(currentProject) {
					this.selectedStatus = currentProject.status || 'ACTIVE';
				}
			});
		}
	}

	getStatusLabel(status: ProjectStatus): string {
		switch(status) {
			case 'ACTIVE': return 'Active';
			case 'CLOSED': return 'Closed';
			case 'ARCHIVED': return 'Archived';
			default: return status;
		}
	}

	onStatusChange(): void {
		if(!this.currentProjectId) {
			return;
		}

		this.projectService.updateProjectStatus(this.currentProjectId, this.selectedStatus)
			.subscribe({
				next: updatedProject => {
					console.log('Project status updated:', updatedProject);
				},
				error: error => {
					console.error('Error updating project status:', error);
				}
			});
	}
}
