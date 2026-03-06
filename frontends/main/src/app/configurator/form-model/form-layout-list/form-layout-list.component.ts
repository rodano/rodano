import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {ModifiedDirective} from '../../shared/modified.directive';
import {Layout} from '@core/model/layout';
import {LanguageService} from '../../services/language.service';
import {FormLayoutService} from '../../services/api/form-layout.service';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {
	FormLayoutCreateDialogComponent
} from '../../dialogs/form-model/form-layout-create-dialog/form-layout-create-dialog.component';
import {FieldModelManagerService} from '../../services/manager/field-model-manager.service';

@Component({
	selector: 'app-form-layout-list',
	standalone: true,
	templateUrl: './form-layout-list.component.html',
	styleUrls: ['./form-layout-list.component.css'],
	imports: [CommonModule, MatIconModule, MatTooltipModule, ModifiedDirective]
})
export class FormLayoutListComponent implements OnInit, OnChanges {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() formModelId = '';
	@Input() selectedLayoutId: string | null = null;
	@Input() compact = false;
	@Input() modifiedLayoutIds = new Set<string>();
	@Output() layoutSelected = new EventEmitter<Layout>();
	@Output() layoutCreated = new EventEmitter<Layout>();
	@Output() layoutDeleted = new EventEmitter<string>();

	layouts: Layout[] = [];
	loading = false;

	constructor(
		public languageService: LanguageService,
		private formLayoutService: FormLayoutService,
		private fieldModelManager: FieldModelManagerService,
		private dialog: MatDialog,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		if(this.projectId) {
			this.fieldModelManager.loadFull(this.projectId).subscribe();
		}
		this.loadLayouts();
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['formModelId'] && !changes['formModelId'].firstChange) {
			this.loadLayouts();
		}
		if(changes['projectId'] && this.projectId) {
			this.fieldModelManager.loadFull(this.projectId).subscribe();
		}
	}

	loadLayouts(): void {
		if(!this.formModelId) {
			return;
		}
		this.loading = true;
		this.formLayoutService.getLayouts(this.projectId, this.formModelId).subscribe({
			next: layouts => {
				this.layouts = layouts;
				this.loading = false;
			},
			error: error => {
				console.error('Error loading layouts:', error);
				this.snackBar.open('Failed to load layouts', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	isSelected(layout: Layout): boolean {
		return this.selectedLayoutId === layout.formLayoutId;
	}

	isModified(layout: Layout): boolean {
		return this.modifiedLayoutIds.has(layout.formLayoutId);
	}

	onSelectLayout(layout: Layout): void {
		this.layoutSelected.emit(layout);
	}

	onCreateLayout(generateFromDataset = false): void {
		const dialogRef = this.dialog.open(FormLayoutCreateDialogComponent, {
			width: '560px',
			data: {
				projectId: this.projectId,
				formModelId: this.formModelId,
				project: this.project,
				generateFromDataset
			}
		});

		dialogRef.afterClosed().subscribe((result: Layout | null) => {
			if(result) {
				this.formLayoutService.createLayout(this.projectId, this.formModelId, result).subscribe({
					next: created => {
						this.snackBar.open('Layout created', 'Close', {duration: 2000});
						this.layouts = [...this.layouts, created];
						this.layoutCreated.emit(created);
					},
					error: error => {
						console.error('Error creating layout:', error);
						this.snackBar.open('Failed to create layout', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onDeleteLayout(layout: Layout, event: Event): void {
		event.stopPropagation();
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '450px',
			data: {
				title: 'Delete Layout',
				message: `Are you sure you want to delete layout "${layout.id}"?`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});

		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed) {
				this.formLayoutService.deleteLayout(this.projectId, this.formModelId, layout.formLayoutId).subscribe({
					next: () => {
						this.snackBar.open('Layout deleted', 'Close', {duration: 2000});
						this.layouts = this.layouts.filter(l => l.formLayoutId !== layout.formLayoutId);
						this.layoutDeleted.emit(layout.formLayoutId);
					},
					error: error => {
						console.error('Error deleting layout:', error);
						this.snackBar.open('Failed to delete layout', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	getUsedFieldModelCount(layout: Layout): number {
		const ids = new Set<string>();
		for(const line of layout.lines) {
			for(const cell of line.cells) {
				if(cell.fieldModelId) {
					ids.add(cell.fieldModelId);
				}
			}
		}
		return ids.size;
	}

	getDatasetModelId(layout: Layout): string | null {
		return (layout.datasetModel as any)?.id ?? null;
	}
}
