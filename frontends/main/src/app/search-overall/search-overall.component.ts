import {Component, DestroyRef, OnInit} from '@angular/core';
import {Router, Routes} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {APIService} from '@core/services/api.service';
import {ScopeOverallSearchResult} from '@core/model/scope-overall-search-result';
import {PaginatedSearch} from '@core/utilities/search/paginated-search';
import {MatTableModule} from '@angular/material/table';
import {ConfigurationService} from '@core/services/configuration.service';
import {MeService} from '@core/services/me.service';
import {ScopeModel} from '@core/model/scope-model';

@Component({
	selector: 'app-search-overall',
	imports: [
		MatTableModule
	],
	templateUrl: './search-overall.component.html',
	styleUrl: './search-overall.component.css'
})
export class SearchOverallComponent implements OnInit {
	columnsToDisplay: any[] = [];

	leafScopeModel: ScopeModel = {} as ScopeModel;
	leafScopeModelParent: ScopeModel = {} as ScopeModel;

	static ROUTES: Routes = [
		{
			path: '',
			component: SearchOverallComponent
		}
	];

	scopeOverallSearchResult: ScopeOverallSearchResult[] = [];

	constructor(
		private configurationService: ConfigurationService,
		private meService: MeService,
		private router: Router,
		private destroyRef: DestroyRef,
		private http: HttpClient,
		private apiService: APIService
	) {}

	ngOnInit(): void {
		const payload = {query: 'test', page: 1, pageSize: 10};
		this.http.post<PaginatedSearch>(`${this.apiService.getApiUrl()}/extended-search`, payload).subscribe({
			next: (data: PaginatedSearch) => {
				console.log('Overall search results:', data);
			},
			error: error => {
				console.error('Error fetching overall search results:', error);
			}
		});
	}
}
