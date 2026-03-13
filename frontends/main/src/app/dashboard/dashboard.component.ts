import {ChangeDetectionStrategy, Component, DestroyRef, OnInit, input, signal} from '@angular/core';
import {CMSLayout} from '@core/model/cms-layout';
import {CMSSection} from '@core/model/cms-section';
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';
import {GenericWidgetComponent} from '../widgets/generic-widget/generic-widget.component';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ActivatedRoute, RouterLink} from '@angular/router';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	templateUrl: './dashboard.component.html',
	styleUrls: ['./dashboard.component.scss'],
	imports: [
		RouterLink,
		GenericWidgetComponent,
		LocalizeMapPipe
	]
})
export class DashboardComponent implements OnInit {
	readonly layout = input.required<CMSLayout>();

	readonly selectedSection = signal<CMSSection | undefined>(undefined);
	readonly sectionBadges = signal<Record<string, number>>({});

	constructor(
		private activatedRoute: ActivatedRoute,
		private destroyRef: DestroyRef
	) {}

	ngOnInit(): void {
		this.activatedRoute.params.pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(params => {
			this.selectedSection.set(this.layout().sections.find(s => s.id === params['sectionId']) ?? this.layout().sections[0]);
		});
	}

	updateBadges(sectionId: string, event: number) {
		const badges = this.sectionBadges();
		if(!badges[sectionId]) {
			badges[sectionId] = 0;
		}
		badges[sectionId] += event;
		this.sectionBadges.set(badges);
	}
}
