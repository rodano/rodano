export class EmptyObjectCheck {
	static isEmptyObject(object: any): boolean {
		if(!object) {
			return true;
		}
		const values = Object.values(object);
		return values.length === 0 || values.every(v => !v);
	}
}
