import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {AppService} from './services/app.service';
import {ConfigurationService} from '@core/services/configuration.service';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {RouterOutlet} from '@angular/router';

@Component({
	selector: 'app-root',
	templateUrl: './app.component.html',
	styleUrls: ['./app.css'],
	imports: [RouterOutlet]
})
export class AppComponent implements OnInit {
	private appService = inject(AppService);
	private configurationService = inject(ConfigurationService);
	private destroyRef = inject(DestroyRef);

	ngOnInit() {
		this.configurationService.getPublicStudy().pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(study => {
			this.appService.setSelectedLanguageId(study.defaultLanguage.id);
			//theme the application with the color of the study, as the main frontend does
			document.body.style.setProperty('--mat-sys-primary', study.color);
			document.body.style.setProperty('--mat-sys-on-primary', 'white');
			document.body.style.setProperty('--mat-sys-primary-container', 'color(from var(--mat-sys-primary) display-p3 calc(r - 0.1) calc(g - 0.1) calc(b - 0.1))');
			document.body.style.setProperty('--mat-sys-on-primary-container', 'white');
		});
	}
}
