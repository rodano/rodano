import {Component, OnDestroy, OnInit} from '@angular/core';
import {AppService} from './services/app.service';
import {ConfigurationService} from '@core/services/configuration.service';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {IonApp, IonRouterOutlet} from '@ionic/angular/standalone';

@Component({
	selector: 'app-root',
	templateUrl: './app.component.html',
	styleUrls: ['./app.css'],
	standalone: true,
	imports: [IonApp, IonRouterOutlet]
})
export class AppComponent implements OnInit, OnDestroy {
	unsubscribe$ = new Subject<void>();

	constructor(
		private appService: AppService,
		private configurationService: ConfigurationService
	) { }

	ngOnInit() {
		this.configurationService.getPublicStudy().pipe(
			takeUntil(this.unsubscribe$)
		).subscribe(study => this.appService.setSelectedLanguageId(study.defaultLanguage.id));
	}

	ngOnDestroy() {
		this.unsubscribe$.next();
		this.unsubscribe$.complete();
	}
}
