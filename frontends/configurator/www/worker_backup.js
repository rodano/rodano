import {DBConnector} from './basic-tools/db_connector.js';

const SESSION_BACKUP_MAX_AGE = 60 * 60 * 1000; //maximum time during which a backup will be kept in milliseconds for the current session
const AUTOMATIC_BACKUP_MAX_AGE = 30 * 24 * 60 * 60 * 1000; //maximum time during which an automatic backup will be kept in milliseconds

self.addEventListener('message', async event => {
	const backup_db = new DBConnector('backups', 'date');
	await backup_db.open();
	switch(event.data.action) {
		case 'backup': {
			//create new backup
			const config = event.data.config;
			const session = event.data.session;
			const description = event.data.description;
			await backup_db.add({
				date: new Date(),
				user: 'System',
				automatic: true,
				session: session,
				description: description || 'Automatic backup',
				name: config.id,
				config: config
			});
			break;
		}
		case 'session_cleanup': {
			//remove old backups of this session
			const study = event.data.study;
			const session = event.data.session;
			const limit_time = new Date().getTime() - SESSION_BACKUP_MAX_AGE;
			await backup_db.removeSome(b => b.automatic && b.name === study && b.session === session && new Date(b.date).getTime() < limit_time);
			break;
		}
		case 'global_cleanup': {
			//remove old backups, keeping only one backup per session
			const limit_time = new Date().getTime() - AUTOMATIC_BACKUP_MAX_AGE;
			const request = backup_db.getCursor();
			const old_backup_keys = [];
			const sessions_backups = {};
			request.addEventListener('success', function(event) {
				const cursor = event.target.result;
				if(cursor) {
					const backup = cursor.value;
					const backup_date = new Date(backup.date);
					if(backup.automatic) {
						if(backup_date.getTime() < limit_time) {
							cursor.delete();
						}
						else {
							//keep only one latest backup per session among newer backups
							if(!sessions_backups.hasOwnProperty(backup.session)) {
								sessions_backups[backup.session] = backup_date;
							}
							else {
								const current_session_backup = sessions_backups[backup.session];
								if(backup_date.getTime() < current_session_backup.getTime()) {
									old_backup_keys.push(backup_date);
								}
								else {
									sessions_backups[backup.session] = backup_date;
									old_backup_keys.push(current_session_backup);
								}
							}
						}
					}
					cursor.continue();
				}
				else {
					if(old_backup_keys.length > 0) {
						console.log(`Delete ${old_backup_keys.length} old automatic backups`);
						old_backup_keys.forEach(k => backup_db.remove(k));
					}
				}
			});
		}
	}
});
