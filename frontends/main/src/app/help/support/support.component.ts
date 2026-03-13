import {ChangeDetectionStrategy, Component, OnInit, signal} from '@angular/core';
import {Study} from '@core/model/study';
import {ConfigurationService} from '@core/services/configuration.service';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	templateUrl: './support.component.html',
	styleUrls: ['./support.component.css']
})
export class SupportComponent implements OnInit {
	readonly study = signal<Study | undefined>(undefined);

	constructor(
		public configurationService: ConfigurationService
	) {}

	ngOnInit() {
		this.configurationService.getStudy().subscribe(study => this.study.set(study));
	}
}
