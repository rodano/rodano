export class Queue {
	constructor(resultCallback?: (result: any) => any);
	run();
	add(promiser: () => Promise<any>);
	addAll(promisers: Array<() => Promise<any>>);
	clear();
	then(callback: (result: any) => any);
	catch(callback: (result: any) => any);
}
