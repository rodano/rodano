import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {forkJoin, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {FormulaFunction} from '@core/model/formula-function';
import {FormulaCondition} from '@core/model/formula-condition';

export interface FormulaProposal {
	id: string;
	label: string;
	value: string;
	category: string;
}

@Injectable({
	providedIn: 'root'
})
export class FormulaAutocompleteService {
	constructor(private http: HttpClient) {}

	getFormulaFunctions(projectId: string): Observable<FormulaFunction[]> {
		return this.http.get<FormulaFunction[]>(
			`/api/superuser/configurator/projects/${projectId}/formula/functions`
		);
	}

	getFormulaConditions(projectId: string): Observable<FormulaCondition[]> {
		return this.http.get<FormulaCondition[]>(
			`/api/superuser/configurator/projects/${projectId}/formula/conditions`
		);
	}

	getAllProposals(projectId: string): Observable<FormulaProposal[]> {
		return forkJoin({
			functions: this.getFormulaFunctions(projectId),
			conditions: this.getFormulaConditions(projectId)
		}).pipe(
			map(({functions, conditions}) => {
				const proposals: FormulaProposal[] = [];

				conditions.forEach(c => {
					proposals.push({
						id: c.id || '',
						label: c.label || '',
						value: c.value || '',
						category: 'Condition'
					});
				});

				functions.forEach(f => {
					proposals.push({
						id: f.id || '',
						label: f.label || '',
						value: f.value || '',
						category: f.category || 'Function'
					});
				});

				return proposals;
			})
		);
	}
}
