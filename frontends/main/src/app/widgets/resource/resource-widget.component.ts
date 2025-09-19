import {Component, Input, OnInit} from '@angular/core';
import {Resource} from '@core/model/resource';
import {ResourceService} from '@core/services/resource.service';
import {Expandable} from '@core/utilities/expandable';
import {ResourceSearch} from '@core/utilities/search/resource-search';
import {MatButton, MatIconButton} from '@angular/material/button';
import {DownloadDirective} from 'src/app/directives/download.component';
import {DateUTCPipe} from '../../pipes/date-utc.pipe';
import {MatIcon} from '@angular/material/icon';

@Component({
	selector: 'app-resource-widget',
	templateUrl: './resource-widget.component.html',
	styleUrls: ['./resource-widget.component.css'],
	imports: [
		MatIconButton,
		MatButton,
		MatIcon,
		DownloadDirective,
		DateUTCPipe
	]
})
export class ResourceWidgetComponent implements OnInit {
	@Input({required: true}) category: string;

	resources: (Resource & Expandable)[];

	constructor(
		private resourceService: ResourceService
	) {}

	ngOnInit(): void {
		const search = new ResourceSearch();
		search.categoryId = this.category;
		search.sortBy = 'creationTime';
		search.orderAscending = false;

		this.resourceService.search(search).subscribe(resources => {
			this.resources = resources.objects.map(resource => {
				return {...resource, expanded: false};
			});
		});
	}

	showMoreLess(resource: (Resource & Expandable)) {
		resource.expanded = !resource.expanded;
	}

	getResourceFileUrl(resource: Resource) {
		return this.resourceService.getFileUrl(resource);
	}
}
