import {bus, ServerMessage} from './event_bus.js';
import {api_base_url} from './fetch.js';
import {TokenManager} from './token.js';

let socket;

function connect_socket() {
	const deployer_status = document.getElementById('deployer_status');
	//removing http from base URI (keeping the "s" if present)
	const base_uri = document.baseURI.substring(4);
	const server = `ws${base_uri}${api_base_url}/messaging`;
	socket = new WebSocket(server, ['access_token', TokenManager.GetToken()]);
	socket.addEventListener(
		'open',
		function() {
			deployer_status.src = 'images/green_circle.png';
			deployer_status.title = 'Connected to server';
			deployer_status.style.cursor = 'auto';
			socket.send('status');
		}
	);
	socket.addEventListener(
		'close',
		function() {
			deployer_status.src = 'images/red_circle.png';
			deployer_status.title = 'Disconnected from the server, click to reconnect';
			deployer_status.style.cursor = 'pointer';
		}
	);
	socket.addEventListener(
		'message',
		function(event) {
			const message = JSON.parse(event.data);
			console.log(message);
			bus.dispatch(new ServerMessage(message));
		}
	);
}

function reconnect_socket() {
	//socket is closed
	if(socket.readyState === WebSocket.CLOSED) {
		connect_socket();
	}
}

export const Messaging = {
	Init: function() {
		document.getElementById('deployer_status').addEventListener('click', reconnect_socket);
		//connect socket
		connect_socket();
		//reconnect socket on a regular basis
		setInterval(reconnect_socket, 2000);
	}
};
