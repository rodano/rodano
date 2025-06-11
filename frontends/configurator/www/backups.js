import {UI} from './tools/ui.js';
import {DBConnector} from './basic-tools/db_connector.js';
import {UUID} from './basic-tools/uuid.js';
import {cron, logger} from './app.js';
import {bus_ui} from './bus_ui.js';
import {Settings} from './settings.js';
import {Configuration} from './configuration.js';
import {Router} from './router.js';
import {APITools} from './api_tools.js';
import {Comparator} from './comparator.js';
import {StudyHandler} from './study_handler.js';

const BACKUP_AUTOMATIC_EVERY = 5 * 60 * 1000; //time in milliseconds between each automatic backup

const backup_session = UUID.Generate();
let backup_db;

const backups_worker = new Worker('worker_backup.js', {type: 'module'});

function backup_delete_listener(event) {
	event.stop();
	const backup_li = this.parentNode.parentNode;
	const backup_date = new Date(parseInt(backup_li.dataset.id));
	backup_db.remove(backup_date).then(function() {
		backup_li.parentNode.removeChild(backup_li);
		const message = 'Backup has been deleted successfully';
		logger.info(message);
		UI.Notify(message, {tag: 'info', icon: 'images/notifications_icons/done.svg'});
	});
}

function backup_compare_listener(event) {
	event.stop();
	const backup_li = this.parentNode.parentNode;
	const backup_date = new Date(parseInt(backup_li.dataset.id));
	backup_db.get(backup_date).then(function(backup) {
		const source = {config: StudyHandler.GetStudy(), name: 'Current configuration', processed: true, revived: true};
		const target = {config: backup.config, name: `${backup.name} - ${backup.description} by ${backup.user}`, processed: true, revived: false};
		Comparator.Compare(source, target);
	});
}

function backup_select_listener() {
	backup_db.get(new Date(parseInt(this.dataset.id))).then(function(backup) {
		Router.Reset();
		Configuration.Load(backup.config);
		/**@type {HTMLDialogElement}*/ (document.getElementById('config_load')).close();
		const message = `Backup ${backup.name} - ${backup.description} loaded successfully`;
		logger.info(message);
		UI.Notify(message, {tag: 'info', icon: 'images/notifications_icons/done.svg'});
	});
}

function automatic_backup_task() {
	if(StudyHandler.HasStudy()) {
		const study = StudyHandler.GetStudy();
		//save current config
		backups_worker.postMessage({action: 'backup', session: backup_session, config: study});
		//backups_worker.postMessage({action: 'session_cleanup', session: backup_session, study: study.id});
		logger.info('Backup task triggered');
	}
}

const bus_listener = {
	onLoadStudy: function(event) {
		backups_worker.postMessage({action: 'backup', session: backup_session, description: 'Automatic backup (original config)', config: event.study});
	},
	//try to do a backup when study is unloaded (like when browser is closed)
	//this may not work with big configurations as browser only let a very short time to do things in unload event
	//see https://developer.mozilla.org/en-US/docs/Web/API/IndexedDB_API/Using_IndexedDB#Warning_About_Browser_Shutdown and https://bugzilla.mozilla.org/show_bug.cgi?id=870645
	onUnloadStudy: function(event) {
		backups_worker.postMessage({action: 'backup', session: backup_session, description: 'Automatic backup (final config)', config: event.study});
	}
};

function start_automatic_backup() {
	cron.addTask(automatic_backup_task, BACKUP_AUTOMATIC_EVERY);
	bus_ui.register(bus_listener);
}

function stop_automatic_backup() {
	cron.removeTask(automatic_backup_task);
	bus_ui.unregister(bus_listener);
}

export const Backups = {
	Init: function() {
		//prepare backup database
		backup_db = new DBConnector('backups', 'date');
		backup_db.open();

		//ask to delete previous automatic backups older than 7 days
		backups_worker.postMessage({action: 'global_cleanup'});

		const save_dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('config_save'));
		const load_dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('config_load'));

		save_dialog.querySelector('form').addEventListener(
			'submit',
			function(event) {
				event.stop();
				this.disable();
				UI.StartLoading();
				const study = StudyHandler.GetStudy();
				const backup = {
					date: new Date(),
					user: APITools.GetUser() ? APITools.GetUser().login : 'NA',
					name: study.id,
					description: this['description'].value,
					config: study
				};
				backup_db.add(backup).then(() => {
					//restore ui
					this.enable();
					UI.StopLoading();
					save_dialog.close();
					//notify user
					const message = 'Study saved successfully';
					logger.info(message);
					UI.Notify(message, {tag: 'info', icon: 'images/notifications_icons/done.svg'});
				});
			}
		);

		document.getElementById('config_save_cancel').addEventListener(
			'click',
			function() {
				save_dialog.close();
			}
		);

		document.getElementById('config_load_close').addEventListener(
			'click',
			function() {
				load_dialog.close();
			}
		);

		document.getElementById('config_load_reset').addEventListener(
			'click',
			function() {
				this.setAttribute('disabled', 'disabled');
				UI.StartLoading();
				backup_db.removeAll().then(() => {
					document.getElementById('config_load_list').empty();
					//restore ui
					this.removeAttribute('disabled');
					UI.StopLoading();
					//notify user
					const message = 'All backups deleted successfully';
					logger.info(message);
					UI.Notify(message, {tag: 'info', icon: 'images/notifications_icons/done.svg'});
				});
			}
		);

		//run backup task if asked
		if(Settings.Get('automatic_backup')) {
			start_automatic_backup();
		}

		bus_ui.register({
			onUpdateSetting: function(event) {
				if(event.setting === 'automatic_backup') {
					if(event.value) {
						start_automatic_backup();
					}
					else {
						stop_automatic_backup();
					}
				}
			}
		});
	},
	Add: function(backup) {
		backup_db.add(backup);
	},
	Get: function(backup_date) {
		return backup_db.get(backup_date);
	},
	GetAll: function() {
		return backup_db.getAll();
	},
	OpenSaveDialog: function() {
		const save_dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('config_save'));
		save_dialog.querySelector('form').reset();
		save_dialog.showModal();
	},
	OpenLoadDialog: function() {
		//manage ui
		const config_load_list = document.getElementById('config_load_list');
		config_load_list.empty();
		/**@type {HTMLDialogElement}*/ (document.getElementById('config_load')).showModal();

		backup_db.getAll().then(function(backups) {
			backups.forEach(function(backup) {
				const backup_li = document.createFullElement('li', {title: 'Load this backup'});
				backup_li.dataset.id = backup.date.getTime();
				backup_li.appendChild(document.createFullElement('time', {}, backup.date.toUTCFullDisplay()));
				backup_li.appendChild(document.createTextNode(`${backup.name} - ${backup.description} by ${backup.user}`));
				backup_li.addEventListener('click', backup_select_listener);
				const backup_actions = document.createFullElement('span');
				backup_li.appendChild(backup_actions);
				//compare config button
				const backup_compare = document.createFullElement('img', {src: 'images/page_white_copy.png', title: 'Compare current config with this backup', alt: 'Compare'});
				backup_compare.addEventListener('click', backup_compare_listener);
				backup_actions.appendChild(backup_compare);
				//delete config button
				const backup_delete = document.createFullElement('img', {src: 'images/cross.png', title: 'Delete this backup', alt: 'Delete'});
				backup_delete.addEventListener('click', backup_delete_listener);
				backup_actions.appendChild(backup_delete);
				config_load_list.appendChild(backup_li);
			});
		});
	}
};
