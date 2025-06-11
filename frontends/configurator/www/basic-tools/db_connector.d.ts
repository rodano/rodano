export class DBConnector<T> {
	constructor(name: string, keypath: string);
	isOpen(): boolean;
	open(): Promise<IDBDatabase>;
	drop(): Promise<Event>;
	getCursor(): IDBRequest;
	add(item: T): Promise<Event>;
	addAll(items: Array<T>): Promise<Array<Event>>;
	add(): Promise<Event>;
	get(key: string | number): Promise<T>;
	getAll(): Promise<Array<T>>;
	getSome(filter?: (item: T) => boolean): Promise<Array<T>>;
	remove(key: string | number): Promise<T>;
	removeAll(): Promise<Array<T>>;
	removeSome(filter?: (item: T) => boolean): Promise<Array<T>>;
}
