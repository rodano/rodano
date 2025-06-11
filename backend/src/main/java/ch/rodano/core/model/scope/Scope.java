package ch.rodano.core.model.scope;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import ch.rodano.configuration.model.payment.Payable;
import ch.rodano.configuration.model.payment.PayableModel;
import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.workflow.WorkflowableEntity;
import ch.rodano.configuration.model.workflow.WorkflowableModel;
import ch.rodano.configuration.utils.DisplayableUtils;
import ch.rodano.core.model.common.AuditableObject;
import ch.rodano.core.model.common.DeletableObject;
import ch.rodano.core.model.common.PersistentObject;
import ch.rodano.core.model.common.TimestampableObject;
import ch.rodano.core.model.enrollment.EnrollmentTarget;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.workflow.Workflowable;

public class Scope extends ScopeRecord implements DeletableObject, TimestampableObject, AuditableObject, PersistentObject, Comparable<Scope>, Workflowable, Payable, Evaluable {
	public static final Comparator<Scope> DEFAULT_COMPARATOR = Comparator
		.comparing(Scope::getScopeModel)
		.thenComparing(Scope::getCode)
		.thenComparing(Scope::getCreationTime)
		.thenComparing(Scope::getPk);

	private Long pk;
	protected ZonedDateTime creationTime;
	protected ZonedDateTime lastUpdateTime;

	private ScopeModel scopeModel;

	public Scope() {
		super();
	}

	@Override
	public Long getPk() {
		return pk;
	}

	@Override
	public void setPk(final Long pk) {
		this.pk = pk;
	}

	@Override
	public ZonedDateTime getCreationTime() {
		return creationTime;
	}

	@Override
	public void setCreationTime(final ZonedDateTime creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public ZonedDateTime getLastUpdateTime() {
		return lastUpdateTime;
	}

	@Override
	public void setLastUpdateTime(final ZonedDateTime lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public void setScopeModel(final ScopeModel scopeModel) {
		this.scopeModel = scopeModel;
		this.scopeModelId = scopeModel.getId();
	}

	public ScopeModel getScopeModel() {
		return scopeModel;
	}

	//Labels
	@Override
	public final String getLocalizedLongname(final String... language) {
		return getLongname();
	}

	@Override
	public final String getLocalizedShortname(final String... language) {
		return getShortname();
	}

	@Override
	public final String getLocalizedDescription(final String... languages) {
		return DisplayableUtils.getLocalizedMap(data.getDescription(), languages);
	}

	@Override
	public WorkflowableEntity getWorkflowableEntity() {
		return WorkflowableEntity.SCOPE;
	}

	public static String formatCodeAndShortname(final String code, final String shortname) {
		if(StringUtils.isBlank(code)) {
			return "Unknown";
		}
		if(code.equals(shortname)) {
			return code;
		}
		return String.format("%s (%s)", code, shortname);
	}

	public final String getCodeAndShortname() {
		return formatCodeAndShortname(getCode(), getShortname());
	}

	public boolean isClosed() {
		final var now = ZonedDateTime.now();
		return getStartDate() == null || getStartDate().isAfter(now) || getStopDate() != null && getStopDate().isBefore(now);
	}

	//Enrollment target
	private Boolean hasEnrollmentTargetForDate(final ZonedDateTime date) {
		return data.getEnrollmentTargets().stream()
			.anyMatch(target -> target.getDate().equals(date));
	}

	public final void updateEnrollmentTarget() {
		final var enrollmentStart = data.getEnrollmentStart();
		final var enrollmentStop = data.getEnrollmentStop();

		if(enrollmentStart == null || enrollmentStop == null) {
			return;
		}

		final var enrollmentTargets = data.getEnrollmentTargets();

		final var unusedTargets = enrollmentTargets.stream()
			.filter(target -> target.getDate() == null || !target.isBetweenDate(enrollmentStart, enrollmentStop))
			.toList();

		// clean enrollment targets
		enrollmentTargets.removeAll(unusedTargets);

		// fill targets
		var date = enrollmentStart;
		date = date.withMonth(1);
		date = date.truncatedTo(ChronoUnit.DAYS);
		while(date.isBefore(enrollmentStop) || date.equals(enrollmentStop)) {
			if(!hasEnrollmentTargetForDate(date)) {
				final var target = new EnrollmentTarget();
				target.setDate(date);
				enrollmentTargets.add(target);
			}
			date = date.plus(1, ChronoUnit.MONTHS);
		}
		// sort targets
		Collections.sort(enrollmentTargets);
		// fill map values
		final var notch = Float.valueOf(expectedNumber) / (enrollmentTargets.size() - 1);
		for(var i = 0; i < enrollmentTargets.size(); i++) {
			final var target = enrollmentTargets.get(i);
			if(i == enrollmentTargets.size() - 1) {
				target.setExpectedNumber(getExpectedNumber());
			}
			else if(target.getExpectedNumber() == null) {
				target.setExpectedNumber(Math.round(i * notch));
			}
		}
	}

	@Override
	public final WorkflowableModel getWorkflowableModel() {
		return getScopeModel();
	}

	@Override
	public final PayableModel getPayableModel() {
		return getScopeModel();
	}

	@Override
	public final String getPayableModelId() {
		return getScopeModelId();
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(final Object o) {
		if(o == null || getClass() != o.getClass()) {
			return false;
		}
		final var scope = (Scope) o;
		if(id == null && scope.id == null) {
			return this == o;
		}
		return Objects.equals(id, scope.id);
	}

	@Override
	public int compareTo(final Scope otherScope) {
		//preserve consistency between equals and comparator
		if(equals(otherScope)) {
			return 0;
		}
		return DEFAULT_COMPARATOR.compare(this, otherScope);
	}

	@Override
	public RulableEntity getRulableEntity() {
		return RulableEntity.SCOPE;
	}

	public boolean canEnroll() {
		final var now = ZonedDateTime.now();
		return startDate != null && !startDate.isAfter(now) && (stopDate == null || stopDate.isAfter(now));
	}

	@Override
	public void onPreUpdate() {
		//nothing
	}

	@Override
	public void onPostUpdate(final Study study) {
		//nothing
	}

	@Override
	public void onPostLoad(final Study study) {
		scopeModel = study.getScopeModel(scopeModelId);
	}
}
