import { Component, OnDestroy, OnInit } from '@angular/core';
import { AppService } from './services/app.service';
import { ConfigurationService } from './api/services/configuration.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ScopeModelDTO } from './api/model/scope-model-dto';
import { IonicModule } from '@ionic/angular';

@Component({
	selector: 'app-root',
	templateUrl: './app.component.html',
	styleUrls: ['./app.css'],
	standalone: true,
	imports: [IonicModule]
})
export class AppComponent implements OnInit, OnDestroy {

	public leafScopeModel: ScopeModelDTO;

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
