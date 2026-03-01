import {Injectable} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {ProjectLanguage} from '@core/model/project-language';
import {Observable} from 'rxjs';
import {ResourceCategory} from '@core/model/resource-category';
import {
	ResourceCategoryDialogComponent, ResourceCategoryDialogData
} from '../../dialogs/resource-category/resource-category-dialog/resource-category-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class ResourceCategoryDialogService {
	constructor(
		private dialog: MatDialog
	) {}

	openCreateDialog(projectId: string, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<ResourceCategoryDialogComponent> = this.dialog.open(
			ResourceCategoryDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {projectId, resourceCategory: null, languages} as ResourceCategoryDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(projectId: string, resourceCategory: ResourceCategory, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<ResourceCategoryDialogComponent> = this.dialog.open(
			ResourceCategoryDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {projectId, resourceCategory: JSON.parse(JSON.stringify(resourceCategory)), languages} as ResourceCategoryDialogData
			}
		);
		return dialogRef.afterClosed();
	}
}
