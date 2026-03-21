import {BaseRightsMatrixComponent} from '../../shared/rights-matrix/rights-matrix.component';
import {Component, Input} from '@angular/core';
import {LanguageService} from '../../services/language.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Observable} from 'rxjs';
import {EntityRight} from '@core/model/entity-right';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {CommonModule} from '@angular/common';
import {DatasetModel} from '@core/model/dataset-model';
import {DatasetModelRightsService} from '../../services/api/dataset-model-rights.service';

@Component({
	selector: 'app-dataset-model-rights-matrix',
	standalone: true,
	templateUrl: '../../shared/rights-matrix/rights-matrix.component.html',
	styleUrls: ['../../shared/matrix-shared.css', '../../shared/list-shared.css'],
	imports: [CommonModule, MatIconModule, MatTooltipModule]
})
export class DatasetModelRightsMatrixComponent extends BaseRightsMatrixComponent {
	@Input() datasetModels: DatasetModel[] = [];

	constructor(
		languageService: LanguageService,
		private datasetModelRightsService: DatasetModelRightsService,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	readonly title = 'Dataset Model Rights';
	readonly subtitle = 'Define read/write access per profile';
	readonly themeClass = 'theme-dataset-model';
	get entities(): any[] {return this.datasetModels;}
	getEntityId(e: DatasetModel): string {return e.datasetModelId;}
	getEntityLabel(e: DatasetModel): string {return this.languageService.getTranslatedName(e.shortname);}
	getEntityCode(e: DatasetModel): string {return e.id;}
	loadRightsFromApi(): Observable<Record<string, Record<string, EntityRight>>> {return this.datasetModelRightsService.getRights(this.projectId);}
	saveRightsToApi(rights: Record<string, Record<string, EntityRight>>): Observable<void> {return this.datasetModelRightsService.saveRights(this.projectId, rights);}
}
