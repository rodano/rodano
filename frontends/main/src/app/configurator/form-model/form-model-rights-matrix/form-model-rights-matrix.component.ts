import {BaseRightsMatrixComponent} from '../../shared/rights-matrix/rights-matrix.component';
import {Component, Input} from '@angular/core';
import {LanguageService} from '../../services/language.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Observable} from 'rxjs';
import {EntityRight} from '@core/model/entity-right';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {CommonModule} from '@angular/common';
import {FormModel} from '@core/model/form-model';
import {FormModelRightsService} from '../../services/api/form-model-rights.service';

@Component({
	selector: 'app-form-model-rights-matrix',
	standalone: true,
	templateUrl: '../../shared/rights-matrix/rights-matrix.component.html',
	styleUrls: ['../../shared/matrix-shared.css', '../../shared/list-shared.css'],
	imports: [CommonModule, MatIconModule, MatTooltipModule]
})
export class FormModelRightsMatrixComponent extends BaseRightsMatrixComponent {
	@Input() formModels: FormModel[] = [];

	constructor(
		languageService: LanguageService,
		private formModelRightsService: FormModelRightsService,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	readonly title = 'Form Model Rights';
	readonly subtitle = 'Define read/write access per profile';
	readonly themeClass = 'theme-form-model';
	get entities(): any[] {return this.formModels;}
	getEntityId(e: FormModel): string {return e.formModelId;}
	getEntityLabel(e: FormModel): string {return this.languageService.getTranslatedName(e.shortname);}
	getEntityCode(e: FormModel): string {return e.id;}
	loadRightsFromApi(): Observable<Record<string, Record<string, EntityRight>>> {return this.formModelRightsService.getRights(this.projectId);}
	saveRightsToApi(rights: Record<string, Record<string, EntityRight>>): Observable<void> {return this.formModelRightsService.saveRights(this.projectId, rights);}
}
