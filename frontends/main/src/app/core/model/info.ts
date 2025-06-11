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
		kv: string;
		sha1: string;
		date: Date;
	};
	deployment: {
		user: string;
		date: string;
		commit_sha1: string;
		info: string;
	};
}
