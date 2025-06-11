package ch.rodano.configuration.model.changelog;

import java.util.Date;

public class Changelog implements Comparable<Changelog> {

	public Date date;
	public String user;
	public String message;

	@Override
	public int compareTo(final Changelog changelog) {
		return date.compareTo(changelog.date);
	}
}
