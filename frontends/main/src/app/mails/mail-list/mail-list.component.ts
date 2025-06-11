import {Component, DestroyRef, OnInit, ViewChild} from '@angular/core';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {MatDialog} from '@angular/material/dialog';
import {startWith, switchMap} from 'rxjs/operators';
import {Subject, merge} from 'rxjs';
import {MailDTO} from '@core/model/mail-dto';
import {PagedResultMailDTO} from '@core/model/paged-result-mail-dto';
import {MailsService} from '@core/services/mails.service';
import {MailSearch} from '@core/utilities/search/mail-search';
import {MailDetailComponent} from '../mail-detail/mail-detail.component';
import {YesNoPipe} from '../../pipes/yes-no.pipe';
import {ShortenStringPipe} from '../../pipes/shorten-string.pipe';
import {CapitalizeFirstPipe} from '../../pipes/capitalize-first.pipe';
import {DownloadDirective} from '../../directives/download.component';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {MatDivider} from '@angular/material/divider';
import {MatTableModule} from '@angular/material/table';
import {MatButton} from '@angular/material/button';
import {MatOption} from '@angular/material/core';
import {KeyValuePipe} from '@angular/common';
import {MatSort, MatSortHeader} from '@angular/material/sort';
import {MatSelect} from '@angular/material/select';
import {MatInputModule} from '@angular/material/input';
import {MatFormField} from '@angular/material/form-field';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MatExpansionModule} from '@angular/material/expansion';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {EMPTY_PAGED_RESULT} from '@core/utilities/empty-paged-result';
import {MatCheckbox} from '@angular/material/checkbox';
import {NotificationService} from 'src/app/services/notification.service';
import {DateTimeUTCPipe} from '../../pipes/date-time-utc.pipe';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MailOrigin} from '@core/model/mail-origin';
import {MailStatus} from '@core/model/mail-status';
import {PaginatedSearch} from '@core/utilities/search/paginated-search';

@Component({
	selector: 'app-mail-list',
	templateUrl: './mail-list.component.html',
	styleUrls: ['./mail-list.component.css'],
	imports: [
		MatExpansionModule,
		ReactiveFormsModule,
		MatFormField,
		MatSelect,
		MatOption,
		MatCheckbox,
		MatButton,
		MatTableModule,
		MatDivider,
		MatToolbar,
		MatToolbarRow,
		MatSort,
		MatSortHeader,
		DownloadDirective,
		MatPaginatorModule,
		KeyValuePipe,
		CapitalizeFirstPipe,
		ShortenStringPipe,
		YesNoPipe,
		DateTimeUTCPipe,
		MatInputModule,
		MatDatepickerModule
	]
})
export class MailListComponent implements OnInit {
	mailOrigin = MailOrigin;
	mailStatus = MailStatus;
	exportUrl: string;

	searchForm = new FormGroup({
		fullText: new FormControl('', {nonNullable: true}),
		recipient: new FormControl(''),
		sender: new FormControl(''),
		afterDate: new FormControl<Date | undefined>(undefined),
		beforeDate: new FormControl<Date | undefined>(undefined),
		status: new FormControl(''),
		intent: new FormControl(''),
		origin: new FormControl('')
	});

	refreshSearch$ = new Subject<void>();

	mails: PagedResultMailDTO = EMPTY_PAGED_RESULT;
	columnsToDisplay: string[] = [
		'selected',
		'status',
		'creationTime',
		'sentTime',
		'recipients',
		'attachments',
		'subject',
		'actions'
	];

	selectedMails: MailDTO[] = [];

	@ViewChild(MatSort, {static: true}) sort: MatSort;
	@ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

	constructor(
		private mailsService: MailsService,
		private dialog: MatDialog,
		private destroyRef: DestroyRef,
		private notificationService: NotificationService
	) {}

	ngOnInit() {
		this.sort.active = MailSearch.DEFAULT_SORT_BY;
		this.sort.direction = PaginatedSearch.getSortDirection(MailSearch.DEFAULT_SORT_ASCENDING);

		merge(
			this.refreshSearch$.asObservable(),
			this.paginator.page,
			this.sort.sortChange
		).pipe(
			takeUntilDestroyed(this.destroyRef),
			startWith({}),
			switchMap(() => {
				const search = new MailSearch();
				Object.assign(search, this.searchForm.value);
				search.sortBy = this.sort.active;
				search.orderAscending = PaginatedSearch.getOrderAscending(this.sort.direction);
				search.pageIndex = this.paginator.pageIndex;
				this.exportUrl = this.mailsService.getExportUrl(search);
				return this.mailsService.search(search);
			})
		).subscribe(m => this.mails = m);
	}

	search() {
		//do not user Paginator:firstPage() as it will trigger a search
		this.paginator.pageIndex = 0;
		this.refreshSearch$.next();
	}

	reset() {
		this.searchForm.reset();
		//do not user Paginator:firstPage() as it will trigger a search
		this.paginator.pageIndex = 0;
		this.refreshSearch$.next();
	}

	openDetails(mail: MailDTO) {
		return this.dialog
			.open(MailDetailComponent, {data: mail})
			.afterClosed();
	}

	onSelect(isSelected: boolean, mail: MailDTO) {
		if(isSelected) {
			this.selectedMails.push(mail);
		}
		else {
			this.selectedMails = this.selectedMails.filter(selectedMail => selectedMail !== mail);
		}
	}

	resendMails() {
		this.mailsService.resendMails(this.selectedMails).subscribe({
			next: () => {
				this.notificationService.showSuccess('Selected emails resent');
			},
			error: () => {
				this.notificationService.showError('Unable to send selected emails');
			}
		});
	}
}
