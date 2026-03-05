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
import {DualListBoxComponent} from '../../dual-list-box/dual-list-box.component';

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
		MatSelectModule, MatIconModule, DualListBoxComponent]
})
export class ScopeModelRelationshipsDialogComponent extends BaseDialogComponent<DialogData> implements OnInit {
	form: FormGroup;
	availableParents: ScopeModel[] = [];
	selectedParents: ScopeModel[] = [];

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
		const all = this.scopeModelManager.getAll().filter(
			sm => sm.scopeModelId !== this.data.scopeModel.scopeModelId
		);
		const selectedIds = this.data.scopeModel.parentIds || [];
		this.selectedParents = all.filter(sm => selectedIds.includes(sm.scopeModelId));
		this.availableParents = all.filter(sm => !selectedIds.includes(sm.scopeModelId));

		this.form = this.fb.group({
			parentIds: [this.data.scopeModel.parentIds || []],
			defaultParentId: [this.data.scopeModel.defaultParentId || '']
		});
	}

	onAddParent(parent: ScopeModel): void {
		this.availableParents = this.availableParents.filter(p => p.scopeModelId !== parent.scopeModelId);
		this.selectedParents = [...this.selectedParents, parent];
	}

	onRemoveParent(parent: ScopeModel): void {
		this.selectedParents = this.selectedParents.filter(p => p.scopeModelId !== parent.scopeModelId);
		this.availableParents = [...this.availableParents, parent];
		if(this.form.get('defaultParentId')?.value === parent.scopeModelId) {
			this.form.patchValue({defaultParentId: ''});
		}
	}

	onSave(): void {
		this.dialogRef.close({
			parentIds: this.selectedParents.map(p => p.scopeModelId),
			defaultParentId: this.form.getRawValue().defaultParentId || ''
		});
	}
}
