import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {forkJoin} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {HttpErrorResponse} from '@angular/common/http';
import {PrivacyPolicy} from '@core/model/privacy-policy';
import {PrivacyPolicyManagerService} from '../../services/manager/privacy-policy-manager.service';
import {PrivacyPolicyDetailComponent} from '../privacy-policy-detail/privacy-policy-detail.component';
import {PrivacyPolicyDialogService} from '../../services/dialogs/privacy-policy-dialog.service';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {BaseListComponent} from '../../shared/base-list.component';
import {ListHeaderComponent} from '../../shared/list-header/list-header.component';

@Component({
	selector: 'app-privacy-policy-list',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule,
		MatSnackBarModule, PrivacyPolicyDetailComponent, EmptyStateComponent, ListHeaderComponent],
	templateUrl: './privacy-policy-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class PrivacyPolicyListComponent
	extends BaseListComponent<PrivacyPolicy>
	implements OnInit, OnChanges, OnDestroy {
	@Input() override projectId = '';
	@Input() override project: ConfiguratorProject | null = null;
	@Input() override selectedNode: string | null = null;
	@Output() privacyPoliciesChanged = new EventEmitter<{modificationCount: number}>();
	@Output() privacyPolicyContextChanged = new EventEmitter<{
		privacyPolicies: any[];
		selectedPrivacyPolicyId: string | null;
	}>();

	constructor(
		public privacyPolicyManager: PrivacyPolicyManagerService,
		public override languageService: LanguageService,
		private privacyPolicyDialogService: PrivacyPolicyDialogService,
		snackBar: MatSnackBar
	) {
		super(privacyPolicyManager, languageService, snackBar);
	}

	getEntityId(pp: PrivacyPolicy): string {return pp.policyId;}
	getNodePrefix(): string {return 'privacyPolicy';}
	getListNodeName(): string {return 'privacyPolicies';}

	get privacyPolicies(): PrivacyPolicy[] {return this.privacyPolicyManager.getAll();}
	get selectedPrivacyPolicy(): PrivacyPolicy | null {return this.selected as PrivacyPolicy | null;}
	get modifiedPrivacyPolicyIds(): Set<string> {return this.privacyPolicyManager.getModifiedIds();}
	get originalPrivacyPolicies(): PrivacyPolicy[] {return this.privacyPolicyManager.getOriginals();}

	loadPrivacyPolicies(): void {this.load();}
	load(): void {
		this.loading = true;
		forkJoin({
			privacyPolicies: this.privacyPolicyManager.load(this.projectId)
		}).subscribe({
			next: ({privacyPolicies}) => this.afterLoad(privacyPolicies),
			error: (e: HttpErrorResponse) => this.handleLoadError(e, 'privacyPolicies')
		});
	}

	emitChangedEvent(count: number): void {
		this.privacyPoliciesChanged.emit({modificationCount: count});
	}

	emitContextEvent(): void {
		this.privacyPolicyContextChanged.emit({
			privacyPolicies: [...this.privacyPolicies],
			selectedPrivacyPolicyId: this.selected?.policyId || null
		});
	}

	onCreate(): void {
		this.privacyPolicyDialogService.openCreateDialog(this.projectId, this.projectLanguages)
			.subscribe((result: PrivacyPolicy | null) => {
				if(result) {
					this.privacyPolicyManager.create(this.projectId, result).subscribe({
						next: () => this.afterCreate('PrivacyPolicy'),
						error: e => {
							console.error(e);
							this.snackBar.open('Failed to create privacy policy', 'Close', {duration: 3000});
						}
					});
				}
			});
	}

	onUpdated(updated: PrivacyPolicy): void {
		this.selected = this.privacyPolicyManager.getById(updated.policyId) || null;
		this.emitModificationChange();
	}

	onDeleted(policyId: string): void {
		const privacyPolicy = this.privacyPolicies.find(pp => pp.policyId === policyId);
		if(!privacyPolicy) {
			return;
		}
		this.privacyPolicyManager.delete(this.projectId, privacyPolicy.policyId).subscribe({
			next: () => this.afterDelete(privacyPolicy, 'PrivacyPolicy'),
			error: (e: HttpErrorResponse) => {
				console.error(e);
				this.snackBar.open('Failed to delete privacy policy', 'Close', {duration: 3000});
			}
		});
	}

	onSelectPrivacyPolicy(pp: PrivacyPolicy): void {this.onSelect(pp);}
	onCreatePrivacyPolicy(): void {this.onCreate();}
	onPrivacyPolicyUpdated(pp: PrivacyPolicy): void {this.onUpdated(pp);}
	onPrivacyPolicyDeleted(id: string): void {this.onDeleted(id);}
}
