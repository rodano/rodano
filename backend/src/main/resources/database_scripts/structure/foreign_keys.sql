/* project */
alter table project
    add constraint fk_project_epro_profile foreign key (project_id, epro_profile_id) references profile (project_id, profile_id),
    add constraint fk_project_active_config_version foreign key (active_config_version_fk) references project_config_version (pk);

alter table project_audit
	add constraint fk_project_audit_action foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_project_audit_user foreign key (audit_user_fk) references user (pk),
	add constraint fk_project_audit_robot foreign key (audit_robot_fk) references robot (pk);

alter table project_language
    add constraint fk_proj_lang_project foreign key (project_id) references project (project_id);

alter table project_rule_tag
    add constraint fk_proj_rule_tag_project foreign key (project_id) references project (project_id);

alter table project_config_version
	add constraint fk_project_config_version_project foreign key (project_id) references project (project_id),
	add constraint fk_project_config_version_created foreign key (created_by) references user (pk),
	add constraint fk_project_config_version_published foreign key (published_by) references user (pk);

/* rule definition */
alter table rule_definition_property
    add constraint fk_rule_def_prop_project foreign key (project_id) references project (project_id);

alter table rule_definition_action
    add constraint fk_rule_def_action_project foreign key (project_id) references project (project_id);

alter table rule_definition_action_parameter
    add constraint fk_rule_def_action_param_action foreign key (project_id, rule_definition_action_id) references rule_definition_action (project_id, rule_definition_action_id),
    add constraint fk_rule_def_action_param_project foreign key (project_id) references project (project_id);

/* scope */
alter table scope
    add constraint fk_scope_scope_model_id foreign key (project_id, scope_model_id) references scope_model (project_id, scope_model_id);

alter table scope_audit
	add constraint fk_scope_audit_object_fk foreign key (audit_object_fk) references scope (pk),
	add constraint fk_scope_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_scope_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_scope_audit_robot_fk foreign key (audit_robot_fk) references robot (pk),
	add constraint fk_scope_audit_scope_model_id foreign key (project_id, scope_model_id) references scope_model (project_id, scope_model_id);

alter table scope_model
    add constraint fk_scope_model_project foreign key (project_id) references project (project_id);

alter table scope_model_parent
    add constraint fk_scope_model_child foreign key (project_id, child_scope_model_id) references scope_model (project_id, scope_model_id),
    add constraint fk_scope_model_parent foreign key (project_id, parent_scope_model_id) references scope_model (project_id, scope_model_id);

alter table scope_model_dataset_model
    add constraint fk_scope_model_dataset_model_scope foreign key (project_id, scope_model_id) references scope_model (project_id, scope_model_id),
    add constraint fk_scope_model_dataset_model_dataset foreign key (project_id, dataset_model_id) references dataset_model (project_id, dataset_model_id);

alter table scope_model_form_model
    add constraint fk_scope_model_form_model_scope foreign key (project_id, scope_model_id) references scope_model (project_id, scope_model_id),
    add constraint fk_scope_model_form_model_form foreign key (project_id, form_model_id) references form_model (project_id, form_model_id);

alter table scope_model_workflow
    add constraint fk_scope_model_wf_scope foreign key (project_id, scope_model_id) references scope_model (project_id, scope_model_id),
    add constraint fk_scope_model_wf_workflow foreign key (project_id, workflow_id) references workflow (project_id, workflow_id);

alter table scope_model_workflow_state_selector
    add constraint fk_scope_model_wf_state_sel_scope foreign key (project_id, scope_model_id) references scope_model (project_id, scope_model_id),
    add constraint fk_scope_model_wf_state_sel_workflow foreign key (project_id, workflow_id) references workflow (project_id, workflow_id),
    add constraint fk_scope_model_wf_state_sel_state foreign key (project_id, workflow_state_id) references workflow_state (project_id, workflow_state_id);

alter table scope_relation
	add constraint fk_scope_relation_scope_fk foreign key (scope_fk) references scope (pk),
	add constraint fk_scope_relation_parent_fk foreign key (parent_fk) references scope (pk);

