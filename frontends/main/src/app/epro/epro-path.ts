export const EPRO_PATH = 'epro';

export function isEproUrl(url: string): boolean {
	const prefix = `/${EPRO_PATH}`;
	return url === prefix || ['/', '?', '#', ';'].some(separator => url.startsWith(`${prefix}${separator}`));
}
