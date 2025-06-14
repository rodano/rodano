const CACHE_NAME = 'rodano-configurator-cache-v5.0';

//list of files to cache (all files used by the app)
//do not edit this list manually
//this list is generated by the command npm run-script update-worker-cache
//BEGINNING OF THE LIST
const FILES = [
	'api.js',
	'api_tools.js',
	'app.js',
	'backups.js',
	'basic-tools/bus.js',
	'basic-tools/csv.js',
	'basic-tools/db_connector.js',
	'basic-tools/dom_extension.js',
	'basic-tools/driver.demo.js',
	'basic-tools/driver.js',
	'basic-tools/extension.js',
	'basic-tools/hash.js',
	'basic-tools/loader.js',
	'basic-tools/lzw.js',
	'basic-tools/queue.js',
	'basic-tools/reviver.js',
	'basic-tools/svg.js',
	'basic-tools/uuid.js',
	'bus_ui.js',
	'changelogs.js',
	'comparator.js',
	'configuration.js',
	'configuration_serialized.js',
	'constraint_helpers.js',
	'contextual_menu.js',
	'css/base.css',
	'css/theme-dark.css',
	'css/theme-light.css',
	'dashboards.js',
	'digests.js',
	'documentation_selection.js',
	'elements/constrained_input.html',
	'elements/constrained_input.js',
	'elements/localized_input.html',
	'elements/localized_input.js',
	'elements/palette.html',
	'elements/palette.js',
	'entities_forms.js',
	'entities_placeholders.js',
	'favorites.js',
	'form_helpers.js',
	'form_static_actions.js',
	'forms/action.html',
	'forms/action.js',
	'forms/cell.css',
	'forms/cell.html',
	'forms/cell.js',
	'forms/chart.html',
	'forms/chart.js',
	'forms/cms_section.html',
	'forms/cms_section.js',
	'forms/cms_widget.html',
	'forms/cms_widget.js',
	'forms/column.html',
	'forms/column.js',
	'forms/cron.html',
	'forms/cron.js',
	'forms/dataset_model.css',
	'forms/dataset_model.html',
	'forms/dataset_model.js',
	'forms/event_group.html',
	'forms/event_group.js',
	'forms/event_model.css',
	'forms/event_model.html',
	'forms/event_model.js',
	'forms/feature.html',
	'forms/feature.js',
	'forms/field_model.css',
	'forms/field_model.html',
	'forms/field_model.js',
	'forms/form_model.css',
	'forms/form_model.html',
	'forms/form_model.js',
	'forms/language.html',
	'forms/language.js',
	'forms/layout.html',
	'forms/layout.js',
	'forms/menu.html',
	'forms/menu.js',
	'forms/payment_plan.html',
	'forms/payment_plan.js',
	'forms/payment_step.html',
	'forms/payment_step.js',
	'forms/privacy_policy.html',
	'forms/privacy_policy.js',
	'forms/profile.html',
	'forms/profile.js',
	'forms/report.html',
	'forms/report.js',
	'forms/resource_category.html',
	'forms/resource_category.js',
	'forms/rule_definition_action.css',
	'forms/rule_definition_action.html',
	'forms/rule_definition_action.js',
	'forms/rule_definition_property.html',
	'forms/rule_definition_property.js',
	'forms/scope_model.css',
	'forms/scope_model.html',
	'forms/scope_model.js',
	'forms/study.html',
	'forms/study.js',
	'forms/timeline_graph.html',
	'forms/timeline_graph.js',
	'forms/timeline_graph_section.css',
	'forms/timeline_graph_section.html',
	'forms/timeline_graph_section.js',
	'forms/timeline_graph_section_reference.css',
	'forms/timeline_graph_section_reference.html',
	'forms/timeline_graph_section_reference.js',
	'forms/validator.html',
	'forms/validator.js',
	'forms/workflow.css',
	'forms/workflow.html',
	'forms/workflow.js',
	'forms/workflow_state.html',
	'forms/workflow_state.js',
	'forms/workflow_summary.css',
	'forms/workflow_summary.html',
	'forms/workflow_summary.js',
	'forms/workflow_widget.css',
	'forms/workflow_widget.html',
	'forms/workflow_widget.js',
	'formulas_help.js',
	'hooks.js',
	'images/add.png',
	'images/anchor.png',
	'images/app_icons/launcher.png',
	'images/app_icons/launcher_32.png',
	'images/app_icons/launcher_64.png',
	'images/application_put.png',
	'images/arrow_down.png',
	'images/arrow_ew.png',
	'images/arrow_nsew.png',
	'images/arrows_up_down.png',
	'images/bin.png',
	'images/bullet_arrow_down.png',
	'images/bullet_arrow_right.png',
	'images/bullet_arrow_up.png',
	'images/bullet_green.png',
	'images/bullet_minus.png',
	'images/bullet_plus.png',
	'images/bullet_red.png',
	'images/bullet_right.png',
	'images/bullet_stop_alt.png',
	'images/bullet_toggle_minus.png',
	'images/bullet_toggle_plus.png',
	'images/bullet_wrench.png',
	'images/bullet_yellow.png',
	'images/cancel.png',
	'images/check_error.png',
	'images/cog.png',
	'images/cog_edit.png',
	'images/control_play.png',
	'images/cross.png',
	'images/database.png',
	'images/database_go.png',
	'images/database_table.png',
	'images/delete.png',
	'images/disk.png',
	'images/disk_upload.png',
	'images/drive_go.png',
	'images/entities_icons/accept.png',
	'images/entities_icons/application_view_list.png',
	'images/entities_icons/book_open.png',
	'images/entities_icons/bricks.png',
	'images/entities_icons/bullet_wrench.png',
	'images/entities_icons/chart_bar.png',
	'images/entities_icons/chart_bar_edit.png',
	'images/entities_icons/chart_bar_link.png',
	'images/entities_icons/chart_curve.png',
	'images/entities_icons/chart_curve_edit.png',
	'images/entities_icons/clock.png',
	'images/entities_icons/cog.png',
	'images/entities_icons/cog_edit.png',
	'images/entities_icons/comments.png',
	'images/entities_icons/flag_red.png',
	'images/entities_icons/folder.png',
	'images/entities_icons/group.png',
	'images/entities_icons/hourglass.png',
	'images/entities_icons/hourglass_link.png',
	'images/entities_icons/key.png',
	'images/entities_icons/key_delete.png',
	'images/entities_icons/layout_sidebar.png',
	'images/entities_icons/money.png',
	'images/entities_icons/page.png',
	'images/entities_icons/page_edit.png',
	'images/entities_icons/page_white_excel.png',
	'images/entities_icons/page_white_wrench.png',
	'images/entities_icons/report_key.png',
	'images/entities_icons/script_link.png',
	'images/entities_icons/tag_blue.png',
	'images/entities_icons/tag_blue_delete.png',
	'images/entities_icons/time.png',
	'images/entities_icons/user.png',
	'images/entities_icons/zoom.png',
	'images/error.png',
	'images/exclamation.png',
	'images/find.png',
	'images/folder_page.png',
	'images/heart_add.png',
	'images/icon.png',
	'images/information.png',
	'images/loading.png',
	'images/magnifier.png',
	'images/notifications_icons/done.svg',
	'images/notifications_icons/error.svg',
	'images/notifications_icons/upload.svg',
	'images/notifications_icons/warning.svg',
	'images/page_copy.png',
	'images/page_paste.png',
	'images/page_white_copy.png',
	'images/resultset_next.png',
	'images/resultset_previous.png',
	'images/script_go.png',
	'images/section_collapsed.png',
	'images/section_expanded.png',
	'images/star.png',
	'images/star_grey.png',
	'images/stop_blue.png',
	'images/stop_green.png',
	'images/stop_red.png',
	'images/tick.png',
	'images/untick.png',
	'importer.js',
	'index.html',
	'keyboard.js',
	'languages.js',
	'layout.js',
	'logs.js',
	'manifest.json',
	'matrices.js',
	'media_types.js',
	'menu.js',
	'model/config/chart_type.js',
	'model/config/comparator_utils.js',
	'model/config/compare.js',
	'model/config/config_helpers.js',
	'model/config/data_type.js',
	'model/config/date_aggregation_function.js',
	'model/config/entities.js',
	'model/config/entities/action.js',
	'model/config/entities/attribute_criterion.js',
	'model/config/entities/cell.js',
	'model/config/entities/changelog.js',
	'model/config/entities/chart.js',
	'model/config/entities/chart_range.js',
	'model/config/entities/chart_request.js',
	'model/config/entities/cms_action.js',
	'model/config/entities/cms_layout.js',
	'model/config/entities/cms_section.js',
	'model/config/entities/cms_widget.js',
	'model/config/entities/column.js',
	'model/config/entities/cron.js',
	'model/config/entities/dataset_model.js',
	'model/config/entities/event_configuration_hook.js',
	'model/config/entities/event_group.js',
	'model/config/entities/event_model.js',
	'model/config/entities/feature.js',
	'model/config/entities/field_model.js',
	'model/config/entities/form_model.js',
	'model/config/entities/language.js',
	'model/config/entities/layout.js',
	'model/config/entities/line.js',
	'model/config/entities/menu.js',
	'model/config/entities/payment_distribution.js',
	'model/config/entities/payment_plan.js',
	'model/config/entities/payment_step.js',
	'model/config/entities/possible_value.js',
	'model/config/entities/privacy_policy.js',
	'model/config/entities/profile.js',
	'model/config/entities/profile_right.js',
	'model/config/entities/report.js',
	'model/config/entities/resource_category.js',
	'model/config/entities/right.js',
	'model/config/entities/rule.js',
	'model/config/entities/rule_action.js',
	'model/config/entities/rule_action_parameter.js',
	'model/config/entities/rule_condition.js',
	'model/config/entities/rule_condition_criterion.js',
	'model/config/entities/rule_condition_list.js',
	'model/config/entities/rule_constraint.js',
	'model/config/entities/rule_definition_action.js',
	'model/config/entities/rule_definition_action_parameter.js',
	'model/config/entities/rule_definition_property.js',
	'model/config/entities/rule_evaluation.js',
	'model/config/entities/scope_criterion_right.js',
	'model/config/entities/scope_model.js',
	'model/config/entities/selection_node.js',
	'model/config/entities/study.js',
	'model/config/entities/timeline_graph.js',
	'model/config/entities/timeline_graph_section.js',
	'model/config/entities/timeline_graph_section_position.js',
	'model/config/entities/timeline_graph_section_reference.js',
	'model/config/entities/timeline_graph_section_reference_entry.js',
	'model/config/entities/timeline_graph_section_scale.js',
	'model/config/entities/validator.js',
	'model/config/entities/value_source.js',
	'model/config/entities/value_source_criteria.js',
	'model/config/entities/visibility_criteria.js',
	'model/config/entities/workflow.js',
	'model/config/entities/workflow_state.js',
	'model/config/entities/workflow_states_selector.js',
	'model/config/entities/workflow_summary.js',
	'model/config/entities/workflow_summary_column.js',
	'model/config/entities/workflow_widget.js',
	'model/config/entities/workflow_widget_column.js',
	'model/config/entities_categories.js',
	'model/config/entities_config.js',
	'model/config/entities_config_bus.js',
	'model/config/entities_hooks.js',
	'model/config/event_time_unit.js',
	'model/config/field_model_type.js',
	'model/config/formulas.js',
	'model/config/label_type.js',
	'model/config/layout_type.js',
	'model/config/migrator.js',
	'model/config/native_types.js',
	'model/config/node.js',
	'model/config/node_displayable.js',
	'model/config/operator.js',
	'model/config/profile_right_type.js',
	'model/config/report.js',
	'model/config/rule_condition_mode.js',
	'model/config/rule_entities.js',
	'model/config/static_actions.js',
	'model/config/timeline_section_mark.js',
	'model/config/timeline_section_scale_position.js',
	'model/config/timeline_section_type.js',
	'model/config/trigger.js',
	'model/config/user_attribute.js',
	'model/config/utils.js',
	'model/config/visibility_criteria_action.js',
	'model/config/widget_types_config.js',
	'model/config/workflow_entities.js',
	'model/config/workflow_state_matcher.js',
	'model/config/workflow_widget_column_type.js',
	'model/data/data_helpers.js',
	'model/data/entities/dataset.js',
	'model/data/entities/event.js',
	'model/data/entities/field.js',
	'model/data/entities/form.js',
	'model/data/entities/scope.js',
	'model/data/entities_data.js',
	'model/templates/repository.js',
	'model_config.js',
	'model_data.js',
	'node_clipboard.js',
	'node_contextual_menu.js',
	'node_tools.js',
	'placeholders/dataset_model.html',
	'placeholders/dataset_model.js',
	'placeholders/event_model.css',
	'placeholders/event_model.html',
	'placeholders/event_model.js',
	'placeholders/menu.html',
	'placeholders/menu.js',
	'placeholders/profile.html',
	'placeholders/profile.js',
	'placeholders/workflow.css',
	'placeholders/workflow.html',
	'placeholders/workflow.js',
	'pwa.js',
	'reports.js',
	'router.js',
	'rule.js',
	'rule_definitions.js',
	'rule_dsl.js',
	'search.js',
	'settings.js',
	'shortcuts.js',
	'statistics.js',
	'study_handler.js',
	'study_loader.js',
	'study_tree.js',
	'template_loader.js',
	'template_repositories.js',
	'templates.js',
	'themes.js',
	'tools/arrows_up_down.png',
	'tools/assert.js',
	'tools/bundle.js',
	'tools/cron.js',
	'tools/custom_dom_extension.js',
	'tools/custom_extension.js',
	'tools/dom_assert.js',
	'tools/effects.demo.js',
	'tools/effects.js',
	'tools/feature.js',
	'tools/forms.js',
	'tools/geometry.js',
	'tools/logger.js',
	'tools/network.js',
	'tools/parser.js',
	'tools/partial_date.js',
	'tools/suite.js',
	'tools/ui.js',
	'transfer.js',
	'tree.js',
	'welcome.js',
	'wizards.js',
	'wizards/arrow.png',
	'wizards/arrows_up_down.png',
	'wizards/bin.png',
	'wizards/check-checked-green.png',
	'wizards/cross.png',
	'wizards/date.png',
	'wizards/disk.png',
	'wizards/form_model.css',
	'wizards/form_model.html',
	'wizards/form_model.js',
	'wizards/number.png',
	'wizards/payment.css',
	'wizards/payment.html',
	'wizards/payment.js',
	'wizards/radio-unchecked.png',
	'wizards/study.css',
	'wizards/study.html',
	'wizards/study.js',
	'wizards/text_ab.png',
	'wizards/text_align_left.png',
	'wizards/text_horizontalrule.png',
	'wizards/text_list_bullets.png',
	'wizards/validator.css',
	'wizards/validator.html',
	'wizards/validator.js',
	'worker_backup.js',
	'worker_cache.js'
];
//END OF THE LIST