/* event */
alter table event
    add constraint fk_event_scope_fk foreign key (scope_fk) references scope (pk),
    add constraint fk_event_scope_model_id foreign key (project_id, scope_model_id) references scope_model (project_id, scope_model_id),
    add constraint fk_event_event_model_id foreign key (project_id, event_model_id) references event_model (project_id, event_model_id);

alter table event_audit
	add constraint fk_event_audit_object_fk foreign key (audit_object_fk) references event (pk),
	add constraint fk_event_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_event_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_event_audit_robot_fk foreign key (audit_robot_fk) references robot (pk),
	add constraint fk_event_audit_scope_model_id foreign key (project_id, scope_model_id) references scope_model (project_id, scope_model_id),
	add constraint fk_event_audit_event_model_id foreign key (project_id, event_model_id) references event_model (project_id, event_model_id);

alter table event_model
    add constraint fk_event_model_project foreign key (project_id) references project (project_id),
    add constraint fk_event_model_scope foreign key (project_id, scope_model_id) references scope_model (project_id, scope_model_id),
    add constraint fk_event_model_group foreign key (project_id, event_group_id) references event_group (project_id, event_group_id);

alter table event_model_dataset_model
    add constraint fk_event_model_dataset_model_event foreign key (project_id, event_model_id) references event_model (project_id, event_model_id),
    add constraint fk_event_model_dataset_model_dataset foreign key (project_id, dataset_model_id) references dataset_model (project_id, dataset_model_id);

alter table event_model_form_model
    add constraint fk_event_model_form_model_event foreign key (project_id, event_model_id) references event_model (project_id, event_model_id),
    add constraint fk_event_model_form_model_form foreign key (project_id, form_model_id) references form_model (project_id, form_model_id);

alter table event_model_workflow
    add constraint fk_event_model_wf_event foreign key (project_id, event_model_id) references event_model (project_id, event_model_id),
    add constraint fk_event_model_wf_workflow foreign key (project_id, workflow_id) references workflow (project_id, workflow_id);

alter table event_model_implied_event
    add constraint fk_event_model_impl_event foreign key (project_id, event_model_id) references event_model (project_id, event_model_id),
    add constraint fk_event_model_impl_event_target foreign key (project_id, implied_event_model_id) references event_model (project_id, event_model_id);

alter table event_model_blocked_event
    add constraint fk_event_model_blocked_event foreign key (project_id, event_model_id) references event_model (project_id, event_model_id),
    add constraint fk_event_model_blocked_event_target foreign key (project_id, blocked_event_model_id) references event_model (project_id, event_model_id);

alter table event_model_deadline_reference
    add constraint fk_event_model_deadline_ref_event foreign key (project_id, event_model_id) references event_model (project_id, event_model_id),
    add constraint fk_event_model_deadline_reference foreign key (project_id, reference_event_model_id) references event_model (project_id, event_model_id);

alter table event_group
    add constraint fk_ev_group_scope foreign key (project_id, scope_model_id) references scope_model (project_id, scope_model_id);

/* dataset */
alter table dataset
	add constraint fk_dataset_scope_fk foreign key (scope_fk) references scope (pk),
	add constraint fk_dataset_event_fk foreign key (event_fk) references event (pk),
	add constraint fk_dataset_dataset_model foreign key (project_id, dataset_model_id) references dataset_model (project_id, dataset_model_id);

alter table dataset_audit
	add constraint fk_dataset_audit_object_fk foreign key (audit_object_fk) references dataset (pk),
	add constraint fk_dataset_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_dataset_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_dataset_audit_robot_fk foreign key (audit_robot_fk) references robot (pk);

alter table dataset_model
    add constraint fk_dataset_model_project foreign key (project_id) references project (project_id);

/* field */
alter table field
    add constraint fk_field_dataset_fk foreign key (dataset_fk) references dataset (pk),
    add constraint fk_field_dataset_model_id foreign key (project_id, dataset_model_id) references dataset_model (project_id, dataset_model_id),
    add constraint fk_field_field_model_id foreign key (project_id, field_model_id) references field_model (project_id, field_model_id);

alter table field_audit
	add constraint fk_field_audit_object_fk foreign key (audit_object_fk) references field (pk),
	add constraint fk_field_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_field_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_field_audit_robot_fk foreign key (audit_robot_fk) references robot (pk),
	add constraint fk_field_audit_dataset_model_id foreign key (project_id, dataset_model_id) references dataset_model (project_id, dataset_model_id),
	add constraint fk_field_audit_field_model_id foreign key (project_id, field_model_id) references field_model (project_id, field_model_id);

