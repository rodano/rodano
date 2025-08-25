/* scope */
alter table scope add index idx_scope_scope_model_id (project_id, scope_model_id);

alter table scope_audit add index idx_scope_audit_scope_model_id (project_id, scope_model_id);

alter table scope_model_parent add index idx_scope_model_parent (project_id, parent_scope_model_id);
alter table scope_model_parent add index idx_scope_model_child (project_id, child_scope_model_id);

alter table scope_model_dataset_model add index idx_scope_model_dataset_model_scope (project_id, scope_model_id);
alter table scope_model_dataset_model add index idx_scope_model_dataset_model_dataset (project_id, dataset_model_id);

alter table scope_model_form_model add index idx_scope_model_form_model_scope (project_id, scope_model_id);
alter table scope_model_form_model add index idx_scope_model_form_model_form (project_id, form_model_id);

alter table scope_model_workflow add index idx_scope_model_wf_scope (project_id, scope_model_id);
alter table scope_model_workflow add index idx_scope_model_wf_workflow (project_id, workflow_id);

alter table scope_model_workflow_state_selector add index idx_scope_model_wf_state_sel_workflow (project_id, workflow_id);
alter table scope_model_workflow_state_selector add index idx_scope_model_wf_state_sel_state (project_id, workflow_state_id);

/* event */
alter table event add index idx_event_date (date);
alter table event add index idx_event_blocking (blocking);
alter table event add index idx_event_scope_fk (scope_fk);
alter table event add index idx_event_scope_model_id (project_id, scope_model_id);
alter table event add index idx_event_event_model_id (project_id, event_model_id);

alter table event_audit add index idx_event_audit_scope_model_id (project_id, scope_model_id);
alter table event_audit add index idx_event_audit_event_model_id (project_id, event_model_id);

alter table event_model add index idx_event_model_scope (project_id, scope_model_id);
alter table event_model add index idx_event_model_group (project_id, event_group_id);

alter table event_model_dataset_model add index idx_event_model_dataset_model_event (project_id, event_model_id);
alter table event_model_dataset_model add index idx_event_model_dataset_model_dataset (project_id, dataset_model_id);

alter table event_model_form_model add index idx_event_model_form_model_event (project_id, event_model_id);
alter table event_model_form_model add index idx_event_model_form_model_form (project_id, form_model_id);

alter table event_model_workflow add index idx_event_model_wf_event (project_id, event_model_id);
alter table event_model_workflow add index idx_event_model_wf_workflow (project_id, workflow_id);

alter table event_model_implied_event add index idx_event_model_impl_ev_event (project_id, event_model_id);
alter table event_model_implied_event add index idx_event_model_impl_ev_implied (project_id, implied_event_model_id);

alter table event_model_blocked_event add index idx_event_model_blocked_event (project_id, event_model_id);
alter table event_model_blocked_event add index idx_event_model_blocked_event_blocked (project_id, blocked_event_model_id);

alter table event_model_deadline_reference add index idx_event_model_deadline_ref_event (project_id, event_model_id);
alter table event_model_deadline_reference add index idx_event_model_deadline_ref_ref (project_id, reference_event_model_id);

alter table event_group add index idx_ev_group_scope (project_id, scope_model_id);

/* dataset */
alter table dataset add index idx_dataset_scope_fk (scope_fk);
alter table dataset add index idx_dataset_event_fk (event_fk);
alter table dataset add index idx_dataset_model_code (dataset_model_id);

alter table dataset_model add index idx_dm_project_family (project_id, family);
alter table dataset_model add index idx_dm_project_export (project_id, export_order);

/* field */
alter table field add index idx_field_dataset_fk (dataset_fk);
alter table field add index idx_field_field_model_id (project_id, field_model_id);
alter table field add index idx_field_dataset_model_id (project_id, dataset_model_id);

alter table field_audit add index idx_field_audit_field_model_id (project_id, field_model_id);
alter table field_audit add index idx_field_audit_dataset_model_id (project_id, dataset_model_id);

alter table field_model add index idx_field_model_project_dataset_model (project_id, dataset_model_id);
alter table field_model add index idx_field_model_order (project_id, dataset_model_id, export_order);

alter table field_possible_value add index idx_possible_value_field (project_id, field_model_id, sort_order);

alter table field_model_validator add index idx_field_model_validator_field (project_id, field_model_id);
alter table field_model_validator add index idx_field_model_validator_validator (project_id, validator_id);

/* form */
alter table form add index idx_form_scope_fk (scope_fk);
alter table form add index idx_form_event_fk (event_fk);
alter table form add index idx_form_form_model_id (project_id, form_model_id);

alter table form_audit add index idx_form_audit_form_model_id (project_id, form_model_id);

alter table form_layout add index idx_form_layout_form (project_id, form_model_id);
alter table form_layout add index idx_form_layout_dataset (project_id, dataset_model_id);
alter table form_layout add index idx_form_layout_field (project_id, default_sort_field_model_id);

alter table form_layout_cell add index idx_form_layout_cell_line (project_id, form_model_id, form_layout_id, form_layout_line_id);
alter table form_layout_cell add index idx_form_layout_cell_field (project_id, field_model_id);
alter table form_layout_cell add index idx_form_layout_cell_dataset (project_id, dataset_model_id);

/* workflow */
alter table workflow_status add index idx_workflow_status_workflow_id (project_id, workflow_id);
alter table workflow_status add index idx_workflow_status_state_id (project_id, workflow_state_id);
alter table workflow_status add index idx_wf_status_scope_fk (scope_fk);
alter table workflow_status add index idx_wf_status_event_fk (event_fk);
alter table workflow_status add index idx_wf_status_form_fk (form_fk);
alter table workflow_status add index idx_wf_status_field_fk (field_fk);

