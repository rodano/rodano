import {Component, Input, Output, EventEmitter, OnChanges, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {EventModel} from '@core/model/event-model';
import {color} from 'chart.js/helpers';
import {LanguageService} from '../../../services/language.service';

interface TimelineEvent {
	eventModel: EventModel;
	deadlineMonth: number;
	intervalMonths: number;
	label: string;
}

@Component({
	selector: 'app-event-model-timeline',
	standalone: true,
	templateUrl: './event-model-timeline.component.html',
	styleUrls: ['./event-model-timeline.component.css'],
	imports: [CommonModule, MatIconModule]
})
export class EventModelTimelineComponent implements OnChanges {
	@Input() eventModels: EventModel[] = [];
	@Input() selectedEventModelId: string | null = null;
	@Output() eventSelected = new EventEmitter<string>();

	timelineEvents: TimelineEvent[] = [];
	labelLayers = new Map<string, number>();
	timeScale = 30;
	maxMonths = 0;
	leftMargin = 55;
	rightMargin = 55;
	axisY = 100;

	constructor(
		private languageService: LanguageService
	) {}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['eventModels']) {
			this.buildTimeline();
		}
	}

	private buildTimeline(): void {
		if(!this.eventModels || this.eventModels.length === 0) {
			this.timelineEvents = [];
			this.labelLayers = new Map();
			this.maxMonths = 0;
			return;
		}

		this.timelineEvents = this.eventModels.map(em => {
			const deadlineMonth = this.convertToMonths(em.deadline, em.deadlineUnit);
			const intervalMonths = this.convertToMonths(em.interval, em.intervalUnit);

			return {
				eventModel: em,
				deadlineMonth,
				intervalMonths,
				label: this.languageService.getDefaultTranslation(em.shortname) || em.id
			};
		});

		this.maxMonths = Math.max(
			...this.timelineEvents.map(te => te.deadlineMonth + (te.intervalMonths || 0)),
			12
		);

		this.maxMonths = Math.ceil(this.maxMonths / 3) * 3;

		this.labelLayers = this.calculateLabelLayers();
	}

	private convertToMonths(value: number | undefined, unit: string | undefined): number {
		if(!value || !unit) {
			return 0;
		}

		switch(unit.toUpperCase()) {
			case 'DAYS':
				return Math.round(value / 30);
			case 'WEEKS':
				return Math.round(value / 4);
			case 'MONTHS':
				return value;
			case 'YEARS':
				return value * 12;
			case 'HOURS':
				return Math.round(value / 720);
			case 'MINUTES':
				return Math.round(value / 43200);
			case 'SECONDS':
				return Math.round(value / 2592000);
			default:
				return value;
		}
	}

	getEventPosition(event: TimelineEvent): {left: number; width: number} {
		const hasInterval = event.intervalMonths > 0;

		const width = hasInterval ? event.intervalMonths * this.timeScale : 3;

		const deadlinePosition = this.leftMargin + (event.deadlineMonth * this.timeScale);
		const left = deadlinePosition - (width / 2);

		return {left, width};
	}

	getTimelineWidth(): number {
		return this.leftMargin + (this.maxMonths * this.timeScale) + this.rightMargin + 40;
	}

	onEventClick(event: TimelineEvent): void {
		this.eventSelected.emit(event.eventModel.eventModelId);
	}

	isSelected(event: TimelineEvent): boolean {
		return this.selectedEventModelId === event.eventModel.eventModelId;
	}

	getMonthMarkers(): number[] {
		const markers: number[] = [];
		for(let month = 0; month <= this.maxMonths; month++) {
			markers.push(month);
		}
		return markers;
	}

	getMonthPosition(month: number): number {
		return this.leftMargin + (month * this.timeScale);
	}

	getAxisStartX(): number {
		return this.getMonthPosition(0);
	}

	getAxisEndX(): number {
		return this.getMonthPosition(this.maxMonths);
	}

	private calculateLabelLayers(): Map<string, number> {
		const labelLayers = new Map<string, number>();
		const positionGroups = new Map<number, string[]>();

		this.timelineEvents.forEach(event => {
			const deadline = event.deadlineMonth;
			if(!positionGroups.has(deadline)) {
				positionGroups.set(deadline, []);
			}
			positionGroups.get(deadline)!.push(event.eventModel.eventModelId);
		});

		positionGroups.forEach((eventIds, _deadline) => {
			eventIds.forEach((eventId, index) => {
				labelLayers.set(eventId, index);
			});
		});

		return labelLayers;
	}

	getLabelYPosition(layer: number): number {
		const baseY = 80;
		const labelSpacing = 16;
		return baseY - (layer * labelSpacing);
	}

	protected readonly color = color;
}
