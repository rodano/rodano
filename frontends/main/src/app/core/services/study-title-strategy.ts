import {inject, Service} from '@angular/core';
import {RouterStateSnapshot, TitleStrategy} from '@angular/router';
import {Title} from '@angular/platform-browser';
import {ConfigurationService} from './configuration.service';

/**
 * Sets the browser tab title on every navigation.
 *
 * The study shortname (in the study's default language) is used as a prefix, so tabs read
 * "<shortname> - <page>" when a route defines a title, or just "<shortname>" otherwise. The study
 * is resolved through the cached getPublicStudy() call, so no extra HTTP request is triggered.
 */
@Service()
export class StudyTitleStrategy extends TitleStrategy {
	private readonly title = inject(Title);
	private readonly configurationService = inject(ConfigurationService);

	private mainTitle: string;

	constructor() {
		super();
		this.configurationService.getPublicStudy().subscribe(study => {
			this.mainTitle = study.shortname[study.defaultLanguage.id] ?? 'Rodano';
		});
	}

	override updateTitle(snapshot: RouterStateSnapshot): void {
		let title = this.mainTitle;
		const pageTitle = this.buildTitle(snapshot);
		if(pageTitle) {
			title += ` - ${pageTitle}`;
		}
		this.title.setTitle(title);
	}
}
