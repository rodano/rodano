import {Component, ElementRef, OnDestroy, OnInit, inject, signal, viewChild} from '@angular/core';
import {Location} from '@angular/common';
import {Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {MatToolbar} from '@angular/material/toolbar';
import {MatIcon} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';
import {MatProgressBar} from '@angular/material/progress-bar';
import {MatDialog} from '@angular/material/dialog';
import QrScanner from 'qr-scanner';
import {AuthStateService} from '../../../services/auth-state.service';
import {ConfirmDialogComponent, ConfirmDialogData} from '../../../dialogs/confirm/confirm.dialog';

@Component({
	templateUrl: './login-qrcode.component.html',
	styleUrls: ['./login-qrcode.component.css'],
	imports: [MatToolbar, MatIcon, MatIconButton, MatProgressBar]
})
export class LoginQrcodeComponent implements OnInit, OnDestroy {
	private router = inject(Router);
	private location = inject(Location);
	private authStateService = inject(AuthStateService);
	private dialog = inject(MatDialog);

	readonly video = viewChild.required<ElementRef<HTMLVideoElement>>('video');

	readonly loading = signal(false);

	private qrScanner: QrScanner;

	ngOnInit() {
		this.qrScanner = new QrScanner(this.video().nativeElement, result => this.onScanSuccess(result.data), {});
		this.qrScanner.start();
	}

	back() {
		this.location.back();
	}

	private onScanSuccess(authURL: string) {
		if(this.loading()) {
			return;
		}

		this.qrScanner.stop();
		this.loading.set(true);

		if(window.navigator.vibrate) {
			window.navigator.vibrate(200);
		}

		const code = new URL(authURL).searchParams.get('code') as string;

		this.authStateService.robotLogin(code).pipe(
			finalize(() => this.loading.set(false))
		).subscribe({
			next: () => {
				this.qrScanner.destroy();
				this.router.navigate(['/main/surveys']);
			},
			error: response => {
				const data: ConfirmDialogData = response.status === 400
					? {title: 'Invalid code', message: 'Ask for a new invitation'}
					: {title: 'Error', message: 'Please, try again in a few minutes'};
				this.dialog.open(ConfirmDialogComponent, {data})
					.afterClosed()
					.subscribe(() => this.qrScanner.start());
			}
		});
	}

	ngOnDestroy() {
		if(this.qrScanner) {
			this.qrScanner.stop();
			this.qrScanner.destroy();
		}
	}
}
