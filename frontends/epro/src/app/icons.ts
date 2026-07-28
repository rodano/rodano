import {addIcons} from 'ionicons';
import * as ionicons from 'ionicons/icons';

/**
 * Ionic's standalone components (provideIonicAngular) do not bundle any icon by default,
 * unlike the legacy IonicModule which auto-registered the whole Ionicons set.
 * Icon names used in this app are not only hardcoded in templates (e.g. "help-circle") but also
 * come from the study configuration (event model icon), so the whole Ionicons set is registered
 * here instead of maintaining a manual list of the icons actually used.
 */
export function registerIcons(): void {
	const icons: Record<string, string> = {};
	for(const [name, data] of Object.entries(ionicons)) {
		//convert the exported camelCase name (e.g. helpCircle) to the kebab-case name used in templates (e.g. help-circle)
		const kebabName = name.replace(/([A-Z])/g, '-$1').toLowerCase();
		icons[kebabName] = data;
	}
	addIcons(icons);
}
