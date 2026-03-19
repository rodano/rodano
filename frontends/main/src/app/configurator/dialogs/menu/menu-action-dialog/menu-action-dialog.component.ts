import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatTabsModule} from '@angular/material/tabs';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {BaseDialogComponent} from '../../base-dialog.component';
import {MenuConfig} from '@core/model/menu-config';
import {ScopeModel} from '@core/model/scope-model';
import {LanguageService} from '../../../services/language.service';
import {MatSelectModule} from '@angular/material/select';

export interface MenuActionDialogData {
	menu: MenuConfig;
	scopeModels: ScopeModel[];
}

@Component({
	selector: 'app-menu-action-dialog',
	standalone: true,
	templateUrl: './menu-action-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, MatCheckboxModule, MatTabsModule,
		ReactiveFormsModule, MatSelectModule]
})
export class MenuActionDialogComponent
	extends BaseDialogComponent<MenuActionDialogData>
	implements OnInit {
	form: FormGroup;
	parameters: {key: string; value: string}[] = [];
	contextItems: string[] = [];

	readonly ACTION_PAGES = [
		'administration',
		'audit-trails',
		'benchmark',
		'crf',
		'dashboard',
		'documentation',
		'epro',
		'extracts',
		'mails',
		'help',
		'resources',
		'robots',
		'send-test-mail',
		'scopes',
		'users',
		'widget'
	];

	constructor(
		private fb: FormBuilder,
		public languageService: LanguageService,
		dialogRef: MatDialogRef<MenuActionDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: MenuActionDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		const action = this.data.menu.action;

		this.form = this.fb.group({
			page: [action?.page ?? '']
		});

		this.form.get('page')!.valueChanges.subscribe(() => {
			this.contextItems = [];
		});

		this.contextItems = [...(action?.context ?? [])];
		this.parameters = Object.entries(action?.parameters ?? {}).map(([key, value]) => ({key, value}));
	}

	get isScopesPage(): boolean {
		return this.form.get('page')?.value?.trim().toLowerCase() === 'scopes';
	}

	get availableScopeModels(): ScopeModel[] {
		return this.data.scopeModels.filter(s => !this.contextItems.includes(s.scopeModelId));
	}

	get selectedScopeModels(): ScopeModel[] {
		return this.contextItems
			.map(id => this.data.scopeModels.find(s => s.scopeModelId === id))
			.filter((s): s is ScopeModel => !!s);
	}

	addContextItem(): void {
		this.contextItems = [...this.contextItems, ''];
	}

	removeContextItem(index: number): void {
		this.contextItems = this.contextItems.filter((_, i) => i !== index);
	}

	updateContextItem(index: number, value: string): void {
		this.contextItems = this.contextItems.map((item, i) => i === index ? value : item);
	}

	addScopeModel(scopeModelId: string): void {
		if(scopeModelId && !this.contextItems.includes(scopeModelId)) {
			this.contextItems = [...this.contextItems, scopeModelId];
		}
	}

	removeScopeModel(scopeModelId: string): void {
		this.contextItems = this.contextItems.filter(id => id !== scopeModelId);
	}

	addParameter(): void {
		this.parameters = [...this.parameters, {key: '', value: ''}];
	}

	removeParameter(index: number): void {
		this.parameters = this.parameters.filter((_, i) => i !== index);
	}

	updateParameter(index: number, field: 'key' | 'value', value: string): void {
		this.parameters = this.parameters.map((p, i) => i === index ? {...p, [field]: value} : p);
	}

	onSave(): void {
		const parameters = this.parameters.reduce((acc, {key, value}) => {
			if(key.trim()) {
				acc[key.trim()] = value;
			}
			return acc;
		}, {} as Record<string, string>);

		this.dialogRef.close({
			action: {
				page: this.form.value.page,
				context: this.contextItems.filter(c => c.trim()),
				parameters
			}
		});
	}
}
