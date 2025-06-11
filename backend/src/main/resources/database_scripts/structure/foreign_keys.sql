/* scope */
alter table scope_audit
	add constraint fk_scope_audit_object_fk foreign key (audit_object_fk) references scope (pk),
	add constraint fk_scope_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_scope_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_scope_audit_robot_fk foreign key (audit_robot_fk) references robot (pk);

alter table scope_relation
	add constraint fk_scope_relation_scope_fk foreign key (scope_fk) references scope (pk),
	add constraint fk_scope_relation_parent_fk foreign key (parent_fk) references scope (pk);

/* event */
alter table event add constraint fk_event_scope_fk foreign key (scope_fk) references scope (pk);

alter table event_audit
	add constraint fk_event_audit_object_fk foreign key (audit_object_fk) references event (pk),
	add constraint fk_event_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_event_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_event_audit_robot_fk foreign key (audit_robot_fk) references robot (pk);

/* dataset */
alter table dataset
	add constraint fk_dataset_scope_fk foreign key (scope_fk) references scope (pk),
	add constraint fk_dataset_event_fk foreign key (event_fk) references event (pk);

alter table dataset_audit
	add constraint fk_dataset_audit_object_fk foreign key (audit_object_fk) references dataset (pk),
	add constraint fk_dataset_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_dataset_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_dataset_audit_robot_fk foreign key (audit_robot_fk) references robot (pk);

/* field */
alter table field add constraint fk_field_dataset_fk foreign key (dataset_fk) references dataset (pk);

alter table field_audit
	add constraint fk_field_audit_object_fk foreign key (audit_object_fk) references field (pk),
	add constraint fk_field_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_field_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_field_audit_robot_fk foreign key (audit_robot_fk) references robot (pk);

/* form */
alter table form
	add constraint fk_form_scope_fk foreign key (scope_fk) references scope (pk),
	add constraint fk_form_event_fk foreign key (event_fk) references event (pk);

alter table form_audit
	add constraint fk_form_audit_object_fk foreign key (audit_object_fk) references form (pk),
	add constraint fk_form_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_form_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_form_audit_robot_fk foreign key (audit_robot_fk) references robot (pk);

/* workflow status */
alter table workflow_status
	add constraint fk_workflow_status_scope_fk foreign key (scope_fk) references scope (pk),
	add constraint fk_workflow_status_event_fk foreign key (event_fk) references event (pk),
	add constraint fk_workflow_status_form_fk foreign key (form_fk) references form (pk),
	add constraint fk_workflow_status_field_fk foreign key (field_fk) references field (pk),
	add constraint fk_workflow_status_user_fk foreign key (user_fk) references user (pk),
	add constraint fk_workflow_status_robot_fk foreign key (robot_fk) references robot (pk);

alter table workflow_status_audit
	add constraint fk_workflow_status_audit_object_fk foreign key (audit_object_fk) references workflow_status (pk),
	add constraint fk_workflow_status_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_workflow_status_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_workflow_status_audit_robot_fk foreign key (audit_robot_fk) references robot (pk);

/* payment */
alter table payment
	add constraint fk_payment_payment_batch_fk foreign key (payment_batch_fk) references payment_batch (pk),
	add constraint fk_payment_workflow_status_fk foreign key (workflow_status_fk) references workflow_status (pk);

alter table payment_target add constraint fk_payment_target_payment_fk foreign key (payment_fk) references payment (pk);

/* role */
alter table role
	add constraint fk_role_user_fk foreign key (user_fk) references user (pk),
	add constraint fk_role_robot_fk foreign key (robot_fk) references robot (pk),
	add constraint fk_role_scope_fk foreign key (scope_fk) references scope (pk);

alter table role_audit
	add constraint fk_role_audit_audit_object_fk foreign key (audit_object_fk) references role (pk),
	add constraint fk_role_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_role_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_role_audit_robot_fk foreign key (audit_robot_fk) references robot (pk);

/* user */
alter table user_audit
	add constraint fk_user_audit_audit_object_fk foreign key (audit_object_fk) references user (pk),
	add constraint fk_user_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_user_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_user_audit_robot_fk foreign key (audit_robot_fk) references robot (pk);

alter table user_session add constraint fk_user_session_user_fk foreign key (user_fk) references user (pk);

/* mail */
alter table mail_attachment add constraint fk_mail_attachment_mail_fk foreign key (mail_fk) references mail (pk);

/* resource */
alter table resource
	add constraint fk_resource_scope_fk foreign key (scope_fk) references scope (pk),
	add constraint fk_resource_user_fk foreign key (user_fk) references user (pk);

/* robot */
alter table robot_audit
	add constraint fk_robot_audit_audit_object_fk foreign key (audit_object_fk) references robot (pk),
	add constraint fk_robot_trail_audit_action_fk foreign key (audit_action_fk) references audit_action (pk),
	add constraint fk_robot_audit_user_fk foreign key (audit_user_fk) references user (pk),
	add constraint fk_robot_audit_robot_fk foreign key (audit_robot_fk) references robot (pk);

/* file */
alter table file
	add constraint fk_file_scope_fk foreign key (scope_fk) references scope (pk),
	add constraint fk_file_dataset_fk foreign key (dataset_fk) references dataset (pk),
	add constraint fk_file_event_fk foreign key (event_fk) references event (pk),
	add constraint fk_file_field_fk foreign key (field_fk) references field (pk),
	add constraint fk_file_user_fk foreign key (user_fk) references user (pk);

alter table audit_action
	add constraint fk_audit_action_user_fk foreign key (user_fk) references user (pk),
	add constraint fk_audit_action_robot_fk foreign key (robot_fk) references robot (pk);

/* chart_color */
alter table chart_color add constraint fk_chart_color_chart_fk foreign key (chart_fk) references chart (pk);

/* chart_state */
alter table chart_state add constraint fk_chart_state_chart_fk foreign key (chart_fk) references chart (pk);

/* chart_category */
alter table chart_category add constraint fk_chart_category_chart_fk foreign key (chart_fk) references chart (pk);
