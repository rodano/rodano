import {BackendChartData} from '@core/model/chart-dto';

export interface AdaptOptions {
	timeline?: boolean;
	percentage?: boolean;
	ignoreNA?: boolean;
	showOtherCategory?: boolean;
}

export class ChartDataAdapter {
	static preprocessChartDatasets(
		data: BackendChartData | undefined,
		options: AdaptOptions = {}
	): {datasets: any[]; labels: any[]} {
		if(!data || !Array.isArray(data.series)) {
			return {datasets: [], labels: []};
		}

		//Collect all unique X values
		let allX = Array.from(
			new Set(
				data.series.flatMap(series =>
					series.values.map(([x]) => (x === '' ? 'N/A' : x))
				)
			)
		).sort();

		//Handle the ` ignoreNA ` option by filtering out 'N/A'
		if(options.ignoreNA) {
			allX = allX.filter(x => x !== 'N/A');
		}

		//Optionally convert x to Date objects
		const getParsedX = (x: any) => {
			if(options.timeline) {
				let parsed: any = x;
				if(typeof x === 'string' || typeof x === 'number') {
					const d = new Date(x);
					if(!isNaN(d.getTime())) {
						parsed = d;
					}
				}
				return parsed;
			}
			return x;
		};

		const labels = allX.map(getParsedX);

		//Construct data series
		const datasets = data.series.map(series => {
			const valuesMap = new Map(series.values.map(([x, y]) => [x === '' ? 'N/A' : x, y]));
			let dataPoints = allX.map(x => valuesMap.get(x) ?? 0);

			//Percentage logic (optional)
			if(options.percentage) {
				const total = dataPoints.reduce((sum, val) => sum + val, 0);
				dataPoints = dataPoints.map(val =>
					total ? +(100 * val / total).toFixed(2) : 0
				);
			}

			return {
				label: series.label,
				data: dataPoints
			};
		});

		return {datasets, labels};
	}
}