alter table field_model
    add constraint fk_field_model_project foreign key (project_id) references project (project_id),
    add constraint fk_field_model_dataset foreign key (project_id, dataset_model_id) references dataset_model (project_id, dataset_model_id);

alter table field_model_workflow
    add constraint fk_field_model_wf_field foreign key (project_id, field_model_id) references field_model (project_id, field_model_id),
    add constraint fk_field_model_wf_workflow foreign key (project_id, workflow_id) references workflow (project_id, workflow_id);

alter table field_possible_value
    add constraint fk_possible_value_field foreign key (project_id, field_model_id) references field_model (project_id, field_model_id);

alter table field_model_validator
    add constraint fk_field_model_validator_field foreign key (project_id, field_model_id) references field_model (project_id, field_model_id),
    add constraint fk_field_model_validator_validator foreign key (project_id, validator_id) references validator (project_id, validator_id);

/* form */
alter table form
	add constraint fk_form_scope_fk foreign key (scope_fk) references scope (pk),
	add constraint fk_form_event_fk foreign key (event_fk) references event (pk),
	add constraint fk_form_form_model_id foreign key (project_id, form_model_id) references form_model (project_id, form_model_id);

alter table form_audit
	add constraint fk_form_audit_object_fk foreign key (audit_object_fk) references form (pk),
	add constraint fk_form_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_form_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_form_audit_robot_fk foreign key (audit_robot_fk) references robot (pk),
	add constraint fk_form_audit_form_model_id foreign key (project_id, form_model_id) references form_model (project_id, form_model_id);

alter table form_model
    add constraint fk_form_model_project foreign key (project_id) references project (project_id);

alter table form_model_workflow
    add constraint fk_form_model_wf_form foreign key (project_id, form_model_id) references form_model (project_id, form_model_id),
    add constraint fk_form_model_wf_workflow foreign key (project_id, workflow_id) references workflow (project_id, workflow_id);

alter table form_layout
    add constraint fk_form_layout_form foreign key (project_id, form_model_id) references form_model (project_id, form_model_id),
    add constraint fk_form_layout_dataset foreign key (project_id, dataset_model_id) references dataset_model (project_id, dataset_model_id),
    add constraint fk_form_layout_sort_field foreign key (project_id, default_sort_field_model_id) references field_model (project_id, field_model_id);

alter table form_layout_column
    add constraint fk_form_layout_col_layout foreign key (project_id, form_model_id, form_layout_id) references form_layout (project_id, form_model_id, form_layout_id);

alter table form_layout_line
    add constraint fk_form_layout_line_layout foreign key (project_id, form_model_id, form_layout_id) references form_layout (project_id, form_model_id, form_layout_id);

alter table form_layout_cell
    add constraint fk_form_layout_cell_line foreign key (project_id, form_model_id, form_layout_id, form_layout_line_id) references form_layout_line (project_id, form_model_id, form_layout_id, form_layout_line_id),
    add constraint fk_form_layout_cell_field foreign key (project_id, field_model_id) references field_model (project_id, field_model_id),
    add constraint fk_form_layout_cell_dataset foreign key (project_id, dataset_model_id) references dataset_model (project_id, dataset_model_id);

alter table form_cell_visibility_criteria
    add constraint fk_form_cell_vis_crit_cell foreign key (project_id, form_layout_cell_id) references form_layout_cell (project_id, form_layout_cell_id);

alter table form_cell_visibility_criteria_target_cell
    add constraint fk_fcvc_tc_rule foreign key (project_id, form_layout_cell_id, form_cell_visible_criteria_id) references form_cell_visibility_criteria (project_id, form_layout_cell_id, form_cell_visible_criteria_id);

alter table form_cell_visibility_criteria_target_layout
    add constraint fk_fcvc_tl_rule foreign key (project_id, form_layout_cell_id, form_cell_visible_criteria_id) references form_cell_visibility_criteria (project_id, form_layout_cell_id, form_cell_visible_criteria_id);

