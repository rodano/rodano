import {Component, OnInit, DestroyRef, input, signal} from '@angular/core';
import {Layout} from '@core/model/layout';
import {VisibilityService} from '../services/visibility.service';
import {CellComponent} from '../cell/cell.component';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {CRFDataset} from '../models/crf-dataset';
import {Cell} from '@core/model/cell';
import {CRFField} from '../models/crf-field';
import {CRFService} from '../services/crf.service';
import {LoggingService} from '@core/services/logging.service';
import {LayoutType} from '@core/model/layout-type';
import {EmptyObjectCheck} from '../../utils/empty-object-check';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {SafeHtmlPipe} from '../../pipes/safe-html.pipe';

@Component({
	selector: 'app-layout',
	templateUrl: './layout.component.html',
	styleUrls: ['./layout.component.css'],
	imports: [
		CellComponent,
		SafeHtmlPipe,
		LocalizeMapPipe
	]
})
export class LayoutComponent implements OnInit {
	readonly layout = input.required<Layout>();
	readonly datasets = input.required<CRFDataset[]>();
	//layoutUid is the global identifier of the layout
	//it is required for visibility criteria
	readonly layoutUid = input.required<string>();
	readonly disabled = input<boolean>(false);

	readonly shown = signal(true);

	constructor(
		private visibilityService: VisibilityService,
		private crfService: CRFService,
		private loggingService: LoggingService,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		this.visibilityService.layoutVisibilityEvents$(this.layout().id).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(shown => {
			//deal only with single layout here
			//if the layout is multiple, its visibility state will be managed by the parent component
			if(this.layout().type === LayoutType.SINGLE) {
				this.loggingService.info(`Layout ${this.layout().id} receiving visibility event containing ${shown}`);
				this.shown.set(shown);
				const cells = this.crfService.getLayoutCells(this.layout());
				//reset state of the cells inside the layout
				this.visibilityService.triggerCellsVisibilityEvent(cells, this.layoutUid(), shown);
				//if the layout and its cells become visible, cell visibility criteria must be re-triggered
				if(shown) {
					cells
						.filter(c => this.crfService.getCellHasField(c))
						.forEach(c => this.visibilityService.triggerCriteria(c, this.layoutUid(), this.getField(c) as CRFField));
				}
			}
		});
	}

	getField(cell: Cell): CRFField | undefined {
		if(!this.crfService.getCellHasField(cell)) {
			return undefined;
		}
		return this.crfService.getCellField(cell, this.datasets());
	}

	getDisabled(cell: Cell): boolean {
		if(!this.crfService.getCellHasField(cell)) {
			return false;
		}
		if(this.disabled()) {
			return true;
		}
		const dataset = this.crfService.getCellDataset(cell, this.datasets());
		//dataset may not be found if the user does not have the right to all datasets used in the form
		if(!dataset) {
			throw new Error(`Missing rights to access dataset ${cell.datasetModelId} (due to a misconfiguration`);
		}
		return !dataset.canWrite;
	}

	isEmptyObject(object: any): boolean {
		return EmptyObjectCheck.isEmptyObject(object);
	}
}
