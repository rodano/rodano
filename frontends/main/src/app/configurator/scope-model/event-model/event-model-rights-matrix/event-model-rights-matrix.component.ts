import {BaseRightsMatrixComponent} from '../../../shared/rights-matrix/rights-matrix.component';
import {Component, Input} from '@angular/core';
import {LanguageService} from '../../../services/language.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Observable} from 'rxjs';
import {EntityRight} from '@core/model/entity-right';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {CommonModule} from '@angular/common';
import {EventModel} from '@core/model/event-model';
import {EventModelRightsService} from '../../../services/api/event-model-rights.service';

@Component({
	selector: 'app-event-model-rights-matrix',
	standalone: true,
	templateUrl: '../../../shared/rights-matrix/rights-matrix.component.html',
	styleUrls: ['../../../shared/matrix-shared.css', '../../../shared/list-shared.css'],
	imports: [CommonModule, MatIconModule, MatTooltipModule]
})
export class EventModelRightsMatrixComponent extends BaseRightsMatrixComponent {
	@Input() eventModels: EventModel[] = [];

	constructor(
		languageService: LanguageService,
		private eventModelRightsService: EventModelRightsService,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	readonly title = 'Event Model Rights';
	readonly subtitle = 'Define read/write access per profile';
	readonly themeClass = 'theme-event-model';
	get entities(): any[] {return this.eventModels;}
	getEntityId(e: EventModel): string {return e.eventModelId;}
	getEntityLabel(e: EventModel): string {return this.languageService.getTranslatedName(e.shortname);}
	getEntityCode(e: EventModel): string {return e.id;}
	loadRightsFromApi(): Observable<Record<string, Record<string, EntityRight>>> {return this.eventModelRightsService.getRights(this.projectId);}
	saveRightsToApi(rights: Record<string, Record<string, EntityRight>>): Observable<void> {return this.eventModelRightsService.saveRights(this.projectId, rights);}
}
