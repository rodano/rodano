import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {TreeNode} from '../tree-node';
import {BaseTreeComponent} from '../base-tree.component';
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
	selector: 'app-timeline-graph-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatTooltipModule],
	templateUrl: './timeline-graph-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class TimelineGraphTreeComponent extends BaseTreeComponent {
	@Input() timelineGraphs: any[] = [];
	@Input() sections: any[] = [];
	@Input() selectedTimelineGraphId: string | null = null;
	@Input() selectedGraphSectionId: string | null = null;

	constructor(languageService: LanguageService) {
		super(languageService);
	}

	getCategoryIcon(): string {return 'timeline';}
	getCategoryLabel(): string {return 'Timeline Graphs';}
	getCategoryTheme(): string {return 'theme-timeline-graph';}

	protected buildTree(): void {
		if(!this.timelineGraphs?.length) {
			return;
		}

		this.treeNodes = this.timelineGraphs.map(tg => {
			const node: TreeNode = {
				id: `timeline-graph-${tg.timelineGraphId}`,
				label: this.languageService.getDefaultTranslation(tg.shortname) || tg.id,
				icon: 'line_start',
				type: 'timeline-graph',
				selected: this.selectedTimelineGraphId === tg.timelineGraphId,
				entityId: tg.timelineGraphId,
				themeClass: 'theme-timeline-graph'
			};

			if(this.selectedTimelineGraphId === tg.timelineGraphId && this.sections.length > 0) {
				const sectionNodes: TreeNode[] = this.sections
					.filter(tgs => tgs.timelineGraphId === tg.timelineGraphId)
					.map(tgs => ({
						id: `timeline-graph-section-${tgs.graphSectionId}`,
						label: this.languageService.getDefaultTranslation(tgs.label) || tgs.id,
						icon: 'bid_landscape',
						type: 'timeline-graph-section',
						selected: this.selectedGraphSectionId === tgs.graphSectionId,
						entityId: tgs.graphSectionId,
						themeClass: 'theme-timeline-graph-section'
					}));

				if(sectionNodes.length > 0) {
					node.children = sectionNodes;
					node.expanded = true;
				}
			}

			return node;
		});
	}
}
