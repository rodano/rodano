import {Feature} from '@core/model/feature';
import {Observable} from 'rxjs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {LanguageService} from '../../services/language.service';
import {FeatureGrantsService} from '../../services/api/feature-grants.service';
import {Component, Input} from '@angular/core';
import {BaseGrantsMatrixComponent} from '../../shared/grants-matrix/grants-matrix.component';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatButtonModule} from '@angular/material/button';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';

@Component({
	selector: 'app-feature-grants-matrix',
	standalone: true,
	templateUrl: '../../shared/grants-matrix/grants-matrix.component.html',
	styleUrls: ['../../shared/matrix-shared.css', '../../shared/list-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule]
})
export class FeatureGrantsMatrixComponent extends BaseGrantsMatrixComponent {
	@Input() features: Feature[] = [];

	constructor(
		languageService: LanguageService,
		private featureGrantsService: FeatureGrantsService,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	readonly title = 'Feature Rights Matrix';
	readonly subtitle = 'Define which profiles have access to each feature';
	readonly themeClass = 'theme-feature';
	get entities(): any[] {return this.features;}
	getEntityId(e: Feature): string {return e.featureId;}
	getEntityLabel(e: Feature): string {return this.languageService.getTranslatedName(e.shortname);}
	getEntityCode(e: Feature): string {return e.id;}
	getEntityCount(): number {return this.features.length;}
	loadGrantsFromApi(): Observable<Record<string, string[]>> {return this.featureGrantsService.getFeatureGrants(this.projectId);}
	saveGrantsToApi(grants: Record<string, string[]>): Observable<void> {return this.featureGrantsService.saveFeatureGrants(this.projectId, grants);}
}
