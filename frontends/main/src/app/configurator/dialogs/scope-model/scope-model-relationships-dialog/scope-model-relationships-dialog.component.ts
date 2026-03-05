import {Component, Inject, OnInit} from '@angular/core';
import {ScopeModel} from '@core/model/scope-model';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../../services/language.service';
import {ScopeModelManagerService} from '../../../services/manager/scope-model-manager.service';
import {BaseDialogComponent} from '../../base-dialog.component';

interface DialogData {
	projectId: string;
	scopeModel: ScopeModel;
}

@Component({
	selector: 'app-scope-model-relationships-dialog',
	standalone: true,
	templateUrl: './scope-model-relationships-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule,
		MatSelectModule, MatIconModule]
})
export class ScopeModelRelationshipsDialogComponent extends BaseDialogComponent<DialogData> implements OnInit {
	form: FormGroup;
	availableParents: ScopeModel[] = [];

	constructor(
		public languageService: LanguageService,
		private fb: FormBuilder,
		dialogRef: MatDialogRef<ScopeModelRelationshipsDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: DialogData,
		private scopeModelManager: ScopeModelManagerService
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.availableParents = this.scopeModelManager.getAll().filter(
			sm => sm.scopeModelId !== this.data.scopeModel.scopeModelId
		);
		this.form = this.fb.group({
			parentIds: [this.data.scopeModel.parentIds || []],
			defaultParentId: [this.data.scopeModel.defaultParentId || '']
		});
	}

	toggleParent(parentId: string): void {
		const currentIds: string[] = [...this.form.get('parentIds')?.value];
		const index = currentIds.indexOf(parentId);

		if(index > -1) {
			currentIds.splice(index, 1);
			if(this.form.get('defaultParentId')?.value === parentId) {
				this.form.patchValue({defaultParentId: ''});
			}
		}
		else {
			currentIds.push(parentId);
		}

		this.form.patchValue({parentIds: currentIds});
	}

	isParentSelected(parentId: string): boolean {
		return (this.form.get('parentIds')?.value || []).includes(parentId);
	}

	getSelectedParents(): ScopeModel[] {
		const selectedIds = this.form.get('parentIds')?.value || [];
		return this.availableParents.filter(p => selectedIds.includes(p.scopeModelId));
	}

	onSave(): void {
		const formValue = this.form.getRawValue();
		const result = {
			parentIds: formValue.parentIds || [],
			defaultParentId: formValue.defaultParentId || ''
		};
		this.dialogRef.close(result);
	}

	getTranslatedName(translations: Record<string, string> | undefined): string {
		return this.languageService.getDefaultTranslation(translations) || '';
	}
}
