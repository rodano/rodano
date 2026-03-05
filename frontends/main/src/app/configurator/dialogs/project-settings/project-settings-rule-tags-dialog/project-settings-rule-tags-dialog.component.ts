import {ProjectRuleTag} from '@core/model/project-rule-tag';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {FormsModule} from '@angular/forms';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface RuleTagsDialogData {
	ruleTags: ProjectRuleTag[];
}

@Component({
	selector: 'app-edit-rule-tags-dialog',
	standalone: true,
	templateUrl: './project-settings-rule-tags-dialog.component.html',
	styleUrls: ['./project-settings-rule-tags-dialog.component.css'],
	imports: [CommonModule, MatDialogModule, MatIconModule, MatTooltipModule, FormsModule]
})
export class ProjectSettingsRuleTagsDialogComponent extends BaseDialogComponent<RuleTagsDialogData> implements OnInit {
	selectedTags: string[] = [];
	newTagInput = '';

	constructor(
		dialogRef: MatDialogRef<ProjectSettingsRuleTagsDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: RuleTagsDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		if(this.data.ruleTags && this.data.ruleTags.length > 0) {
			this.selectedTags = this.data.ruleTags
				.map(tag => tag.tag)
				.filter(tag => tag !== undefined) as string[];
		}
	}

	addTag(): void {
		const tag = this.newTagInput.trim();

		if(!tag) {
			return;
		}

		if(this.selectedTags.includes(tag)) {
			return;
		}

		this.selectedTags.push(tag);
		this.selectedTags.sort();

		this.newTagInput = '';
	}

	removeTag(tag: string): void {
		this.selectedTags = this.selectedTags.filter(t => t !== tag);
	}

	onKeyPress(event: KeyboardEvent): void {
		if(event.key === 'Enter') {
			event.preventDefault();
			this.addTag();
		}
	}

	isValid(): boolean {
		return this.selectedTags.length > 0;
	}

	onSave(): void {
		if(!this.isValid()) {
			return;
		}

		const result: ProjectRuleTag[] = this.selectedTags.map(tag => ({
			tag: tag
		}));

		this.dialogRef.close(result);
	}
}
