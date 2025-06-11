import {Component, DestroyRef, Input, OnInit} from '@angular/core';
import {CMSLayoutDTO} from '@core/model/cms-layout-dto';
import {CMSSectionDTO} from '@core/model/cms-section-dto';
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';
import {GenericWidgetComponent} from '../widgets/generic-widget/generic-widget.component';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ActivatedRoute, RouterLink} from '@angular/router';

@Component({
	templateUrl: './dashboard.component.html',
	styleUrls: ['./dashboard.component.scss'],
	imports: [
		RouterLink,
		GenericWidgetComponent,
		LocalizeMapPipe
	]
})
export class DashboardComponent implements OnInit {
	@Input() layout: CMSLayoutDTO;

	selectedSection: CMSSectionDTO;
	sectionBadges: Record<string, number> = {};

	constructor(
		private activatedRoute: ActivatedRoute,
		private destroyRef: DestroyRef
	) {}

	ngOnInit(): void {
		this.activatedRoute.params.pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(params => {
			this.selectedSection = this.layout.sections.find(s => s.id === params['sectionId']) ?? this.layout.sections[0];
		});
	}

	updateBadges(sectionId: string, event: number) {
		if(!this.sectionBadges[sectionId]) {
			this.sectionBadges[sectionId] = 0;
			this.sectionBadges[sectionId] += event;
		}
	}
}
