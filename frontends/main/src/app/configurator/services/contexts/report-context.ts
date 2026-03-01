import {ReportManagerService} from '../manager/report-manager.service';
import {Report} from '@core/model/report';

export interface ReportContext {
	reportManager: ReportManagerService;
	reports: Report[];
	originalReports: Report[];
	modifiedReportIds: Set<string>;
}
