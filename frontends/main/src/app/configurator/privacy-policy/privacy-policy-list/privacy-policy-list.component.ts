import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {ProjectLanguage} from '@core/model/project-language';
import {forkJoin, Subscription} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {HttpErrorResponse} from '@angular/common/http';
import {PrivacyPolicy} from '@core/model/privacy-policy';
import {PrivacyPolicyManagerService} from '../../services/manager/privacy-policy-manager.service';
import {PrivacyPolicyDetailComponent} from '../privacy-policy-detail/privacy-policy-detail.component';
import {PrivacyPolicyDialogService} from '../../services/dialogs/privacy-policy-dialog.service';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';

@Component({
	selector: 'app-privacy-policy-list',
	standalone: true,
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatProgressSpinnerModule,
		MatSnackBarModule,
		PrivacyPolicyDetailComponent,
		EmptyStateComponent
	],
	templateUrl: './privacy-policy-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class PrivacyPolicyListComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string | null>();
	@Output() privacyPoliciesChanged = new EventEmitter<{modificationCount: number}>();
	@Output() privacyPolicyContextChanged = new EventEmitter<{
		privacyPolicies: any[];
		selectedPrivacyPolicyId: string | null;
	}>();

	selectedPrivacyPolicy: PrivacyPolicy | null = null;
	viewMode = 'detail';
	loading = false;

	projectLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';
	private languageSubscription: Subscription;

	constructor(
		public privacyPolicyManager: PrivacyPolicyManagerService,
		public languageService: LanguageService,
		private privacyPolicyDialogService: PrivacyPolicyDialogService,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadPrivacyPolicies();

		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnChanges(changes: any): void {
		if(changes['selectedNode'] && this.privacyPolicies.length > 0) {
			const nodeId = this.selectedNode;
			if(nodeId?.startsWith('privacy-policy-')) {
				const policyId = nodeId.replace('privacy-policy-', '');
				const privacyPolicy = this.privacyPolicies.find(pp => pp.policyId === policyId);
				if(privacyPolicy) {
					this.selectedPrivacyPolicy = privacyPolicy;
				}
			}
			else if(nodeId === 'privacy-policies') {
				this.selectedPrivacyPolicy = null;
			}
		}
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	get viewLevel(): number {
		if(!this.selectedPrivacyPolicy) {
			return 0;
		}
		return 2;
	}

	get privacyPolicies(): PrivacyPolicy[] {
		return this.privacyPolicyManager.getAll();
	}

	get modifiedPrivacyPolicyIds(): Set<string> {
		return this.privacyPolicyManager.getModifiedIds();
	}

	get originalPrivacyPolicies(): PrivacyPolicy[] {
		return this.privacyPolicyManager.getOriginals();
	}

	get totalModificationCount(): number {
		return this.privacyPolicyManager.getModificationCount();
	}

	loadPrivacyPolicies(): void {
		this.loading = true;
		forkJoin({
			privacyPolicies: this.privacyPolicyManager.load(this.projectId)
		}).subscribe({
			next: ({privacyPolicies}) => {
				if(this.selectedPrivacyPolicy) {
					this.selectedPrivacyPolicy = privacyPolicies.find(
						pp => pp.policyId === this.selectedPrivacyPolicy!.policyId
					) || null;
				}
				this.loading = false;
				this.emitContext();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error loading privacy policies:', error);
				this.snackBar.open('Failed to load privacy policies', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	onSelectPrivacyPolicy(privacyPolicy: PrivacyPolicy): void {
		if(this.selectedPrivacyPolicy?.policyId === privacyPolicy.policyId) {
			this.clearSelection();
		}
		else {
			this.selectPrivacyPolicy(privacyPolicy);
		}
		this.emitContext();
	}

	clearSelection(): void {
		this.selectedPrivacyPolicy = null;
		this.viewMode = 'detail';
		this.nodeSelected.emit('privacy-policy');
	}

	private selectPrivacyPolicy(privacyPolicy: PrivacyPolicy): void {
		const previousPrivacyPolicyId = this.selectedPrivacyPolicy?.policyId;
		this.selectedPrivacyPolicy = privacyPolicy;

		if(previousPrivacyPolicyId !== privacyPolicy.policyId) {
			this.viewMode = 'detail';
		}
		this.emitContext();
		this.nodeSelected.emit(`privacy-policy-${privacyPolicy.policyId}`);
	}

	isSelected(privacyPolicy: PrivacyPolicy): boolean {
		return this.selectedPrivacyPolicy?.policyId === privacyPolicy.policyId;
	}

	onCreatePrivacyPolicy(): void {
		this.privacyPolicyDialogService.openCreateDialog(
			this.projectId,
			this.projectLanguages
		).subscribe((result: PrivacyPolicy | null) => {
			if(result) {
				this.privacyPolicyManager.create(this.projectId, result).subscribe({
					next: () => {
						this.snackBar.open('Privacy policy created', 'Close', {duration: 2000});
						this.loadPrivacyPolicies();
						this.emitModificationChange();
					},
					error: error => {
						console.error('Error creating privacy policy', error);
						this.snackBar.open('Failed to create privacy policy', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onPrivacyPolicyUpdated(updatedPrivacyPolicy: PrivacyPolicy): void {
		this.selectedPrivacyPolicy = this.privacyPolicyManager.getById(updatedPrivacyPolicy.policyId) || null;
		this.emitModificationChange();
	}

	onPrivacyPolicyDeleted(policyId: string): void {
		const privacyPolicy = this.privacyPolicies.find(pp => pp.policyId === policyId);
		if(!privacyPolicy) {
			return;
		}
		this.performDelete(privacyPolicy);
	}

	private performDelete(privacyPolicy: PrivacyPolicy): void {
		this.privacyPolicyManager.delete(this.projectId, privacyPolicy.policyId).subscribe({
			next: () => {
				this.snackBar.open('Privacy Policy deleted', 'Close', {duration: 2000});

				if(this.selectedPrivacyPolicy?.policyId === privacyPolicy.policyId) {
					this.clearSelection();
				}

				this.loadPrivacyPolicies();
				this.emitModificationChange();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting privacy policy', error);
				this.snackBar.open('Failed to delete privacy policy', 'Close', {duration: 3000});
			}
		});
	}

	private emitModificationChange(): void {
		this.privacyPoliciesChanged.emit({modificationCount: this.totalModificationCount});
	}

	private emitContext(): void {
		this.privacyPolicyContextChanged.emit({
			privacyPolicies: [...this.privacyPolicies],
			selectedPrivacyPolicyId: this.selectedPrivacyPolicy?.policyId || null
		});
	}

	isModified(policyId: string): boolean {
		return this.privacyPolicyManager.isModified(policyId);
	}
}
