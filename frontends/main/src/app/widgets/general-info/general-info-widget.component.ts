import {Component, OnInit} from '@angular/core';
import {WidgetService} from '@core/services/widget.service';
import {MatTableModule} from '@angular/material/table';

@Component({
	selector: 'app-general-info-widget',
	templateUrl: './general-info-widget.component.html',
	styleUrls: ['./general-info-widget.component.css'],
	imports: [
		MatTableModule
	]
})
export class GeneralInfoWidgetComponent implements OnInit {
	generalInfos: {title: string; value: string}[] = [];

	columnsToDisplay: string[] = ['title', 'value'];
	dataSource = this.generalInfos;

	constructor(
		private widgetService: WidgetService
	) {}

	ngOnInit(): void {
		this.widgetService.getGeneralInfo().subscribe(infos => {
			this.generalInfos = infos;
		});
	}
}
