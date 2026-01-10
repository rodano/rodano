import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Project, ProjectService} from '@core/services/project.service';
import {Router} from '@angular/router';
import {PublicStudy} from '@core/model/public-study';
import {ConfigurationService} from '@core/services/configuration.service';
import {AuthStateService} from '../services/auth-state.service';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {LogoComponent} from '../logo/logo.component';
import {FormsModule} from '@angular/forms';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

@Component({
	selector: 'app-project-selection',
	standalone: true,
	templateUrl: './project-selection.component.html',
	styleUrl: './project-selection.component.css',
	imports: [
		CommonModule,
		FormsModule,
		MatIconModule,
		MatIconButton,
		MatTooltipModule,
		MatProgressSpinnerModule,
		LogoComponent,
		MatButton
	]
})
export class ProjectSelectionComponent implements OnInit {
	projects: Project[] = [];
	filteredProjects: Project[] = [];
	loading = false;
	selectingProject: string | null = null;
	isSuperuser = false;

	searchTerm = '';
	filters = {
		showActive: true,
		showClosed: true,
		showArchived: false,

		createdAfter: null as string | null,
		createdBefore: null as string | null
	};

	sortBy: 'name' | 'nameDesc' | 'createdNewest' | 'createdOldest' = 'name';

	expandedProjects = new Set<string>();

	constructor(
		private projectService: ProjectService,
		private router: Router,
		private configurationService: ConfigurationService,
		private authStateService: AuthStateService
	) {}

	ngOnInit(): void {
		document.documentElement.style.removeProperty('--mat-sys-primary');
		this.loadProjects();
		this.checkSuperuserStatus();
	}

	checkSuperuserStatus(): void {
		this.authStateService.listenConnectedUser().subscribe(user => {
			this.isSuperuser = user?.superuser || false;
		});
	}

	navigateToUserManagement(): void {
		this.router.navigate(['/user-management']);
	}

	navigateToConfigurator(): void {
		this.router.navigate(['/configurator']);
	}

	loadProjects(): void {
		this.loading = true;
		this.projectService.getProjects().subscribe({
			next: (projects: Project[]) => {
				this.projects = projects;
				this.applyFilters();
				this.loading = false;
			},
			error: (error: any) => {
				console.error('Error loading projects:', error);
				this.loading = false;
			}
		});
	}

	applyFilters(): void {
		let filtered = [...this.projects];

		if(this.searchTerm.trim()) {
			const term = this.searchTerm.toLowerCase();
			filtered = filtered.filter(p => {
				const lang = this.getDefaultLanguage(p);
				return p.description[lang]?.toLowerCase().includes(term) || p.longname[lang]?.toLowerCase().includes(term) || p.shortname[lang]?.toLowerCase().includes(term) || p.code.toLowerCase().includes(term);
			}
			);
		}

		filtered = filtered.filter(p => {
			const status = p.status || 'ACTIVE';
			if(status === 'ACTIVE' && !this.filters.showActive) {
				return false;
			}
			if(status === 'CLOSED' && !this.filters.showClosed) {
				return false;
			}
			if(status === 'ARCHIVED' && !this.filters.showArchived) {
				return false;
			}
			return true;
		});

		filtered = filtered.filter(p => {
			if(this.filters.createdAfter && p.created) {
				const createdDate = new Date(p.created);
				if(createdDate < new Date(this.filters.createdAfter)) {
					return false;
				}
			}
			if(this.filters.createdBefore && p.created) {
				const createdDate = new Date(p.created);
				if(createdDate > new Date(this.filters.createdBefore)) {
					return false;
				}
			}
			return true;
		});

		filtered.sort((a, b) => {
			switch(this.sortBy) {
				case 'name':
					return (a.shortname[this.getDefaultLanguage(a)] || a.code).localeCompare(b.shortname[this.getDefaultLanguage(b)] || b.code);
				case 'nameDesc':
					return (b.shortname[this.getDefaultLanguage(b)] || b.code).localeCompare(a.shortname[this.getDefaultLanguage(a)] || a.code);
				case 'createdNewest':
					return this.getCreatedTimestamp(b) - this.getCreatedTimestamp(a);
				case 'createdOldest':
					return this.getCreatedTimestamp(a) - this.getCreatedTimestamp(b);
				default:
					return 0;
			}
		});

		this.filteredProjects = filtered;
	}

	clearFilters(): void {
		this.searchTerm = '';
		this.filters = {
			showActive: true,
			showClosed: true,
			showArchived: false,
			createdAfter: null,
			createdBefore: null
		};
		this.sortBy = 'name';
		this.applyFilters();
	}

	hasActiveFilters(): boolean {
		return this.searchTerm.trim() !== '' || !this.filters.showActive || !this.filters.showClosed || this.filters.showArchived || this.filters.createdAfter !== null || this.filters.createdBefore !== null;
	}

	selectProject(projectId: string): void {
		this.selectingProject = projectId;

		this.projectService.selectProject(projectId).subscribe({
			next: (study: PublicStudy) => {
				this.selectingProject = null;
				console.log('Study loaded after project selection:', study);
				this.configurationService.setStudy(study);
				this.router.navigate(['/dashboard']);
			},
			error: (error: any) => {
				this.selectingProject = null;
				console.error('Error selecting project:', error);
				alert('Failed to load project. Please try again.');
			}
		});
	}

	logout(): void {
		document.documentElement.style.removeProperty('--mat-sys-primary');
		this.authStateService.logout().subscribe(
			() => this.router.navigate(['/login'])
		);
	}

	getDescription(project: Project): string {
		return project.introductionText || project.description['en'] || '';
	}

	toggleDescription(event: Event, projectId: string): void {
		event.stopPropagation();
		if(this.expandedProjects.has(projectId)) {
			this.expandedProjects.delete(projectId);
		}
		else {
			this.expandedProjects.add(projectId);
		}
	}

	shouldShowViewMore(project: Project): boolean {
		const description = this.getDescription(project);
		return description.length > 160;
	}

	private getCreatedTimestamp(project: Project): number {
		return project.created ? new Date(project.created).getTime() : 0;
	}

	getDefaultLanguage(project: Project): string {
		const defaultLang = project.languages?.find(l => l.isDefault);
		return defaultLang?.languageCode || project.languages?.[0]?.languageCode || 'en';
	}
}
