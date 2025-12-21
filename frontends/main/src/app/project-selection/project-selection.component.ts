import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Project, ProjectService} from '@core/services/project.service';
import {Router} from '@angular/router';
import {PublicStudy} from '@core/model/public-study';
import {ConfigurationService} from '@core/services/configuration.service';
import {AuthStateService} from '../services/auth-state.service';
import {MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatTooltip} from '@angular/material/tooltip';
import {LogoComponent} from '../logo/logo.component';

@Component({
	selector: 'app-project-selection',
	standalone: true,
	templateUrl: './project-selection.component.html',
	styleUrl: './project-selection.component.css',
	imports: [
		CommonModule,
		MatIcon,
		MatIconButton,
		MatTooltip,
		LogoComponent
	]
})
export class ProjectSelectionComponent implements OnInit {
	projects: Project[] = [];
	loading = false;
	selectingProject = false;

	constructor(
		private projectService: ProjectService,
		private router: Router,
		private configurationService: ConfigurationService,
		private authStateService: AuthStateService
	) {}

	ngOnInit(): void {
		document.documentElement.style.removeProperty('--mat-sys-primary');
		this.loadProjects();
	}

	loadProjects(): void {
		this.loading = true;
		this.projectService.getProjects().subscribe({
			next: (projects: Project[]) => {
				this.projects = projects;
				this.loading = false;
			},
			error: (error: any) => {
				console.error('Error loading projects:', error);
				this.loading = false;
			}
		});
	}

	selectProject(projectId: string): void {
		this.selectingProject = true;

		this.projectService.selectProject(projectId).subscribe({
			next: (study: PublicStudy) => {
				this.selectingProject = false;
				console.log('Study loaded after project selection:', study);
				this.configurationService.setStudy(study);
				this.router.navigate(['/dashboard']);
			},
			error: (error: any) => {
				this.selectingProject = false;
				console.error('Error selecting project:', error);
				alert('Failed to load project. Please try again.');
			}
		});
	}

	logout(): void {
		this.authStateService.logout().subscribe(
			() => this.router.navigate(['/login'])
		);
	}

	parseIntroductionText(html: string): {title: string; description: string} {
		const tmp = document.createElement('DIV');
		tmp.innerHTML = html;

		const h1Element = tmp.querySelector('h1');
		const pElement = tmp.querySelector('p');

		return {
			title: h1Element?.textContent || '',
			description: pElement?.textContent || ''
		};
	}
}
