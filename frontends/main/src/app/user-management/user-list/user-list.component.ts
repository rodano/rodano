import {Component, OnInit} from '@angular/core';
import {MatDialog, MatDialogModule} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {UserManagement} from '@core/model/user-management';
import {UserManagementService} from '@core/services/user-management.service';
import {UserDialogComponent} from '../user-dialog/user-dialog.component';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatMenu, MatMenuItem, MatMenuTrigger} from '@angular/material/menu';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatTooltip} from '@angular/material/tooltip';
import {Router} from '@angular/router';
import {ConfirmationDialogComponent} from '../../confirmation-dialog/confirmation-dialog.component';

@Component({
	selector: 'app-user-list',
	standalone: true,
	templateUrl: './user-list.component.html',
	styleUrls: ['./user-list.component.css'],
	imports: [
		CommonModule,
		FormsModule,
		MatButtonModule,
		MatIconModule,
		MatDialogModule,
		MatMenuItem,
		MatProgressSpinner,
		MatMenuTrigger,
		MatMenu,
		MatTooltip
	]
})
export class UserListComponent implements OnInit {
	users: UserManagement[] = [];
	filteredUsers: UserManagement[] = [];
	searchTerm = '';
	loading = false;

	filters = {
		showActive: true,
		showDeleted: false,
		showActivated: true,
		showPending: true,
		showSuperusers: true,
		showRegularUsers: true,
		languageId: '',
		createdAfter: null as string | null,
		createdBefore: null as string | null,
		loginAfter: null as string | null,
		loginBefore: null as string | null
	};

	sortBy: 'name' | 'nameDesc' | 'emailAsc' | 'emailDesc' | 'createdNewest' | 'createdOldest' | 'loginNewest' | 'loginOldest' = 'name';

	constructor(
		private userManagementService: UserManagementService,
		private dialog: MatDialog,
		private snackBar: MatSnackBar,
		private router: Router
	) {}

	ngOnInit(): void {
		this.loadUsers();
	}

