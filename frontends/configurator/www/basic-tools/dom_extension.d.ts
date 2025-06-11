interface Document {
	createFullElement<K extends keyof HTMLElementTagNameMap>(tag: K, attributes?: object, text?: string, listeners?: {[key: string]: (this: Element, event: any) => any;}): HTMLElementTagNameMap[K];
	createFullElement(tag: string, attributes?: object, text?: string, listeners?: {[key: string]: (this: Element, event: any) => any;}): HTMLElement;
	createFullElementNS<K extends keyof HTMLElementTagNameMap>(ns: string, tag: K, attributes?: object, text?: string, listeners?: {[key: string]: (this: Element, event: any) => any;}): HTMLElementTagNameMap[K];
	createFullElementNS(ns: string, tag: string, attributes?: object, text?: string, listeners?: {[key: string]: (this: Element, event: any) => any;}): Element;
}

interface Node {
	empty(): Node;
	appendChildren(nodes: Array<Node>): void;
}

interface NodeList {
	isEmpty(): boolean;
	last(): Node;
	first(): Node;

	indexOf(element: Node): number;
	includes(element: Node): boolean;
	slice(start?: number, end?: number): Array<Node>;

	forEach(callback: Function, that?: any): void;
	map(callback: Function, that?: any): Array<Node>;
	find(callback: Function, that?: any): Node;
	filter(callback: Function, that?: any): Array<Node>;
	every(callback: Function, that?: any): boolean;
	some(callback: Function, that?: any): boolean;
}

interface NodeListOf<TNode extends Node> extends NodeList {
	isEmpty(): boolean;
	last(): TNode;
	first(): TNode;

	indexOf(element: Node): number;
	includes(element: Node): boolean;
	slice(start?: number, end?: number): Array<TNode>;

	forEach(callback: Function, that?: any): void;
	map(callback: Function, that?: any): Array<TNode>;
	find(callback: Function, that?: any): TNode;
	filter(callback: Function, that?: any): Array<TNode>;
	every(callback: Function, that?: any): boolean;
	some(callback: Function, that?: any): boolean;
}

interface Element {
	empty(selector?: string): Element;
	setAttributes(attributes: object): Element;
	getPosition(): {left: number; top: number};
}

interface HTMLCollectionBase {
	indexOf(element: HTMLElement): number;
	includes(element: HTMLElement): boolean;
	slice(start?: number, end?: number): Array<any>;

	first(): HTMLElement;
	last(): HTMLElement;
	isEmpty(): boolean;

	sort(comparator: (element1, element2) => number);
	slice(): Array<any>;

	forEach(callback: Function, that?: any): void;
	map(callback: Function, that?: any): Array<HTMLElement>;
	find(callback: Function, that?: any): HTMLElement;
	filter(callback: Function, that?: any): Array<HTMLElement>;
	every(callback: Function, that?: any): boolean;
	some(callback: Function, that?: any): boolean;
}

interface HTMLFormElement {
	disable(): void;
	enable(): void;
}

interface HTMLDataListElement {
	fill(entries: string[]): HTMLDataListElement;
	fillObjects<T>(objects: Array<any>, property: string | ((object: T) => string)): HTMLDataListElement;
}

interface HTMLSelectElement {
	fill(entries: [string, string][] | string[], blank_entry: boolean, selected_entries: string[] | string): HTMLSelectElement;
	fillObjects<T>(objects:  Array<T>, value_property: string | ((object: T) => string), label_property: string | ((object: T) => string), blank_entry: boolean, selected_entries: string[] | string): HTMLSelectElement;
}

interface Storage {
	setObject(key: string, value: any);
	getObject(key: string);
}

interface Event {
	stop();
}
