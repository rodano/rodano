import {HttpClient, HttpResponse} from '@angular/common/http';
import {Directive, Input, HostListener, ElementRef, OnChanges, DestroyRef} from '@angular/core';
import {NotificationService} from '../services/notification.service';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {finalize} from 'rxjs';

@Directive({
	selector: '[appDownload]'
})
export class DownloadDirective implements OnChanges {
	@Input('appDownload') url: string | undefined;

	constructor(
		private http: HttpClient,
		private elementRef: ElementRef<HTMLButtonElement>,
		private destroyRef: DestroyRef,
		private notificationService: NotificationService
	) {}

	ngOnChanges() {
		//this must happen in the next tick, to let Angular material handle the disable mapping
		setTimeout(() => this.toggleDisabled(!this.url), 0);
	}

	toggleDisabled(disabled: boolean) {
		if(disabled) {
			this.elementRef.nativeElement.setAttribute('disabled', 'true');
		}
		else {
			this.elementRef.nativeElement.removeAttribute('disabled');
		}
	}

	@HostListener('click', ['$event'])
	onClick(event: Event) {
		//nothing to do if no url is set
		//this should not happen as the button should be disabled
		if(!this.url) {
			return;
		}

		event.preventDefault();
		event.stopPropagation();

		//update button style
		const button = this.elementRef.nativeElement;
		this.toggleDisabled(true);
		button.classList.add('loading');

		this.http.get(this.url, {observe: 'response', responseType: 'blob'}).pipe(
			takeUntilDestroyed(this.destroyRef),
			finalize(() => {
				this.toggleDisabled(false);
				button.classList.remove('loading');
			})).subscribe({
			next: (response: HttpResponse<Blob>) => {
				const contentDisposition = response.headers.get('content-disposition');
				const contentType = response.headers.get('content-type');
				if(contentDisposition && contentType) {
					const filename = contentDisposition.substring(contentDisposition.lastIndexOf('=') + 1);
					const blob = response.body;
					if(blob) {
						const file = new File([blob], filename, {type: contentType, lastModified: Date.now()});
						const fileUrl = window.URL.createObjectURL(file);
						//Chrome and Safari do not support to set location href
						if(/Chrome|Safari/.test(navigator.userAgent)) {
							const link = document.createElement('a');
							link.setAttribute('href', fileUrl);
							link.setAttribute('download', filename);

							const event = new MouseEvent('click');
							link.dispatchEvent(event);
						}
						else {
							location.href = fileUrl;
						}
						//revoke url after event has been dispatched
						setTimeout(() => {
							window.URL.revokeObjectURL(fileUrl);
						}, 0);
					}
				}
			},
			error: error => {
				this.notificationService.showError(`Error getting file: ${error}`);
			}
		});
	}
}
