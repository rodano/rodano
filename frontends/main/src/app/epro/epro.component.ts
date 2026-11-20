import {Component, DestroyRef, OnInit, ViewEncapsulation, inject} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {RouterOutlet} from '@angular/router';
import {ConfigurationService} from '@core/services/configuration.service';
import {EproLanguageService} from './services/epro-language.service';

@Component({
	selector: 'app-epro',
	template: '<router-outlet></router-outlet>',
	styleUrls: ['./epro.component.scss'],
	//the styles of the ePRO pages are global but scoped to this component, so they don't leak into the staff application
	encapsulation: ViewEncapsulation.None,
	imports: [RouterOutlet]
})
export class EproComponent implements OnInit {
	private configurationService = inject(ConfigurationService);
	private languageService = inject(EproLanguageService);
	private destroyRef = inject(DestroyRef);

	ngOnInit() {
		this.configurationService.getPublicStudy().pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(study => this.languageService.setSelectedLanguageId(study.defaultLanguage.id));
	}
}