	loadUsers(): void {
		this.loading = true;
		this.userManagementService.getAllUsers().subscribe({
			next: users => {
				this.users = users;
				this.applyFilters();
				this.loading = false;
			},
			error: error => {
				console.error('Error loading users:', error);
				this.snackBar.open('Failed to load users', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	applyFilters(): void {
		let filtered = [...this.users];

		if(this.searchTerm.trim()) {
			const term = this.searchTerm.toLowerCase();
			filtered = filtered.filter(user =>
				(user.name?.toLowerCase() || '').includes(term)
				|| (user.email?.toLowerCase() || '').includes(term)
			);
		}

		filtered = filtered.filter(user => {
			if(user.isDeleted && !this.filters.showDeleted) {
				return false;
			}
			if(!user.isDeleted && !this.filters.showActive) {
				return false;
			}
			return true;
		});

		filtered = filtered.filter(user => {
			if(user.isActivated && !this.filters.showActivated) {
				return false;
			}
			if(!user.isActivated && !this.filters.showPending) {
				return false;
			}
			return true;
		});

		filtered = filtered.filter(user => {
			if(user.isSuperuser && !this.filters.showSuperusers) {
				return false;
			}
			if(!user.isSuperuser && !this.filters.showRegularUsers) {
				return false;
			}
			return true;
		});

		if(this.filters.languageId) {
			filtered = filtered.filter(user => user.languageId === this.filters.languageId);
		}

		if(this.filters.createdAfter) {
			filtered = filtered.filter(user => {
				if(!user.creationTime) {
					return false;
				}
				const createdDate = new Date(user.creationTime);
				return createdDate >= new Date(this.filters.createdAfter!);
			});
		}
		if(this.filters.createdBefore) {
			filtered = filtered.filter(user => {
				if(!user.creationTime) {
					return false;
				}
				const createdDate = new Date(user.creationTime);
				return createdDate <= new Date(this.filters.createdBefore!);
			});
		}

		if(this.filters.loginAfter) {
			filtered = filtered.filter(user => {
				if(!user.loginDate) {
					return false;
				}
				const loginDate = new Date(user.loginDate);
				return loginDate >= new Date(this.filters.loginAfter!);
			});
		}
		if(this.filters.loginBefore) {
			filtered = filtered.filter(user => {
				if(!user.loginDate) {
					return false;
				}
				const loginDate = new Date(user.loginDate);
				return loginDate <= new Date(this.filters.loginBefore!);
			});
		}

		filtered.sort((a, b) => {
			if(a.isSuperuser && !b.isSuperuser) {
				return -1;
			}
			if(!a.isSuperuser && b.isSuperuser) {
				return 1;
			}

			switch(this.sortBy) {
				case 'name':
					return (a.name || '').localeCompare(b.name || '');
				case 'nameDesc':
					return (b.name || '').localeCompare(a.name || '');
				case 'emailAsc':
					return (a.email || '').localeCompare(b.email || '');
				case 'emailDesc':
					return (b.email || '').localeCompare(a.email || '');
				case 'createdNewest':
					return this.getTimestamp(b.creationTime) - this.getTimestamp(a.creationTime);
				case 'createdOldest':
					return this.getTimestamp(a.creationTime) - this.getTimestamp(b.creationTime);
				case 'loginNewest':
					return this.getTimestamp(b.loginDate) - this.getTimestamp(a.loginDate);
				case 'loginOldest':
					return this.getTimestamp(a.loginDate) - this.getTimestamp(b.loginDate);
				default:
					return 0;
			}
		});

		this.filteredUsers = filtered;
	}

	clearFilters(): void {
		this.searchTerm = '';
		this.filters = {
			showActive: true,
			showDeleted: false,
			showActivated: true,
			showPending: true,
			showSuperusers: true,
			showRegularUsers: true,
			languageId: '',
			createdAfter: null,
			createdBefore: null,
			loginAfter: null,
			loginBefore: null
		};
		this.sortBy = 'name';
		this.applyFilters();
	}

	hasActiveFilters(): boolean {
		return this.searchTerm.trim() !== '' || !this.filters.showActive || this.filters.showDeleted || !this.filters.showActivated || !this.filters.showPending || !this.filters.showSuperusers || !this.filters.showRegularUsers || this.filters.languageId !== '' || this.filters.createdAfter !== null || this.filters.createdBefore !== null || this.filters.loginAfter !== null || this.filters.loginBefore !== null;
	}

	navigateToProjects(): void {
		this.router.navigate(['/projects']);
	}

	openCreateDialog(): void {
		const dialogRef = this.dialog.open(UserDialogComponent, {
			width: '600px',
			maxWidth: '90wh',
			data: {mode: 'create'}
		});

		dialogRef.afterClosed().subscribe(result => {
			if(result) {
				this.loadUsers();
				this.snackBar.open('User created successfully', 'Close', {duration: 3000});
			}
		});
	}

	openEditDialog(user: UserManagement): void {
		const dialogRef = this.dialog.open(UserDialogComponent, {
			width: '600px',
			maxWidth: '90wh',
			data: {mode: 'edit', user}
		});

		dialogRef.afterClosed().subscribe(result => {
			if(result) {
				this.loadUsers();
				this.snackBar.open('User updated successfully', 'Close', {duration: 3000});
			}
		});
	}

	deleteUser(user: UserManagement): void {
		if(!user.name) {
			console.error('Cannot delete user: missing name');
			return;
		}

		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			maxWidth: '90vw',
			data: {
				title: 'Delete User',
				message: `Are you sure you want to delete user "${user.name}"? This action can be undone by restoring the user.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});

		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.userManagementService.deleteUser(user.pk).subscribe({
					next: () => {
						this.loadUsers();
						this.snackBar.open('User deleted successfully', 'Close', {duration: 3000});
					},
					error: error => {
						console.error('Error deleting user:', error);
						this.snackBar.open('Failed to delete user', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	restoreUser(user: UserManagement): void {
		this.userManagementService.restoreUser(user.pk).subscribe({
			next: () => {
				this.loadUsers();
				this.snackBar.open('User restored successfully', 'Close', {duration: 3000});
			},
			error: error => {
				console.error('Error restoring user:', error);
				this.snackBar.open('Failed to restore user', 'Close', {duration: 3000});
			}
		});
	}

	toggleSuperuser(user: UserManagement): void {
		if(!user.name) {
			console.error('Cannot toggle superuser: missing name');
			return;
		}

		const action = user.isSuperuser ? 'revoke' : 'grant';

		if(action === 'revoke') {
			const superuserCount = this.users.filter(u => u.isSuperuser && !u.isDeleted).length;
			if(superuserCount <= 1) {
				this.snackBar.open(
					'Cannot revoke the last superuser. At least one superuser must remain.',
					'Close',
					{duration: 5000}
				);
				return;
			}
		}

		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			maxWidth: '90vw',
			data: {
				title: `${action === 'grant' ? 'Grant' : 'Revoke'} Superuser Status`,
				message: `Are you sure you want to ${action} superuser status for "${user.name}"?`,
				confirmText: action === 'grant' ? 'Grant' : 'Revoke',
				cancelText: 'Cancel',
				type: 'warning'
			}
		});

		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.userManagementService.toggleSuperuser(user.pk, !user.isSuperuser).subscribe({
					next: () => {
						this.loadUsers();
						this.snackBar.open(`Superuser status ${action}ed successfully`, 'Close', {duration: 3000});
					},
					error: error => {
						console.error('Error toggling superuser:', error);
						this.snackBar.open('Failed to toggle superuser status', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	unblockUser(user: UserManagement): void {
		if(!user.name) {
			console.error('Cannot unblock user: missing name');
			return;
		}

		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			maxWidth: '90vw',
			data: {
				title: 'Unblock User',
				message: `Are you sure you want to unblock user "${user.name}"? This will reset their failed login attempts and allow them to log in again.`,
				confirmText: 'Unblock',
				cancelText: 'Cancel',
				type: 'warning'
			}
		});

		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.userManagementService.unblockUser(user.pk).subscribe({
					next: () => {
						this.loadUsers();
						this.snackBar.open('User unblocked successfully', 'Close', {duration: 3000});
					},
					error: error => {
						console.error('Error unblocking user:', error);
						this.snackBar.open('Failed to unblock user', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	private getTimestamp(date: Date | undefined): number {
		return date ? new Date(date).getTime() : 0;
	}
}