alter table form_cell_visibility_criteria_value
    add constraint fk_fc_vis_criteria_value foreign key (project_id, form_layout_cell_id, form_cell_visible_criteria_id) references form_cell_visibility_criteria (project_id, form_layout_cell_id, form_cell_visible_criteria_id),
    add constraint fk_fc_vis_criteria_pos_value foreign key (project_id, possible_value_id) references field_possible_value (project_id, possible_value_id);

/* workflow */
alter table workflow_status
	add constraint fk_workflow_status_scope_fk foreign key (scope_fk) references scope (pk),
	add constraint fk_workflow_status_event_fk foreign key (event_fk) references event (pk),
	add constraint fk_workflow_status_form_fk foreign key (form_fk) references form (pk),
	add constraint fk_workflow_status_field_fk foreign key (field_fk) references field (pk),
	add constraint fk_workflow_status_user_fk foreign key (user_fk) references user (pk),
	add constraint fk_workflow_status_robot_fk foreign key (robot_fk) references robot (pk),
	add constraint fk_wf_status_workflow_id foreign key (project_id, workflow_id) references workflow (project_id, workflow_id),
	add constraint fk_wf_status_workflow_state_id foreign key (project_id, workflow_state_id) references workflow_state (project_id, workflow_state_id);

alter table workflow_status_audit
	add constraint fk_workflow_status_audit_object_fk foreign key (audit_object_fk) references workflow_status (pk),
	add constraint fk_workflow_status_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_workflow_status_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_workflow_status_audit_robot_fk foreign key (audit_robot_fk) references robot (pk),
	add constraint fk_wf_status_audit_workflow_id foreign key (project_id, workflow_id) references workflow (project_id, workflow_id),
	add constraint fk_wf_status_audit_workflow_state_id foreign key (project_id, workflow_state_id) references workflow_state (project_id, workflow_state_id);

alter table workflow
    add constraint fk_workflow_project foreign key (project_id) references project (project_id);

alter table workflow_state
    add constraint fk_workflow_state_project foreign key (project_id) references project (project_id),
    add constraint fk_wf_state_workflow foreign key (project_id, workflow_id) references workflow (project_id, workflow_id),
    add constraint fk_wf_state_aggregate_state foreign key (project_id, aggregate_state_id) references workflow_state (project_id, workflow_state_id);

alter table workflow_state_possible_action
    add constraint fk_wf_state_possible_action_state foreign key (project_id, workflow_state_id) references workflow_state (project_id, workflow_state_id),
    add constraint fk_wf_state_possible_action_action foreign key (project_id, workflow_action_id) references workflow_action (project_id, workflow_action_id);

alter table workflow_action
    add constraint fk_workflow_action_project foreign key (project_id) references project (project_id),
    add constraint fk_wf_action_workflow foreign key (project_id, workflow_id) references workflow (project_id, workflow_id);

/* payment */
alter table payment
	add constraint fk_payment_payment_batch_fk foreign key (payment_batch_fk) references payment_batch (pk),
	add constraint fk_payment_workflow_status_fk foreign key (workflow_status_fk) references workflow_status (pk);

alter table payment_target add constraint fk_payment_target_payment_fk foreign key (payment_fk) references payment (pk);

alter table payment_plan
    add constraint fk_payment_plan_workflow foreign key (project_id, workflow_id) references workflow (project_id, workflow_id),
    add constraint fk_payment_plan_scope_model foreign key (project_id, invoiced_scope_model_id) references scope_model (project_id, scope_model_id);

alter table payment_step
    add constraint fk_payment_step_plan foreign key (project_id, payment_plan_id) references payment_plan (project_id, payment_plan_id),
    add constraint fk_payment_step_event_model foreign key (project_id, event_model_id) references event_model (project_id, event_model_id);

alter table payment_step_distribution
    add constraint fk_payment_step_dist_step foreign key (project_id, payment_step_id) references payment_step (project_id, payment_step_id),
    add constraint fk_payment_step_dist_scope_model foreign key (project_id, scope_model_id) references scope_model (project_id, scope_model_id),
    add constraint fk_payment_step_dist_profile foreign key (project_id, profile_id) references profile (project_id, profile_id);

