import {
	Component,
	DestroyRef,
	EventEmitter,
	Input,
	OnChanges,
	OnInit,
	Output,
	SimpleChanges
} from '@angular/core';
import {MatIcon} from '@angular/material/icon';
import {NavigationEnd, Router} from '@angular/router';
import {MatButton} from '@angular/material/button';
import {filter} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';

/**
	* Represents a single item in the sidebar menu
	*/
export interface SidebarItem {
	icon: string;
	link: string;
	text: string;
	children?: SidebarItem[];
	supportsChildren?: boolean;
}

@Component({
	selector: 'app-sidebar',
	templateUrl: './sidebar.component.html',
	imports: [
		MatIcon,
		MatButton
	],
	styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnChanges, OnInit {
	/**
		* Optional title/heading for the sidebar
		*/
	@Input() heading: string | undefined = 'Sidebar';

	/**
		* List of items (and potentially children) to render in the sidebar.
		*/
	@Input() sidebarItems: SidebarItem[];

	/**
		* Current active path or identifier to highlight the active sidebar item.
		*/
	@Input() activeSite: string | null = null;

	/**
		* Indicates if this sidebar is a child-level sidebar (for nested rendering).
		*/
	@Input() isChild = false;

	/**
		* Emitted whenever the active sidebar item changes.
		*/
	@Output() activeSiteChange = new EventEmitter<string>();

	/**
		* Emitted when the user clicks a button to create a new item (e.g. "+").
		*/
	@Output() createNewClicked = new EventEmitter<SidebarItem>();

	/**
		* Base path for routing. Used to build a full route when navigating from sidebar items.
		*/
	@Input() basePath: string[] = [];

	constructor(
		private router: Router,
		private destroyRef: DestroyRef
	) {}

	/**Controls whether the sidebar is expanded or collapsed.*/
	expanded = true;

	/**Tracks the currently selected item based on the route.*/
	selectedItem: SidebarItem | null = null;

	ngOnInit() {
		this.router.events
			.pipe(
				filter(event => event instanceof NavigationEnd),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe((event: NavigationEnd) => {
				this.activeSite = event.urlAfterRedirects;
				this.selectedItem = this.findItemForUrl(this.activeSite);
			});
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes.activeSite) {
			console.log('Sidebar received activeSite:', this.activeSite);
		}
		console.log('Sidebar items:', this.sidebarItems);
	}

	/**
		* Toggles between expanded and collapsed sidebar state.
		*/
	toggleExpanded() {
		this.expanded = !this.expanded;
	}

	/**
		* Handles click events on a sidebar item and navigates to the corresponding route.
		* @param item The sidebar item that was clicked.
		*/
	onItemClick(item: SidebarItem) {
		const fullPath = [...this.basePath, ...item.link.split('/')];
		this.router.navigate(fullPath).then(success => {
			if(!success) {
				console.warn('Navigation blocked or failed');
			}
		});
	}

	/**
		* Emits the `createNewClicked` event with the current heading as the item context.
		*/
	handleCreateNewClicked() {
		this.createNewClicked.emit({
			icon: this.heading?.toLowerCase() || '',
			text: this.heading || '',
			link: ''
		});
	}

	/**
		* Attempts to find a sidebar item that matches the given URL.
		* @param url The current route URL.
		*/
	private findItemForUrl(url: string): SidebarItem | null {
		return this.sidebarItems.find(i => url.includes(i.link)) || null;
	}

	/**
		* Determines if a sidebar item is currently active, based on the current route.
		* @param item The item to check.
		*/
	isItemActive(item: SidebarItem): boolean {
		if(!this.activeSite || !item.link) {
			return false;
		}

		//Normalize active route (e.g. "chart/ENROLLMENT")
		const normalizedActive = this.activeSite.split('/').slice(-2).join('/');

		//Case 1: Exact match or subpath match
		const isLinkMatch = normalizedActive === item.link || normalizedActive.startsWith(`${item.link}/`);

		//Case 2: Top-level items that share the same link (e.g. 'placeholder') but different labels
		const isTextFallbackMatch
			= item.link === 'placeholder' && this.activeSite.toLowerCase().includes(item.text.toLowerCase().replace(/\s+/g, ''));

		return isLinkMatch || isTextFallbackMatch;
	}
}
