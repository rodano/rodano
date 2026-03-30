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
import {RuleService} from '../../../services/api/rule.service';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatOptionModule} from '@angular/material/core';

interface RuleBasicInfoDialogData {
	rule: Rule;
	languages: ProjectLanguage[];
	projectId: string;
	entityPath: string;
	availableTags: string[];
}

@Component({
	selector: 'app-rule-basic-info-dialog',
	standalone: true,
	templateUrl: './rule-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatInputModule, MatTabsModule, MatIcon,
		FormsModule, MatAutocompleteModule, MatOptionModule]
})
export class RuleBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	selectedTags: string[] = [];
	newTagInput = '';

	private readonly sb: MatSnackBar;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<RuleBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: RuleBasicInfoDialogData,
		snackBar: MatSnackBar,
		private ruleService: RuleService
	) {
		super(fb, languageService, dialogRef, data, snackBar);
		this.sb = snackBar;
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

	get tagSuggestions(): string[] {
		const input = this.newTagInput.trim().toUpperCase();
		if(!input) {
			return [];
		}
		return (this.data.availableTags as string[] ?? [])
			.map((t: string) => t.toUpperCase())
			.filter((t: string) => t.includes(input) && !this.selectedTags.includes(t));
	}

	selectTag(tag: string): void {
		this.newTagInput = tag;
		this.addTag();
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

		const updated = {
			...this.data.rule,
			description: v.description,
			tags: this.selectedTags,
			message
		};

		this.ruleService.updateRule(
			this.data.projectId,
			this.data.entityPath,
			this.data.rule.ruleId!,
			updated
		).subscribe({
			next: saved => {
				this.sb.open('Rule saved', 'Close', {duration: 2000});
				this.dialogRef.close(saved);
			},
			error: () => this.sb.open('Failed to save rule', 'Close', {duration: 3000})
		});
	}
}
