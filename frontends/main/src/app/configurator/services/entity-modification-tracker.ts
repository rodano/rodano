export class EntityModificationTracker<T extends Record<string, any>> {
	private modifiedIds = new Set<string>();
	private modifiedFieldsByEntity = new Map<string, Set<string>>();
	private originals: T[] = [];
	private current: T[] = [];

	constructor(
		private getIdFn: (entity: T) => string,
		private simpleFields: (keyof T)[],
		private translationFields: (keyof T)[],
		private arrayFields: (keyof T)[]
	) {}

	initialize(entities: T[]): void {
		this.originals = JSON.parse(JSON.stringify(entities));
		this.current = JSON.parse(JSON.stringify(entities));
		this.modifiedIds.clear();
		this.modifiedFieldsByEntity.clear();
	}

	getModifiedIds(): Set<string> {
		return this.modifiedIds;
	}

	getModifiedFieldsMap(): Map<string, Set<string>> {
		return this.modifiedFieldsByEntity;
	}

	getOriginals(): T[] {
		return this.originals;
	}

	clearModifications(): void {
		this.modifiedIds.clear();
		this.modifiedFieldsByEntity.clear();
	}

	resetToOriginals(): void {
		this.current = JSON.parse(JSON.stringify(this.originals));
		this.modifiedIds.clear();
		this.modifiedFieldsByEntity.clear();
	}

	updateEntity(updated: T): void {
		const id = this.getIdFn(updated);
		this.current = this.current.map(entity =>
			this.getIdFn(entity) === id ? updated : entity
		);

		const original = this.originals.find(e => this.getIdFn(e) === id);
		if(original) {
			this.trackChanges(id, original, updated);
		}
	}

	addEntity(entity: T): void {
		this.current.push(entity);
		this.originals.push(JSON.parse(JSON.stringify(entity)));
	}

	removeEntity(id: string): void {
		this.current = this.current.filter(e => this.getIdFn(e) !== id);
		this.originals = this.originals.filter(e => this.getIdFn(e) !== id);
		this.modifiedIds.delete(id);
		this.modifiedFieldsByEntity.delete(id);
	}

	private trackChanges(id: string, original: T, updated: T): void {
		const modifiedFields = new Set<string>();

		const normalize = (value: any) =>
			value === null || value === undefined ? undefined : value;

		this.simpleFields.forEach(field => {
			if(normalize(original[field]) !== normalize(updated[field])) {
				modifiedFields.add(field as string);
			}
		});

		this.translationFields.forEach(field => {
			const originalValue = original[field] as Record<string, string> | undefined;
			const updatedValue = updated[field] as Record<string, string> | undefined;

			if(originalValue && updatedValue) {
				const allLanguages = new Set([
					...Object.keys(originalValue),
					...Object.keys(updatedValue)
				]);
				allLanguages.forEach(lang => {
					if(originalValue[lang] !== updatedValue[lang]) {
						modifiedFields.add(`${field as string}.${lang}`);
					}
				});
			}
			else if(normalize(originalValue) !== normalize(updatedValue)) {
				modifiedFields.add(field as string);
			}
		});

		this.arrayFields.forEach(field => {
			if(JSON.stringify(original[field]) !== JSON.stringify(updated[field])) {
				modifiedFields.add(field as string);
			}
		});

		if(modifiedFields.size > 0) {
			this.modifiedFieldsByEntity.set(id, modifiedFields);
			this.modifiedIds.add(id);
		}
		else {
			this.modifiedFieldsByEntity.delete(id);
			this.modifiedIds.delete(id);
		}
	}

	isModified(id: string): boolean {
		return this.modifiedIds.has(id);
	}

	isFieldModified(id: string, fieldName: string): boolean {
		const modifiedFields = this.modifiedFieldsByEntity.get(id);
		if(!modifiedFields) {
			return false;
		}

		if(modifiedFields.has(fieldName)) {
			return true;
		}

		const languageFieldPattern = new RegExp(`^${fieldName}\\.`);
		return Array.from(modifiedFields).some(field => languageFieldPattern.test(field));
	}

	getTotalModifiedFieldsCount(): number {
		let count = 0;
		this.modifiedFieldsByEntity.forEach(fields => {
			count += fields.size;
		});
		return count;
	}

	getCurrent(): T[] {
		return this.current;
	}

	getEntity(id: string): T | undefined {
		return this.current.find(e => this.getIdFn(e) === id);
	}

	syncOriginalsWithCurrent(): void {
		this.originals = JSON.parse(JSON.stringify(this.current));
		this.modifiedIds.clear();
		this.modifiedFieldsByEntity.clear();
	}
}
