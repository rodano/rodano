package ch.rodano.configuration.model.cms;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import ch.rodano.configuration.model.common.Entity;

public class CMSComponentParameter {
	private Class<?> classname;
	private boolean mandatory;
	private Entity entity;
	private List<String> possibleValues;
	private boolean file;

	public CMSComponentParameter(final Class<?> classname) {
		this.classname = classname;
	}

	public Class<?> getClassname() {
		return classname;
	}

	public CMSComponentParameter setClassname(final Class<? extends Serializable> classname) {
		this.classname = classname;
		return this;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public CMSComponentParameter setMandatory(final boolean mandatory) {
		this.mandatory = mandatory;
		return this;
	}

	public boolean isFile() {
		return file;
	}

	public CMSComponentParameter setFile(final boolean file) {
		this.file = file;
		return this;
	}

	public CMSComponentParameter setEntity(final Entity entity) {
		this.entity = entity;
		return this;
	}

	public Entity getEntity() {
		return entity;
	}

	public List<String> getPossibleValues() {
		return possibleValues;
	}

	public CMSComponentParameter setPossibleValues(final List<String> possibleValues) {
		this.possibleValues = possibleValues;
		return this;
	}

	public CMSComponentParameter setPossibleValues(final String... possibleValues) {
		this.possibleValues = Arrays.asList(possibleValues);
		return this;
	}
}