//add all static resources to the cache
self.addEventListener('install', function(event) {
	console.log('Install cache');
	event.waitUntil(
		caches.open(CACHE_NAME)
			.then(function(cache) {
				console.log(`Cache ${FILES.length} static resources`);
				return cache.addAll(FILES);
			})
	);
});

//delete old versions of cache
self.addEventListener('activate', function(event) {
	console.log('Activate cache');
	event.waitUntil(
		caches.keys()
			.then(function(keys) {
				const old_keys = keys.filter(k => CACHE_NAME !== k);
				if(old_keys.length > 0) {
					console.log('Delete old versions of cache', old_keys);
					return Promise.all(old_keys.map(k => caches.delete(k)));
				}
				return Promise.resolve([true]);
			})
	);
});

self.addEventListener('message', function(event) {
	console.log('Receiving event', event.data);
	switch(event.data.action) {
		case 'clean':
			caches.delete(CACHE_NAME);
			break;
	}
});

function cache_request_predicate(response) {
	//templates
	//disable this for now because we're not able to make a difference between templates and whole configuration
	/*if(response.headers.get('Content-Type') === 'application/json') {
		return true;
	}*/
	//Google fonts
	console.log(response.url);
	if(response.url.startsWith('https://fonts.googleapis.com') || response.url.startsWith('https://fonts.gstatic.com') && response.headers.get('Content-Type') === 'font/woff2') {
		return true;
	}
	return false;
}

function get_response(request) {
	return caches.match(request).then(function(response) {
		//return cache response if any
		if(response) {
			//console.log(`Using cache for request to ${request.url}`);
			return response;
		}
		//cache response from templates server
		console.log(`Doing uncached request to ${request.url}`);
		return fetch(request).then(function(response) {
			//add response to cache if the response is good
			if(response.status === 200) {
				const cloned_response = response.clone();
				//add only response that match a predicate
				if(cache_request_predicate(response)) {
					console.log(`Caching request to ${request.url}`);
					caches.open(CACHE_NAME).then(function(cache) {
						cache.put(request, cloned_response);
					});
				}
			}
			return response;
		});
	});
}

//handle requests
self.addEventListener('fetch', function(event) {
	event.respondWith(get_response(event.request));
});