alter table workflow_status_audit add index idx_workflow_status_audit_workflow_id (project_id, workflow_id);
alter table workflow_status_audit add index idx_workflow_status_audit_state_id (project_id, workflow_state_id);

alter table workflow add index idx_workflow_project (project_id);
alter table workflow add index idx_workflow_aggregate (project_id, aggregate_workflow_id);

alter table workflow_state_possible_action add index idx_wf_state_possible_action_state (project_id, workflow_state_id);
alter table workflow_state_possible_action add index idx_wf_state_possible_action_action (project_id, workflow_action_id);

/* role */
alter table role add index idx_role_scope_fk (scope_fk);
alter table role add index idx_role_profile_id (project_id, profile_id);

alter table role_audit add index idx_role_audit_profile_id (project_id, profile_id);

/* user */
alter table user add index idx_user_name (name);
alter table user add index idx_user_email (email);

/* mail */
alter table mail add index idx_mail_status (status);
alter table mail add index idx_mail_origin (origin);
alter table mail add index idx_mail_sender (sender);
alter table mail add index idx_mail_subject (subject);
alter table mail add index idx_mail_project (project_id);

alter table mail_attachment add index idx_mail_attach_project (project_id);

/* deleted indices */
alter table dataset add index idx_dataset_deleted (deleted);
alter table form add index idx_form_deleted (deleted);
alter table payment add index idx_payment_deleted (deleted);
alter table payment_batch add index idx_payment_batch_deleted (deleted);
alter table payment_target add index idx_payment_target_deleted (deleted);
alter table resource add index idx_resource_deleted (deleted);
alter table robot add index idx_robot_deleted (deleted);
alter table scope add index idx_scope_deleted (deleted);
alter table user add index idx_user_deleted (deleted);
alter table event add index idx_event_deleted (deleted);
alter table workflow_status add index idx_workflow_status_deleted (deleted);

/* aggregate workflow states indices */
alter table workflow_status add index aggregate_scope (workflow_id, deleted, scope_fk);
alter table workflow_status add index aggregate_event (workflow_id, deleted, event_fk, form_fk, field_fk);

/* rule definition */
alter table rule_definition_action_parameter add index idx_rule_def_action_param_parent (project_id, rule_definition_action_id);

/* validator */
alter table validator add index idx_validator_workflow (project_id, workflow_id);

/* payment */
alter table payment add index idx_payment_pay_plan_id (project_id, payment_plan_id);

alter table payment_batch add index idx_payment_batch_pay_plan_id (project_id, payment_plan_id);

alter table payment_step add index idx_payment_step_plan (project_id, payment_plan_id, sort_order);
alter table payment_step add index idx_payment_step_event (project_id, event_model_id);

alter table payment_step_distribution add index idx_payment_step_dist_scope (project_id, scope_model_id);
alter table payment_step_distribution add index idx_payment_step_dist_profile (project_id, profile_id);

/* workflow summary */
alter table workflow_summary add index idx_wf_summary_project (project_id);

/* chart */
alter table chart add index idx_chart_project (project_id);

alter table chart_color add index idx_chart_color_chart (project_id, chart_id);

alter table chart_range add index idx_chart_range_sort (project_id, chart_id, sort_order);

alter table chart_state_filter add index idx_chart_state_filter_kind (project_id, chart_id, kind);

/* menu */
alter table menu_layout_section_widget add index idx_menu_widget_feature (project_id, required_feature_id);

/* timeline graph */
alter table timeline_graph add index idx_timeline_graph_scope (project_id, scope_model_id);
alter table timeline_graph add index idx_timeline_graph_start_event (project_id, study_start_event_model_id);

alter table timeline_graph_section add index idx_timeline_graph_section_dataset (project_id, dataset_model_id);
alter table timeline_graph_section add index idx_timeline_graph_section_date_field (project_id, date_field_id);
alter table timeline_graph_section add index idx_timeline_graph_section_end_date_field (project_id, end_date_field_id);
alter table timeline_graph_section add index idx_timeline_graph_section_label_field (project_id, label_field_id);
alter table timeline_graph_section add index idx_timeline_graph_section_value_field (project_id, value_field_id);

alter table timeline_graph_section_reference add index idx_timeline_graph_section_ref_owner (project_id, timeline_graph_id, graph_section_id);
alter table timeline_graph_section_reference add index idx_timeline_graph_section_ref_ref (project_id, timeline_graph_id, graph_section_id, reference_section_id);

/* rule */
alter table rule add index idx_rule_entity_type (project_id, entity_type);
alter table rule add index idx_rule_entity (project_id, entity_type, entity_id);

alter table rule_action add index idx_rule_action_rule (project_id, rule_id);

/* selection */
alter table selection_node add index idx_selection_node_parent (project_id, parent_selection_id);

/* rule constraint */
alter table rule_constraint add index idx_constraint_owner (project_id, owner_type, owner_id);

alter table rule_condition add index idx_constraint_condition_parent (project_id, condition_list_id, parent_condition_id);

/* resource */
alter table resource add index idx_resource_project_uuid (project_id, uuid);

/* audit action */
alter table audit_action add index idx_audit_action_project (project_id);

/* internal patch */
alter table internal_patch add index idx_internal_patch_project (project_id);

/* robot */
alter table robot add index idx_robot_project (project_id);

alter table robot_audit add index idx_robot_audit_project (project_id);

/* user */
alter table user add index idx_user_project (project_id);

alter table user_audit add index idx_user_audit_project (project_id);

alter table user_session add index idx_user_session_project (project_id);