/* role */
alter table role
	add constraint fk_role_user_fk foreign key (user_fk) references user (pk),
	add constraint fk_role_robot_fk foreign key (robot_fk) references robot (pk),
	add constraint fk_role_scope_fk foreign key (scope_fk) references scope (pk),
	add constraint fk_role_profile_id foreign key (project_id, profile_id) references profile (project_id, profile_id);

alter table role_audit
	add constraint fk_role_audit_audit_object_fk foreign key (audit_object_fk) references role (pk),
	add constraint fk_role_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_role_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_role_audit_robot_fk foreign key (audit_robot_fk) references robot (pk),
	add constraint fk_role_audit_profile_id foreign key (project_id, profile_id) references profile (project_id, profile_id);

/* user */
alter table user_audit
	add constraint fk_user_audit_audit_object_fk foreign key (audit_object_fk) references user (pk),
	add constraint fk_user_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_user_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_user_audit_robot_fk foreign key (audit_robot_fk) references robot (pk);

alter table user_session
    add constraint fk_user_session_user_fk foreign key (user_fk) references user (pk);

/* mail */
alter table mail add constraint fk_mail_project foreign key (project_id) references project (project_id);

alter table mail_attachment
    add constraint fk_mail_attachment_mail_fk foreign key (mail_fk) references mail (pk),
    add constraint fk_mail_attachment_project foreign key (project_id) references project (project_id);

/* resource */
alter table resource
	add constraint fk_resource_scope_fk foreign key (scope_fk) references scope (pk),
	add constraint fk_resource_user_fk foreign key (user_fk) references user (pk),
	add constraint fk_resource_category foreign key (project_id, category_id) references resource_category (project_id, category_id);

/* robot */
alter table robot add constraint fk_robot_project foreign key (project_id) references project (project_id);

alter table robot_audit
	add constraint fk_robot_audit_audit_object_fk foreign key (audit_object_fk) references robot (pk),
	add constraint fk_robot_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_robot_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_robot_audit_robot_fk foreign key (audit_robot_fk) references robot (pk),
	add constraint fk_robot_audit_project foreign key (project_id) references project (project_id);

/* file */
alter table file
	add constraint fk_file_scope_fk foreign key (scope_fk) references scope (pk),
	add constraint fk_file_dataset_fk foreign key (dataset_fk) references dataset (pk),
	add constraint fk_file_event_fk foreign key (event_fk) references event (pk),
	add constraint fk_file_field_fk foreign key (field_fk) references field (pk),
	add constraint fk_file_user_fk foreign key (user_fk) references user (pk);

alter table audit_action
	add constraint fk_audit_action_user_fk foreign key (user_fk) references user (pk),
	add constraint fk_audit_action_robot_fk foreign key (robot_fk) references robot (pk),
	add constraint fk_audit_action_project foreign key (project_id) references project (project_id);

alter table internal_patch
    add constraint fk_internal_patch_project foreign key (project_id) references project (project_id);

/* profile */
alter table profile
    add constraint fk_profile_project foreign key (project_id) references project (project_id);

alter table profile_profile_rights
    add constraint fk_profile_profile_rights_profile foreign key (project_id, profile_id) references profile (project_id, profile_id),
    add constraint fk_profile_profile_rights_target foreign key (project_id, target_profile_id) references profile (project_id, profile_id);

alter table profile_dataset_model_rights
    add constraint fk_profile_dataset_model_rights_profile foreign key (project_id, profile_id) references profile (project_id, profile_id),
    add constraint fk_profile_dataset_model_rights_dataset_model foreign key (project_id, dataset_model_id) references dataset_model (project_id, dataset_model_id);

alter table profile_scope_model_rights
    add constraint fk_profile_scope_model_rights_profile foreign key (project_id, profile_id) references profile (project_id, profile_id),
    add constraint fk_profile_scope_model_rights_scope_model foreign key (project_id, scope_model_id) references scope_model (project_id, scope_model_id);

alter table profile_payment_model_rights
    add constraint fk_profile_payment_model_rights_profile foreign key (project_id, profile_id) references profile (project_id, profile_id),
    add constraint fk_profile_payment_model_rights_pay_plan foreign key (project_id, payment_plan_id) references payment_plan (project_id, payment_plan_id);

