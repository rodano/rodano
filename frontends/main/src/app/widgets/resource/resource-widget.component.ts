import {ChangeDetectionStrategy, Component, OnInit, input, signal} from '@angular/core';
import {Resource} from '@core/model/resource';
import {ResourceService} from '@core/services/resource.service';
import {Expandable} from '@core/utilities/expandable';
import {ResourceSearch} from '@core/utilities/search/resource-search';
import {MatButton, MatIconButton} from '@angular/material/button';
import {DownloadDirective} from '../../directives/download.component';
import {DateUTCPipe} from '../../pipes/date-utc.pipe';
import {MatIcon} from '@angular/material/icon';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
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
	readonly category = input.required<string>();

	readonly resources = signal<(Resource & Expandable)[]>([]);

	constructor(
		private resourceService: ResourceService
	) {}

	ngOnInit(): void {
		const search = new ResourceSearch();
		search.categoryId = this.category();
		search.sortBy = 'creationTime';
		search.orderAscending = false;

		this.resourceService.search(search).subscribe(resources => {
			this.resources.set(resources.objects.map(resource => {
				return {...resource, expanded: false};
			}));
		});
	}

	showMoreLess(resource: (Resource & Expandable)) {
		resource.expanded = !resource.expanded;
	}

	getResourceFileUrl(resource: Resource) {
		return this.resourceService.getFileUrl(resource);
	}
}
