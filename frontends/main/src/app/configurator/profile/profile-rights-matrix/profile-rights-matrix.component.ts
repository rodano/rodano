import {BaseRightsMatrixComponent} from '../../shared/rights-matrix/rights-matrix.component';
import {Component, Input} from '@angular/core';
import {LanguageService} from '../../services/language.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Observable} from 'rxjs';
import {EntityRight} from '@core/model/entity-right';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {CommonModule} from '@angular/common';
import {Profile} from '@core/model/profile';
import {ProfileRightsService} from '../../services/api/profile-rights.service';

@Component({
	selector: 'app-profile-rights-matrix',
	standalone: true,
	templateUrl: '../../shared/rights-matrix/rights-matrix.component.html',
	styleUrls: ['../../shared/matrix-shared.css', '../../shared/list-shared.css'],
	imports: [CommonModule, MatIconModule, MatTooltipModule]
})
export class ProfileRightsMatrixComponent extends BaseRightsMatrixComponent {
	@Input() targetProfiles: Profile[] = [];

	constructor(
		languageService: LanguageService,
		private profileRightsService: ProfileRightsService,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	readonly title = 'Profile Rights';
	readonly subtitle = 'Define read/write access per profile';
	readonly themeClass = 'theme-profile';
	get entities(): any[] {return this.targetProfiles;}
	getEntityId(e: Profile): string {return e.profileId;}
	getEntityLabel(e: Profile): string {return this.languageService.getTranslatedName(e.shortname);}
	getEntityCode(e: Profile): string {return e.id;}
	loadRightsFromApi(): Observable<Record<string, Record<string, EntityRight>>> {return this.profileRightsService.getRights(this.projectId);}
	saveRightsToApi(rights: Record<string, Record<string, EntityRight>>): Observable<void> {return this.profileRightsService.saveRights(this.projectId, rights);}
}
