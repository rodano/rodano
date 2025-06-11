import {Component, DestroyRef, OnInit} from '@angular/core';
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';
import {ConfigurationService} from '@core/services/configuration.service';
import {ScopeModelDTO} from '@core/model/scope-model-dto';
import {MatButton} from '@angular/material/button';
import {MatFormField, MatLabel} from '@angular/material/input';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatCheckbox} from '@angular/material/checkbox';
import {DownloadDirective} from '../directives/download.component';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {DocumentationService} from '@core/services/documentation.service';
import {MatOption, MatSelect} from '@angular/material/select';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {ScopeDTO} from '@core/model/scope-dto';
import {forkJoin, interval, skipWhile, switchMap} from 'rxjs';
import {ScopePickerComponent} from '../scope-picker/scope-picker.component';
import {MeService} from '@core/services/me.service';
import {FeatureStatic} from '@core/model/feature-static';
import {ScopeMiniDTO} from '@core/model/scope-mini-dto';
import {CRFDocumentationGenerationStatus} from '@core/model/crf-documentation-generation-status';
import {ScopeFinderComponent} from '../scope-finder/scope-finder.component';

@Component({
	templateUrl: './documentation.component.html',
	styleUrls: ['./documentation.component.css'],
	imports: [
		MatButton,
		MatLabel,
		ReactiveFormsModule,
		MatFormField,
		MatSelect,
		MatOption,
		MatCheckbox,
		DownloadDirective,
		LocalizeMapPipe,
		MatAutocompleteModule,
		ScopePickerComponent,
		ScopeFinderComponent
	]
})
export class DocumentationComponent implements OnInit {
	crfDocumentationGenerationStatus: CRFDocumentationGenerationStatus;

	scopeModels: ScopeModelDTO[] = [];
	rootScopes: ScopeMiniDTO[] = [];
	scopes: ScopeDTO[] = [];

	generationStatus: CRFDocumentationGenerationStatus;

	blankCrfForm = new FormGroup({
		scopeModelId: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
		annotated: new FormControl(false, {nonNullable: true})
	});

	archiveOneCrfForm = new FormGroup({
		scopePk: new FormControl<number | null>(null, {validators: [Validators.required]}),
		withAuditTrails: new FormControl(false, {nonNullable: true})
	});

	archiveMultipleCrfForm = new FormGroup({
		scopeModelId: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
		rootScopePk: new FormControl<number | null>(null, {validators: [Validators.required]}),
		withAuditTrails: new FormControl(false, {nonNullable: true})
	});

	constructor(
		private configurationService: ConfigurationService,
		private meService: MeService,
		private documentationService: DocumentationService,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		forkJoin({
			rootScopes: this.meService.getScopes(FeatureStatic.DOCUMENTATION, true, false),
			scopeModels: this.configurationService.getScopeModelsSorted()
		}).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(({rootScopes, scopeModels}) => {
			this.rootScopes = rootScopes;
			this.archiveMultipleCrfForm.get('rootScopePk')?.setValue(this.rootScopes[0].pk);
			this.scopeModels = scopeModels;
			const leafScopeModel = this.scopeModels[this.scopeModels.length - 1];
			this.blankCrfForm.get('scopeModelId')?.setValue(leafScopeModel.id);
			this.archiveMultipleCrfForm.get('scopeModelId')?.setValue(leafScopeModel.id);
		});

		this.documentationService.getArchiveCrfStatus().pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(s => this.generationStatus = s);

		interval(1000).pipe(
			takeUntilDestroyed(this.destroyRef),
			skipWhile(() => this.generationStatus !== CRFDocumentationGenerationStatus.IN_PROGRESS),
			switchMap(() => this.documentationService.getArchiveCrfStatus())
		).subscribe(s => this.generationStatus = s);
	}

	getScopes(modelId: string): ScopeDTO[] {
		return this.scopes?.filter(s => s.modelId === modelId) ?? [];
	}

	get dataStructureUrl(): string {
		return this.documentationService.getDataStructureUrl();
	}

	get blankCrfUrl(): string {
		const scopeModelId = this.blankCrfForm.get('scopeModelId')?.value ?? this.scopeModels[0].id;
		const annotated = this.blankCrfForm.get('annotated')?.value ?? false;
		return this.documentationService.getBlankCrfUrl(scopeModelId, annotated);
	}

	get archiveOneCrfUrl(): string | undefined {
		const scopePk = this.archiveOneCrfForm.get('scopePk')?.value as number;
		const withAuditTrails = this.archiveOneCrfForm.get('withAuditTrails')?.value ?? false;
		return this.documentationService.getArchiveOneCrfUrl(scopePk, withAuditTrails);
	}

	get archiveMultipleCrfUrl(): string {
		return this.documentationService.getArchiveMultipleCrfUrl();
	}

	generateArchive() {
		const rootScopePk = this.archiveMultipleCrfForm.get('rootScopePk')?.value as number;
		const scopeModelId = this.archiveMultipleCrfForm.get('scopeModelId')?.value ?? this.scopeModels[0].id;
		const withAuditTrails = this.archiveMultipleCrfForm.get('withAuditTrails')?.value ?? false;

		this.documentationService.archiveCrfRequest([rootScopePk], scopeModelId, withAuditTrails)
			.pipe(
				takeUntilDestroyed(this.destroyRef)
			).subscribe(() => this.generationStatus = CRFDocumentationGenerationStatus.IN_PROGRESS);
	}
}
