import {Directive, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Subscription} from 'rxjs';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {ProjectLanguage} from '@core/model/project-language';
import {LanguageService} from '../services/language.service';

@Directive()
export abstract class BaseDetailComponent implements OnInit, OnDestroy {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Output() closed = new EventEmitter<void>();

	selectedLanguage = '';
	projectLanguages: ProjectLanguage[] = [];
	private languageSubscription!: Subscription;

	protected constructor(
		public languageService: LanguageService,
		protected snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.projectLanguages = this.project?.languages?.length
			? this.project.languages
			: this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(lang => {
			this.selectedLanguage = lang;
		});
		this.onInit();
	}

	ngOnDestroy(): void {
		this.languageSubscription?.unsubscribe();
		this.onDestroy();
	}

	protected onInit(): void {}

	protected onDestroy(): void {}

	onClose(): void {
		this.closed.emit();
	}

	protected showStagedMessage(): void {
		this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
	}
}
