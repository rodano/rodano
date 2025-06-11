const PORT = 1337;

import * as http from 'http';
import finalhandler from 'finalhandler';
import serve_static from 'serve-static';

function get_parameter_value(args, name, default_value) {
	//retrieve parameter
	const parameter = args.find(a => a.startsWith(`-${name}`));
	//interpret parameter
	if(parameter) {
		const equals_position = parameter.indexOf('=');
		//manage parameter without value
		if(equals_position === -1) {
			return true;
		}
		const value = parameter.substring(equals_position + 1);
		switch(value) {
			case 'true': return true;
			case 'false': return false;
			default: return value;
		}
	}
	return default_value;
}

/*eslint-disable quotes*/
let csp = "default-src * data: blob: filesystem: about: ws: wss: 'unsafe-inline' 'unsafe-eval';";
csp += "script-src * data: blob: 'unsafe-inline' 'unsafe-eval';";
csp += "frame-src * data: blob: ;";
/*eslint-enable quotes*/

//launch HTTP server
const serve = serve_static('www', {
	setHeaders: response => response.setHeader('Content-Security-Policy', csp)
});
const server = http.createServer((request, response) => {
	if(request.url.startsWith('/api/')) {
		console.log(`Transferring request ${request.url} to the API`);
		const options = {
			hostname: 'localhost',
			port: 8080,
			path: request.url.replace('/api', ''),
			method: request.method,
			headers: request.headers
		};
		const proxy = http.request(options, api_response => {
			response.writeHead(api_response.statusCode, api_response.headers);
			api_response.pipe(response, {end: true});
		});
		request.pipe(proxy, {end: true});
	}
	else {
		serve(request, response, finalhandler(request, response));
	}
});
server.listen(PORT);

//display URL to connect
let url = `http://localhost:${PORT}/`;
//get API URL if specified
const api_url = get_parameter_value(process.argv, 'api-url');
if(api_url) {
	url += (`?api_url=${api_url}`);
}
console.log(`Open ${url}`);
