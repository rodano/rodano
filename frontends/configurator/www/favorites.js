import {StudyHandler} from './study_handler.js';
import {Router} from './router.js';
import {bus_ui} from './bus_ui.js';
import {NodeTools} from './node_tools.js';
import {MediaTypes} from './media_types.js';

const favorites = [];

export const Favorites = {
	Init: function() {
		const favorites_window = document.getElementById('favorites');

		favorites_window.addEventListener(
			'dragover',
			function(event) {
				if(event.dataTransfer.types.includes(MediaTypes.NODE_GLOBAL_ID)) {
					event.preventDefault();
					event.dataTransfer.dropEffect = 'link';
					this.classList.add('dragover');
				}
			}
		);

		favorites_window.addEventListener(
			'dragleave',
			function() {
				this.classList.remove('dragover');
			}
		);

		favorites_window.addEventListener(
			'drop',
			function(event) {
				event.preventDefault();
				this.classList.remove('dragover');
				Favorites.Add(StudyHandler.GetStudy().getNode(event.dataTransfer.getData(MediaTypes.NODE_GLOBAL_ID)));
			}
		);

		//mange moving window
		let offset;

		function move_favorites(event) {
			const left = Math.max(0, Math.min(event.clientX - offset.left, window.innerWidth - favorites_window.offsetWidth));
			favorites_window.style.left = `${left}px`;
			const top = Math.max(0, Math.min(event.clientY - offset.top, window.innerHeight - favorites_window.offsetHeight));
			favorites_window.style.top = `${top}px`;
		}

		const favorites_handle = document.getElementById('favorites_handle');
		favorites_handle.addEventListener(
			'mousedown',
			function(event) {
				const position = favorites_window.getPosition();
				offset = {
					left: event.clientX - position.left,
					top: event.clientY - position.top
				};
				document.addEventListener('mousemove', move_favorites);
				document.body.classList.add('grabbing');
			}
		);
		document.addEventListener(
			'mouseup',
			function() {
				document.body.classList.remove('grabbing');
				document.removeEventListener('mousemove', move_favorites);
			}
		);

		document.getElementById('favorites_clear').addEventListener(
			'click',
			function() {
				favorites.slice().forEach(Favorites.Remove);
			}
		);

		document.getElementById('favorites_close').addEventListener(
			'click',
			function() {
				favorites_window.style.display = 'none';
			}
		);

		bus_ui.register({
			onDelete: function(event) {
				if(Favorites.IsFavorited(event.node)) {
					Favorites.Remove(event.node);
				}
			},
			onLoadStudy: function(event) {
				//restore study favorites
				if(event.settings?.favorites) {
					event.settings.favorites.forEach(function(favorite_id) {
						try {
							const favorite = event.study.getNode(favorite_id);
							Favorites.Add(favorite);
						}
						catch {
							console.warn(`Node with id ${favorite_id} no longer exists`);
						}
					});
				}
			},
			onUnloadStudy: function(event) {
				//save study favorites
				event.settings.favorites = favorites.map(f => f.getGlobalId());
			}
		});
	},
	UpdateFavoriteImage: function(image, favorite) {
		if(favorite) {
			image.setAttribute('src', 'images/star.png');
			image.setAttribute('alt', 'Favorite');
			image.setAttribute('title', 'Remove node from favorites');
		}
		else {
			image.setAttribute('src', 'images/star_grey.png');
			image.setAttribute('alt', 'Not favorite');
			image.setAttribute('title', 'Save node as a favorite');
		}
	},
	Add: function(node) {
		//check node is not already favorited
		if(!favorites.includes(node)) {
			//update model
			favorites.push(node);
			//update favorites list
			const favorite_li = document.createElement('li');
			favorite_li.appendChild(NodeTools.Draw(node, undefined, true));
			const node_delete = document.createFullElement('img', {src: 'images/cross.png', title: 'Delete'});
			node_delete.addEventListener('click', Favorites.Remove.bind(undefined, node));
			favorite_li.appendChild(node_delete);
			document.getElementById('favorites_nodes').appendChild(favorite_li);
			//update editor
			if(node === Router.selectedNode) {
				Favorites.UpdateFavoriteImage(document.querySelector(`#edit_${node.getEntity().id} h2 > img`), true);
			}
		}
		document.getElementById('favorites').style.display = 'block';
	},
	Remove: function(node) {
		//check node is favorited
		if(favorites.includes(node)) {
			//update model
			favorites.removeElement(node);
			//update favorites list
			const node_list = document.getElementById('favorites_nodes');
			const favorite_link = node_list.querySelector(`a[data-node-global-id="${node.getGlobalId()}"]`);
			node_list.removeChild(favorite_link.parentNode);
			//update editor
			if(node === Router.selectedNode) {
				Favorites.UpdateFavoriteImage(document.querySelector(`#edit_${node.getEntity().id} h2 > img`), false);
			}
		}
	},
	IsFavorited: function(node) {
		return favorites.includes(node);
	},
	Toggle: function(node) {
		if(Favorites.IsFavorited(node)) {
			Favorites.Remove(Router.selectedNode);
		}
		else {
			Favorites.Add(Router.selectedNode);
		}
	}
};
