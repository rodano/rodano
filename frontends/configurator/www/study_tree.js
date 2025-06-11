import {Entities} from './model/config/entities.js';
import {Languages} from './languages.js';
import {bus_ui} from './bus_ui.js';
import {Router} from './router.js';
import {Tree} from './tree.js';
import {NodeTools} from './node_tools.js';

let tree;

export const StudyTree = {
	Init: function() {
		//TODO improve script loading order to be able to put this above
		const HIDDEN_ENTITIES_TREE = [
			Entities.Rule,
			Entities.RuleConstraint,
			Entities.PossibleValue,
			Entities.Layout,
			Entities.Column,
			Entities.Line,
			Entities.RuleDefinitionActionParameter,
			Entities.TimelineGraphSectionReferenceEntry,
			Entities.CMSLayout,
			Entities.PaymentDistribution,
			Entities.SelectionNode
		];
		const HIDDEN_ENTITIES_SEARCH = [
			Entities.RuleDefinitionActionParameter,
			Entities.RuleConstraint,
			Entities.RuleAction,
			Entities.RuleActionParameter
		];

		const tree_container = document.getElementById('tree');

		bus_ui.register({
			onLoadStudy: function(event) {
				//create and display tree
				tree = new Tree(event.study, HIDDEN_ENTITIES_TREE);
				tree.draw(tree_container);

				//restore state from settings
				if(event.settings?.tree?.state) {
					tree.restore(event.settings.tree.state);
				}
			},
			onUnloadStudy: function(event) {
				//save state to settings
				if(!event.settings.tree) {
					event.settings.tree = {};
				}
				event.settings.tree.state = tree.save();

				//destroy tree
				tree.destroy();
				tree = undefined;
				tree_container.empty();
			}
		});

		//manage buttons
		document.getElementById('tree_collapse_all').addEventListener(
			'click',
			function() {
				tree.collapse();
			}
		);
		document.getElementById('tree_expand_all').addEventListener(
			'click',
			function() {
				tree.expand();
			}
		);

		//manage search
		const search_form = document.getElementById('tree_search');
		const search_input = search_form['search'];
		search_input.value = '';

		const search_results = document.getElementById('tree_search_results');

		let result_node;

		NodeTools.ManageAutocomplete(
			search_input,
			search_results,
			function(node) {
				return !HIDDEN_ENTITIES_SEARCH.includes(node.getEntity());
			},
			function(node) {
				result_node = node;
			},
			Router.SelectNode
		);

		//save tree state
		let state;

		function search_submit(event) {
			event.stop();
			search_results.style.display = 'none';
			//something is selected
			if(result_node) {
				search_input.value = '';
				Router.SelectNode(result_node);
			}
			//normal search
			else {
				const value = search_input.value;
				//save state before applying filter
				if(value) {
					state = tree.save();
				}
				//restore state to previous state if any and if search is empty
				else if(state) {
					tree.restore(state);
				}
				//filter tree
				tree.filter(function(node) {
					//filter current node
					if(!value) {
						return true;
					}
					//search in id
					if(node.id?.nocaseIncludes(value)) {
						return true;
					}
					//search in shortname
					if(node.getLocalizedShortname?.(Languages.GetLanguage()).nocaseIncludes(value)) {
						return true;
					}
					//search in longname
					if(node.getLocalizedLongname?.(Languages.GetLanguage()).nocaseIncludes(value)) {
						return true;
					}
					return false;
				});
			}
		}

		//add listeners to filter tree when search form is validated
		search_form.addEventListener('submit', search_submit);
		//search event works only in Chrome for now but it must be listen to clear search when the input's reset button is clicked
		search_input.addEventListener('search', search_submit);
	},
	HasNode: function(node) {
		return tree.hasNode(node);
	},
	GetSelection: function() {
		return tree.selection;
	},
	SelectClosestNode: function(node) {
		let tree_node = node;
		while(!tree.hasNode(tree_node)) {
			tree_node = tree_node.getParent();
		}
		tree.find(tree_node).highlight();
	},
	Filter: function(filter) {
		if(filter) {
			document.getElementById('tree_advanced_search').classList.add('highlight');
		}
		else {
			document.getElementById('tree_advanced_search').classList.remove('highlight');
		}
		tree.filter(filter);
	},
	//TODO remove this and create business oriented methods
	GetTree: function() {
		return tree;
	}
};
