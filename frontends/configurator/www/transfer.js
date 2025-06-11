export const Transfer = {
	Init: function() {
		document.getElementById('transfer_start').addEventListener(
			'click',
			function() {
				const peer = new RTCPeerConnection(); //{iceServers : [{url : 'stun:stun.l.google.com:19302'}]});
				peer.onicecandidate = function() {
					console.log('peer ice candidate');
					document.getElementById('transfer_users').appendChild(document.createFullElement('li', {}));
					//signalingChannel.send(JSON.stringify({candidate : event.candidate}));
				};
				peer.onconnectionstatechange = function() {
					console.log('peer connection');
					const channel = peer.createDataChannel('configs', {});

					channel.onmessage = function(event) {
						document.getElementById('transfer_user_files').appendChild(document.createFullElement('li', {}, event.data));
					};

					channel.onopen = function() {
						channel.send('Hello');
					};
				};
				//create fake stream to launch connection
				const media = navigator.mediaDevices.getUserMedia({audio: true});
				media.then(function(stream) {
					console.log('add stream on peer');
					peer.addStream(stream);
					//peer.createOffer(offer);
				});
				media.catch(function(error) {
					console.error(error.name);
				});
			}
		);
	},
	Open: function() {
		/**@type {HTMLDialogElement}*/ (document.getElementById('transfer')).showModal();
	}
};
