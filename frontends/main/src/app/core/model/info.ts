export interface Info {
	instance: {
		uid: string;
	};
	build: {
		artifact: string;
		name: string;
		time: string;
		version: string;
		group: string;
	};
	config: {
		sha1: string;
		date: Date;
	};
}
