import {Component, OnInit} from '@angular/core';
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
	info: {title: string; value: string}[] = [];
	loading = false;

	columnsToDisplay: string[] = ['title', 'value'];
	dataSource = this.info;

	constructor(
		private widgetService: WidgetService
	) {}

	ngOnInit(): void {
		this.loading = true;
		this.widgetService.getGeneralInfo().subscribe(info => {
			this.info = info;
			this.loading = false;
		});
	}
}
