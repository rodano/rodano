package ch.rodano.core.model.user;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserClient implements Comparable<UserClient> {
	public static final int VALIDITY_DAYS = 30;
	// public static long VALIDITY_TIME = 1000l * 60l * 60l * 24l * 30l; //valid 30 days

	private final String key;
	private final String host;
	private final String agent;
	private final ZonedDateTime authorization;
	private ZonedDateTime lastUse;

	public UserClient(final String host, final String agent) {
		key = UUID.randomUUID().toString();
		this.host = host;
		this.agent = agent;
		final var now = ZonedDateTime.now();
		authorization = now;
		lastUse = now;
	}

	public UserClient(
		@JsonProperty("key") final String key,
		@JsonProperty("host") final String host,
		@JsonProperty("agent") final String agent,
		@JsonProperty("authorization") final ZonedDateTime authorization,
		@JsonProperty("lastUse") final ZonedDateTime lastUse
	) {
		this.key = key;
		this.host = host;
		this.agent = agent;
		this.authorization = authorization;
		this.lastUse = lastUse;
	}

	@JsonIgnore
	public boolean isValid() {
		final var now = ZonedDateTime.now();
		lastUse = now;
		return ChronoUnit.DAYS.between(now, authorization) < VALIDITY_DAYS;
	}

	public final String getKey() {
		return key;
	}

	public final ZonedDateTime getAuthorization() {
		return authorization;
	}

	public final String getHost() {
		return host;
	}

	public final String getAgent() {
		return agent;
	}

	public final ZonedDateTime getLastUse() {
		return lastUse;
	}

	public final void setLastUse(final ZonedDateTime lastUse) {
		this.lastUse = lastUse;
	}

	@Override
	public int compareTo(final UserClient o) {
		return authorization.compareTo(o.getAuthorization());
	}
}
