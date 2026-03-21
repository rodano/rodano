import {Observable} from 'rxjs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {LanguageService} from '../../services/language.service';
import {Component, Input} from '@angular/core';
import {BaseGrantsMatrixComponent} from '../../shared/grants-matrix/grants-matrix.component';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatButtonModule} from '@angular/material/button';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {ResourceCategory} from '@core/model/resource-category';
import {ResourceCategoryGrantsService} from '../../services/api/resource-category-grants.service';

@Component({
	selector: 'app-resource-category-grants-matrix',
	standalone: true,
	templateUrl: '../../shared/grants-matrix/grants-matrix.component.html',
	styleUrls: ['../../shared/matrix-shared.css', '../../shared/list-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule]
})
export class ResourceCategoryGrantsMatrixComponent extends BaseGrantsMatrixComponent {
	@Input() resourceCategories: ResourceCategory[] = [];

	constructor(
		languageService: LanguageService,
		private resourceCategoryGrantsService: ResourceCategoryGrantsService,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	readonly title = 'Resource Category Rights Matrix';
	readonly subtitle = 'Define which profiles have access to each resource category';
	readonly themeClass = 'theme-resource-category';
	get entities(): any[] {return this.resourceCategories;}
	getEntityId(e: ResourceCategory): string {return e.categoryId;}
	getEntityLabel(e: ResourceCategory): string {return this.languageService.getTranslatedName(e.shortname);}
	getEntityCode(e: ResourceCategory): string {return e.id;}
	getEntityCount(): number {return this.resourceCategories.length;}
	loadGrantsFromApi(): Observable<Record<string, string[]>> {return this.resourceCategoryGrantsService.getResourceCategoryGrants(this.projectId);}
	saveGrantsToApi(grants: Record<string, string[]>): Observable<void> {return this.resourceCategoryGrantsService.saveResourceCategoryGrants(this.projectId, grants);}
}
