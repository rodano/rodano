package ch.rodano.core.model.enrollment;

import java.io.Serializable;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class EnrollmentTarget implements Comparable<EnrollmentTarget>, Serializable {
	private static final long serialVersionUID = -8726806535623074346L;

	private ZonedDateTime date;
	private Integer expectedNumber;

	public ZonedDateTime getDate() {
		return date;
	}

	public void setDate(final ZonedDateTime date) {
		this.date = date;
	}

	public Integer getExpectedNumber() {
		return expectedNumber;
	}

	public void setExpectedNumber(final Integer number) {
		expectedNumber = number;
	}

	@Override
	@JsonIgnore
	public int compareTo(final EnrollmentTarget o) {
		return getDate().compareTo(o.getDate());
	}

	@JsonIgnore
	public boolean isBetweenDate(final ZonedDateTime startDate, final ZonedDateTime stopDate) {
		return date.isAfter(startDate) && date.isBefore(stopDate);
	}

	@Override
	public boolean equals(final Object o) {
		if(this == o) {
			return true;
		}
		if(o == null || getClass() != o.getClass()) {
			return false;
		}
		final var other = (EnrollmentTarget) o;

		return new EqualsBuilder()
			.append(date, other.date)
			.append(expectedNumber, other.expectedNumber)
			.isEquals();
	}
}
