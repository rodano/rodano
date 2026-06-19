import {Component, OnInit, signal} from '@angular/core';
import {WidgetService} from '@core/services/widget.service';
import {MatProgressBar} from '@angular/material/progress-bar';
import {MatTableModule} from '@angular/material/table';

@Component({
	selector: 'app-general-info-widget',
	templateUrl: './general-info-widget.component.html',
	styleUrls: ['./general-info-widget.component.css'],
	imports: [
		MatProgressBar,
		MatTableModule
	]
})
export class GeneralInfoWidgetComponent implements OnInit {
	readonly info = signal<{title: string; value: string}[]>([]);
	readonly loading = signal(false);

	columnsToDisplay: string[] = ['title', 'value'];

	constructor(
		private widgetService: WidgetService
	) {}

	ngOnInit(): void {
		this.loading.set(true);
		this.widgetService.getGeneralInfo().subscribe(info => {
			this.info.set(info);
			this.loading.set(false);
		});
	}
}
