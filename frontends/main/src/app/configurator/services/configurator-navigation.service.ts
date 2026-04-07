import {Subject} from 'rxjs';
import {Injectable} from '@angular/core';

export interface NavigationRequest {
	entityType: string;
	entityId: string;
	parentId?: string;
}

@Injectable({providedIn: 'root'})
export class ConfiguratorNavigationService {
	private navigateSubject = new Subject<NavigationRequest>();
	navigate$ = this.navigateSubject.asObservable();

	navigateTo(entityType: string, entityId: string): void {
		this.navigateSubject.next({entityType, entityId});
	}

	getEntityIcon(type: string): string {
		const icons: Record<string, string> = {
			'scope-model': 'account_tree',
			'event-model': 'event',
			'event-group': 'group',
			'dataset-model': 'dataset',
			'field-model': 'text_ad',
			'form-model': 'description',
			'form-layout': 'view_quilt',
			'workflow': 'settings',
			'workflow-state': 'adjust',
			'workflow-action': 'play_circle',
			'workflow-summary': 'summarize',
			'workflow-widget': 'widgets',
			'timeline-graph': 'timeline',
			'timeline-graph-section': 'bid_landscape',
			'privacy-policy': 'security',
			'resource-category': 'eco',
			'profile': 'badge',
			'cron': 'schedule',
			'menu': 'menu',
			'report': 'assessment',
			'chart': 'bar_chart',
			'validator': 'rule'
		};
		return icons[type] ?? 'link';
	}

	getEntityLabel(type: string): string {
		const labels: Record<string, string> = {
			'scope-model': 'Scope Model',
			'event-model': 'Event Model',
			'event-group': 'Event Group',
			'dataset-model': 'Dataset Model',
			'field-model': 'Field Model',
			'form-model': 'Form Model',
			'form-layout': 'Form Layout',
			'workflow': 'Workflow',
			'workflow-state': 'Workflow State',
			'workflow-action': 'Workflow Action',
			'workflow-summary': 'Workflow Summary',
			'workflow-widget': 'Workflow Widget',
			'timeline-graph': 'Timeline Graph',
			'timeline-graph-section': 'Timeline Graph Section',
			'privacy-policy': 'Privacy Policy',
			'resource-category': 'Resource Category',
			'profile': 'Profile',
			'cron': 'Cron',
			'menu': 'Menu',
			'report': 'Report',
			'chart': 'Chart',
			'validator': 'Validator'
		};
		return labels[type] ?? type;
	}
}
