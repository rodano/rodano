export class BusEvent {
	getCallbacks(): Array<string>;
	hit(listener: any);
}

export class Bus {
	disable();
	enable();
	lock();
	unlock();
	reset();
	register(listener: any);
	unregister(listener: any);
	isRegistered(listener: any): boolean;
	pause();
	resume();
	dispatch(event: BusEvent);
	listeners: any;
}
