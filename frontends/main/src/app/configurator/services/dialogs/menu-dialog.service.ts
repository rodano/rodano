import {Injectable} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {ProjectLanguage} from '@core/model/project-language';
import {Observable} from 'rxjs';
import {
	MenuBasicInfoDialogComponent, MenuBasicInfoDialogData
} from '../../dialogs/menu/menu-basic-info-dialog/menu-basic-info-dialog.component';
import {MenuConfig} from '@core/model/menu-config';
import {MenuActionDialogComponent, MenuActionDialogData} from '../../dialogs/menu/menu-action-dialog/menu-action-dialog.component';
import {ScopeModelManagerService} from '../manager/scope-model-manager.service';
import {MenuParentDialogComponent, MenuParentDialogData} from '../../dialogs/menu/menu-parent-dialog/menu-parent-dialog.component';
import {
	MenuSubmenusOrderDialogComponent, MenuSubmenusOrderDialogData
} from '../../dialogs/menu/menu-submenus-order-dialog/menu-submenus-order-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class MenuDialogService {
	constructor(
		private scopeModelManager: ScopeModelManagerService,
		private dialog: MatDialog
	) {}

	openCreateDialog(projectId: string, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<MenuBasicInfoDialogComponent> = this.dialog.open(
			MenuBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {
					projectId,
					menu: null,
					languages
				} as MenuBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(projectId: string, menu: MenuConfig, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<MenuBasicInfoDialogComponent> = this.dialog.open(
			MenuBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {
					projectId,
					menu: JSON.parse(JSON.stringify(menu)),
					languages
				} as MenuBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openParentDialog(menu: MenuConfig, allMenus: MenuConfig[]): Observable<any> {
		return this.dialog.open(MenuParentDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				menu: JSON.parse(JSON.stringify(menu)),
				allMenus
			} as MenuParentDialogData
		}).afterClosed();
	}

	openSubmenusOrderDialog(submenus: MenuConfig[]): Observable<any> {
		return this.dialog.open(MenuSubmenusOrderDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				submenus: JSON.parse(JSON.stringify(submenus))
			} as MenuSubmenusOrderDialogData
		}).afterClosed();
	}

	openActionDialog(menu: MenuConfig): Observable<any> {
		return this.dialog.open(MenuActionDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				menu: JSON.parse(JSON.stringify(menu)),
				scopeModels: this.scopeModelManager.getAll()
			} as MenuActionDialogData
		}).afterClosed();
	}
}
