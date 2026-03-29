import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatInputModule} from '@angular/material/input';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Rule} from '@core/model/rule';
import {ProjectLanguage} from '@core/model/project-language';
import {LanguageService} from '../../../services/language.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';
import {MatIcon} from '@angular/material/icon';

interface RuleBasicInfoDialogData {
	rule: Rule;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-rule-basic-info-dialog',
	standalone: true,
	templateUrl: './rule-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatInputModule, MatTabsModule, MatIcon, FormsModule]
})
export class RuleBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	selectedTags: string[] = [];
	newTagInput = '';

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<RuleBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: RuleBasicInfoDialogData,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
	}

	ngOnInit(): void {
		this.loadProjectLanguages(this.data.languages);
		this.form = this.fb.group({
			description: [this.data.rule.description ?? '']
		});
		this.selectedTags = [...(this.data.rule.tags ?? [])].sort();
	}

	initializeLanguageForms(): void {
		this.availableLanguages.forEach(lang => {
			if(!lang.languageCode) {
				return;
			}
			this.languageForms.set(lang.languageCode, this.fb.group({
				message: [this.data.rule.message?.[lang.languageCode] ?? '']
			}));
		});
	}

	addTag(): void {
		const tag = this.newTagInput.trim().toUpperCase();
		if(!tag || this.selectedTags.includes(tag)) {
			return;
		}
		this.selectedTags = [...this.selectedTags, tag].sort();
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

	onSave(): void {
		const v = this.form.getRawValue();
		const message: Record<string, string> = {};
		this.languageForms.forEach((form, lang) => {
			const val = form.getRawValue().message;
			if(val) {
				message[lang] = val;
			}
		});
		this.dialogRef.close({
			description: v.description,
			tags: this.selectedTags,
			message
		});
	}
}
