/*
 * This file is generated by jOOQ.
 */
package ch.rodano.core.model.jooq;


import ch.rodano.core.model.jooq.tables.Dataset;
import ch.rodano.core.model.jooq.tables.Event;
import ch.rodano.core.model.jooq.tables.Form;
import ch.rodano.core.model.jooq.tables.Mail;
import ch.rodano.core.model.jooq.tables.Payment;
import ch.rodano.core.model.jooq.tables.PaymentBatch;
import ch.rodano.core.model.jooq.tables.PaymentTarget;
import ch.rodano.core.model.jooq.tables.Resource;
import ch.rodano.core.model.jooq.tables.Robot;
import ch.rodano.core.model.jooq.tables.Role;
import ch.rodano.core.model.jooq.tables.Scope;
import ch.rodano.core.model.jooq.tables.User;
import ch.rodano.core.model.jooq.tables.WorkflowStatus;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling indexes of tables in the default schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Indexes {

	// -------------------------------------------------------------------------
	// INDEX definitions
	// -------------------------------------------------------------------------

	public static final Index WORKFLOW_STATUS_AGGREGATE_EVENT = Internal.createIndex(DSL.name("aggregate_event"), WorkflowStatus.WORKFLOW_STATUS, new OrderField[] { WorkflowStatus.WORKFLOW_STATUS.WORKFLOW_ID, WorkflowStatus.WORKFLOW_STATUS.DELETED, WorkflowStatus.WORKFLOW_STATUS.EVENT_FK, WorkflowStatus.WORKFLOW_STATUS.FORM_FK, WorkflowStatus.WORKFLOW_STATUS.FIELD_FK }, false);
	public static final Index WORKFLOW_STATUS_AGGREGATE_SCOPE = Internal.createIndex(DSL.name("aggregate_scope"), WorkflowStatus.WORKFLOW_STATUS, new OrderField[] { WorkflowStatus.WORKFLOW_STATUS.WORKFLOW_ID, WorkflowStatus.WORKFLOW_STATUS.DELETED, WorkflowStatus.WORKFLOW_STATUS.SCOPE_FK }, false);
	public static final Index DATASET_IDX_DATASET_DELETED = Internal.createIndex(DSL.name("idx_dataset_deleted"), Dataset.DATASET, new OrderField[] { Dataset.DATASET.DELETED }, false);
	public static final Index EVENT_IDX_EVENT_BLOCKING = Internal.createIndex(DSL.name("idx_event_blocking"), Event.EVENT, new OrderField[] { Event.EVENT.BLOCKING }, false);
	public static final Index EVENT_IDX_EVENT_DATE = Internal.createIndex(DSL.name("idx_event_date"), Event.EVENT, new OrderField[] { Event.EVENT.DATE }, false);
	public static final Index EVENT_IDX_EVENT_DELETED = Internal.createIndex(DSL.name("idx_event_deleted"), Event.EVENT, new OrderField[] { Event.EVENT.DELETED }, false);
	public static final Index EVENT_IDX_EVENT_EVENT_MODEL_ID = Internal.createIndex(DSL.name("idx_event_event_model_id"), Event.EVENT, new OrderField[] { Event.EVENT.EVENT_MODEL_ID }, false);
	public static final Index FORM_IDX_FORM_DELETED = Internal.createIndex(DSL.name("idx_form_deleted"), Form.FORM, new OrderField[] { Form.FORM.DELETED }, false);
	public static final Index MAIL_IDX_MAIL_ORIGIN = Internal.createIndex(DSL.name("idx_mail_origin"), Mail.MAIL, new OrderField[] { Mail.MAIL.ORIGIN }, false);
	public static final Index MAIL_IDX_MAIL_SENDER = Internal.createIndex(DSL.name("idx_mail_sender"), Mail.MAIL, new OrderField[] { Mail.MAIL.SENDER }, false);
	public static final Index MAIL_IDX_MAIL_STATUS = Internal.createIndex(DSL.name("idx_mail_status"), Mail.MAIL, new OrderField[] { Mail.MAIL.STATUS }, false);
	public static final Index MAIL_IDX_MAIL_SUBJECT = Internal.createIndex(DSL.name("idx_mail_subject"), Mail.MAIL, new OrderField[] { Mail.MAIL.SUBJECT }, false);
	public static final Index PAYMENT_BATCH_IDX_PAYMENT_BATCH_DELETED = Internal.createIndex(DSL.name("idx_payment_batch_deleted"), PaymentBatch.PAYMENT_BATCH, new OrderField[] { PaymentBatch.PAYMENT_BATCH.DELETED }, false);
	public static final Index PAYMENT_IDX_PAYMENT_DELETED = Internal.createIndex(DSL.name("idx_payment_deleted"), Payment.PAYMENT, new OrderField[] { Payment.PAYMENT.DELETED }, false);
	public static final Index PAYMENT_TARGET_IDX_PAYMENT_TARGET_DELETED = Internal.createIndex(DSL.name("idx_payment_target_deleted"), PaymentTarget.PAYMENT_TARGET, new OrderField[] { PaymentTarget.PAYMENT_TARGET.DELETED }, false);
	public static final Index RESOURCE_IDX_RESOURCE_DELETED = Internal.createIndex(DSL.name("idx_resource_deleted"), Resource.RESOURCE, new OrderField[] { Resource.RESOURCE.DELETED }, false);
	public static final Index ROBOT_IDX_ROBOT_DELETED = Internal.createIndex(DSL.name("idx_robot_deleted"), Robot.ROBOT, new OrderField[] { Robot.ROBOT.DELETED }, false);
	public static final Index ROLE_IDX_ROLE_PROFILE_ID = Internal.createIndex(DSL.name("idx_role_profile_id"), Role.ROLE, new OrderField[] { Role.ROLE.PROFILE_ID }, false);
	public static final Index SCOPE_IDX_SCOPE_DELETED = Internal.createIndex(DSL.name("idx_scope_deleted"), Scope.SCOPE, new OrderField[] { Scope.SCOPE.DELETED }, false);
	public static final Index USER_IDX_USER_DELETED = Internal.createIndex(DSL.name("idx_user_deleted"), User.USER, new OrderField[] { User.USER.DELETED }, false);
	public static final Index USER_IDX_USER_EMAIL = Internal.createIndex(DSL.name("idx_user_email"), User.USER, new OrderField[] { User.USER.EMAIL }, false);
	public static final Index USER_IDX_USER_NAME = Internal.createIndex(DSL.name("idx_user_name"), User.USER, new OrderField[] { User.USER.NAME }, false);
	public static final Index WORKFLOW_STATUS_IDX_WORKFLOW_STATUS_DELETED = Internal.createIndex(DSL.name("idx_workflow_status_deleted"), WorkflowStatus.WORKFLOW_STATUS, new OrderField[] { WorkflowStatus.WORKFLOW_STATUS.DELETED }, false);
	public static final Index WORKFLOW_STATUS_IDX_WORKFLOW_STATUS_STATE_ID = Internal.createIndex(DSL.name("idx_workflow_status_state_id"), WorkflowStatus.WORKFLOW_STATUS, new OrderField[] { WorkflowStatus.WORKFLOW_STATUS.STATE_ID }, false);
	public static final Index WORKFLOW_STATUS_IDX_WORKFLOW_STATUS_WORKFLOW_ID = Internal.createIndex(DSL.name("idx_workflow_status_workflow_id"), WorkflowStatus.WORKFLOW_STATUS, new OrderField[] { WorkflowStatus.WORKFLOW_STATUS.WORKFLOW_ID }, false);
}