alter table profile_event_model_rights
    add constraint fk_profile_event_model_rights_profile foreign key (project_id, profile_id) references profile (project_id, profile_id),
    add constraint fk_profile_event_model_rights_event_model foreign key (project_id, event_model_id) references event_model (project_id, event_model_id);

alter table profile_form_model_rights
    add constraint fk_profile_form_model_rights_profile foreign key (project_id, profile_id) references profile (project_id, profile_id),
    add constraint fk_profile_form_model_rights_form_model foreign key (project_id, form_model_id) references form_model (project_id, form_model_id);

alter table profile_workflow_rights
    add constraint fk_profile_wf_rights_profile foreign key (project_id, profile_id) references profile (project_id, profile_id),
    add constraint fk_profile_wf_rights_workflow foreign key (project_id, workflow_id) references workflow (project_id, workflow_id);

alter table profile_workflow_action_rights
    add constraint fk_profile_wf_action_rights_profile foreign key (project_id, profile_id) references profile (project_id, profile_id),
    add constraint fk_profile_wf_action_rights_workflow_action foreign key (project_id, workflow_action_id) references workflow_action (project_id, workflow_action_id);

alter table profile_feature_grants
    add constraint fk_profile_feature_grants_profile foreign key (project_id, profile_id) references profile (project_id, profile_id),
    add constraint fk_profile_feature_grants_feature foreign key (project_id, feature_id) references feature (project_id, feature_id);

alter table profile_menu_grants
    add constraint fk_profile_menu_grants_profile foreign key (project_id, profile_id) references profile (project_id, profile_id),
    add constraint fk_profile_menu_grants_menu foreign key (project_id, menu_id) references menu (project_id, menu_id);

alter table profile_category_grants
    add constraint fk_profile_category_grants_profile foreign key (project_id, profile_id) references profile (project_id, profile_id),
    add constraint fk_profile_category_grants_category foreign key (project_id, category_id) references resource_category (project_id, category_id);

alter table profile_timeline_graph_grants
    add constraint fk_profile_timeline_graph_grants_profile foreign key (project_id, profile_id) references profile (project_id, profile_id),
    add constraint fk_profile_timeline_graph_grants_timeline_graph foreign key (project_id, timeline_graph_id) references timeline_graph (project_id, timeline_graph_id);

alter table profile_report_grants
    add constraint fk_profile_report_grants_profile foreign key (project_id, profile_id) references profile (project_id, profile_id),
    add constraint fk_profile_report_grants_report foreign key (project_id, report_id) references report (project_id, report_id);

/* validator */
alter table validator
    add constraint fk_validator_workflow foreign key (project_id, workflow_id) references workflow (project_id, workflow_id),
    add constraint fk_validator_invalid_state foreign key (project_id, invalid_state_id) references workflow_state (project_id, workflow_state_id),
    add constraint fk_validator_valid_state foreign key (project_id, valid_state_id) references workflow_state (project_id, workflow_state_id);

/* workflow summary */
alter table workflow_summary
    add constraint fk_wf_summary_leaf_scope_model foreign key (project_id, leaf_scope_model_id) references scope_model (project_id, scope_model_id);

alter table workflow_summary_workflow
    add constraint fk_wf_summary_workflow_summary foreign key (project_id, workflow_summary_id) references workflow_summary (project_id, workflow_summary_id),
    add constraint fk_wf_summary_workflow_workflow foreign key (project_id, workflow_id) references workflow (project_id, workflow_id);

alter table workflow_summary_filter_event_model
    add constraint fk_wf_summary_filter_event_model_summary foreign key (project_id, workflow_summary_id) references workflow_summary (project_id, workflow_summary_id),
    add constraint fk_wf_summary_filter_event_model foreign key (project_id, event_model_id) references event_model (project_id, event_model_id);

alter table workflow_summary_column
    add constraint fk_wf_summary_col_summary foreign key (project_id, workflow_summary_id) references workflow_summary (project_id, workflow_summary_id);

alter table workflow_summary_column_state
    add constraint fk_wf_summary_col_state_column foreign key (project_id, summary_column_id) references workflow_summary_column (project_id, summary_column_id),
    add constraint fk_wf_summary_col_state_workflow_state foreign key (project_id, workflow_state_id) references workflow_state (project_id, workflow_state_id);

