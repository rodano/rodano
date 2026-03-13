import {ChangeDetectionStrategy, Component, DestroyRef, Inject, OnInit, signal} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef, MatDialogModule} from '@angular/material/dialog';
import {filter, forkJoin, map, Observable, of, switchMap} from 'rxjs';
import {Resource} from '@core/model/resource';
import {ResourceSubmission} from '@core/model/resource-submission';
import {ResourceService} from '@core/services/resource.service';
import {NotificationService} from 'src/app/services/notification.service';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {MatIcon} from '@angular/material/icon';
import {MatButton} from '@angular/material/button';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {HttpResponse} from '@angular/common/http';
import {ConfigurationService} from '@core/services/configuration.service';
import {ResourceCategory} from '@core/model/resource-category';
import {LocalizeMapPipe} from 'src/app/pipes/localize-map.pipe';
import {MeService} from '@core/services/me.service';
import {ScopeMini} from '@core/model/scope-mini';
import {ScopePickerComponent} from 'src/app/scope-picker/scope-picker.component';
import {FeatureStatic} from '@core/model/feature-static';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	selector: 'app-modify-resource-dialog',
	templateUrl: './modify-resource-dialog.component.html',
	styleUrls: ['./modify-resource-dialog.component.css'],
	imports: [
		MatDialogModule,
		MatFormField,
		MatInput,
		ReactiveFormsModule,
		MatLabel,
		MatButton,
		MatIcon,
		MatSlideToggleModule,
		LocalizeMapPipe,
		ScopePickerComponent
	]
})
export class ModifyResourceDialogComponent implements OnInit {
	resourceForm = new FormGroup({
		title: new FormControl('', [Validators.required]),
		description: new FormControl(''),
		scopePk: new FormControl(0, [Validators.required]),
		publicResource: new FormControl(false, {nonNullable: true})
	});

	readonly newResource = signal(false);
	readonly scopes = signal<ScopeMini[]>([]);
	readonly category = signal<ResourceCategory | undefined>(undefined);
	rootScope?: ScopeMini;
	readonly fileToUpload = signal<File | null>(null);

	constructor(
		private dialogRef: MatDialogRef<ModifyResourceDialogComponent, Resource>,
		@Inject(MAT_DIALOG_DATA) public resource: Resource | ResourceSubmission,
		private configurationService: ConfigurationService,
		private meService: MeService,
		private resourceService: ResourceService,
		private notificationService: NotificationService,
		private destroyRef: DestroyRef
	) {
		this.newResource.set((resource as Resource).pk === undefined);
	}

	ngOnInit(): void {
		this.resourceForm.reset(this.resource);
		forkJoin({
			categories: this.configurationService.getResourceCategories(),
			scopes: this.meService.getScopes(FeatureStatic.MANAGE_RESOURCE, true, true)
		}).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(({scopes, categories}) => {
			this.category.set(categories.find(c => c.id === this.resource.categoryId) as ResourceCategory);
			this.scopes.set(scopes);
			this.rootScope = scopes[0];
		});
		(this.resourceForm.controls['publicResource'] as FormControl).valueChanges.subscribe(value => {
			const scopeControl = this.resourceForm.get('scopePk') as FormControl;
			if(value) {
				scopeControl.setValue(this.rootScope?.pk);
				scopeControl.disable();
			}
			else {
				scopeControl.enable();
			}
		});
	}

	openInput() {
		document.getElementById('fileInput')?.click();
	}

	handleFileInput(files: FileList) {
		//Handle file error message when files is more than 1
		this.fileToUpload.set(files.item(0));
	}

	uploadFile(resourcePk: number): Observable<Resource> {
		if(!this.fileToUpload()) {
			throw new Error('There is no file to upload');
		}
		return this.resourceService.uploadFile(resourcePk, this.fileToUpload()!).pipe(
			//HTTP event will emit multiple times but we are only interested when it is a final response
			filter((event): event is HttpResponse<Resource> => event instanceof HttpResponse),
			map((response: HttpResponse<Resource>) => response.body as Resource)
		);
	}

	save(): void {
		this.resource = Object.assign(this.resource, this.resourceForm.getRawValue()) as ResourceSubmission;
		const resource$ = this.newResource()
			? this.resourceService.create(this.resource as ResourceSubmission)
			: this.resourceService.save(this.resource as Resource);

		resource$.pipe(
			switchMap(resource => {
				return this.fileToUpload() ? this.uploadFile(resource.pk) : of(resource);
			})
		).subscribe({
			next: resource => this.dialogRef.close(resource),
			error: () => this.notificationService.showError('Could not save resource')
		});
	}
}
