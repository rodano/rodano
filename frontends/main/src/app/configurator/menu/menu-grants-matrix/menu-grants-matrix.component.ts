import {Observable} from 'rxjs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {LanguageService} from '../../services/language.service';
import {Component, Input} from '@angular/core';
import {BaseGrantsMatrixComponent} from '../../shared/grants-matrix/grants-matrix.component';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatButtonModule} from '@angular/material/button';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MenuConfig} from '@core/model/menu-config';
import {MenuGrantsService} from '../../services/api/menu-grants.service';

@Component({
	selector: 'app-menu-grants-matrix',
	standalone: true,
	templateUrl: '../../shared/grants-matrix/grants-matrix.component.html',
	styleUrls: ['../../shared/matrix-shared.css', '../../shared/list-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule]
})
export class MenuGrantsMatrixComponent extends BaseGrantsMatrixComponent {
	@Input() menus: MenuConfig[] = [];

	constructor(
		languageService: LanguageService,
		private menuGrantsService: MenuGrantsService,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	readonly title = 'Menu Rights Matrix';
	readonly subtitle = 'Define which profiles have access to each menu';
	readonly themeClass = 'theme-menu';
	get entities(): any[] {return this.menus;}
	getEntityId(e: MenuConfig): string {return e.menuId;}
	getEntityLabel(e: MenuConfig): string {return this.languageService.getTranslatedName(e.shortname);}
	getEntityCode(e: MenuConfig): string {return e.id;}
	getEntityCount(): number {return this.menus.length;}
	loadGrantsFromApi(): Observable<Record<string, string[]>> {return this.menuGrantsService.getMenuGrants(this.projectId);}
	saveGrantsToApi(grants: Record<string, string[]>): Observable<void> {return this.menuGrantsService.saveMenuGrants(this.projectId, grants);}
}