/* chart */
alter table chart
    add constraint fk_chart_project foreign key (project_id) references project (project_id),
    add constraint fk_chart_workflow foreign key (project_id, workflow_id) references workflow (project_id, workflow_id),
    add constraint fk_chart_scope_model foreign key (project_id, scope_model_id) references scope_model (project_id, scope_model_id),
    add constraint fk_chart_leaf_scope_model foreign key (project_id, leaf_scope_model_id) references scope_model (project_id, scope_model_id),
    add constraint fk_chart_dataset_model foreign key (project_id, dataset_model_id) references dataset_model (project_id, dataset_model_id),
    add constraint fk_chart_field_model foreign key (project_id, field_model_id) references field_model (project_id, field_model_id);

alter table chart_color
    add constraint fk_chart_color_chart foreign key (project_id, chart_id) references chart (project_id, chart_id);

alter table chart_range
    add constraint fk_chart_range_chart foreign key (project_id, chart_id) references chart (project_id, chart_id);

alter table chart_state_filter
    add constraint fk_chart_state_filter_chart foreign key (project_id, chart_id) references chart (project_id, chart_id),
    add constraint fk_chart_state_filter_workflow_state foreign key (project_id, workflow_state_id) references workflow_state (project_id, workflow_state_id);

/* workflow widget */
alter table workflow_widget_state_selector
    add constraint fk_wf_widget_state_selector_widget foreign key (project_id, workflow_widget_id) references workflow_widget (project_id, workflow_widget_id),
    add constraint fk_wf_widget_state_selector_workflow foreign key (project_id, workflow_id) references workflow (project_id, workflow_id),
    add constraint fk_wf_widget_state_selector_wf_state foreign key (project_id, workflow_state_id) references workflow_state (project_id, workflow_state_id);

alter table workflow_widget_column
    add constraint fk_wf_widget_col_widget foreign key (project_id, workflow_widget_id) references workflow_widget (project_id, workflow_widget_id);

/* menu */
alter table menu
    add constraint fk_menu_parent foreign key (project_id, parent_menu_id) references menu (project_id, menu_id) on delete cascade;

alter table menu_action
    add constraint fk_menu_action_menu foreign key (project_id, menu_id) references menu (project_id, menu_id);

alter table menu_layout_section
    add constraint fk_menu_layout_section_menu foreign key (project_id, menu_id) references menu (project_id, menu_id),
    add constraint fk_menu_layout_section_feature foreign key (project_id, required_feature_id) references feature (project_id, feature_id);

alter table menu_layout_section_widget
    add constraint fk_menu_layout_section_widget_section foreign key (project_id, menu_id, menu_section_id) references menu_layout_section (project_id, menu_id, menu_section_id),
    add constraint fk_menu_layout_section_widget_feature foreign key (project_id, required_feature_id) references feature (project_id, feature_id);

alter table menu_layout_section_widget_parameter
    add constraint fk_menu_layout_section_widget_parameter_widget foreign key (project_id, menu_id, menu_section_id, menu_widget_id) references menu_layout_section_widget (project_id, menu_id, menu_section_id, menu_widget_id),
    add constraint fk_menu_layout_section_widget_parameter_scope foreign key (project_id, scope_model_id) references scope_model (project_id, scope_model_id),
    add constraint fk_menu_layout_section_widget_parameter_wf_widget foreign key (project_id, workflow_widget_id) references workflow_widget (project_id, workflow_widget_id),
    add constraint fk_menu_layout_section_widget_parameter_wf_summary foreign key (project_id, workflow_summary_id) references workflow_summary (project_id, workflow_summary_id),
    add constraint fk_menu_layout_section_widget_parameter_chart foreign key (project_id, chart_id) references chart (project_id, chart_id),
    add constraint fk_menu_layout_section_widget_parameter_category foreign key (project_id, category_id) references resource_category (project_id, category_id);

/* privacy policy */
alter table privacy_policy_profile
    add constraint fk_privacy_policy_profile foreign key (project_id, policy_id) references privacy_policy (project_id, policy_id),
    add constraint fk_privacy_policy_profile_profile foreign key (project_id, profile_id) references profile (project_id, profile_id);

/* report */
alter table report
    add constraint fk_report_dataset_model foreign key (project_id, dataset_model_id) references dataset_model (project_id, dataset_model_id),
    add constraint fk_report_workflow foreign key (project_id, workflow_id) references workflow (project_id, workflow_id);

