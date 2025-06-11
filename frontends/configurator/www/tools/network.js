export const Network = {
	Check: function(check_url) {
		return new Promise(function(resolve, reject) {
			//network can only be available in a check URL has been provided an browser in running in http(s) mode
			if(check_url && window.location.protocol === 'http:' || window.location.protocol === 'https:') {
				fetch(check_url)
					.then(response => [404, 502].includes(response.status) ? reject() : resolve())
					.catch(reject);
			}
			//network is obviously not available in file mode
			else {
				reject();
			}
		});
	}
};
