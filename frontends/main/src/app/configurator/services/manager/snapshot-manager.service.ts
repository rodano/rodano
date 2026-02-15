import {Injectable} from '@angular/core';
import {ConfiguratorService} from '../api/configurator.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Observable} from 'rxjs';
import {
	CreateSnapshotDialogComponent
} from '../../dialogs/base/create-snapshot-dialog/create-snapshot-dialog.component';
import {SnapshotsListDialogComponent} from '../../dialogs/base/snapshots-list-dialog/snapshots-list-dialog.component';
import {ConfiguratorProject} from '@core/model/configurator-project';

@Injectable({providedIn: 'root'})
export class SnapshotManagerService {
	constructor(
		private configuratorService: ConfiguratorService,
		private dialog: MatDialog,
		private snackBar: MatSnackBar
	) {}

	createSnapshot(projectId: string, versionId: number): Observable<void> {
		return new Observable(observer => {
			const dialogRef = this.dialog.open(CreateSnapshotDialogComponent, {
				width: '500px'
			});

			dialogRef.afterClosed().subscribe(summary => {
				if(summary) {
					this.configuratorService.createSnapshot(projectId, versionId, summary).subscribe({
						next: () => {
							this.snackBar.open('Snapshot created', 'Close', {duration: 2000});
							observer.next();
							observer.complete();
						},
						error: error => {
							console.error('Error creating snapshot:', error);
							this.snackBar.open('Failed to create snapshot', 'Close', {duration: 3000});
							observer.error(error);
						}
					});
				}
				else {
					observer.complete();
				}
			});
		});
	}

	viewSnapshots(projectId: string, versionId: number): Observable<number | null> {
		return new Observable(observer => {
			this.configuratorService.getSnapshots(projectId, versionId).subscribe({
				next: snapshots => {
					const dialogRef = this.dialog.open(SnapshotsListDialogComponent, {
						width: '500px',
						data: {snapshots}
					});

					dialogRef.afterClosed().subscribe(result => {
						if(result?.action === 'restore') {
							observer.next(result.index);
						}
						else {
							observer.next(null);
						}
						observer.complete();
					});
				},
				error: error => {
					console.error('Error loading snapshots:', error);
					this.snackBar.open('Failed to load snapshots', 'Close', {duration: 3000});
					observer.error(error);
				}
			});
		});
	}

	restoreToSnapshot(projectId: string, versionId: number, targetIndex: number): Observable<ConfiguratorProject> {
		return new Observable(observer => {
			this.configuratorService.getSnapshots(projectId, versionId).subscribe({
				next: snapshots => {
					const currentIndex = snapshots.currentIndex;

					if(currentIndex === undefined) {
						this.snackBar.open('Invalid snapshot state', 'Close', {duration: 3000});
						observer.error(new Error('Invalid snapshot state'));
						return;
					}

					if(targetIndex < currentIndex) {
						const stepsBack = currentIndex - targetIndex;
						this.performRollback(projectId, versionId, stepsBack, observer);
					}
					else if(targetIndex > currentIndex) {
						const stepsForward = targetIndex - currentIndex;
						this.performRollForward(projectId, versionId, stepsForward, observer);
					}
					else {
						observer.complete();
					}
				},
				error: error => observer.error(error)
			});
		});
	}

	private performRollback(projectId: string, versionId: number, steps: number, observer: any): void {
		if(steps <= 0) {
			observer.complete();
			return;
		}

		this.configuratorService.rollbackSnapshot(projectId, versionId).subscribe({
			next: updatedProject => {
				if(steps > 1) {
					this.performRollback(projectId, versionId, steps - 1, observer);
				}
				else {
					this.snackBar.open('Restored to snapshot', 'Close', {duration: 2000});
					observer.next(updatedProject);
					observer.complete();
				}
			},
			error: error => {
				console.error('Error rolling back:', error);
				this.snackBar.open('Failed to restore snapshot', 'Close', {duration: 3000});
				observer.error(error);
			}
		});
	}

	private performRollForward(projectId: string, versionId: number, steps: number, observer: any): void {
		if(steps <= 0) {
			observer.complete();
			return;
		}

		this.configuratorService.rollForwardSnapshot(projectId, versionId).subscribe({
			next: updatedProject => {
				if(steps > 1) {
					this.performRollForward(projectId, versionId, steps - 1, observer);
				}
				else {
					this.snackBar.open('Restored to snapshot', 'Close', {duration: 2000});
					observer.next(updatedProject);
					observer.complete();
				}
			},
			error: error => {
				console.error('Error rolling forward:', error);
				this.snackBar.open('Failed to restore snapshot', 'Close', {duration: 3000});
				observer.error(error);
			}
		});
	}

	getSnapshotState(projectId: string, versionId: number): Observable<{canRollback: boolean; canRollForward: boolean}> {
		return new Observable(observer => {
			this.configuratorService.getSnapshots(projectId, versionId).subscribe({
				next: snapshots => {
					observer.next({
						canRollback: snapshots.currentIndex !== undefined && snapshots.currentIndex > 0,
						canRollForward: snapshots.currentIndex !== undefined && snapshots.snapshots !== undefined && snapshots.currentIndex < snapshots.snapshots.length - 1
					});
					observer.complete();
				},
				error: () => {
					observer.next({canRollback: false, canRollForward: false});
					observer.complete();
				}
			});
		});
	}
}