alter table report_field
    add constraint fk_report_field_report foreign key (project_id, report_id) references report (project_id, report_id),
    add constraint fk_report_field_field_model foreign key (project_id, field_model_id) references field_model (project_id, field_model_id);

/* timeline graph */
alter table timeline_graph
    add constraint fk_timeline_graph_scope_model foreign key (project_id, scope_model_id) references scope_model (project_id, scope_model_id),
    add constraint fk_timeline_graph_event_model foreign key (project_id, study_start_event_model_id) references event_model (project_id, event_model_id);

alter table timeline_graph_section
    add constraint fk_timeline_graph_section_graph foreign key (project_id, timeline_graph_id) references timeline_graph (project_id, timeline_graph_id),
    add constraint fk_timeline_graph_section_dataset foreign key (project_id, dataset_model_id) references dataset_model (project_id, dataset_model_id),
    add constraint fk_timeline_graph_section_date_field foreign key (project_id, date_field_id) references field_model (project_id, field_model_id),
    add constraint fk_timeline_graph_section_end_date_field foreign key (project_id, end_date_field_id) references field_model (project_id, field_model_id),
    add constraint fk_timeline_graph_section_label_field foreign key (project_id, label_field_id) references field_model (project_id, field_model_id),
    add constraint fk_timeline_graph_section_value_field foreign key (project_id, value_field_id) references field_model (project_id, field_model_id);

alter table timeline_graph_section_meta_field
    add constraint fk_timeline_graph_section_meta_field_graph foreign key (project_id, timeline_graph_id, graph_section_id) references timeline_graph_section (project_id, timeline_graph_id, graph_section_id),
    add constraint fk_timeline_graph_section_meta_field_field foreign key (project_id, field_model_id) references field_model (project_id, field_model_id);

alter table timeline_graph_section_event
    add constraint fk_timeline_graph_section_event_section foreign key (project_id, timeline_graph_id, graph_section_id) references timeline_graph_section (project_id, timeline_graph_id, graph_section_id),
    add constraint fk_timeline_graph_section_event_model foreign key (project_id, event_model_id) references event_model (project_id, event_model_id);

alter table timeline_graph_section_reference
    add constraint fk_timeline_graph_section_ref_section foreign key (project_id, timeline_graph_id, graph_section_id) references timeline_graph_section (project_id, timeline_graph_id, graph_section_id),
    add constraint fk_timeline_graph_section_ref_ref_section foreign key (project_id, timeline_graph_id, reference_section_id) references timeline_graph_section (project_id, timeline_graph_id, graph_section_id);

alter table timeline_graph_section_reference_entry
    add constraint fk_timeline_graph_section_ref_entry foreign key (project_id, timeline_graph_id, graph_section_id, graph_reference_id) references timeline_graph_section_reference (project_id, timeline_graph_id, graph_section_id, graph_reference_id);

/* rule */
alter table rule_action
    add constraint fk_rule_action_rule foreign key (project_id, rule_id) references rule (project_id, rule_id);

alter table rule_action_parameter
    add constraint fk_rule_action_param_action foreign key (project_id, rule_action_id) references rule_action (project_id, rule_action_id);

/* selection */
alter table selection_node
    add constraint fk_selection_node_parent foreign key (project_id, parent_selection_id) references selection_node (project_id, selection_id);

/* rule constraint */
alter table rule_condition_list
    add constraint fk_rule_constraint_condition_list foreign key (project_id, constraint_id) references rule_constraint (project_id, constraint_id);

alter table rule_condition
    add constraint fk_rule_constraint_condition_list_condition foreign key (project_id, condition_list_id) references rule_condition_list (project_id, condition_list_id),
    add constraint fk_constraint_condition_list_parent_condition foreign key (project_id, parent_condition_id) references rule_condition (project_id, condition_id);

alter table rule_criterion
    add constraint fk_constraint_condition_criterion foreign key (project_id, condition_id) references rule_condition (project_id, condition_id);

alter table rule_criterion_value
    add constraint fk_constraint_condition_criterion_value foreign key (project_id, criterion_id) references rule_criterion (project_id, criterion_id);

