import {Injectable} from '@angular/core';
import {Subject, BehaviorSubject} from 'rxjs';
import {LayoutDTO} from '@core/model/layout-dto';
import {CRFDataset} from '../models/crf-dataset';
import {CRFService} from './crf.service';
import {LoggingService} from '@core/services/logging.service';
import {LayoutType} from '@core/model/layout-type';

@Injectable({
	providedIn: 'root'
})
export class CellLoadingService {
	//The subject for collective cell loading completion
	private readonly allCellsLoadedSource = new BehaviorSubject<boolean>(false);

	//The exposed observable
	public readonly allCellsLoaded$ = this.allCellsLoadedSource.asObservable();

	//The list of cells that are loading
	private loadingCellIds: string[] = [];

	//The subject used to monitor the individual cell loading completion
	private readonly cellLoaded$ = new Subject<string>();

	constructor(
		private dataStateService: CRFService,
		private loggingService: LoggingService
	) {
		//whenever a cell finishes loading, remove it from the list
		//when the list is empty announce the loading completion to all the subscribers
		this.cellLoaded$.asObservable().subscribe(cellId => {
			this.loadingCellIds.splice(this.loadingCellIds.indexOf(cellId), 1);
			if(this.loadingCellIds.length === 0) {
				//TODO improve this
				//this delay is needed to not trigger new changes in the same tick when cells were loaded
				//otherwise the state changes twice in the same view update and we get a ExpressionChangedAfterItHasBeenCheckedError
				setTimeout(() => this.allCellsLoadedSource.next(true), 0);
			}
		});
	}

	public registerFormCells(layouts: LayoutDTO[], datasets: CRFDataset[]) {
		this.loadingCellIds = [];
		const cellIds: string[] = [];
		layouts.forEach(layout => {
			const layoutCellIds = this.dataStateService.getLayoutCells(layout).map(c => c.id);
			if(layout.type === LayoutType.SINGLE) {
				cellIds.push(...layoutCellIds);
			}
			else {
				datasets.filter(d => d.modelId === layout.datasetModel.id).forEach(() => {
					cellIds.push(...layoutCellIds);
				});
			}
		});
		//form may not contain any cell (a form with only an empty layout)
		if(cellIds.length === 0) {
			this.allCellsLoadedSource.next(true);
			return;
		}
		this.registerCellIds(cellIds);
		this.loggingService.info('New form cell ids registered, waiting to be loaded', cellIds);
	}

	public registerLayoutCells(layout: LayoutDTO) {
		const layoutCellIds = this.dataStateService.getLayoutCells(layout).map(c => c.id);
		//layout may not contain any cell
		if(layoutCellIds.length === 0) {
			this.allCellsLoadedSource.next(true);
			return;
		}
		this.registerCellIds(layoutCellIds);
		this.loggingService.info('New layout cell ids registered, waiting to be loaded', layoutCellIds);
	}

	/**
	 * Add cell ids to the loading list
	 * @param cellIds New cell ids
	 */
	registerCellIds(cellIds: string[]) {
		this.loadingCellIds.push(...cellIds);
	}

	/**
	 * The individual cell loading announcement function
	 * @param cellId The id of the completed cell
	 */
	cellLoadingComplete(cellId: string) {
		this.cellLoaded$.next(cellId);
	}
}
