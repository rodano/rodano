import {ChartManagerService} from '../manager/chart-manager.service';
import {ChartModel} from '@core/model/chart-model';

export interface ChartContext {
	chartManager: ChartManagerService;
	charts: ChartModel[];
	originalCharts: ChartModel[];
	modifiedChartIds: Set<string>;
}
