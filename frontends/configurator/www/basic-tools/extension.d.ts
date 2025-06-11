interface ObjectConstructor {
	isObject(object: any): object is Object;
	isEmpty(object: object): boolean;
	equals(object1: any, object2: any): boolean;
	key(object: object, value: any): any;
	clone(object: object): any;
	getObjectPathValue(object: any, path: string): any;
	getLastObjectInPath(object: any, path: string): any;
}

interface FunctionConstructor {
	isFunction(object: any): object is Function;
}

interface StringConstructor {
	isString(object: any): object is string;
}

interface String {
	capitalize(): string;
	reverse(): string;
	nocaseIncludes(string: string): boolean;
	compareTo(string: string): number;
	replaceObject(object: {[key: string]: any}): string;
	interpolate(object: {[key: string]: any}): string;
	getBytes(): Array<number>;
}

interface Boolean {
	compareTo(boolean: boolean): number;
}

interface NumberConstructor {
	isNumber(object: any): object is number;
}

interface Number {
	compareTo(number: number): number;
	pad(length: number, pad?: string): string;
}

interface Array<T> {
	isEmpty(): boolean;
	last(): T;
	first(): T;

	indexOfSame(element: T): number;
	includesSame(element: T): boolean;
	includesAll(elements: Array<T>): boolean;
	includesOne(elements: Array<T>): boolean;

	pushAll(elements: Array<T>);
	insert(number: number, object: T);
	remove(index: number);
	remove(from: number, to: number);
	removeElement(element: T);
	removeElements(elements: Array<T>);

	replace(oldElement: T, newElement: T);
}

interface DateConstructor {
	isDate(object: any): object is Date;
	isValidDate(object: any): boolean;
	getDifferenceInDays(start: Date, stop: Date): number;
	getDifferenceInHours(start: Date, stop: Date): number;
	getDifferenceInMinutes(start: Date, stop: Date): number;
	getDifferenceInSeconds(start: Date, stop: Date): number;
	getDifferenceInMilliseconds(start: Date, stop: Date): number;
	parseToDisplay(date: string): Date;
	parseToFullDisplay(date: string): Date;
	parseToFullDisplayUTC(date: string): Date;
	getDurationLiteral(duration: number): string;

	SECONDS_IN_MINUTE: number;
	MINUTES_IN_HOUR: number;
	HOURS_IN_DAY: number;

	MS_IN_SECOND: number;
	MS_IN_MINUTE: number;
	MS_IN_HOUR: number;
	MS_IN_DAY: number;
}

interface Date {
	toDisplay(): string;
	toFullDisplay(): string;
	format(formatter: string): string;
	toUTCDisplay(): string;
	toUTCFullDisplay(): string;
	formatUTC(formatter: string): string;

	equals(otherDate: Date): boolean;
	compareTo(otherDate: Date): number;

	isBefore(otherDate: Date): boolean;
	isAfter(otherDate: Date): boolean;

	clone(): Date;

	addMilliseconds(milliseconds: number): Date;
	addSeconds(seconds: number): Date;
	addMinutes(minutes: number): Date;
	addHours(hours: number): Date;
	addDays(days: number): Date;
	addMonths(months: number): Date;
	addYears(years: number): Date;

	roundToDay(): Date;
	roundToHour(): Date;
	roundToMinute(): Date;

	getAge(): number;
	getAgeLiteral(): string;
}
