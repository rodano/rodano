import {Component, Input, DestroyRef} from '@angular/core';
import {FileService} from '@core/services/file.service';
import {tap, last, map} from 'rxjs/operators';
import {HttpEventType, HttpResponse} from '@angular/common/http';
import {trigger, state, style, transition, animate} from '@angular/animations';
import {NotificationService} from 'src/app/services/notification.service';
import {FileDTO} from '@core/model/file-dto';
import {MatProgressBar} from '@angular/material/progress-bar';
import {DownloadDirective} from '../../../directives/download.component';
import {MatIcon} from '@angular/material/icon';
import {MatButton, MatIconButton} from '@angular/material/button';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FieldUpdateService} from '../../services/field-update.service';
import {CRFField} from '../../models/crf-field';

@Component({
	selector: 'app-file-upload',
	templateUrl: './file-upload.component.html',
	styleUrls: ['../field/field.component.css', './file-upload.component.css'],
	animations: [trigger('fadeInOut', [state('in', style({opacity: 100})), transition('* => void', [animate(300, style({opacity: 0}))])])],
	imports: [
		MatButton,
		MatIconButton,
		MatIcon,
		DownloadDirective,
		MatProgressBar
	]
})
export class FileUploadComponent {
	@Input() field: CRFField;
	@Input() id: string;
	@Input() disabled: boolean;

	uploadInProgress = false;
	uploadProgress: number;
	animState: string | undefined;

	constructor(
		private fileService: FileService,
		private fieldUpdateService: FieldUpdateService,
		private notificationService: NotificationService,
		private destroyRef: DestroyRef
	) { }

	uploadFile(file: File) {
		if(file.size > 15728640) {
			this.notificationService.showError('File must be smaller than 15MB');
			return;
		}
		if(file.name.length > 255) {
			this.notificationService.showError('File name must be shorter than 255 characters');
			return;
		}

		if(this.field.model.maxLength && file.name.length > this.field.model.maxLength) {
			this.notificationService.showError(`File name must be shorter than ${this.field.model.maxLength} characters`);
			return;
		}

		this.uploadInProgress = true;
		this.animState = 'in';

		this.fileService.upload(this.field.scopePk, file, this.field.eventPk).pipe(
			takeUntilDestroyed(this.destroyRef),
			tap(event => {
				if(event.type === HttpEventType.UploadProgress && event.total) {
					this.uploadProgress = Math.round((event.loaded * 100) / event.total);
				}
			}),
			last(),
			map(f => f as HttpResponse<FileDTO>)
		).subscribe({
			next: response => {
				const value = response.body?.uniqueName ?? '';
				this.fieldUpdateService.updateField(this.field, value, value);
				this.field.filePk = response.body?.pk;
				this.field.fileName = response.body?.name;
				this.notificationService.showSuccess('File uploaded');
			},
			error: () => {
				this.notificationService.showError('Error occurred while uploading the file');
			}
		}).add(() => {
			this.uploadInProgress = false;
			this.animState = undefined;
		});
	}

	onClick() {
		const nativeInput = document.getElementById(this.id) as HTMLInputElement;
		nativeInput.value = '';

		nativeInput.onchange = () => {
			const files = nativeInput.files;

			if(files) {
				if(files.length !== 1) {
					this.notificationService.showError('Only one file at a time is allowed');
					return;
				}

				const file = files[0];
				this.uploadFile(file);
			}
		};

		nativeInput.click();
	}

	removeFile() {
		this.field.value = undefined;
	}

	getFileUrl(): string {
		return this.fileService.getUrl(this.field.filePk as number);
	}

	dragOverHandler(ev: DragEvent) {
		console.log('File in drop zone');
		//Prevent default behavior (Prevent file from being opened)
		ev.preventDefault();
	}

	dropHandler(ev: DragEvent) {
		console.log('Files dropped');

		//Prevent default behavior (Prevent file from being opened)
		ev.preventDefault();

		let file: File | null = null;

		if(ev.dataTransfer?.items) {
			//Use DataTransferItemList interface to access the file(s)
			if(ev.dataTransfer.items.length === 1 && ev.dataTransfer.items[0].kind === 'file') {
				file = ev.dataTransfer.items[0].getAsFile();
			}
		}
		else {
			//Use DataTransfer interface to access the file(s)
			if(ev.dataTransfer?.files.length === 1) {
				file = ev.dataTransfer.files[0];
			}
		}

		if(file) {
			this.uploadFile(file);
		}
	}
}
