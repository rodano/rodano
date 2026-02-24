import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {LanguageService} from '../../services/language.service';
import {Validator} from '@core/model/validator';
import {HttpErrorResponse} from '@angular/common/http';
import {ValidatorManagerService} from '../../services/manager/validator-manager.service';
import {ProjectLanguage} from '@core/model/project-language';
import {forkJoin, of, Subscription} from 'rxjs';
import {MatTooltip} from '@angular/material/tooltip';
import {ValidatorDialogService} from '../../services/dialogs/validator-dialog.service';
import {ValidatorDetailComponent} from '../validator-detail/validator-detail.component';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';

@Component({
	selector: 'app-validators-list',
	standalone: true,
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatProgressSpinnerModule,
		MatSnackBarModule,
		MatTooltip,
		ValidatorDetailComponent,
		EmptyStateComponent
	],
	templateUrl: './validators-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class ValidatorsListComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string | null>();
	@Output() validatorsChanged = new EventEmitter<{modificationCount: number}>();
	@Output() validatorContextChanged = new EventEmitter<{
		validators: any[];
		selectedValidatorId: string | null;
	}>();

	selectedValidator: Validator | null = null;
	viewMode = 'detail';
	loading = false;

	projectLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';
	private languageSubscription: Subscription;

	constructor(
		public validatorManager: ValidatorManagerService,
		public languageService: LanguageService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private validatorDialogService: ValidatorDialogService,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadValidators();

		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnChanges(changes: any): void {
		if(changes['selectedNode'] && this.validators.length > 0) {
			const nodeId = this.selectedNode;
			if(nodeId?.startsWith('validator-')) {
				const validatorId = nodeId.replace('validator-', '');
				const validator = this.validators.find(v => v.validatorId === validatorId);
				if(validator) {
					this.selectedValidator = validator;
				}
			}
			else if(nodeId === 'validators') {
				this.selectedValidator = null;
			}
		}
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	get viewLevel(): number {
		if(!this.selectedValidator) {
			return 0;
		}
		return 2;
	}

	get validators(): Validator[] {
		return this.validatorManager.getAll();
	}

	get modifiedValidatorIds(): Set<string> {
		return this.validatorManager.getModifiedIds();
	}

	get originalValidators(): Validator[] {
		return this.validatorManager.getOriginals();
	}

	get totalModificationCount(): number {
		return this.validatorManager.getModificationCount();
	}

	loadValidators(): void {
		this.loading = true;
		forkJoin({
			validators: this.validatorManager.load(this.projectId),
			workflowStates: this.workflowStateManager.isLoaded()
				? of(null)
				: this.workflowStateManager.load(this.projectId)
		}).subscribe({
			next: ({validators}) => {
				if(this.selectedValidator) {
					this.selectedValidator = validators.find(
						v => v.validatorId === this.selectedValidator!.validatorId
					) || null;
				}
				this.loading = false;
				this.emitContext();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error loading validators:', error);
				this.snackBar.open('Failed to load validators', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	onSelectValidator(validator: Validator): void {
		if(this.selectedValidator?.validatorId === validator.validatorId) {
			this.clearSelection();
		}
		else {
			this.selectValidator(validator);
		}
		this.emitContext();
	}

	clearSelection(): void {
		this.selectedValidator = null;
		this.viewMode = 'detail';
		this.nodeSelected.emit('validators');
	}

	private selectValidator(validator: Validator): void {
		const previousValidatorId = this.selectedValidator?.validatorId;
		this.selectedValidator = validator;

		if(previousValidatorId !== validator.validatorId) {
			this.viewMode = 'detail';
		}
		this.emitContext();
		this.nodeSelected.emit(`validator-${validator.validatorId}`);
	}

	isSelected(validator: Validator): boolean {
		return this.selectedValidator?.validatorId === validator.validatorId;
	}

	onCreateValidator(): void {
		this.validatorDialogService.openCreateDialog(
			this.projectId,
			this.projectLanguages
		).subscribe((result: Validator | null) => {
			if(result) {
				this.validatorManager.create(this.projectId, result).subscribe({
					next: () => {
						this.snackBar.open('Validator created', 'Close', {duration: 2000});
						this.loadValidators();
						this.emitModificationChange();
					},
					error: error => {
						console.error('Error creating validator', error);
						this.snackBar.open('Failed to create validator', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onValidatorUpdated(updatedValidator: Validator): void {
		this.selectedValidator = this.validatorManager.getById(updatedValidator.validatorId) || null;
		this.emitModificationChange();
	}

	onValidatorDeleted(validatorId: string): void {
		const validator = this.validators.find(v => v.validatorId === validatorId);
		if(!validator) {
			return;
		}
		this.performDelete(validator);
	}

	private performDelete(validator: Validator): void {
		this.validatorManager.delete(this.projectId, validator.validatorId).subscribe({
			next: () => {
				this.snackBar.open('Validator deleted', 'Close', {duration: 2000});

				if(this.selectedValidator?.validatorId === validator.validatorId) {
					this.clearSelection();
				}

				this.loadValidators();
				this.emitModificationChange();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting validator', error);
				this.snackBar.open('Failed to delete validator', 'Close', {duration: 3000});
			}
		});
	}

	private emitModificationChange(): void {
		this.validatorsChanged.emit({modificationCount: this.totalModificationCount});
	}

	private emitContext(): void {
		this.validatorContextChanged.emit({
			validators: [...this.validators],
			selectedValidatorId: this.selectedValidator?.validatorId || null
		});
	}

	isModified(validatorId: string): boolean {
		return this.validatorManager.isModified(validatorId);
	}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}

	getWorkflowStateLabel(workflowStateId: string): string {
		return this.languageService.getLabelById(workflowStateId, id => this.workflowStateManager.getById(id));
	}
}
