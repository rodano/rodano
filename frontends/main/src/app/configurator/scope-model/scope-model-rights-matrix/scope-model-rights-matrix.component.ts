import {BaseRightsMatrixComponent} from '../../shared/rights-matrix/rights-matrix.component';
import {ScopeModel} from '@core/model/scope-model';
import {Component, Input} from '@angular/core';
import {LanguageService} from '../../services/language.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ScopeModelRightsService} from '../../services/api/scope-model-rights.service';
import {Observable} from 'rxjs';
import {EntityRight} from '@core/model/entity-right';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {CommonModule} from '@angular/common';

@Component({
	selector: 'app-scope-model-rights-matrix',
	standalone: true,
	templateUrl: '../../shared/rights-matrix/rights-matrix.component.html',
	styleUrls: ['../../shared/matrix-shared.css', '../../shared/list-shared.css'],
	imports: [CommonModule, MatIconModule, MatTooltipModule]
})
export class ScopeModelRightsMatrixComponent extends BaseRightsMatrixComponent {
	@Input() scopeModels: ScopeModel[] = [];

	constructor(
		languageService: LanguageService,
		private scopeModelRightsService: ScopeModelRightsService,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	readonly title = 'Scope Model Rights';
	readonly subtitle = 'Define read/write access per profile';
	readonly themeClass = 'theme-scope-model';
	get entities(): any[] {return this.scopeModels;}
	getEntityId(e: ScopeModel): string {return e.scopeModelId;}
	getEntityLabel(e: ScopeModel): string {return this.languageService.getTranslatedName(e.shortname);}
	getEntityCode(e: ScopeModel): string {return e.id;}
	loadRightsFromApi(): Observable<Record<string, Record<string, EntityRight>>> {return this.scopeModelRightsService.getRights(this.projectId);}
	saveRightsToApi(rights: Record<string, Record<string, EntityRight>>): Observable<void> {return this.scopeModelRightsService.saveRights(this.projectId, rights);}
}
