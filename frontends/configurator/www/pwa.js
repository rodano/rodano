import {Settings} from './settings.js';

const SETTING_LAST_INSTALLATION_REJECTION = 'last_installation_rejection';
const INSTALLATION_OFFER_DELAY = 7 * 24 * 60 * 60 * 1000; //delay before offering to install application again

export const PWA = {
	Init: function() {
		const installation_reoffer_delay = new Date().getTime() - INSTALLATION_OFFER_DELAY;
		const last_installation_rejection = Settings.Get(SETTING_LAST_INSTALLATION_REJECTION);
		if(!last_installation_rejection || last_installation_rejection < installation_reoffer_delay) {

			let installation_prompt;
			window.addEventListener(
				'beforeinstallprompt',
				function(event) {
					//prevent installation for now
					event.preventDefault();
					//stash the event so it can be triggered later
					installation_prompt = event;
					document.getElementById('installation').style.display = 'flex';
				}
			);

			document.getElementById('installation_install').addEventListener(
				'click',
				function() {
					this.style.display = 'none';
					installation_prompt.prompt();
					installation_prompt.userChoice.then(result => {
						if(result.outcome === 'accepted') {
							console.log('User accepted the installation');
						}
						else {
							console.log('User dismissed the installation');
						}
						installation_prompt = undefined;
					});
				}
			);

			document.getElementById('installation_close').addEventListener(
				'click',
				function() {
					Settings.Set(SETTING_LAST_INSTALLATION_REJECTION, new Date().getTime());
					document.getElementById('installation').style.display = 'none';
				}
			);

			window.addEventListener(
				'appinstalled',
				function() {
					document.getElementById('installation').style.display = 'none';
					console.log('Application installed successfully');
				}
			);
		}
	}
};
